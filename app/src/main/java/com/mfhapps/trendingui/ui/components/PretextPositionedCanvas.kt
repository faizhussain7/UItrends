package com.mfhapps.trendingui.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.mfhapps.trendingui.core.text.PositionedTextLayout


@Composable
fun PretextPositionedCanvas(
    layout: PositionedTextLayout,
    fontSizePx: Float,
    lineHeightPx: Float,
    modifier: Modifier = Modifier,
    typeface: Typeface = Typeface.DEFAULT,
    textColor: Color = Color.White,
) {
    val paint = remember(fontSizePx, typeface, textColor) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = fontSizePx
            this.typeface = typeface
            color = textColor.toArgb()
            setShadowLayer(5f, 0f, 1f, android.graphics.Color.argb(180, 0, 0, 0))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        paint.color = textColor.toArgb()
        layout.lines.forEach { line ->
            if (line.text.isEmpty()) return@forEach
            val baseline = line.y + lineHeightPx - paint.fontMetrics.descent
            drawContext.canvas.nativeCanvas.drawText(line.text, line.x, baseline, paint)
        }
    }
}
