package com.mfhapps.trendingui.screens.pretext

import android.graphics.Typeface
import com.mfhapps.trendingui.core.text.LayoutRegion
import com.mfhapps.trendingui.core.text.MeasuredTextLayout
import com.mfhapps.trendingui.core.text.NewspaperLayout
import com.mfhapps.trendingui.core.text.PolygonObstacle
import com.mfhapps.trendingui.core.text.PositionedLine
import com.mfhapps.trendingui.core.text.PositionedTextLayout
import com.mfhapps.trendingui.core.text.PreparedText
import com.mfhapps.trendingui.core.text.RectObstacle
import com.mfhapps.trendingui.core.text.TextMeasurementEngine
import com.mfhapps.trendingui.core.text.ViewPaintColumnMeasureSource
import kotlin.math.ceil

object PretextCameraReflowScheduler {

    private const val LAYOUT_VERTICES = 32
    private const val QUANTIZE_PX = 4f

    fun layoutFingerprint(shape: ViewShape?): Int = layoutFingerprint(shapes = listOfNotNull(shape))

    fun layoutFingerprint(shapes: List<ViewShape>): Int {
        if (shapes.isEmpty()) return 0
        val step = if (shapes.any { it.source == VisionSource.Face }) 2f else QUANTIZE_PX
        var h = shapes.size
        shapes.forEach { shape ->
            h = h * 31 + quantize(shape.boundsPx.left, step)
            h = h * 31 + quantize(shape.boundsPx.top, step)
            h = h * 31 + quantize(shape.boundsPx.right, step)
            h = h * 31 + quantize(shape.boundsPx.bottom, step)
            shape.polygonPx?.points?.forEach { (x, y) ->
                h = h * 31 + quantize(x, step)
                h = h * 31 + quantize(y, step)
            }
        }
        return h
    }

    fun layoutPolygon(shape: ViewShape?): PolygonObstacle? =
        shape?.polygonPx?.let { downsamplePolygon(it, LAYOUT_VERTICES) }

    fun layoutRect(shape: ViewShape?): RectObstacle? =
        shape?.toRectObstacle()

    fun splitHeadlineBody(fullText: String): Pair<String, String> {
        val trimmed = fullText.trim()
        if (trimmed.isEmpty()) return "" to ""

        val paraBreak = trimmed.indexOf("\n\n")
        if (paraBreak in 1 until trimmed.length - 1) {
            return trimmed.substring(0, paraBreak).trim() to trimmed.substring(paraBreak + 2).trim()
        }

        val dot = trimmed.indexOf(". ")
        if (dot in 40..140) {
            return trimmed.substring(0, dot + 1).trim() to trimmed.substring(dot + 2).trim()
        }

        val newline = trimmed.indexOf('\n')
        if (newline in 24..100) {
            return trimmed.substring(0, newline).trim() to trimmed.substring(newline + 1).trim()
        }

        val words = trimmed.split(' ')
        if (words.size > 14) {
            val head = words.take(10).joinToString(" ")
            val body = words.drop(10).joinToString(" ")
            return head to body
        }

        val cut = trimmed.length.coerceAtMost(72)
        return trimmed.take(cut).trim() to trimmed.drop(cut).trim().ifEmpty { trimmed }
    }

    fun prepareHeadlineBody(
        fullText: String,
        headlineFontSizePx: Float,
        bodyFontSizePx: Float,
        typeface: Typeface = Typeface.DEFAULT,
    ): Pair<PreparedText, PreparedText> {
        val (headline, body) = splitHeadlineBody(fullText)
        val headlinePrep = TextMeasurementEngine.prepareSync(
            headline.ifEmpty { " " },
            headlineFontSizePx,
            typeface,
        )
        val bodyPrep = TextMeasurementEngine.prepareSync(
            body.ifEmpty { fullText },
            bodyFontSizePx,
            typeface,
        )
        return headlinePrep to bodyPrep
    }

