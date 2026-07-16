package com.mfhapps.trendingui.ui.detail

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.platform.isCompactWindowWidth

val LocalDetailPaneActive = staticCompositionLocalOf { false }

val LocalDemoSourceCodeUrl = staticCompositionLocalOf<String?> { null }


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

@Stable
class ImmersiveTopBarCollapseState {
    var collapsedFraction by mutableFloatStateOf(0f)
}

val LocalImmersiveTopBarCollapse = staticCompositionLocalOf<ImmersiveTopBarCollapseState?> { null }

@Stable
class ImmersiveTopBarStyleState {
    var foreground by mutableStateOf(Color.Unspecified)
    var iconTint by mutableStateOf(Color.Unspecified)
    var iconContainer by mutableStateOf(Color.Unspecified)
}

val LocalImmersiveTopBarStyle = staticCompositionLocalOf<ImmersiveTopBarStyleState?> { null }
