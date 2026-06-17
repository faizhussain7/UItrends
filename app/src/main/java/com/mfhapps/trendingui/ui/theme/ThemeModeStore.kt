package com.mfhapps.trendingui.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class ThemeModeStore private constructor(
    appContext: Context,
) {
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(readPersisted())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun current(): ThemeMode = _themeMode.value

    fun set(mode: ThemeMode) {
        prefs.edit()
            .putString(KEY_THEME_MODE, mode.name)
            .commit()
        _themeMode.value = mode
    }

    fun ensureMigratedFromLegacyDataStore(context: Context) {
        if (prefs.contains(KEY_THEME_MODE)) return
        runBlocking {
            withContext(Dispatchers.IO) {
                val legacy = context.themeDataStore.data.first()[LEGACY_DATASTORE_KEY]
                val mode = ThemeMode.entries.firstOrNull { it.name == legacy } ?: ThemeMode.System
                set(mode)
            }
        }
    }

    private fun readPersisted(): ThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.System
        return ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.System
    }

    companion object {
        private const val PREFS_NAME = "uitrends_theme_mode"
        private const val KEY_THEME_MODE = "theme_mode"
        private val LEGACY_DATASTORE_KEY = stringPreferencesKey("theme_mode")

        @Volatile
        private var instance: ThemeModeStore? = null

        fun get(context: Context): ThemeModeStore {
            val storeContext = context.applicationContext ?: context
            return instance ?: synchronized(this) {
                instance ?: ThemeModeStore(storeContext).also { instance = it }
            }
        }
    }
}
