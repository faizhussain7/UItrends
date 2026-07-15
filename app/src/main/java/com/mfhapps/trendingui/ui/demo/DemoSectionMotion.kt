package com.mfhapps.trendingui.ui.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.motion.expressiveEffectsSpec
import com.mfhapps.trendingui.ui.motion.expressiveSpatialSpec
import kotlinx.coroutines.delay

@Composable
fun DemoAnimatedSection(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reduceMotion = LocalReduceMotion.current
    var visible by remember { mutableStateOf(reduceMotion) }
    val enterEffects = expressiveEffectsSpec<Float>()
    val enterSpatial = expressiveSpatialSpec<IntOffset>()

    LaunchedEffect(index, reduceMotion) {
        if (reduceMotion) {
            visible = true
        } else {
            visible = false
            delay((index * 70).coerceAtMost(420).toLong())
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(enterEffects) + slideInVertically(
            animationSpec = enterSpatial,
            initialOffsetY = { fullHeight -> fullHeight / 5 },
        ),
        exit = fadeOut(enterEffects),
    ) {
        content()
    }
}
