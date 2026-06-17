package com.mfhapps.trendingui.screens.pretext

import android.view.View
import androidx.camera.view.PreviewView
import java.util.concurrent.atomic.AtomicReference


class PretextPreviewLayoutCache {

    private data class ViewSize(
        val width: Float,
        val height: Float,
        val mirrorX: Boolean,
    )

    private val viewSizeRef = AtomicReference<ViewSize?>(null)
    private var attachedView: PreviewView? = null
    private var mirrorX: Boolean = false

    private val layoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        attachedView?.let { publish(it) }
    }

    fun attach(previewView: PreviewView, mirrorX: Boolean) {
        this.mirrorX = mirrorX
        if (attachedView === previewView) {
            previewView.post { publish(previewView) }
            return
        }
        detach()
        attachedView = previewView
        previewView.addOnLayoutChangeListener(layoutListener)
        previewView.post { publish(previewView) }
    }

    fun setMirrorX(mirrorX: Boolean) {
        if (this.mirrorX == mirrorX) return
        this.mirrorX = mirrorX
        attachedView?.let { view -> view.post { publish(view) } }
    }

    fun detach() {
        attachedView?.removeOnLayoutChangeListener(layoutListener)
        attachedView = null
        viewSizeRef.set(null)
    }

    fun current(contour: VisionContour): PretextViewportMapper.ViewLayout? {
        val size = viewSizeRef.get()
        if (size != null && size.width > 0f && size.height > 0f) {
            return PretextViewportMapper.ViewLayout(
                analysisWidth = contour.frame.analysisWidth,
                analysisHeight = contour.frame.analysisHeight,
                viewWidth = size.width,
                viewHeight = size.height,
                mirrorX = size.mirrorX,
            )
        }
        val view = attachedView ?: return null
        if (view.width <= 0 || view.height <= 0) return null
        return PretextViewportMapper.ViewLayout(
            analysisWidth = contour.frame.analysisWidth,
            analysisHeight = contour.frame.analysisHeight,
            viewWidth = view.width.toFloat(),
            viewHeight = view.height.toFloat(),
            mirrorX = mirrorX,
        )
    }

    private fun publish(previewView: PreviewView) {
        if (previewView.width <= 0 || previewView.height <= 0) return
        viewSizeRef.set(
            ViewSize(
                width = previewView.width.toFloat(),
                height = previewView.height.toFloat(),
                mirrorX = mirrorX,
            ),
        )
    }
}
