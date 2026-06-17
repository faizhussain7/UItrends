package com.mfhapps.trendingui.launcher

import android.content.Context
import com.mfhapps.trendingui.di.AppContainer
import com.mfhapps.trendingui.ui.theme.ThemeModeStore
import com.mfhapps.trendingui.ui.theme.isSystemInNightMode
import com.mfhapps.trendingui.ui.theme.resolveDarkTheme

object LauncherIconCoordinator {

    suspend fun applySelection(
        context: Context,
        icon: AppLauncherIcon,
        container: AppContainer,
    ): LauncherIconChangeResult {
        val darkTheme = resolveLauncherDarkTheme(context)
        val previous = container.launcherIcons.selectedIconOnce()
        val active = LauncherIconManager.readActive(context)
        if (previous == icon && active == icon &&
            LauncherIconManager.readActiveWithTheme(context).darkTheme == darkTheme
        ) {
            return LauncherIconChangeResult.Unchanged
        }
        container.launcherIcons.setSelectedIcon(icon)
        LauncherIconManager.apply(context, icon, darkTheme)
        return LauncherIconChangeResult.Applied(
            icon = icon,
            launcherUpdatesImmediately = true,
            splashUpdatesOnNextColdStart = true,
        )
    }

    suspend fun reconcileOnStartup(container: AppContainer, context: Context): AppLauncherIcon {
        container.launcherIcons.migrateFromLegacyIfNeeded()
        val stored = container.launcherIcons.selectedIconOnce()
        val darkTheme = resolveLauncherDarkTheme(context)
        val active = LauncherIconManager.readActiveWithTheme(context)
        return if (stored == active.icon && active.darkTheme == darkTheme) {
            stored
        } else {
            LauncherIconManager.apply(context, stored, darkTheme)
            stored
        }
    }

    fun syncTheme(context: Context) {
        val darkTheme = resolveLauncherDarkTheme(context)
        val icon = LauncherIconManager.readActiveOrDefault(context)
        LauncherIconManager.apply(context, icon, darkTheme)
    }

    private fun resolveLauncherDarkTheme(context: Context): Boolean {
        val themeMode = ThemeModeStore.get(context).current()
        return resolveDarkTheme(themeMode, context.resources.configuration.isSystemInNightMode)
    }
}

sealed class LauncherIconChangeResult {
    data object Unchanged : LauncherIconChangeResult()

    data class Applied(
        val icon: AppLauncherIcon,
        val launcherUpdatesImmediately: Boolean,
        val splashUpdatesOnNextColdStart: Boolean,
    ) : LauncherIconChangeResult()
}
