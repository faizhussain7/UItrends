package com.mfhapps.trendingui.screens.pretext

import androidx.camera.core.ImageProxy

internal data class PretextVisionFrame(
    val rgb: ByteArray,
    val rgbWidth: Int,
    val rgbHeight: Int,
    val frame: FrameMetrics,
    val imageProxy: ImageProxy?,
    val lensFacing: Int,
)
