package com.mfhapps.trendingui.ui.platform

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

@Composable
fun isExpandedWindowWidth(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
}

@Composable
fun isMediumOrWiderWindow(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
}


@Composable
fun catalogAdaptiveMinTileWidth(): Dp {
    val info = currentWindowAdaptiveInfo()
    return catalogAdaptiveMinTileWidth(info)
}

fun catalogAdaptiveMinTileWidth(info: WindowAdaptiveInfo): Dp {
    val widthClass = info.windowSizeClass
    return when {
        widthClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 220.dp
        widthClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 188.dp
        else -> 156.dp
    }
}
