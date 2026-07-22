package com.mfhapps.trendingui.native

import com.mfhapps.trendingui.core.text.PolygonObstacle
import com.mfhapps.trendingui.screens.pretext.FrameMetrics
import com.mfhapps.trendingui.screens.pretext.VisionContour
import com.mfhapps.trendingui.screens.pretext.VisionSource

data class NativeContour(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val polygonNorm: List<Pair<Float, Float>>,
) {
    fun toVisionContour(
        source: VisionSource,
        label: String?,
        frame: FrameMetrics,
    ): VisionContour = VisionContour(
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        polygonNorm = polygonNorm,
        label = label,
        source = source,
        frame = frame,
    )

    fun toPacket(): FloatArray {
        val n = polygonNorm.size
        return FloatArray(5 + n * 2).also { out ->
            out[0] = left
            out[1] = top
            out[2] = right
            out[3] = bottom
            out[4] = n.toFloat()
            polygonNorm.forEachIndexed { i, (x, y) ->
                out[5 + i * 2] = x
                out[5 + i * 2 + 1] = y
            }
        }
    }

    companion object {
        fun fromPacket(data: FloatArray?): NativeContour? {
            if (data == null || data.size < 5) return null
            val n = data[4].toInt()
            if (n < 3 || data.size < 5 + n * 2) return null
            val poly = ArrayList<Pair<Float, Float>>(n)
            for (i in 0 until n) {
                poly += data[5 + i * 2] to data[5 + i * 2 + 1]
            }
            return NativeContour(
                left = data[0],
                top = data[1],
                right = data[2],
                bottom = data[3],
                polygonNorm = poly,
            )
        }
    }
}

object PretextNativeGeometry {
    private var loaded = false

    private fun ensureLoaded() {
        if (loaded) return
        System.loadLibrary("pretext_geometry")
        loaded = true
    }

    fun extractPerson(
        mask: FloatArray,
        maskW: Int,
        maskH: Int,
        landmarks: FloatArray?,
        imageW: Int,
        imageH: Int,
    ): NativeContour? {
        ensureLoaded()
        return NativeContour.fromPacket(
            nativeExtractPerson(mask, maskW, maskH, landmarks, imageW, imageH),
        )
    }

    fun extractFace(
        polylineXy: FloatArray?,
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        imageW: Int,
        imageH: Int,
    ): NativeContour? {
        ensureLoaded()
        return NativeContour.fromPacket(
            nativeExtractFace(polylineXy, cx, cy, rx, ry, imageW, imageH),
        )
    }

    fun bandInterval(polyPx: FloatArray, bandTop: Float, bandBottom: Float): Pair<Float, Float>? {
        ensureLoaded()
        val out = nativeBandInterval(polyPx, bandTop, bandBottom) ?: return null
        return out[0] to out[1]
    }

    fun polygonToFlat(poly: PolygonObstacle): FloatArray {
        val n = poly.points.size
        return FloatArray(1 + n * 2).also { out ->
            out[0] = n.toFloat()
            poly.points.forEachIndexed { i, (x, y) ->
                out[1 + i * 2] = x
                out[1 + i * 2 + 1] = y
            }
        }
    }

    fun resetSmoothing() {
        ensureLoaded()
        nativeResetSmoothing()
    }

    private external fun nativeExtractPerson(
        mask: FloatArray,
        maskW: Int,
        maskH: Int,
        landmarks: FloatArray?,
        imageW: Int,
        imageH: Int,
    ): FloatArray?

    private external fun nativeExtractFace(
        polylineXy: FloatArray?,
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        imageW: Int,
        imageH: Int,
    ): FloatArray?

    private external fun nativeBandInterval(polyPx: FloatArray, bandTop: Float, bandBottom: Float): FloatArray?
    private external fun nativeResetSmoothing()
}
