package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipScope
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.detail.NestedBackEffect
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberAppTooltipState(
    isPersistent: Boolean = true,
): TooltipState = rememberTooltipState(isPersistent = isPersistent)

/**
 * Tooltip wrapper that registers the shared modal blur scrim (same as [AppAlertDialog] /
 * [AppModalBottomSheet]) while the tooltip is visible.
 *
 * The anchor composable is measured so the blur scrim starts below it — the icon/button stays sharp.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTooltipBox(
    state: TooltipState,
    positionProvider: PopupPositionProvider,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    focusable: Boolean = false,
    enableUserInput: Boolean = true,
    hasAction: Boolean = false,
    hapticsOnShow: Boolean = true,
    anchorClearance: Dp = 8.dp,
    tooltip: @Composable TooltipScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissAction = remember(state, onDismissRequest) {
        { onDismissRequest?.invoke() ?: state.dismiss() }
    }
    val currentDismiss by rememberUpdatedState(dismissAction)
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    val registrationId = remember { Any() }
    var tooltipVisible by remember { mutableStateOf(false) }
    var excludeTop by remember { mutableStateOf(0.dp) }

    LaunchedEffect(state, hapticsOnShow, reduceMotion, haptics) {
        snapshotFlow { state.isVisible }
            .distinctUntilChanged()
            .collect { isVisible ->
                tooltipVisible = isVisible
                if (isVisible && hapticsOnShow && !reduceMotion) {
                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
            }
    }

    RegisterModalBackdrop(
        id = registrationId,
        visible = tooltipVisible,
        onDismiss = { currentDismiss() },
        excludeTop = excludeTop,
    )

    NestedBackEffect(enabled = tooltipVisible, onBack = { currentDismiss() })

    TooltipBox(
        positionProvider = positionProvider,
        tooltip = tooltip,
        state = state,
        modifier = modifier,
        onDismissRequest = { currentDismiss() },
        focusable = focusable,
        enableUserInput = enableUserInput,
        hasAction = hasAction,
    ) {
        Box(
            Modifier.onGloballyPositioned { coordinates ->
                val measuredTop = with(density) {
                    coordinates.boundsInRoot().bottom.toDp()
                } + anchorClearance
                if (abs(measuredTop.value - excludeTop.value) >= 0.5f) {
                    excludeTop = measuredTop
                }
            },
        ) {
            content()
        }
    }
}

/**
 * Programmatic reveal with a fixed on-screen duration.
 *
 * [TooltipState.show] suspends until the tooltip is dismissed, so the show call must run in a
 * child coroutine; otherwise a timed dismiss can never run.
 */
@Composable
fun AppTooltipTimedRevealEffect(
    state: TooltipState,
    reveal: Boolean,
    enterDelayMillis: Long = 320,
    visibleMillis: Long = 5_000,
    once: Boolean = false,
) {
    val reduceMotion = LocalReduceMotion.current
    var hasRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(reveal, reduceMotion) {
        if (!reveal) {
            state.dismiss()
            return@LaunchedEffect
        }
        if (once && hasRevealed) return@LaunchedEffect

        if (!reduceMotion) delay(enterDelayMillis)

        val showJob = launch { state.show() }
        if (once) hasRevealed = true
        delay(visibleMillis)
        state.dismiss()
        showJob.cancel()
    }
}
