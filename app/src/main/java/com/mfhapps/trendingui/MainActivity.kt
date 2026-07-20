package com.mfhapps.trendingui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mfhapps.trendingui.deeplink.AppDeepLinks
import com.mfhapps.trendingui.launcher.LauncherIconViewModel
import com.mfhapps.trendingui.launcher.installTrendingSplashScreen
import com.mfhapps.trendingui.play.PlayServicesViewModel
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
    private val playServicesViewModel: PlayServicesViewModel by viewModels()
    private var openDestination by mutableStateOf<String?>(null)

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
        openDestination = AppDeepLinks.destinationFrom(intent)
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
            val snackbarHostState = remember { SnackbarHostState() }
            val lifecycleOwner = LocalLifecycleOwner.current

            LaunchedEffect(lifecycleOwner) {
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    playServicesViewModel.checkForUpdate(this@MainActivity)
                }
            }
            LaunchedEffect(Unit) {
                playServicesViewModel.updateInstallReady.collect {
                    val result = snackbarHostState.showSnackbar(
                        message = "Update ready to install",
                        actionLabel = "Restart",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        playServicesViewModel.completeFlexibleUpdate()
                    }
                }
            }

            UITrendsTheme(
                darkTheme = darkTheme,
                dynamicColor = prefs.useDynamicColor,
                brandAccentColor = prefs.brandAccentColor,
                homeLayoutStyle = prefs.homeLayoutStyle,
                appFontStyle = prefs.appFontStyle,
                splashHeld = holdSplash,
                splashExitGeneration = splashExitGeneration,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    UITrendsApp(
                        themePreferences = prefs,
                        launcherIcon = selectedLauncherIcon,
                        openDestination = openDestination,
                        onOpenDestinationConsumed = { openDestination = null },
                        onThemeModeChange = themeViewModel::setThemeMode,
                        onDynamicColorChange = themeViewModel::setUseDynamicColor,
                        onBrandAccentChange = themeViewModel::setBrandAccentColor,
                        onAppFontStyleChange = themeViewModel::setAppFontStyle,
                        onHomeLayoutChange = themeViewModel::setHomeLayoutStyle,
                        onBlurModalBackdropChange = themeViewModel::setBlurModalBackdrop,
                        onModalBackdropStyleChange = themeViewModel::setModalBackdropStyle,
                        onSyncLauncherIconWithThemeChange = themeViewModel::setSyncLauncherIconWithTheme,
                        onLauncherIconChange = { icon ->
                            launcherIconViewModel.setLauncherIcon(icon, restartProcess = true)
                        },
                    )
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openDestination = AppDeepLinks.destinationFrom(intent)
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
