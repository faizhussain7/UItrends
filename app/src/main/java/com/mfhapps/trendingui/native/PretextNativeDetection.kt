package com.mfhapps.trendingui.native

data class PretextNativeDetection(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val score: Float,
    val classId: Int,
    val polygonNorm: List<Pair<Float, Float>> = emptyList(),
) {
    val hasMaskContour: Boolean get() = polygonNorm.size >= 3
}
