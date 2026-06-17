package com.mfhapps.trendingui.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

enum class HomeGradientStyle(val label: String) {
    RoseBloom("Rose bloom"),
    Aurora("Aurora"),
    OceanDepth("Ocean depth"),
    WarmSunset("Warm sunset"),
    MinimalMist("Minimal mist"),
    Custom("Custom"),
}

enum class HomeLayoutStyle(val label: String) {
    FeaturedList("Featured list"),
    BentoGrid("Bento grid"),
    CompactTiles("Compact tiles"),
}

val LocalHomeLayoutStyle = staticCompositionLocalOf { HomeLayoutStyle.FeaturedList }

fun HomeGradientStyle.brushes(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
    customGradient: CustomGradientConfig = CustomGradientConfig.Default,
): BrandGradients =
    when (this) {
        HomeGradientStyle.RoseBloom -> roseBloomGradients(colorScheme, darkTheme)
        HomeGradientStyle.Aurora -> auroraGradients(colorScheme, darkTheme)
        HomeGradientStyle.OceanDepth -> oceanGradients(colorScheme, darkTheme)
        HomeGradientStyle.WarmSunset -> sunsetGradients(colorScheme, darkTheme)
        HomeGradientStyle.MinimalMist -> minimalGradients(colorScheme, darkTheme)
        HomeGradientStyle.Custom -> customGradients(colorScheme, darkTheme, customGradient.normalized())
    }

private fun customGradients(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
    config: CustomGradientConfig,
): BrandGradients {
    val start = config.startColor(darkTheme)
    val end = config.endColor(darkTheme)
    val mid = lerp(start, end, 0.5f)
    val topAlpha = if (darkTheme) 0.4f else 0.26f
    val midAlpha = if (darkTheme) 0.26f else 0.16f
    val backdrop = lerp(colorScheme.background, lerp(start, end, 0.35f), if (darkTheme) 0.4f else 0.28f)
    val bg = colorScheme.background
    val samples = chromeSamples(
        background = bg,
        backdrop = backdrop,
        topLayers = listOf(
            start.copy(alpha = topAlpha + 0.1f),
            start.copy(alpha = topAlpha),
            mid.copy(alpha = midAlpha),
            lerp(start, end, 0.35f).copy(alpha = if (darkTheme) 0.5f else 0.38f),
        ),
    )
    return BrandGradients(
        toolbar = Brush.linearGradient(
            colors = listOf(
                start.copy(alpha = topAlpha + 0.1f),
                end.copy(alpha = midAlpha + 0.08f),
                colorScheme.background,
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 420f),
        ),
        screen = Brush.linearGradient(
            colors = listOf(
                start.copy(alpha = topAlpha),
                mid.copy(alpha = midAlpha),
                end.copy(alpha = if (darkTheme) 0.2f else 0.12f),
                colorScheme.surfaceContainerLow,
                colorScheme.background,
            ),
            start = Offset(0f, 0f),
            end = Offset(1100f, 2400f),
        ),
        statusBarBackdrop = backdrop,
        screenBackdrop = GradientBackdrop.fromEvenlySpaced(
            listOf(
                start.copy(alpha = topAlpha),
                mid.copy(alpha = midAlpha),
                end.copy(alpha = if (darkTheme) 0.2f else 0.12f),
                colorScheme.surfaceContainerLow,
                colorScheme.background,
            ),
        ),
        chromeBackdropSamples = samples,
        motionPalette = listOf(
            start.copy(alpha = 1f),
            mid.copy(alpha = 1f),
            end.copy(alpha = 1f),
            lerp(start, end, 0.35f).copy(alpha = 1f),
        ),
    )
}

private fun brandGradients(
    screen: Brush,
    toolbar: Brush,
    statusBarBackdrop: Color,
    screenGradientColors: List<Color>,
    chromeBackdropSamples: List<Color> = listOf(statusBarBackdrop),
): BrandGradients = BrandGradients(
    screen = screen,
    toolbar = toolbar,
    statusBarBackdrop = statusBarBackdrop,
    screenBackdrop = GradientBackdrop.fromEvenlySpaced(screenGradientColors),
    chromeBackdropSamples = chromeBackdropSamples,
    motionPalette = screenGradientColors.take(4).map { it.copy(alpha = 1f) },
)


