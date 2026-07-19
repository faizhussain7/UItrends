package com.mfhapps.trendingui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive

@Composable
fun rememberComposeFps(): Int {
    var fps by remember { mutableIntStateOf(0) }
    var frameCount by remember { mutableLongStateOf(0L) }
    var lastNano by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { nano ->
                frameCount++
                if (lastNano == 0L) {
                    lastNano = nano
                } else if (nano - lastNano >= 1_000_000_000L) {
                    fps = frameCount.toInt()
                    frameCount = 0
                    lastNano = nano
                }
            }
        }
    }

    return fps
}

@Composable
fun FpsMeasureChip(
    fps: Int,
    lastMeasureMs: Long,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    contentColor: Color = Color.Unspecified,
    containerColor: Color = Color.Unspecified,
) {
    val scheme = MaterialTheme.colorScheme
    val bg = when {
        containerColor != Color.Unspecified -> containerColor
        highlighted -> scheme.tertiaryContainer.copy(alpha = 0.92f)
        else -> scheme.surfaceContainerHigh.copy(alpha = 0.92f)
    }
    val fg = when {
        contentColor != Color.Unspecified -> contentColor
        highlighted -> scheme.onTertiaryContainer
        else -> scheme.onSurface
    }

    Text(
        text = "FPS $fps · ${lastMeasureMs}ms",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Medium,
        color = fg,
        modifier = modifier
            .background(bg, MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = if (highlighted) 3.dp else 4.dp),
    )
}
