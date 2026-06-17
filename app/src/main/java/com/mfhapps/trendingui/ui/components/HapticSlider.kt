package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mfhapps.trendingui.ui.accessibility.rememberControlHaptics
import com.mfhapps.trendingui.ui.accessibility.sliderStepIndex

@Composable
fun HapticSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val haptics = rememberControlHaptics()
    var lastStep by remember(valueRange, steps) {
        mutableIntStateOf(sliderStepIndex(value, valueRange, steps))
    }

    Slider(
        value = value,
        onValueChange = { newValue ->
            if (enabled) {
                val nextStep = sliderStepIndex(newValue, valueRange, steps)
                haptics.onSliderStep(lastStep, nextStep)
                lastStep = nextStep
            }
            onValueChange(newValue)
        },
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = {
            haptics.onSliderFinished()
            onValueChangeFinished?.invoke()
        },
        colors = colors,
        interactionSource = interactionSource,
    )
}
