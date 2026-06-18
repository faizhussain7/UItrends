package com.mfhapps.trendingui.launcher

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

data class ActiveLauncherIcon(
    val icon: AppLauncherIcon,
    val darkTheme: Boolean,
)

object LauncherIconManager {

    fun apply(context: Context, target: AppLauncherIcon, darkTheme: Boolean) {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val active = findEnabled(packageManager, packageName)

        if (active?.icon == target && active.darkTheme == darkTheme) {
            return
        }

        disableAllAliases(packageManager, packageName)

        setAliasState(
            packageManager = packageManager,
            packageName = packageName,
            className = target.aliasClassName(darkTheme),
            state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        )
    }

    fun readActive(context: Context): AppLauncherIcon? =
        findEnabled(context.packageManager, context.packageName)?.icon

    fun readActiveOrDefault(context: Context): AppLauncherIcon =
        readActive(context) ?: AppLauncherIcon.Default

    fun readActiveWithTheme(context: Context): ActiveLauncherIcon {
        val packageManager = context.packageManager
        val packageName = context.packageName
        return findEnabled(packageManager, packageName)
            ?: ActiveLauncherIcon(AppLauncherIcon.Default, darkTheme = false)
    }

    private fun findEnabled(
        packageManager: PackageManager,
        packageName: String,
    ): ActiveLauncherIcon? {
        AppLauncherIcon.entries.forEach { icon ->
            if (isAliasEnabled(packageManager, packageName, icon.lightAliasClassName)) {
                return ActiveLauncherIcon(icon, darkTheme = false)
            }
            if (isAliasEnabled(packageManager, packageName, icon.darkAliasClassName)) {
                return ActiveLauncherIcon(icon, darkTheme = true)
            }
        }
        return null
    }

    private fun isAliasEnabled(
        packageManager: PackageManager,
        packageName: String,
        className: String,
    ): Boolean {
        val component = ComponentName(packageName, className)
        return when (packageManager.getComponentEnabledSetting(component)) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> {
                try {
                    packageManager
                        .getActivityInfo(component, PackageManager.MATCH_DISABLED_COMPONENTS)
                        .enabled
                } catch (_: PackageManager.NameNotFoundException) {
                    false
                }
            }
            else -> false
        }
    }

    private fun disableAllAliases(packageManager: PackageManager, packageName: String) {
        AppLauncherIcon.entries.forEach { icon ->
            setAliasState(
                packageManager,
                packageName,
                icon.lightAliasClassName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            )
            setAliasState(
                packageManager,
                packageName,
                icon.darkAliasClassName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            )
        }
    }

    private fun setAliasState(
        packageManager: PackageManager,
        packageName: String,
        className: String,
        state: Int,
    ) {
        if (aliasState(packageManager, packageName, className) == state) {
            return
        }
        packageManager.setComponentEnabledSetting(
            ComponentName(packageName, className),
            state,
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun aliasState(
        packageManager: PackageManager,
        packageName: String,
        className: String,
    ): Int = packageManager.getComponentEnabledSetting(ComponentName(packageName, className))
}
