package com.mfhapps.trendingui.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

@Composable
fun rememberDynamicHomeScreenBrush(
    gradients: BrandGradients,
    colorScheme: ColorScheme,
    reduceMotion: Boolean,
): Brush {
    val palette = gradients.motionPalette
    val infinite = rememberInfiniteTransition(label = "homeGradientMotion")
    val wave by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave",
    )
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )

    return remember(palette, colorScheme, wave, drift, reduceMotion) {
        val p = palette.ifEmpty { listOf(colorScheme.primary, colorScheme.tertiary) }
        val c0 = lerp(p[0], p[p.lastIndex.coerceAtLeast(0)], wave * 0.35f)
        val c1 = lerp(p[1 % p.size], p[0], drift * 0.4f)
        val c2 = lerp(p[p.lastIndex], p[1 % p.size], (wave + drift) * 0.25f)
        Brush.linearGradient(
            colors = listOf(
                c0,
                c1,
                c2,
                colorScheme.surfaceContainerLow,
                colorScheme.background,
            ),
            start = Offset(0f, 0f),
            end = Offset(
                x = 960f + drift * 520f,
                y = 2200f + wave * 480f,
            ),
        )
    }
}
