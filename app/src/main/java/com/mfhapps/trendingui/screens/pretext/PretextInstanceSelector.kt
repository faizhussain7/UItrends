package com.mfhapps.trendingui.screens.pretext

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

internal object PretextInstanceSelector {

    private const val CROSS_CLASS_IOU = 0.42f

    fun mergeAuto(
        faceReports: List<VisionDetectReport>,
        personReports: List<VisionDetectReport>,
        objectReports: List<VisionDetectReport>,
        maxTotal: Int,
    ): List<VisionDetectReport> {
        val ranked = (faceReports + personReports + objectReports)
            .filter { report ->
                val contour = report.contour ?: return@filter false
                if (report.note in REJECTED_NOTES) return@filter false
                val quality = PretextShapeAnalyzer.analyze(contour, report.score)
                PretextShapeAnalyzer.isPublishable(quality, contour.source)
            }
            .sortedByDescending { rankScore(it) }
        return suppressOverlapping(ranked, CROSS_CLASS_IOU).take(maxTotal.coerceAtLeast(1))
    }

    fun suppressOverlapping(
        reports: List<VisionDetectReport>,
        iouThreshold: Float,
    ): List<VisionDetectReport> {
        val kept = ArrayList<VisionDetectReport>()
        for (candidate in reports) {
            val contour = candidate.contour ?: continue
            val bounds = contour.boundsRectNorm()
            if (kept.any { existing ->
                    val other = existing.contour?.boundsRectNorm() ?: return@any false
                    iouNorm(bounds, other) > iouThreshold
                }) {
                continue
            }
            kept += candidate
        }
        return kept
    }

    private fun rankScore(report: VisionDetectReport): Float {
        val contour = report.contour ?: return 0f
        val quality = PretextShapeAnalyzer.analyze(contour, report.score)
        val area = bboxArea(contour)
        val detScore = report.score ?: 0.4f
        val sourceBoost = when (contour.source) {
            VisionSource.Face -> 1.08f
            VisionSource.Person -> 1.02f
            VisionSource.Object -> 1f
            else -> 0.9f
        }
        return detScore * sourceBoost * area.coerceIn(0.01f, 0.5f) * quality.score
    }

    private fun bboxArea(contour: VisionContour): Float {
        val rect = contour.boundsRectNorm()
        return rect.width() * rect.height()
    }

    private fun iouNorm(a: RectF, b: RectF): Float {
        val ix0 = max(a.left, b.left)
        val iy0 = max(a.top, b.top)
        val ix1 = min(a.right, b.right)
        val iy1 = min(a.bottom, b.bottom)
        val inter = max(0f, ix1 - ix0) * max(0f, iy1 - iy0)
        val union = a.width() * a.height() + b.width() * b.height() - inter
        return if (union > 1e-6f) inter / union else 0f
    }

    private val REJECTED_NOTES = setOf(
        "frame-fill",
        "area-too-large",
        "mask-failed",
        "contour-failed",
        "face-too-small",
        "face-too-large",
        "face-quality",
        "low-foreground",
    )
}
