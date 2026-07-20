package com.mfhapps.trendingui.screens.pretext

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal object PretextRecordingTheme {
    val overlayContent = Color.White
    val overlayScrim = Color.Black.copy(alpha = 0.68f)
    val overlayScrimSoft = Color.Black.copy(alpha = 0.52f)

    @Composable
    fun cardShareContainer() = MaterialTheme.colorScheme.surfaceContainerHighest

    @Composable
    fun cardShareContent() = MaterialTheme.colorScheme.onSurface

    @Composable
    fun cardShareAccent() = MaterialTheme.colorScheme.primary
}

internal enum class PretextShareSurfaceStyle {
    Card,
    PlayerOverlay,
    Sheet,
}

internal data class PretextShareColors(
    val container: Color,
    val content: Color,
    val accent: Color,
)

@Composable
internal fun PretextShareSurfaceStyle.colors(): PretextShareColors = when (this) {
    PretextShareSurfaceStyle.Card -> PretextShareColors(
        container = PretextRecordingTheme.cardShareContainer(),
        content = PretextRecordingTheme.cardShareContent(),
        accent = PretextRecordingTheme.cardShareAccent(),
    )
    PretextShareSurfaceStyle.PlayerOverlay -> PretextShareColors(
        container = PretextRecordingTheme.overlayScrimSoft,
        content = PretextRecordingTheme.overlayContent,
        accent = PretextRecordingTheme.overlayContent,
    )
    PretextShareSurfaceStyle.Sheet -> PretextShareColors(
        container = MaterialTheme.colorScheme.surfaceContainerHighest,
        content = MaterialTheme.colorScheme.onSurface,
        accent = MaterialTheme.colorScheme.primary,
    )
}
