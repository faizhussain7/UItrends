package com.mfhapps.trendingui.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.max
import kotlin.math.pow

@Immutable
data class HeroGradientForeground(
    val badgeFill: Color,
    val badgeText: Color,
    val section: Color,
    val title: Color,
    val subtitle: Color,
)

@Immutable
data class GradientChipColors(
    val container: Color,
    val label: Color,
)

@Immutable
data class FilterChipColors(
    val idleContainer: Color,
    val selectedContainer: Color,
    val idleLabel: Color,
    val selectedLabel: Color,
)

internal object CatalogColorMath {

    private val OnGradientDark = Color(0xFF050508)
    private val OnGradientLight = Color(0xFFFFFFFF)

    private const val WCAG_BODY = 4.5
    private const val WCAG_LARGE = 3.0

    fun colorsForBackground(
        background: Color,
        colorScheme: androidx.compose.material3.ColorScheme,
        role: GradientForegroundRole,
        style: VibrantForegroundStyle = VibrantForegroundStyle.OnGradient,
    ): Color = VibrancyMath.resolve(background, colorScheme, role, style)

    fun sampleLinearGradient(stops: List<Color>, position: Float): Color {
        require(stops.isNotEmpty()) { "stops must not be empty" }
        if (stops.size == 1) return stops.first().copy(alpha = 1f)
        val clamped = position.coerceIn(0f, 1f)
        val scaled = clamped * stops.lastIndex
        val index = scaled.toInt().coerceIn(0, stops.lastIndex - 1)
        val fraction = scaled - index
        return lerp(stops[index], stops[index + 1], fraction).copy(alpha = 1f)
    }

    fun resolveHeroCardForeground(
        gradientStops: List<Color>,
        colorScheme: androidx.compose.material3.ColorScheme,
    ): HeroGradientForeground {
        require(gradientStops.isNotEmpty()) { "gradientStops must not be empty" }
        val opaqueStops = gradientStops.map { it.copy(alpha = 1f) }

        val badgeBackdrops = listOf(
            sampleLinearGradient(opaqueStops, 0.08f),
            sampleLinearGradient(opaqueStops, 0.18f),
            sampleLinearGradient(opaqueStops, 0.28f),
        )
        val textBackdrops = listOf(
            sampleLinearGradient(opaqueStops, 0.62f),
            sampleLinearGradient(opaqueStops, 0.78f),
            sampleLinearGradient(opaqueStops, 0.92f),
        )

        val title = VibrancyMath.resolve(
            rawBackdrops = textBackdrops,
            colorScheme = colorScheme,
            role = GradientForegroundRole.Title,
            style = VibrantForegroundStyle.OnGradient,
        )
        val subtitle = VibrancyMath.resolve(
            rawBackdrops = textBackdrops,
            colorScheme = colorScheme,
            role = GradientForegroundRole.Subtitle,
            style = VibrantForegroundStyle.OnGradient,
        )
        val section = VibrancyMath.resolve(
            rawBackdrops = textBackdrops + badgeBackdrops.take(1),
            colorScheme = colorScheme,
            role = GradientForegroundRole.Section,
            style = VibrantForegroundStyle.OnGradient,
        )
        val badge = resolveOnGradientChip(
            backdrops = badgeBackdrops,
            colorScheme = colorScheme,
        )

        return HeroGradientForeground(
            badgeFill = badge.container,
            badgeText = badge.label,
            section = section,
            title = title,
            subtitle = subtitle,
        )
    }


    fun resolveOnGradientChip(
        backdrops: List<Color>,
        colorScheme: androidx.compose.material3.ColorScheme,
    ): GradientChipColors {
        require(backdrops.isNotEmpty()) { "backdrops must not be empty" }
        val avgLum = backdrops.map { it.luminance() }.average().toFloat()
        val scrim = if (avgLum >= 0.5f) {
            Color.White.copy(alpha = 0.46f)
        } else {
            Color.Black.copy(alpha = 0.40f)
        }
        val containerSamples = backdrops.map { compositeOver(scrim, it) }
        val container = containerSamples
            .reduce { acc, sample -> lerp(acc, sample, 0.5f) }
            .copy(alpha = 1f)
        val label = VibrancyMath.resolve(
            rawBackdrops = containerSamples,
            colorScheme = colorScheme,
            role = GradientForegroundRole.Title,
            style = VibrantForegroundStyle.OnGradient,
        )
        return GradientChipColors(container = container, label = label)
    }

