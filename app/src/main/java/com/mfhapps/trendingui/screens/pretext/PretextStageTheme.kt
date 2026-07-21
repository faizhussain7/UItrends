package com.mfhapps.trendingui.screens.pretext

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

data class PretextStageTheme(
    val textColor: Color,
    val hudOnDark: Boolean,
    val showCameraGradient: Boolean,
    val monoHud: Boolean,
    val accentCyan: Color,
    val accentAmber: Color,
)

@Composable
fun PretextCameraStage.theme(): PretextStageTheme {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        PretextCameraStage.CameraOverlay -> PretextStageTheme(
            textColor = Color.White,
            hudOnDark = false,
            showCameraGradient = true,
            monoHud = false,
            accentCyan = scheme.primary,
            accentAmber = scheme.tertiary,
        )
        PretextCameraStage.TypePortrait -> PretextStageTheme(
            textColor = scheme.onPrimaryContainer,
            hudOnDark = false,
            showCameraGradient = false,
            monoHud = false,
            accentCyan = scheme.primary,
            accentAmber = scheme.tertiary,
        )
        PretextCameraStage.Studio -> PretextStageTheme(
            textColor = scheme.onSurface,
            hudOnDark = true,
            showCameraGradient = false,
            monoHud = false,
            accentCyan = scheme.primary,
            accentAmber = scheme.tertiary,
        )
        PretextCameraStage.Editorial -> PretextStageTheme(
            textColor = scheme.onSurface,
            hudOnDark = true,
            showCameraGradient = false,
            monoHud = false,
            accentCyan = scheme.primary,
            accentAmber = scheme.tertiary,
        )
        PretextCameraStage.Generative -> PretextStageTheme(
            textColor = scheme.onSurface,
            hudOnDark = true,
            showCameraGradient = false,
            monoHud = false,
            accentCyan = scheme.primary,
            accentAmber = scheme.tertiary,
        )
    }
}

val PretextTerminalMono: FontFamily = FontFamily.Monospace

@Composable
fun PretextCameraStage.hudFontWeight(): FontWeight =
    if (theme().monoHud) FontWeight.Medium else FontWeight.Normal
