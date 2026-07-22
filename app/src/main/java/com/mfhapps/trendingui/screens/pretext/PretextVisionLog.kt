package com.mfhapps.trendingui.screens.pretext

import android.graphics.RectF
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

internal object PretextVisionLog {

    private var lastNormRect: RectF? = null

    fun analyzeContour(contour: VisionContour?, detScore: Float? = null): VisionAccuracySnapshot? {
        if (contour == null) return null
        val norm = contour.boundsRectNorm()
        val area = (norm.width() * norm.height()).coerceIn(0f, 1f)
        val quality = PretextShapeAnalyzer.analyze(contour, detScore)
        val iou = lastNormRect?.let { iouNorm(it, norm) }
        val drift = lastNormRect?.let { centerDriftNorm(it, norm) }
        lastNormRect = RectF(norm)
        return VisionAccuracySnapshot(
            tracking = ShapeTrackingState.Live,
            backend = null,
            score = detScore,
            normBBoxArea = area,
            viewBBoxAreaRatio = 0f,
            polygonVertices = quality.polygonVertices,
            shapeQuality = quality.score,
            polygonFillRatio = quality.fillRatio,
            boundsFullyInView = true,
            iouVsPrevious = iou,
            centerDriftNorm = drift,
            detectMs = 0L,
            mapMs = 0L,
        )
    }

    fun analyzeMappedShape(
        contour: VisionContour,
        shape: ViewShape,
        layout: PretextViewportMapper.ViewLayout,
        base: VisionAccuracySnapshot,
        mapMs: Long,
    ): VisionAccuracySnapshot {
        val b = shape.boundsPx
        val viewArea = layout.viewWidth * layout.viewHeight
        val shapeArea = (b.width() * b.height()).coerceAtLeast(0f)
        val ratio = if (viewArea > 0f) shapeArea / viewArea else 0f
        val inView = b.left >= -4f && b.top >= -4f &&
            b.right <= layout.viewWidth + 4f &&
            b.bottom <= layout.viewHeight + 4f
        return base.copy(
            viewBBoxAreaRatio = ratio.coerceIn(0f, 1f),
            boundsFullyInView = inView,
            mapMs = mapMs,
        )
    }

    fun resetSession() {
        lastNormRect = null
    }

    private fun iouNorm(a: RectF, b: RectF): Float {
        val il = max(a.left, b.left)
        val it = max(a.top, b.top)
        val ir = min(a.right, b.right)
        val ib = min(a.bottom, b.bottom)
        val iw = (ir - il).coerceAtLeast(0f)
        val ih = (ib - it).coerceAtLeast(0f)
        val inter = iw * ih
        val union = a.width() * a.height() + b.width() * b.height() - inter
        return if (union > 1e-6f) inter / union else 0f
    }

    private fun centerDriftNorm(a: RectF, b: RectF): Float {
        val ax = (a.left + a.right) * 0.5f
        val ay = (a.top + a.bottom) * 0.5f
        val bx = (b.left + b.right) * 0.5f
        val by = (b.top + b.bottom) * 0.5f
        return hypot(ax - bx, ay - by)
    }
}
