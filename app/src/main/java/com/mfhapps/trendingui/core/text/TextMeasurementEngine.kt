package com.mfhapps.trendingui.core.text

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.LruCache
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.mfhapps.trendingui.native.PretextNativeGeometry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


data class TextSegment(
    val text: String,
    val widthPx: Float,
    val isEmoji: Boolean,
)

data class CachedFontMetrics(
    val ascent: Float,
    val descent: Float,
    val lineHeight: Float,
)


data class PreparedText(
    val originalText: String,
    val segments: List<TextSegment>,
    val fontMetrics: CachedFontMetrics,
    val breakableUnits: List<String>,
    val unitWidthsPx: FloatArray,
    val fontSizePx: Float = 16f,
    val typeface: Typeface = Typeface.DEFAULT,
    val typefaceHash: Int = 0,
    val letterSpacingPx: Float = 0f,
    val options: PrepareOptions = PrepareOptions(),
    val unitGraphemeFits: List<UnitGraphemeFit?> = emptyList(),
)

data class MeasuredTextLayout(
    val height: Int,
    val lineCount: Int,
    val lines: List<String>,
    val lineWidthsPx: List<Float> = emptyList(),
)


data class PositionedLine(
    val x: Float,
    val y: Float,
    val text: String,
    val width: Float,
)

data class PositionedTextLayout(
    val lines: List<PositionedLine>,
    val height: Int,

    val endUnitIndex: Int = 0,
)


data class NewspaperLayout(
    val headlineLines: List<PositionedLine>,
    val bodyLines: List<PositionedLine>,
    val columnCount: Int,
    val columnWidthPx: Float,
    val gutterPx: Float,
    val headlineHeightPx: Float,
    val endUnitIndex: Int,
)

data class CircleObstacle(
    val cx: Float,
    val cy: Float,
    val r: Float,
)

data class RectObstacle(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
)


data class PolygonObstacle(
    val points: List<Pair<Float, Float>>,
)

