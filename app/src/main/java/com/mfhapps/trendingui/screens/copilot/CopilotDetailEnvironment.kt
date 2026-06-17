package com.mfhapps.trendingui.screens.copilot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.mfhapps.trendingui.screens.orbs.MeshPreset
import com.mfhapps.trendingui.screens.orbs.drawMeshGradient

@Composable
fun ProvideCopilotDetailEnvironment(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        CopilotDetailBackdrop(Modifier.matchParentSize())
        content()
    }
}

@Composable
fun CopilotDetailBackdrop(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        scheme.surfaceContainerLow,
                        scheme.background,
                        scheme.surfaceContainerHighest.copy(alpha = 0.65f),
                    ),
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawMeshGradient(
                preset = MeshPreset.Brand,
                baseAlpha = 0.55f,
            )
            val w = size.width
            val h = size.height
            repeat(3) { page ->
                val top = h * (0.08f + page * 0.28f)
                drawRoundRect(
                    color = scheme.surface.copy(alpha = 0.42f - page * 0.08f),
                    topLeft = Offset(w * 0.06f, top),
                    size = Size(w * 0.88f, h * 0.22f),
                    cornerRadius = CornerRadius(20f, 20f),
                )
                drawRoundRect(
                    color = scheme.primary.copy(alpha = 0.06f),
                    topLeft = Offset(w * 0.1f, top + 18f),
                    size = Size(w * 0.5f, 6f),
                    cornerRadius = CornerRadius(3f, 3f),
                )
            }
        }
    }
}