    fun computeLayout(
        prepared: PreparedText,
        shape: ViewShape?,
        region: LayoutRegion,
        lineHeightPx: Float,
        style: PretextCameraTextLayoutStyle = PretextCameraTextLayoutStyle.ColumnWrap,
        pageWidthPx: Float = region.x + region.width,
        pageHeightPx: Float = region.y + region.height,
        paddingPx: Float = region.x,
        headlinePrepared: PreparedText? = null,
        bodyPrepared: PreparedText? = null,
    ): PositionedTextLayout = computeLayout(
        measureMode = PretextMeasureMode.Engine,
        prepared = prepared,
        sourceText = prepared.originalText,
        fontSizePx = prepared.fontSizePx,
        typeface = prepared.typeface,
        shapes = listOfNotNull(shape),
        region = region,
        lineHeightPx = lineHeightPx,
        style = style,
        pageWidthPx = pageWidthPx,
        pageHeightPx = pageHeightPx,
        paddingPx = paddingPx,
        headlinePrepared = headlinePrepared,
        bodyPrepared = bodyPrepared,
    )

    fun computeLayout(
        measureMode: PretextMeasureMode,
        prepared: PreparedText?,
        sourceText: String,
        fontSizePx: Float,
        typeface: Typeface,
        shapes: List<ViewShape>,
        region: LayoutRegion,
        lineHeightPx: Float,
        style: PretextCameraTextLayoutStyle = PretextCameraTextLayoutStyle.ColumnWrap,
        pageWidthPx: Float = region.x + region.width,
        pageHeightPx: Float = region.y + region.height,
        paddingPx: Float = region.x,
        headlinePrepared: PreparedText? = null,
        bodyPrepared: PreparedText? = null,
    ): PositionedTextLayout {
        val polygons = shapes.mapNotNull { layoutPolygon(it) }
        val rects = if (polygons.isEmpty()) {
            shapes.mapNotNull { layoutRect(it) }
        } else {
            emptyList()
        }

        val useViewPaint = measureMode == PretextMeasureMode.ViewMeasure &&
            (style == PretextCameraTextLayoutStyle.ColumnWrap ||
                style == PretextCameraTextLayoutStyle.Uniform)

        if (useViewPaint) {
            val paintPolygons = if (style == PretextCameraTextLayoutStyle.Uniform) {
                emptyList()
            } else {
                polygons
            }
            val paintRects = if (style == PretextCameraTextLayoutStyle.Uniform) {
                emptyList()
            } else {
                rects
            }
            return TextMeasurementEngine.layoutColumn(
                source = ViewPaintColumnMeasureSource(sourceText, fontSizePx, typeface),
                region = region,
                lineHeightPx = lineHeightPx,
                rectObstacles = paintRects,
                polygonObstacles = paintPolygons,
            )
        }

        val prep = prepared
            ?: return PositionedTextLayout(lines = emptyList(), height = 0)

        return when (style) {
            PretextCameraTextLayoutStyle.Uniform -> {
                val measured = TextMeasurementEngine.layout(
                    prepared = prep,
                    containerWidthPx = region.width.toInt().coerceAtLeast(1),
                    lineHeightPx = lineHeightPx,
                )
                measured.toPositionedLayout(region.x, region.y, lineHeightPx)
            }

            PretextCameraTextLayoutStyle.DynamicFloat -> {
                layoutDynamicAroundShapes(
                    prepared = prep,
                    shapes = shapes,
                    region = region,
                    lineHeightPx = lineHeightPx,
                )
            }

            PretextCameraTextLayoutStyle.Newspaper,
            PretextCameraTextLayoutStyle.Magazine,
            -> {
                val headline = headlinePrepared ?: prep
                val body = bodyPrepared ?: prep
                val columns = when (style) {
                    PretextCameraTextLayoutStyle.Magazine -> 2
                    else -> if (pageWidthPx >= 720f) 3 else 2
                }
                val gutter = (pageWidthPx * 0.028f).coerceIn(10f, 24f)
                val headlineLh = lineHeightPx * 1.28f
                val paper = TextMeasurementEngine.layoutNewspaper(
                    headline = headline,
                    body = body,
                    pageWidthPx = pageWidthPx,
                    pageHeightPx = pageHeightPx,
                    paddingPx = paddingPx,
                    headlineLineHeightPx = headlineLh,
                    bodyLineHeightPx = lineHeightPx,
                    columnCount = columns,
                    gutterPx = gutter,
                    rectObstacles = rects,
                    polygonObstacles = polygons,
                )
                paper.toPositionedLayout(lineHeightPx)
            }

            PretextCameraTextLayoutStyle.ColumnWrap -> {
                TextMeasurementEngine.layoutColumn(
                    prepared = prep,
                    region = region,
                    lineHeightPx = lineHeightPx,
                    rectObstacles = rects,
                    polygonObstacles = polygons,
                )
            }
        }
    }

