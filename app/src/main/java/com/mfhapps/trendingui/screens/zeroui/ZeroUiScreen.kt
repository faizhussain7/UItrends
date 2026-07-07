package com.mfhapps.trendingui.screens.zeroui

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.mfhapps.trendingui.ui.components.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import com.mfhapps.trendingui.ui.components.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.ButtonGroup
import com.mfhapps.trendingui.ui.demo.DemoAnimatedSection
import com.mfhapps.trendingui.ui.detail.DemoPaneHeader
import com.mfhapps.trendingui.ui.detail.LocalDetailPaneActive
import com.mfhapps.trendingui.ui.detail.demoDetailScrollBottomGap
import com.mfhapps.trendingui.ui.detail.demoDetailScrollInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

private val SearchSuggestions = listOf(
    "Trending UI patterns",
    "Material Expressive",
    "Compose adaptive layouts",
    "Predictive back gesture",
    "Zero UI chrome",
)

private const val AutoHideMs = 5_000L

@Composable
fun ZeroUiScreen() {
    val scheme = MaterialTheme.colorScheme
    val inDetailPane = LocalDetailPaneActive.current
    val scroll = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val keyboard = LocalSoftwareKeyboardController.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)

    LaunchedEffect(scroll) {
        snapshotFlow { scroll.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (scrolling) {
                    focusManager.clearFocus()
                    keyboard?.hide()
                }
            }
    }

    var wasImeOpen by remember { mutableStateOf(false) }
    LaunchedEffect(imeBottomPx) {
        val imeOpen = imeBottomPx > 0
        if (wasImeOpen && !imeOpen) {
            focusManager.clearFocus()
        }
        wasImeOpen = imeOpen
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .demoDetailScrollInsets()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp)
            .padding(top = if (inDetailPane) 8.dp else 16.dp)
            .demoDetailScrollBottomGap(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DemoPaneHeader(
            title = "Zero UI",
            subtitle = "Chrome appears only when focus, gesture, or idle rules demand it.",
        )

        DemoAnimatedSection(index = 0) {
            ZeroUiSection(
                title = "Content first",
                body = "Readers, maps, and assistants hide toolbars until the user signals intent — tap, focus, or long-press.",
            )
        }

        DemoAnimatedSection(index = 1) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZeroUiSection(
                    title = "Focus reveals suggestions",
                    body = "Chips show only while focused — scrolling or dismissing the keyboard clears focus.",
                )
                FocusSearchDemo()
            }
        }

        DemoAnimatedSection(index = 2) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZeroUiSection(
                    title = "Tap to reveal actions",
                    body = "Tap the content area — actions slide in and auto-hide after five seconds of idle time.",
                )
                TapRevealActionsDemo()
            }
        }

        DemoAnimatedSection(index = 3) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZeroUiSection(
                    title = "Long-press selection",
                    body = "No permanent edit toolbar — long-press enters selection mode with a compact action group.",
                )
                LongPressSelectDemo()
            }
        }

        DemoAnimatedSection(index = 4) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZeroUiSection(
                    title = "Reader chrome",
                    body = "Tap the article canvas to peek playback-style controls along the bottom edge.",
                )
                ReaderChromeDemo()
            }
        }

        DemoAnimatedSection(index = 5) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZeroUiSection(
                    title = "Idle hides controls",
                    body = "Touch the player to show transport controls; they fade away when you stop interacting.",
                )
                IdleMediaChromeDemo()
            }
        }
    }
}

