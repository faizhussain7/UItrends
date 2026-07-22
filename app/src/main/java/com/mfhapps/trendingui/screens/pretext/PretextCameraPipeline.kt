package com.mfhapps.trendingui.screens.pretext

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.mfhapps.trendingui.util.traceSection
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.mfhapps.trendingui.native.PretextNativeGeometry
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@ExperimentalGetImage
class PretextCameraPipeline(
    appContext: Context,
    private val previewLayoutCache: PretextPreviewLayoutCache,
    private val trackModeProvider: () -> VisionTrackMode,
    private val stageProvider: () -> PretextCameraStage = { PretextCameraStage.CameraOverlay },
    private val lensFacingProvider: () -> Int = { CameraSelector.LENS_FACING_BACK },
    private val onVisionFrame: (CameraVisionFrame) -> Unit,
    private val onTelemetry: ((VisionTelemetry) -> Unit)? = null,
    private val previewBitmapEnabledProvider: () -> Boolean = { false },
    private val onPreviewBitmap: ((Bitmap) -> Unit)? = null,
) : ImageAnalysis.Analyzer {

    private val processing = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)
    private val activeMode = AtomicReference<VisionTrackMode?>(null)
    private val vision = PretextVisionEngine(appContext.applicationContext)
    private val multiTracker = PretextMultiShapeTracker(maxSlots = PretextVisionLimits.MAX_TRACKED_SLOTS)

    @Volatile private var processedCount = 0
    @Volatile private var lastDetectMs = 0L
    @Volatile private var droppedCount = 0
    @Volatile private var fpsWindowStart = System.currentTimeMillis()
    @Volatile private var lastPublishedSource: VisionSource = VisionSource.Idle
    @Volatile private var lastAccuracy: VisionAccuracySnapshot? = null
    @Volatile private var lastBackend: String? = null

    @Volatile private var windowFrames = 0
    @Volatile private var windowHits = 0
    @Volatile private var windowNormAreaSum = 0f
    @Volatile private var windowIouSum = 0f
    @Volatile private var windowIouCount = 0
    override fun analyze(imageProxy: ImageProxy) {
        traceSection("pretext:pipeline") {
            analyzeFrame(imageProxy)
        }
    }

    private fun analyzeFrame(imageProxy: ImageProxy) {
        if (!processing.compareAndSet(false, true)) {
            droppedCount++
            imageProxy.close()
            return
        }
        try {
            if (closed.get()) return

            val now = System.currentTimeMillis()
            if (imageProxy.image == null) {
                onVisionFrame(
                    CameraVisionFrame(
                        primary = null,
                        report = VisionDetectReport(null, "none", note = "null-image"),
                    ),
                )
                return
            }

            processedCount++
            reportTelemetry(now)

            val mode = trackModeProvider()
            val previous = activeMode.getAndSet(mode)
            if (previous != null && previous != mode) {
                PretextNativeGeometry.resetSmoothing()
                multiTracker.clear()
                PretextVisionLog.resetSession()
                lastBackend = null
                lastPublishedSource = VisionSource.Idle
            }

            val maxInstances = PretextVisionLimits.maxFor(mode)

            val detectStart = SystemClock.elapsedRealtime()
            val reports = try {
                vision.detectMulti(
                    imageProxy = imageProxy,
                    mode = mode,
                    lensFacing = lensFacingProvider(),
                    maxInstances = maxInstances,
                    activeSource = if (mode == VisionTrackMode.Auto) lastPublishedSource else null,
                )
            } catch (e: Exception) {
                emptyList()
            }
            val detectMs = SystemClock.elapsedRealtime() - detectStart
            lastDetectMs = detectMs
            val report = reports.firstOrNull()
                ?: VisionDetectReport(null, "none", note = "no-detection")
            lastBackend = report.backend

            val mappedDetections = reports.mapNotNull { item ->
                val contour = item.contour ?: return@mapNotNull null
                val layout = previewLayoutCache.current(contour) ?: return@mapNotNull null
                val viewShape = PretextViewportMapper.mapContourToView(
                    contour = contour,
                    layout = layout,
                    isLiveDetection = true,
                )
                PretextMultiShapeTracker.Detection(contour = contour, shape = viewShape, score = item.score)
            }

            val tracked = multiTracker.update(mappedDetections)
            val shape = tracked.firstOrNull()
            val extraShapes = tracked.drop(1)

            if (shape != null || extraShapes.isNotEmpty()) {
                val accuracyContour = reports.firstOrNull()?.contour
                if (accuracyContour != null) {
                    val accuracy = PretextVisionLog.analyzeContour(accuracyContour, report.score)?.copy(
                        backend = report.backend,
                        score = report.score,
                        detectMs = detectMs,
                        tracking = multiTracker.trackingState,
                        note = report.note,
                    )
                    if (accuracy != null) {
                        lastAccuracy = accuracy
                        recordAccuracyWindow(accuracy, hit = true)
                    }
                }
            } else {
                val accuracy = VisionAccuracySnapshot(
                    tracking = multiTracker.trackingState,
                    backend = report.backend,
                    score = report.score,
                    normBBoxArea = 0f,
                    viewBBoxAreaRatio = 0f,
                    polygonVertices = 0,
                    boundsFullyInView = false,
                    iouVsPrevious = null,
                    centerDriftNorm = null,
                    detectMs = detectMs,
                    mapMs = 0L,
                    note = report.note ?: "miss",
                )
                lastAccuracy = accuracy
                recordAccuracyWindow(accuracy, hit = false)
            }

            if (shape != null) {
                lastPublishedSource = shape.source
            } else if (extraShapes.isNotEmpty()) {
                lastPublishedSource = extraShapes.first().source
            }

            if (onPreviewBitmap != null && previewBitmapEnabledProvider()) {
                maybePublishPreviewBitmap(imageProxy)
            }
            onVisionFrame(
                CameraVisionFrame(primary = shape, extraShapes = extraShapes, report = report),
            )
        } finally {
            processing.set(false)
            imageProxy.close()
        }
    }

    private fun recordAccuracyWindow(snapshot: VisionAccuracySnapshot, hit: Boolean) {
        windowFrames++
        if (hit) {
            windowHits++
            windowNormAreaSum += snapshot.normBBoxArea
            snapshot.iouVsPrevious?.let {
                windowIouSum += it
                windowIouCount++
            }
        }
    }

    private fun reportTelemetry(now: Long) {
        val elapsed = now - fpsWindowStart
        if (elapsed >= 1000L) {
            val fps = processedCount * 1000f / elapsed
            val hitRate = if (windowFrames > 0) windowHits.toFloat() / windowFrames else 0f
            val avgArea = if (windowHits > 0) windowNormAreaSum / windowHits else 0f
            val avgIou = if (windowIouCount > 0) windowIouSum / windowIouCount else 0f
            onTelemetry?.invoke(
                VisionTelemetry(
                    processedFps = fps,
                    droppedFrames = droppedCount,
                    lastSource = lastPublishedSource,
                    detectHitRate = hitRate,
                    avgNormArea = avgArea,
                    avgIouStability = avgIou,
                    lastAccuracy = lastAccuracy,
                    lastBackend = lastBackend,
                    ncnnReady = vision.isNcnnReady,
                ),
            )
            processedCount = 0
            droppedCount = 0
            fpsWindowStart = now
            windowFrames = 0
            windowHits = 0
            windowNormAreaSum = 0f
            windowIouSum = 0f
            windowIouCount = 0
        }
    }

    fun close() {
        if (!closed.compareAndSet(false, true)) return
        val deadline = SystemClock.elapsedRealtime() + 3_000L
        while (processing.get() && SystemClock.elapsedRealtime() < deadline) {
            Thread.sleep(5)
        }
        PretextNativeGeometry.resetSmoothing()
        vision.close()
        multiTracker.clear()
        activeMode.set(null)
        PretextVisionLog.resetSession()
    }

    private fun maybePublishPreviewBitmap(imageProxy: ImageProxy) {
        val frameIdx = processedCount
        if (frameIdx % 3 != 0) return
        val img = imageProxy.image ?: return
        val rotation = imageProxy.imageInfo.rotationDegrees
        val rgbRaw = PretextYuvConverter.toRgb888(imageProxy).copyOf()
        val (rgb, rgbW) = PretextYuvConverter.rotateRgb(rgbRaw, img.width, img.height, rotation)
        val rgbH = if (rgbW == img.width) img.height else img.width
        val bmp = rgbToDownscaledBitmap(rgb, rgbW, rgbH, targetWidth = 360)
        onPreviewBitmap?.invoke(bmp)
    }

    private fun rgbToDownscaledBitmap(rgb: ByteArray, width: Int, height: Int, targetWidth: Int): Bitmap {
        val tw = targetWidth.coerceIn(120, width.coerceAtLeast(1))
        val th = ((height.toFloat() / width.toFloat()) * tw).toInt().coerceAtLeast(1)
        val pixels = IntArray(tw * th)
        val xStep = width.toFloat() / tw.toFloat()
        val yStep = height.toFloat() / th.toFloat()
        var o = 0
        for (y in 0 until th) {
            val sy = (y * yStep).toInt().coerceIn(0, height - 1)
            for (x in 0 until tw) {
                val sx = (x * xStep).toInt().coerceIn(0, width - 1)
                val i = (sy * width + sx) * 3
                val r = rgb[i].toInt() and 0xff
                val g = rgb[i + 1].toInt() and 0xff
                val b = rgb[i + 2].toInt() and 0xff
                pixels[o++] = (0xff shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return Bitmap.createBitmap(pixels, tw, th, Bitmap.Config.ARGB_8888)
    }
}
