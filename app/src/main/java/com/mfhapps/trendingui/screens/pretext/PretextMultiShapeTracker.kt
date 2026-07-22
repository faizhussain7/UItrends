package com.mfhapps.trendingui.screens.pretext

import android.graphics.RectF
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class PretextMultiShapeTracker(
    private val maxSlots: Int = PretextVisionLimits.MAX_TRACKED_SLOTS,
    private val maxConsecutiveMisses: Int = 24,
    private val highScoreThreshold: Float = 0.25f,
    private val lowScoreThreshold: Float = 0.12f,
    private val matchIouThreshold: Float = 0.22f,
    private val softMatchIouThreshold: Float = 0.10f,
    private val viewSmoothAlpha: Float = 0.46f,
    private val faceViewSmoothAlpha: Float = 0.50f,
    private val personSmoothAlpha: Float = 0.48f,
    private val objectSmoothAlpha: Float = 0.44f,
) {
    data class Detection(
        val contour: VisionContour,
        val shape: ViewShape,
        val score: Float?,
    )

    private data class Slot(
        val id: Int,
        var shape: ViewShape,
        var missCount: Int = 0,
        var lastScore: Float = 0f,
        val labelHistory: ArrayDeque<String> = ArrayDeque(LABEL_HISTORY),
    )

    companion object {
        private const val LABEL_HISTORY = 6
    }

    private val slots = ArrayList<Slot>()
    private var nextId = 1

    val trackedShapes: List<ViewShape>
        get() = slots.map { it.shape }

    val trackingState: ShapeTrackingState
        get() = when {
            slots.isEmpty() -> ShapeTrackingState.Lost
            slots.all { it.missCount == 0 } -> ShapeTrackingState.Live
            slots.any { it.missCount <= maxConsecutiveMisses } -> ShapeTrackingState.Held
            else -> ShapeTrackingState.Lost
        }

    fun update(detections: List<Detection>): List<ViewShape> {
        if (detections.isEmpty()) {
            markAllMissed()
            pruneLost()
            return trackedShapes
        }

        val high = detections.filter { (it.score ?: 0f) >= highScoreThreshold }.toMutableList()
        val low = detections.filter {
            val s = it.score ?: 0f
            s >= lowScoreThreshold && s < highScoreThreshold
        }.toMutableList()
        val matchedSlots = HashSet<Int>()

        for (slot in slots) {
            val match = findBestMatch(slot, high, matchIouThreshold)
            if (match >= 0) {
                applyMatch(slot, high.removeAt(match), matchedSlots)
            }
        }
        for (slot in slots) {
            if (slot.id in matchedSlots) continue
            val match = findBestMatch(slot, low, softMatchIouThreshold)
            if (match >= 0) {
                applyMatch(slot, low.removeAt(match), matchedSlots)
            }
        }

        val remaining = (high + low).sortedByDescending { it.score ?: 0f }
        for (det in remaining) {
            if (slots.size >= maxSlots) break
            val id = nextId++
            val rawLabel = det.shape.label?.trim().orEmpty().ifEmpty { "object" }
            val history = ArrayDeque<String>(LABEL_HISTORY)
            history.addLast(rawLabel)
            slots += Slot(
                id = id,
                shape = det.shape.copy(instanceId = id, isLiveDetection = true, label = rawLabel),
                lastScore = det.score ?: 0f,
                labelHistory = history,
            )
            matchedSlots += id
        }

        for (slot in slots) {
            if (slot.id !in matchedSlots) {
                slot.missCount++
                if (slot.missCount <= maxConsecutiveMisses) {
                    slot.shape = slot.shape.copy(isLiveDetection = false)
                }
            }
        }
        pruneLost()
        return trackedShapes
    }

    fun clear() {
        slots.clear()
        nextId = 1
    }

    private fun findBestMatch(
        slot: Slot,
        detections: List<Detection>,
        iouThreshold: Float,
    ): Int {
        var bestIdx = -1
        var bestScore = iouThreshold
        for (i in detections.indices) {
            val det = detections[i]
            if (det.contour.source != slot.shape.source) continue
            val iou = iouPx(slot.shape.boundsPx, det.shape.boundsPx)
            val centerDist = centerDistanceNorm(slot.shape.boundsPx, det.shape.boundsPx)
            val detScore = det.score ?: 0f
            val combined = when (det.contour.source) {
                VisionSource.Object -> {
                    val distScore = (1f - centerDist.coerceIn(0f, 1f)) * 0.35f
                    iou * 0.50f + distScore + detScore * 0.15f
                }
                else -> iou * 0.72f + detScore * 0.28f
            }
            if (combined > bestScore) {
                bestScore = combined
                bestIdx = i
            }
        }
        return bestIdx
    }

    private fun applyMatch(
        slot: Slot,
        detection: Detection,
        matchedSlots: HashSet<Int>,
    ) {
        val iou = iouPx(slot.shape.boundsPx, detection.shape.boundsPx)
        var alpha = smoothAlphaFor(detection.contour.source)
        val drift = centerDistanceNorm(slot.shape.boundsPx, detection.shape.boundsPx)
        if (drift > 0.12f) {
            alpha = (alpha + 0.10f).coerceAtMost(0.62f)
        } else if (iou > 0.55f) {
            alpha = (alpha * 0.88f).coerceAtLeast(0.28f)
        }
        slot.shape = smoothToward(slot.shape, detection.shape, alpha).copy(
            instanceId = slot.id,
            isLiveDetection = true,
            label = stabilizedLabel(slot, detection.shape.label),
        )
        slot.missCount = 0
        slot.lastScore = detection.score ?: slot.lastScore
        matchedSlots += slot.id
    }

    private fun markAllMissed() {
        for (slot in slots) {
            slot.missCount++
            if (slot.missCount <= maxConsecutiveMisses) {
                slot.shape = slot.shape.copy(isLiveDetection = false)
            }
        }
    }

    private fun pruneLost() {
        slots.removeAll { it.missCount > maxConsecutiveMisses }
    }

    private fun smoothAlphaFor(source: VisionSource): Float = when (source) {
        VisionSource.Face -> faceViewSmoothAlpha
        VisionSource.Person -> personSmoothAlpha
        VisionSource.Object -> objectSmoothAlpha
        else -> viewSmoothAlpha
    }

    private fun stabilizedLabel(slot: Slot, candidate: String?): String {
        val raw = candidate?.trim().orEmpty().ifEmpty { "object" }
        if (slot.shape.source != VisionSource.Object) return raw
        slot.labelHistory.addLast(raw)
        while (slot.labelHistory.size > LABEL_HISTORY) {
            slot.labelHistory.removeFirst()
        }
        return slot.labelHistory
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: raw
    }

    private fun smoothToward(prev: ViewShape, next: ViewShape, alpha: Float): ViewShape {
        val t = alpha.coerceIn(0.22f, 0.62f)
        val inv = 1f - t
        val b0 = prev.boundsPx
        val b1 = next.boundsPx
        val bounds = RectF(
            b0.left * inv + b1.left * t,
            b0.top * inv + b1.top * t,
            b0.right * inv + b1.right * t,
            b0.bottom * inv + b1.bottom * t,
        )
        return next.copy(
            boundsPx = bounds,
            polygonPx = smoothPolygon(prev.polygonPx, next.polygonPx, t),
        )
    }

    private fun smoothPolygon(
        prev: com.mfhapps.trendingui.core.text.PolygonObstacle?,
        next: com.mfhapps.trendingui.core.text.PolygonObstacle?,
        t: Float,
    ): com.mfhapps.trendingui.core.text.PolygonObstacle? {
        if (next == null) return prev
        if (prev == null) return next
        val p0 = prev.points
        val p1 = next.points
        if (p0.size != p1.size || p0.size < 3) return next
        val inv = 1f - t
        val out = ArrayList<Pair<Float, Float>>(p0.size)
        for (i in p0.indices) {
            val (x0, y0) = p0[i]
            val (x1, y1) = p1[i]
            out += (x0 * inv + x1 * t) to (y0 * inv + y1 * t)
        }
        return com.mfhapps.trendingui.core.text.PolygonObstacle(out)
    }

    private fun iouPx(a: RectF, b: RectF): Float {
        val ix0 = max(a.left, b.left)
        val iy0 = max(a.top, b.top)
        val ix1 = min(a.right, b.right)
        val iy1 = min(a.bottom, b.bottom)
        val inter = max(0f, ix1 - ix0) * max(0f, iy1 - iy0)
        val union = a.width() * a.height() + b.width() * b.height() - inter
        return if (union > 1e-6f) inter / union else 0f
    }

    private fun centerDistanceNorm(a: RectF, b: RectF): Float {
        val diag = hypot(a.width(), a.height()).coerceAtLeast(1f)
        val dx = a.centerX() - b.centerX()
        val dy = a.centerY() - b.centerY()
        return hypot(dx, dy) / diag
    }
}
