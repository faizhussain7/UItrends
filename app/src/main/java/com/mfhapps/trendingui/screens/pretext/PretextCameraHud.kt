package com.mfhapps.trendingui.screens.pretext

internal data class PretextCameraHudContent(
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
    extraShapes: List<ViewShape> = emptyList(),
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
    val trackedShapes = buildList {
        viewShape?.let { add(it) }
        addAll(extraShapes)
    }
    val statusActive = manualOverride || trackedShapes.isNotEmpty() || editorialOrbs.isNotEmpty()
    val statusText = when {
        manualOverride -> "Manual override"
        trackedShapes.size > 1 -> trackedMultiStatus(trackedShapes)
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

private fun trackedMultiStatus(shapes: List<ViewShape>): String {
    val faces = shapes.count { it.source == VisionSource.Face }
    val persons = shapes.count { it.source == VisionSource.Person }
    val objects = shapes.count { it.source == VisionSource.Object }
    val parts = buildList {
        if (faces > 0) add("$faces face${if (faces > 1) "s" else ""}")
        if (persons > 0) add("$persons person${if (persons > 1) "s" else ""}")
        if (objects > 0) add("$objects object${if (objects > 1) "s" else ""}")
    }
    return if (parts.isEmpty()) {
        "${shapes.size} shapes tracked"
    } else {
        parts.joinToString(" · ") + " tracked"
    }
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
