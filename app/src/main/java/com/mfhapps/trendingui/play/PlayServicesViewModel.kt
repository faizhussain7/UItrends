package com.mfhapps.trendingui.play

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mfhapps.trendingui.TrendingApplication
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PlayServicesViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val container = (application as TrendingApplication).container
    private val updateRepository = container.playUpdate
    private val reviewRepository = container.playReview

    private val _updateInstallReady = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val updateInstallReady: SharedFlow<Unit> = _updateInstallReady.asSharedFlow()

    init {
        viewModelScope.launch {
            updateRepository.installReadyEvents().collect {
                _updateInstallReady.emit(Unit)
            }
        }
    }

    fun checkForUpdate(activity: Activity) {
        viewModelScope.launch {
            updateRepository.checkAndStartUpdate(activity)
        }
    }

    fun completeFlexibleUpdate() {
        updateRepository.completeFlexibleUpdate()
    }

    fun onDemoClosed(activity: Activity) {
        viewModelScope.launch {
            reviewRepository.onDemoClosed(activity)
        }
    }

    fun requestReview(activity: Activity) {
        viewModelScope.launch {
            reviewRepository.launchReview(activity, force = true)
        }
    }
}
