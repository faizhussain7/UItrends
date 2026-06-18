package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.flow.distinctUntilChanged


@Composable
fun DismissFocusOnScrollAndImeEffect(
    scrollInProgress: () -> Boolean,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeBottomPx = WindowInsets.ime.getBottom(LocalDensity.current)

    LaunchedEffect(scrollInProgress) {
        snapshotFlow { scrollInProgress() }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (scrolling) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
    }

    var wasImeOpen by remember { mutableStateOf(false) }
    LaunchedEffect(imeBottomPx) {
        val imeOpen = imeBottomPx > 0
        if (wasImeOpen && !imeOpen) {
            focusManager.clearFocus()
        }
        wasImeOpen = imeOpen
    }
}
