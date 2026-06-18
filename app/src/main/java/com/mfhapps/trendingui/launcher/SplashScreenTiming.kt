package com.mfhapps.trendingui.launcher

import android.content.Context
import com.mfhapps.trendingui.R


object SplashScreenTiming {
    private lateinit var values: Values

    data class Values(
        val motifCount: Int,
        val motifSlotMs: Long,
        val catalogCycleMs: Long,
        val crossfadeMs: Long,
        val fadeInMs: Long,
        val animMs: Long,
        val holdMs: Long,
        val minDisplayMs: Long,
    ) {
        val themeAnimationDurationMs: Int = catalogCycleMs.toInt()
    }

    fun init(context: Context) {
        if (::values.isInitialized) return
        val res = context.resources
        values = Values(
            motifCount = res.getInteger(R.integer.splash_motif_count),
            motifSlotMs = res.getInteger(R.integer.splash_motif_slot_ms).toLong(),
            catalogCycleMs = res.getInteger(R.integer.splash_catalog_cycle_ms).toLong(),
            crossfadeMs = res.getInteger(R.integer.splash_crossfade_ms).toLong(),
            fadeInMs = res.getInteger(R.integer.splash_fade_in_ms).toLong(),
            animMs = res.getInteger(R.integer.splash_anim_ms).toLong(),
            holdMs = res.getInteger(R.integer.splash_hold_ms).toLong(),
            minDisplayMs = res.getInteger(R.integer.splash_min_display_ms).toLong(),
        )
    }

    val motifCount: Int
        get() = values.motifCount

    val motifSlotMs: Long
        get() = values.motifSlotMs

    val catalogCycleMs: Long
        get() = values.catalogCycleMs

    val crossfadeMs: Long
        get() = values.crossfadeMs

    val minDisplayMs: Long
        get() = values.minDisplayMs

    val themeAnimationDurationMs: Int
        get() = values.themeAnimationDurationMs
}
