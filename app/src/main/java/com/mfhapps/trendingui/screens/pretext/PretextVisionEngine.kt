package com.mfhapps.trendingui.screens.pretext

import android.content.Context
import com.mfhapps.trendingui.util.traceSection
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy

@ExperimentalGetImage
internal class PretextVisionEngine(context: Context) {

    private val runtime = PretextVisionRuntime(context)
    private val objectBackend = PretextObjectVisionBackend(runtime)
    private val registry = PretextVisionRegistry(
        face = PretextFaceVisionBackend(runtime),
        person = PretextPersonVisionBackend(runtime),
        objectBackend = objectBackend,
    )

    fun close() = runtime.close()

    val isNcnnReady: Boolean get() = runtime.isNcnnReady

    fun detect(
        imageProxy: ImageProxy,
        mode: VisionTrackMode,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        activeSource: VisionSource? = null,
    ): VisionDetectReport {
        if (runtime.closed) {
            return VisionDetectReport(null, "closed", note = "engine-closed")
        }

        if (mode == VisionTrackMode.Object) {
            val frame = frameMetrics(imageProxy)
            if (!runtime.isNcnnReady) {
                return VisionDetectReport(null, "native-yuv", note = "ncnn-not-ready")
            }
            objectBackend.processNativeFrame(imageProxy, frame)?.let { return it }
            return VisionDetectReport(null, "native-yuv", note = "no-detection")
        }

        return traceSection("pretext:rgb") {
            registry.detect(mode, toVisionFrame(imageProxy, lensFacing), activeSource)
        }
    }

    fun detectAutoMulti(
        imageProxy: ImageProxy,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        maxShapes: Int = 3,
        activeSource: VisionSource? = null,
    ): PretextVisionRegistry.AutoDetectBundle {
        if (runtime.closed) {
            return PretextVisionRegistry.AutoDetectBundle(
                VisionDetectReport(null, "closed", note = "engine-closed"),
            )
        }
        return traceSection("pretext:rgb") {
            registry.detectAutoMulti(toVisionFrame(imageProxy, lensFacing), maxShapes, activeSource)
        }
    }

    private fun toVisionFrame(imageProxy: ImageProxy, lensFacing: Int): PretextVisionFrame =
        toVisionFrame(imageProxy, frameMetrics(imageProxy), lensFacing)

    private fun toVisionFrame(
        imageProxy: ImageProxy,
        frame: FrameMetrics,
        lensFacing: Int,
    ): PretextVisionFrame {
        val rotation = frame.rotationDegrees
        val imageW = frame.analysisWidth
        val imageH = frame.analysisHeight
        val rawRgb = PretextYuvConverter.toRgb888(imageProxy).copyOf()
        val (rgb, rgbW) = PretextYuvConverter.rotateRgb(
            rawRgb,
            imageProxy.width,
            imageProxy.height,
            rotation,
        )
        val rgbH = if (rgbW == imageW) imageH else imageW
        return PretextVisionFrame(
            rgb = rgb,
            rgbWidth = rgbW,
            rgbHeight = rgbH,
            frame = frame,
            imageProxy = imageProxy,
            lensFacing = lensFacing,
        )
    }

    private fun frameMetrics(imageProxy: ImageProxy): FrameMetrics {
        val rotation = imageProxy.imageInfo.rotationDegrees
        val w = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val h = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height
        return FrameMetrics(analysisWidth = w, analysisHeight = h, rotationDegrees = rotation)
    }
}
