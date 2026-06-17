package com.mfhapps.trendingui.ui.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.roundToInt

@Composable
fun rememberControlHaptics(): ControlHaptics {
    val feedback = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    return ControlHaptics(feedback = feedback, reduceMotion = reduceMotion)
}

class ControlHaptics internal constructor(
    private val feedback: HapticFeedback,
    private val reduceMotion: Boolean,
) {
    fun onToggle() {
        if (reduceMotion) return
        feedback.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    fun onSliderStep(previousStep: Int, nextStep: Int) {
        if (reduceMotion || previousStep == nextStep) return
        feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }

    fun onSliderFinished() {
        if (reduceMotion) return
        feedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
    }
}

fun sliderStepIndex(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
): Int {
    if (steps <= 0) {
        val span = valueRange.endInclusive - valueRange.start
        if (span <= 0f) return 0
        val normalized = ((value - valueRange.start) / span).coerceIn(0f, 1f)
        return (normalized * 24f).roundToInt()
    }
    val span = valueRange.endInclusive - valueRange.start
    if (span <= 0f) return 0
    val normalized = ((value - valueRange.start) / span).coerceIn(0f, 1f)
    return (normalized * steps).roundToInt()
}
