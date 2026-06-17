package com.mfhapps.trendingui.screens.spatial

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Immutable
data class SpatialChrome(
    val isDark: Boolean,
    val readableSurface: Color,
    val readableSurfaceStrong: Color,
    val cardSurface: Color,
    val cardSurfaceSelected: Color,
    val gridAlpha: Float,
    val bandPrimaryAlpha: Float,
    val bandTertiaryAlpha: Float,
    val glowSecondaryAlpha: Float,
    val glowPrimaryAlpha: Float,
    val frameAlpha: Float,
    val backgroundScrim: Float,
)

@Composable
fun rememberSpatialChrome(): SpatialChrome {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) { buildSpatialChrome(scheme) }
}

fun buildSpatialChrome(scheme: ColorScheme): SpatialChrome {
    val isDark = scheme.background.luminance() < 0.45f
    return SpatialChrome(
        isDark = isDark,
        readableSurface = scheme.surface.copy(alpha = if (isDark) 0.90f else 0.94f),
        readableSurfaceStrong = scheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.94f else 0.97f),
        cardSurface = scheme.surface.copy(alpha = if (isDark) 0.78f else 0.88f),
        cardSurfaceSelected = scheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.92f else 0.96f),
        gridAlpha = if (isDark) 0.06f else 0.11f,
        bandPrimaryAlpha = if (isDark) 0.14f else 0.22f,
        bandTertiaryAlpha = if (isDark) 0.12f else 0.18f,
        glowSecondaryAlpha = if (isDark) 0.28f else 0.34f,
        glowPrimaryAlpha = if (isDark) 0.22f else 0.28f,
        frameAlpha = if (isDark) 0.25f else 0.32f,
        backgroundScrim = if (isDark) 0.35f else 0.20f,
    )
}
