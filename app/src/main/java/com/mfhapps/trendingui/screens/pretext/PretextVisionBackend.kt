package com.mfhapps.trendingui.screens.pretext

internal interface PretextVisionBackend {
    val source: VisionSource
    val backendLabel: String

    fun detect(frame: PretextVisionFrame): VisionDetectReport
}
