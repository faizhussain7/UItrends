package com.mfhapps.trendingui.startup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mfhapps.trendingui.TrendingApplication
import com.mfhapps.trendingui.launcher.SplashScreenTiming
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class AppStartupViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val snapshot = (application as TrendingApplication).startupSnapshot

    private val _holdSystemSplash = MutableStateFlow(true)
    val holdSystemSplash: StateFlow<Boolean> = _holdSystemSplash.asStateFlow()


    private val _splashExitGeneration = MutableStateFlow(0)
    val splashExitGeneration: StateFlow<Int> = _splashExitGeneration.asStateFlow()

    val themePreferences get() = snapshot.themePreferences

    init {
        viewModelScope.launch {
            delay(SplashScreenTiming.minDisplayMs)
            _holdSystemSplash.value = false
        }
    }

    fun notifySplashExitComplete() {
        _splashExitGeneration.value++
    }
}
