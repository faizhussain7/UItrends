package com.mfhapps.trendingui.screens.pretext

import android.content.Context
import com.mfhapps.trendingui.util.traceSection
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.mfhapps.trendingui.native.PretextNativeVision
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@ExperimentalGetImage
internal class PretextVisionEngine(context: Context) {

    private val assets = context.assets
    private val maskScratch = ThreadLocal<FloatArray>()
    private val mediaPipeFaceLandmarker = PretextMediaPipeFaceLandmarker(context)

    private var selfieInterpreter: Interpreter? = null
    private var faceInterpreter: Interpreter? = null
    private var ssdInterpreter: Interpreter? = null
    private var ncnnReady = false

    init {
        ncnnReady = runCatching { PretextNativeVision.init(assets) }.getOrDefault(false)
    }

    fun close() {
        mediaPipeFaceLandmarker.close()
        selfieInterpreter?.close()
        faceInterpreter?.close()
        ssdInterpreter?.close()
        selfieInterpreter = null
        faceInterpreter = null
        ssdInterpreter = null
        if (ncnnReady) {
            PretextNativeVision.release()
        }
        ncnnReady = false
    }

    fun detect(
        imageProxy: ImageProxy,
        mode: VisionTrackMode,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    ): VisionDetectReport {
        val frame = frameMetrics(imageProxy)

        if (mode == VisionTrackMode.Object) {
            processNativeFrame(imageProxy, frame)?.let { return it }
            return VisionDetectReport(null, "native-yuv", note = "no-detection")
        }

        val rotation = frame.rotationDegrees
        val imageW = frame.analysisWidth
        val imageH = frame.analysisHeight

        return traceSection("pretext:rgb") {
            val rawRgb = PretextYuvConverter.toRgb888(imageProxy).copyOf()
            val (rgb, rgbW) = PretextYuvConverter.rotateRgb(rawRgb, imageProxy.width, imageProxy.height, rotation)
            val rgbH = if (rgbW == imageW) imageH else imageW
            when (mode) {
            VisionTrackMode.Person -> detectPerson(rgb, rgbW, rgbH, frame, lensFacing)
            VisionTrackMode.Face -> detectFace(rgb, rgbW, rgbH, frame)
            VisionTrackMode.Object -> detectObject(rgb, rgbW, rgbH, frame)
            VisionTrackMode.Auto -> detectAuto(rgb, rgbW, rgbH, frame, lensFacing)
            }
        }
    }

    private fun frameMetrics(imageProxy: ImageProxy): FrameMetrics {
        val rotation = imageProxy.imageInfo.rotationDegrees
        val w = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val h = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height
        return FrameMetrics(analysisWidth = w, analysisHeight = h, rotationDegrees = rotation)
    }

    private fun processNativeFrame(imageProxy: ImageProxy, frame: FrameMetrics): VisionDetectReport? {
        if (!ncnnReady) return null
        val planes = imageProxy.planes
        if (planes.size < 3) return null
        val y = planes[0]
        val u = planes[1]
        val v = planes[2]
        val packet = PretextNativeVision.processFrame(
            mode = 0,
            rotationDegrees = imageProxy.imageInfo.rotationDegrees,
            imageW = imageProxy.width,
            imageH = imageProxy.height,
            y = y.buffer,
            yRowStride = y.rowStride,
            u = u.buffer,
            uRowStride = u.rowStride,
            uPixelStride = u.pixelStride,
            v = v.buffer,
            vRowStride = v.rowStride,
            vPixelStride = v.pixelStride,
        ) ?: return VisionDetectReport(null, "native-yuv-ncnn", note = "empty-packet")
        val score = if (packet.size > 2) packet[2] else null
        val contour = VisionContour.fromNativeVisionPacket(packet, SSD_LABELS, frame)
            ?: return VisionDetectReport(null, "native-yuv-ncnn", note = "bad-packet")
        return VisionDetectReport(contour, "native-yuv-ncnn", score = score)
    }

    fun detectAutoMulti(
        imageProxy: ImageProxy,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        maxShapes: Int = 3,
    ): AutoDetectBundle {
        val frame = frameMetrics(imageProxy)
        val rotation = frame.rotationDegrees
        val imageW = frame.analysisWidth
        val imageH = frame.analysisHeight
        return traceSection("pretext:rgb") {
            val rawRgb = PretextYuvConverter.toRgb888(imageProxy).copyOf()
            val (rgb, rgbW) = PretextYuvConverter.rotateRgb(rawRgb, imageProxy.width, imageProxy.height, rotation)
            val rgbH = if (rgbW == imageW) imageH else imageW
            detectAutoMultiRgb(rgb, rgbW, rgbH, frame, lensFacing, maxShapes)
        }
    }

