package com.mfhapps.trendingui.screens.copilot

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.screens.orbs.MeshPreset
import com.mfhapps.trendingui.screens.orbs.drawMeshGradient
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProvideCopilotDetailEnvironment(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        CopilotDetailBackdrop(Modifier.matchParentSize().appHazeSource())
        content()
    }
}

@Composable
fun CopilotDetailBackdrop(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val reduceMotion = LocalReduceMotion.current
    val infinite = rememberInfiniteTransition(label = "copilotAmbient")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(16_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = if (reduceMotion) 1f else 1.15f,
        animationSpec = infiniteRepeatable(tween(5_500, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    val baseGradient = remember(scheme) {
        Brush.verticalGradient(
            colors = listOf(
                scheme.primaryContainer.copy(alpha = 0.35f),
                scheme.background,
                scheme.surfaceContainerLow,
                scheme.tertiaryContainer.copy(alpha = 0.28f),
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseGradient),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            drawMeshGradient(
                preset = MeshPreset.Aurora,
                baseAlpha = 0.22f * pulse,
            )

            val glowCenters = listOf(
                Offset(w * (0.18f + drift * 0.05f), h * (0.14f + drift * 0.03f)),
                Offset(w * (0.82f - drift * 0.04f), h * (0.22f + drift * 0.02f)),
                Offset(w * (0.52f + drift * 0.03f), h * (0.62f - drift * 0.04f)),
            )
            val glowColors = listOf(
                scheme.primary.copy(alpha = 0.28f),
                scheme.tertiary.copy(alpha = 0.24f),
                scheme.secondary.copy(alpha = 0.20f),
            )
            glowCenters.forEachIndexed { index, center ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColors[index], Color.Transparent),
                        center = center,
                        radius = w * (0.34f + index * 0.04f) * pulse,
                    ),
                    radius = w * (0.34f + index * 0.04f) * pulse,
                    center = center,
                )
            }

            val gridStep = 28f
            val dotColor = scheme.onBackground.copy(alpha = 0.06f)
            var x = 0f
            while (x < w) {
                var y = 0f
                while (y < h) {
                    drawCircle(dotColor, radius = 1.2f, center = Offset(x, y))
                    y += gridStep
                }
                x += gridStep
            }

            val nodeCount = 5
            val nodes = List(nodeCount) { i ->
                val angle = (i / nodeCount.toFloat()) * 6.28f + drift * 0.8f
                val radiusX = w * (0.28f + i * 0.04f)
                val radiusY = h * (0.18f + i * 0.05f)
                Offset(
                    w * 0.5f + cos(angle) * radiusX,
                    h * 0.38f + sin(angle * 1.3f) * radiusY,
                )
            }
            val linkColor = scheme.primary.copy(alpha = 0.12f)
            nodes.forEachIndexed { i, start ->
                val end = nodes[(i + 2) % nodes.size]
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    val mid = Offset(
                        (start.x + end.x) / 2f,
                        (start.y + end.y) / 2f - h * 0.06f,
                    )
                    quadraticTo(mid.x, mid.y, end.x, end.y)
                }
                drawPath(
                    path = path,
                    color = linkColor,
                    style = Stroke(width = 1.5f, cap = StrokeCap.Round),
                )
            }
            nodes.forEach { center ->
                drawCircle(
                    color = scheme.primary.copy(alpha = 0.35f),
                    radius = 4f,
                    center = center,
                )
                drawCircle(
                    color = scheme.surface.copy(alpha = 0.9f),
                    radius = 2f,
                    center = center,
                )
            }
        }
    }
}
