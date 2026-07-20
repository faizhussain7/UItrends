package com.mfhapps.trendingui.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mfhapps.trendingui.TrendingApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherIconViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val container = (application as TrendingApplication).container

    val selectedIcon: StateFlow<AppLauncherIcon> = container.launcherIcons.selectedIcon.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = (application as TrendingApplication).startupSnapshot.launcherIcon,
    )

    fun setLauncherIcon(icon: AppLauncherIcon, restartProcess: Boolean = false) {
        if (icon == selectedIcon.value && !restartProcess) return
        viewModelScope.launch {
            LauncherIconCoordinator.applySelection(
                context = getApplication(),
                icon = icon,
                container = container,
            )
            if (restartProcess) {
                AppProcessRestarter.restart(getApplication())
            }
        }
    }
}
