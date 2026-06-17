package com.mfhapps.trendingui.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

enum class VibrantForegroundStyle {
    OnGradient,
    OnLiquidGlass,
}

data class VibrantForeground(
    val primary: Color,
    val secondary: Color,
    val separatorShadow: Color,
)

internal object VibrancyMath {

    private val InkDark = Color(0xFF0A0A10)
    private val InkLight = Color(0xFFFAF8FF)

    private const val WCAG_BODY = 4.5
    private const val WCAG_LARGE = 3.0

    fun resolve(
        rawBackdrop: Color,
        colorScheme: ColorScheme,
        role: GradientForegroundRole,
        style: VibrantForegroundStyle,
    ): Color = resolve(listOf(rawBackdrop), colorScheme, role, style)

    fun resolve(
        rawBackdrops: List<Color>,
        colorScheme: ColorScheme,
        role: GradientForegroundRole,
        style: VibrantForegroundStyle,
    ): Color = resolveBundle(rawBackdrops, colorScheme, role, style).primary

    fun resolveBundle(
        rawBackdrop: Color,
        colorScheme: ColorScheme,
        role: GradientForegroundRole,
        style: VibrantForegroundStyle,
    ): VibrantForeground = resolveBundle(listOf(rawBackdrop), colorScheme, role, style)

    fun resolveBundle(
        rawBackdrops: List<Color>,
        colorScheme: ColorScheme,
        role: GradientForegroundRole,
        style: VibrantForegroundStyle,
    ): VibrantForeground {
        val samples = rawBackdrops.map { backdrop ->
            when (style) {
                VibrantForegroundStyle.OnGradient -> backdrop.copy(alpha = 1f)
                VibrantForegroundStyle.OnLiquidGlass -> glassEffectiveBackdrop(backdrop)
            }
        }.ifEmpty { listOf(Color.Black) }
        val title = vibrantPrimary(samples)
        val subtitle = vibrantMuted(title, samples)
        val section = vibrantAccent(colorScheme.primary, title, samples)
        val shadow = separatorShadow(samples.first(), title)

        val primary = when (role) {
            GradientForegroundRole.Title,
            GradientForegroundRole.Icon,
            -> title
            GradientForegroundRole.Subtitle -> subtitle
            GradientForegroundRole.Section -> section
        }
        return VibrantForeground(
            primary = primary,
            secondary = subtitle,
            separatorShadow = shadow,
        )
    }

    fun glassEffectiveBackdrop(raw: Color): Color {
        val opaque = raw.copy(alpha = 1f)
        val scrim = if (opaque.luminance() >= 0.52f) {
            Color.White.copy(alpha = 0.40f)
        } else {
            Color.Black.copy(alpha = 0.34f)
        }
        return CatalogColorMath.compositeOver(scrim, opaque)
    }

    private fun vibrantPrimary(samples: List<Color>): Color {
        val base = pickContrastInk(samples, WCAG_BODY)
        val avg = samples.first()
        return tintInk(base, avg, amount = 0.12f)
    }

    private fun vibrantMuted(primary: Color, samples: List<Color>): Color {
        val worst = samples.minByOrNull { CatalogColorMath.wcagContrastRatio(primary, it) } ?: samples.first()
        var lo = 0f
        var hi = 0.82f
        var best = primary
        repeat(12) {
            val mid = (lo + hi) * 0.5f
            val candidate = lerp(primary, worst, mid).copy(alpha = 1f)
            if (samples.all { CatalogColorMath.wcagContrastRatio(candidate, it) >= WCAG_LARGE }) {
                best = candidate
                lo = mid
            } else {
                hi = mid
            }
        }
        return best
    }

    private fun vibrantAccent(accent: Color, title: Color, samples: List<Color>): Color {
        val seed = lerp(accent, title, 0.28f).copy(alpha = 1f)
        return if (samples.all { CatalogColorMath.wcagContrastRatio(seed, it) >= WCAG_LARGE }) {
            seed
        } else {
            title
        }
    }

    private fun separatorShadow(backdrop: Color, ink: Color): Color =
        if (backdrop.luminance() > ink.luminance()) {
            Color.Black.copy(alpha = 0.28f)
        } else {
            Color.White.copy(alpha = 0.22f)
        }

    private fun pickContrastInk(samples: List<Color>, minWcag: Double): Color {
        val candidates = listOf(InkDark, InkLight)
        return candidates.maxByOrNull { ink ->
            samples.minOf { CatalogColorMath.wcagContrastRatio(ink, it) }
        }?.takeIf { ink ->
            samples.all { CatalogColorMath.wcagContrastRatio(ink, it) >= minWcag }
        } ?: candidates.maxByOrNull { ink ->
            samples.minOf { CatalogColorMath.wcagContrastRatio(ink, it) }
        } ?: InkDark
    }

    private fun tintInk(ink: Color, backdrop: Color, amount: Float): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(backdrop.toArgb(), hsv)
        hsv[1] = (hsv[1] * 0.55f).coerceIn(0.12f, 0.72f)
        hsv[2] = if (ink.luminance() > 0.5f) 0.94f else 0.30f
        val hint = Color(android.graphics.Color.HSVToColor(hsv))
        return lerp(ink, hint, amount).copy(alpha = 1f)
    }
}
