package com.mfhapps.trendingui.screens.pretext

import android.os.SystemClock
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

@Stable
class PretextOverlayControlsState internal constructor() {
    var isVisible by mutableStateOf(true)
        private set

    var isSettingsOpen by mutableStateOf(false)
        private set

    var isRecordingSettingsOpen by mutableStateOf(false)
        private set

    internal var lastInteractionMs by mutableLongStateOf(SystemClock.elapsedRealtime())
        private set

    val isChromeVisible: Boolean
        get() = isVisible || isSettingsOpen || isRecordingSettingsOpen

    fun reveal() {
        isVisible = true
        lastInteractionMs = SystemClock.elapsedRealtime()
    }

    fun openSettings() {
        isSettingsOpen = true
        reveal()
    }

    fun closeSettings() {
        isSettingsOpen = false
        reveal()
    }

    fun openRecordingSettings() {
        isRecordingSettingsOpen = true
        reveal()
    }

    fun closeRecordingSettings() {
        isRecordingSettingsOpen = false
        reveal()
    }

    internal fun hide() {
        isVisible = false
    }

    internal fun toggleFromTap() {
        if (isVisible) {
            hide()
        } else {
            reveal()
        }
    }
}

@Composable
private fun PretextOverlayAutoHideEffect(
    state: PretextOverlayControlsState,
    autoHideDelayMs: Long,
    enabled: Boolean,
) {
    LaunchedEffect(
        state.isVisible,
        state.isSettingsOpen,
        state.isRecordingSettingsOpen,
        state.lastInteractionMs,
        autoHideDelayMs,
        enabled,
    ) {
        if (!enabled || state.isSettingsOpen || state.isRecordingSettingsOpen || !state.isVisible) {
            return@LaunchedEffect
        }
        val token = state.lastInteractionMs
        delay(autoHideDelayMs)
        if (
            enabled &&
            !state.isSettingsOpen &&
            !state.isRecordingSettingsOpen &&
            state.isVisible &&
            state.lastInteractionMs == token
        ) {
            state.hide()
        }
    }
}

@Composable
fun rememberPretextCameraHudState(isRecording: Boolean = false): PretextOverlayControlsState {
    val state = remember { PretextOverlayControlsState() }
    val autoHideDelayMs = if (isRecording) {
        PretextRecordingConfig.OVERLAY_HIDE_RECORDING_MS
    } else {
        PretextRecordingConfig.OVERLAY_HIDE_CAMERA_MS
    }
    PretextOverlayAutoHideEffect(
        state = state,
        autoHideDelayMs = autoHideDelayMs,
        enabled = true,
    )
    return state
}

@Composable
fun rememberPretextPlayerOverlayState(
    isPlaying: Boolean,
    scrubbing: Boolean,
): PretextOverlayControlsState {
    val state = remember { PretextOverlayControlsState() }
    val autoHideDelayMs = if (isPlaying) {
        PretextRecordingConfig.OVERLAY_HIDE_PLAYING_MS
    } else {
        PretextRecordingConfig.OVERLAY_HIDE_PAUSED_MS
    }
    PretextOverlayAutoHideEffect(
        state = state,
        autoHideDelayMs = autoHideDelayMs,
        enabled = state.isVisible && !scrubbing,
    )
    return state
}

internal fun Modifier.pretextOverlayRevealOnTap(state: PretextOverlayControlsState): Modifier =
    pointerInput(state) {
        detectTapGestures(onTap = { state.reveal() })
    }

internal fun Modifier.pretextPlayerOverlayToggleOnTap(state: PretextOverlayControlsState): Modifier =
    pointerInput(state) {
        detectTapGestures(onTap = { state.toggleFromTap() })
    }

typealias PretextCameraHudState = PretextOverlayControlsState

internal fun Modifier.pretextCameraHudRevealOnTap(state: PretextCameraHudState): Modifier =
    pretextOverlayRevealOnTap(state)