    data class AutoDetectBundle(
        val primary: VisionDetectReport,
        val extras: List<VisionDetectReport> = emptyList(),
    )

    private fun detectAutoMultiRgb(
        rgb: ByteArray,
        rgbW: Int,
        rgbH: Int,
        frame: FrameMetrics,
        lensFacing: Int,
        maxShapes: Int,
    ): AutoDetectBundle {
        val face = runCatching { detectFace(rgb, rgbW, rgbH, frame) }
            .getOrElse { VisionDetectReport(null, "mediapipe-face-landmarker", note = it.javaClass.simpleName) }
        val person = runCatching { detectPerson(rgb, rgbW, rgbH, frame, lensFacing) }
            .getOrElse { VisionDetectReport(null, "tflite-selfie", note = it.javaClass.simpleName) }
        val skipObject = PretextAutoSelector.pickPrimary(face, person, SKIPPED_OBJECT_REPORT) != null
        val obj = if (skipObject) {
            SKIPPED_OBJECT_REPORT
        } else {
            runCatching { detectObject(rgb, rgbW, rgbH, frame) }
                .getOrElse { VisionDetectReport(null, "tflite-ssd", note = it.javaClass.simpleName) }
        }
        val multi = PretextAutoSelector.pickMulti(face, person, obj, maxShapes)
        val primaryPick = multi.primary
        if (primaryPick != null) {
            val extras = multi.extras.map { it.report }
            return AutoDetectBundle(primaryPick.report, extras)
        }
        return AutoDetectBundle(
            VisionDetectReport(null, "auto", note = "no-pick"),
        )
    }

    private fun detectAuto(
        rgb: ByteArray,
        rgbW: Int,
        rgbH: Int,
        frame: FrameMetrics,
        lensFacing: Int,
    ): VisionDetectReport {
        val face = runCatching { detectFace(rgb, rgbW, rgbH, frame) }
            .getOrElse { VisionDetectReport(null, "mediapipe-face-landmarker", note = it.javaClass.simpleName) }
        val person = runCatching { detectPerson(rgb, rgbW, rgbH, frame, lensFacing) }
            .getOrElse { VisionDetectReport(null, "tflite-selfie", note = it.javaClass.simpleName) }
        PretextAutoSelector.pickPrimary(face, person, SKIPPED_OBJECT_REPORT)?.let { return it.report }
        val obj = runCatching { detectObject(rgb, rgbW, rgbH, frame) }
            .getOrElse { VisionDetectReport(null, "tflite-ssd", note = it.javaClass.simpleName) }
        val pick = PretextAutoSelector.pickPrimary(face, person, obj)
        return pick?.report ?: VisionDetectReport(null, "auto", note = "no-pick")
    }

    private fun detectPerson(
        rgb: ByteArray,
        rgbW: Int,
        rgbH: Int,
        frame: FrameMetrics,
        lensFacing: Int,
    ): VisionDetectReport {
        val mask = traceSection("pretext:tflite:selfie") {
            runSelfieSegmentation(rgb, rgbW, rgbH)
        } ?: return VisionDetectReport(null, "tflite-selfie", note = "mask-failed")
        return detectPersonFromMask(mask, frame, lensFacing)
    }

    private fun detectPersonFromMask(
        mask: MaskResult,
        frame: FrameMetrics,
        lensFacing: Int,
    ): VisionDetectReport {
        val plane = PretextPersonSegmentation.MaskPlane(mask.data, mask.width, mask.height)
        val stats = PretextPersonSegmentation.analyzeMask(plane)
        val score = max(stats.fgRatio, stats.centerFgRatio)

        if (!PretextPersonSegmentation.passesForegroundGate(stats, lensFacing)) {
            return VisionDetectReport(null, "tflite-selfie", score = score, note = "low-foreground")
        }

        val contour = traceSection("pretext:native:person") {
            PretextContourExtractor.fromMask(
                mask.data, mask.width, mask.height, frame.analysisWidth, frame.analysisHeight,
            )
        }

        val hadNativeContour = contour != null
        var vision = contour?.toVisionContour(VisionSource.Person, "person", frame)
        val hadVisionContour = vision != null
        if (vision != null) {
            vision = PretextPersonSegmentation.clampContourToReasonableSize(vision)
            val area = vision.boundsRectNorm().width() * vision.boundsRectNorm().height()
            if (area in MIN_PUBLISH_NORM_AREA..MAX_PUBLISH_NORM_AREA) {
                return VisionDetectReport(vision, "tflite-selfie+cpp-contour", score = score)
            }
            vision = null
        }

        val preferLoose = score >= 0.12f || PretextPersonSegmentation.isFrameFillBlob(stats)
        val blobContour = personBlobContourFallback(plane, frame, preferLoose = preferLoose)
        if (blobContour != null) {
            return VisionDetectReport(blobContour, "tflite-selfie+blob-contour", score = score, note = "blob-contour")
        }

        val note = when {
            PretextPersonSegmentation.isBlobTooSmall(stats) -> "blob-too-small"
            PretextPersonSegmentation.isFrameFillBlob(stats) -> "frame-fill"
            !hadNativeContour || !hadVisionContour -> "contour-failed"
            else -> "area-too-large"
        }
        return VisionDetectReport(null, "tflite-selfie", score = score, note = note)
    }

