package com.mfhapps.trendingui.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class CatalogAppearance(
    val gradients: BrandGradients,
    val colors: HomeCatalogColors,
)

val LocalCatalogAppearance = staticCompositionLocalOf<CatalogAppearance> {
    error("CatalogAppearance not provided")
}

@Composable
fun rememberCatalogAppearance(
    gradientStyle: HomeGradientStyle,
    customGradient: CustomGradientConfig,
    colorScheme: ColorScheme,
    darkTheme: Boolean,
): CatalogAppearance {
    val config = customGradient.normalized()
    return remember(gradientStyle, config, colorScheme, darkTheme) {
        val gradients = gradientStyle.brushes(colorScheme, darkTheme, config)
        val colors = CatalogColorMath.resolveGradientChromeColors(
            chromeSamples = gradients.chromeBackdropSamples,
            statusBarBackdrop = gradients.statusBarBackdrop,
            colorScheme = colorScheme,
        )
        CatalogAppearance(gradients = gradients, colors = colors)
    }
}
