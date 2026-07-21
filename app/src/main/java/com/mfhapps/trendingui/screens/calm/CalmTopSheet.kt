package com.mfhapps.trendingui.screens.calm

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.LocalModalBackdropBlurEnabled
import com.mfhapps.trendingui.ui.components.RegisterModalBackdrop
import com.mfhapps.trendingui.ui.motion.ExpressiveMotion
import kotlinx.coroutines.launch

@Composable
internal fun CalmTopSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onProgress: (Float) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val reduceMotion = LocalReduceMotion.current
    val scope = rememberCoroutineScope()
    val blurBackdrop = LocalModalBackdropBlurEnabled.current

    var sheetHeightPx by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    val registrationId = remember { Any() }

    val currentOnProgress = rememberUpdatedState(onProgress)
    LaunchedEffect(progress) {
        snapshotFlow { progress.value }.collect { currentOnProgress.value(it) }
    }

    LaunchedEffect(visible, reduceMotion) {
        if (visible) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = if (reduceMotion) tween(140) else ExpressiveMotion.defaultSpatial(),
            )
        } else {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = if (reduceMotion) tween(120) else ExpressiveMotion.fastSpatial(),
            )
        }
    }

    val present = visible || progress.value > 0.001f || progress.isRunning
    if (!present) return

    val sheetVisibleHeight = with(density) {
        (sheetHeightPx * progress.value.coerceIn(0f, 1f)).toDp()
    }

    RegisterModalBackdrop(
        id = registrationId,
        visible = visible,
        onDismiss = onDismiss,
        excludeTop = sheetVisibleHeight,
    )

    BackHandler(enabled = visible, onBack = onDismiss)

    Box(modifier = modifier.fillMaxSize().zIndex(6f)) {
        if (!blurBackdrop && visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = progress.value * 0.55f }
                    .background(scheme.scrim)
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    },
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .navigationBarsPadding()
                .onSizeChanged { sheetHeightPx = it.height }
                .graphicsLayer {
                    translationY = -(1f - progress.value.coerceIn(0f, 1f)) * sheetHeightPx
                },
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            color = scheme.surfaceContainerLow,
            contentColor = scheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
        ) {
            Column(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .weight(1f, fill = false),
                ) {
                    content()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable(
                            onClickLabel = "Close reading options",
                            onClick = onDismiss,
                        )
                        .pointerInput(sheetHeightPx) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    if (sheetHeightPx <= 0) return@detectVerticalDragGestures
                                    val next = (progress.value + dragAmount / sheetHeightPx)
                                        .coerceIn(0f, 1f)
                                    scope.launch { progress.snapTo(next) }
                                },
                                onDragEnd = {
                                    if (progress.value < 0.72f) {
                                        onDismiss()
                                    } else {
                                        scope.launch {
                                            progress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = ExpressiveMotion.defaultSpatial(),
                                            )
                                        }
                                    }
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(scheme.primary.copy(alpha = 0.6f)),
                    )
                }
            }
        }
    }
}