    private fun personBlobContourFallback(
        plane: PretextPersonSegmentation.MaskPlane,
        frame: FrameMetrics,
        preferLoose: Boolean = false,
    ): VisionContour? {
        val tightBlob = PretextPersonSegmentation.contourFromMaskBlobTight(plane, frame)
        val looseBlob = if (preferLoose) {
            PretextPersonSegmentation.contourFromMaskBlob(plane, frame)
        } else {
            null
        }
        val blob = tightBlob ?: looseBlob ?: return null
        val aw = frame.analysisWidth.toFloat().coerceAtLeast(1f)
        val ah = frame.analysisHeight.toFloat().coerceAtLeast(1f)
        val rounded = PretextContourExtractor.fromBox(
            left = blob.left * aw,
            top = blob.top * ah,
            right = blob.right * aw,
            bottom = blob.bottom * ah,
            imageW = frame.analysisWidth,
            imageH = frame.analysisHeight,
        )?.toVisionContour(VisionSource.Person, "person", frame) ?: return null
        val area = rounded.boundsRectNorm().width() * rounded.boundsRectNorm().height()
        return if (area in MIN_PUBLISH_NORM_AREA..MAX_PUBLISH_NORM_AREA) rounded else null
    }

    private fun detectFace(rgb: ByteArray, rgbW: Int, rgbH: Int, frame: FrameMetrics): VisionDetectReport {
        val mpLm = traceSection("pretext:mediapipe:face-landmarker") {
            mediaPipeFaceLandmarker.detect(rgb, rgbW, rgbH)
        }
        if (mpLm != null) {
            return buildFaceContourReport(
                polylinePx = mpLm.polylinePx,
                boxPx = null,
                score = mpLm.score,
                detectBackend = mpLm.backend,
                frame = frame,
            )
        }

        val fallback = traceSection("pretext:tflite:blazeface") {
            PretextBlazeFaceDecoder.detectWithScore(rgb, rgbW, rgbH, faceInterpreter())
        }
        if (fallback == null) {
            return VisionDetectReport(null, "mediapipe-face-landmarker", note = "no-face")
        }
        return buildFaceContourReport(
            polylinePx = null,
            boxPx = fallback.box,
            score = fallback.score,
            detectBackend = "tflite-blazeface",
            frame = frame,
        )
    }

    private fun buildFaceContourReport(
        polylinePx: FloatArray?,
        boxPx: FloatArray?,
        score: Float,
        detectBackend: String,
        frame: FrameMetrics,
    ): VisionDetectReport {
        val contour = traceSection("pretext:native:face") {
            when {
                polylinePx != null -> PretextContourExtractor.fromFacePolyline(
                    polylinePx,
                    frame.analysisWidth,
                    frame.analysisHeight,
                )
                boxPx != null -> PretextContourExtractor.fromFaceBox(
                    boxPx,
                    frame.analysisWidth,
                    frame.analysisHeight,
                )
                else -> null
            }
        }
        var vision = contour?.toVisionContour(VisionSource.Face, "face", frame)
            ?: return VisionDetectReport(null, detectBackend, score = score, note = "contour-failed")

        vision = PretextPersonSegmentation.clampFaceContour(vision)
        val area = vision.boundsRectNorm().width() * vision.boundsRectNorm().height()
        if (area < MIN_FACE_NORM_AREA) {
            return VisionDetectReport(null, detectBackend, score = score, note = "face-too-small")
        }
        if (area > MAX_FACE_PUBLISH_NORM_AREA) {
            return VisionDetectReport(null, detectBackend, score = score, note = "face-too-large")
        }
        return VisionDetectReport(
            contour = vision,
            backend = "$detectBackend+cpp-contour",
            score = score,
        )
    }

