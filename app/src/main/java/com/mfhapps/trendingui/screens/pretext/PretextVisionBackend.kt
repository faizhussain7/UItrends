package com.mfhapps.trendingui.screens.pretext

internal interface PretextVisionBackend {
    val source: VisionSource
    val backendLabel: String

    fun detect(frame: PretextVisionFrame): VisionDetectReport

    fun detectMulti(frame: PretextVisionFrame, maxInstances: Int): List<VisionDetectReport> {
        val single = detect(frame)
        return if (single.contour != null) listOf(single) else emptyList()
    }
}
