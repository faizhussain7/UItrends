package com.mfhapps.trendingui.screens.pretext

import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min
import kotlin.math.roundToInt

internal object PretextSsdDetector {

    private const val MAX_DETECTIONS = 10
    private const val SCORE_THRESHOLD = 0.25f

    data class SsdBox(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val label: String,
        val score: Float,
    )

    fun detect(
        interpreter: Interpreter,
        rgb: ByteArray,
        width: Int,
        height: Int,
        labels: Array<String>,
        excludePerson: Boolean,
    ): SsdBox? {
        if (width <= 0 || height <= 0 || rgb.size < width * height * 3) return null

        return runCatching {
            detectLocked(interpreter, rgb, width, height, labels, excludePerson)
        }.getOrNull()
    }

    private fun detectLocked(
        interpreter: Interpreter,
        rgb: ByteArray,
        width: Int,
        height: Int,
        labels: Array<String>,
        excludePerson: Boolean,
    ): SsdBox? {
        val inTensor = interpreter.getInputTensor(0)
        val inShape = inTensor.shape()
        val (inH, inW) = inputHeightWidth(inShape) ?: return null
        if (inH <= 0 || inW <= 0) return null

        val input = ByteBuffer.allocateDirect(tensorByteCount(inTensor))
            .order(ByteOrder.nativeOrder())

        val isUint8 = inTensor.dataType() == DataType.UINT8
        for (y in 0 until inH) {
            val sy = (y * height / inH).coerceIn(0, height - 1)
            for (x in 0 until inW) {
                val sx = (x * width / inW).coerceIn(0, width - 1)
                val i = (sy * width + sx) * 3
                if (i + 2 >= rgb.size) return null
                val r = rgb[i].toInt() and 0xff
                val g = rgb[i + 1].toInt() and 0xff
                val b = rgb[i + 2].toInt() and 0xff
                if (isUint8) {
                    input.put(r.toByte())
                    input.put(g.toByte())
                    input.put(b.toByte())
                } else {
                    input.putFloat(r / 255f)
                    input.putFloat(g / 255f)
                    input.putFloat(b / 255f)
                }
            }
        }
        input.rewind()

        val boxes = Array(1) { Array(MAX_DETECTIONS) { FloatArray(4) } }
        val classes = Array(1) { FloatArray(MAX_DETECTIONS) }
        val scores = Array(1) { FloatArray(MAX_DETECTIONS) }
        val count = Array(1) { FloatArray(1) }

        val outputMap = mutableMapOf<Int, Any>()
        for (i in 0 until interpreter.outputTensorCount) {
            val name = interpreter.getOutputTensor(i).name()?.lowercase().orEmpty()
            when {
                name.contains("box") || name.contains("location") -> outputMap[i] = boxes
                name.contains("class") -> outputMap[i] = classes
                name.contains("score") -> outputMap[i] = scores
                name.contains("num") || name.contains("count") -> outputMap[i] = count
            }
        }
        if (outputMap.size < 3) {
            outputMap.clear()
            if (interpreter.outputTensorCount > 0) outputMap[0] = boxes
            if (interpreter.outputTensorCount > 1) outputMap[1] = classes
            if (interpreter.outputTensorCount > 2) outputMap[2] = scores
            if (interpreter.outputTensorCount > 3) outputMap[3] = count
        }

        interpreter.runForMultipleInputsOutputs(arrayOf(input), outputMap)

        val n = min(MAX_DETECTIONS, count[0][0].roundToInt().coerceAtLeast(0))
        var bestIdx = -1
        var bestScore = SCORE_THRESHOLD
        for (i in 0 until n) {
            val classId = classes[0][i].roundToInt().coerceIn(0, labels.lastIndex)
            if (excludePerson && classId == 0) continue
            if (scores[0][i] > bestScore) {
                bestScore = scores[0][i]
                bestIdx = i
            }
        }
        if (bestIdx < 0) return null

        val box = boxes[0][bestIdx]
        val classId = classes[0][bestIdx].roundToInt().coerceIn(0, labels.lastIndex)
        return SsdBox(
            left = box[1] * width,
            top = box[0] * height,
            right = box[3] * width,
            bottom = box[2] * height,
            label = labels[classId],
            score = bestScore,
        )
    }

    private fun inputHeightWidth(shape: IntArray): Pair<Int, Int>? = when (shape.size) {
        4 -> when {
            shape[3] == 3 -> shape[1] to shape[2]
            shape[1] == 3 -> shape[2] to shape[3]
            else -> shape.getOrNull(1)?.let { h -> shape.getOrNull(2)?.let { w -> h to w } }
        }
        3 -> shape.getOrNull(1)?.let { h -> shape.getOrNull(2)?.let { w -> h to w } }
        else -> null
    }

    private fun tensorByteCount(tensor: Tensor): Int {
        val n = tensor.numElements()
        return when (tensor.dataType()) {
            DataType.UINT8 -> n
            DataType.FLOAT32 -> n * 4
            DataType.INT32 -> n * 4
            else -> n * 4
        }
    }
}
