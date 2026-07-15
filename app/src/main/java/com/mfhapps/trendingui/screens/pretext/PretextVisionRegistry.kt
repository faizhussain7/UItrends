package com.mfhapps.trendingui.screens.pretext

internal class PretextVisionRegistry(
    private val face: PretextVisionBackend,
    private val person: PretextVisionBackend,
    private val objectBackend: PretextVisionBackend,
) {

    data class AutoDetectBundle(
        val primary: VisionDetectReport,
        val extras: List<VisionDetectReport> = emptyList(),
    )

    fun detect(
        mode: VisionTrackMode,
        frame: PretextVisionFrame,
        activeSource: VisionSource?,
    ): VisionDetectReport = when (mode) {
        VisionTrackMode.Face -> safeDetect(face, frame)
        VisionTrackMode.Person -> safeDetect(person, frame)
        VisionTrackMode.Object -> safeDetect(objectBackend, frame)
        VisionTrackMode.Auto -> {
            val bundle = detectAll(frame)
            PretextAutoSelector.selectPrimary(bundle, activeSource)?.report
                ?: VisionDetectReport(null, "auto", note = "no-pick")
        }
    }

    fun detectAutoMulti(
        frame: PretextVisionFrame,
        maxShapes: Int,
        activeSource: VisionSource?,
    ): AutoDetectBundle {
        val multi = PretextAutoSelector.selectMulti(detectAll(frame), maxShapes, activeSource)
        val primaryPick = multi.primary
        if (primaryPick != null) {
            return AutoDetectBundle(primaryPick.report, multi.extras.map { it.report })
        }
        return AutoDetectBundle(VisionDetectReport(null, "auto", note = "no-pick"))
    }

    fun detectAll(frame: PretextVisionFrame): PretextDetectionBundle =
        PretextDetectionBundle(
            face = safeDetect(face, frame),
            person = safeDetect(person, frame),
            objectReport = safeDetect(objectBackend, frame),
        )

    private fun safeDetect(backend: PretextVisionBackend, frame: PretextVisionFrame): VisionDetectReport =
        runCatching { backend.detect(frame) }
            .getOrElse { error ->
                VisionDetectReport(null, backend.backendLabel, note = error.javaClass.simpleName)
            }
}
