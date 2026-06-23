package com.mfhapps.trendingui.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

data class ModalBackdropStyle(
    val blurRadiusDp: Float = DEFAULT_MODAL_BLUR_RADIUS_DP,
    val tintAlpha: Float = DEFAULT_MODAL_TINT_ALPHA,
    val tintHue: Float = DEFAULT_MODAL_TINT_HUE,
    val autoTint: Boolean = true,
    val blurType: ModalBackdropBlurType = ModalBackdropBlurType.Default,
) {
    fun normalized(): ModalBackdropStyle = copy(
        blurRadiusDp = blurRadiusDp.coerceIn(MIN_MODAL_BLUR_RADIUS_DP, MAX_MODAL_BLUR_RADIUS_DP),
        tintAlpha = tintAlpha.coerceIn(MIN_MODAL_TINT_ALPHA, MAX_MODAL_TINT_ALPHA),
        tintHue = ((tintHue % 360f) + 360f) % 360f,
        blurType = blurType,
    )

    fun forCollapsedHeader(): ModalBackdropStyle = copy(
        blurRadiusDp = (blurRadiusDp * COLLAPSED_HEADER_BLUR_SCALE).coerceIn(
            MIN_MODAL_BLUR_RADIUS_DP,
            MAX_MODAL_BLUR_RADIUS_DP,
        ),
    )

    companion object {
        val Default = ModalBackdropStyle()
    }
}

const val DEFAULT_MODAL_BLUR_RADIUS_DP = 32f
const val DEFAULT_MODAL_TINT_ALPHA = 0.62f
const val DEFAULT_MODAL_TINT_HUE = 210f
const val MIN_MODAL_BLUR_RADIUS_DP = 12f
const val MAX_MODAL_BLUR_RADIUS_DP = 48f
const val MIN_MODAL_TINT_ALPHA = 0.18f
const val MAX_MODAL_TINT_ALPHA = 0.88f
private const val COLLAPSED_HEADER_BLUR_SCALE = 0.625f

fun ModalBackdropStyle.resolveTintColor(
    scheme: ColorScheme,
    isDark: Boolean,
): Color {
    if (autoTint) {
        val alpha = if (isDark) tintAlpha * 0.9f else tintAlpha
        return scheme.surface.copy(alpha = alpha.coerceIn(MIN_MODAL_TINT_ALPHA, MAX_MODAL_TINT_ALPHA))
    }
    val saturation = if (isDark) 0.22f else 0.16f
    val value = if (isDark) 0.38f else 0.94f
    return Color.hsv(tintHue, saturation, value).copy(alpha = tintAlpha)
}

fun ModalBackdropStyle.toHazeStyle(
    scheme: ColorScheme,
    isDark: Boolean,
    noiseFactor: Float = resolveHazeNoiseFactor(isDark),
): HazeStyle {
    val tintColor = resolveTintColor(scheme, isDark)
    return HazeDefaults.style(
        backgroundColor = scheme.surface,
        blurRadius = resolveEffectBlurRadiusDp().dp,
        tint = HazeDefaults.tint(tintColor),
        noiseFactor = noiseFactor,
    )
}

fun ModalBackdropStyle.resolveEffectBlurRadiusDp(): Float {
    val radius = blurRadiusDp.coerceIn(MIN_MODAL_BLUR_RADIUS_DP, MAX_MODAL_BLUR_RADIUS_DP)
    return when (blurType) {
        ModalBackdropBlurType.Frosted -> (radius * 1.26f).coerceAtMost(MAX_MODAL_BLUR_RADIUS_DP)
        ModalBackdropBlurType.Mosaic -> (radius * 0.68f).coerceAtLeast(MIN_MODAL_BLUR_RADIUS_DP)
        else -> radius
    }
}

fun ModalBackdropStyle.resolveHazeNoiseFactor(
    isDark: Boolean,
    collapsedHeader: Boolean = false,
): Float {
    val base = when (blurType) {
        ModalBackdropBlurType.Frosted -> if (isDark) 0.19f else 0.16f
        ModalBackdropBlurType.Mosaic -> 0.012f
        ModalBackdropBlurType.Progressive -> if (isDark) 0.035f else 0.03f
        ModalBackdropBlurType.Default -> if (isDark) 0.045f else 0.04f
    }
    return (if (collapsedHeader) base * 0.72f else base).coerceIn(0f, 1f)
}

fun ModalBackdropStyle.resolveHazeInputScale(): HazeInputScale = when (blurType) {
    ModalBackdropBlurType.Mosaic -> HazeInputScale.Fixed(resolveMosaicInputScale())
    else -> HazeInputScale.None
}

fun ModalBackdropStyle.resolveMosaicInputScale(): Float {
    val strength = (blurRadiusDp - MIN_MODAL_BLUR_RADIUS_DP) /
        (MAX_MODAL_BLUR_RADIUS_DP - MIN_MODAL_BLUR_RADIUS_DP)
    return (0.20f + strength * 0.07f).coerceIn(0.20f, 0.27f)
}

fun ModalBackdropStyle.resolveHazeProgressive(
    collapsedHeader: Boolean,
): HazeProgressive? = when (blurType) {
    ModalBackdropBlurType.Progressive -> if (collapsedHeader) {
        HazeProgressive.verticalGradient(
            easing = FastOutSlowInEasing,
            startIntensity = 1f,
            endIntensity = 0f,
        )
    } else {
        HazeProgressive.verticalGradient(
            easing = FastOutSlowInEasing,
            startIntensity = 1f,
            endIntensity = 0.06f,
        )
    }
    else -> null
}

fun ModalBackdropStyle.resolveHazeTints(
    scheme: ColorScheme,
    isDark: Boolean,
): List<HazeTint> {
    val primary = resolveTintColor(scheme, isDark)
    return when (blurType) {
        ModalBackdropBlurType.Frosted -> {
            val frostWash = if (isDark) 0.07f else 0.15f
            val lift = if (isDark) 0.05f else 0.09f
            listOf(
                HazeDefaults.tint(primary),
                HazeDefaults.tint(Color.White.copy(alpha = frostWash)),
                HazeDefaults.tint(scheme.surfaceBright.copy(alpha = lift)),
            )
        }
        ModalBackdropBlurType.Mosaic -> listOf(
            HazeDefaults.tint(primary.copy(alpha = (primary.alpha * 0.88f).coerceIn(MIN_MODAL_TINT_ALPHA, MAX_MODAL_TINT_ALPHA))),
        )
        ModalBackdropBlurType.Progressive,
        ModalBackdropBlurType.Default,
        -> listOf(HazeDefaults.tint(primary))
    }
}

fun ModalBackdropStyle.toCollapsedHeaderHazeStyle(
    scheme: ColorScheme,
    isDark: Boolean,
): HazeStyle = forCollapsedHeader().toHazeStyle(
    scheme = scheme,
    isDark = isDark,
    noiseFactor = forCollapsedHeader().resolveHazeNoiseFactor(isDark, collapsedHeader = true),
)
