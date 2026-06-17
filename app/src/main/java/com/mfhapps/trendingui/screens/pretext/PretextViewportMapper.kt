package com.mfhapps.trendingui.screens.pretext

import android.graphics.RectF
import com.mfhapps.trendingui.core.text.PolygonObstacle


object PretextViewportMapper {

    data class ViewLayout(
        val analysisWidth: Int,
        val analysisHeight: Int,
        val viewWidth: Float,
        val viewHeight: Float,
        val mirrorX: Boolean,
    )

    fun mapContourToView(
        contour: VisionContour,
        layout: ViewLayout,
        isLiveDetection: Boolean,
    ): ViewShape {
        val boundsPx = mapNormRect(contour.boundsRectNorm(), layout)
        val polygonPx = contour.polygonNorm?.let { mapNormPolygon(it, layout) }
        return ViewShape(
            boundsPx = boundsPx,
            polygonPx = polygonPx,
            label = contour.label,
            source = contour.source,
            isLiveDetection = isLiveDetection,
        )
    }

    private fun fillCenterScale(layout: ViewLayout): Float {
        val aw = layout.analysisWidth.toFloat().coerceAtLeast(1f)
        val ah = layout.analysisHeight.toFloat().coerceAtLeast(1f)
        return maxOf(layout.viewWidth / aw, layout.viewHeight / ah)
    }

    private fun mapNormToView(xNorm: Float, yNorm: Float, layout: ViewLayout): Pair<Float, Float> {
        val scale = fillCenterScale(layout)
        val scaledW = layout.analysisWidth * scale
        val scaledH = layout.analysisHeight * scale
        val offsetX = (layout.viewWidth - scaledW) * 0.5f
        val offsetY = (layout.viewHeight - scaledH) * 0.5f
        var x = offsetX + xNorm.coerceIn(0f, 1f) * scaledW
        val y = offsetY + yNorm.coerceIn(0f, 1f) * scaledH
        if (layout.mirrorX) {
            x = layout.viewWidth - x
        }
        return x to y
    }

    private fun mapNormRect(norm: RectF, layout: ViewLayout): RectF {
        val (x0, y0) = mapNormToView(norm.left, norm.top, layout)
        val (x1, y1) = mapNormToView(norm.right, norm.bottom, layout)
        return RectF(
            minOf(x0, x1),
            minOf(y0, y1),
            maxOf(x0, x1),
            maxOf(y0, y1),
        )
    }

    private fun mapNormPolygon(
        norm: List<Pair<Float, Float>>,
        layout: ViewLayout,
        maxVertices: Int = 96,
    ): PolygonObstacle? {
        if (norm.size < 3) return null
        val pts = norm.map { (xn, yn) -> mapNormToView(xn, yn, layout) }
        return PolygonObstacle(downsample(pts, maxVertices.coerceIn(3, 96)))
    }

    private fun downsample(points: List<Pair<Float, Float>>, maxVertices: Int): List<Pair<Float, Float>> {
        if (points.size <= maxVertices) return points
        val step = points.size.toFloat() / maxVertices
        return List(maxVertices) { i ->
            points[(i * step).toInt().coerceIn(0, points.lastIndex)]
        }
    }
}
