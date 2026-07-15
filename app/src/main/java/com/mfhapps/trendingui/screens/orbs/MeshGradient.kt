package com.mfhapps.trendingui.screens.orbs

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

enum class MeshPreset(
    val title: String,
    val colors: List<Color>,
    val anchors: List<Offset>,
) {
    Aurora(
        title = "Aurora",
        colors = listOf(
            Color(0xFF7F77DD),
            Color(0xFF1D9E75),
            Color(0xFFD4537E),
            Color(0xFF4FA8D8),
        ),
        anchors = listOf(
            Offset(0.12f, 0.18f),
            Offset(0.88f, 0.22f),
            Offset(0.20f, 0.82f),
            Offset(0.78f, 0.78f),
        ),
    ),
    Sunset(
        title = "Sunset",
        colors = listOf(
            Color(0xFFFF6B4A),
            Color(0xFFFFB347),
            Color(0xFFD85A30),
            Color(0xFF8E44AD),
        ),
        anchors = listOf(
            Offset(0.08f, 0.35f),
            Offset(0.92f, 0.15f),
            Offset(0.50f, 0.90f),
            Offset(0.72f, 0.55f),
        ),
    ),
    Brand(
        title = "Brand",
        colors = listOf(
            Color(0xFFD42D6A),
            Color(0xFF4F6D95),
            Color(0xFFA61E52),
            Color(0xFFFFFCF9),
        ),
        anchors = listOf(
            Offset(0.15f, 0.25f),
            Offset(0.85f, 0.30f),
            Offset(0.35f, 0.75f),
            Offset(0.70f, 0.70f),
        ),
    ),
    Midnight(
        title = "Midnight",
        colors = listOf(
            Color(0xFF151028),
            Color(0xFF2A3A6B),
            Color(0xFF0C1218),
            Color(0xFF4A2C6E),
        ),
        anchors = listOf(
            Offset(0.0f, 0.0f),
            Offset(1f, 0.0f),
            Offset(0.0f, 1f),
            Offset(1f, 1f),
        ),
    ),
}

fun DrawScope.drawMeshGradient(
    preset: MeshPreset,
    baseAlpha: Float = 1f,
    blendMode: BlendMode = BlendMode.Screen,
    parallax: Offset = Offset.Zero,
) {
    drawMeshGradient(preset.toField(), baseAlpha, blendMode, parallax)
}

fun DrawScope.drawMeshGradient(
    field: MeshColorField,
    baseAlpha: Float = 1f,
    blendMode: BlendMode = BlendMode.Screen,
    parallax: Offset = Offset.Zero,
) {
    val w = size.width
    val h = size.height
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                field.colors.first().copy(alpha = 0.92f * baseAlpha),
                field.colors.last().copy(alpha = 0.88f * baseAlpha),
            ),
        ),
        size = Size(w, h),
    )
    field.colors.forEachIndexed { index, color ->
        val anchor = field.anchors[index]
        val cx = w * (anchor.x + parallax.x * 0.08f).coerceIn(0f, 1f)
        val cy = h * (anchor.y + parallax.y * 0.08f).coerceIn(0f, 1f)
        val radius = size.minDimension * (0.48f + index * 0.05f)
        val peakAlpha = (color.alpha.coerceAtLeast(0.82f) * baseAlpha * 0.92f).coerceIn(0f, 0.94f)
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to color.copy(alpha = peakAlpha),
                    0.45f to color.copy(alpha = peakAlpha * 0.55f),
                    1f to Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = radius,
            ),
            radius = radius,
            center = Offset(cx, cy),
            blendMode = blendMode,
        )
    }
}

fun DrawScope.drawGlowOrbs(
    colors: List<Color>,
    normalizedCenters: List<Offset>,
    blendMode: BlendMode = BlendMode.Screen,
    strength: Float = 1f,
    radiusFraction: Float = 0.28f,
    vividCore: Boolean = false,
) {
    val strengthClamped = strength.coerceIn(0.5f, 1.35f)
    normalizedCenters.forEachIndexed { index, norm ->
        val color = colors[index % colors.size]
        val center = Offset(size.width * norm.x, size.height * norm.y)
        val radius = size.minDimension * radiusFraction * strengthClamped
        val coreAlpha = (0.88f * strengthClamped).coerceIn(0f, 0.96f)

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to color.copy(alpha = coreAlpha),
                    0.32f to color.copy(alpha = coreAlpha * 0.78f),
                    0.62f to color.copy(alpha = coreAlpha * 0.45f),
                    1f to Color.Transparent,
                ),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
            blendMode = blendMode,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to color.copy(alpha = coreAlpha * 0.55f),
                    1f to Color.Transparent,
                ),
                center = center,
                radius = radius * 1.55f,
            ),
            radius = radius * 1.55f,
            center = center,
            blendMode = blendMode,
        )
        if (vividCore) {
            drawCircle(
                color = color.copy(alpha = (coreAlpha * 0.92f).coerceIn(0f, 1f)),
                radius = radius * 0.2f,
                center = center,
                blendMode = blendMode,
            )
        }
    }
}