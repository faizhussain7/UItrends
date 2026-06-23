package com.mfhapps.trendingui.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.theme.ModalBackdropStyle
import com.mfhapps.trendingui.ui.theme.resolveEffectBlurRadiusDp
import com.mfhapps.trendingui.ui.theme.resolveHazeInputScale
import com.mfhapps.trendingui.ui.theme.resolveHazeNoiseFactor
import com.mfhapps.trendingui.ui.theme.resolveHazeProgressive
import com.mfhapps.trendingui.ui.theme.resolveHazeTints
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

@Composable
fun Modifier.modalBackdropHazeEffect(
    state: HazeState,
    style: ModalBackdropStyle = LocalModalBackdropStyle.current,
    collapsedHeader: Boolean = false,
): Modifier {
    val normalized = style.normalized()
    val resolved = if (collapsedHeader) normalized.forCollapsedHeader() else normalized
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.45f
    val baseStyle = if (collapsedHeader) {
        rememberCollapsedHeaderHazeStyle(resolved)
    } else {
        rememberModalBackdropHazeStyle(resolved)
    }

    return hazeEffect(state = state, style = baseStyle) {
        blurRadius = resolved.resolveEffectBlurRadiusDp().dp
        backgroundColor = scheme.surface
        noiseFactor = resolved.resolveHazeNoiseFactor(isDark, collapsedHeader)
        tints = resolved.resolveHazeTints(scheme, isDark)
        mask = null
        progressive = resolved.resolveHazeProgressive(collapsedHeader)
        inputScale = resolved.resolveHazeInputScale()
    }
}
