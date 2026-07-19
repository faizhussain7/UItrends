package com.mfhapps.trendingui.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.navigation.enterForward
import com.mfhapps.trendingui.navigation.exitForward
import com.mfhapps.trendingui.navigation.popEnter
import com.mfhapps.trendingui.navigation.popExit
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion

@Composable
fun <T : Any> AppModalSheetStack(
    page: T,
    isRoot: Boolean,
    goingForward: Boolean,
    onPop: () -> Unit,
    modifier: Modifier = Modifier,
    contentSpacing: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: @Composable (T) -> Unit,
) {
    val reduceMotion = LocalReduceMotion.current
    val bodyHeight = (LocalConfiguration.current.screenHeightDp * 0.88f).dp

    BackHandler(enabled = !isRoot, onBack = onPop)

    Box(
        modifier
            .fillMaxWidth()
            .height(bodyHeight),
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                when {
                    reduceMotion ->
                        fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                    goingForward ->
                        enterForward() togetherWith exitForward()
                    else ->
                        popEnter() togetherWith popExit()
                }
            },
            contentAlignment = Alignment.TopStart,
            label = "app-modal-sheet-stack",
            modifier = Modifier.fillMaxSize(),
        ) { current ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = contentSpacing,
            ) {
                content(current)
            }
        }
    }
}
