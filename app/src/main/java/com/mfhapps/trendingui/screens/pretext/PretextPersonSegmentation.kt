package com.mfhapps.trendingui.screens.pretext

import androidx.camera.core.CameraSelector
import kotlin.math.max
import kotlin.math.min

internal object PretextPersonSegmentation {

    data class MaskPlane(val data: FloatArray, val width: Int, val height: Int)

    private const val MASK_SAMPLE_STRIDE = 8
    private const val MASK_PIXEL_THRESH = 0.28f
    private const val MASK_TIGHT_BLOB_THRESH = 0.48f
    private const val MAX_FRAME_FILL_NORM = 0.82f
    private const val MAX_CLAMP_NORM_AREA = 0.68f
    private const val MIN_BLOB_NORM = 0.012f

    data class MaskStats(
        val fgRatio: Float,
        val centerFgRatio: Float,
        val maxActivation: Float,
        val blobNormArea: Float,
    )

    fun analyzeMask(mask: MaskPlane): MaskStats {
        val w = mask.width
        val h = mask.height
        val cx0 = w / 4
        val cx1 = w * 3 / 4
        val cy0 = h / 4
        val cy1 = h * 3 / 4
        var samples = 0
        var foreground = 0
        var centerSamples = 0
        var centerFg = 0
        var maxAct = 0f
        var minX = w
        var minY = h
        var maxX = 0
        var maxY = 0
        var blobPixels = 0

        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val v = mask.data[y * w + x]
                if (v > maxAct) maxAct = v
                if (v >= MASK_PIXEL_THRESH) {
                    foreground++
                    minX = min(minX, x)
                    maxX = max(maxX, x)
                    minY = min(minY, y)
                    maxY = max(maxY, y)
                    blobPixels++
                    if (x in cx0..cx1 && y in cy0..cy1) centerFg++
                }
                samples++
                if (x in cx0..cx1 && y in cy0..cy1) centerSamples++
                x += MASK_SAMPLE_STRIDE
            }
            y += MASK_SAMPLE_STRIDE
        }

        val fgRatio = if (samples > 0) foreground.toFloat() / samples else 0f
        val centerFgRatio = if (centerSamples > 0) centerFg.toFloat() / centerSamples else 0f
        val blobNormArea = if (blobPixels > 0 && maxX >= minX && maxY >= minY) {
            val bw = (maxX - minX + 1).toFloat() / w
            val bh = (maxY - minY + 1).toFloat() / h
            (bw * bh).coerceIn(0f, 1f)
        } else {
            0f
        }
        return MaskStats(fgRatio, centerFgRatio, maxAct, blobNormArea)
    }

    fun minForegroundRatio(lensFacing: Int): Float = when (lensFacing) {
        CameraSelector.LENS_FACING_FRONT -> 0.010f
        else -> 0.005f
    }

    fun passesForegroundGate(stats: MaskStats, lensFacing: Int): Boolean {
        val minFg = minForegroundRatio(lensFacing)
        val effective = max(stats.fgRatio, stats.centerFgRatio * 1.15f)
        if (effective < minFg) return false
        val minPeak = if (lensFacing == CameraSelector.LENS_FACING_BACK) 0.32f else 0.42f
        if (stats.maxActivation < minPeak) return false
        return true
    }

    fun passesFaceForegroundGate(stats: MaskStats, lensFacing: Int): Boolean {
        if (passesForegroundGate(stats, lensFacing)) return true
        if (stats.blobNormArea < MIN_BLOB_NORM) return false
        val minFg = minForegroundRatio(lensFacing) * 0.45f
        val minPeak = if (lensFacing == CameraSelector.LENS_FACING_BACK) 0.24f else 0.30f
        return stats.centerFgRatio >= minFg && stats.maxActivation >= minPeak
    }

    fun isFrameFillBlob(stats: MaskStats): Boolean =
        stats.blobNormArea > MAX_FRAME_FILL_NORM

    fun isBlobTooSmall(stats: MaskStats): Boolean =
        stats.blobNormArea < MIN_BLOB_NORM

    fun contourFromMaskBlob(
        mask: MaskPlane,
        frame: FrameMetrics,
        pixelThresh: Float = MASK_PIXEL_THRESH,
    ): VisionContour? {
        val w = mask.width
        val h = mask.height
        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (mask.data[y * w + x] >= pixelThresh) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < minX || maxY < minY) return null

        val aw = frame.analysisWidth.toFloat().coerceAtLeast(1f)
        val ah = frame.analysisHeight.toFloat().coerceAtLeast(1f)
        val left = (minX / w.toFloat()).coerceIn(0f, 1f)
        val top = (minY / h.toFloat()).coerceIn(0f, 1f)
        val right = ((maxX + 1) / w.toFloat()).coerceIn(0f, 1f)
        val bottom = ((maxY + 1) / h.toFloat()).coerceIn(0f, 1f)
        if ((right - left) * (bottom - top) < MIN_BLOB_NORM) return null

        val poly = listOf(
            left to top,
            right to top,
            right to bottom,
            left to bottom,
        )
        return VisionContour(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            polygonNorm = poly,
            label = "person",
            source = VisionSource.Person,
            frame = frame,
        )
    }

    fun contourFromMaskBlobTight(mask: MaskPlane, frame: FrameMetrics): VisionContour? =
        contourFromMaskBlob(mask, frame, MASK_TIGHT_BLOB_THRESH)

    private const val HEAD_BAND_FRACTION = 0.34f
    private const val HEAD_WIDTH_OF_BODY = 0.72f
    private const val FACE_TARGET_ASPECT = 0.82f
    private const val MAX_FACE_NORM_AREA = 0.12f
    private const val MIN_FACE_NORM_AREA = 0.012f

    fun refinedFaceNormBox(plane: MaskPlane): FloatArray? {
        val w = plane.width
        val h = plane.height
        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (plane.data[y * w + x] >= MASK_PIXEL_THRESH) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < minX || maxY < minY) return null

        val blobW = (maxX - minX + 1).toFloat()
        val blobH = (maxY - minY + 1).toFloat()
        if (blobW < 8f || blobH < 12f) return null

        var headH = blobH * HEAD_BAND_FRACTION
        var headW = blobW * HEAD_WIDTH_OF_BODY
        val ar = headW / headH.coerceAtLeast(1f)
        if (ar > FACE_TARGET_ASPECT * 1.2f) {
            headW = headH * FACE_TARGET_ASPECT
        } else if (ar < FACE_TARGET_ASPECT / 1.2f) {
            headH = headW / FACE_TARGET_ASPECT
        }

        val cx = (minX + maxX + 1) * 0.5f
        val topPx = minY.toFloat() + blobH * 0.02f
        val leftPx = (cx - headW * 0.5f).coerceIn(minX.toFloat(), maxX + 1f)
        val rightPx = (cx + headW * 0.5f).coerceIn(minX.toFloat(), maxX + 1f)
        val bottomPx = (topPx + headH).coerceAtMost(minY + blobH * 0.42f)

        val left = (leftPx / w).coerceIn(0f, 1f)
        val top = (topPx / h).coerceIn(0f, 1f)
        val right = (rightPx / w).coerceIn(0f, 1f)
        val bottom = (bottomPx / h).coerceIn(0f, 1f)
        if ((right - left) * (bottom - top) < MIN_FACE_NORM_AREA) return null
        return floatArrayOf(left, top, right, bottom)
    }

    fun isPlausibleFaceNorm(box: FloatArray): Boolean {
        if (box.size < 4) return false
        val bw = box[2] - box[0]
        val bh = box[3] - box[1]
        if (bw <= 0f || bh <= 0f) return false
        val area = bw * bh
        if (area !in MIN_FACE_NORM_AREA..MAX_FACE_NORM_AREA) return false
        val ar = bw / bh
        if (ar !in 0.55f..1.35f) return false
        val cx = (box[0] + box[2]) * 0.5f
        val cy = (box[1] + box[3]) * 0.5f
        if (cx !in 0.12f..0.88f || cy !in 0.08f..0.82f) return false
        return true
    }

    fun cropMaskToHeadBand(plane: MaskPlane, headFraction: Float = HEAD_BAND_FRACTION): MaskPlane? {
        val w = plane.width
        val h = plane.height
        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (plane.data[y * w + x] >= MASK_PIXEL_THRESH) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < minX || maxY < minY) return null
        val blobH = maxY - minY + 1
        val headBottom = minY + (blobH * headFraction).toInt().coerceIn(1, blobH) - 1
        val cropW = maxX - minX + 1
        val cropH = headBottom - minY + 1
        if (cropW < 4 || cropH < 4) return null
        val out = FloatArray(cropW * cropH)
        for (row in 0 until cropH) {
            val srcRow = (minY + row) * w + minX
            val dstRow = row * cropW
            System.arraycopy(plane.data, srcRow, out, dstRow, cropW)
        }
        return MaskPlane(out, cropW, cropH)
    }

    fun headBlobNormBox(plane: MaskPlane, headFraction: Float = HEAD_BAND_FRACTION): FloatArray? {
        val w = plane.width
        val h = plane.height
        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (plane.data[y * w + x] >= MASK_TIGHT_BLOB_THRESH) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < minX || maxY < minY) return null
        val blobH = maxY - minY + 1
        val headBottom = minY + (blobH * headFraction).toInt().coerceIn(1, blobH) - 1
        var headMinX = w
        var headMaxX = -1
        for (y in minY..headBottom) {
            for (x in minX..maxX) {
                if (plane.data[y * w + x] >= MASK_TIGHT_BLOB_THRESH) {
                    if (x < headMinX) headMinX = x
                    if (x > headMaxX) headMaxX = x
                }
            }
        }
        if (headMaxX < headMinX) return null
        val left = headMinX / w.toFloat()
        val top = minY / h.toFloat()
        val right = (headMaxX + 1) / w.toFloat()
        val bottom = (headBottom + 1) / h.toFloat()
        if ((right - left) * (bottom - top) < MIN_BLOB_NORM) return null
        return floatArrayOf(left, top, right, bottom)
    }

    fun clampFaceContour(contour: VisionContour): VisionContour {
        val norm = contour.boundsRectNorm()
        val area = norm.width() * norm.height()
        if (area <= MAX_FACE_NORM_AREA) return contour
        val cx = (norm.left + norm.right) * 0.5f
        val cy = (norm.top + norm.bottom) * 0.5f
        val side = kotlin.math.sqrt(MAX_FACE_NORM_AREA * 0.85f)
        val halfW = side * 0.45f
        val halfH = side * 0.55f
        return contour.copy(
            left = (cx - halfW).coerceIn(0f, 1f),
            top = (cy - halfH).coerceIn(0f, 1f),
            right = (cx + halfW).coerceIn(0f, 1f),
            bottom = (cy + halfH).coerceIn(0f, 1f),
            polygonNorm = contour.polygonNorm,
        )
    }

    fun clampContourToReasonableSize(contour: VisionContour): VisionContour {
        val norm = contour.boundsRectNorm()
        val area = norm.width() * norm.height()
        if (area <= MAX_CLAMP_NORM_AREA) return contour
        val cx = (norm.left + norm.right) * 0.5f
        val cy = (norm.top + norm.bottom) * 0.5f
        val side = kotlin.math.sqrt(MAX_CLAMP_NORM_AREA * 0.85f)
        val halfW = side * 0.45f
        val halfH = side * 0.55f
        return contour.copy(
            left = (cx - halfW).coerceIn(0f, 1f),
            top = (cy - halfH).coerceIn(0f, 1f),
            right = (cx + halfW).coerceIn(0f, 1f),
            bottom = (cy + halfH).coerceIn(0f, 1f),
            polygonNorm = contour.polygonNorm,
        )
    }
}
