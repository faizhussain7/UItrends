package com.mfhapps.trendingui.screens.spatial

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.appHazeSource

@Composable
fun ProvideSpatialDetailEnvironment(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ProvideSpatialChrome {
        Box(modifier.fillMaxSize()) {
            SpatialDepthBackdrop(Modifier.matchParentSize().appHazeSource())
            content()
        }
    }
}

@Composable
fun SpatialDepthBackdrop(modifier: Modifier = Modifier) {
    val chrome = spatialChromeOrRemember()
    val tilt = rememberSpatialTiltDegrees(sensitivity = 0.18f)
    val reduceMotion = LocalReduceMotion.current
    val animateDrift = !reduceMotion
    val infinite = rememberInfiniteTransition(label = "spatialDrift")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (animateDrift) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(14_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )

    val pitchShift = if (tilt.enabled) tilt.pitch * 2.4f else 0f
    val rollShift = if (tilt.enabled) tilt.roll * 2.4f else 0f

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to chrome.atmosphereTop,
                        0.42f to chrome.atmosphereMid,
                        1f to chrome.atmosphereBottom,
                    ),
                ),
            )

            drawAtmosphericFog(
                fog = chrome.fog,
                drift = drift,
                pitchShift = pitchShift,
            )

            withTransform({
                translate(left = rollShift * 1.6f, top = pitchShift * 0.9f)
            }) {
                drawPerspectiveFloor(
                    gridColor = chrome.gridLine,
                    frameColor = chrome.frameStroke,
                    strokeWidth = if (chrome.isDark) 1f else 1.2f,
                    drift = drift,
                )
            }

            drawDepthPlanes(
                near = chrome.planeNear,
                mid = chrome.planeMid,
                far = chrome.planeFar,
                drift = drift,
                rollShift = rollShift,
                pitchShift = pitchShift,
            )

            drawDepthBands(
                primary = chrome.bandPrimary,
                tertiary = chrome.bandTertiary,
                drift = drift,
                pitchShift = pitchShift,
            )

            drawAmbientGlows(
                secondary = chrome.glowSecondary,
                primary = chrome.glowPrimary,
                drift = drift,
                rollShift = rollShift,
                pitchShift = pitchShift,
            )

            drawVignette(isDark = chrome.isDark)
        }
    }
}

private fun DrawScope.drawAtmosphericFog(
    fog: Color,
    drift: Float,
    pitchShift: Float,
) {
    val horizon = size.height * (0.34f + pitchShift * 0.002f)
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                0.55f to fog.copy(alpha = fog.alpha * 0.35f),
                1f to fog,
            ),
            startY = horizon - size.height * 0.08f,
            endY = size.height,
        ),
    )
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                fog.copy(alpha = fog.alpha * 0.25f),
                Color.Transparent,
                fog.copy(alpha = fog.alpha * 0.18f),
            ),
            startX = size.width * (0.08f + drift * 0.04f),
            endX = size.width * (0.92f - drift * 0.03f),
        ),
        topLeft = Offset(0f, horizon - size.height * 0.05f),
        size = Size(size.width, size.height * 0.18f),
    )
}

private fun DrawScope.drawPerspectiveFloor(
    gridColor: Color,
    frameColor: Color,
    strokeWidth: Float,
    drift: Float,
) {
    val w = size.width
    val h = size.height
    val vanishY = h * (0.32f + drift * 0.012f)
    val cols = 10
    for (i in 0..cols) {
        val t = i / cols.toFloat()
        val x = w * t
        drawLine(
            color = gridColor,
            start = Offset(x, vanishY),
            end = Offset(w * 0.5f + (x - w * 0.5f) * 1.42f, h),
            strokeWidth = strokeWidth,
        )
    }
    val rows = 7
    for (j in 0..rows) {
        val t = j / rows.toFloat()
        val ease = t * t
        val y = vanishY + (h - vanishY) * ease
        drawLine(
            color = gridColor.copy(alpha = gridColor.alpha * (0.55f + ease * 0.45f)),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = strokeWidth,
        )
    }
    drawRoundRect(
        color = frameColor,
        topLeft = Offset(w * 0.07f, h * 0.10f),
        size = Size(w * 0.86f, h * 0.78f),
        cornerRadius = CornerRadius(56f, 56f),
        style = Stroke(width = strokeWidth.coerceAtLeast(1.5f)),
    )
}

private fun DrawScope.drawDepthPlanes(
    near: Color,
    mid: Color,
    far: Color,
    drift: Float,
    rollShift: Float,
    pitchShift: Float,
) {
    val w = size.width
    val h = size.height
    val planes = listOf(
        Triple(far, 0.18f, 0.55f),
        Triple(mid, 0.34f, 0.42f),
        Triple(near, 0.52f, 0.30f),
    )
    planes.forEachIndexed { index, (color, yFrac, widthFrac) ->
        val planeW = w * widthFrac
        val x = w * (0.5f - widthFrac / 2f) + rollShift * (index + 1) * 0.8f
        val y = h * yFrac + drift * (8f + index * 4f) + pitchShift * (index + 1) * 0.35f
        drawRoundRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(planeW, h * (0.045f + index * 0.012f)),
            cornerRadius = CornerRadius(28f, 28f),
        )
        drawRoundRect(
            color = color.copy(alpha = color.alpha * 1.35f),
            topLeft = Offset(x, y),
            size = Size(planeW, h * (0.045f + index * 0.012f)),
            cornerRadius = CornerRadius(28f, 28f),
            style = Stroke(width = 1.25f),
        )
    }
}

private fun DrawScope.drawDepthBands(
    primary: Color,
    tertiary: Color,
    drift: Float,
    pitchShift: Float,
) {
    val w = size.width
    val h = size.height
    val shift = drift * 20f + pitchShift * 0.8f
    listOf(0.24f, 0.50f, 0.76f).forEachIndexed { layer, frac ->
        val y = h * frac + shift * (layer + 1) * 0.28f
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    primary.copy(alpha = primary.alpha - layer * 0.03f),
                    tertiary.copy(alpha = tertiary.alpha - layer * 0.02f),
                    Color.Transparent,
                ),
            ),
            topLeft = Offset(0f, y - h * 0.07f),
            size = Size(w, h * 0.14f),
        )
    }
}

private fun DrawScope.drawAmbientGlows(
    secondary: Color,
    primary: Color,
    drift: Float,
    rollShift: Float,
    pitchShift: Float,
) {
    val w = size.width
    val h = size.height
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(secondary, Color.Transparent),
            center = Offset(
                w * (0.74f + drift * 0.035f) + rollShift * 1.2f,
                h * 0.20f + pitchShift * 0.6f,
            ),
            radius = w * 0.48f,
        ),
        size = size,
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(primary, Color.Transparent),
            center = Offset(
                w * (0.16f - drift * 0.03f) - rollShift * 0.9f,
                h * 0.80f - pitchShift * 0.5f,
            ),
            radius = w * 0.40f,
        ),
        size = size,
    )
}

private fun DrawScope.drawVignette(isDark: Boolean) {
    val edge = if (isDark) Color.Black.copy(alpha = 0.34f) else Color.Black.copy(alpha = 0.10f)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, edge),
            center = Offset(size.width * 0.5f, size.height * 0.46f),
            radius = size.maxDimension * 0.72f,
        ),
        size = size,
    )
}
