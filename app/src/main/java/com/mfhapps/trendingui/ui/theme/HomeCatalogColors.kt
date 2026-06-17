package com.mfhapps.trendingui.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class HomeCatalogColors(
    val headerTitle: Color,
    val headerSubtitle: Color,
    val headerIcon: Color,
    val exploreTitle: Color,
    val exploreSubtitle: Color,
    val sectionOnGradient: Color,
    val glassBarTitle: Color,
    val glassBarSubtitle: Color,
    val glassBarIcon: Color,
    val glassBarShadow: Color,
    val useLightStatusBarIcons: Boolean,
)

val LocalHomeCatalogColors = staticCompositionLocalOf<HomeCatalogColors> {
    error("HomeCatalogColors not provided — use LocalCatalogAppearance")
}

fun HomeCatalogColors.foregroundFor(
    role: GradientForegroundRole,
    style: VibrantForegroundStyle = VibrantForegroundStyle.OnGradient,
): Color = when (style) {
    VibrantForegroundStyle.OnGradient -> when (role) {
        GradientForegroundRole.Title -> headerTitle
        GradientForegroundRole.Subtitle -> headerSubtitle
        GradientForegroundRole.Section -> sectionOnGradient
        GradientForegroundRole.Icon -> headerIcon
    }
    VibrantForegroundStyle.OnLiquidGlass -> when (role) {
        GradientForegroundRole.Title -> glassBarTitle
        GradientForegroundRole.Subtitle -> glassBarSubtitle
        GradientForegroundRole.Section -> glassBarTitle
        GradientForegroundRole.Icon -> glassBarIcon
    }
}
