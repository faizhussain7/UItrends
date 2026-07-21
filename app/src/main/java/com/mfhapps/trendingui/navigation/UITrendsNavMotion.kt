package com.mfhapps.trendingui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mfhapps.trendingui.ui.motion.ExpressiveMotion

private val slideSpatial: FiniteAnimationSpec<IntOffset> = ExpressiveMotion.navOffset
private val floatSpatial: FiniteAnimationSpec<Float> = ExpressiveMotion.navFloat
private val floatEffects: FiniteAnimationSpec<Float> = ExpressiveMotion.navEffects
private val sizeSpatial: FiniteAnimationSpec<IntSize> = ExpressiveMotion.defaultSpatial()

fun AnimatedContentTransitionScope<*>.enterForward(): EnterTransition =
    fadeIn(floatEffects) +
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = slideSpatial,
        ) +
        scaleIn(initialScale = 0.97f, animationSpec = floatSpatial)

fun AnimatedContentTransitionScope<*>.exitForward(): ExitTransition =
    fadeOut(floatEffects) +
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = slideSpatial,
        ) +
        scaleOut(targetScale = 0.98f, animationSpec = floatSpatial)

fun AnimatedContentTransitionScope<*>.popEnter(): EnterTransition =
    fadeIn(floatEffects) +
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = slideSpatial,
        ) +
        scaleIn(initialScale = 0.98f, animationSpec = floatSpatial)

fun AnimatedContentTransitionScope<*>.popExit(): ExitTransition =
    fadeOut(floatEffects) +
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = slideSpatial,
        ) +
        scaleOut(targetScale = 0.97f, animationSpec = floatSpatial)

fun AnimatedContentTransitionScope<*>.homeExit(): ExitTransition =
    fadeOut(floatEffects) +
        scaleOut(targetScale = 0.94f, animationSpec = floatSpatial)

fun AnimatedContentTransitionScope<*>.homePopEnter(): EnterTransition =
    fadeIn(floatEffects) +
        scaleIn(initialScale = 0.94f, animationSpec = floatSpatial)

fun screenPushEnter(): EnterTransition =
    fadeIn(floatEffects) +
        slideInHorizontally(animationSpec = slideSpatial, initialOffsetX = { it }) +
        scaleIn(initialScale = 0.97f, animationSpec = floatSpatial)

fun screenPopExit(): ExitTransition =
    fadeOut(floatEffects) +
        slideOutHorizontally(animationSpec = slideSpatial, targetOffsetX = { it }) +
        scaleOut(targetScale = 0.97f, animationSpec = floatSpatial)

fun screenTransitionFadeIn(): EnterTransition = fadeIn(floatEffects)

fun screenTransitionFadeOut(): ExitTransition = fadeOut(floatEffects)

fun AnimatedContentTransitionScope<*>.nestedPushTransform(): ContentTransform =
    (enterForward() togetherWith exitForward()) using sizeTransform()

fun AnimatedContentTransitionScope<*>.nestedPopTransform(): ContentTransform =
    (popEnter() togetherWith popExit()) using sizeTransform()

fun sizeTransform(): SizeTransform = SizeTransform(clip = false) { _, _ ->
    sizeSpatial
}

fun sharedBoundsTransform(): FiniteAnimationSpec<Rect> = ExpressiveMotion.sharedBounds

fun pretextModeEnterForward(): EnterTransition =
    slideInVertically(
        animationSpec = slideSpatial,
        initialOffsetY = { fullHeight -> fullHeight },
    )

fun pretextModeExitForward(): ExitTransition =
    slideOutVertically(
        animationSpec = slideSpatial,
        targetOffsetY = { fullHeight -> -fullHeight / 4 },
    ) + fadeOut(floatEffects) +
        scaleOut(targetScale = 0.96f, animationSpec = floatSpatial)

fun pretextModePopEnter(): EnterTransition =
    slideInVertically(
        animationSpec = slideSpatial,
        initialOffsetY = { fullHeight -> -fullHeight / 4 },
    ) + fadeIn(floatEffects) +
        scaleIn(initialScale = 0.96f, animationSpec = floatSpatial)

fun pretextModePopExit(): ExitTransition =
    slideOutVertically(
        animationSpec = slideSpatial,
        targetOffsetY = { fullHeight -> fullHeight },
    )

fun Any.demoSharedContentKey(): String = "demo-icon-${this::class.simpleName}"
