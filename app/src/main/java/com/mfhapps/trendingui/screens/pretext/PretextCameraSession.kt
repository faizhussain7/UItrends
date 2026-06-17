package com.mfhapps.trendingui.screens.pretext

import android.content.Context
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.View
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PretextCameraSession(
    private val context: Context,
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var analysisExecutor: ExecutorService? = null

    var lensFacing: Int = CameraSelector.LENS_FACING_BACK
        private set
    var torchEnabled: Boolean = false
        private set

    private val resolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
        .setResolutionStrategy(
            ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER),
        )
        .build()

    suspend fun bind(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        analyzer: ImageAnalysis.Analyzer,
    ) {
        val provider = cameraProvider ?: obtainProvider()
        val viewport = awaitViewPort(previewView)

        analysisExecutor?.shutdown()
        val executor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "PretextCamera-Analysis").apply {
                priority = Thread.NORM_PRIORITY
            }
        }
        analysisExecutor = executor

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
            .also { it.surfaceProvider = previewView.surfaceProvider }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setResolutionSelector(resolutionSelector)
            .build()
            .also { it.setAnalyzer(executor, analyzer) }

        val useCaseGroup = UseCaseGroup.Builder()
            .setViewPort(viewport)
            .addUseCase(preview)
            .addUseCase(imageAnalysis)
            .build()

        provider.unbindAll()
        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        camera = provider.bindToLifecycle(lifecycleOwner, selector, useCaseGroup)
        camera?.cameraControl?.enableTorch(torchEnabled)
    }

    suspend fun flipCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        analyzer: ImageAnalysis.Analyzer,
    ) {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        torchEnabled = false
        bind(previewView, lifecycleOwner, analyzer)
    }

    fun toggleTorch(): Boolean {
        torchEnabled = !torchEnabled
        camera?.cameraControl?.enableTorch(torchEnabled)
        return torchEnabled
    }

    fun hasTorch(): Boolean = camera?.cameraInfo?.hasFlashUnit() == true

    fun close() {
        analysisExecutor?.shutdown()
        analysisExecutor = null
        runCatching { cameraProvider?.unbindAll() }
        camera = null
    }

    private suspend fun obtainProvider(): ProcessCameraProvider = suspendCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            { cont.resume(future.get()) },
            ContextCompat.getMainExecutor(context),
        )
    }.also { cameraProvider = it }

    private suspend fun awaitViewPort(previewView: PreviewView): ViewPort {
        previewView.viewPort?.let { return it }
        return suspendCoroutine { cont ->
            var resumed = false
            fun resumeOnce(vp: ViewPort) {
                if (!resumed) {
                    resumed = true
                    cont.resume(vp)
                }
            }
            fun resolve(): ViewPort? {
                previewView.viewPort?.let { return it }
                if (previewView.width <= 0 || previewView.height <= 0) return null
                val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
                val aspectRatio = Rational(previewView.width, previewView.height)
                return ViewPort.Builder(aspectRatio, rotation)
                    .setScaleType(ViewPort.FILL_CENTER)
                    .build()
            }
            fun tryResume() {
                resolve()?.let { resumeOnce(it) }
            }
            if (previewView.isAttachedToWindow && previewView.width > 0 && previewView.height > 0) {
                previewView.post { tryResume() }
            } else {
                previewView.addOnLayoutChangeListener(
                    object : View.OnLayoutChangeListener {
                        override fun onLayoutChange(
                            v: View?,
                            left: Int,
                            top: Int,
                            right: Int,
                            bottom: Int,
                            oldLeft: Int,
                            oldTop: Int,
                            oldRight: Int,
                            oldBottom: Int,
                        ) {
                            previewView.removeOnLayoutChangeListener(this)
                            tryResume()
                        }
                    },
                )
            }
        }
    }
}
