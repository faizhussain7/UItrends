package com.mfhapps.trendingui.screens.pretext

import android.graphics.RectF
import com.mfhapps.trendingui.core.text.PolygonObstacle
import com.mfhapps.trendingui.core.text.RectObstacle

enum class VisionTrackMode(val label: String) {
    Person("Full body"),
    Face("Face"),
    Object("Object"),
    Auto("Auto"),
}

enum class VisionSource(val label: String) {
    Face("Face"),
    Person("Person"),
    Object("Object"),
    Manual("Manual"),
    Idle("Scanning"),
}

data class FrameMetrics(
    val analysisWidth: Int,
    val analysisHeight: Int,
    val rotationDegrees: Int,
)

data class VisionContour(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val polygonNorm: List<Pair<Float, Float>>? = null,
    val label: String?,
    val source: VisionSource,
    val frame: FrameMetrics,
) {
    fun boundsRectNorm(): RectF = RectF(left, top, right, bottom)

    companion object {
        fun fromCenterNorm(
            cx: Float,
            cy: Float,
            widthFrac: Float,
            heightFrac: Float,
            source: VisionSource,
            frame: FrameMetrics,
            label: String? = null,
        ): VisionContour {
            val halfW = widthFrac * 0.5f
            val halfH = heightFrac * 0.5f
            return VisionContour(
                left = (cx - halfW).coerceIn(0f, 1f - widthFrac),
                top = (cy - halfH).coerceIn(0f, 1f - heightFrac),
                right = (cx + halfW).coerceIn(widthFrac, 1f),
                bottom = (cy + halfH).coerceIn(heightFrac, 1f),
                label = label,
                source = source,
                frame = frame,
            )
        }

        fun fromNativeVisionPacket(
            packet: FloatArray,
            objectLabels: Array<String>,
            frame: FrameMetrics,
        ): VisionContour? {
            if (packet.size < 8) return null
            val sourceId = packet[0].toInt().coerceAtLeast(0)
            val classId = packet[1].toInt().coerceAtLeast(0)
            val left = packet[3].coerceIn(0f, 1f)
            val top = packet[4].coerceIn(0f, 1f)
            val right = packet[5].coerceIn(0f, 1f)
            val bottom = packet[6].coerceIn(0f, 1f)
            val nPts = packet[7].toInt()
            if (nPts < 3 || packet.size < 8 + nPts * 2) return null
            val poly = ArrayList<Pair<Float, Float>>(nPts)
            for (i in 0 until nPts) {
                poly += packet[8 + i * 2].coerceIn(0f, 1f) to packet[8 + i * 2 + 1].coerceIn(0f, 1f)
            }
            val source = when (sourceId) {
                2 -> VisionSource.Object
                0 -> VisionSource.Face
                else -> VisionSource.Person
            }
            val label = if (source == VisionSource.Object) {
                objectLabels.getOrElse(classId) { "object" }
            } else {
                source.label.lowercase()
            }
            return VisionContour(
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                polygonNorm = poly,
                label = label,
                source = source,
                frame = frame,
            )
        }
    }
}

fun generativeOrbAt(viewW: Float, viewH: Float, phase: Float): ViewShape {
    val cx = viewW * (0.35f + 0.28f * kotlin.math.sin(phase * Math.PI * 2).toFloat())
    val cy = viewH * (0.48f + 0.22f * kotlin.math.cos(phase * Math.PI * 2).toFloat())
    return editorialOrbShapeAt(cx, cy, viewW, viewH, radiusFrac = 0.11f)
}

fun editorialOrbShapeAt(
    xPx: Float,
    yPx: Float,
    viewW: Float,
    viewH: Float,
    radiusFrac: Float = 0.09f,
): ViewShape {
    val r = minOf(viewW, viewH) * radiusFrac
    val bounds = RectF(
        (xPx - r).coerceIn(0f, viewW - r * 2f),
        (yPx - r).coerceIn(0f, viewH - r * 2f),
        (xPx + r).coerceIn(r * 2f, viewW),
        (yPx + r).coerceIn(r * 2f, viewH),
    )
    val cx = bounds.centerX()
    val cy = bounds.centerY()
    val verts = 16
    val pts = List(verts) { i ->
        val a = (i.toFloat() / verts) * (Math.PI * 2).toFloat()
        cx + kotlin.math.cos(a) * r to cy + kotlin.math.sin(a) * r
    }
    return ViewShape(
        boundsPx = bounds,
        polygonPx = PolygonObstacle(pts),
        label = "orb",
        source = VisionSource.Manual,
        isLiveDetection = false,
    )
}

fun manualViewShapeAt(
    xPx: Float,
    yPx: Float,
    viewW: Float,
    viewH: Float,
): ViewShape {
    val w = viewW * 0.22f
    val h = viewH * 0.45f
    val bounds = RectF(
        (xPx - w * 0.5f).coerceIn(0f, viewW - w),
        (yPx - h * 0.5f).coerceIn(0f, viewH - h),
        (xPx + w * 0.5f).coerceIn(w, viewW),
        (yPx + h * 0.5f).coerceIn(h, viewH),
    )
    return ViewShape(
        boundsPx = bounds,
        polygonPx = null,
        label = null,
        source = VisionSource.Manual,
        isLiveDetection = true,
    )
}

data class ViewShape(
    val boundsPx: RectF,
    val polygonPx: PolygonObstacle?,
    val label: String?,
    val source: VisionSource,
    val isLiveDetection: Boolean,
) {
    fun toRectObstacle(): RectObstacle = RectObstacle(
        x = boundsPx.left,
        y = boundsPx.top,
        w = (boundsPx.width()).coerceAtLeast(4f),
        h = (boundsPx.height()).coerceAtLeast(4f),
    )
}

internal fun VisionContour.isCoarseBoxFallback(): Boolean =
    (polygonNorm?.size ?: 0) <= 4

internal fun VisionDetectReport.isBlobBoxFallback(): Boolean =
    note in BLOB_BOX_NOTES || (contour?.isCoarseBoxFallback() == true)

private val BLOB_BOX_NOTES = setOf(
    "frame-fill-clamped",
    "bbox-fallback",
    "area-clamped",
)


data class VisionDetectReport(
    val contour: VisionContour?,
    val backend: String,
    val score: Float? = null,
    val note: String? = null,
    val autoPickSource: VisionSource? = null,
    val autoPickReason: String? = null,
) {
    val hit: Boolean get() = contour != null
}

data class CameraVisionFrame(
    val primary: ViewShape?,
    val extraShapes: List<ViewShape> = emptyList(),
    val report: VisionDetectReport,
)

enum class ShapeTrackingState {
    Live,
    Held,
    Lost,
    Manual,
}

data class VisionAccuracySnapshot(
    val tracking: ShapeTrackingState,
    val backend: String?,
    val score: Float?,
    val normBBoxArea: Float,
    val viewBBoxAreaRatio: Float,
    val polygonVertices: Int,
    val boundsFullyInView: Boolean,
    val iouVsPrevious: Float?,
    val centerDriftNorm: Float?,
    val detectMs: Long,
    val mapMs: Long,
    val note: String? = null,
)

data class VisionTelemetry(
    val processedFps: Float = 0f,
    val droppedFrames: Int = 0,
    val lastSource: VisionSource = VisionSource.Idle,
    val detectHitRate: Float = 0f,
    val avgNormArea: Float = 0f,
    val avgIouStability: Float = 0f,
    val lastAccuracy: VisionAccuracySnapshot? = null,
    val lastBackend: String? = null,
)
