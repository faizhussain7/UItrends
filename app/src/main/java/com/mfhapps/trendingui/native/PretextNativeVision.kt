package com.mfhapps.trendingui.native

import android.content.res.AssetManager
import java.nio.ByteBuffer


object PretextNativeVision {
    private var loaded = false

    @Volatile
    var isAvailable: Boolean = false
        private set

    private fun ensureLoaded() {
        if (loaded) return
        System.loadLibrary("pretext_geometry")
        loaded = true
    }

    fun init(assetManager: AssetManager): Boolean {
        ensureLoaded()
        isAvailable = nativeInit(assetManager)
        return isAvailable
    }

    fun release() {
        if (!loaded) return
        nativeRelease()
        isAvailable = false
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

    fun detectBestRgb(
        rgb: ByteArray,
        width: Int,
        height: Int,
        excludePerson: Boolean = false,
    ): FloatArray? {
        if (!isAvailable) return null
        return nativeDetectBestRgb(rgb, width, height, excludePerson)
    }

    fun classLabel(classId: Int): String = nativeClassLabel(classId)

    private external fun nativeInit(assetManager: AssetManager): Boolean
    private external fun nativeRelease()
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
    private external fun nativeDetectBestRgb(
        rgb: ByteArray,
        width: Int,
        height: Int,
        excludePerson: Boolean,
    ): FloatArray?
    private external fun nativeClassLabel(classId: Int): String
}
