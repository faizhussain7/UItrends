package com.mfhapps.trendingui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mfhapps.trendingui.TrendingApplication
import com.mfhapps.trendingui.launcher.LauncherIconViewModel
import com.mfhapps.trendingui.launcher.installTrendingSplashScreen
import com.mfhapps.trendingui.startup.AppStartupViewModel
import com.mfhapps.trendingui.ui.theme.ThemeMode
import com.mfhapps.trendingui.ui.theme.ThemeModeStore
import com.mfhapps.trendingui.ui.theme.ThemeViewModel
import com.mfhapps.trendingui.ui.theme.UITrendsTheme
import com.mfhapps.trendingui.ui.theme.applyInitialSystemBarAppearance
import com.mfhapps.trendingui.ui.theme.enableActivityEdgeToEdge
import com.mfhapps.trendingui.ui.theme.reapplySystemBarIconsAfterSplash
import com.mfhapps.trendingui.ui.theme.isSystemInNightMode
import com.mfhapps.trendingui.ui.theme.resolveDarkTheme
import com.mfhapps.trendingui.ui.theme.resolveLaunchSplashTheme
import com.mfhapps.trendingui.ui.theme.resolvePostSplashTheme
import com.mfhapps.trendingui.ui.theme.syncThemeAppearance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import androidx.core.graphics.drawable.toDrawable

class MainActivity : ComponentActivity() {
    private val startupViewModel: AppStartupViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val launcherIconViewModel: LauncherIconViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val themeMode = ThemeModeStore.get(this).current()
        val launchDark = resolveDarkTheme(themeMode, resources.configuration.isSystemInNightMode)

        setTheme(resolveLaunchSplashTheme(launchDark))
        installTrendingSplashScreen(
            shouldKeepOnScreen = { startupViewModel.holdSystemSplash.value },
            onSplashExitComplete = {
                reapplySystemBarIconsAfterSplash(themeMode, resources.configuration.isSystemInNightMode)
                startupViewModel.notifySplashExitComplete()
            },
        )
        super.onCreate(savedInstanceState)
        enableActivityEdgeToEdge(launchDark)
        applyInitialSystemBarAppearance(themeMode, resources.configuration.isSystemInNightMode)
        applySessionTheme(themeMode, resources.configuration.isSystemInNightMode)
        setContent {
            val selectedLauncherIcon by launcherIconViewModel.selectedIcon.collectAsStateWithLifecycle()
            val prefs by themeViewModel.preferences.collectAsStateWithLifecycle()
            val holdSplash by startupViewModel.holdSystemSplash.collectAsStateWithLifecycle()
            val splashExitGeneration by startupViewModel.splashExitGeneration.collectAsStateWithLifecycle()
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = resolveDarkTheme(prefs.themeMode, systemDarkTheme)

            UITrendsTheme(
                darkTheme = darkTheme,
                dynamicColor = prefs.useDynamicColor,
                brandAccentColor = prefs.brandAccentColor,
                homeLayoutStyle = prefs.homeLayoutStyle,
                splashHeld = holdSplash,
                splashExitGeneration = splashExitGeneration,
            ) {
                UITrendsApp(
                    themePreferences = prefs,
                    launcherIcon = selectedLauncherIcon,
                    onThemeModeChange = themeViewModel::setThemeMode,
                    onDynamicColorChange = themeViewModel::setUseDynamicColor,
                    onBrandAccentChange = themeViewModel::setBrandAccentColor,
                    onHomeLayoutChange = themeViewModel::setHomeLayoutStyle,
                    onBlurModalBackdropChange = themeViewModel::setBlurModalBackdrop,
                    onSyncLauncherIconWithThemeChange = themeViewModel::setSyncLauncherIconWithTheme,
                    onLauncherIconChange = launcherIconViewModel::setLauncherIcon,
                )
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val themeMode = ThemeModeStore.get(this).current()
        applySessionTheme(themeMode, newConfig.isSystemInNightMode)
    }

    private fun applySessionTheme(themeMode: ThemeMode, systemDark: Boolean) {
        val darkTheme = resolveDarkTheme(themeMode, systemDark)
        setTheme(resolvePostSplashTheme(darkTheme))
        window.setBackgroundDrawable(
            ContextCompat.getColor(
                this,
                if (darkTheme) R.color.splash_background_dark else R.color.splash_background,
            ).toDrawable(),
        )
        if (themeMode == ThemeMode.System) {
            val syncLauncherIcon = runBlocking(Dispatchers.IO) {
                (application as TrendingApplication).container.themePreferences.syncLauncherIconWithThemeOnce()
            }
            syncThemeAppearance(themeMode, syncLauncherIcon)
        }
    }
}
