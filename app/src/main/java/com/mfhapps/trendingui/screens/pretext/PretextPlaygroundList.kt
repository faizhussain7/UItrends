package com.mfhapps.trendingui.screens.pretext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import com.mfhapps.trendingui.ui.components.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Videocam
import com.mfhapps.trendingui.ui.components.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.AppTooltipBox
import com.mfhapps.trendingui.ui.components.AppTooltipTimedRevealEffect
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.components.HapticSlider
import com.mfhapps.trendingui.ui.components.rememberAppTooltipState

object PretextPlaygroundListKeys {
    const val Intro = "intro"
    const val PlaygroundTitle = "playground_title"
    const val LayoutMode = "layout_mode"
    const val Script = "script"
    const val StickyStack = "sticky_stack"
    const val LivePreview = "live_preview"
    const val Benchmark = "benchmark"
}


object PretextPlaygroundStickyIndices {
    const val StickyStack = 4
    const val LivePreview = 5
    const val Benchmark = 9
}

internal const val PRETEXT_OPEN_SCROLL_ANCHOR_FRAMES = 6

@Composable
fun rememberPretextPlaygroundListState(): LazyListState {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        repeat(PRETEXT_OPEN_SCROLL_ANCHOR_FRAMES) {
            withFrameNanos { }
            if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                return@repeat
            }
            if (listState.firstVisibleItemIndex == 0) {
                listState.scrollToItem(0, 0)
            }
        }
    }
    return listState
}

@Immutable
data class PretextStickyAttachState(
    val isPinned: Boolean,
    val controlsAttached: Boolean,
    val runAttached: Boolean,
)


internal fun LazyListState.isListItemScrolledPast(itemKey: Any, itemIndex: Int): Boolean {
    if (firstVisibleItemIndex > itemIndex) return true
    if (firstVisibleItemIndex < itemIndex) return false
    return layoutInfo.visibleItemsInfo.none { it.key == itemKey }
}


@Composable
fun rememberPretextStickyAttachState(
    listState: LazyListState,
    runRevealIndex: Int = PretextPlaygroundStickyIndices.Benchmark,
): PretextStickyAttachState {
    val isPinned by rememberPretextMeasureFieldPinned(listState)

    val runAttached by remember(listState, runRevealIndex, isPinned) {
        derivedStateOf {
            isPinned && listState.isListItemScrolledPast(
                itemKey = PretextPlaygroundListKeys.Benchmark,
                itemIndex = runRevealIndex,
            )
        }
    }

    return PretextStickyAttachState(
        isPinned = isPinned,
        controlsAttached = isPinned,
        runAttached = runAttached,
    )
}

@Composable
fun rememberPretextPlaygroundCameraInHeader(
    listState: LazyListState,
    revealFromIndex: Int = PretextPlaygroundStickyIndices.StickyStack,
): State<Boolean> = remember(listState, revealFromIndex) {
    derivedStateOf {
        listState.isListItemScrolledPast(
            itemKey = PretextPlaygroundListKeys.StickyStack,
            itemIndex = revealFromIndex,
        )
    }
}

@Composable
fun rememberPretextStickyPinned(
    listState: LazyListState,
    stickyKey: Any,
    stackedAfterKey: Any? = null,
    activateFromIndex: Int = 0,
): State<Boolean> = remember(listState, stickyKey, activateFromIndex) {
    derivedStateOf {
        listState.isListItemScrolledPast(
            itemKey = stickyKey,
            itemIndex = activateFromIndex,
        )
    }
}

@Composable
fun rememberPretextMeasureFieldPinned(
    listState: LazyListState,
    measureFieldKey: Any = PretextPlaygroundListKeys.StickyStack,
): State<Boolean> = rememberPretextStickyPinned(
    listState = listState,
    stickyKey = measureFieldKey,
    activateFromIndex = PretextPlaygroundStickyIndices.StickyStack,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PretextPlaygroundLazyList(
    listState: LazyListState,
    screenMode: PretextScreenMode,
    onScreenModeChange: (PretextScreenMode) -> Unit,
    layoutMode: PretextLayoutMode,
    onLayoutModeChange: (PretextLayoutMode) -> Unit,
    selectedScript: TextScript,
    onScriptSelected: (TextScript) -> Unit,
    customText: String,
    onCustomTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    val attachState = rememberPretextStickyAttachState(listState)

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .appHazeSource(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = PretextPlaygroundListKeys.Intro) {
            PretextPlaygroundIntroSection(
                screenMode = screenMode,
                onScreenModeChange = onScreenModeChange,
            )
        }

        item(key = PretextPlaygroundListKeys.PlaygroundTitle) {
            PretextSectionTitle(
                title = "Playground",
                subtitle = "Tune the engine and inspect every layout decision.",
                icon = Icons.Outlined.Tune,
            )
        }

        item(key = PretextPlaygroundListKeys.LayoutMode) {
            PretextLayoutModeSelector(
                selected = layoutMode,
                onSelected = onLayoutModeChange,
            )
        }

        item(key = PretextPlaygroundListKeys.Script) {
            PretextScriptChips(
                selected = selectedScript,
                onSelected = onScriptSelected,
            )
        }

        stickyHeader(key = PretextPlaygroundListKeys.StickyStack) {
            PretextPlaygroundStickyStack(
                attachState = attachState,
                customText = customText,
                onCustomTextChange = onCustomTextChange,
                containerWidthDp = 300f,
                onContainerWidthChange = {},
                layoutMode = layoutMode,
                obstacleFraction = 0.55f,
                onObstacleFractionChange = {},
                running = false,
                onRunBenchmark = {},
            )
        }

        content()
    }
}

