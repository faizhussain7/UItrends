package com.mfhapps.trendingui.screens.orbs

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp

@Immutable
data class OrbsChrome(
    val isDark: Boolean,
    val backdropField: MeshColorField,
    val playgroundOrbColors: List<Color>,
    val backdropOrbColors: List<Color>,
    val meshBlendMode: BlendMode,
    val orbBlendMode: BlendMode,
    val readableSurface: Color,
    val readableSurfaceStrong: Color,
    val titleColor: Color,
    val bodyColor: Color,
    val sectionAccent: Color,
    val cardScrim: Float,
    val playgroundScrim: Float,
)

@Immutable
data class MeshColorField(
    val title: String,
    val colors: List<Color>,
    val anchors: List<Offset>,
)

@Composable
fun rememberOrbsChrome(): OrbsChrome {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) { buildOrbsChrome(scheme) }
}

fun buildOrbsChrome(scheme: ColorScheme): OrbsChrome {
    val isDark = scheme.background.luminance() < 0.45f
    val base = if (isDark) MeshPreset.Midnight else MeshPreset.Aurora
    return OrbsChrome(
        isDark = isDark,
        backdropField = base.themeTinted(scheme),
        playgroundOrbColors = themePlaygroundOrbColors(scheme, isDark),
        backdropOrbColors = themeBackdropOrbColors(scheme, isDark),
        meshBlendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
        orbBlendMode = if (isDark) BlendMode.Screen else BlendMode.Plus,
        readableSurface = scheme.surface.copy(alpha = if (isDark) 0.90f else 0.94f),
        readableSurfaceStrong = scheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.94f else 0.97f),
        titleColor = scheme.onSurface,
        bodyColor = scheme.onSurfaceVariant,
        sectionAccent = scheme.primary,
        cardScrim = if (isDark) 0.22f else 0.28f,
        playgroundScrim = if (isDark) 0.14f else 0.20f,
    )
}

fun MeshPreset.themeTinted(scheme: ColorScheme, mix: Float = Float.NaN): MeshColorField {
    val isDark = scheme.background.luminance() < 0.45f
    val effectiveMix = if (mix.isNaN()) {
        if (isDark) 0.62f else 0.38f
    } else {
        mix
    }
    val themeColors = listOf(
        scheme.primaryContainer,
        scheme.secondaryContainer,
        scheme.tertiaryContainer,
        scheme.surfaceContainerHighest,
    )
    return MeshColorField(
        title = title,
        colors = colors.mapIndexed { index, presetColor ->
            lerp(presetColor, themeColors[index % themeColors.size], effectiveMix)
        },
        anchors = anchors,
    )
}

fun MeshPreset.toField(): MeshColorField = MeshColorField(title, colors, anchors)

private fun orbTint(color: Color, alpha: Float, isDark: Boolean): Color {
    val tuned = if (!isDark && color.luminance() > 0.58f) {
        lerp(color, Color(0xFF243048), 0.22f)
    } else {
        color
    }
    return tuned.copy(alpha = alpha)
}

private fun themePlaygroundOrbColors(scheme: ColorScheme, isDark: Boolean): List<Color> {
    val alpha = if (isDark) 0.72f else 0.82f
    val sources = if (isDark) {
        listOf(scheme.primary, scheme.secondary, scheme.tertiary, scheme.primary)
    } else {
        listOf(
            lerp(Color(0xFF8F87F5), scheme.primary, 0.30f),
            lerp(Color(0xFF2AB88A), scheme.secondary, 0.30f),
            lerp(Color(0xFFE85A8A), scheme.tertiary, 0.30f),
            lerp(Color(0xFF5EB4F0), scheme.primary, 0.30f),
        )
    }
    return sources.map { source ->
        if (isDark) orbTint(source, alpha, isDark = true) else source.copy(alpha = alpha)
    }
}

private fun themeBackdropOrbColors(scheme: ColorScheme, isDark: Boolean): List<Color> {
    val alpha = if (isDark) 0.50f else 0.54f
    return listOf(
        orbTint(scheme.primary, alpha, isDark),
        orbTint(scheme.secondary, alpha, isDark),
        orbTint(scheme.tertiary, alpha, isDark),
        orbTint(scheme.primary, alpha * 0.85f, isDark),
    )
}
