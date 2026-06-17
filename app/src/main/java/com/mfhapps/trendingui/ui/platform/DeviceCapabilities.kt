package com.mfhapps.trendingui.ui.platform

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

fun Context.isLowRamDevice(): Boolean {
    val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    return am?.isLowRamDevice == true
}

fun Context.supportsBackdropBlur(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isLowRamDevice()

@Composable
fun isCompactWindowWidth(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
}
