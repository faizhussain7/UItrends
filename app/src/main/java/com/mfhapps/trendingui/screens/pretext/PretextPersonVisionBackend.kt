package com.mfhapps.trendingui.screens.pretext

import com.mfhapps.trendingui.util.traceSection
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

internal class PretextPersonVisionBackend(
    private val runtime: PretextVisionRuntime,
) : PretextVisionBackend {

    private val maskFilter = PretextMaskTemporalFilter(depth = 3)

    override val source = VisionSource.Person
    override val backendLabel = "tflite-selfie"

    override fun detect(frame: PretextVisionFrame): VisionDetectReport {
        return detectMulti(frame, maxInstances = 1).firstOrNull()
            ?: VisionDetectReport(null, backendLabel, note = "mask-failed")
    }

    override fun detectMulti(frame: PretextVisionFrame, maxInstances: Int): List<VisionDetectReport> {
        val mask = traceSection("pretext:tflite:selfie") {
            runSelfieSegmentation(frame.rgb, frame.rgbWidth, frame.rgbHeight)
        } ?: return emptyList()

        val plane = PretextPersonSegmentation.MaskPlane(mask.data, mask.width, mask.height)
        val stats = PretextPersonSegmentation.analyzeMask(plane)
        val score = max(stats.fgRatio, stats.centerFgRatio)
        if (!PretextPersonSegmentation.passesForegroundGate(stats, frame.lensFacing)) {
            return emptyList()
        }

        if (maxInstances <= 1) {
            val single = detectPersonFromMask(mask, frame.frame, frame.lensFacing)
            return if (single.contour != null) listOf(single) else emptyList()
        }

        val blobSet = PretextPersonSegmentation.findTopForegroundBlobs(
            mask = plane,
            maxK = maxInstances.coerceAtMost(PretextVisionLimits.MAX_PERSONS),
        )
        if (blobSet.blobs.isEmpty()) {
            val single = detectPersonFromMask(mask, frame.frame, frame.lensFacing)
            return if (single.contour != null) listOf(single) else emptyList()
        }

        return blobSet.blobs.mapNotNull { blob ->
            val isolated = PretextPersonSegmentation.isolateBlobMask(plane, blob, blobSet.labels)
            val contour = traceSection("pretext:native:person") {
                PretextContourExtractor.fromMask(
                    isolated.data,
                    isolated.width,
                    isolated.height,
                    frame.frame.analysisWidth,
                    frame.frame.analysisHeight,
                )
            }
            var vision = contour?.toVisionContour(VisionSource.Person, "person", frame.frame)
                ?: return@mapNotNull null
            vision = PretextPersonSegmentation.clampContourToReasonableSize(vision)
            val quality = PretextShapeAnalyzer.analyze(vision, score)
            if (!PretextShapeAnalyzer.isPublishable(quality, VisionSource.Person)) return@mapNotNull null
            val area = vision.boundsRectNorm().width() * vision.boundsRectNorm().height()
            if (area !in MIN_PUBLISH_NORM_AREA..MAX_PUBLISH_NORM_AREA) return@mapNotNull null
            VisionDetectReport(vision, "tflite-selfie+cpp-contour", score = score)
        }.let { reports ->
            PretextInstanceSelector.suppressOverlapping(reports, iouThreshold = 0.55f)
        }
    }

    private fun detectPersonFromMask(
        mask: MaskResult,
        frame: FrameMetrics,
        lensFacing: Int,
    ): VisionDetectReport {
        val plane = PretextPersonSegmentation.MaskPlane(mask.data, mask.width, mask.height)
        val stats = PretextPersonSegmentation.analyzeMask(plane)
        val score = max(stats.fgRatio, stats.centerFgRatio)

        if (!PretextPersonSegmentation.passesForegroundGate(stats, lensFacing)) {
            return VisionDetectReport(null, backendLabel, score = score, note = "low-foreground")
        }

        val smoothed = maskFilter.smooth(mask.data)
        val contour = traceSection("pretext:native:person") {
            PretextContourExtractor.fromMask(
                smoothed,
                mask.width,
                mask.height,
                frame.analysisWidth,
                frame.analysisHeight,
            )
        }

        var vision = contour?.toVisionContour(VisionSource.Person, "person", frame)
        if (vision != null) {
            vision = PretextPersonSegmentation.clampContourToReasonableSize(vision)
            val quality = PretextShapeAnalyzer.analyze(vision, score)
            if (PretextShapeAnalyzer.isPublishable(quality, VisionSource.Person)) {
                val area = vision.boundsRectNorm().width() * vision.boundsRectNorm().height()
                if (area in MIN_PUBLISH_NORM_AREA..MAX_PUBLISH_NORM_AREA) {
                    return VisionDetectReport(vision, "tflite-selfie+cpp-contour", score = score)
                }
            }
        }

        val note = when {
            PretextPersonSegmentation.isBlobTooSmall(stats) -> "blob-too-small"
            !PretextPersonSegmentation.passesForegroundGate(stats, lensFacing) -> "low-foreground"
            else -> "contour-failed"
        }
        return VisionDetectReport(null, backendLabel, score = score, note = note)
    }

    private data class MaskResult(val data: FloatArray, val width: Int, val height: Int)

    private fun runSelfieSegmentation(rgb: ByteArray, width: Int, height: Int): MaskResult? {
        synchronized(runtime.interpreterLock) {
            if (runtime.closed) return null
            val interpreter = runtime.selfieInterpreter() ?: return null
            return runSelfieSegmentationLocked(interpreter, rgb, width, height)
        }
    }

    private fun runSelfieSegmentationLocked(
        interpreter: Interpreter,
        rgb: ByteArray,
        width: Int,
        height: Int,
    ): MaskResult? {
        val inTensor = interpreter.getInputTensor(0)
        val inShape = inTensor.shape()
        val inH = inShape.getOrNull(1) ?: inShape.getOrNull(2) ?: return null
        val inW = inShape.getOrNull(2) ?: inShape.getOrNull(3) ?: return null
        val isUint8 = inTensor.dataType() == DataType.UINT8

        val input = ByteBuffer.allocateDirect(runtime.tensorByteCount(inTensor))
            .order(ByteOrder.nativeOrder())

        for (y in 0 until inH) {
            val sy = y * height / inH
            for (x in 0 until inW) {
                val sx = x * width / inW
                val i = (sy * width + sx) * 3
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

        val outTensor = interpreter.getOutputTensor(0)
        val outShape = outTensor.shape()
        val outIsUint8 = outTensor.dataType() == DataType.UINT8
        val outBuf = ByteBuffer.allocateDirect(runtime.tensorByteCount(outTensor))
            .order(ByteOrder.nativeOrder())
        interpreter.run(input, outBuf)
        outBuf.rewind()

        val outH: Int
        val outW: Int
        when (outShape.size) {
            4 -> {
                outH = outShape[1]
                outW = outShape[2]
            }
            3 -> {
                outH = outShape[1]
                outW = outShape[2]
            }
            else -> return null
        }

        val count = outW * outH
        val floats = runtime.maskScratch.get()?.takeIf { it.size == count }
            ?: FloatArray(count).also { runtime.maskScratch.set(it) }

        val channels = if (outShape.size == 4) outShape[3] else 1
        for (y in 0 until outH) {
            for (x in 0 until outW) {
                val v = when {
                    channels > 1 && outIsUint8 -> {
                        outBuf.get()
                        (outBuf.get().toInt() and 0xff) / 255f
                    }
                    channels > 1 -> {
                        outBuf.float
                        outBuf.float
                    }
                    outIsUint8 -> (outBuf.get().toInt() and 0xff) / 255f
                    else -> outBuf.float
                }
                floats[y * outW + x] = v.coerceIn(0f, 1f)
            }
        }
        return MaskResult(floats, outW, outH)
    }

    companion object {
        private const val MIN_PUBLISH_NORM_AREA = 0.006f
        private const val MAX_PUBLISH_NORM_AREA = 0.78f
    }
}
