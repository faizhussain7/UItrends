package com.mfhapps.trendingui.screens.pretext

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import com.mfhapps.trendingui.ui.motion.ExpressiveMotion
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Immutable
data class PretextLiquidTabsColors(
    val container: Color,
    val containerBorder: Color,
    val lens: Color,
    val lensBorder: Color,
    val lensHighlight: Color,
    val selectedContent: Color,
    val unselectedContent: Color,
)

@Composable
fun pretextLiquidTabsCameraColors() = PretextLiquidTabsColors(
    container = Color.Black.copy(alpha = 0.44f),
    containerBorder = Color.White.copy(alpha = 0.16f),
    lens = Color.White.copy(alpha = 0.22f),
    lensBorder = Color.White.copy(alpha = 0.42f),
    lensHighlight = Color.White.copy(alpha = 0.20f),
    selectedContent = Color.White,
    unselectedContent = Color.White.copy(alpha = 0.72f),
)

@Composable
fun pretextLiquidTabsSurfaceColors(): PretextLiquidTabsColors {
    val scheme = MaterialTheme.colorScheme
    return PretextLiquidTabsColors(
        container = scheme.surfaceContainerHigh,
        containerBorder = scheme.outlineVariant.copy(alpha = 0.45f),
        lens = scheme.primaryContainer,
        lensBorder = scheme.primary.copy(alpha = 0.30f),
        lensHighlight = scheme.onPrimaryContainer.copy(alpha = 0.08f),
        selectedContent = scheme.onPrimaryContainer,
        unselectedContent = scheme.onSurfaceVariant,
    )
}

@Composable
fun PretextLiquidGlassTabs(
    selected: PretextScreenMode,
    onSelected: (PretextScreenMode) -> Unit,
    modifier: Modifier = Modifier,
    colors: PretextLiquidTabsColors = pretextLiquidTabsSurfaceColors(),
) {
    val tabs = PretextScreenMode.entries
    val tabCount = tabs.size
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var barWidthPx by remember { mutableIntStateOf(0) }
    val tabWidthPx = if (barWidthPx > 0) barWidthPx.toFloat() / tabCount else 0f

    val selectedIndex = tabs.indexOf(selected).coerceAtLeast(0)
    val lensX = remember { Animatable(0f) }
    var pressed by remember { mutableStateOf(false) }
    var focusIndex by remember { mutableIntStateOf(selectedIndex) }

    LaunchedEffect(selectedIndex, tabWidthPx) {
        if (tabWidthPx <= 0f) return@LaunchedEffect
        focusIndex = selectedIndex
        if (!pressed) {
            if (lensX.value == 0f && selectedIndex == 0) {
                lensX.snapTo(0f)
            } else {
                lensX.animateTo(selectedIndex * tabWidthPx, ExpressiveMotion.defaultSpatial())
            }
        }
    }

    val lensStretch by animateFloatAsState(
        targetValue = if (pressed) 1.07f else 1f,
        animationSpec = ExpressiveMotion.fastSpatial(),
        label = "lensStretch",
    )
    val lensSquash by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = ExpressiveMotion.fastSpatial(),
        label = "lensSquash",
    )

    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(colors.container)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(colors.containerBorder, colors.containerBorder.copy(alpha = colors.containerBorder.alpha * 0.3f)),
                ),
                shape = shape,
            )
            .onSizeChanged { barWidthPx = it.width }
            .pointerInput(tabCount) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val tabW = size.width.toFloat() / tabCount
                    if (tabW <= 0f) return@awaitEachGesture
                    down.consume()
                    pressed = true
                    val maxLensX = (tabCount - 1) * tabW
                    var lastIndex = (down.position.x / tabW).toInt().coerceIn(0, tabCount - 1)
                    focusIndex = lastIndex
                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                    scope.launch {
                        lensX.animateTo(lastIndex * tabW, ExpressiveMotion.fastSpatial())
                    }
                    drag(down.id) { change ->
                        change.consume()
                        val target = (change.position.x - tabW / 2f).coerceIn(0f, maxLensX)
                        scope.launch { lensX.snapTo(target) }
                        val index = (change.position.x / tabW).toInt().coerceIn(0, tabCount - 1)
                        if (index != lastIndex) {
                            lastIndex = index
                            focusIndex = index
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                    pressed = false
                    focusIndex = lastIndex
                    scope.launch {
                        lensX.animateTo(lastIndex * tabW, ExpressiveMotion.defaultSpatial())
                    }
                    onSelected(tabs[lastIndex])
                }
            },
    ) {
        if (tabWidthPx > 0f) {
            val tabWidthDp = with(density) { tabWidthPx.toDp() }
            Box(
                modifier = Modifier
                    .offset { IntOffset(lensX.value.roundToInt(), 0) }
                    .width(tabWidthDp)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .graphicsLayer {
                        scaleX = lensStretch
                        scaleY = lensSquash
                        transformOrigin = TransformOrigin.Center
                    }
                    .clip(shape)
                    .background(colors.lens)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(colors.lensBorder, colors.lensBorder.copy(alpha = colors.lensBorder.alpha * 0.25f)),
                        ),
                        shape = shape,
                    )
                    .background(
                        Brush.verticalGradient(
                            0f to colors.lensHighlight,
                            0.45f to Color.Transparent,
                        ),
                    ),
            )
        }

        Row(Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, mode ->
                val active = index == (if (pressed) focusIndex else selectedIndex)
                val contentColor by animateColorAsState(
                    targetValue = if (active) colors.selectedContent else colors.unselectedContent,
                    animationSpec = ExpressiveMotion.defaultEffects(),
                    label = "tabContent",
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics {
                            role = Role.Tab
                            this.selected = active
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    DecorativeIcon(
                        mode.icon(),
                        tint = contentColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