@Composable
private fun PretextPlaygroundIntroSection(
    screenMode: PretextScreenMode,
    onScreenModeChange: (PretextScreenMode) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PretextHeroCard()
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
        ) {
            PretextModeSelector(
                selected = screenMode,
                onSelected = onScreenModeChange,
                modifier = Modifier.padding(12.dp),
            )
        }
        PretextHowItWorksStrip()
    }
}

@Composable
private fun PretextStickyAttachHaptics(attachState: PretextStickyAttachState) {
    val haptics = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current

    var wasPinned by remember { mutableStateOf(false) }
    var wasRunAttached by remember { mutableStateOf(false) }

    LaunchedEffect(attachState.isPinned) {
        if (attachState.isPinned && !wasPinned && !reduceMotion) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasPinned = attachState.isPinned
    }
    LaunchedEffect(attachState.runAttached) {
        if (attachState.runAttached && !wasRunAttached && !reduceMotion) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasRunAttached = attachState.runAttached
    }
}

@Composable
fun PretextPlaygroundStickyStack(
    attachState: PretextStickyAttachState,
    customText: String,
    onCustomTextChange: (String) -> Unit,
    containerWidthDp: Float,
    onContainerWidthChange: (Float) -> Unit,
    layoutMode: PretextLayoutMode,
    obstacleFraction: Float,
    onObstacleFractionChange: (Float) -> Unit,
    running: Boolean,
    onRunBenchmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPinned = attachState.isPinned
    PretextStickyAttachHaptics(attachState)

    val shadowElevation by animateDpAsState(
        targetValue = if (isPinned) 8.dp else 0.dp,
        animationSpec = if (isPinned) {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
        } else {
            snap()
        },
        label = "pretext_sticky_shadow",
    )
    val dividerAlpha by animateFloatAsState(
        targetValue = if (isPinned) 1f else 0f,
        animationSpec = if (isPinned) spring(stiffness = Spring.StiffnessMedium) else snap(),
        label = "pretext_sticky_divider",
    )
    val sectionPaddingH = 0.dp
    val sectionPaddingV = 0.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds(),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = shadowElevation.coerceAtLeast(0.dp),
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sectionPaddingH, vertical = sectionPaddingV),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PretextMeasureTextField(
                    value = customText,
                    onValueChange = onCustomTextChange,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (attachState.controlsAttached) {
                    Text(
                        text = "Container ${containerWidthDp.toInt()}dp",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    HapticSlider(
                        value = containerWidthDp,
                        onValueChange = onContainerWidthChange,
                        valueRange = 120f..360f,
                    )
                    if (layoutMode == PretextLayoutMode.VariableWidth ||
                        layoutMode == PretextLayoutMode.ReflowCompare
                    ) {
                        Text(
                            text = "Obstacle ${(obstacleFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        HapticSlider(
                            value = obstacleFraction,
                            onValueChange = onObstacleFractionChange,
                            valueRange = 0.25f..0.9f,
                        )
                    }
                }

                if (attachState.runAttached) {
                    Button(
                        enabled = !running,
                        onClick = onRunBenchmark,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (running) "Running…" else "Run benchmark")
                    }
                }
            }
        }
        PretextStickyStackDivider(alpha = dividerAlpha)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretextPlaygroundCameraHeaderAction(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    blurBackdropEnabled: Boolean = false,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
            scaleIn(initialScale = 0.82f, animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut(spring(stiffness = Spring.StiffnessHigh)) +
            scaleOut(targetScale = 0.82f, animationSpec = spring(stiffness = Spring.StiffnessHigh)),
    ) {
        val tooltipState = rememberAppTooltipState()

        AppTooltipTimedRevealEffect(
            state = tooltipState,
            reveal = true,
            visibleMillis = 5_000,
        )

        if (blurBackdropEnabled) {
            AppTooltipBox(
                state = tooltipState,
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Below,
                ),
                tooltip = { PlainTooltip { Text("Live camera") } },
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Outlined.Videocam,
                        contentDescription = "Open live camera",
                    )
                }
            }
        } else {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Below,
                ),
                tooltip = { PlainTooltip { Text("Live camera") } },
                state = tooltipState,
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Outlined.Videocam,
                        contentDescription = "Open live camera",
                    )
                }
            }
        }
    }
}

@Composable
private fun PretextStickyStackDivider(
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha.coerceIn(0f, 1f) },
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp,
    )
}
