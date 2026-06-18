package com.mfhapps.trendingui

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.mfhapps.trendingui.di.AppContainer
import com.mfhapps.trendingui.launcher.LauncherIconCoordinator
import com.mfhapps.trendingui.launcher.SplashDrawableCache
import com.mfhapps.trendingui.launcher.SplashScreenTiming
import com.mfhapps.trendingui.startup.StartupSnapshot
import com.mfhapps.trendingui.ui.theme.ThemeModeStore
import com.mfhapps.trendingui.ui.theme.applyThemeNightMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TrendingApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    lateinit var startupSnapshot: StartupSnapshot
        private set

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        applyThemeNightMode(ThemeModeStore.get(this).current())
    }

    override fun onCreate() {
        super.onCreate()
        SplashScreenTiming.init(this)
        val themeModeStore = ThemeModeStore.get(this)
        themeModeStore.ensureMigratedFromLegacyDataStore(this)
        applyThemeNightMode(themeModeStore.current())
        container = AppContainer(this)
        startupSnapshot = runBlocking(Dispatchers.IO) { loadStartupSnapshot() }
        runBlocking(Dispatchers.IO) {
            LauncherIconCoordinator.reconcileOnStartup(container, this@TrendingApplication)
        }
        SplashDrawableCache.warmUp(this)
    }

    private suspend fun loadStartupSnapshot(): StartupSnapshot {
        container.launcherIcons.migrateFromLegacyIfNeeded()
        val launcherIcon = container.launcherIcons.selectedIconOnce()
        val themePreferences = container.themePreferences.preferences.first()
        return StartupSnapshot(
            launcherIcon = launcherIcon,
            themePreferences = themePreferences,
        )
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .crossfade(180)
            .respectCacheHeaders(false)
            .build()
}
