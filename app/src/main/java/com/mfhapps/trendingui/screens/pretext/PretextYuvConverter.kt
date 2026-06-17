package com.mfhapps.trendingui.screens.pretext

import androidx.camera.core.ImageProxy
import kotlin.math.max
import kotlin.math.min


internal object PretextYuvConverter {

    private var rgbScratch: ByteArray? = null

    fun toRgb888(image: ImageProxy): ByteArray {
        val width = image.width
        val height = image.height
        val size = width * height * 3
        val rgb = rgbScratch?.takeIf { it.size == size } ?: ByteArray(size).also { rgbScratch = it }

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val yBuf = yPlane.buffer.duplicate().apply { rewind() }
        val uBuf = uPlane.buffer.duplicate().apply { rewind() }
        val vBuf = vPlane.buffer.duplicate().apply { rewind() }

        val yRow = yPlane.rowStride
        val uvRow = uPlane.rowStride
        val uvPixel = uPlane.pixelStride

        var o = 0
        for (j in 0 until height) {
            val yRowStart = j * yRow
            val uvRowStart = (j shr 1) * uvRow
            for (i in 0 until width) {
                val y = (yBuf.get(yRowStart + i).toInt() and 0xff) - 16
                val uvIndex = uvRowStart + (i shr 1) * uvPixel
                val u = (uBuf.get(uvIndex).toInt() and 0xff) - 128
                val v = (vBuf.get(uvIndex).toInt() and 0xff) - 128
                val y2 = max(0, y) * 1192
                val r = clamp((y2 + 1634 * v) shr 10)
                val g = clamp((y2 - 833 * v - 400 * u) shr 10)
                val b = clamp((y2 + 2066 * u) shr 10)
                rgb[o++] = r.toByte()
                rgb[o++] = g.toByte()
                rgb[o++] = b.toByte()
            }
        }
        return rgb
    }


    fun rotateRgb(rgb: ByteArray, width: Int, height: Int, rotationDegrees: Int): Pair<ByteArray, Int> {
        return when (rotationDegrees) {
            90 -> {
                val out = ByteArray(rgb.size)
                var o = 0
                for (x in 0 until width) {
                    for (y in height - 1 downTo 0) {
                        val i = (y * width + x) * 3
                        out[o++] = rgb[i]
                        out[o++] = rgb[i + 1]
                        out[o++] = rgb[i + 2]
                    }
                }
                out to height
            }
            180 -> {
                val out = ByteArray(rgb.size)
                var o = 0
                for (y in height - 1 downTo 0) {
                    for (x in width - 1 downTo 0) {
                        val i = (y * width + x) * 3
                        out[o++] = rgb[i]
                        out[o++] = rgb[i + 1]
                        out[o++] = rgb[i + 2]
                    }
                }
                out to width
            }
            270 -> {
                val out = ByteArray(rgb.size)
                var o = 0
                for (x in width - 1 downTo 0) {
                    for (y in 0 until height) {
                        val i = (y * width + x) * 3
                        out[o++] = rgb[i]
                        out[o++] = rgb[i + 1]
                        out[o++] = rgb[i + 2]
                    }
                }
                out to height
            }
            else -> rgb to width
        }
    }

    private fun clamp(v: Int): Int = min(255, max(0, v))
}