@Composable
private fun ZeroUiSection(title: String, body: String) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.primary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ZeroUiPlaySurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
    ) {
        Box(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun FocusSearchDemo() {
    val scheme = MaterialTheme.colorScheme
    val reduceMotion = LocalReduceMotion.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var query by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    val showSuggestions = searchFocused

    ZeroUiPlaySurface {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { searchFocused = it.isFocused },
                placeholder = {
                    Text("Ask anything…", color = scheme.onSurfaceVariant)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )

            AnimatedVisibility(
                visible = showSuggestions,
                enter = fadeIn(tween(220)) + slideInVertically { it / 3 },
                exit = fadeOut(tween(160)) + slideOutVertically { it / 3 },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SearchSuggestions.forEachIndexed { index, label ->
                        var chipVisible by remember { mutableStateOf(reduceMotion) }
                        val matches = query.isEmpty() ||
                            label.contains(query, ignoreCase = true)

                        LaunchedEffect(showSuggestions, index, reduceMotion) {
                            if (!showSuggestions || !matches) {
                                chipVisible = false
                                return@LaunchedEffect
                            }
                            if (reduceMotion) {
                                chipVisible = true
                            } else {
                                chipVisible = false
                                delay(index * 55L)
                                chipVisible = true
                            }
                        }

                        AnimatedVisibility(visible = chipVisible && matches) {
                            FilterChip(
                                selected = query.equals(label, ignoreCase = true),
                                onClick = {
                                    query = label
                                    focusManager.clearFocus()
                                    keyboard?.hide()
                                },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = scheme.surfaceContainerHigh,
                                    labelColor = scheme.onSurface,
                                ),
                            )
                        }
                    }
                }
            }

            if (!showSuggestions) {
                Text(
                    text = "Focus the field to surface suggestions.",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TapRevealActionsDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    var actionsVisible by remember { mutableStateOf(false) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val hideTimer = remember { Animatable(1f) }

    LaunchedEffect(actionsVisible, lastInteraction) {
        if (!actionsVisible) {
            hideTimer.snapTo(1f)
            return@LaunchedEffect
        }
        hideTimer.snapTo(1f)
        hideTimer.animateTo(
            targetValue = 0f,
            animationSpec = tween(AutoHideMs.toInt(), easing = FastOutSlowInEasing),
        )
        actionsVisible = false
    }

    ZeroUiPlaySurface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures {
                        actionsVisible = !actionsVisible
                        lastInteraction = System.currentTimeMillis()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = actionsVisible,
                enter = slideInVertically { it } + fadeIn(tween(240)),
                exit = slideOutVertically { it } + fadeOut(tween(180)),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        listOf(
                            Icons.Default.Share to "Share",
                            Icons.Default.BookmarkBorder to "Save",
                            Icons.Default.MoreHoriz to "More",
                        ).forEach { (icon, label) ->
                            FilledTonalButton(
                                onClick = { lastInteraction = System.currentTimeMillis() },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(start = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
                    LinearWavyProgressIndicator(
                        progress = { hideTimer.value.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = scheme.primary,
                        trackColor = scheme.surfaceContainerHighest,
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Tap anywhere here to toggle contextual actions. " +
                        "They hide automatically after five seconds.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp),
                )
            }

            Text(
                text = if (actionsVisible) "Actions visible — timer running" else "Actions hidden",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LongPressSelectDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val clipboard = remember(context) {
        context.getSystemService(ClipboardManager::class.java)
    }
    var multiSelect by remember { mutableStateOf(false) }
    val sampleText =
        "Long-press this paragraph to enter selection mode. Copy or dismiss without a persistent toolbar."

    ZeroUiPlaySurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectionContainer {
                Text(
                    text = sampleText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    multiSelect = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                            )
                        },
                )
            }

            AnimatedVisibility(
                visible = multiSelect,
                enter = fadeIn(tween(220)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(160)) + slideOutVertically { it / 2 },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Selection mode",
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.primary,
                    )
                    ButtonGroup {
                        ToggleButton(
                            checked = true,
                            onCheckedChange = {
                                clipboard?.setPrimaryClip(
                                    ClipData.newPlainText("plain text", sampleText),
                                )
                                multiSelect = false
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            },
                        ) {
                            Text("Copy")
                        }
                        ToggleButton(
                            checked = false,
                            onCheckedChange = { multiSelect = false },
                        ) {
                            Text("Done")
                        }
                    }
                }
            }

            if (!multiSelect) {
                Text(
                    text = "Hint: long-press the text above.",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReaderChromeDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    var chromeVisible by remember { mutableStateOf(false) }
    val chromeBarHeight = 52.dp

    ZeroUiPlaySurface {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(scheme.surfaceContainerHighest)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            chromeVisible = !chromeVisible
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .padding(bottom = if (chromeVisible) chromeBarHeight else 0.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Text(
                        text = "Chapter 3 · Ambient reading",
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Tap the canvas to peek bottom chrome. Text stays above the bar when controls show.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                ReaderChromeBarOverlay(
                    visible = chromeVisible,
                    barHeight = chromeBarHeight,
                    onDismiss = { chromeVisible = false },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            Text(
                text = if (chromeVisible) "Chrome visible — tap canvas to hide" else "Chrome hidden — tap canvas to show",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ReaderChromeBarOverlay(
    visible: Boolean,
    barHeight: Dp,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically { it } + fadeIn(tween(260)),
        exit = slideOutVertically { it } + fadeOut(tween(200)),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            color = scheme.surface.copy(alpha = 0.96f),
            tonalElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next")
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Bookmark, contentDescription = "Bookmark")
                }
            }
        }
    }
}

@Composable
private fun IdleMediaChromeDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    var controlsVisible by remember { mutableStateOf(false) }
    var lastTouch by remember { mutableLongStateOf(0L) }
    var playing by remember { mutableStateOf(true) }
    val hideTimer = remember { Animatable(1f) }

    fun bumpInteraction() {
        lastTouch = System.currentTimeMillis()
    }

    LaunchedEffect(controlsVisible, lastTouch) {
        if (!controlsVisible) {
            hideTimer.snapTo(1f)
            return@LaunchedEffect
        }
        hideTimer.snapTo(1f)
        hideTimer.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = if (reduceMotion) 2_000 else AutoHideMs.toInt(),
                easing = FastOutSlowInEasing,
            ),
        )
        controlsVisible = false
    }

    ZeroUiPlaySurface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(controlsVisible) {
                    detectTapGestures {
                        if (!controlsVisible) {
                            controlsVisible = true
                            bumpInteraction()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } else {
                            bumpInteraction()
                        }
                    }
                },
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(scheme.primaryContainer),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Midnight Patterns",
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Demo Studio · 3:42",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            @OptIn(ExperimentalMaterial3ExpressiveApi::class)
            LinearWavyProgressIndicator(
                progress = { 0.58f },
                modifier = Modifier.fillMaxWidth(),
            )

            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(tween(220)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(180)) + slideOutVertically { it / 2 },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { bumpInteraction() }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                        }
                        IconButton(
                            onClick = {
                                playing = !playing
                                bumpInteraction()
                            },
                        ) {
                            Icon(
                                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                            )
                        }
                        IconButton(onClick = { bumpInteraction() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next")
                        }
                    }
                    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
                    LinearWavyProgressIndicator(
                        progress = { hideTimer.value.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = scheme.error,
                        trackColor = scheme.surfaceContainerHighest,
                    )
                }
            }

            Text(
                text = when {
                    controlsVisible -> "Controls visible — auto-hide timer running"
                    else -> "Controls hidden — tap player to show"
                },
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