private fun chromeSamples(
    background: Color,
    backdrop: Color,
    topLayers: List<Color>,
): List<Color> = CatalogColorMath.stackChromeSamples(background, topLayers, backdrop)

private fun roseBloomGradients(colorScheme: ColorScheme, darkTheme: Boolean): BrandGradients =
    if (darkTheme) {
        val bg = colorScheme.background
        val top = colorScheme.primary.copy(alpha = 0.45f)
        val backdrop = lerp(bg, top, 0.38f)
        val samples = chromeSamples(
            bg,
            backdrop,
            listOf(top, colorScheme.primary.copy(alpha = 0.22f)),
        )
        brandGradients(
            toolbar = Brush.verticalGradient(
                listOf(
                    colorScheme.primary.copy(alpha = 0.45f),
                    colorScheme.primaryContainer,
                    colorScheme.background,
                ),
            ),
            screen = Brush.verticalGradient(
                listOf(
                    colorScheme.primary.copy(alpha = 0.22f),
                    colorScheme.surfaceContainerLow,
                    colorScheme.background,
                    Neutral4,
                ),
            ),
            statusBarBackdrop = backdrop,
            screenGradientColors = listOf(
                colorScheme.primary.copy(alpha = 0.22f),
                colorScheme.surfaceContainerLow,
                colorScheme.background,
                Neutral4,
            ),
            chromeBackdropSamples = samples,
        )
    } else {
        val bg = colorScheme.background
        val top = colorScheme.primaryContainer.copy(alpha = 0.65f)
        val backdrop = lerp(colorScheme.surfaceContainerLow, top, 0.45f)
        val samples = chromeSamples(
            bg,
            backdrop,
            listOf(
                colorScheme.primary.copy(alpha = 0.12f),
                top,
                colorScheme.primaryContainer.copy(alpha = 0.35f),
            ),
        )
        brandGradients(
            toolbar = Brush.verticalGradient(
                listOf(
                    colorScheme.primary.copy(alpha = 0.12f),
                    colorScheme.primaryContainer.copy(alpha = 0.65f),
                    colorScheme.surfaceContainerLow,
                ),
            ),
            screen = Brush.verticalGradient(
                listOf(
                    colorScheme.primaryContainer.copy(alpha = 0.35f),
                    colorScheme.surfaceContainerLow,
                    colorScheme.background,
                ),
            ),
            statusBarBackdrop = backdrop,
            screenGradientColors = listOf(
                colorScheme.primaryContainer.copy(alpha = 0.35f),
                colorScheme.surfaceContainerLow,
                colorScheme.background,
            ),
            chromeBackdropSamples = samples,
        )
    }

private fun auroraGradients(colorScheme: ColorScheme, darkTheme: Boolean): BrandGradients {
    val bg = colorScheme.background
    val top = if (darkTheme) colorScheme.tertiary.copy(alpha = 0.35f) else colorScheme.tertiary.copy(alpha = 0.2f)
    val mid = colorScheme.secondary.copy(alpha = if (darkTheme) 0.28f else 0.18f)
    val low = colorScheme.primary.copy(alpha = if (darkTheme) 0.2f else 0.12f)
    val backdrop = lerp(bg, top, 0.4f)
    val samples = chromeSamples(bg, backdrop, listOf(top, mid, low))
    return brandGradients(
        toolbar = Brush.linearGradient(
            colors = listOf(top, mid, colorScheme.background),
            start = Offset.Zero,
            end = Offset(900f, 400f),
        ),
        screen = Brush.linearGradient(
            colors = listOf(top, mid, low, colorScheme.background),
            start = Offset(0f, 0f),
            end = Offset(1200f, 2400f),
        ),
        statusBarBackdrop = backdrop,
        screenGradientColors = listOf(top, mid, low, colorScheme.background),
        chromeBackdropSamples = samples,
    )
}

