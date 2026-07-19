package com.mfhapps.trendingui.core.text

import android.graphics.Paint
import android.graphics.Typeface

data class ColumnLinePiece(
    val text: String,
    val widthPx: Float,
)

/**
 * Feeds text into [TextMeasurementEngine.layoutColumn].
 *
 * Engine and View.measure share one obstacle-band / slot-carving pipeline.
 * Only the per-slot width probe differs:
 * - [EngineColumnMeasureSource]: precomputed grapheme advances from prepare()
 * - [ViewPaintColumnMeasureSource]: [Paint.breakText] each slot (View-style measure)
 */
interface PretextColumnMeasureSource {
    val endUnitIndex: Int get() = 0
    fun hasMore(): Boolean
    fun nextLine(maxWidthPx: Float): ColumnLinePiece?
}

class EngineColumnMeasureSource(
    private val prepared: PreparedText,
    startUnitIndex: Int = 0,
) : PretextColumnMeasureSource {
    private val unitCount = prepared.breakableUnits.size
    private var cursor = LayoutCursor(
        segmentIndex = startUnitIndex.coerceIn(0, unitCount),
        graphemeIndex = 0,
    )

    override val endUnitIndex: Int
        get() = cursor.segmentIndex

    override fun hasMore(): Boolean = cursor.segmentIndex < unitCount

    override fun nextLine(maxWidthPx: Float): ColumnLinePiece? {
        val line = TextMeasurementEngine.layoutNextLine(prepared, cursor, maxWidthPx) ?: return null
        cursor = line.end
        return ColumnLinePiece(text = line.text, widthPx = line.width)
    }
}

class ViewPaintColumnMeasureSource(
    private val text: String,
    fontSizePx: Float,
    typeface: Typeface,
) : PretextColumnMeasureSource {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = fontSizePx
        this.typeface = typeface
    }
    private var offset = 0
    private val measuredWidth = FloatArray(1)

    override fun hasMore(): Boolean {
        skipLeadingWhitespace()
        return offset < text.length
    }

    override fun nextLine(maxWidthPx: Float): ColumnLinePiece? {
        skipLeadingWhitespace()
        if (offset >= text.length) return null

        val counted = paint.breakText(
            text,
            offset,
            text.length,
            true,
            maxWidthPx,
            measuredWidth,
        )
        if (counted <= 0) return null

        var end = offset + counted
        if (end < text.length && !text[end - 1].isWhitespace() && text[end] != '\n') {
            val lastSpace = text.lastIndexOf(' ', end - 1)
            if (lastSpace >= offset) {
                end = lastSpace + 1
            }
        }

        val lineText = text.substring(offset, end).trimEnd()
        offset = end
        if (lineText.isEmpty()) {
            return if (hasMore()) nextLine(maxWidthPx) else null
        }
        return ColumnLinePiece(text = lineText, widthPx = paint.measureText(lineText))
    }

    private fun skipLeadingWhitespace() {
        while (offset < text.length && text[offset].isWhitespace()) {
            offset++
        }
    }
}