    fun resolveFilterChipColors(
        barBackground: Color,
        colorScheme: androidx.compose.material3.ColorScheme,
    ): FilterChipColors {
        val bar = barBackground.copy(alpha = 1f)
        val barSamples = listOf(bar)
        val idleLabel = VibrancyMath.resolve(
            rawBackdrops = barSamples,
            colorScheme = colorScheme,
            role = GradientForegroundRole.Title,
            style = VibrantForegroundStyle.OnGradient,
        )
        val accentOnBar = VibrancyMath.resolve(
            rawBackdrops = barSamples,
            colorScheme = colorScheme,
            role = GradientForegroundRole.Section,
            style = VibrantForegroundStyle.OnGradient,
        )
        val primarySeed = lerp(colorScheme.primary, accentOnBar, 0.2f).copy(alpha = 1f)
        val primaryBlend = if (bar.luminance() >= 0.5f) 0.88f else 0.92f
        var selectedContainer = lerp(bar, primarySeed, primaryBlend).copy(alpha = 1f)
        if (wcagContrastRatio(selectedContainer, bar) < 2.4) {
            selectedContainer = lerp(bar, colorScheme.primary, 0.94f).copy(alpha = 1f)
        }
        val selectedLabel = resolveChipLabelOnContainer(
            container = selectedContainer,
            colorScheme = colorScheme,
        )
        return FilterChipColors(
            idleContainer = Color.Transparent,
            selectedContainer = selectedContainer,
            idleLabel = idleLabel,
            selectedLabel = selectedLabel,
        )
    }

    private fun resolveChipLabelOnContainer(
        container: Color,
        colorScheme: androidx.compose.material3.ColorScheme,
    ): Color {
        val onPrimaryContrast = wcagContrastRatio(colorScheme.onPrimary, container)
        val onPrimaryContainerContrast = wcagContrastRatio(colorScheme.onPrimaryContainer, container)
        return when {
            onPrimaryContrast >= WCAG_BODY -> colorScheme.onPrimary
            onPrimaryContainerContrast >= WCAG_BODY -> colorScheme.onPrimaryContainer
            else -> VibrancyMath.resolve(
                rawBackdrops = listOf(container),
                colorScheme = colorScheme,
                role = GradientForegroundRole.Title,
                style = VibrantForegroundStyle.OnGradient,
            )
        }
    }

    fun resolveGradientChromeColors(
        chromeSamples: List<Color>,
        statusBarBackdrop: Color,
        colorScheme: androidx.compose.material3.ColorScheme,
    ): HomeCatalogColors {
        val samples = chromeSamples
            .map { it.copy(alpha = 1f) }
            .ifEmpty { listOf(colorScheme.background) }

        val titleFg = bestForeground(samples, WCAG_BODY)
        val subtitleFg = mutedForeground(titleFg, samples, WCAG_LARGE)
        val sectionFg = accentOnGradient(colorScheme.primary, titleFg, samples)
        val darkStatusBarIcons = prefersLightStatusBarIcons(samples)

        val glassTitle = VibrancyMath.resolve(
            rawBackdrop = statusBarBackdrop,
            colorScheme = colorScheme,
            role = GradientForegroundRole.Title,
            style = VibrantForegroundStyle.OnLiquidGlass,
        )
        val glassIcon = VibrancyMath.resolve(
            rawBackdrop = statusBarBackdrop,
            colorScheme = colorScheme,
            role = GradientForegroundRole.Icon,
            style = VibrantForegroundStyle.OnLiquidGlass,
        )
        val glassSubtitle = VibrancyMath.resolve(
            rawBackdrop = statusBarBackdrop,
            colorScheme = colorScheme,
            role = GradientForegroundRole.Subtitle,
            style = VibrantForegroundStyle.OnLiquidGlass,
        )
        val glassShadow = VibrancyMath.resolveBundle(
            rawBackdrop = statusBarBackdrop,
            colorScheme = colorScheme,
            role = GradientForegroundRole.Title,
            style = VibrantForegroundStyle.OnLiquidGlass,
        ).separatorShadow

        return HomeCatalogColors(
            headerTitle = titleFg,
            headerSubtitle = subtitleFg,
            headerIcon = titleFg,
            exploreTitle = titleFg,
            exploreSubtitle = subtitleFg,
            sectionOnGradient = sectionFg,
            glassBarTitle = glassTitle,
            glassBarSubtitle = glassSubtitle,
            glassBarIcon = glassIcon,
            glassBarShadow = glassShadow,
            useLightStatusBarIcons = darkStatusBarIcons,
        )
    }

