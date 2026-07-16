package com.mfhapps.trendingui.screens.spatial

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

@Immutable
data class SpatialChrome(
    val isDark: Boolean,
    val atmosphereTop: Color,
    val atmosphereMid: Color,
    val atmosphereBottom: Color,
    val fog: Color,
    val gridLine: Color,
    val frameStroke: Color,
    val bandPrimary: Color,
    val bandTertiary: Color,
    val glowSecondary: Color,
    val glowPrimary: Color,
    val planeNear: Color,
    val planeMid: Color,
    val planeFar: Color,
    val readableSurface: Color,
    val readableSurfaceStrong: Color,
    val cardSurface: Color,
    val cardSurfaceSelected: Color,
    val cardBorder: Color,
    val cardBorderSelected: Color,
    val badgeContainer: Color,
    val badgeLabel: Color,
    val meterActive: Color,
    val meterIdle: Color,
    val sectionTitle: Color,
    val body: Color,
    val muted: Color,
    val accent: Color,
    val headerSticky: Color,
    val headerStickyLabel: Color,
    val topBarIcon: Color,
)

val LocalSpatialChrome = staticCompositionLocalOf<SpatialChrome?> { null }

@Composable
fun rememberSpatialChrome(): SpatialChrome {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) { buildSpatialChrome(scheme) }
}

@Composable
fun ProvideSpatialChrome(
    chrome: SpatialChrome = rememberSpatialChrome(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSpatialChrome provides chrome, content = content)
}

@Composable
fun spatialChromeOrRemember(): SpatialChrome =
    LocalSpatialChrome.current ?: rememberSpatialChrome()

fun buildSpatialChrome(scheme: ColorScheme): SpatialChrome {
    val isDark = scheme.background.luminance() < 0.45f
    val atmosphereTop = lerp(scheme.background, scheme.primary, if (isDark) 0.22f else 0.10f)
    val atmosphereMid = lerp(scheme.background, scheme.secondary, if (isDark) 0.16f else 0.08f)
    val atmosphereBottom = lerp(scheme.background, scheme.tertiary, if (isDark) 0.28f else 0.14f)
    val fog = lerp(
        atmosphereBottom,
        if (isDark) Color.Black else Color.White,
        if (isDark) 0.35f else 0.18f,
    )
    val cardSurface = scheme.surface.copy(alpha = if (isDark) 0.78f else 0.90f)
    val cardSurfaceSelected = scheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.94f else 0.97f)
    val badgeContainer = scheme.primaryContainer.copy(alpha = if (isDark) 0.78f else 0.92f)

    return SpatialChrome(
        isDark = isDark,
        atmosphereTop = atmosphereTop,
        atmosphereMid = atmosphereMid,
        atmosphereBottom = atmosphereBottom,
        fog = fog.copy(alpha = if (isDark) 0.55f else 0.42f),
        gridLine = scheme.onBackground.copy(alpha = if (isDark) 0.07f else 0.12f),
        frameStroke = scheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.36f),
        bandPrimary = scheme.primary.copy(alpha = if (isDark) 0.16f else 0.20f),
        bandTertiary = scheme.tertiary.copy(alpha = if (isDark) 0.12f else 0.16f),
        glowSecondary = scheme.secondary.copy(alpha = if (isDark) 0.30f else 0.28f),
        glowPrimary = scheme.primary.copy(alpha = if (isDark) 0.24f else 0.22f),
        planeNear = scheme.primary.copy(alpha = if (isDark) 0.10f else 0.12f),
        planeMid = scheme.secondary.copy(alpha = if (isDark) 0.08f else 0.10f),
        planeFar = scheme.tertiary.copy(alpha = if (isDark) 0.06f else 0.08f),
        readableSurface = scheme.surface.copy(alpha = if (isDark) 0.90f else 0.94f),
        readableSurfaceStrong = scheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.94f else 0.97f),
        cardSurface = cardSurface,
        cardSurfaceSelected = cardSurfaceSelected,
        cardBorder = scheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.45f),
        cardBorderSelected = scheme.primary.copy(alpha = if (isDark) 0.55f else 0.48f),
        badgeContainer = badgeContainer,
        badgeLabel = scheme.onPrimaryContainer,
        meterActive = scheme.primary.copy(alpha = if (isDark) 0.88f else 0.78f),
        meterIdle = scheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.70f else 0.85f),
        sectionTitle = scheme.onSurface,
        body = scheme.onSurfaceVariant,
        muted = scheme.onSurfaceVariant.copy(alpha = 0.78f),
        accent = scheme.primary,
        headerSticky = scheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.88f else 0.94f),
        headerStickyLabel = scheme.onSurface,
        topBarIcon = scheme.primary,
    )
}
