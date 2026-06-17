package com.mfhapps.trendingui.screens.pretext

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
internal class PretextMediaPipeFaceLandmarker(context: Context) {

    private val appContext = context.applicationContext
    private var landmarker: FaceLandmarker? = null
    private var initFailed = false

    data class FaceLandmarkDetection(
        val polylinePx: FloatArray,
        val score: Float,
        val backend: String = "mediapipe-face-landmarker",
    )

    fun detect(rgb: ByteArray, imageW: Int, imageH: Int): FaceLandmarkDetection? {
        if (imageW < 8 || imageH < 8) return null
        val marker = landmarker ?: createLandmarker() ?: return null
        val bitmap = rgbToBitmap(rgb, imageW, imageH) ?: return null
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = marker.detect(mpImage)
            val faces = result.faceLandmarks()
            if (faces.isEmpty()) return null

            val landmarks = faces.maxByOrNull { face ->
                ovalPresenceScore(face)
            } ?: return null
            if (landmarks.size < MIN_LANDMARK_COUNT) return null

            val score = ovalPresenceScore(landmarks)
            if (score < MIN_PRESENCE_SCORE) return null

            val polylinePx = buildFaceOvalPolylinePx(landmarks, imageW, imageH) ?: return null
            FaceLandmarkDetection(polylinePx = polylinePx, score = score)
        } catch (_: Exception) {
            null
        } finally {
            bitmap.recycle()
        }
    }

    fun close() {
        landmarker?.close()
        landmarker = null
    }

    private fun createLandmarker(): FaceLandmarker? {
        if (initFailed) return null
        return runCatching {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET)
                .build()
            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumFaces(1)
                .setMinFaceDetectionConfidence(MIN_FACE_DETECTION_CONFIDENCE)
                .setMinFacePresenceConfidence(MIN_FACE_PRESENCE_CONFIDENCE)
                .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
                .setOutputFaceBlendshapes(false)
                .setOutputFacialTransformationMatrixes(false)
                .build()
            FaceLandmarker.createFromOptions(appContext, options).also { landmarker = it }
        }.getOrElse {
            initFailed = true
            null
        }
    }

    private fun buildFaceOvalPolylinePx(
        landmarks: List<NormalizedLandmark>,
        imageW: Int,
        imageH: Int,
    ): FloatArray? {
        val out = FloatArray(FACE_OVAL_INDICES.size * 2)
        var i = 0
        for (idx in FACE_OVAL_INDICES) {
            if (idx >= landmarks.size) return null
            val lm = landmarks[idx]
            out[i++] = (lm.x() * imageW).coerceIn(0f, imageW.toFloat())
            out[i++] = (lm.y() * imageH).coerceIn(0f, imageH.toFloat())
        }
        return out
    }

    private fun ovalPresenceScore(landmarks: List<NormalizedLandmark>): Float {
        var sum = 0f
        var count = 0
        for (idx in FACE_OVAL_INDICES) {
            if (idx >= landmarks.size) continue
            sum += landmarkPresence(landmarks[idx])
            count++
        }
        return if (count > 0) sum / count else 0f
    }

    private fun landmarkPresence(lm: NormalizedLandmark): Float {
        val presence = lm.presence()
        if (presence.isPresent) {
            return presence.get().coerceIn(0f, 1f)
        }
        val visibility = lm.visibility()
        if (visibility.isPresent) {
            return visibility.get().coerceIn(0f, 1f)
        }
        return 1f
    }

    private fun rgbToBitmap(rgb: ByteArray, width: Int, height: Int): Bitmap? {
        val expected = width * height * 3
        if (rgb.size < expected) return null
        val pixels = IntArray(width * height)
        var i = 0
        var j = 0
        while (i < pixels.size) {
            val r = rgb[j++].toInt() and 0xff
            val g = rgb[j++].toInt() and 0xff
            val b = rgb[j++].toInt() and 0xff
            pixels[i++] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    companion object {
        private const val MODEL_ASSET = "vision/face_landmarker.task"

        private const val MIN_FACE_DETECTION_CONFIDENCE = 0.45f
        private const val MIN_FACE_PRESENCE_CONFIDENCE = 0.45f
        private const val MIN_TRACKING_CONFIDENCE = 0.45f
        private const val MIN_PRESENCE_SCORE = 0.40f
        private const val MIN_LANDMARK_COUNT = 468
        private val FACE_OVAL_INDICES = intArrayOf(
            10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288,
            397, 365, 379, 378, 400, 377, 152, 148, 176, 149, 150, 136,
            172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109,
        )
    }
}
