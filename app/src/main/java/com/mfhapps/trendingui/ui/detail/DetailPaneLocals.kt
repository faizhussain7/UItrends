package com.mfhapps.trendingui.ui.detail

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.mfhapps.trendingui.ui.platform.isCompactWindowWidth
import androidx.compose.ui.unit.dp

val LocalDetailPaneActive = staticCompositionLocalOf { false }


@Composable
fun Modifier.demoDetailScrollInsets(): Modifier {
    val inDetailPane = LocalDetailPaneActive.current
    val compact = isCompactWindowWidth()
    return if (inDetailPane && compact) {
        this
    } else {
        navigationBarsPadding()
    }
}

fun Modifier.demoDetailScrollBottomGap(): Modifier = padding(bottom = 8.dp)


class NestedBackDispatcher {
    var handler: (() -> Boolean)? = null
}

val LocalNestedBackDispatcher = staticCompositionLocalOf { NestedBackDispatcher() }
