package com.mfhapps.trendingui.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomGradientConfig(
    val startHue: Float = DEFAULT_CUSTOM_GRADIENT_START_HUE,
    val endHue: Float = DEFAULT_CUSTOM_GRADIENT_END_HUE,
    val saturation: Float = DEFAULT_CUSTOM_GRADIENT_SATURATION,
) {
    fun normalized(): CustomGradientConfig = copy(
        startHue = startHue.mod(360f),
        endHue = endHue.mod(360f),
        saturation = saturation.coerceIn(0.22f, 0.78f),
    )

    fun startColor(darkTheme: Boolean): Color = colorFromHue(startHue, darkTheme)

    fun endColor(darkTheme: Boolean): Color = colorFromHue(endHue, darkTheme)

    private fun colorFromHue(hue: Float, darkTheme: Boolean): Color {
        val value = if (darkTheme) 0.68f else 0.88f
        return Color.hsv(hue.mod(360f), saturation, value)
    }

    companion object {
        val Default = CustomGradientConfig()
    }
}

const val DEFAULT_CUSTOM_GRADIENT_START_HUE = 240f
const val DEFAULT_CUSTOM_GRADIENT_END_HUE = 300f
const val DEFAULT_CUSTOM_GRADIENT_SATURATION = 0.5f


const val DEFAULT_CUSTOM_GRADIENT_HUE = DEFAULT_CUSTOM_GRADIENT_START_HUE
