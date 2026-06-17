package com.mfhapps.trendingui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive

@Composable
fun FpsOverlay(
    modifier: Modifier = Modifier,
    lastMeasureMs: Long = 0L,
    content: @Composable () -> Unit,
) {
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

    Box(modifier = modifier) {
        content()
        Text(
            text = "FPS $fps · ${lastMeasureMs}ms",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                    MaterialTheme.shapes.small,
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
