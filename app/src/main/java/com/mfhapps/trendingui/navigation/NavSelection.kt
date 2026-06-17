package com.mfhapps.trendingui.navigation

import androidx.navigation.NavBackStackEntry

fun NavBackStackEntry?.isHome(): Boolean =
    this?.destination?.route?.contains("HomeRoute") == true

fun NavBackStackEntry?.demoTitle(): String =
    demoEntryForBackStackRoute(this?.destination?.route)?.title ?: "UITrends"

fun NavBackStackEntry?.demoSharedContentKey(): String? =
    demoEntryForBackStackRoute(this?.destination?.route)?.route?.demoSharedContentKey()
