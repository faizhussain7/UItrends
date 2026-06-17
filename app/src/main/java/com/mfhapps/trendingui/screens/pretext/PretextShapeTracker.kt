package com.mfhapps.trendingui.screens.pretext

import android.graphics.RectF
import com.mfhapps.trendingui.core.text.PolygonObstacle
import com.mfhapps.trendingui.native.PretextNativeGeometry

class PretextShapeTracker(
    private val maxConsecutiveMisses: Int = 20,
    private val viewSmoothAlpha: Float = 0.52f,
    private val faceViewSmoothAlpha: Float = 0.65f,
) {
    private var publishedShape: ViewShape? = null
    private var missCount = 0
    private var weakMissStreak = 0
    private var lastSource: VisionSource? = null

    val currentShape: ViewShape?
        get() = publishedShape

    val consecutiveMisses: Int
        get() = missCount

    val trackingState: ShapeTrackingState
        get() = when {
            missCount == 0 -> ShapeTrackingState.Live
            missCount <= maxConsecutiveMisses -> ShapeTrackingState.Held
            else -> ShapeTrackingState.Lost
        }

    fun onDetection(contour: VisionContour, viewShape: ViewShape) {
        lastSource = contour.source
        val smooth = smoothAlphaFor(contour.source)
        val previous = publishedShape
        publishedShape = if (previous != null && shouldRejectBoxDowngrade(previous, viewShape)) {
            previous
        } else {
            previous?.let { smoothToward(it, viewShape, smooth) } ?: viewShape
        }
        missCount = 0
        weakMissStreak = 0
    }

    private fun shouldRejectBoxDowngrade(previous: ViewShape, next: ViewShape): Boolean {
        val prevVerts = previous.polygonPx?.points?.size ?: 0
        val nextVerts = next.polygonPx?.points?.size ?: 0
        return prevVerts > 4 && nextVerts <= 4
    }

    fun onMiss(): ViewShape? {
        weakMissStreak = 0
        missCount++
        return heldOrClear()
    }

    fun onWeakMiss(): ViewShape? {
        weakMissStreak++
        if (weakMissStreak % 3 == 0) {
            missCount++
        }
        return heldOrClear()
    }

    private fun heldOrClear(): ViewShape? =
        if (missCount <= maxConsecutiveMisses) {
            publishedShape?.copy(isLiveDetection = false)
        } else {
            publishedShape = null
            null
        }

    fun clear() {
        publishedShape = null
        missCount = 0
        weakMissStreak = 0
        lastSource = null
    }

    fun smoothAlphaFor(source: VisionSource): Float =
        if (source == VisionSource.Face) faceViewSmoothAlpha else viewSmoothAlpha

    private fun smoothToward(prev: ViewShape, next: ViewShape, alpha: Float): ViewShape {
        val t = alpha.coerceIn(0.12f, 0.85f)
        val inv = 1f - t
        val b0 = prev.boundsPx
        val b1 = next.boundsPx
        val bounds = RectF(
            b0.left * inv + b1.left * t,
            b0.top * inv + b1.top * t,
            b0.right * inv + b1.right * t,
            b0.bottom * inv + b1.bottom * t,
        )
        val polygon = smoothPolygon(prev.polygonPx, next.polygonPx, t)
        return next.copy(boundsPx = bounds, polygonPx = polygon)
    }

    private fun smoothPolygon(
        prev: PolygonObstacle?,
        next: PolygonObstacle?,
        t: Float,
    ): PolygonObstacle? {
        if (next == null) return prev
        if (prev == null) return next
        val p0 = prev.points
        val p1 = next.points
        if (p1.size <= 4 && p0.size > 4) return prev
        if (p0.size != p1.size || p0.size < 3) return next
        val inv = 1f - t
        val out = ArrayList<Pair<Float, Float>>(p0.size)
        for (i in p0.indices) {
            val (x0, y0) = p0[i]
            val (x1, y1) = p1[i]
            out += (x0 * inv + x1 * t) to (y0 * inv + y1 * t)
        }
        return PolygonObstacle(out)
    }
}
