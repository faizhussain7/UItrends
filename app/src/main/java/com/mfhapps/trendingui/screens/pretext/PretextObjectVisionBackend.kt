package com.mfhapps.trendingui.screens.pretext

import androidx.camera.core.ImageProxy
import com.mfhapps.trendingui.native.PretextNativeVision
import com.mfhapps.trendingui.util.traceSection
import java.nio.ByteBuffer
import kotlin.math.min
import kotlin.math.roundToInt

internal class PretextObjectVisionBackend(
    private val runtime: PretextVisionRuntime,
) : PretextVisionBackend {

    override val source = VisionSource.Object
    override val backendLabel = "tflite-ssd"

    fun processNativeFrame(imageProxy: ImageProxy, frame: FrameMetrics): VisionDetectReport? {
        if (!runtime.ncnnReady) return null
        val planes = imageProxy.planes
        if (planes.size < 3) {
            return VisionDetectReport(null, "native-yuv-ncnn", note = "bad-planes")
        }
        val y = planes[0]
        val u = planes[1]
        val v = planes[2]
        val yBuf = y.buffer
        val uBuf = u.buffer
        val vBuf = v.buffer
        if (!yBuf.isDirect || !uBuf.isDirect || !vBuf.isDirect) {
            return VisionDetectReport(null, "native-yuv-ncnn", note = "non-direct-buffer")
        }
        val packet = PretextNativeVision.processFrame(
            mode = 0,
            rotationDegrees = imageProxy.imageInfo.rotationDegrees,
            imageW = imageProxy.width,
            imageH = imageProxy.height,
            y = yBuf,
            yRowStride = y.rowStride,
            u = uBuf,
            uRowStride = u.rowStride,
            uPixelStride = u.pixelStride,
            v = vBuf,
            vRowStride = v.rowStride,
            vPixelStride = v.pixelStride,
        ) ?: return VisionDetectReport(null, "native-yuv-ncnn", note = "empty-packet")
        val score = if (packet.size > 2) packet[2] else null
        val contour = visionContourFromNativePacket(packet, frame)
            ?: return VisionDetectReport(null, "native-yuv-ncnn", note = "bad-packet")
        return VisionDetectReport(contour, "native-yuv-ncnn", score = score)
    }

    private fun visionContourFromNativePacket(packet: FloatArray, frame: FrameMetrics): VisionContour? {
        if (packet.size < 8) return null
        val sourceId = packet[0].toInt().coerceAtLeast(0)
        val classId = packet[1].toInt().coerceAtLeast(0)
        val left = packet[3].coerceIn(0f, 1f)
        val top = packet[4].coerceIn(0f, 1f)
        val right = packet[5].coerceIn(0f, 1f)
        val bottom = packet[6].coerceIn(0f, 1f)
        val nPts = packet[7].toInt()
        if (nPts < 3 || packet.size < 8 + nPts * 2) return null
        val poly = ArrayList<Pair<Float, Float>>(nPts)
        for (i in 0 until nPts) {
            poly += packet[8 + i * 2].coerceIn(0f, 1f) to packet[8 + i * 2 + 1].coerceIn(0f, 1f)
        }
        val source = when (sourceId) {
            2 -> VisionSource.Object
            0 -> VisionSource.Face
            else -> VisionSource.Person
        }
        val label = when (source) {
            VisionSource.Object -> PretextNativeVision.classLabel(classId)
            else -> source.label.lowercase()
        }
        return VisionContour(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            polygonNorm = poly,
            label = label,
            source = source,
            frame = frame,
        )
    }

    override fun detect(frame: PretextVisionFrame): VisionDetectReport = detect(frame, skipYuv = false)

    fun detect(frame: PretextVisionFrame, skipYuv: Boolean): VisionDetectReport {
        val imageProxy = frame.imageProxy
        if (!skipYuv && imageProxy != null && runtime.ncnnReady) {
            processNativeFrame(imageProxy, frame.frame)?.let { report ->
                if (report.contour != null) return report
            }
        }

        if (runtime.ncnnReady) {
            val ncnnRgb = detectNcnnRgb(frame)
            if (ncnnRgb.contour != null) return ncnnRgb
        }

        return detectSsd(frame)
    }

    private fun detectNcnnRgb(frame: PretextVisionFrame): VisionDetectReport {
        val det = traceSection("pretext:ncnn:object") {
            PretextNativeVision.detectBestRgb(
                frame.rgb,
                frame.rgbWidth,
                frame.rgbHeight,
                excludePerson = true,
            )
        }
        if (det == null || det.size < 6) {
            return VisionDetectReport(null, "ncnn", note = "no-object")
        }
        val classId = det[5].roundToInt()
        val label = PretextNativeVision.classLabel(classId)
        val score = det[4]
        val contour = traceSection("pretext:native:object") {
            PretextContourExtractor.fromBox(
                det[0], det[1], det[2], det[3],
                frame.frame.analysisWidth,
                frame.frame.analysisHeight,
            )
        }
        val vision = contour?.toVisionContour(VisionSource.Object, label, frame.frame)
        return VisionDetectReport(
            contour = vision,
            backend = "ncnn+cpp-contour",
            score = score,
            note = if (vision == null) "contour-failed" else null,
        )
    }

    private fun detectSsd(frame: PretextVisionFrame): VisionDetectReport {
        val ssd = traceSection("pretext:tflite:object") {
            runSsdDetection(frame.rgb, frame.rgbWidth, frame.rgbHeight, excludePerson = true)
        } ?: return VisionDetectReport(
            null,
            backendLabel,
            note = if (runtime.ssdLoadFailed) "model-load-failed" else "no-object",
        )
        val contour = traceSection("pretext:native:object") {
            PretextContourExtractor.fromBox(
                ssd.left, ssd.top, ssd.right, ssd.bottom,
                frame.frame.analysisWidth,
                frame.frame.analysisHeight,
            )
        }
        val vision = contour?.toVisionContour(VisionSource.Object, ssd.label, frame.frame)
        return VisionDetectReport(
            contour = vision,
            backend = "tflite-ssd+cpp-contour",
            score = ssd.score,
            note = if (vision == null) "contour-failed" else null,
        )
    }

    private fun runSsdDetection(rgb: ByteArray, width: Int, height: Int, excludePerson: Boolean): PretextSsdDetector.SsdBox? {
        synchronized(runtime.interpreterLock) {
            if (runtime.closed) return null
            val interpreter = runtime.ssdInterpreter() ?: return null
            return PretextSsdDetector.detect(
                interpreter = interpreter,
                rgb = rgb,
                width = width,
                height = height,
                labels = SSD_LABELS,
                excludePerson = excludePerson,
            )
        }
    }

    companion object {
        private val SSD_LABELS = arrayOf(
            "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
            "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
            "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
            "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
            "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
            "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair",
            "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse",
            "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator",
            "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush",
        )
    }
}
