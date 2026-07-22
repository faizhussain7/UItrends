package com.mfhapps.trendingui.screens.pretext

import kotlin.math.abs

internal data class ShapeQuality(
    val score: Float,
    val polygonVertices: Int,
    val fillRatio: Float,
    val isMaskContour: Boolean,
)

internal object PretextShapeAnalyzer {

    fun analyze(contour: VisionContour, detScore: Float?): ShapeQuality {
        val poly = contour.polygonNorm.orEmpty()
        val verts = poly.size
        val bboxArea = contour.boundsRectNorm().let { it.width() * it.height() }.coerceAtLeast(1e-5f)
        val polyArea = shoelaceNorm(poly).coerceAtLeast(0f)
        val fill = (polyArea / bboxArea).coerceIn(0f, 1.2f)
        val maskLike = verts > 4
        val det = detScore ?: 0.5f
        val quality = (
            (if (maskLike) 0.42f else 0.08f) +
                fill.coerceIn(0.2f, 0.95f) * 0.38f +
                (verts.coerceAtMost(96) / 96f) * 0.12f +
                det.coerceIn(0f, 1f) * 0.28f
            ).coerceIn(0f, 1f)
        return ShapeQuality(
            score = quality,
            polygonVertices = verts,
            fillRatio = fill,
            isMaskContour = maskLike,
        )
    }

    fun isPublishable(quality: ShapeQuality, source: VisionSource): Boolean {
        if (quality.polygonVertices < 3) return false
        return when (source) {
            VisionSource.Object -> quality.isMaskContour && quality.polygonVertices >= 6 && quality.fillRatio >= 0.28f
            VisionSource.Person -> quality.isMaskContour && quality.polygonVertices >= 6 && quality.fillRatio >= 0.22f
            VisionSource.Face -> quality.polygonVertices >= 8 && quality.fillRatio >= 0.35f
            else -> quality.score >= 0.35f
        }
    }

    private fun shoelaceNorm(poly: List<Pair<Float, Float>>): Float {
        if (poly.size < 3) return 0f
        var sum = 0f
        for (i in poly.indices) {
            val (x0, y0) = poly[i]
            val (x1, y1) = poly[(i + 1) % poly.size]
            sum += x0 * y1 - x1 * y0
        }
        return abs(sum) * 0.5f
    }
}
