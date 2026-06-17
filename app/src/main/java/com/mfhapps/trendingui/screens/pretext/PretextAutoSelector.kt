package com.mfhapps.trendingui.screens.pretext

internal object PretextAutoSelector {

    private val REJECTED_NOTES = setOf(
        "frame-fill",
        "frame-fill-clamped",
        "area-too-large",
        "mask-failed",
        "bbox-fallback",
        "contour-failed",
    )

    private const val FACE_MIN_SCORE = 0.45f
    private const val FACE_MAX_AREA = 0.38f
    private const val FACE_MIN_AREA = 0.008f

    private const val PERSON_MIN_SCORE = 0.006f
    private const val PERSON_MAX_AREA = 0.72f
    private const val PERSON_MIN_AREA = 0.015f

    private const val OBJECT_MIN_SCORE = 0.35f
    private const val OBJECT_MAX_AREA = 0.55f
    private const val OBJECT_MIN_AREA = 0.01f

    private val PREFERRED_OBJECT_LABELS = setOf(
        "cell phone",
        "cup",
        "laptop",
        "book",
        "bottle",
        "keyboard",
        "mouse",
        "remote",
        "tv",
        "clock",
        "vase",
        "wine glass",
        "bowl",
        "backpack",
        "handbag",
        "suitcase",
    )

    data class Pick(
        val report: VisionDetectReport,
        val source: VisionSource,
        val reason: String,
    )

    data class MultiPick(
        val primary: Pick?,
        val extras: List<Pick>,
    )

    fun pickPrimary(
        face: VisionDetectReport,
        person: VisionDetectReport,
        objectReport: VisionDetectReport,
    ): Pick? = rankAll(face, person, objectReport).firstOrNull()

    fun pickMulti(
        face: VisionDetectReport,
        person: VisionDetectReport,
        objectReport: VisionDetectReport,
        maxShapes: Int = 3,
    ): MultiPick {
        val ranked = rankAll(face, person, objectReport)
        if (ranked.isEmpty()) return MultiPick(null, emptyList())
        val primary = ranked.first()
        val extras = ranked.drop(1).take((maxShapes - 1).coerceAtLeast(0))
        return MultiPick(primary, extras)
    }

    private fun rankAll(
        face: VisionDetectReport,
        person: VisionDetectReport,
        objectReport: VisionDetectReport,
    ): List<Pick> = listOfNotNull(
        scoreFace(face)?.toPick(),
        scorePerson(person)?.toPick(),
        scoreObject(objectReport)?.toPick(),
    )

    private data class Scored(
        val report: VisionDetectReport,
        val source: VisionSource,
        val rankScore: Float,
        val areaNorm: Float,
        val reason: String,
    ) {
        fun toPick() = Pick(
            report = report.copy(
                backend = "auto",
                autoPickSource = source,
                autoPickReason = reason,
            ),
            source = source,
            reason = reason,
        )
    }

    private fun scoreFace(report: VisionDetectReport): Scored? {
        val contour = report.contour ?: return null
        val area = bboxArea(contour)
        val detScore = report.score ?: 0.5f
        if (detScore < FACE_MIN_SCORE) return null
        if (area !in FACE_MIN_AREA..FACE_MAX_AREA) return null
        if (report.note == "contour-failed") return null
        val rank = detScore * areaPenalty(area, ideal = 0.06f, max = FACE_MAX_AREA)
        val reason = "score=${"%.2f".format(detScore)} area=${"%.3f".format(area)}"
        return Scored(report, VisionSource.Face, rank, area, reason)
    }

    private fun scorePerson(report: VisionDetectReport): Scored? {
        val contour = report.contour ?: return null
        val area = bboxArea(contour)
        val detScore = report.score ?: 0f
        if (detScore < PERSON_MIN_SCORE) return null
        if (area !in PERSON_MIN_AREA..PERSON_MAX_AREA) return null
        if (report.note in REJECTED_NOTES || report.isBlobBoxFallback()) return null
        val rank = detScore.coerceAtMost(0.5f) * 2f * areaPenalty(area, ideal = 0.18f, max = PERSON_MAX_AREA)
        val reason = "score=${"%.3f".format(detScore)} area=${"%.3f".format(area)}"
        return Scored(report, VisionSource.Person, rank, area, reason)
    }

    private fun scoreObject(report: VisionDetectReport): Scored? {
        val contour = report.contour ?: return null
        val area = bboxArea(contour)
        val detScore = report.score ?: 0.4f
        if (detScore < OBJECT_MIN_SCORE) return null
        if (area !in OBJECT_MIN_AREA..OBJECT_MAX_AREA) return null
        val label = contour.label?.lowercase() ?: ""
        val labelBoost = if (label in PREFERRED_OBJECT_LABELS) 1.15f else 1f
        val rank = detScore * labelBoost * areaPenalty(area, ideal = 0.08f, max = OBJECT_MAX_AREA)
        val reason = "score=${"%.2f".format(detScore)} label=$label area=${"%.3f".format(area)}"
        return Scored(report, VisionSource.Object, rank, area, reason)
    }

    private fun bboxArea(contour: VisionContour): Float {
        val w = (contour.right - contour.left).coerceAtLeast(0f)
        val h = (contour.bottom - contour.top).coerceAtLeast(0f)
        return w * h
    }

    private fun areaPenalty(area: Float, ideal: Float, max: Float): Float {
        if (area > max) return 0.2f
        val d = kotlin.math.abs(area - ideal) / ideal
        return (1f - d.coerceIn(0f, 0.85f)).coerceIn(0.25f, 1f)
    }
}
