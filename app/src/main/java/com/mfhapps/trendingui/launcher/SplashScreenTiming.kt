package com.mfhapps.trendingui.launcher

import com.mfhapps.trendingui.launcher.AppLauncherIcon


object SplashScreenTiming {
    val motifCount: Int = AppLauncherIcon.entries.size


    const val MOTIF_SLOT_MS = 1_400L

    const val CROSSFADE_MS = 360L


    const val MOTIF_HOLD_MS = 420L

    val catalogCycleMs: Long = motifCount * MOTIF_SLOT_MS

    val themeAnimationDurationMs: Int = catalogCycleMs.toInt()

    val minDisplayMs: Long = catalogCycleMs + 400L
}
