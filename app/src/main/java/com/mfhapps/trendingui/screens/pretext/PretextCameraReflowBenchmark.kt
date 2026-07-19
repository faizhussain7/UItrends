package com.mfhapps.trendingui.screens.pretext

import android.graphics.Typeface
import com.mfhapps.trendingui.core.text.LayoutRegion
import com.mfhapps.trendingui.core.text.PositionedTextLayout
import com.mfhapps.trendingui.core.text.PreparedText
import kotlin.system.measureNanoTime

/**
 * One camera reflow pass input. Shared by Engine and View.measure backends so
 * speed samples compare the same page geometry, style, and obstacle set.
 */
data class CameraReflowRequest(
    val prepared: PreparedText?,
    val sourceText: String,
    val fontSizePx: Float,
    val typeface: Typeface,
    val shapes: List<ViewShape>,
    val region: LayoutRegion,
    val lineHeightPx: Float,
    val style: PretextCameraTextLayoutStyle,
    val pageWidthPx: Float,
    val pageHeightPx: Float,
    val paddingPx: Float,
    val headlinePrepared: PreparedText? = null,
    val bodyPrepared: PreparedText? = null,
)

/**
 * Dual-backend timing sample for one reflow tick.
 *
 * [engineNs] / [viewNs] are wall time of [PretextCameraReflowScheduler.computeLayout]
 * for each [PretextMeasureMode] on the same [CameraReflowRequest].
 */
data class PretextMeasureSpeed(
    val engineNs: Long = 0L,
    val viewNs: Long = 0L,
) {
    val fastest: PretextMeasureMode? get() = when {
        engineNs <= 0L || viewNs <= 0L -> null
        engineNs <= viewNs -> PretextMeasureMode.Engine
        else -> PretextMeasureMode.ViewMeasure
    }

    val slowest: PretextMeasureMode? get() = when (fastest) {
        PretextMeasureMode.Engine -> PretextMeasureMode.ViewMeasure
        PretextMeasureMode.ViewMeasure -> PretextMeasureMode.Engine
        null -> null
    }

    fun nanosFor(mode: PretextMeasureMode): Long = when (mode) {
        PretextMeasureMode.Engine -> engineNs
        PretextMeasureMode.ViewMeasure -> viewNs
    }

    fun format(mode: PretextMeasureMode): String = formatNanos(nanosFor(mode))

    fun speedTag(mode: PretextMeasureMode): String? = when (mode) {
        fastest -> "fast"
        slowest -> "slow"
        else -> null
    }

    val comparisonLabel: String
        get() {
            if (engineNs <= 0L || viewNs <= 0L) return ""
            val fast = fastest ?: return ""
            val slow = slowest ?: return ""
            val ratio = nanosFor(slow).toDouble() / nanosFor(fast).toDouble()
            return "${fast.shortLabel} ${formatNanos(nanosFor(fast))} (fast) · " +
                "${slow.shortLabel} ${formatNanos(nanosFor(slow))} (slow) · " +
                "${"%.0f".format(ratio)}×"
        }

    /**
     * Exponential moving average so chip/segment labels stay readable under
     * frame-to-frame jitter instead of flashing raw single-shot timings.
     */
    fun blend(sample: PretextMeasureSpeed, alpha: Float = 0.28f): PretextMeasureSpeed {
        fun blendNs(previous: Long, next: Long): Long {
            if (previous <= 0L) return next
            if (next <= 0L) return previous
            return (previous * (1f - alpha) + next * alpha).toLong()
        }
        return PretextMeasureSpeed(
            engineNs = blendNs(engineNs, sample.engineNs),
            viewNs = blendNs(viewNs, sample.viewNs),
        )
    }

    companion object {
        fun formatNanos(nanos: Long): String {
            if (nanos <= 0L) return "—"
            val us = nanos / 1_000.0
            return if (us >= 1000.0) {
                "%.2fms".format(us / 1000.0)
            } else {
                "%.0fµs".format(us)
            }
        }
    }
}

data class CameraReflowBenchmarkResult(
    val layout: PositionedTextLayout,
    val sample: PretextMeasureSpeed,
)

/**
 * Runs Engine and View.measure on the same request, times each, and returns the
 * layout for the active mode. This is the only place dual-backend cost is sampled
 * so UI chips and sheet segments stay consistent.
 */
object PretextCameraReflowBenchmark {

    fun run(
        request: CameraReflowRequest,
        activeMode: PretextMeasureMode,
    ): CameraReflowBenchmarkResult {
        val (engineLayout, engineNs) = timed(PretextMeasureMode.Engine, request)
        val (viewLayout, viewNs) = timed(PretextMeasureMode.ViewMeasure, request)
        val layout = when (activeMode) {
            PretextMeasureMode.Engine -> engineLayout
            PretextMeasureMode.ViewMeasure -> viewLayout
        }
        return CameraReflowBenchmarkResult(
            layout = layout,
            sample = PretextMeasureSpeed(engineNs = engineNs, viewNs = viewNs),
        )
    }

    private fun timed(
        mode: PretextMeasureMode,
        request: CameraReflowRequest,
    ): Pair<PositionedTextLayout, Long> {
        var layout = PositionedTextLayout(lines = emptyList(), height = 0)
        val nanos = measureNanoTime {
            layout = PretextCameraReflowScheduler.computeLayout(
                measureMode = mode,
                prepared = request.prepared,
                sourceText = request.sourceText,
                fontSizePx = request.fontSizePx,
                typeface = request.typeface,
                shapes = request.shapes,
                region = request.region,
                lineHeightPx = request.lineHeightPx,
                style = request.style,
                pageWidthPx = request.pageWidthPx,
                pageHeightPx = request.pageHeightPx,
                paddingPx = request.paddingPx,
                headlinePrepared = request.headlinePrepared,
                bodyPrepared = request.bodyPrepared,
            )
        }
        return layout to nanos
    }
}
