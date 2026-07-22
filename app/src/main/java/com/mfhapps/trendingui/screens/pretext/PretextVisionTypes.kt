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
    val w = viewW * 0.26f
    val h = viewH * 0.52f
    val left = (xPx - w * 0.5f).coerceIn(0f, viewW - w)
    val top = (yPx - h * 0.5f).coerceIn(0f, viewH - h)
    val bounds = RectF(left, top, left + w, top + h)
    val cx = bounds.centerX()
    val cy = bounds.centerY()
    val halfW = bounds.width() * 0.5f
    val halfH = bounds.height() * 0.5f

    val pts = listOf(
        cx to (cy - halfH),
        cx + halfW * 0.28f to (cy - halfH * 0.78f),
        cx + halfW * 0.38f to (cy - halfH * 0.52f),
        cx + halfW * 0.92f to (cy - halfH * 0.20f),
        cx + halfW * 0.72f to (cy - halfH * 0.02f),
        cx + halfW * 0.58f to (cy + halfH * 0.28f),
        cx + halfW * 0.42f to (cy + halfH * 0.62f),
        cx + halfW * 0.22f to (cy + halfH * 0.96f),
        cx to (cy + halfH * 0.88f),
        cx - halfW * 0.22f to (cy + halfH * 0.96f),
        cx - halfW * 0.42f to (cy + halfH * 0.62f),
        cx - halfW * 0.58f to (cy + halfH * 0.28f),
        cx - halfW * 0.72f to (cy - halfH * 0.02f),
        cx - halfW * 0.92f to (cy - halfH * 0.20f),
        cx - halfW * 0.38f to (cy - halfH * 0.52f),
        cx - halfW * 0.28f to (cy - halfH * 0.78f),
    )

    return ViewShape(
        boundsPx = bounds,
        polygonPx = PolygonObstacle(pts),
        label = "manual",
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
    val instanceId: Int = -1,
) {
    fun toRectObstacle(): RectObstacle = RectObstacle(
        x = boundsPx.left,
        y = boundsPx.top,
        w = (boundsPx.width()).coerceAtLeast(4f),
        h = (boundsPx.height()).coerceAtLeast(4f),
    )
}


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
    val shapeQuality: Float = 0f,
    val polygonFillRatio: Float = 0f,
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
    val ncnnReady: Boolean = false,
)
