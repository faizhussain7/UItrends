package com.mfhapps.trendingui.screens.pretext

internal class PretextVisionRegistry(
    private val face: PretextVisionBackend,
    private val person: PretextVisionBackend,
    private val objectBackend: PretextVisionBackend,
) {

    fun detect(
        mode: VisionTrackMode,
        frame: PretextVisionFrame,
        activeSource: VisionSource?,
    ): VisionDetectReport = detectMulti(mode, frame, maxInstances = 1, activeSource).firstOrNull()
        ?: when (mode) {
            VisionTrackMode.Face -> VisionDetectReport(null, "auto", note = "no-face")
            VisionTrackMode.Person -> VisionDetectReport(null, "auto", note = "mask-failed")
            VisionTrackMode.Object -> VisionDetectReport(null, "auto", note = "no-object")
            VisionTrackMode.Auto -> VisionDetectReport(null, "auto", note = "no-pick")
        }

    fun detectMulti(
        mode: VisionTrackMode,
        frame: PretextVisionFrame,
        maxInstances: Int,
        activeSource: VisionSource?,
    ): List<VisionDetectReport> = when (mode) {
        VisionTrackMode.Face -> safeDetectMulti(face, frame, maxInstances)
        VisionTrackMode.Person -> safeDetectMulti(person, frame, maxInstances)
        VisionTrackMode.Object -> safeDetectMulti(objectBackend, frame, maxInstances)
        VisionTrackMode.Auto -> {
            val faces = safeDetectMulti(face, frame, PretextVisionLimits.MAX_FACES)
            val persons = safeDetectMulti(person, frame, PretextVisionLimits.MAX_PERSONS)
            val objects = safeDetectMulti(objectBackend, frame, PretextVisionLimits.MAX_OBJECTS)
            PretextInstanceSelector.mergeAuto(
                faceReports = faces,
                personReports = persons,
                objectReports = objects,
                maxTotal = maxInstances.coerceAtMost(PretextVisionLimits.MAX_AUTO),
            )
        }
    }

    private fun safeDetectMulti(
        backend: PretextVisionBackend,
        frame: PretextVisionFrame,
        maxInstances: Int,
    ): List<VisionDetectReport> =
        runCatching { backend.detectMulti(frame, maxInstances) }
            .getOrElse { emptyList() }
}
