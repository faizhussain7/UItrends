package com.mfhapps.trendingui.screens.pretext

import com.mfhapps.trendingui.util.traceSection

internal class PretextFaceVisionBackend(
    private val runtime: PretextVisionRuntime,
) : PretextVisionBackend {

    override val source = VisionSource.Face
    override val backendLabel = "mediapipe-face-landmarker"

    override fun detect(frame: PretextVisionFrame): VisionDetectReport {
        val mpLm = traceSection("pretext:mediapipe:face-landmarker") {
            runtime.mediaPipeFaceLandmarker.detect(frame.rgb, frame.rgbWidth, frame.rgbHeight)
        }
        if (mpLm != null) {
            return buildFaceContourReport(
                polylinePx = mpLm.polylinePx,
                boxPx = null,
                score = mpLm.score,
                detectBackend = mpLm.backend,
                frame = frame.frame,
            )
        }

        val fallback = traceSection("pretext:tflite:blazeface") {
            synchronized(runtime.interpreterLock) {
                if (runtime.closed) return@traceSection null
                val interpreter = runtime.faceInterpreter() ?: return@traceSection null
                PretextBlazeFaceDecoder.detectWithScore(
                    frame.rgb,
                    frame.rgbWidth,
                    frame.rgbHeight,
                    interpreter,
                )
            }
        }
        if (fallback == null) {
            return VisionDetectReport(null, backendLabel, note = "no-face")
        }
        return buildFaceContourReport(
            polylinePx = null,
            boxPx = fallback.box,
            score = fallback.score,
            detectBackend = "tflite-blazeface",
            frame = frame.frame,
        )
    }

    private fun buildFaceContourReport(
        polylinePx: FloatArray?,
        boxPx: FloatArray?,
        score: Float,
        detectBackend: String,
        frame: FrameMetrics,
    ): VisionDetectReport {
        val contour = traceSection("pretext:native:face") {
            when {
                polylinePx != null -> PretextContourExtractor.fromFacePolyline(
                    polylinePx,
                    frame.analysisWidth,
                    frame.analysisHeight,
                )
                boxPx != null -> PretextContourExtractor.fromFaceBox(
                    boxPx,
                    frame.analysisWidth,
                    frame.analysisHeight,
                )
                else -> null
            }
        }
        var vision = contour?.toVisionContour(VisionSource.Face, "face", frame)
            ?: return VisionDetectReport(null, detectBackend, score = score, note = "contour-failed")

        vision = PretextPersonSegmentation.clampFaceContour(vision)
        val area = vision.boundsRectNorm().width() * vision.boundsRectNorm().height()
        if (area < MIN_FACE_NORM_AREA) {
            return VisionDetectReport(null, detectBackend, score = score, note = "face-too-small")
        }
        if (area > MAX_FACE_PUBLISH_NORM_AREA) {
            return VisionDetectReport(null, detectBackend, score = score, note = "face-too-large")
        }
        return VisionDetectReport(
            contour = vision,
            backend = "$detectBackend+cpp-contour",
            score = score,
        )
    }

    companion object {
        private const val MIN_FACE_NORM_AREA = 0.008f
        private const val MAX_FACE_PUBLISH_NORM_AREA = 0.14f
    }
}
