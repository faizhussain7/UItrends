package com.mfhapps.trendingui.ui.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun SectionChangeHapticEffect(
    sectionKey: Any?,
    enabled: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    var previousKey by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(sectionKey, enabled, reduceMotion) {
        if (!enabled || reduceMotion) {
            previousKey = sectionKey
            return@LaunchedEffect
        }
        val key = sectionKey ?: return@LaunchedEffect
        if (key != previousKey) {
            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
        }
        previousKey = key
    }
}
