package com.mfhapps.trendingui.screens.pretext

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PretextRecordingController(
    private val appContext: Context,
    private val recordingRepository: PretextRecordingRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow<PretextRecordingSessionState>(PretextRecordingSessionState.Idle)
    val state: StateFlow<PretextRecordingSessionState> = _state.asStateFlow()

    private val recorder = PretextRegionVideoRecorder(
        activityProvider = { activityRef },
        captureBoundsProvider = { captureBoundsProvider?.invoke() },
    )

    @Volatile
    private var activityRef: Activity? = null

    @Volatile
    private var captureBoundsProvider: (() -> Rect?)? = null

    @Volatile
    private var pendingFile: File? = null

    val regionRecorder: PretextRegionVideoRecorder
        get() = recorder

    val isRecording: Boolean
        get() = _state.value is PretextRecordingSessionState.Active

    fun attachActivity(activity: Activity) {
        activityRef = activity
    }

    fun detachActivity(activity: Activity) {
        if (activityRef === activity) {
            activityRef = null
        }
    }

    fun setCaptureBoundsProvider(provider: () -> Rect?) {
        captureBoundsProvider = provider
    }

    fun startRecording(
        file: File,
        profile: PretextRecordingProfile,
        bounds: Rect,
    ): Boolean {
        if (isRecording) return false
        if (!recorder.start(file, profile, bounds)) return false
        pendingFile = file
        _state.value = PretextRecordingSessionState.Active(
            startedAtMillis = System.currentTimeMillis(),
            captureSuspended = false,
        )
        return true
    }

    fun stopRecording(onFinished: (saved: Boolean) -> Unit = {}) {
        if (!isRecording) {
            onFinished(false)
            return
        }
        scope.launch {
            val saved = stopRecordingInternal()
            onFinished(saved)
        }
    }

    suspend fun stopRecordingAndPersist(): Boolean = withContext(Dispatchers.Main.immediate) {
        stopRecordingInternal()
    }

    fun onUiHidden() {
        val current = _state.value as? PretextRecordingSessionState.Active ?: return
        recorder.pauseCapture()
        _state.value = current.copy(captureSuspended = true)
    }

    fun onUiVisible() {
        val current = _state.value as? PretextRecordingSessionState.Active ?: return
        recorder.resumeCapture()
        _state.value = current.copy(captureSuspended = false)
    }

    fun releaseIfIdle() {
        if (!isRecording) {
            recorder.release()
        }
    }

    private suspend fun stopRecordingInternal(): Boolean {
        val saved = recorder.stop()
        val file = pendingFile
        pendingFile = null
        _state.value = PretextRecordingSessionState.Idle
        return if (saved && file != null && file.exists() && file.length() > 0L) {
            withContext(Dispatchers.IO) {
                recordingRepository.persistRecording(file)
            }
            true
        } else {
            file?.delete()
            false
        }
    }
}
