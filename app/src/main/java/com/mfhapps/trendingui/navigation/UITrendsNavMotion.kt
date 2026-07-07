package com.mfhapps.trendingui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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

private const val PUSH_MS = 340
private val easing = FastOutSlowInEasing
private val slideSpring: FiniteAnimationSpec<IntOffset> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

fun AnimatedContentTransitionScope<*>.enterForward(): EnterTransition =
    fadeIn(tween(PUSH_MS, easing = easing)) +
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = slideSpring,
        ) +
        scaleIn(initialScale = 0.97f, animationSpec = tween(PUSH_MS, easing = easing))

fun AnimatedContentTransitionScope<*>.exitForward(): ExitTransition =
    fadeOut(tween(260, easing = easing)) +
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(300, easing = easing),
        ) +
        scaleOut(targetScale = 0.98f, animationSpec = tween(260, easing = easing))

fun AnimatedContentTransitionScope<*>.popEnter(): EnterTransition =
    fadeIn(tween(PUSH_MS, easing = easing)) +
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = slideSpring,
        ) +
        scaleIn(initialScale = 0.98f, animationSpec = tween(PUSH_MS, easing = easing))

fun AnimatedContentTransitionScope<*>.popExit(): ExitTransition =
    fadeOut(tween(260, easing = easing)) +
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(300, easing = easing),
        ) +
        scaleOut(targetScale = 0.97f, animationSpec = tween(260, easing = easing))

fun screenPushEnter(): EnterTransition =
    fadeIn(tween(PUSH_MS, easing = easing)) +
        slideInHorizontally(animationSpec = slideSpring, initialOffsetX = { it }) +
        scaleIn(initialScale = 0.97f, animationSpec = tween(PUSH_MS, easing = easing))

fun screenPushExit(): ExitTransition =
    fadeOut(tween(260, easing = easing)) +
        slideOutHorizontally(animationSpec = tween(300, easing = easing), targetOffsetX = { -it / 4 }) +
        scaleOut(targetScale = 0.98f, animationSpec = tween(260, easing = easing))

fun screenPopEnter(): EnterTransition =
    fadeIn(tween(PUSH_MS, easing = easing)) +
        slideInHorizontally(animationSpec = slideSpring, initialOffsetX = { -it / 4 }) +
        scaleIn(initialScale = 0.98f, animationSpec = tween(PUSH_MS, easing = easing))

fun screenPopExit(): ExitTransition =
    fadeOut(tween(260, easing = easing)) +
        slideOutHorizontally(animationSpec = tween(300, easing = easing), targetOffsetX = { it }) +
        scaleOut(targetScale = 0.97f, animationSpec = tween(260, easing = easing))

fun screenTransitionFadeIn(): EnterTransition = fadeIn(tween(200, easing = easing))

fun screenTransitionFadeOut(): ExitTransition = fadeOut(tween(180, easing = easing))

fun AnimatedContentTransitionScope<*>.nestedPushTransform(): ContentTransform =
    (enterForward() togetherWith exitForward()) using sizeTransform()

fun AnimatedContentTransitionScope<*>.nestedPopTransform(): ContentTransform =
    (popEnter() togetherWith popExit()) using sizeTransform()

fun AnimatedContentTransitionScope<*>.nestedScreenTransform(pushing: Boolean): ContentTransform =
    if (pushing) nestedPushTransform() else nestedPopTransform()

fun AnimatedContentTransitionScope<*>.homeExit(): ExitTransition =
    fadeOut(tween(280, easing = easing)) +
        scaleOut(
            targetScale = 0.94f,
            animationSpec = tween(280, easing = easing),
        )

fun AnimatedContentTransitionScope<*>.homePopEnter(): EnterTransition =
    fadeIn(tween(300, easing = easing)) +
        scaleIn(
            initialScale = 0.94f,
            animationSpec = tween(300, easing = easing),
        )

fun sizeTransform(): SizeTransform = SizeTransform(clip = false) { _, _ ->
    tween(PUSH_MS, easing = easing)
}

fun sharedBoundsTransform(): FiniteAnimationSpec<Rect> =
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

fun pretextModeEnterForward(): EnterTransition =
    slideInVertically(
        animationSpec = tween(360, easing = easing),
        initialOffsetY = { fullHeight -> fullHeight / 6 },
    ) + fadeIn(tween(300, easing = easing)) +
        scaleIn(initialScale = 0.98f, animationSpec = tween(360, easing = easing))

fun pretextModeExitForward(): ExitTransition =
    slideOutVertically(
        animationSpec = tween(320, easing = easing),
        targetOffsetY = { fullHeight -> -fullHeight / 10 },
    ) + fadeOut(tween(260, easing = easing))

fun pretextModePopEnter(): EnterTransition =
    slideInVertically(
        animationSpec = tween(360, easing = easing),
        initialOffsetY = { fullHeight -> -fullHeight / 10 },
    ) + fadeIn(tween(300, easing = easing))

fun pretextModePopExit(): ExitTransition =
    slideOutVertically(
        animationSpec = tween(320, easing = easing),
        targetOffsetY = { fullHeight -> fullHeight / 6 },
    ) + fadeOut(tween(260, easing = easing))

fun Any.demoSharedContentKey(): String = "demo-icon-${this::class.simpleName}"
