package com.mfhapps.trendingui.screens.pretext

internal data class PretextDetectionBundle(
    val face: VisionDetectReport,
    val person: VisionDetectReport,
    val objectReport: VisionDetectReport,
)

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

    private const val OBJECT_MIN_SCORE = 0.28f
    private const val OBJECT_MAX_AREA = 0.55f
    private const val OBJECT_MIN_AREA = 0.01f

    private const val SWITCH_MARGIN = 0.18f

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

    data class Candidate(
        val report: VisionDetectReport,
        val source: VisionSource,
        val rankScore: Float,
        val reason: String,
    ) {
        fun toPick(): Pick = Pick(
            report = report.copy(
                backend = "auto",
                autoPickSource = source,
                autoPickReason = reason,
            ),
            source = source,
            reason = reason,
        )
    }

    data class Pick(
        val report: VisionDetectReport,
        val source: VisionSource,
        val reason: String,
    )

    data class MultiPick(
        val primary: Pick?,
        val extras: List<Pick>,
    )

    fun selectPrimary(
        bundle: PretextDetectionBundle,
        activeSource: VisionSource? = null,
    ): Pick? = applyHysteresis(scoreAll(bundle), activeSource)?.toPick()

    fun selectMulti(
        bundle: PretextDetectionBundle,
        maxShapes: Int = 3,
        activeSource: VisionSource? = null,
    ): MultiPick {
        val ranked = scoreAll(bundle)
        if (ranked.isEmpty()) return MultiPick(null, emptyList())
        val primary = applyHysteresis(ranked, activeSource) ?: ranked.first()
        val extras = ranked
            .filter { it.source != primary.source }
            .take((maxShapes - 1).coerceAtLeast(0))
        return MultiPick(primary.toPick(), extras.map { it.toPick() })
    }

    private fun scoreAll(bundle: PretextDetectionBundle): List<Candidate> = listOfNotNull(
        scoreFace(bundle.face),
        scorePerson(bundle.person),
        scoreObject(bundle.objectReport),
    ).sortedByDescending { it.rankScore }

    private fun applyHysteresis(
        ranked: List<Candidate>,
        activeSource: VisionSource?,
    ): Candidate? {
        if (ranked.isEmpty()) return null
        val best = ranked.first()
        val stickySource = activeSource?.takeUnless {
            it == VisionSource.Idle || it == VisionSource.Manual
        } ?: return best
        val active = ranked.find { it.source == stickySource } ?: return best
        return if (best.source != stickySource && best.rankScore < active.rankScore * (1f + SWITCH_MARGIN)) {
            active
        } else {
            best
        }
    }

    private fun scoreFace(report: VisionDetectReport): Candidate? {
        val contour = report.contour ?: return null
        val area = bboxArea(contour)
        val detScore = report.score ?: 0.5f
        if (detScore < FACE_MIN_SCORE) return null
        if (area !in FACE_MIN_AREA..FACE_MAX_AREA) return null
        if (report.note == "contour-failed") return null
        val rank = detScore * areaPenalty(area, ideal = 0.06f, max = FACE_MAX_AREA)
        val reason = "face score=${"%.2f".format(detScore)} area=${"%.3f".format(area)} rank=${"%.2f".format(rank)}"
        return Candidate(report, VisionSource.Face, rank, reason)
    }

    private fun scorePerson(report: VisionDetectReport): Candidate? {
        val contour = report.contour ?: return null
        val area = bboxArea(contour)
        val detScore = report.score ?: 0f
        if (detScore < PERSON_MIN_SCORE) return null
        if (area !in PERSON_MIN_AREA..PERSON_MAX_AREA) return null
        if (report.note in REJECTED_NOTES || report.isBlobBoxFallback()) return null
        val rank = detScore.coerceAtMost(0.5f) * 2f * areaPenalty(area, ideal = 0.18f, max = PERSON_MAX_AREA)
        val reason = "person score=${"%.3f".format(detScore)} area=${"%.3f".format(area)} rank=${"%.2f".format(rank)}"
        return Candidate(report, VisionSource.Person, rank, reason)
    }

    private fun scoreObject(report: VisionDetectReport): Candidate? {
        val contour = report.contour ?: return null
        val area = bboxArea(contour)
        val detScore = report.score ?: 0.4f
        if (detScore < OBJECT_MIN_SCORE) return null
        if (area !in OBJECT_MIN_AREA..OBJECT_MAX_AREA) return null
        val label = contour.label?.lowercase() ?: ""
        val labelBoost = if (label in PREFERRED_OBJECT_LABELS) 1.15f else 1f
        val rank = detScore * labelBoost * areaPenalty(area, ideal = 0.08f, max = OBJECT_MAX_AREA)
        val reason = "object score=${"%.2f".format(detScore)} label=$label area=${"%.3f".format(area)} rank=${"%.2f".format(rank)}"
        return Candidate(report, VisionSource.Object, rank, reason)
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
