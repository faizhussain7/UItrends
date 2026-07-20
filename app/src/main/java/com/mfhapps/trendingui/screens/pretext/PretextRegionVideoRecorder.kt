package com.mfhapps.trendingui.screens.pretext

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.PixelCopy
import android.view.Surface
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class PretextRegionVideoRecorder(
    private val activityProvider: () -> Activity?,
    private val captureBoundsProvider: () -> Rect?,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val running = AtomicBoolean(false)
    private val capturePaused = AtomicBoolean(false)
    private val frameInFlight = AtomicBoolean(false)

    private var mediaRecorder: MediaRecorder? = null
    private var recorderSurface: Surface? = null
    private var outputFile: File? = null
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0
    private var lockedCaptureBounds: Rect? = null
    private var frameCallback: Choreographer.FrameCallback? = null
    private var lastFrameNs: Long = 0L
    private var frameIntervalNs: Long = defaultFrameIntervalNs()
    private var activeProfile: PretextRecordingProfile? = null
    private val framePaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

    val isCapturePaused: Boolean
        get() = capturePaused.get()

    val isRecording: Boolean
        get() = running.get()

    val currentProfile: PretextRecordingProfile?
        get() = activeProfile

    fun start(
        file: File,
        profile: PretextRecordingProfile,
        boundsOverride: Rect? = null,
    ): Boolean {
        if (running.get()) return false
        val bounds = boundsOverride?.takeIf { it.width() > 1 && it.height() > 1 }
            ?: captureBoundsProvider()?.takeIf { it.width() > 1 && it.height() > 1 }
            ?: return false
        activeProfile = profile
        frameIntervalNs = 1_000_000_000L / profile.targetFps.coerceAtLeast(1)
        lockedCaptureBounds = Rect(bounds)
        val (width, height) = computeVideoSize(
            sourceWidth = bounds.width(),
            sourceHeight = bounds.height(),
            maxLongEdgePx = profile.maxLongEdgePx,
        )
        outputFile = file
        videoWidth = width
        videoHeight = height
        val encodingBitrate = maxOf(
            profile.videoBitrate,
            PretextRecordingProfile.recommendBitrate(width, height, profile.targetFps),
        )
        return runCatching {
            val recorder = createMediaRecorder(
                file = file,
                width = width,
                height = height,
                fps = profile.targetFps,
                bitrate = encodingBitrate,
            )
            mediaRecorder = recorder
            recorderSurface = recorder.surface
            recorder.start()
            running.set(true)
            lastFrameNs = 0L
            scheduleFrameCapture()
            true
        }.getOrElse {
            releaseRecorder(deleteOutput = true)
            false
        }
    }

    fun pauseCapture() {
        if (!running.get()) return
        capturePaused.set(true)
        frameCallback?.let { Choreographer.getInstance().removeFrameCallback(it) }
        frameCallback = null
    }

    fun resumeCapture() {
        if (!running.get() || !capturePaused.getAndSet(false)) return
        lastFrameNs = 0L
        val activity = activityProvider() ?: return
        val decorView = activity.window?.decorView ?: return
        decorView.post {
            if (running.get() && !capturePaused.get() && canCaptureFromWindow(activity)) {
                scheduleFrameCapture()
            }
        }
    }

    fun stop(): Boolean {
        if (!running.getAndSet(false)) return false
        frameCallback?.let { Choreographer.getInstance().removeFrameCallback(it) }
        frameCallback = null
        capturePaused.set(false)
        lockedCaptureBounds = null
        activeProfile = null
        return runCatching {
            mediaRecorder?.stop()
            true
        }.getOrElse {
            outputFile?.delete()
            false
        }.also {
            releaseRecorder(deleteOutput = false)
        }
    }

    fun release() {
        if (running.get()) {
            stop()
        } else {
            releaseRecorder(deleteOutput = false)
        }
    }

    private fun scheduleFrameCapture() {
        val choreographer = Choreographer.getInstance()
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!running.get() || capturePaused.get()) return
                choreographer.postFrameCallback(this)
                if (lastFrameNs != 0L && frameTimeNanos - lastFrameNs < frameIntervalNs) return
                val activity = activityProvider()
                if (activity == null || !canCaptureFromWindow(activity)) return
                lastFrameNs = frameTimeNanos
                captureFrame(activity)
            }
        }
        frameCallback = callback
        choreographer.postFrameCallback(callback)
    }

    private fun captureFrame(activity: Activity) {
        if (!running.get() || capturePaused.get() || frameInFlight.get()) return
        if (!canCaptureFromWindow(activity)) return
        val bounds = lockedCaptureBounds?.takeIf { it.width() > 1 && it.height() > 1 } ?: return
        val surface = recorderSurface ?: return
        val width = videoWidth
        val height = videoHeight
        if (width <= 1 || height <= 1) return

        frameInFlight.set(true)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            PixelCopy.request(
                activity.window,
                bounds,
                bitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS && running.get() && !capturePaused.get()) {
                        runCatching {
                            val canvas = surface.lockHardwareCanvas()
                            canvas.drawColor(Color.BLACK)
                            canvas.drawBitmap(bitmap, null, Rect(0, 0, width, height), framePaint)
                            surface.unlockCanvasAndPost(canvas)
                        }
                    }
                    bitmap.recycle()
                    frameInFlight.set(false)
                },
                mainHandler,
            )
        } catch (_: IllegalArgumentException) {
            bitmap.recycle()
            frameInFlight.set(false)
        } catch (_: IllegalStateException) {
            bitmap.recycle()
            frameInFlight.set(false)
        }
    }

    private fun canCaptureFromWindow(activity: Activity): Boolean {
        if (activity.isFinishing) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed) {
            return false
        }
        val decorView = activity.window?.decorView ?: return false
        return decorView.isAttachedToWindow && decorView.isShown
    }

    private fun createMediaRecorder(
        file: File,
        width: Int,
        height: Int,
        fps: Int,
        bitrate: Int,
    ): MediaRecorder {
        val activity = activityProvider()
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(activity ?: throw IllegalStateException("Activity required for recording"))
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        recorder.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoSize(width, height)
            setVideoFrameRate(fps)
            setVideoEncodingBitRate(bitrate)
            setOutputFile(file.absolutePath)
            prepare()
        }
        return recorder
    }

    private fun releaseRecorder(deleteOutput: Boolean) {
        runCatching { mediaRecorder?.reset() }
        runCatching { mediaRecorder?.release() }
        mediaRecorder = null
        recorderSurface = null
        if (deleteOutput) {
            outputFile?.delete()
        }
        outputFile = null
    }

    private companion object {
        fun defaultFrameIntervalNs(): Long =
            1_000_000_000L / PretextRecordingConfig.TARGET_FPS_DEFAULT

        fun even(value: Int): Int = value and 0x7FFFFFFE

        fun computeVideoSize(
            sourceWidth: Int,
            sourceHeight: Int,
            maxLongEdgePx: Int,
        ): Pair<Int, Int> {
            var width = sourceWidth.coerceAtLeast(2)
            var height = sourceHeight.coerceAtLeast(2)
            val longest = maxOf(width, height)
            if (longest > maxLongEdgePx) {
                val scale = maxLongEdgePx.toFloat() / longest
                width = (width * scale).toInt().coerceAtLeast(2)
                height = (height * scale).toInt().coerceAtLeast(2)
            }
            return even(width) to even(height)
        }
    }
}
