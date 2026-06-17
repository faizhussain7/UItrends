package com.mfhapps.trendingui.startup

import com.mfhapps.trendingui.launcher.AppLauncherIcon
import com.mfhapps.trendingui.ui.theme.ThemePreferences

data class StartupSnapshot(
    val launcherIcon: AppLauncherIcon,
    val themePreferences: ThemePreferences,
)
