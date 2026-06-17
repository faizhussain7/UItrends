package com.mfhapps.trendingui.screens.pretext

import com.mfhapps.trendingui.native.NativeContour
import com.mfhapps.trendingui.native.PretextNativeGeometry

internal object PretextContourExtractor {

    fun fromMask(
        mask: FloatArray,
        maskW: Int,
        maskH: Int,
        imageW: Int,
        imageH: Int,
    ): NativeContour? = PretextNativeGeometry.extractPerson(
        mask = mask,
        maskW = maskW,
        maskH = maskH,
        landmarks = null,
        imageW = imageW,
        imageH = imageH,
    )

    fun fromFacePolyline(polylinePx: FloatArray, imageW: Int, imageH: Int): NativeContour? {
        if (polylinePx.size < 12) return null
        return PretextNativeGeometry.extractFace(
            polylineXy = polylinePx,
            cx = 0f,
            cy = 0f,
            rx = 0f,
            ry = 0f,
            imageW = imageW,
            imageH = imageH,
        )
    }

    fun fromFaceBox(box: FloatArray, imageW: Int, imageH: Int): NativeContour? {
        val cx = (box[0] + box[2]) * 0.5f
        val cy = (box[1] + box[3]) * 0.5f
        val rx = (box[2] - box[0]) * 0.46f
        val ry = (box[3] - box[1]) * 0.54f
        return PretextNativeGeometry.extractFace(
            polylineXy = null,
            cx = cx,
            cy = cy,
            rx = rx,
            ry = ry,
            imageW = imageW,
            imageH = imageH,
        )
    }

    fun fromBox(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        imageW: Int,
        imageH: Int,
    ): NativeContour? = PretextNativeGeometry.extractObject(
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        imageW = imageW,
        imageH = imageH,
    )
}