private fun oceanGradients(colorScheme: ColorScheme, darkTheme: Boolean): BrandGradients {
    val bg = colorScheme.background
    val deep = if (darkTheme) BlueGray20 else BlueGray90
    val mid = colorScheme.secondaryContainer
    val top = colorScheme.secondary.copy(alpha = if (darkTheme) 0.5f else 0.25f)
    val backdrop = lerp(bg, top, if (darkTheme) 0.44f else 0.3f)
    val samples = chromeSamples(
        bg,
        backdrop,
        listOf(
            top,
            colorScheme.secondary.copy(alpha = if (darkTheme) 0.35f else 0.2f),
            deep.copy(alpha = if (darkTheme) 0.9f else 0.45f),
        ),
    )
    return brandGradients(
        toolbar = Brush.verticalGradient(
            listOf(top, mid, colorScheme.background),
        ),
        screen = Brush.verticalGradient(
            listOf(
                colorScheme.secondary.copy(alpha = if (darkTheme) 0.35f else 0.2f),
                deep.copy(alpha = if (darkTheme) 0.9f else 0.45f),
                colorScheme.surfaceContainerLow,
                colorScheme.background,
            ),
        ),
        statusBarBackdrop = backdrop,
        screenGradientColors = listOf(
            colorScheme.secondary.copy(alpha = if (darkTheme) 0.35f else 0.2f),
            deep.copy(alpha = if (darkTheme) 0.9f else 0.45f),
            colorScheme.surfaceContainerLow,
            colorScheme.background,
        ),
        chromeBackdropSamples = samples,
    )
}

private fun sunsetGradients(colorScheme: ColorScheme, darkTheme: Boolean): BrandGradients {
    val bg = colorScheme.background
    val warm = if (darkTheme) Honey30 else Honey90
    val rose = colorScheme.primaryContainer
    val backdrop = if (darkTheme) {
        lerp(bg, rose, 0.36f)
    } else {
        lerp(Color.White, rose, 0.28f)
    }
    val samples = chromeSamples(
        bg,
        backdrop,
        listOf(
            rose,
            warm.copy(alpha = 0.85f),
            colorScheme.primary.copy(alpha = if (darkTheme) 0.35f else 0.18f),
        ),
    )
    return brandGradients(
        toolbar = Brush.linearGradient(
            colors = listOf(rose, warm.copy(alpha = 0.85f), colorScheme.background),
            start = Offset(0f, 0f),
            end = Offset(800f, 500f),
        ),
        screen = Brush.linearGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = if (darkTheme) 0.35f else 0.18f),
                warm.copy(alpha = if (darkTheme) 0.55f else 0.75f),
                colorScheme.surfaceContainerLow,
                colorScheme.background,
            ),
            start = Offset(0f, 0f),
            end = Offset(1000f, 2200f),
        ),
        statusBarBackdrop = backdrop,
        screenGradientColors = listOf(
            colorScheme.primary.copy(alpha = if (darkTheme) 0.35f else 0.18f),
            warm.copy(alpha = if (darkTheme) 0.55f else 0.75f),
            colorScheme.surfaceContainerLow,
            colorScheme.background,
        ),
        chromeBackdropSamples = samples,
    )
}

private fun minimalGradients(colorScheme: ColorScheme, darkTheme: Boolean): BrandGradients {
    val fade = if (darkTheme) 0.08f else 0.05f
    val bg = colorScheme.background
    val backdrop = if (darkTheme) colorScheme.surfaceContainerHigh else colorScheme.surfaceContainerLow
    val samples = chromeSamples(
        bg,
        backdrop,
        listOf(
            colorScheme.surfaceContainerHigh.copy(alpha = fade + 0.5f),
            colorScheme.surfaceContainerLow.copy(alpha = fade + 0.4f),
        ),
    )
    return brandGradients(
        toolbar = Brush.verticalGradient(
            listOf(
                colorScheme.surfaceContainerHigh.copy(alpha = fade + 0.5f),
                colorScheme.background,
            ),
        ),
        screen = Brush.verticalGradient(
            listOf(
                colorScheme.surfaceContainerLow.copy(alpha = fade + 0.4f),
                colorScheme.background,
                colorScheme.background,
            ),
        ),
        statusBarBackdrop = backdrop,
        screenGradientColors = listOf(
            colorScheme.surfaceContainerLow.copy(alpha = fade + 0.4f),
            colorScheme.background,
            colorScheme.background,
        ),
        chromeBackdropSamples = samples,
    )
}


@Composable
fun HomeGradientStyle.previewBrush(
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    customGradient: CustomGradientConfig = CustomGradientConfig.Default,
): Brush {
    val dark = colorScheme.background.luminance() < 0.5f
    return brushes(colorScheme, dark, customGradient).screen
}
