package com.mfhapps.trendingui.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mfhapps.trendingui.TrendingApplication
import com.mfhapps.trendingui.launcher.LauncherIconCoordinator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as TrendingApplication).container.themePreferences
    private val themeModeStore = ThemeModeStore.get(application)

    val preferences: StateFlow<ThemePreferences> = repository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemePreferences(themeMode = themeModeStore.current()),
    )

    fun setThemeMode(mode: ThemeMode) {
        if (mode == themeModeStore.current()) return
        repository.setThemeMode(mode)
        val app = getApplication<Application>()
        app.applyThemeNightMode(mode)
        viewModelScope.launch {
            if (repository.syncLauncherIconWithThemeOnce()) {
                LauncherIconCoordinator.syncTheme(app, syncLauncherIconWithTheme = true)
            }
        }
    }

    fun setUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.setUseDynamicColor(enabled) }
    }

    fun setBrandAccentColor(accent: BrandAccentColor) {
        viewModelScope.launch { repository.setBrandAccentColor(accent) }
    }

    fun setHomeLayoutStyle(style: HomeLayoutStyle) {
        viewModelScope.launch { repository.setHomeLayoutStyle(style) }
    }

    fun setBlurModalBackdrop(enabled: Boolean) {
        viewModelScope.launch { repository.setBlurModalBackdrop(enabled) }
    }

    fun setModalBackdropStyle(style: ModalBackdropStyle) {
        viewModelScope.launch { repository.setModalBackdropStyle(style) }
    }

    fun setSyncLauncherIconWithTheme(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSyncLauncherIconWithTheme(enabled)
            if (enabled) {
                LauncherIconCoordinator.syncTheme(getApplication(), syncLauncherIconWithTheme = true)
            }
        }
    }
}
