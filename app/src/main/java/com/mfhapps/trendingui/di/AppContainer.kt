package com.mfhapps.trendingui.di

import android.content.Context
import com.mfhapps.trendingui.launcher.LauncherIconRepository
import com.mfhapps.trendingui.ui.theme.ThemeModeStore
import com.mfhapps.trendingui.ui.theme.ThemePreferencesRepository

class AppContainer(context: Context) {
    val themeMode: ThemeModeStore = ThemeModeStore.get(context.applicationContext)
    val launcherIcons: LauncherIconRepository = LauncherIconRepository(context.applicationContext)
    val themePreferences: ThemePreferencesRepository =
        ThemePreferencesRepository(context.applicationContext, themeMode)
}
