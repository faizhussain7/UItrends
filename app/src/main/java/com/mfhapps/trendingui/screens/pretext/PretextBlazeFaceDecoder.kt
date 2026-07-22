package com.mfhapps.trendingui.screens.pretext

import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal object PretextBlazeFaceDecoder {

    private const val INPUT = 128
    private const val NUM_ANCHORS = 896
    private const val RAW_DIM = 16
    private const val MIN_SCORE = 0.45f
    private const val RAW_SCORE_LIMIT = 80f
    private const val NMS_IOU = 0.3f
    private const val MAX_CANDIDATE_NORM_AREA = 0.48f
    private const val MIN_CANDIDATE_NORM_AREA = 0.0015f
    private const val TARGET_FACE_NORM_AREA = 0.15f
    private const val MAX_PUBLISH_NORM_AREA = 0.20f
    private const val TARGET_FACE_ASPECT = 0.82f

    data class Detection(val box: FloatArray, val score: Float)

    private data class Letterbox(
        val scale: Float,
        val padX: Float,
        val padY: Float,
        val contentW: Float,
        val contentH: Float,
    )

    private val anchors: Array<FloatArray> by lazy { generateAnchors() }

    fun detectWithScoreInNormRoi(
        rgb: ByteArray,
        imageW: Int,
        imageH: Int,
        roiNorm: FloatArray,
        interpreter: Interpreter,
    ): Detection? {
        if (roiNorm.size < 4) return null
        val x0 = (roiNorm[0] * imageW).toInt().coerceIn(0, imageW - 2)
        val y0 = (roiNorm[1] * imageH).toInt().coerceIn(0, imageH - 2)
        val x1 = (roiNorm[2] * imageW).toInt().coerceIn(x0 + 2, imageW)
        val y1 = (roiNorm[3] * imageH).toInt().coerceIn(y0 + 2, imageH)
        val cropW = x1 - x0
        val cropH = y1 - y0
        val crop = ByteArray(cropW * cropH * 3)
        val stride = imageW * 3
        for (row in 0 until cropH) {
            val src = (y0 + row) * stride + x0 * 3
            val dst = row * cropW * 3
            System.arraycopy(rgb, src, crop, dst, cropW * 3)
        }
        val local = detectWithScore(crop, cropW, cropH, interpreter) ?: return null
        val boxPx = floatArrayOf(
            local.box[0] + x0,
            local.box[1] + y0,
            local.box[2] + x0,
            local.box[3] + y0,
        )
        return Detection(
            box = refineFaceBox(boxPx, imageW, imageH),
            score = local.score,
        )
    }

    fun detectWithScore(rgb: ByteArray, imageW: Int, imageH: Int, interpreter: Interpreter): Detection? =
        detectMulti(rgb, imageW, imageH, interpreter, maxInstances = 1).firstOrNull()

    fun detectMulti(
        rgb: ByteArray,
        imageW: Int,
        imageH: Int,
        interpreter: Interpreter,
        maxInstances: Int,
    ): List<Detection> {
        if (imageW < 2 || imageH < 2 || maxInstances <= 0) return emptyList()
        val letterbox = computeLetterbox(imageW, imageH)
        val input = ByteBuffer.allocateDirect(INPUT * INPUT * 3 * 4).order(ByteOrder.nativeOrder())
        fillInputLetterbox(rgb, imageW, imageH, letterbox, input)

        val regressors = Array(1) { Array(NUM_ANCHORS) { FloatArray(RAW_DIM) } }
        val scores = Array(1) { Array(NUM_ANCHORS) { FloatArray(1) } }
        val outputMap = linkedMapOf<Int, Any>()
        var regIdx = -1
        var scoreIdx = -1
        for (i in 0 until interpreter.outputTensorCount) {
            val shape = interpreter.getOutputTensor(i).shape()
            val last = shape.lastOrNull() ?: 0
            val name = interpreter.getOutputTensor(i).name()?.lowercase() ?: ""
            when {
                last == RAW_DIM || name.contains("regress") -> regIdx = i
                last == 1 || name.contains("score") || name.contains("class") -> scoreIdx = i
            }
        }
        if (regIdx < 0) regIdx = 0
        if (scoreIdx < 0) scoreIdx = if (regIdx == 0) 1 else 0
        outputMap[regIdx] = regressors
        outputMap[scoreIdx] = scores
        interpreter.runForMultipleInputsOutputs(arrayOf(input), outputMap)

        val decoded = decodeBoxes(regressors[0])
        val candidates = ArrayList<Pair<FloatArray, Float>>(NUM_ANCHORS / 4)
        for (i in 0 until NUM_ANCHORS) {
            val score = sigmoid(scores[0][i][0].coerceIn(-RAW_SCORE_LIMIT, RAW_SCORE_LIMIT))
            if (score < MIN_SCORE) continue
            val box = mapBoxFromLetterbox(decoded[i], letterbox, imageW, imageH)
            if (!isFaceLikeBox(box)) continue
            candidates += box to score
        }
        if (candidates.isEmpty()) return emptyList()

        return nonMaxSuppression(candidates)
            .sortedByDescending { it.second }
            .take(maxInstances.coerceAtLeast(1))
            .map { (normBox, score) ->
                val refined = refineFaceBox(
                    floatArrayOf(
                        normBox[0] * imageW,
                        normBox[1] * imageH,
                        normBox[2] * imageW,
                        normBox[3] * imageH,
                    ),
                    imageW,
                    imageH,
                )
                Detection(box = refined, score = score)
            }
    }

    fun refineFaceBox(boxPx: FloatArray, imageW: Int, imageH: Int): FloatArray {
        val w = imageW.toFloat().coerceAtLeast(1f)
        val h = imageH.toFloat().coerceAtLeast(1f)
        var left = (boxPx[0] / w).coerceIn(0f, 1f)
        var top = (boxPx[1] / h).coerceIn(0f, 1f)
        var right = (boxPx[2] / w).coerceIn(0f, 1f)
        var bottom = (boxPx[3] / h).coerceIn(0f, 1f)
        if (right <= left || bottom <= top) return boxPx

        var bw = right - left
        var bh = bottom - top
        var cx = (left + right) * 0.5f
        var cy = (top + bottom) * 0.5f
        cy += bh * 0.04f

        val ar = bw / bh.coerceAtLeast(1e-4f)
        if (ar > TARGET_FACE_ASPECT * 1.25f || ar < TARGET_FACE_ASPECT / 1.25f) {
            val area = bw * bh
            bh = sqrt(area / TARGET_FACE_ASPECT)
            bw = bh * TARGET_FACE_ASPECT
        }

        val area = bw * bh
        when {
            area > MAX_PUBLISH_NORM_AREA -> {
                val scale = sqrt(MAX_PUBLISH_NORM_AREA / area)
                bw *= scale
                bh *= scale
            }
            area > TARGET_FACE_NORM_AREA -> {
                val scale = sqrt(TARGET_FACE_NORM_AREA / area)
                bw *= scale
                bh *= scale
            }
        }

        left = (cx - bw * 0.5f).coerceIn(0f, 1f - bw)
        top = (cy - bh * 0.5f).coerceIn(0f, 1f - bh)
        right = (left + bw).coerceAtMost(1f)
        bottom = (top + bh).coerceAtMost(1f)
        return floatArrayOf(left * w, top * h, right * w, bottom * h)
    }

    private fun computeLetterbox(imageW: Int, imageH: Int): Letterbox {
        val scale = min(INPUT.toFloat() / imageW, INPUT.toFloat() / imageH)
        val contentW = imageW * scale
        val contentH = imageH * scale
        val padX = (INPUT - contentW) * 0.5f
        val padY = (INPUT - contentH) * 0.5f
        return Letterbox(scale, padX, padY, contentW, contentH)
    }

    private fun fillInputLetterbox(
        rgb: ByteArray,
        imageW: Int,
        imageH: Int,
        letterbox: Letterbox,
        input: ByteBuffer,
    ) {
        val stride = imageW * 3
        for (y in 0 until INPUT) {
            val srcY = (y - letterbox.padY) / letterbox.scale
            val sy = srcY.toInt().coerceIn(0, imageH - 1)
            val row = sy * stride
            for (x in 0 until INPUT) {
                val srcX = (x - letterbox.padX) / letterbox.scale
                val inBounds = srcX >= 0f && srcX < imageW && srcY >= 0f && srcY < imageH
                if (inBounds) {
                    val sx = srcX.toInt().coerceIn(0, imageW - 1)
                    val i = row + sx * 3
                    input.putFloat((rgb[i].toInt() and 0xff) / 127.5f - 1f)
                    input.putFloat((rgb[i + 1].toInt() and 0xff) / 127.5f - 1f)
                    input.putFloat((rgb[i + 2].toInt() and 0xff) / 127.5f - 1f)
                } else {
                    input.putFloat(-1f)
                    input.putFloat(-1f)
                    input.putFloat(-1f)
                }
            }
        }
        input.rewind()
    }

    private fun mapBoxFromLetterbox(
        normLetterbox: FloatArray,
        letterbox: Letterbox,
        imageW: Int,
        imageH: Int,
    ): FloatArray {
        fun mapX(nx: Float): Float {
            val px = nx * INPUT
            return ((px - letterbox.padX) / letterbox.scale / imageW).coerceIn(0f, 1f)
        }

        fun mapY(ny: Float): Float {
            val py = ny * INPUT
            return ((py - letterbox.padY) / letterbox.scale / imageH).coerceIn(0f, 1f)
        }

        val left = min(mapX(normLetterbox[0]), mapX(normLetterbox[2]))
        val right = max(mapX(normLetterbox[0]), mapX(normLetterbox[2]))
        val top = min(mapY(normLetterbox[1]), mapY(normLetterbox[3]))
        val bottom = max(mapY(normLetterbox[1]), mapY(normLetterbox[3]))
        return floatArrayOf(left, top, right, bottom)
    }

    private fun isFaceLikeBox(box: FloatArray): Boolean {
        if (box[2] <= box[0] || box[3] <= box[1]) return false
        val bw = box[2] - box[0]
        val bh = box[3] - box[1]
        val area = bw * bh
        if (area < MIN_CANDIDATE_NORM_AREA || area > MAX_CANDIDATE_NORM_AREA) return false
        val ar = bw / bh.coerceAtLeast(1e-4f)
        return ar in 0.45f..1.65f
    }

    fun detect(rgb: ByteArray, imageW: Int, imageH: Int, interpreter: Interpreter): FloatArray? =
        detectWithScore(rgb, imageW, imageH, interpreter)?.box

    private fun decodeBoxes(raw: Array<FloatArray>): Array<FloatArray> {
        val scale = INPUT.toFloat()
        val out = Array(NUM_ANCHORS) { FloatArray(4) }
        for (i in 0 until NUM_ANCHORS) {
            val reg = raw[i]
            val ax = anchors[i][0]
            val ay = anchors[i][1]

            val cx = reg[0] / scale + ax
            val cy = reg[1] / scale + ay
            val sizeX = reg[2] / scale
            val sizeY = reg[3] / scale
            val halfW = sizeX * 0.5f
            val halfH = sizeY * 0.5f
            out[i] = floatArrayOf(
                (cx - halfW).coerceIn(0f, 1f),
                (cy - halfH).coerceIn(0f, 1f),
                (cx + halfW).coerceIn(0f, 1f),
                (cy + halfH).coerceIn(0f, 1f),
            )
        }
        return out
    }

    private fun generateAnchors(): Array<FloatArray> {
        val strides = intArrayOf(8, 16, 16, 16)
        val anchors = ArrayList<FloatArray>(NUM_ANCHORS)
        var layerId = 0
        while (layerId < strides.size) {
            var lastSameStride = layerId
            var repeats = 0
            while (lastSameStride < strides.size && strides[lastSameStride] == strides[layerId]) {
                lastSameStride++
                repeats += 2
            }
            val stride = strides[layerId]
            val gridH = INPUT / stride
            val gridW = INPUT / stride
            for (y in 0 until gridH) {
                val yCenter = (y + 0.5f) / gridH
                for (x in 0 until gridW) {
                    val xCenter = (x + 0.5f) / gridW
                    repeat(repeats) {
                        anchors += floatArrayOf(xCenter, yCenter)
                    }
                }
            }
            layerId = lastSameStride
        }
        check(anchors.size == NUM_ANCHORS) { "anchor count ${anchors.size} != $NUM_ANCHORS" }
        return anchors.toTypedArray()
    }

    private fun nonMaxSuppression(
        candidates: List<Pair<FloatArray, Float>>,
    ): List<Pair<FloatArray, Float>> {
        val sorted = candidates.sortedByDescending { it.second }
        val kept = ArrayList<Pair<FloatArray, Float>>()
        for (c in sorted) {
            if (kept.any { iou(c.first, it.first) > NMS_IOU }) continue
            kept += c
            if (kept.size >= PretextVisionLimits.MAX_FACES) break
        }
        return kept
    }

    private fun iou(a: FloatArray, b: FloatArray): Float {
        val ix0 = max(a[0], b[0])
        val iy0 = max(a[1], b[1])
        val ix1 = min(a[2], b[2])
        val iy1 = min(a[3], b[3])
        val inter = max(0f, ix1 - ix0) * max(0f, iy1 - iy0)
        val areaA = (a[2] - a[0]) * (a[3] - a[1])
        val areaB = (b[2] - b[0]) * (b[3] - b[1])
        val union = areaA + areaB - inter
        return if (union > 1e-6f) inter / union else 0f
    }

    private fun sigmoid(x: Float): Float = 1f / (1f + kotlin.math.exp(-x))
}