data class LayoutRegion(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

private data class Interval(val left: Float, val right: Float)


enum class TruncationMode { Tail, Head, Middle }


data class FitFontResult(
    val fontSizePx: Float,
    val prepared: PreparedText,
    val layout: MeasuredTextLayout,
)


data class TruncationResult(
    val text: String,
    val wasTruncated: Boolean,
    val visibleCharCount: Int,
)


data class EngineStats(
    val preparedCacheSize: Int,
    val widthsCacheSize: Int,
)


data class LayoutCursor(
    val segmentIndex: Int = 0,
    val graphemeIndex: Int = 0,
)


data class LayoutLineRange(
    val width: Float,
    val start: LayoutCursor,
    val end: LayoutCursor,
)


data class LayoutLine(
    val text: String,
    val width: Float,
    val start: LayoutCursor,
    val end: LayoutCursor,
)


data class LineStats(
    val lineCount: Int,
    val maxLineWidth: Float,
)

object TextMeasurementEngine {
    private const val EMOJI_WIDTH_FACTOR = 1.1f
    private val cache = LruCache<String, PreparedText>(500)


    private val unitWidthCache = LruCache<String, Float>(4_096)

    suspend fun prepare(
        text: String,
        fontSize: Float,
        typeface: Typeface,
        options: PrepareOptions = PrepareOptions(),
    ): PreparedText = withContext(Dispatchers.Default) {
        prepareSync(text, fontSize, typeface, options)
    }

    fun prepareSync(
        text: String,
        fontSize: Float,
        typeface: Typeface,
        options: PrepareOptions = PrepareOptions(),
    ): PreparedText {
        val key = cacheKey(text, fontSize, typeface, options)
        return cache.get(key) ?: buildPrepared(text, fontSize, typeface, options).also { cache.put(key, it) }
    }

    fun prepareWithSegments(
        text: String,
        fontSize: Float,
        typeface: Typeface,
        options: PrepareOptions = PrepareOptions(),
    ): PreparedText = prepareSync(text, fontSize, typeface, options)


    fun prepareBatch(
        texts: List<String>,
        fontSize: Float,
        typeface: Typeface,
    ): List<PreparedText> {
        if (texts.isEmpty()) return emptyList()
        val paint = makePaint(fontSize, typeface)
        return texts.map { text ->
            val key = cacheKey(text, fontSize, typeface, PrepareOptions())
            cache.get(key) ?: buildPreparedWithPaint(text, paint).also { cache.put(key, it) }
        }
    }


    fun prepareStreaming(
        previous: PreparedText,
        appendedText: String,
        fontSize: Float,
        typeface: Typeface,
    ): PreparedText {
        if (appendedText.isEmpty()) return previous
        val paint = makePaint(fontSize, typeface)
        val newUnits = PretextTextAnalysis.breakIntoUnits(appendedText, previous.options)
        val combinedUnits = ArrayList<String>(previous.breakableUnits.size + newUnits.size)
        combinedUnits.addAll(previous.breakableUnits)
        combinedUnits.addAll(newUnits)
        val widths = FloatArray(combinedUnits.size)
        System.arraycopy(previous.unitWidthsPx, 0, widths, 0, previous.unitWidthsPx.size)
        val tfHash = typeface.hashCode()
        val newFits = ArrayList<UnitGraphemeFit?>(newUnits.size)
        for ((i, unit) in newUnits.withIndex()) {
            val w = measureUnit(paint, unit, fontSize, tfHash)
            widths[previous.unitWidthsPx.size + i] = w
            newFits += graphemeFitForUnit(paint, unit, fontSize, tfHash, previous.letterSpacingPx)
        }
        val newSegments = previous.segments + newUnits.mapIndexed { i, unit ->
            TextSegment(unit, widths[previous.unitWidthsPx.size + i], isEmojiUnit(unit))
        }
        return PreparedText(
            originalText = previous.originalText + appendedText,
            segments = newSegments,
            fontMetrics = previous.fontMetrics,
            breakableUnits = combinedUnits,
            unitWidthsPx = widths,
            fontSizePx = previous.fontSizePx,
            typeface = previous.typeface,
            typefaceHash = previous.typefaceHash,
            letterSpacingPx = previous.letterSpacingPx,
            options = previous.options,
            unitGraphemeFits = previous.unitGraphemeFits + newFits,
        )
    }


    fun clearCaches() {
        cache.evictAll()
        unitWidthCache.evictAll()
    }


    fun clearCache() = clearCaches()

    fun stats(): EngineStats = EngineStats(cache.size(), unitWidthCache.size())


    fun layout(
        prepared: PreparedText,
        containerWidthPx: Int,
        lineHeightPx: Float,
        maxUnits: Int = prepared.breakableUnits.size,
    ): MeasuredTextLayout = layoutWithWidths(
        prepared = prepared,
        lineHeightPx = lineHeightPx,
        maxUnits = maxUnits,
        widthForLineIndex = { containerWidthPx.coerceAtLeast(1) },
    )


    fun measureNaturalWidth(prepared: PreparedText): Float =
        measureLineStats(prepared, Float.MAX_VALUE).maxLineWidth

    fun layoutNextLineRange(
        prepared: PreparedText,
        start: LayoutCursor,
        maxWidthPx: Float,
    ): LayoutLineRange? {
        val result = computeNextLine(prepared, start, maxWidthPx) ?: return null
        return LayoutLineRange(
            width = result.width,
            start = start,
            end = result.end,
        )
    }

    fun layoutNextLine(
        prepared: PreparedText,
        start: LayoutCursor,
        maxWidthPx: Float,
    ): LayoutLine? {
        val range = layoutNextLineRange(prepared, start, maxWidthPx) ?: return null
        return materializeLineRange(prepared, range)
    }

    fun materializeLineRange(prepared: PreparedText, range: LayoutLineRange): LayoutLine {
        val builder = StringBuilder()
        appendRangeText(builder, prepared, range.start, range.end)
        return LayoutLine(
            text = builder.toString().trimEnd(),
            width = range.width,
            start = range.start,
            end = range.end,
        )
    }

    fun walkLineRanges(
        prepared: PreparedText,
        maxWidthPx: Float,
        maxUnits: Int = prepared.breakableUnits.size,
        onLine: (LayoutLineRange) -> Unit,
    ): Int {
        val limit = maxUnits.coerceIn(0, prepared.breakableUnits.size)
        var cursor = LayoutCursor(0, 0)
        var count = 0
        while (cursor.segmentIndex < limit) {
            val range = layoutNextLineRange(prepared, cursor, maxWidthPx.coerceAtLeast(1f)) ?: break
            onLine(range)
            count++
            cursor = range.end
        }
        return count
    }

    fun measureLineStats(
        prepared: PreparedText,
        maxWidthPx: Float,
        maxUnits: Int = prepared.breakableUnits.size,
    ): LineStats {
        var maxLineWidth = 0f
        val lineCount = walkLineRanges(prepared, maxWidthPx, maxUnits) { line ->
            if (line.width > maxLineWidth) maxLineWidth = line.width
        }
        return LineStats(lineCount = lineCount, maxLineWidth = maxLineWidth)
    }

    fun layoutWithLines(
        prepared: PreparedText,
        containerWidthPx: Int,
        lineHeightPx: Float,
        maxUnits: Int = prepared.breakableUnits.size,
    ): MeasuredTextLayout {
        val lines = mutableListOf<String>()
        val lineWidths = mutableListOf<Float>()
        val count = walkLineRanges(
            prepared = prepared,
            maxWidthPx = containerWidthPx.toFloat(),
            maxUnits = maxUnits,
        ) { range ->
            val line = materializeLineRange(prepared, range)
            lines.add(line.text)
            lineWidths.add(line.width)
        }
        if (lines.isEmpty()) {
            lines.add("")
            lineWidths.add(0f)
        }
        return MeasuredTextLayout(
            height = ceil(count * lineHeightPx).toInt(),
            lineCount = count,
            lines = lines,
            lineWidthsPx = lineWidths,
        )
    }


    fun layoutDynamic(
        prepared: PreparedText,
        region: LayoutRegion,
        lineHeightPx: Float,
        startCursor: LayoutCursor = LayoutCursor(),
        maxWidthAtLineTop: (lineTop: Float) -> Float,
    ): PositionedTextLayout {
        val positioned = ArrayList<PositionedLine>()
        var cursor = startCursor
        var lineTop = region.y
        val regionBottom = region.y + region.height
        val unitLimit = prepared.breakableUnits.size

        while (lineTop + lineHeightPx <= regionBottom && cursor.segmentIndex < unitLimit) {
            val maxW = maxWidthAtLineTop(lineTop).coerceAtLeast(1f)
            val range = layoutNextLineRange(prepared, cursor, maxW) ?: break
            val line = materializeLineRange(prepared, range)
            positioned.add(
                PositionedLine(
                    x = region.x,
                    y = lineTop,
                    text = line.text,
                    width = line.width,
                ),
            )
            cursor = range.end
            lineTop += lineHeightPx
        }

        val height = if (positioned.isEmpty()) {
            0
        } else {
            ceil((positioned.maxOf { it.y } + lineHeightPx).toDouble()).toInt()
        }
        return PositionedTextLayout(
            lines = positioned,
            height = height,
            endUnitIndex = cursor.segmentIndex,
        )
    }


    fun layoutWithWidths(
        prepared: PreparedText,
        lineHeightPx: Float,
        maxUnits: Int = prepared.breakableUnits.size,
        widthForLineIndex: (lineIndex: Int) -> Int,
    ): MeasuredTextLayout {
        val lines = mutableListOf<String>()
        val lineWidths = mutableListOf<Float>()
        var lineIndex = 0
        var cursor = LayoutCursor(0, 0)
        val limit = maxUnits.coerceIn(0, prepared.breakableUnits.size)

        while (cursor.segmentIndex < limit) {
            val maxW = widthForLineIndex(lineIndex).coerceAtLeast(1).toFloat()
            val range = layoutNextLineRange(prepared, cursor, maxW) ?: break
            val line = materializeLineRange(prepared, range)
            lines.add(line.text)
            lineWidths.add(line.width)
            cursor = range.end
            lineIndex++
        }
        if (lines.isEmpty()) {
            lines.add("")
            lineWidths.add(0f)
        }
        return MeasuredTextLayout(
            height = ceil(lines.size * lineHeightPx).toInt(),
            lineCount = lines.size,
            lines = lines,
            lineWidthsPx = lineWidths,
        )
    }

    fun findTightestWidth(
        prepared: PreparedText,
        targetLines: Int,
        lineHeightPx: Float,
        minWidthPx: Int,
        maxWidthPx: Int,
    ): Pair<Int, MeasuredTextLayout> {
        var low = minWidthPx
        var high = maxWidthPx
        var best = layout(prepared, maxWidthPx, lineHeightPx)
        var bestWidth = maxWidthPx

        while (low <= high) {
            val mid = (low + high) / 2
            val result = layout(prepared, mid, lineHeightPx)
            if (result.lineCount <= targetLines) {
                best = result
                bestWidth = mid
                high = mid - 1
            } else {
                low = mid + 1
            }
        }
        return bestWidth to best
    }


    fun layoutAroundObstacle(
        prepared: PreparedText,
        containerWidthPx: Int,
        lineHeightPx: Float,
        obstacleLeftPx: Int,
        obstacleTopPx: Float,
        obstacleHeightPx: Float,
    ): MeasuredTextLayout {
        val obstacleBottom = obstacleTopPx + obstacleHeightPx
        val dynamic = layoutDynamic(
            prepared = prepared,
            region = LayoutRegion(
                x = 0f,
                y = 0f,
                width = containerWidthPx.toFloat(),
                height = obstacleBottom + lineHeightPx * 64f,
            ),
            lineHeightPx = lineHeightPx,
            maxWidthAtLineTop = { lineTop ->
                val lineBottom = lineTop + lineHeightPx
                val intersects = lineBottom > obstacleTopPx && lineTop < obstacleBottom
                if (intersects) {
                    obstacleLeftPx.coerceIn(80, containerWidthPx).toFloat()
                } else {
                    containerWidthPx.toFloat()
                }
            },
        )
        return MeasuredTextLayout(
            height = dynamic.height,
            lineCount = dynamic.lines.size,
            lines = dynamic.lines.map { it.text },
            lineWidthsPx = dynamic.lines.map { it.width },
        )
    }


    fun layoutColumn(
        prepared: PreparedText,
        startUnitIndex: Int = 0,
        region: LayoutRegion,
        lineHeightPx: Float,
        circleObstacles: List<CircleObstacle> = emptyList(),
        rectObstacles: List<RectObstacle> = emptyList(),
        polygonObstacles: List<PolygonObstacle> = emptyList(),
        minSlotWidthPx: Float = 48f,
    ): PositionedTextLayout = layoutColumn(
        source = EngineColumnMeasureSource(prepared, startUnitIndex),
        region = region,
        lineHeightPx = lineHeightPx,
        circleObstacles = circleObstacles,
        rectObstacles = rectObstacles,
        polygonObstacles = polygonObstacles,
        minSlotWidthPx = minSlotWidthPx,
    )

    fun layoutColumnViewMeasure(
        text: String,
        fontSizePx: Float,
        typeface: Typeface,
        region: LayoutRegion,
        lineHeightPx: Float,
        rectObstacles: List<RectObstacle> = emptyList(),
        polygonObstacles: List<PolygonObstacle> = emptyList(),
        minSlotWidthPx: Float = 48f,
    ): PositionedTextLayout = layoutColumn(
        source = ViewPaintColumnMeasureSource(text, fontSizePx, typeface),
        region = region,
        lineHeightPx = lineHeightPx,
        rectObstacles = rectObstacles,
        polygonObstacles = polygonObstacles,
        minSlotWidthPx = minSlotWidthPx,
    )

    fun layoutColumn(
        source: PretextColumnMeasureSource,
        region: LayoutRegion,
        lineHeightPx: Float,
        circleObstacles: List<CircleObstacle> = emptyList(),
        rectObstacles: List<RectObstacle> = emptyList(),
        polygonObstacles: List<PolygonObstacle> = emptyList(),
        minSlotWidthPx: Float = 48f,
    ): PositionedTextLayout {
        var lineTop = region.y
        val estLines = if (lineHeightPx > 0f) (region.height / lineHeightPx).toInt() + 2 else 8
        val positioned = ArrayList<PositionedLine>(
            estLines * (1 + circleObstacles.size + rectObstacles.size + polygonObstacles.size),
        )
        val regionBottom = region.y + region.height
        val blocked = ArrayList<Interval>(
            circleObstacles.size + rectObstacles.size + polygonObstacles.size,
        )
        val base = Interval(region.x, region.x + region.width)

        while (lineTop + lineHeightPx <= regionBottom && source.hasMore()) {
            val bandTop = lineTop
            val bandBottom = lineTop + lineHeightPx
            blocked.clear()

            for (i in circleObstacles.indices) {
                circleIntervalForBand(circleObstacles[i], bandTop, bandBottom)?.let(blocked::add)
            }
            for (i in rectObstacles.indices) {
                rectIntervalForBand(rectObstacles[i], bandTop, bandBottom)?.let(blocked::add)
            }
            for (i in polygonObstacles.indices) {
                polygonIntervalForBand(polygonObstacles[i], bandTop, bandBottom)?.let(blocked::add)
            }

            val slots = carveTextLineSlots(base, blocked, minSlotWidthPx)
            if (slots.isEmpty()) {
                lineTop += lineHeightPx
                continue
            }

            for (i in slots.indices) {
                if (!source.hasMore()) break
                val slot = slots[i]
                val piece = source.nextLine(slot.right - slot.left) ?: break
                positioned.add(
                    PositionedLine(
                        x = slot.left,
                        y = lineTop,
                        text = piece.text,
                        width = piece.widthPx,
                    ),
                )
            }
            lineTop += lineHeightPx
        }

        val height = if (positioned.isEmpty()) {
            0
        } else {
            var maxY = positioned[0].y
            for (i in 1 until positioned.size) {
                if (positioned[i].y > maxY) maxY = positioned[i].y
            }
            ceil((maxY + lineHeightPx).toDouble()).toInt()
        }
        return PositionedTextLayout(
            lines = positioned,
            height = height,
            endUnitIndex = source.endUnitIndex,
        )
    }


    fun layoutNewspaper(
        headline: PreparedText,
        body: PreparedText,
        pageWidthPx: Float,
        pageHeightPx: Float,
        paddingPx: Float,
        headlineLineHeightPx: Float,
        bodyLineHeightPx: Float,
        columnCount: Int,
        gutterPx: Float,
        circleObstacles: List<CircleObstacle> = emptyList(),
        rectObstacles: List<RectObstacle> = emptyList(),
        polygonObstacles: List<PolygonObstacle> = emptyList(),
    ): NewspaperLayout {
        val cols = columnCount.coerceIn(1, 4)
        val innerWidth = (pageWidthPx - paddingPx * 2f).coerceAtLeast(1f)
        val columnWidth = ((innerWidth - gutterPx * (cols - 1)) / cols).coerceAtLeast(48f)

        val headlineLayout = layoutColumn(
            prepared = headline,
            region = LayoutRegion(
                x = paddingPx,
                y = paddingPx,
                width = innerWidth,
                height = (pageHeightPx - paddingPx * 2f).coerceAtLeast(headlineLineHeightPx),
            ),
            lineHeightPx = headlineLineHeightPx,
            circleObstacles = circleObstacles,
            rectObstacles = rectObstacles,
            polygonObstacles = polygonObstacles,
            minSlotWidthPx = 64f,
        )
        val headlineBottom = if (headlineLayout.lines.isEmpty()) {
            paddingPx + headlineLineHeightPx * 1.6f
        } else {
            headlineLayout.lines.maxOf { it.y } + headlineLineHeightPx * 1.35f
        }

        var unitIndex = 0
        val bodyLines = mutableListOf<PositionedLine>()
        val bodyTop = headlineBottom
        val bodyHeight = (pageHeightPx - bodyTop - paddingPx).coerceAtLeast(bodyLineHeightPx)

        for (col in 0 until cols) {
            if (unitIndex >= body.breakableUnits.size) break
            val colX = paddingPx + col * (columnWidth + gutterPx)
            val columnLayout = layoutColumn(
                prepared = body,
                startUnitIndex = unitIndex,
                region = LayoutRegion(
                    x = colX,
                    y = bodyTop,
                    width = columnWidth,
                    height = bodyHeight,
                ),
                lineHeightPx = bodyLineHeightPx,
                circleObstacles = circleObstacles,
                rectObstacles = rectObstacles,
                polygonObstacles = polygonObstacles,
                minSlotWidthPx = 36f,
            )
            bodyLines.addAll(columnLayout.lines)
            unitIndex = columnLayout.endUnitIndex
        }

        return NewspaperLayout(
            headlineLines = headlineLayout.lines,
            bodyLines = bodyLines,
            columnCount = cols,
            columnWidthPx = columnWidth,
            gutterPx = gutterPx,
            headlineHeightPx = headlineBottom - paddingPx,
            endUnitIndex = unitIndex,
        )
    }

    private fun computeNextLine(
        prepared: PreparedText,
        start: LayoutCursor,
        maxWidthPx: Float,
    ): NextLineResult? {
        if (start.segmentIndex >= prepared.breakableUnits.size) return null
        val maxW = maxWidthPx.coerceAtLeast(1f)
        val builder = StringBuilder()
        var width = 0f
        var segIdx = start.segmentIndex
        var gIdx = start.graphemeIndex

        while (segIdx < prepared.breakableUnits.size) {
            val unit = prepared.breakableUnits[segIdx]
            if (PretextTextAnalysis.isHardBreak(unit)) {
                if (builder.isEmpty()) {
                    segIdx++
                    gIdx = 0
                }
                break
            }

            val fit = prepared.unitGraphemeFits.getOrNull(segIdx)
            if (fit != null && gIdx < fit.clusters.size) {
                while (gIdx < fit.clusters.size) {
                    val gw = fit.advancesPx[gIdx]
                    if (width + gw > maxW && builder.isNotEmpty()) {
                        return finishLine(builder, width, LayoutCursor(segIdx, gIdx))
                    }
                    builder.append(fit.clusters[gIdx])
                    width += gw
                    gIdx++
                }
                segIdx++
                gIdx = 0
                continue
            }

            val unitWidth = wholeUnitAdvance(prepared, segIdx, gIdx)
            if (unitWidth > maxW && builder.isEmpty()) {
                val forced = appendGraphemesFromFit(
                    prepared = prepared,
                    unitIndex = segIdx,
                    startGrapheme = gIdx,
                    maxWidthPx = maxW,
                    builder = builder,
                    currentWidth = width,
                )
                width = forced.width
                segIdx = forced.end.segmentIndex
                gIdx = forced.end.graphemeIndex
                break
            }

            if (width + unitWidth > maxW && builder.isNotEmpty()) break

            appendWholeUnit(builder, prepared, segIdx, gIdx)
            width += unitWidth
            segIdx++
            gIdx = 0
        }

        if (builder.isEmpty() && segIdx < prepared.breakableUnits.size) {
            val unit = prepared.breakableUnits[segIdx]
            if (PretextTextAnalysis.isHardBreak(unit)) {
                return NextLineResult("", 0f, LayoutCursor(segIdx + 1, 0))
            }
            val unitWidth = wholeUnitAdvance(prepared, segIdx, 0)
            if (unitWidth > maxW) {
                val forced = appendGraphemesFromFit(
                    prepared, segIdx, gIdx, maxW, builder, width,
                )
                width = forced.width
                segIdx = forced.end.segmentIndex
                gIdx = forced.end.graphemeIndex
            } else {
                appendWholeUnit(builder, prepared, segIdx, 0)
                width = unitWidth
                segIdx++
                gIdx = 0
            }
        }

        return finishLine(builder, width, LayoutCursor(segIdx, gIdx))
    }

    private fun finishLine(
        builder: StringBuilder,
        width: Float,
        end: LayoutCursor,
    ): NextLineResult = NextLineResult(
        text = builder.toString().trimEnd(),
        width = width,
        end = end,
    )

    private fun wholeUnitAdvance(prepared: PreparedText, unitIndex: Int, graphemeIndex: Int): Float {
        if (graphemeIndex > 0) {
            val fit = prepared.unitGraphemeFits.getOrNull(unitIndex) ?: return 0f
            return fit.widthSum(graphemeIndex)
        }
        val unit = prepared.breakableUnits[unitIndex]
        return prepared.unitWidthsPx[unitIndex] +
            prepared.letterSpacingPx * unitGraphemeCount(unit).coerceAtLeast(1)
    }

    private data class GraphemeAppendResult(
        val width: Float,
        val end: LayoutCursor,
    )

    private fun appendGraphemesFromFit(
        prepared: PreparedText,
        unitIndex: Int,
        startGrapheme: Int,
        maxWidthPx: Float,
        builder: StringBuilder,
        currentWidth: Float,
    ): GraphemeAppendResult {
        val fit = prepared.unitGraphemeFits.getOrNull(unitIndex)
            ?: run {
                val paint = makePaint(prepared.fontSizePx, prepared.typeface)
                val fallback = PretextTextAnalysis.measureGraphemeFit(
                    prepared.breakableUnits[unitIndex],
                    paint,
                    prepared.fontSizePx,
                    prepared.typefaceHash,
                    prepared.letterSpacingPx,
                    ::measureUnit,
                )
                return appendGraphemesFromFitData(
                    fallback, unitIndex, startGrapheme, maxWidthPx, builder, currentWidth,
                )
            }
        return appendGraphemesFromFitData(
            fit, unitIndex, startGrapheme, maxWidthPx, builder, currentWidth,
        )
    }

    private fun appendGraphemesFromFitData(
        fit: UnitGraphemeFit,
        unitIndex: Int,
        startGrapheme: Int,
        maxWidthPx: Float,
        builder: StringBuilder,
        currentWidth: Float,
    ): GraphemeAppendResult {
        var width = currentWidth
        var gIdx = startGrapheme
        while (gIdx < fit.clusters.size) {
            val gw = fit.advancesPx[gIdx]
            if (width + gw > maxWidthPx && builder.isNotEmpty()) {
                return GraphemeAppendResult(width, LayoutCursor(unitIndex, gIdx))
            }
            builder.append(fit.clusters[gIdx])
            width += gw
            gIdx++
            if (width >= maxWidthPx) {
                val nextSeg = if (gIdx >= fit.clusters.size) unitIndex + 1 else unitIndex
                val nextG = if (gIdx >= fit.clusters.size) 0 else gIdx
                return GraphemeAppendResult(width, LayoutCursor(nextSeg, nextG))
            }
        }
        if (builder.isEmpty() && fit.clusters.isNotEmpty()) {
            builder.append(fit.clusters[0])
            width = currentWidth + fit.advancesPx[0]
            gIdx = 1
        }
        val nextSeg = if (gIdx >= fit.clusters.size) unitIndex + 1 else unitIndex
        val nextG = if (gIdx >= fit.clusters.size) 0 else gIdx
        return GraphemeAppendResult(width, LayoutCursor(nextSeg, nextG))
    }

    private fun appendWholeUnit(
        builder: StringBuilder,
        prepared: PreparedText,
        unitIndex: Int,
        graphemeIndex: Int,
    ) {
        val fit = prepared.unitGraphemeFits.getOrNull(unitIndex)
        if (fit != null && graphemeIndex > 0) {
            for (i in graphemeIndex until fit.clusters.size) {
                builder.append(fit.clusters[i])
            }
        } else {
            builder.append(prepared.breakableUnits[unitIndex])
        }
    }

    private fun appendRangeText(
        builder: StringBuilder,
        prepared: PreparedText,
        start: LayoutCursor,
        end: LayoutCursor,
    ) {
        var idx = start.segmentIndex
        val lastExclusive = end.segmentIndex
        if (idx == lastExclusive && end.graphemeIndex > start.graphemeIndex) {
            appendUnitSlice(builder, prepared, idx, start.graphemeIndex, end.graphemeIndex)
            return
        }
        if (start.graphemeIndex > 0) {
            val fit = prepared.unitGraphemeFits.getOrNull(idx)
            if (fit != null) {
                for (g in start.graphemeIndex until fit.clusters.size) {
                    builder.append(fit.clusters[g])
                }
            } else {
                builder.append(prepared.breakableUnits[idx])
            }
            idx++
        }
        while (idx < lastExclusive) {
            builder.append(prepared.breakableUnits[idx])
            idx++
        }
        if (end.graphemeIndex > 0 && idx == end.segmentIndex) {
            appendUnitSlice(builder, prepared, idx, 0, end.graphemeIndex)
        }
    }

    private fun appendUnitSlice(
        builder: StringBuilder,
        prepared: PreparedText,
        unitIndex: Int,
        fromGrapheme: Int,
        toGrapheme: Int,
    ) {
        val fit = prepared.unitGraphemeFits.getOrNull(unitIndex) ?: return
        for (g in fromGrapheme until toGrapheme.coerceAtMost(fit.clusters.size)) {
            builder.append(fit.clusters[g])
        }
    }

    private fun unitGraphemeCount(unit: String): Int =
        if (unit.length <= 1) 1 else PretextTextAnalysis.graphemes(unit).size

    private data class NextLineResult(
        val text: String,
        val width: Float,
        val end: LayoutCursor,
    )

    private fun circleIntervalForBand(
        circle: CircleObstacle,
        bandTop: Float,
        bandBottom: Float,
    ): Interval? {
        val cx = circle.cx
        val cy = circle.cy
        val r = circle.r
        if (bandTop >= cy + r || bandBottom <= cy - r) return null
        val minDy = when {
            cy in bandTop..bandBottom -> 0f
            cy < bandTop -> bandTop - cy
            else -> cy - bandBottom
        }
        if (minDy >= r) return null
        val maxDx = kotlin.math.sqrt(r * r - minDy * minDy)
        return Interval(cx - maxDx, cx + maxDx)
    }

    private fun rectIntervalForBand(
        rect: RectObstacle,
        bandTop: Float,
        bandBottom: Float,
    ): Interval? {
        if (bandBottom <= rect.y || bandTop >= rect.y + rect.h) return null
        return Interval(rect.x, rect.x + rect.w)
    }

    private fun polygonIntervalForBand(
        polygon: PolygonObstacle,
        bandTop: Float,
        bandBottom: Float,
    ): Interval? {
        val flat = PretextNativeGeometry.polygonToFlat(polygon)
        val band = PretextNativeGeometry.bandInterval(flat, bandTop, bandBottom) ?: return null
        return Interval(band.first, band.second)
    }

    private fun carveTextLineSlots(
        base: Interval,
        blocked: List<Interval>,
        minSlotWidthPx: Float,
    ): List<Interval> {
        if (blocked.isEmpty()) {
            return if (base.right - base.left >= minSlotWidthPx) listOf(base) else emptyList()
        }
        var slots: List<Interval> = listOf(base)
        for (block in blocked) {
            val next = ArrayList<Interval>(slots.size + 1)
            for (slot in slots) {
                if (block.right <= slot.left || block.left >= slot.right) {
                    next.add(slot)
                } else {
                    if (block.left > slot.left) next.add(Interval(slot.left, block.left))
                    if (block.right < slot.right) next.add(Interval(block.right, slot.right))
                }
            }
            slots = next
        }
        if (slots.size > 1) slots = slots.sortedBy { it.left }
        return slots.filter { it.right - it.left >= minSlotWidthPx }
    }

    fun layoutForPrefix(
        prepared: PreparedText,
        visibleText: String,
        containerWidthPx: Int,
        lineHeightPx: Float,
    ): MeasuredTextLayout {
        if (visibleText.isEmpty()) {
            return MeasuredTextLayout(height = 0, lineCount = 0, lines = emptyList())
        }
        var consumed = 0
        var units = 0
        for (unit in prepared.breakableUnits) {
            if (consumed >= visibleText.length) break
            consumed += unit.length
            units++
        }
        return layout(prepared, containerWidthPx, lineHeightPx, maxUnits = units.coerceAtLeast(1))
    }


    fun truncateText(
        prepared: PreparedText,
        containerWidthPx: Int,
        lineHeightPx: Float,
        maxLines: Int,
        ellipsis: String = "…",
        mode: TruncationMode = TruncationMode.Tail,
    ): TruncationResult {
        val full = layout(prepared, containerWidthPx, lineHeightPx)
        if (full.lineCount <= maxLines) {
            return TruncationResult(prepared.originalText, false, prepared.originalText.length)
        }
        val joined = prepared.originalText
        return when (mode) {
            TruncationMode.Tail -> {
                var visible = (joined.length * maxLines / full.lineCount).coerceAtLeast(1)
                while (visible > ellipsis.length) {
                    val candidate = joined.substring(0, visible).trimEnd() + ellipsis
                    if (heightOf(candidate, prepared, containerWidthPx, lineHeightPx) <= maxLines) {
                        return TruncationResult(candidate, true, visible)
                    }
                    visible -= max(1, visible / 32)
                }
                TruncationResult(ellipsis, true, 0)
            }
            TruncationMode.Head -> {
                var keep = (joined.length * maxLines / full.lineCount).coerceAtLeast(1)
                while (keep > ellipsis.length) {
                    val candidate = ellipsis + joined.substring(joined.length - keep)
                    if (heightOf(candidate, prepared, containerWidthPx, lineHeightPx) <= maxLines) {
                        return TruncationResult(candidate, true, keep)
                    }
                    keep -= max(1, keep / 32)
                }
                TruncationResult(ellipsis, true, 0)
            }
            TruncationMode.Middle -> {
                var keep = (joined.length * maxLines / full.lineCount).coerceAtLeast(2)
                while (keep > ellipsis.length + 2) {
                    val half = keep / 2
                    val candidate = joined.substring(0, half) + ellipsis + joined.substring(joined.length - half)
                    if (heightOf(candidate, prepared, containerWidthPx, lineHeightPx) <= maxLines) {
                        return TruncationResult(candidate, true, keep)
                    }
                    keep -= max(1, keep / 32)
                }
                TruncationResult(ellipsis, true, 0)
            }
        }
    }

    private fun heightOf(text: String, ref: PreparedText, widthPx: Int, lineHeightPx: Float): Int {
        val prep = prepareSync(text, ref.fontMetrics.lineHeight, Typeface.DEFAULT)
        return layout(prep, widthPx, lineHeightPx).lineCount
    }


    fun fitFontSize(
        text: String,
        typeface: Typeface,
        maxWidthPx: Int,
        maxHeightPx: Float,
        lineHeightMultiplier: Float = 1.25f,
        minFontSizePx: Float = 8f,
        maxFontSizePx: Float = 96f,
    ): FitFontResult {
        var lo = minFontSizePx
        var hi = maxFontSizePx
        var best = lo
        var bestPrep = prepareSync(text, lo, typeface)
        var bestLayout = layout(bestPrep, maxWidthPx, lo * lineHeightMultiplier)
        repeat(12) {
            if (hi - lo < 0.5f) return@repeat
            val mid = (lo + hi) * 0.5f
            val prep = prepareSync(text, mid, typeface)
            val lh = mid * lineHeightMultiplier
            val laid = layout(prep, maxWidthPx, lh)
            if (laid.height <= maxHeightPx) {
                best = mid
                bestPrep = prep
                bestLayout = laid
                lo = mid
            } else {
                hi = mid
            }
        }
        return FitFontResult(best, bestPrep, bestLayout)
    }


    fun balanceLines(
        prepared: PreparedText,
        maxContainerWidthPx: Int,
        lineHeightPx: Float,
        minContainerWidthPx: Int = (maxContainerWidthPx * 0.35f).toInt(),
    ): MeasuredTextLayout {
        val baseline = layout(prepared, maxContainerWidthPx, lineHeightPx)
        val targetLines = baseline.lineCount
        if (targetLines <= 1) return baseline
        var lo = minContainerWidthPx.coerceAtLeast(1)
        var hi = maxContainerWidthPx
        var best = baseline
        while (lo <= hi) {
            val mid = (lo + hi) / 2
            val candidate = layout(prepared, mid, lineHeightPx)
            if (candidate.lineCount == targetLines) {
                best = candidate
                hi = mid - 1
            } else {
                lo = mid + 1
            }
        }
        return best
    }

    fun measureViewHeight(
        context: Context,
        text: String,
        fontSizeSp: Float,
        widthPx: Int,
    ): Int {
        val tv = TextView(context).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
        }
        val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        tv.measure(widthSpec, heightSpec)
        return tv.measuredHeight
    }

    private fun buildPrepared(
        text: String,
        fontSize: Float,
        typeface: Typeface,
        options: PrepareOptions,
    ): PreparedText {
        val paint = makePaint(fontSize, typeface)
        return buildPreparedWithPaint(text, paint, options)
    }

    private fun buildPreparedWithPaint(
        text: String,
        paint: Paint,
        options: PrepareOptions = PrepareOptions(),
    ): PreparedText {
        val metrics = paint.fontMetrics
        val fontMetrics = CachedFontMetrics(
            ascent = metrics.ascent,
            descent = metrics.descent,
            lineHeight = metrics.descent - metrics.ascent,
        )
        val units = PretextTextAnalysis.breakIntoUnits(text, options)
        val widths = FloatArray(units.size)
        val tfHash = paint.typeface?.hashCode() ?: 0
        val fontSize = paint.textSize
        val segments = ArrayList<TextSegment>(units.size)
        val graphemeFits = ArrayList<UnitGraphemeFit?>(units.size)
        for (i in units.indices) {
            val unit = units[i]
            val w = measureUnit(paint, unit, fontSize, tfHash)
            widths[i] = w
            segments.add(TextSegment(unit, w, isEmojiUnit(unit)))
            graphemeFits += graphemeFitForUnit(paint, unit, fontSize, tfHash, options.letterSpacingPx)
        }
        return PreparedText(
            originalText = text,
            segments = segments,
            fontMetrics = fontMetrics,
            breakableUnits = units,
            unitWidthsPx = widths,
            fontSizePx = fontSize,
            typeface = paint.typeface ?: Typeface.DEFAULT,
            typefaceHash = tfHash,
            letterSpacingPx = options.letterSpacingPx,
            options = options,
            unitGraphemeFits = graphemeFits,
        )
    }

    private fun graphemeFitForUnit(
        paint: Paint,
        unit: String,
        fontSizePx: Float,
        typefaceHash: Int,
        letterSpacingPx: Float,
    ): UnitGraphemeFit? {
        if (!PretextTextAnalysis.needsGraphemeBreakdown(unit)) return null
        return PretextTextAnalysis.measureGraphemeFit(
            unit = unit,
            paint = paint,
            fontSizePx = fontSizePx,
            typefaceHash = typefaceHash,
            letterSpacingPx = letterSpacingPx,
            measureCluster = ::measureUnit,
        )
    }

    private fun makePaint(fontSize: Float, typeface: Typeface): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            this.typeface = typeface
        }

    private fun measureUnit(paint: Paint, unit: String, fontSize: Float, typefaceHash: Int): Float {
        val key = "$unit|$fontSize|$typefaceHash"
        unitWidthCache.get(key)?.let { return it }
        val raw = paint.measureText(unit)
        val w = if (isEmojiUnit(unit)) raw * EMOJI_WIDTH_FACTOR else raw
        unitWidthCache.put(key, w)
        return w
    }

    private fun cacheKey(
        text: String,
        fontSize: Float,
        typeface: Typeface,
        options: PrepareOptions,
    ): String =
        "$text|$fontSize|${typeface.hashCode()}|${options.whiteSpace}|${options.wordBreak}|${options.letterSpacingPx}"

    private fun isEmojiUnit(unit: String): Boolean = unit.any { ch ->
        val block = Character.UnicodeBlock.of(ch)
        block == Character.UnicodeBlock.EMOTICONS ||
            block == Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS ||
            ch.code > 0x1F000
    }
}
