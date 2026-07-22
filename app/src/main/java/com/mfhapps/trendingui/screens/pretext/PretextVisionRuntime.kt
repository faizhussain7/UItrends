package com.mfhapps.trendingui.screens.pretext

import android.content.Context
import android.content.res.AssetManager
import com.mfhapps.trendingui.native.PretextNativeVision
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

internal class PretextVisionRuntime(context: Context) {

    val assets: AssetManager = context.assets
    val interpreterLock = Any()
    val maskScratch = ThreadLocal<FloatArray>()
    val mediaPipeFaceLandmarker = PretextMediaPipeFaceLandmarker(context)

    @Volatile var closed = false
        private set

    @Volatile var ncnnReady = false
        internal set

    val isNcnnReady: Boolean get() = ncnnReady

    var selfieInterpreter: Interpreter? = null
        private set
    var faceInterpreter: Interpreter? = null
        private set

    var selfieLoadFailed = false
    var faceLoadFailed = false

    @Volatile var objectHighQuality: Boolean = false

    init {
        ncnnReady = runCatching { PretextNativeVision.init(assets, highQuality = objectHighQuality) }.getOrDefault(false)
    }

    fun reloadNcnn(highQuality: Boolean = objectHighQuality): Boolean {
        objectHighQuality = highQuality
        if (ncnnReady) {
            PretextNativeVision.release()
            ncnnReady = false
        }
        ncnnReady = runCatching { PretextNativeVision.init(assets, highQuality = objectHighQuality) }.getOrDefault(false)
        return ncnnReady
    }

    fun close() {
        synchronized(interpreterLock) {
            if (closed) return
            closed = true
            mediaPipeFaceLandmarker.close()
            selfieInterpreter?.close()
            faceInterpreter?.close()
            selfieInterpreter = null
            faceInterpreter = null
            if (ncnnReady) {
                PretextNativeVision.release()
            }
            ncnnReady = false
        }
    }

    fun selfieInterpreter(): Interpreter? {
        if (selfieLoadFailed) return null
        selfieInterpreter?.let { return it }
        return runCatching { loadModel("vision/selfie_segmentation.tflite", 2) }
            .onFailure { selfieLoadFailed = true }
            .getOrNull()
            ?.also { selfieInterpreter = it }
    }

    fun faceInterpreter(): Interpreter? {
        if (faceLoadFailed) return null
        faceInterpreter?.let { return it }
        return runCatching { loadModel("vision/blaze_face_short_range.tflite", 2) }
            .onFailure { faceLoadFailed = true }
            .getOrNull()
            ?.also { faceInterpreter = it }
    }

    private fun loadModel(assetPath: String, threads: Int): Interpreter {
        val buffer = loadAsset(assetPath)
        val opts = Interpreter.Options().apply { setNumThreads(threads.coerceIn(1, 2)) }
        val interpreter = Interpreter(buffer, opts)
        require(interpreter.inputTensorCount > 0) { "Model has no inputs: $assetPath" }
        return interpreter
    }

    private fun loadAsset(path: String): MappedByteBuffer {
        assets.openFd(path).use { fd ->
            FileInputStream(fd.fileDescriptor).channel.use { channel ->
                return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
            }
        }
    }

    fun tensorByteCount(tensor: Tensor): Int {
        val n = tensor.numElements()
        return when (tensor.dataType()) {
            DataType.UINT8 -> n
            DataType.FLOAT32 -> n * 4
            DataType.INT32 -> n * 4
            else -> n * 4
        }
    }
}
