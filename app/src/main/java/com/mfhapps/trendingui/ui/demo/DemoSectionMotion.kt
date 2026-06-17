package com.mfhapps.trendingui.ui.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import kotlinx.coroutines.delay


@Composable
fun DemoAnimatedSection(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reduceMotion = LocalReduceMotion.current
    var visible by remember { mutableStateOf(reduceMotion) }

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
        enter = fadeIn(tween(380)) + slideInVertically(
            animationSpec = tween(420),
            initialOffsetY = { fullHeight -> fullHeight / 5 },
        ),
        exit = fadeOut(tween(180)),
    ) {
        content()
    }
}
