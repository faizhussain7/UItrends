package com.mfhapps.trendingui.native

import android.content.res.AssetManager
import java.nio.ByteBuffer

object PretextNativeVision {
    private var loaded = false

    @Volatile
    var isAvailable: Boolean = false
        private set

    @Volatile
    var isSegmentationBackend: Boolean = false
        private set

    private fun ensureLoaded() {
        if (loaded) return
        System.loadLibrary("pretext_geometry")
        loaded = true
    }

    fun init(assetManager: AssetManager, highQuality: Boolean = false): Boolean {
        ensureLoaded()
        nativeSetHighQuality(highQuality)
        isAvailable = nativeInit(assetManager)
        isSegmentationBackend = isAvailable && nativeIsSegmentationBackend()
        return isAvailable
    }

    fun release() {
        if (!loaded) return
        nativeRelease()
        isAvailable = false
        isSegmentationBackend = false
    }

    fun processFrame(
        mode: Int,
        rotationDegrees: Int,
        imageW: Int,
        imageH: Int,
        y: ByteBuffer,
        yRowStride: Int,
        u: ByteBuffer,
        uRowStride: Int,
        uPixelStride: Int,
        v: ByteBuffer,
        vRowStride: Int,
        vPixelStride: Int,
    ): FloatArray? {
        if (!isAvailable) return null
        return nativeProcessFrame(
            mode,
            rotationDegrees,
            imageW,
            imageH,
            y,
            yRowStride,
            u,
            uRowStride,
            uPixelStride,
            v,
            vRowStride,
            vPixelStride,
        )
    }

    fun classLabel(classId: Int): String = nativeClassLabel(classId)

    fun detectTopK(
        rgb: ByteArray,
        width: Int,
        height: Int,
        maxK: Int,
        excludePerson: Boolean = false,
    ): List<PretextNativeDetection> {
        if (!isAvailable || maxK <= 0) return emptyList()
        val flat = nativeDetectTopKRgb(rgb, width, height, maxK, excludePerson) ?: return emptyList()
        if (flat.isEmpty()) return emptyList()
        val count = flat[0].toInt().coerceAtLeast(0)
        if (count <= 0) return emptyList()

        val out = ArrayList<PretextNativeDetection>(count)
        var offset = 1
        repeat(count) {
            if (offset + 7 >= flat.size) return@repeat
            val polyCount = flat[offset + 6].toInt().coerceAtLeast(0)
            val poly = ArrayList<Pair<Float, Float>>(polyCount)
            var polyOffset = offset + 8
            repeat(polyCount) {
                if (polyOffset + 1 >= flat.size) return@repeat
                poly += flat[polyOffset] to flat[polyOffset + 1]
                polyOffset += 2
            }
            out += PretextNativeDetection(
                left = flat[offset],
                top = flat[offset + 1],
                right = flat[offset + 2],
                bottom = flat[offset + 3],
                score = flat[offset + 4],
                classId = flat[offset + 5].toInt(),
                polygonNorm = poly,
            )
            offset = polyOffset
        }
        return out
    }

    private external fun nativeInit(assetManager: AssetManager): Boolean
    private external fun nativeRelease()
    private external fun nativeSetHighQuality(highQuality: Boolean)
    private external fun nativeIsSegmentationBackend(): Boolean
    private external fun nativeProcessFrame(
        mode: Int,
        rotationDegrees: Int,
        imageW: Int,
        imageH: Int,
        y: ByteBuffer,
        yRowStride: Int,
        u: ByteBuffer,
        uRowStride: Int,
        uPixelStride: Int,
        v: ByteBuffer,
        vRowStride: Int,
        vPixelStride: Int,
    ): FloatArray?
    private external fun nativeDetectTopKRgb(
        rgb: ByteArray,
        width: Int,
        height: Int,
        maxK: Int,
        excludePerson: Boolean,
    ): FloatArray?
    private external fun nativeClassLabel(classId: Int): String
}
