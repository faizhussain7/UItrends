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

private const val PretextCameraHudAutoHideMs = 2_600L

@Stable
class PretextCameraHudState internal constructor() {
    var isVisible by mutableStateOf(true)
        private set

    var isSettingsOpen by mutableStateOf(false)
        private set

    internal var lastInteractionMs by mutableLongStateOf(SystemClock.elapsedRealtime())
        private set

    val isChromeVisible: Boolean
        get() = isVisible || isSettingsOpen

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

    internal fun hide() {
        isVisible = false
    }
}

@Composable
fun rememberPretextCameraHudState(
    autoHideDelayMs: Long = PretextCameraHudAutoHideMs,
): PretextCameraHudState {
    val state = remember { PretextCameraHudState() }

    LaunchedEffect(state.isVisible, state.isSettingsOpen, state.lastInteractionMs, autoHideDelayMs) {
        if (state.isSettingsOpen || !state.isVisible) return@LaunchedEffect

        val token = state.lastInteractionMs
        delay(autoHideDelayMs)
        if (!state.isSettingsOpen && state.isVisible && state.lastInteractionMs == token) {
            state.hide()
        }
    }

    return state
}

internal fun Modifier.pretextCameraHudRevealOnTap(state: PretextCameraHudState): Modifier =
    pointerInput(state) {
        detectTapGestures(onTap = { state.reveal() })
    }

internal data class PretextCameraHudContent(
    val shapeLabel: String,
    val statusText: String,
    val statusActive: Boolean,
    val telemetry: VisionTelemetry,
    val stage: PretextCameraStage,
    val stageTheme: PretextStageTheme,
    val torchAvailable: Boolean,
    val torchOn: Boolean,
    val showDragHint: Boolean,
)

internal fun buildPretextCameraHudContent(
    manualOverride: Boolean,
    viewShape: ViewShape?,
    editorialOrbs: List<ViewShape>,
    trackMode: VisionTrackMode,
    telemetry: VisionTelemetry,
    lastDetectReport: VisionDetectReport? = null,
    stage: PretextCameraStage,
    stageTheme: PretextStageTheme,
    torchAvailable: Boolean,
    torchOn: Boolean,
    measureMode: PretextMeasureMode,
): PretextCameraHudContent {
    val statusActive = manualOverride || viewShape != null || editorialOrbs.isNotEmpty()
    val statusText = when {
        manualOverride -> "Manual override"
        viewShape?.isLiveDetection == true -> buildString {
            append(viewShape.source.label)
            viewShape.label?.let { append(" · $it") }
        }
        viewShape != null -> buildString {
            append(viewShape.source.label)
            viewShape.label?.let { append(" · $it") }
            append(" · held")
        }
        else -> when (trackMode) {
            VisionTrackMode.Person -> "Looking for a person…"
            VisionTrackMode.Face -> "Looking for a face…"
            VisionTrackMode.Object -> objectDetectStatus(
                telemetry = telemetry,
                report = lastDetectReport,
            )
            VisionTrackMode.Auto -> "Auto: scoring face, body, and object…"
        }
    }

    return PretextCameraHudContent(
        shapeLabel = pretextContourShapeLabel(viewShape?.source ?: VisionSource.Idle),
        statusText = statusText,
        statusActive = statusActive,
        telemetry = telemetry,
        stage = stage,
        stageTheme = stageTheme,
        torchAvailable = torchAvailable,
        torchOn = torchOn,
        showDragHint = measureMode == PretextMeasureMode.Engine,
    )
}

internal fun pretextContourShapeLabel(source: VisionSource): String = when (source) {
    VisionSource.Person -> "TFLite segmentation"
    VisionSource.Face -> "Face contour"
    VisionSource.Object -> "Object contour"
    VisionSource.Manual -> "Manual override"
    VisionSource.Idle -> "Scanning"
}

private fun objectDetectStatus(
    telemetry: VisionTelemetry,
    report: VisionDetectReport?,
): String {
    val note = report?.note ?: telemetry.lastAccuracy?.note
    val backend = report?.backend ?: telemetry.lastBackend
    return when {
        !telemetry.ncnnReady || note == "ncnn-not-ready" ->
            "NCNN not in APK — download assets, then ./gradlew clean assembleDebug"
        note == "non-direct-buffer" ->
            "Camera buffer incompatible with NCNN"
        note == "bad-packet" -> "Detection parse error"
        note == "bad-planes" -> "Camera frame unavailable"
        note == "layout-missing" -> "Mapping contour to screen…"
        backend == "error" && note != null -> "Vision error · $note"
        backend == "native-yuv-ncnn" && note == "empty-packet" -> "Looking for objects…"
        else -> "Looking for objects…"
    }
}
