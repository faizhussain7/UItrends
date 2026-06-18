package com.mfhapps.trendingui.screens.spatial

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.theme.launcherIconGradientBackground

@Composable
fun ProvideSpatialDetailEnvironment(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        SpatialDepthBackdrop(Modifier.matchParentSize().appHazeSource())
        content()
    }
}

@Composable
fun SpatialDepthBackdrop(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val chrome = rememberSpatialChrome()
    val reduceMotion = LocalReduceMotion.current
    val animateDrift = !reduceMotion
    val infinite = rememberInfiniteTransition(label = "spatialDrift")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (animateDrift) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )

    val gridColor = remember(scheme, chrome.gridAlpha) {
        scheme.onBackground.copy(alpha = chrome.gridAlpha)
    }
    val frameColor = remember(scheme, chrome.frameAlpha) {
        scheme.outlineVariant.copy(alpha = chrome.frameAlpha)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .launcherIconGradientBackground()
            .background(scheme.background.copy(alpha = chrome.backgroundScrim)),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawStaticSpatialGrid(
                gridColor = gridColor,
                frameColor = frameColor,
                strokeWidth = if (chrome.isDark) 1f else 1.25f,
            )

            val w = size.width
            val h = size.height
            val shift = drift * 24f

            listOf(0.22f, 0.48f, 0.74f).forEachIndexed { layer, frac ->
                val y = h * frac + shift * (layer + 1) * 0.35f
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            scheme.primary.copy(alpha = chrome.bandPrimaryAlpha - layer * 0.03f),
                            scheme.tertiary.copy(alpha = chrome.bandTertiaryAlpha - layer * 0.02f),
                            Color.Transparent,
                        ),
                    ),
                    topLeft = Offset(0f, y - h * 0.08f),
                    size = Size(w, h * 0.16f),
                )
            }

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        scheme.secondary.copy(alpha = chrome.glowSecondaryAlpha),
                        Color.Transparent,
                    ),
                    center = Offset(w * (0.72f + drift * 0.04f), h * 0.22f),
                    radius = w * 0.45f,
                ),
                size = size,
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        scheme.primary.copy(alpha = chrome.glowPrimaryAlpha),
                        Color.Transparent,
                    ),
                    center = Offset(w * (0.18f - drift * 0.03f), h * 0.78f),
                    radius = w * 0.38f,
                ),
                size = size,
            )
        }
    }
}

private fun DrawScope.drawStaticSpatialGrid(
    gridColor: Color,
    frameColor: Color,
    strokeWidth: Float,
) {
    val w = size.width
    val h = size.height
    val vanishY = h * 0.35f
    val cols = 8
    for (i in 0..cols) {
        val t = i / cols.toFloat()
        val x = w * t
        drawLine(
            gridColor,
            Offset(x, vanishY),
            Offset(w * 0.5f + (x - w * 0.5f) * 1.35f, h),
            strokeWidth = strokeWidth,
        )
    }
    val rows = 6
    for (j in 0..rows) {
        val t = j / rows.toFloat()
        val y = vanishY + (h - vanishY) * t
        drawLine(
            gridColor,
            Offset(0f, y),
            Offset(w, y),
            strokeWidth = strokeWidth,
        )
    }
    drawRoundRect(
        color = frameColor,
        topLeft = Offset(w * 0.08f, h * 0.12f),
        size = Size(w * 0.84f, h * 0.76f),
        cornerRadius = CornerRadius(48f, 48f),
        style = Stroke(width = strokeWidth.coerceAtLeast(1.5f)),
    )
}
