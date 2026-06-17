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
    private val activeMode = AtomicReference<VisionTrackMode?>(null)
    private val vision = PretextVisionEngine(appContext.applicationContext)
    private val tracker = PretextShapeTracker(maxConsecutiveMisses = 30)

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
        val now = System.currentTimeMillis()
        if (!processing.compareAndSet(false, true)) {
            droppedCount++
            imageProxy.close()
            return
        }
        if (imageProxy.image == null) {
            finishFrame(
                imageProxy,
                CameraVisionFrame(
                    primary = null,
                    report = VisionDetectReport(null, "none", note = "null-image"),
                ),
                0L,
            )
            return
        }

        processedCount++
        reportTelemetry(now)

        val mode = trackModeProvider()
        val previous = activeMode.getAndSet(mode)
        if (previous != null && previous != mode) {
            PretextNativeGeometry.resetSmoothing()
            tracker.clear()
            PretextVisionLog.resetSession()
        }

        val stage = stageProvider()
        val useAutoMulti = stage.supportsMultiObstacle && mode == VisionTrackMode.Auto

        val detectStart = SystemClock.elapsedRealtime()
        val detectResult = try {
            if (useAutoMulti) {
                val bundle = vision.detectAutoMulti(imageProxy, lensFacingProvider(), maxShapes = 3)
                DetectResult(bundle.primary, bundle.extras)
            } else {
                DetectResult(vision.detect(imageProxy, mode, lensFacingProvider()), emptyList())
            }
        } catch (e: Exception) {
            DetectResult(VisionDetectReport(null, "error", note = e.javaClass.simpleName), emptyList())
        }
        val detectMs = SystemClock.elapsedRealtime() - detectStart
        lastDetectMs = detectMs
        val report = detectResult.primary
        val contour = report.contour

        var accuracy: VisionAccuracySnapshot? = null
        var layoutMissing = false

        val shape = when {
            contour != null && report.isBlobBoxFallback() && tracker.currentShape != null -> {
                val held = tracker.onWeakMiss()
                accuracy = VisionAccuracySnapshot(
                    tracking = tracker.trackingState,
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
                    note = report.note ?: "blob-fallback-held",
                )
                lastAccuracy = accuracy
                recordAccuracyWindow(accuracy, hit = false)
                held
            }
            contour != null -> {
                val mapped = mapContourToTrackedShape(contour, report, detectMs)
                accuracy = mapped.accuracy
                layoutMissing = mapped.layoutMissing
                mapped.shape
            }
            else -> {
                val held = when {
                    report.note in SOFT_HOLD_NOTES && tracker.currentShape != null ->
                        tracker.onWeakMiss()
                    report.note in SOFT_HOLD_NOTES -> null
                    else -> tracker.onMiss()
                }
                accuracy = VisionAccuracySnapshot(
                    tracking = tracker.trackingState,
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
                held
            }
        }

        val extraShapes = detectResult.extras.mapNotNull { extra ->
            val c = extra.contour ?: return@mapNotNull null
            mapContourToViewShape(c, extra, detectMs, useTracker = false)
        }

        if (shape != null) {
            lastPublishedSource = shape.source
        } else if (extraShapes.isNotEmpty()) {
            lastPublishedSource = extraShapes.first().source
        }

        if (onPreviewBitmap != null && previewBitmapEnabledProvider()) {
            maybePublishPreviewBitmap(imageProxy)
        }
        finishFrame(
            imageProxy = imageProxy,
            frame = CameraVisionFrame(primary = shape, extraShapes = extraShapes, report = report),
            detectMs = detectMs,
        )
    }

    private data class DetectResult(
        val primary: VisionDetectReport,
        val extras: List<VisionDetectReport>,
    )

    private data class MappedShape(
        val shape: ViewShape?,
        val accuracy: VisionAccuracySnapshot?,
        val layoutMissing: Boolean,
    )

    private fun mapContourToTrackedShape(
        contour: VisionContour,
        report: VisionDetectReport,
        detectMs: Long,
    ): MappedShape {
        var base = PretextVisionLog.analyzeContour(contour)?.copy(
            backend = report.backend,
            score = report.score,
            detectMs = detectMs,
            tracking = ShapeTrackingState.Live,
            note = report.note,
        )
        val layout = previewLayoutCache.current(contour)
        if (layout == null) {
            return MappedShape(tracker.currentShape, base, layoutMissing = true)
        }
        val mapStart = SystemClock.elapsedRealtime()
        val viewShape = PretextViewportMapper.mapContourToView(
            contour = contour,
            layout = layout,
            isLiveDetection = true,
        )
        val mapMs = SystemClock.elapsedRealtime() - mapStart
        tracker.onDetection(contour, viewShape)
        base = base?.let { PretextVisionLog.analyzeMappedShape(contour, viewShape, layout, it, mapMs) }
        lastAccuracy = base
        lastBackend = report.backend
        base?.let { recordAccuracyWindow(it, hit = true) }
        return MappedShape(tracker.currentShape ?: viewShape, base, layoutMissing = false)
    }

    private fun mapContourToViewShape(
        contour: VisionContour,
        report: VisionDetectReport,
        detectMs: Long,
        useTracker: Boolean,
    ): ViewShape? {
        val layout = previewLayoutCache.current(contour) ?: return null
        val viewShape = PretextViewportMapper.mapContourToView(
            contour = contour,
            layout = layout,
            isLiveDetection = true,
        )
        if (useTracker) {
            tracker.onDetection(contour, viewShape)
        }
        lastBackend = report.backend
        return viewShape
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

    private fun finishFrame(
        imageProxy: ImageProxy,
        frame: CameraVisionFrame,
        @Suppress("UNUSED_PARAMETER") detectMs: Long,
    ) {
        onVisionFrame(frame)
        processing.set(false)
        imageProxy.close()
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
        PretextNativeGeometry.resetSmoothing()
        vision.close()
        tracker.clear()
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

    companion object {
        private val SOFT_HOLD_NOTES = setOf(
            "low-foreground",
            "frame-fill",
            "area-too-large",
            "blob-too-small",
            "blob-fallback-held",
            "no-face",
            "face-too-small",
            "face-out-of-range",
            "face-too-large",
        )
    }
}
