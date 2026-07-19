package com.mfhapps.trendingui.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class BrandGradients(
    val screen: Brush,
    val toolbar: Brush,
    val statusBarBackdrop: Color,
    val screenBackdrop: GradientBackdrop,
    val chromeBackdropSamples: List<Color> = listOf(statusBarBackdrop),
    val motionPalette: List<Color> = emptyList(),
)

val LocalBrandGradients = staticCompositionLocalOf<BrandGradients> {
    error("BrandGradients not provided")
}

internal val BrandLightScheme = lightColorScheme(
    primary = Rose40,
    onPrimary = Color.White,
    primaryContainer = Rose90,
    onPrimaryContainer = Rose10,
    inversePrimary = Rose80,
    secondary = BlueGray40,
    onSecondary = Color.White,
    secondaryContainer = BlueGray90,
    onSecondaryContainer = BlueGray10,
    tertiary = Honey40,
    onTertiary = Color.White,
    tertiaryContainer = Honey90,
    onTertiaryContainer = Honey10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Color(0xFF410002),
    background = Neutral98,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral90,
    onSurfaceVariant = Neutral20,
    surfaceTint = Rose40,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    outline = Color(0xFF787680),
    outlineVariant = Color(0xFFC9C5D4),
    scrim = Color(0xFF000000),
    surfaceBright = Neutral99,
    surfaceDim = Neutral90,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Neutral96,
    surfaceContainer = Neutral94,
    surfaceContainerHigh = Neutral92,
    surfaceContainerHighest = Neutral90,
)

internal val BrandDarkScheme = darkColorScheme(
    primary = Rose80,
    onPrimary = Rose20,
    primaryContainer = Rose30,
    onPrimaryContainer = Rose90,
    inversePrimary = Rose40,
    secondary = BlueGray80,
    onSecondary = BlueGray20,
    secondaryContainer = BlueGray30,
    onSecondaryContainer = BlueGray90,
    tertiary = Honey80,
    onTertiary = Honey20,
    tertiaryContainer = Honey30,
    onTertiaryContainer = Honey90,
    error = Red80,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Red90,
    background = Neutral6,
    onBackground = Neutral90,
    surface = Neutral6,
    onSurface = Neutral90,
    surfaceVariant = Neutral22,
    onSurfaceVariant = Neutral87,
    surfaceTint = Rose80,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    outline = Color(0xFF928F9A),
    outlineVariant = Color(0xFF484650),
    scrim = Color(0xFF000000),
    surfaceBright = Neutral24,
    surfaceDim = Neutral4,
    surfaceContainerLowest = Neutral4,
    surfaceContainerLow = Neutral10,
    surfaceContainer = Neutral12,
    surfaceContainerHigh = Neutral17,
    surfaceContainerHighest = Neutral22,
)

@Composable
fun rememberBrandGradients(
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    gradientStyle: HomeGradientStyle = HomeGradientStyle.RoseBloom,
    customGradient: CustomGradientConfig = CustomGradientConfig.Default,
): BrandGradients = remember(colorScheme, darkTheme, gradientStyle, customGradient) {
    gradientStyle.brushes(colorScheme, darkTheme, customGradient)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UITrendsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    brandAccentColor: BrandAccentColor = BrandAccentColor.Rose,
    homeGradientStyle: HomeGradientStyle = HomeGradientStyle.RoseBloom,
    homeLayoutStyle: HomeLayoutStyle = HomeLayoutStyle.FeaturedList,
    appFontStyle: AppFontStyle = AppFontStyle.UnboundedManrope,
    customGradient: CustomGradientConfig = CustomGradientConfig.Default,
    splashHeld: Boolean = false,
    splashExitGeneration: Int = 0,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val brandScheme = brandAccentColor.colorScheme(darkTheme)
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> brandScheme
    }

    val typography = remember(appFontStyle) {
        buildExpressiveTypography(
            brandFont = AppFonts.brandFont(appFontStyle),
            bodyFont = AppFonts.bodyFont(appFontStyle),
        )
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes,
        motionScheme = MotionScheme.expressive(),
    ) {
        val catalogAppearance = rememberCatalogAppearance(
            gradientStyle = homeGradientStyle,
            customGradient = customGradient,
            colorScheme = colorScheme,
            darkTheme = darkTheme,
        )
        CompositionLocalProvider(
            LocalBrandGradients provides catalogAppearance.gradients,
            LocalCatalogAppearance provides catalogAppearance,
            LocalHomeCatalogColors provides catalogAppearance.colors,
            LocalHomeLayoutStyle provides homeLayoutStyle,
        ) {
            SyncActivityWindowBackground()
            SystemBarAppearanceHost(
                splashHeld = splashHeld,
                splashExitGeneration = splashExitGeneration,
                content = content,
            )
        }
    }
}