    private fun detectObject(rgb: ByteArray, rgbW: Int, rgbH: Int, frame: FrameMetrics): VisionDetectReport {
        if (ncnnReady) {
            val det = traceSection("pretext:ncnn:object") {
                PretextNativeVision.detectBestRgb(rgb, rgbW, rgbH, excludePerson = true)
            }
            if (det != null && det.size >= 6) {
                val classId = det[5].roundToInt()
                val label = PretextNativeVision.classLabel(classId)
                val score = det[4]
                val contour = traceSection("pretext:native:object") {
                    PretextContourExtractor.fromBox(
                        det[0], det[1], det[2], det[3], frame.analysisWidth, frame.analysisHeight,
                    )
                }
                val vision = contour?.toVisionContour(VisionSource.Object, label, frame)
                if (vision != null) {
                    return VisionDetectReport(vision, "ncnn+cpp-contour", score = score)
                }
            }
        }

        val ssd = traceSection("pretext:tflite:object") {
            runSsdDetection(rgb, rgbW, rgbH, excludePerson = true)
        } ?: return VisionDetectReport(null, "tflite-ssd", note = "no-object")
        val contour = traceSection("pretext:native:object") {
            PretextContourExtractor.fromBox(
                ssd.left, ssd.top, ssd.right, ssd.bottom, frame.analysisWidth, frame.analysisHeight,
            )
        }
        val vision = contour?.toVisionContour(VisionSource.Object, ssd.label, frame)
        return VisionDetectReport(
            contour = vision,
            backend = "tflite-ssd+cpp-contour",
            score = ssd.score,
            note = if (vision == null) "contour-failed" else null,
        )
    }

    private data class MaskResult(val data: FloatArray, val width: Int, val height: Int)
    private data class SsdBox(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val label: String,
        val score: Float,
    )

    private fun runSelfieSegmentation(rgb: ByteArray, width: Int, height: Int): MaskResult? {
        val interpreter = selfieInterpreter()
        val inTensor = interpreter.getInputTensor(0)
        val inShape = inTensor.shape()
        val inH = inShape.getOrNull(1) ?: inShape.getOrNull(2) ?: return null
        val inW = inShape.getOrNull(2) ?: inShape.getOrNull(3) ?: return null
        val isUint8 = inTensor.dataType() == DataType.UINT8

        val input = ByteBuffer.allocateDirect(tensorByteCount(inTensor))
            .order(ByteOrder.nativeOrder())

        for (y in 0 until inH) {
            val sy = y * height / inH
            for (x in 0 until inW) {
                val sx = x * width / inW
                val i = (sy * width + sx) * 3
                val r = rgb[i].toInt() and 0xff
                val g = rgb[i + 1].toInt() and 0xff
                val b = rgb[i + 2].toInt() and 0xff
                if (isUint8) {
                    input.put(r.toByte())
                    input.put(g.toByte())
                    input.put(b.toByte())
                } else {
                    input.putFloat(r / 255f)
                    input.putFloat(g / 255f)
                    input.putFloat(b / 255f)
                }
            }
        }
        input.rewind()

        val outTensor = interpreter.getOutputTensor(0)
        val outShape = outTensor.shape()
        val outIsUint8 = outTensor.dataType() == DataType.UINT8
        val outBuf = ByteBuffer.allocateDirect(tensorByteCount(outTensor))
            .order(ByteOrder.nativeOrder())
        interpreter.run(input, outBuf)
        outBuf.rewind()

        val outH: Int
        val outW: Int
        when (outShape.size) {
            4 -> {
                outH = outShape[1]
                outW = outShape[2]
            }
            3 -> {
                outH = outShape[1]
                outW = outShape[2]
            }
            else -> return null
        }

        val count = outW * outH
        val floats = maskScratch.get()?.takeIf { it.size == count }
            ?: FloatArray(count).also { maskScratch.set(it) }

        val channels = if (outShape.size == 4) outShape[3] else 1
        for (y in 0 until outH) {
            for (x in 0 until outW) {
                val v = when {
                    channels > 1 && outIsUint8 -> {
                        outBuf.get()
                        (outBuf.get().toInt() and 0xff) / 255f
                    }
                    channels > 1 -> {
                        outBuf.float
                        outBuf.float
                    }
                    outIsUint8 -> (outBuf.get().toInt() and 0xff) / 255f
                    else -> outBuf.float
                }
                floats[y * outW + x] = v.coerceIn(0f, 1f)
            }
        }
        return MaskResult(floats, outW, outH)
    }

