package com.mfhapps.trendingui.screens.pretext

import com.mfhapps.trendingui.util.traceSection

internal class PretextFaceVisionBackend(
    private val runtime: PretextVisionRuntime,
) : PretextVisionBackend {

    override val source = VisionSource.Face
    override val backendLabel = "mediapipe-face-landmarker"

    override fun detect(frame: PretextVisionFrame): VisionDetectReport {
        return detectMulti(frame, maxInstances = 1).firstOrNull()
            ?: VisionDetectReport(null, backendLabel, note = "no-face")
    }

    override fun detectMulti(frame: PretextVisionFrame, maxInstances: Int): List<VisionDetectReport> {
        val mpFaces = traceSection("pretext:mediapipe:face-landmarker") {
            runtime.mediaPipeFaceLandmarker.detectMulti(
                frame.rgb,
                frame.rgbWidth,
                frame.rgbHeight,
                maxFaces = maxInstances.coerceAtMost(PretextVisionLimits.MAX_FACES),
            )
        }
        if (mpFaces.isNotEmpty()) {
            return mpFaces.mapNotNull { face ->
                buildFaceContourReport(
                    polylinePx = face.polylinePx,
                    boxPx = null,
                    score = face.score,
                    detectBackend = face.backend,
                    frame = frame.frame,
                ).takeIf { it.contour != null }
            }
        }

        val fallback = traceSection("pretext:tflite:blazeface") {
            synchronized(runtime.interpreterLock) {
                if (runtime.closed) return@traceSection emptyList()
                val interpreter = runtime.faceInterpreter() ?: return@traceSection emptyList()
                PretextBlazeFaceDecoder.detectMulti(
                    frame.rgb,
                    frame.rgbWidth,
                    frame.rgbHeight,
                    interpreter,
                    maxInstances = maxInstances,
                )
            }
        }
        return fallback.mapNotNull { detection ->
            buildFaceContourReport(
                polylinePx = null,
                boxPx = detection.box,
                score = detection.score,
                detectBackend = "tflite-blazeface",
                frame = frame.frame,
            ).takeIf { it.contour != null }
        }
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
        val quality = PretextShapeAnalyzer.analyze(vision, score)
        if (!PretextShapeAnalyzer.isPublishable(quality, VisionSource.Face)) {
            return VisionDetectReport(null, detectBackend, score = score, note = "face-quality")
        }
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
