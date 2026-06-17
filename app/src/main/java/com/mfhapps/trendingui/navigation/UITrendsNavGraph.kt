package com.mfhapps.trendingui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.homeDestination(
    sharedTransitionScope: SharedTransitionScope,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    composable<HomeRoute>(
        enterTransition = { EnterTransition.None },
        exitTransition = { homeExit() },
        popEnterTransition = { homePopEnter() },
        popExitTransition = { ExitTransition.None },
        sizeTransform = { sizeTransform() },
    ) {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides sharedTransitionScope,
            LocalNavAnimatedVisibilityScope provides this@composable,
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
inline fun <reified T : Any> NavGraphBuilder.demoDestination(
    sharedTransitionScope: SharedTransitionScope,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable<T>(
        enterTransition = { enterForward() },
        exitTransition = { exitForward() },
        popEnterTransition = { popEnter() },
        popExitTransition = { popExit() },
        sizeTransform = { sizeTransform() },
        content = { entry ->
            CompositionLocalProvider(
                LocalSharedTransitionScope provides sharedTransitionScope,
                LocalNavAnimatedVisibilityScope provides this@composable,
            ) {
                content(entry)
            }
        },
    )
}
