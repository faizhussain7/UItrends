package com.mfhapps.trendingui.screens.pretext

internal class PretextMaskTemporalFilter(
    private val depth: Int = 3,
) {
    private val frames = ArrayDeque<FloatArray>()

    fun smooth(mask: FloatArray): FloatArray {
        frames.addLast(mask.copyOf())
        while (frames.size > depth) {
            frames.removeFirst()
        }
        if (frames.size == 1) return mask
        val out = FloatArray(mask.size)
        val n = frames.size.toFloat()
        for (i in mask.indices) {
            var sum = 0f
            for (frame in frames) {
                sum += frame[i]
            }
            out[i] = sum / n
        }
        return out
    }

    fun clear() {
        frames.clear()
    }
}