    fun compositeOver(foreground: Color, background: Color): Color {
        val a = foreground.alpha.coerceIn(0f, 1f)
        if (a <= 0f) return background.copy(alpha = 1f)
        if (a >= 1f) return foreground.copy(alpha = 1f)
        val ia = 1f - a
        return Color(
            red = foreground.red * a + background.red * ia,
            green = foreground.green * a + background.green * ia,
            blue = foreground.blue * a + background.blue * ia,
            alpha = 1f,
        )
    }

    fun compositeLayersOverBackground(background: Color, layers: List<Color>): Color =
        layers.fold(background) { acc, layer -> compositeOver(layer, acc) }

    fun stackChromeSamples(background: Color, layers: List<Color>, backdrop: Color): List<Color> {
        val stacked = mutableListOf<Color>()
        var acc = background
        for (layer in layers) {
            acc = compositeOver(layer, acc)
            stacked.add(acc)
        }
        stacked.add(backdrop)
        return stacked.distinctBy { colorKey(it) }
    }

    private fun colorKey(c: Color): Int {
        val r = (c.red * 255f).toInt()
        val g = (c.green * 255f).toInt()
        val b = (c.blue * 255f).toInt()
        return r shl 16 or (g shl 8) or b
    }

    private fun bestForeground(samples: List<Color>, minWcag: Double): Color {
        val candidates = listOf(OnGradientDark, OnGradientLight)
        return candidates.maxByOrNull { candidate ->
            samples.minOf { wcagContrastRatio(candidate, it) }
        }?.takeIf { candidate ->
            samples.all { wcagContrastRatio(candidate, it) >= minWcag }
        } ?: candidates.maxByOrNull { candidate ->
            samples.minOf { wcagContrastRatio(candidate, it) }
        } ?: OnGradientDark
    }

    private fun mutedForeground(primary: Color, samples: List<Color>, minWcag: Double): Color {
        val worstBg = samples.minByOrNull { wcagContrastRatio(primary, it) } ?: samples.first()
        var lo = 0f
        var hi = 0.85f
        var best = primary
        repeat(12) {
            val mid = (lo + hi) * 0.5f
            val candidate = lerp(primary, worstBg, mid).copy(alpha = 1f)
            val passes = samples.all { wcagContrastRatio(candidate, it) >= minWcag }
            if (passes) {
                best = candidate
                lo = mid
            } else {
                hi = mid
            }
        }
        return best
    }

    private fun accentOnGradient(
        accent: Color,
        titleFg: Color,
        samples: List<Color>,
    ): Color {
        val seed = lerp(accent, titleFg, 0.3f).copy(alpha = 1f)
        if (samples.all { wcagContrastRatio(seed, it) >= WCAG_LARGE }) return seed
        return titleFg
    }

    fun prefersLightStatusBarIcons(samples: List<Color>): Boolean {
        val avgLum = samples.map { it.luminance() }.average().toFloat()
        if (avgLum >= 0.55f) return true
        if (avgLum <= 0.4f) return false
        val darkScore = samples.minOf { wcagContrastRatio(Color.Black, it) }
        val lightScore = samples.minOf { wcagContrastRatio(Color.White, it) }
        return darkScore >= lightScore
    }

    fun wcagContrastRatio(foreground: Color, background: Color): Double {
        val fg = if (foreground.alpha < 1f) compositeOver(foreground, background) else foreground
        val bg = background.copy(alpha = 1f)
        return ColorUtils.calculateContrast(fg.toArgb(), bg.toArgb()).toDouble()
    }

    private fun relativeLuminance(color: Color): Double {
        fun linear(channel: Float): Double {
            val c = channel.toDouble().coerceIn(0.0, 1.0)
            return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(color.red) +
            0.7152 * linear(color.green) +
            0.0722 * linear(color.blue)
    }
}
