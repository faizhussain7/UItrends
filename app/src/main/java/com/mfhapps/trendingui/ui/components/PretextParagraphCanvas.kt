package com.mfhapps.trendingui.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.mfhapps.trendingui.core.text.MeasuredTextLayout


@Composable
fun PretextParagraphCanvas(
    layout: MeasuredTextLayout,
    fontSizePx: Float,
    lineHeightPx: Float,
    modifier: Modifier = Modifier,
    typeface: Typeface = Typeface.DEFAULT,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val paint = remember(fontSizePx, typeface, textColor) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = fontSizePx
            this.typeface = typeface
            color = textColor.toArgb()
            setShadowLayer(4f, 0f, 1f, android.graphics.Color.argb(160, 0, 0, 0))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        paint.color = textColor.toArgb()
        layout.lines.forEachIndexed { index, line ->
            if (line.isEmpty()) return@forEachIndexed
            val baseline = (index + 1) * lineHeightPx - paint.fontMetrics.descent
            drawContext.canvas.nativeCanvas.drawText(line, 0f, baseline, paint)
        }
    }
}
