package com.mfhapps.trendingui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.demoSharedElement(
    sharedTransitionScope: SharedTransitionScope,
    key: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
): Modifier = with(sharedTransitionScope) {
    sharedElement(
        sharedContentState = rememberSharedContentState(key = key),
        animatedVisibilityScope = animatedVisibilityScope,
    )
}
