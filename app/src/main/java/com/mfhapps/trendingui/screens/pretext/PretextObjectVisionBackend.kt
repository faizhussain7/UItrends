package com.mfhapps.trendingui.screens.pretext

import androidx.camera.core.ImageProxy
import com.mfhapps.trendingui.native.NativeContour
import com.mfhapps.trendingui.native.PretextNativeDetection
import com.mfhapps.trendingui.native.PretextNativeVision
import com.mfhapps.trendingui.util.traceSection
import java.nio.ByteBuffer

internal class PretextObjectVisionBackend(
    private val runtime: PretextVisionRuntime,
) : PretextVisionBackend {

    override val source = VisionSource.Object
    override val backendLabel: String
        get() = if (PretextNativeVision.isSegmentationBackend) "ncnn-yolo11-seg" else "none"

    fun processNativeFrame(imageProxy: ImageProxy, frame: FrameMetrics): VisionDetectReport? {
        if (!runtime.ncnnReady || !PretextNativeVision.isSegmentationBackend) return null
        val planes = imageProxy.planes
        if (planes.size < 3) return null
        val y = planes[0]
        val u = planes[1]
        val v = planes[2]
        val yBuf = y.buffer
        val uBuf = u.buffer
        val vBuf = v.buffer
        if (!yBuf.isDirect || !uBuf.isDirect || !vBuf.isDirect) return null
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
        ) ?: return null
        val score = if (packet.size > 2) packet[2] else null
        val contour = visionContourFromNativePacket(packet, frame) ?: return null
        val quality = PretextShapeAnalyzer.analyze(contour, score)
        if (!PretextShapeAnalyzer.isPublishable(quality, VisionSource.Object)) return null
        return VisionDetectReport(contour, backendLabel, score = score)
    }

    private fun visionContourFromNativePacket(packet: FloatArray, frame: FrameMetrics): VisionContour? {
        if (packet.size < 8) return null
        val classId = packet[1].toInt().coerceAtLeast(0)
        val label = PretextNativeVision.classLabel(classId)
        val vertexCount = packet[7].toInt()
        if (vertexCount < 6 || packet.size < 8 + vertexCount * 2) return null
        val poly = ArrayList<Pair<Float, Float>>(vertexCount)
        for (i in 0 until vertexCount) {
            poly += packet[8 + i * 2] to packet[8 + i * 2 + 1]
        }
        return NativeContour(
            left = packet[3].coerceIn(0f, 1f),
            top = packet[4].coerceIn(0f, 1f),
            right = packet[5].coerceIn(0f, 1f),
            bottom = packet[6].coerceIn(0f, 1f),
            polygonNorm = poly,
        ).toVisionContour(VisionSource.Object, label, frame)
    }

    private fun contourFromNativeDetection(
        det: PretextNativeDetection,
        frame: FrameMetrics,
    ): VisionContour? {
        if (!det.hasMaskContour || det.polygonNorm.size < 6) return null
        val label = PretextNativeVision.classLabel(det.classId)
        return NativeContour(
            left = det.left / frame.analysisWidth,
            top = det.top / frame.analysisHeight,
            right = det.right / frame.analysisWidth,
            bottom = det.bottom / frame.analysisHeight,
            polygonNorm = det.polygonNorm,
        ).toVisionContour(VisionSource.Object, label, frame)
    }

    override fun detect(frame: PretextVisionFrame): VisionDetectReport =
        detectMulti(frame, maxInstances = 1).firstOrNull()
            ?: VisionDetectReport(null, backendLabel, note = "no-object")

    override fun detectMulti(frame: PretextVisionFrame, maxInstances: Int): List<VisionDetectReport> {
        if (!runtime.ncnnReady) return emptyList()
        val capped = maxInstances.coerceAtMost(PretextVisionLimits.MAX_OBJECTS)
        if (capped <= 0) return emptyList()
        return detectNcnnRgbMulti(frame, capped)
    }

    private fun detectNcnnRgbMulti(frame: PretextVisionFrame, maxInstances: Int): List<VisionDetectReport> {
        val detections = traceSection("pretext:ncnn:object-multi") {
            PretextNativeVision.detectTopK(
                frame.rgb,
                frame.rgbWidth,
                frame.rgbHeight,
                maxK = maxInstances,
                excludePerson = true,
            )
        }
        if (detections.isEmpty()) return emptyList()
        val backend = "ncnn-yolo11-seg"
        return detections.mapNotNull { det ->
            val vision = contourFromNativeDetection(det, frame.frame) ?: return@mapNotNull null
            val quality = PretextShapeAnalyzer.analyze(vision, det.score)
            if (!PretextShapeAnalyzer.isPublishable(quality, VisionSource.Object)) return@mapNotNull null
            VisionDetectReport(
                contour = vision,
                backend = backend,
                score = det.score,
            )
        }
    }
}
