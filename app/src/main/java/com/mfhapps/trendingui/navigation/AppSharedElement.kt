package com.mfhapps.trendingui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

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
        boundsTransform = BoundsTransform { _, _ -> sharedBoundsTransform() },
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.photoSharedBounds(
    sharedTransitionScope: SharedTransitionScope,
    key: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    contentScale: ContentScale = ContentScale.Crop,
): Modifier = with(sharedTransitionScope) {
    sharedBounds(
        sharedContentState = rememberSharedContentState(key = key),
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = BoundsTransform { _, _ -> sharedBoundsTransform() },
        resizeMode = scaleToBounds(
            contentScale = contentScale,
            alignment = Alignment.Center,
        ),
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.photoSharedElementManaged(
    sharedTransitionScope: SharedTransitionScope,
    key: String,
    visible: Boolean,
): Modifier = with(sharedTransitionScope) {
    sharedElementWithCallerManagedVisibility(
        sharedContentState = rememberSharedContentState(key = key),
        visible = visible,
        boundsTransform = BoundsTransform { _, _ -> sharedBoundsTransform() },
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.masonrySharedBounds(
    sharedTransitionScope: SharedTransitionScope,
    key: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
): Modifier = with(sharedTransitionScope) {
    sharedBounds(
        sharedContentState = rememberSharedContentState(key = key),
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = BoundsTransform { _, _ -> sharedBoundsTransform() },
        resizeMode = scaleToBounds(
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
        ),
    )
}
