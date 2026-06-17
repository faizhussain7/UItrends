package com.mfhapps.trendingui.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

@Immutable
data class GradientBackdrop(
    private val fractions: FloatArray,
    private val colors: List<Color>,
) {
    init {
        require(colors.isNotEmpty()) { "GradientBackdrop requires at least one color" }
        require(fractions.size == colors.size) { "fractions and colors must match" }
    }

    fun sampleAt(verticalFraction: Float): Color {
        val t = verticalFraction.coerceIn(0f, 1f)
        if (colors.size == 1) return colors[0]

        var lower = 0
        for (i in 1 until fractions.size) {
            if (t <= fractions[i]) {
                lower = i - 1
                break
            }
            lower = i - 1
        }
        val upper = (lower + 1).coerceAtMost(colors.lastIndex)
        if (lower == upper) return colors[lower]

        val start = fractions[lower]
        val end = fractions[upper]
        val span = (end - start).coerceAtLeast(1e-6f)
        val local = ((t - start) / span).coerceIn(0f, 1f)
        return lerp(colors[lower], colors[upper], local)
    }

    companion object {
        fun fromEvenlySpaced(colors: List<Color>): GradientBackdrop {
            if (colors.isEmpty()) return GradientBackdrop(floatArrayOf(0f), listOf(Color.Black))
            if (colors.size == 1) return GradientBackdrop(floatArrayOf(0f), colors)
            val fractions = FloatArray(colors.size) { i ->
                if (colors.size == 1) 0f else i / (colors.size - 1).toFloat()
            }
            return GradientBackdrop(fractions, colors)
        }

        fun fromScreenLayers(background: Color, overlays: List<Color>): GradientBackdrop {
            if (overlays.isEmpty()) return fromEvenlySpaced(listOf(background))
            var acc = background
            val stacked = mutableListOf(acc)
            for (overlay in overlays) {
                acc = CatalogColorMath.compositeOver(overlay, acc)
                stacked.add(acc)
            }
            return fromEvenlySpaced(stacked)
        }
    }
}
