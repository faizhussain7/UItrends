package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.core.text.MeasuredTextLayout

@Composable
fun PretextLineCanvas(
    layout: MeasuredTextLayout,
    containerWidthPx: Int,
    lineHeightPx: Float,
    obstacleStartPx: Int?,
    modifier: Modifier = Modifier,
    lineHeight: Dp = 22.dp,
) {
    val density = LocalDensity.current
    val scheme = MaterialTheme.colorScheme
    val canvasHeight = with(density) {
        (layout.lineCount * lineHeightPx).toDp().coerceAtLeast(lineHeight)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(canvasHeight),
    ) {
        val containerW = containerWidthPx.toFloat()
        layout.lines.forEachIndexed { index, _ ->
            val top = index * lineHeightPx
            val usedW = layout.lineWidthsPx.getOrElse(index) { containerW }
            drawRoundRect(
                color = scheme.primaryContainer,
                topLeft = Offset(0f, top + 2f),
                size = Size(usedW.coerceAtMost(containerW), lineHeightPx - 4f),
                cornerRadius = CornerRadius(6f, 6f),
            )
            drawRoundRect(
                color = scheme.outlineVariant,
                topLeft = Offset(0f, top),
                size = Size(containerW, lineHeightPx),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.5f),
            )
            obstacleStartPx?.let { obstacle ->
                if (index % 2 == 1) {
                    drawRoundRect(
                        color = scheme.tertiary.copy(alpha = 0.35f),
                        topLeft = Offset(obstacle.toFloat(), top),
                        size = Size((containerW - obstacle).coerceAtLeast(0f), lineHeightPx),
                        cornerRadius = CornerRadius(4f, 4f),
                    )
                }
            }
        }
    }
}
