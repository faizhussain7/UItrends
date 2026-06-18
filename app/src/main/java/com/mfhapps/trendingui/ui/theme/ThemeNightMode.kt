package com.mfhapps.trendingui.ui.theme

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.mfhapps.trendingui.R
import com.mfhapps.trendingui.launcher.LauncherIconCoordinator

fun resolveDarkTheme(themeMode: ThemeMode, systemDark: Boolean): Boolean = when (themeMode) {
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
    ThemeMode.System -> systemDark
}

fun resolveLaunchSplashTheme(launchDark: Boolean): Int =
    if (launchDark) {
        R.style.Theme_Trendinguipatterns_Starting_Dark
    } else {
        R.style.Theme_Trendinguipatterns_Starting_Light
    }

fun resolvePostSplashTheme(launchDark: Boolean): Int =
    if (launchDark) {
        R.style.Theme_Trendinguipatterns_Dark
    } else {
        R.style.Theme_Trendinguipatterns
    }

fun Context.applyThemeNightMode(themeMode: ThemeMode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val uiModeManager = getSystemService(UiModeManager::class.java) ?: return
        uiModeManager.setApplicationNightMode(
            when (themeMode) {
                ThemeMode.Light -> UiModeManager.MODE_NIGHT_NO
                ThemeMode.Dark -> UiModeManager.MODE_NIGHT_YES
                ThemeMode.System -> UiModeManager.MODE_NIGHT_AUTO
            },
        )
        return
    }

    val nightMode = when (themeMode) {
        ThemeMode.Light -> Configuration.UI_MODE_NIGHT_NO
        ThemeMode.Dark -> Configuration.UI_MODE_NIGHT_YES
        ThemeMode.System -> Configuration.UI_MODE_NIGHT_UNDEFINED
    }
    val config = Configuration(resources.configuration)
    config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
    @Suppress("DEPRECATION")
    resources.updateConfiguration(config, resources.displayMetrics)
}

fun Activity.applyThemeNightMode(themeMode: ThemeMode) {
    (this as Context).applyThemeNightMode(themeMode)
}


fun Context.syncThemeAppearance(
    themeMode: ThemeMode,
    syncLauncherIconWithTheme: Boolean,
) {
    applyThemeNightMode(themeMode)
    LauncherIconCoordinator.syncTheme(this, syncLauncherIconWithTheme)
}

fun ComponentActivity.enableActivityEdgeToEdge(appDark: Boolean) {
    val statusBarStyle = if (appDark) {
        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
    } else {
        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
    }
    val navigationBarStyle = if (appDark) {
        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
    } else {
        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
    }
    enableEdgeToEdge(
        statusBarStyle = statusBarStyle,
        navigationBarStyle = navigationBarStyle,
    )
}


fun ComponentActivity.applyInitialSystemBarAppearance(themeMode: ThemeMode, systemDark: Boolean) {
    val appDark = resolveDarkTheme(themeMode, systemDark)
    val backdropArgb = ContextCompat.getColor(
        this,
        if (appDark) R.color.splash_background_dark else R.color.splash_background,
    )
    val backdrop = androidx.compose.ui.graphics.Color(backdropArgb)
    val appearance = SystemBarAppearance.forBackdrop(backdrop)
    applySystemBarAppearance(this, window.decorView, appearance)
}


fun ComponentActivity.reapplySystemBarIconsAfterSplash(themeMode: ThemeMode, systemDark: Boolean) {
    applyInitialSystemBarAppearance(themeMode, systemDark)
    window.decorView.post {
        applyInitialSystemBarAppearance(themeMode, systemDark)
    }
}


fun ComponentActivity.applyEdgeToEdgeForTheme(darkTheme: Boolean) {
    enableActivityEdgeToEdge(darkTheme)
}

internal val Configuration.isSystemInNightMode: Boolean
    get() = (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
