package com.mfhapps.trendingui.launcher

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mfhapps.trendingui.ui.theme.themeDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.launcherIconDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "uitrends_launcher_icon",
)

private val KEY_SELECTED_ICON = stringPreferencesKey("selected_launcher_icon")
private val LEGACY_THEME_KEY_LAUNCHER_ICON = stringPreferencesKey("launcher_icon")

class LauncherIconRepository(
    private val context: Context,
) {
    val selectedIcon: Flow<AppLauncherIcon> = context.launcherIconDataStore.data.map { prefs ->
        AppLauncherIcon.fromStoredName(prefs[KEY_SELECTED_ICON])
    }

    suspend fun selectedIconOnce(): AppLauncherIcon = selectedIcon.first()

    suspend fun setSelectedIcon(icon: AppLauncherIcon) {
        context.launcherIconDataStore.edit { prefs ->
            prefs[KEY_SELECTED_ICON] = icon.name
        }
    }


    suspend fun migrateFromLegacyIfNeeded() {
        val store = context.launcherIconDataStore.data.first()
        if (KEY_SELECTED_ICON in store) return

        val legacyIcon = readLegacyThemePreference()
        val resolved = legacyIcon ?: LauncherIconManager.readActiveOrDefault(context)
        setSelectedIcon(resolved)
    }

    private suspend fun readLegacyThemePreference(): AppLauncherIcon? {
        val legacyStore = context.themeDataStore.data.first()
        val raw = legacyStore[LEGACY_THEME_KEY_LAUNCHER_ICON] ?: return null
        return AppLauncherIcon.entries.firstOrNull { it.name == raw }
    }
}
