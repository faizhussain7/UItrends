package com.mfhapps.trendingui.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.components.LocalModalBackdropBlurEnabled
import com.mfhapps.trendingui.ui.platform.supportsBackdropBlur
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState

val LocalCatalogHazeState = compositionLocalOf<HazeState?> { null }
val LocalCatalogHazeEnabled = compositionLocalOf { false }

@Composable
fun rememberCatalogHazeState(): Pair<HazeState, Boolean> {
    val context = LocalContext.current
    val deviceSupportsBlur = remember(context) { context.supportsBackdropBlur() }
    val settingsBlurEnabled = LocalModalBackdropBlurEnabled.current
    val enabled = deviceSupportsBlur && settingsBlurEnabled
    val state = rememberHazeState(blurEnabled = enabled)
    return state to enabled
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val hazeState = LocalCatalogHazeState.current
    val hazeEnabled = LocalCatalogHazeEnabled.current

    val glassModifier = if (hazeEnabled && hazeState != null) {
        modifier.hazeEffect(
            state = hazeState,
            style = HazeDefaults.style(
                backgroundColor = scheme.background,
                tint = HazeDefaults.tint(scheme.surface.copy(alpha = 0.62f)),
            ),
        )
    } else {
        modifier
    }

    val rim = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.42f),
            Color.White.copy(alpha = 0.08f),
            Color.Black.copy(alpha = 0.14f),
        ),
    )

    Surface(
        modifier = glassModifier,
        shape = shape,
        color = scheme.surface.copy(alpha = if (hazeEnabled) 0.48f else 0.72f),
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .border(width = 1.dp, brush = rim, shape = shape)
                .background(Color.White.copy(alpha = if (hazeEnabled) 0.06f else 0f)),
            content = content,
        )
    }
}

@Composable
fun ProvideCatalogHaze(
    hazeState: HazeState,
    hazeEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalCatalogHazeState provides hazeState,
        LocalCatalogHazeEnabled provides hazeEnabled,
    ) {
        content()
    }
}