    private fun layoutDynamicAroundShapes(
        prepared: PreparedText,
        shapes: List<ViewShape>,
        region: LayoutRegion,
        lineHeightPx: Float,
    ): PositionedTextLayout {
        val primary = shapes.maxByOrNull { it.boundsPx.width() * it.boundsPx.height() }
        if (primary == null) {
            val measured = TextMeasurementEngine.layout(
                prepared = prepared,
                containerWidthPx = region.width.toInt().coerceAtLeast(1),
                lineHeightPx = lineHeightPx,
            )
            return measured.toPositionedLayout(region.x, region.y, lineHeightPx)
        }

        val bounds = primary.boundsPx
        val obstacleTop = bounds.top
        val obstacleBottom = bounds.bottom
        val obstacleLeft = bounds.left.coerceIn(region.x + 48f, region.x + region.width - 48f)
        val minTextWidth = 72f

        return TextMeasurementEngine.layoutDynamic(
            prepared = prepared,
            region = region,
            lineHeightPx = lineHeightPx,
            maxWidthAtLineTop = { lineTop ->
                val lineBottom = lineTop + lineHeightPx
                val intersects = lineBottom > obstacleTop && lineTop < obstacleBottom
                if (intersects) {
                    (obstacleLeft - region.x).coerceAtLeast(minTextWidth)
                } else {
                    region.width
                }
            },
        )
    }

    private fun MeasuredTextLayout.toPositionedLayout(
        originX: Float,
        originY: Float,
        lineHeightPx: Float,
    ): PositionedTextLayout {
        var y = originY
        val positioned = ArrayList<PositionedLine>(lineCount.coerceAtLeast(1))
        for (i in lines.indices) {
            positioned.add(
                PositionedLine(
                    x = originX,
                    y = y,
                    text = lines[i],
                    width = lineWidthsPx.getOrElse(i) { 0f },
                ),
            )
            y += lineHeightPx
        }
        return PositionedTextLayout(
            lines = positioned,
            height = if (positioned.isEmpty()) 0 else ceil((y - originY).toDouble()).toInt(),
        )
    }

    private fun NewspaperLayout.toPositionedLayout(lineHeightPx: Float): PositionedTextLayout {
        val all = headlineLines + bodyLines
        val height = if (all.isEmpty()) {
            0
        } else {
            var maxY = all[0].y
            for (i in 1 until all.size) {
                if (all[i].y > maxY) maxY = all[i].y
            }
            ceil((maxY + lineHeightPx * 1.28f).toDouble()).toInt()
        }
        return PositionedTextLayout(
            lines = all,
            height = height,
            endUnitIndex = endUnitIndex,
        )
    }

    private fun quantize(v: Float, stepPx: Float = QUANTIZE_PX): Int = (v / stepPx).toInt()

    private fun downsamplePolygon(poly: PolygonObstacle, maxVertices: Int): PolygonObstacle {
        val pts = poly.points
        if (pts.size <= maxVertices) return poly
        val step = pts.size.toFloat() / maxVertices
        val out = List(maxVertices) { i ->
            pts[(i * step).toInt().coerceIn(0, pts.lastIndex)]
        }
        return PolygonObstacle(out)
    }
}
