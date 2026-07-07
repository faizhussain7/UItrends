package com.mfhapps.trendingui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion

@Composable
fun NestedScreenOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reduceMotion = LocalReduceMotion.current
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (reduceMotion) {
            screenTransitionFadeIn()
        } else {
            screenPushEnter()
        },
        exit = if (reduceMotion) {
            screenTransitionFadeOut()
        } else {
            screenPopExit()
        },
        label = "nested-screen-overlay",
        content = { content() },
    )
}
