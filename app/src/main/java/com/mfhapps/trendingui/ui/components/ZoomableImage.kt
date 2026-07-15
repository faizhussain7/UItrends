package com.mfhapps.trendingui.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import com.mfhapps.trendingui.ui.motion.ExpressiveMotion
import kotlinx.coroutines.launch

private const val MinZoom = 1f
private const val MaxZoom = 4f
private const val DoubleTapZoom = 2.5f

@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onZoomChanged: (Boolean) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(MinZoom) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastHapticScale by remember { mutableFloatStateOf(MinZoom) }

    fun clampOffset(raw: Offset, zoom: Float): Offset {
        if (zoom <= MinZoom || containerSize == IntSize.Zero) return Offset.Zero
        val maxX = containerSize.width * (zoom - MinZoom) / 2f
        val maxY = containerSize.height * (zoom - MinZoom) / 2f
        return Offset(
            x = raw.x.coerceIn(-maxX, maxX),
            y = raw.y.coerceIn(-maxY, maxY),
        )
    }

    fun pulseIfNeeded(newScale: Float) {
        val crossedMin = lastHapticScale <= MinZoom + 0.02f && newScale > MinZoom + 0.02f
        val crossedMax = lastHapticScale < MaxZoom - 0.02f && newScale >= MaxZoom - 0.02f
        if (crossedMin || crossedMax) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        lastHapticScale = newScale
    }

    suspend fun animateTo(zoom: Float, pan: Offset) {
        val targetPan = clampOffset(pan, zoom)
        val startScale = scale
        val startPan = offset
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = ExpressiveMotion.zoomSpatial,
        ) { fraction, _ ->
            scale = startScale + (zoom - startScale) * fraction
            offset = Offset(
                x = startPan.x + (targetPan.x - startPan.x) * fraction,
                y = startPan.y + (targetPan.y - startPan.y) * fraction,
            )
        }
        scale = zoom
        offset = targetPan
        onZoomChanged(zoom > MinZoom + 0.01f)
        lastHapticScale = zoom
    }

    suspend fun resetZoom() {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val startScale = scale
        val startPan = offset
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = ExpressiveMotion.contentFade,
        ) { fraction, _ ->
            scale = startScale + (MinZoom - startScale) * fraction
            offset = Offset(
                x = startPan.x * (1f - fraction),
                y = startPan.y * (1f - fraction),
            )
        }
        scale = MinZoom
        offset = Offset.Zero
        onZoomChanged(false)
        lastHapticScale = MinZoom
    }

    val transformState = rememberTransformableState { _, zoomDelta, panDelta, _ ->
        if (!enabled) return@rememberTransformableState
        val newScale = (scale * zoomDelta).coerceIn(MinZoom, MaxZoom)
        pulseIfNeeded(newScale)
        scale = newScale
        offset = if (newScale > MinZoom) clampOffset(offset + panDelta, newScale) else Offset.Zero
        onZoomChanged(newScale > MinZoom + 0.01f)
    }

    Box(
        modifier = modifier
            .onSizeChanged { containerSize = it }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onDoubleTap = { tap ->
                        scope.launch {
                            if (scale > MinZoom + 0.05f) {
                                resetZoom()
                            } else {
                                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                                val focal = tap - Offset(containerSize.width / 2f, containerSize.height / 2f)
                                val targetPan = clampOffset(-focal * (DoubleTapZoom - 1f), DoubleTapZoom)
                                animateTo(DoubleTapZoom, targetPan)
                            }
                        }
                    },
                )
            }
            .transformable(state = transformState, enabled = enabled),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        ) {
            content()
        }
    }
}
