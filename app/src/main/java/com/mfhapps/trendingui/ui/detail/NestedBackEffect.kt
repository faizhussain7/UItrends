package com.mfhapps.trendingui.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * Registers a nested back handler for detail-pane chrome and the system back gesture.
 *
 * When [enabled], back invokes [onBack] instead of leaving the demo. Used for inner
 * navigation such as camera mode, list/detail stacks, and modal overlays.
 */
@Composable
fun NestedBackEffect(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    val nestedBackDispatcher = LocalNestedBackDispatcher.current
    val currentOnBack by rememberUpdatedState(onBack)

    DisposableEffect(enabled) {
        nestedBackDispatcher.handler = if (enabled) {
            { currentOnBack(); true }
        } else {
            null
        }
        onDispose { nestedBackDispatcher.handler = null }
    }

    BackHandler(enabled = enabled, onBack = onBack)
}