    private fun runSsdDetection(rgb: ByteArray, width: Int, height: Int, excludePerson: Boolean): SsdBox? {
        val interpreter = ssdInterpreter()
        val inTensor = interpreter.getInputTensor(0)
        val inShape = inTensor.shape()
        val inH = inShape[1]
        val inW = inShape[2]
        val isUint8 = inTensor.dataType() == DataType.UINT8

        val input = ByteBuffer.allocateDirect(tensorByteCount(inTensor))
            .order(ByteOrder.nativeOrder())

        for (y in 0 until inH) {
            val sy = y * height / inH
            for (x in 0 until inW) {
                val sx = x * width / inW
                val i = (sy * width + sx) * 3
                val r = rgb[i].toInt() and 0xff
                val g = rgb[i + 1].toInt() and 0xff
                val b = rgb[i + 2].toInt() and 0xff
                if (isUint8) {
                    input.put(r.toByte())
                    input.put(g.toByte())
                    input.put(b.toByte())
                } else {
                    input.putFloat(r / 255f)
                    input.putFloat(g / 255f)
                    input.putFloat(b / 255f)
                }
            }
        }
        input.rewind()

        val boxes = Array(1) { Array(10) { FloatArray(4) } }
        val classes = Array(1) { FloatArray(10) }
        val scores = Array(1) { FloatArray(10) }
        val count = Array(1) { FloatArray(1) }
        val outputMap = mutableMapOf<Int, Any>()
        for (i in 0 until interpreter.outputTensorCount) {
            val name = interpreter.getOutputTensor(i).name()?.lowercase() ?: ""
            when {
                name.contains("box") || name.contains("location") -> outputMap[i] = boxes
                name.contains("class") -> outputMap[i] = classes
                name.contains("score") -> outputMap[i] = scores
                name.contains("num") -> outputMap[i] = count
            }
        }
        if (outputMap.size < 3) {
            outputMap[0] = boxes
            outputMap[1] = classes
            outputMap[2] = scores
            outputMap[3] = count
        }
        interpreter.runForMultipleInputsOutputs(arrayOf(input), outputMap)

        val n = min(10, count[0][0].roundToInt().coerceAtLeast(0))
        var bestIdx = -1
        var bestScore = 0.25f
        for (i in 0 until n) {
            val classId = classes[0][i].roundToInt().coerceIn(0, SSD_LABELS.lastIndex)
            if (excludePerson && classId == 0) continue
            if (scores[0][i] > bestScore) {
                bestScore = scores[0][i]
                bestIdx = i
            }
        }
        if (bestIdx < 0) return null

        val b = boxes[0][bestIdx]
        val classId = classes[0][bestIdx].roundToInt().coerceIn(0, SSD_LABELS.lastIndex)
        return SsdBox(
            left = b[1] * width,
            top = b[0] * height,
            right = b[3] * width,
            bottom = b[2] * height,
            label = SSD_LABELS[classId],
            score = bestScore,
        )
    }

    private fun selfieInterpreter(): Interpreter =
        selfieInterpreter ?: loadModel("vision/selfie_segmentation.tflite", 2).also { selfieInterpreter = it }

    private fun faceInterpreter(): Interpreter =
        faceInterpreter ?: loadModel("vision/blaze_face_short_range.tflite", 2).also { faceInterpreter = it }

    private fun ssdInterpreter(): Interpreter =
        ssdInterpreter ?: loadModel("vision/ssd_mobilenet_coco.tflite", 2).also { ssdInterpreter = it }

    private fun loadModel(assetPath: String, threads: Int): Interpreter {
        val buffer = loadAsset(assetPath)
        val opts = Interpreter.Options().apply { setNumThreads(threads) }
        return Interpreter(buffer, opts)
    }

    private fun loadAsset(path: String): MappedByteBuffer {
        assets.openFd(path).use { fd ->
            FileInputStream(fd.fileDescriptor).channel.use { channel ->
                return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
            }
        }
    }

    private fun tensorByteCount(tensor: Tensor): Int {
        val n = tensor.numElements()
        return when (tensor.dataType()) {
            DataType.UINT8 -> n
            DataType.FLOAT32 -> n * 4
            DataType.INT32 -> n * 4
            else -> n * 4
        }
    }

    companion object {
        private const val MIN_PUBLISH_NORM_AREA = 0.006f
        private const val MAX_PUBLISH_NORM_AREA = 0.78f
        private const val MIN_FACE_NORM_AREA = 0.008f
        private const val MAX_FACE_PUBLISH_NORM_AREA = 0.14f

        private val SKIPPED_OBJECT_REPORT = VisionDetectReport(null, "auto-skip", note = "object-skipped")

        private val SSD_LABELS = arrayOf(
            "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
            "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
            "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
            "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
            "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
            "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair",
            "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse",
            "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator",
            "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush",
        )
    }
}
