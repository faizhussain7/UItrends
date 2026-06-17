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
    val showAsciiOverlay: Boolean,
    val showGenerativeObstacle: Boolean,
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
            showAsciiOverlay = false,
            showGenerativeObstacle = false,
            monoHud = false,
            accentCyan = scheme.primary,
            accentAmber = scheme.tertiary,
        )
        PretextCameraStage.Studio -> PretextStageTheme(
            textColor = scheme.onSurface,
            hudOnDark = true,
            showCameraGradient = false,
            showAsciiOverlay = false,
            showGenerativeObstacle = false,
            monoHud = false,
            accentCyan = scheme.primary,
            accentAmber = scheme.tertiary,
        )
        PretextCameraStage.Editorial -> PretextStageTheme(
            textColor = scheme.onSurface,
            hudOnDark = true,
            showCameraGradient = false,
            showAsciiOverlay = false,
            showGenerativeObstacle = false,
            monoHud = false,
            accentCyan = scheme.primary,
            accentAmber = scheme.tertiary,
        )
        PretextCameraStage.Terminal -> PretextStageTheme(
            textColor = Color(0xFFE8F4F8),
            hudOnDark = false,
            showCameraGradient = false,
            showAsciiOverlay = false,
            showGenerativeObstacle = false,
            monoHud = true,
            accentCyan = Color(0xFF4FD1C5),
            accentAmber = Color(0xFFFFB020),
        )
        PretextCameraStage.Generative -> PretextStageTheme(
            textColor = scheme.onSurface,
            hudOnDark = true,
            showCameraGradient = false,
            showAsciiOverlay = false,
            showGenerativeObstacle = true,
            monoHud = false,
            accentCyan = scheme.primary,
            accentAmber = scheme.tertiary,
        )
        PretextCameraStage.Ascii -> PretextStageTheme(
            textColor = Color(0xFF9AE6B4),
            hudOnDark = false,
            showCameraGradient = false,
            showAsciiOverlay = true,
            showGenerativeObstacle = false,
            monoHud = true,
            accentCyan = Color(0xFF4FD1C5),
            accentAmber = Color(0xFFFFB020),
        )
    }
}

val PretextTerminalMono: FontFamily = FontFamily.Monospace

@Composable
fun PretextCameraStage.hudFontWeight(): FontWeight =
    if (theme().monoHud) FontWeight.Medium else FontWeight.Normal
