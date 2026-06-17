package com.mfhapps.trendingui.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp


@Immutable
data class SystemBarAppearance(
    val lightStatusBarIcons: Boolean,
    val lightNavigationBarIcons: Boolean,
) {
    companion object {
        fun forBackdrop(
            backdrop: Color,
            samples: List<Color> = listOf(backdrop),
        ): SystemBarAppearance {
            val lightStatus = StatusBarIconPolicy.prefersLightIcons(samples)
            val lightNav = StatusBarIconPolicy.prefersLightIcons(listOf(backdrop))
            return SystemBarAppearance(
                lightStatusBarIcons = lightStatus,
                lightNavigationBarIcons = lightNav,
            )
        }

        fun forGradientChrome(
            statusBarBackdrop: Color,
            chromeSamples: List<Color>,
            navigationBackdrop: Color,
        ): SystemBarAppearance = SystemBarAppearance(
            lightStatusBarIcons = StatusBarIconPolicy.prefersLightIcons(chromeSamples),
            lightNavigationBarIcons = StatusBarIconPolicy.prefersLightIcons(listOf(navigationBackdrop)),
        )

        fun forCollapsingToolbar(
            collapsedFraction: Float,
            background: Color,
            surface: Color,
        ): SystemBarAppearance {
            val fraction = collapsedFraction.coerceIn(0f, 1f)
            val statusBackdrop = lerp(background, surface, fraction)
            return SystemBarAppearance(
                lightStatusBarIcons = StatusBarIconPolicy.prefersLightIcons(listOf(statusBackdrop)),
                lightNavigationBarIcons = StatusBarIconPolicy.prefersLightIcons(listOf(background)),
            )
        }

        fun forImmersiveOverlay(
            background: Color,
            overlaySample: Color,
        ): SystemBarAppearance = SystemBarAppearance(
            lightStatusBarIcons = StatusBarIconPolicy.prefersLightIcons(listOf(overlaySample)),
            lightNavigationBarIcons = StatusBarIconPolicy.prefersLightIcons(listOf(background)),
        )
    }
}

object StatusBarIconPolicy {
    fun prefersLightIcons(backdrop: Color): Boolean = prefersLightIcons(listOf(backdrop))

    fun prefersLightIcons(samples: List<Color>): Boolean =
        CatalogColorMath.prefersLightStatusBarIcons(samples)
}
