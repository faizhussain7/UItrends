package com.mfhapps.trendingui.screens.pretext

sealed interface PretextRecordingSessionState {
    data object Idle : PretextRecordingSessionState

    data class Active(
        val startedAtMillis: Long,
        val captureSuspended: Boolean,
    ) : PretextRecordingSessionState
}
