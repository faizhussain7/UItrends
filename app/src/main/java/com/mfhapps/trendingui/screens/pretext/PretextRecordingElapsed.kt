package com.mfhapps.trendingui.screens.pretext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
internal fun rememberRecordingElapsedLabel(
    active: Boolean,
    startedAtMillis: Long,
): String {
    var label by remember { mutableStateOf("00:00") }

    androidx.compose.runtime.LaunchedEffect(active, startedAtMillis) {
        if (!active) {
            label = "00:00"
            return@LaunchedEffect
        }
        while (isActive) {
            label = formatRecordingElapsed(startedAtMillis)
            delay(PretextRecordingConfig.RECORDING_ELAPSED_TICK_MS)
        }
    }

    return label
}

internal fun formatRecordingElapsed(startedAtMillis: Long): String {
    val elapsedSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1000L).coerceAtLeast(0L)
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
