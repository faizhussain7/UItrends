package com.mfhapps.trendingui.screens.pretext

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.core.text.LayoutRegion
import com.mfhapps.trendingui.core.text.PreparedText
import com.mfhapps.trendingui.core.text.PositionedTextLayout
import com.mfhapps.trendingui.core.text.RectObstacle
import com.mfhapps.trendingui.core.text.TextMeasurementEngine
import com.mfhapps.trendingui.ui.components.PretextPositionedCanvas

data class ReflowCompareLayouts(
    val dynamic: PositionedTextLayout,
    val column: PositionedTextLayout,
    val previewHeightPx: Int,
)

@Composable
fun rememberReflowCompareLayouts(
    prepared: PreparedText?,
    containerWidthPx: Int,
    lineHeightPx: Float,
    obstacleLeftPx: Int,
): ReflowCompareLayouts? {
    val prep = prepared ?: return null
    return remember(prep, containerWidthPx, lineHeightPx, obstacleLeftPx) {
        val obstacleTop = lineHeightPx
        val obstacleHeight = lineHeightPx * 2.5f
        val regionHeight = lineHeightPx * 14f
        val region = LayoutRegion(
            x = 0f,
            y = 0f,
            width = containerWidthPx.toFloat().coerceAtLeast(1f),
            height = regionHeight,
        )
        val obstacleBottom = obstacleTop + obstacleHeight
        val rectObstacle = RectObstacle(
            x = obstacleLeftPx.toFloat(),
            y = obstacleTop,
            w = (containerWidthPx - obstacleLeftPx).toFloat().coerceAtLeast(1f),
            h = obstacleHeight,
        )
        val dynamic = TextMeasurementEngine.layoutDynamic(
            prepared = prep,
            region = region,
            lineHeightPx = lineHeightPx,
            maxWidthAtLineTop = { lineTop ->
                val lineBottom = lineTop + lineHeightPx
                val intersects = lineBottom > obstacleTop && lineTop < obstacleBottom
                if (intersects) {
                    obstacleLeftPx.coerceIn(80, containerWidthPx).toFloat()
                } else {
                    containerWidthPx.toFloat()
                }
            },
        )
        val column = TextMeasurementEngine.layoutColumn(
            prepared = prep,
            region = region,
            lineHeightPx = lineHeightPx,
            rectObstacles = listOf(rectObstacle),
        )
        val previewHeightPx = maxOf(dynamic.height, column.height, lineHeightPx.toInt() * 2)
        ReflowCompareLayouts(dynamic, column, previewHeightPx)
    }
}

@Composable
fun PretextReflowComparePanel(
    prepared: PreparedText?,
    containerWidthPx: Int,
    lineHeightPx: Float,
    obstacleLeftPx: Int,
    fontSizePx: Float,
    modifier: Modifier = Modifier,
) {
    val layouts = rememberReflowCompareLayouts(
        prepared = prepared,
        containerWidthPx = containerWidthPx,
        lineHeightPx = lineHeightPx,
        obstacleLeftPx = obstacleLeftPx,
    ) ?: return

    val density = LocalDensity.current
    val previewHeight = with(density) { layouts.previewHeightPx.toDp().coerceAtLeast(120.dp) }
    val obstacleTop = lineHeightPx
    val obstacleHeight = lineHeightPx * 2.5f
    val obstacleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ReflowComparePane(
                title = "layoutDynamic",
                subtitle = "Per-line max width callback",
                layout = layouts.dynamic,
                previewHeight = previewHeight,
                containerWidthPx = containerWidthPx,
                lineHeightPx = lineHeightPx,
                fontSizePx = fontSizePx,
                obstacleLeftPx = obstacleLeftPx,
                obstacleTopPx = obstacleTop,
                obstacleHeightPx = obstacleHeight,
                obstacleColor = obstacleColor,
                modifier = Modifier.weight(1f),
            )
            ReflowComparePane(
                title = "layoutColumn",
                subtitle = "Interval slots around rect",
                layout = layouts.column,
                previewHeight = previewHeight,
                containerWidthPx = containerWidthPx,
                lineHeightPx = lineHeightPx,
                fontSizePx = fontSizePx,
                obstacleLeftPx = obstacleLeftPx,
                obstacleTopPx = obstacleTop,
                obstacleHeightPx = obstacleHeight,
                obstacleColor = obstacleColor,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            "Same rect obstacle · grapheme advances prepared at measure time",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReflowComparePane(
    title: String,
    subtitle: String,
    layout: PositionedTextLayout,
    previewHeight: androidx.compose.ui.unit.Dp,
    containerWidthPx: Int,
    lineHeightPx: Float,
    fontSizePx: Float,
    obstacleLeftPx: Int,
    obstacleTopPx: Float,
    obstacleHeightPx: Float,
    obstacleColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Text(
            "${layout.lines.size} lines · ${subtitle}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight)
                .padding(top = 4.dp),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val obstacleSize = Size(
                    (containerWidthPx - obstacleLeftPx).toFloat().coerceAtLeast(1f),
                    obstacleHeightPx,
                )
                val obstacleOrigin = Offset(obstacleLeftPx.toFloat(), obstacleTopPx)
                drawRect(color = obstacleColor, topLeft = obstacleOrigin, size = obstacleSize)
                drawRect(
                    color = obstacleColor.copy(alpha = 0.9f),
                    topLeft = obstacleOrigin,
                    size = obstacleSize,
                    style = Stroke(width = 2f),
                )
            }
            PretextPositionedCanvas(
                layout = layout,
                fontSizePx = fontSizePx,
                lineHeightPx = lineHeightPx,
                modifier = Modifier.fillMaxSize(),
                textColor = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
