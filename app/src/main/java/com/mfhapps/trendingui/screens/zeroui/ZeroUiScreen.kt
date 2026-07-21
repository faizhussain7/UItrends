@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.mfhapps.trendingui.screens.zeroui

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.ButtonGroup
import com.mfhapps.trendingui.ui.components.FilledTonalButton
import com.mfhapps.trendingui.ui.components.FloatingActionButton
import com.mfhapps.trendingui.ui.components.IconButton
import com.mfhapps.trendingui.ui.components.rememberExpressiveBadgeShape
import com.mfhapps.trendingui.ui.demo.DemoAnimatedSection
import com.mfhapps.trendingui.ui.detail.DemoHeroTitleCollapsingScaffold
import com.mfhapps.trendingui.ui.detail.LocalDetailChromeStyle
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.sin

private val SearchSuggestions = listOf(
    "Material Expressive",
    "Adaptive layouts",
    "Predictive back",
    "Zero UI chrome",
)

private const val AutoHideMs = 4_000L

@Composable
fun ZeroUiScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val scroll = rememberScrollState()
    var toolbarVisible by remember { mutableStateOf(true) }

    var wasImeOpen by remember { mutableStateOf(false) }
    LaunchedEffect(imeBottomPx) {
        val imeOpen = imeBottomPx > 0
        if (wasImeOpen && !imeOpen) focusManager.clearFocus()
        wasImeOpen = imeOpen
    }

    LaunchedEffect(scroll) {
        snapshotFlow { scroll.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (scrolling) {
                    focusManager.clearFocus(force = true)
                    keyboard?.hide()
                }
            }
    }

    LaunchedEffect(scroll) {
        var last = scroll.value
        snapshotFlow { scroll.value }
            .collect { value ->
                val delta = value - last
                last = value
                when {
                    delta > 8 -> toolbarVisible = false
                    delta < -8 -> toolbarVisible = true
                    value == 0 -> toolbarVisible = true
                }
            }
    }

    DemoHeroTitleCollapsingScaffold(
        title = "Zero UI",
        onNavigateBack = onNavigateBack,
        guide = guide,
        scrollValuePx = scroll.value,
        heroTitleThresholdDp = 96.dp,
        chromeStyle = LocalDetailChromeStyle.current,
        bottomPadding = 0.dp,
        modifier = Modifier.background(scheme.background),
    ) { contentPadding ->
        val listBottomPadding by animateDpAsState(
            targetValue = if (toolbarVisible) 96.dp else 16.dp,
            animationSpec = tween(220),
            label = "listBottomPadding",
        )
        CompositionLocalProvider(LocalContentColor provides scheme.onSurface) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(contentPadding)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = listBottomPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Zero UI",
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Chrome only when needed",
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    DemoAnimatedSection(index = 0) { FocusRevealDemo() }
                    DemoAnimatedSection(index = 1) { TapRevealDemo() }
                    DemoAnimatedSection(index = 2) { SelectionToolbarDemo() }
                    DemoAnimatedSection(index = 3) { ReaderEdgeDemo() }
                    DemoAnimatedSection(index = 4) { IdlePlayerDemo() }
                    DemoAnimatedSection(index = 5) { MapChromeDemo() }
                    DemoAnimatedSection(index = 6) { EdgeSwipeDemo() }
                    DemoAnimatedSection(index = 7) { AmbientChipDemo() }
                }

                AnimatedVisibility(
                    visible = toolbarVisible,
                    enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 2 },
                    exit = fadeOut(tween(140)) + slideOutVertically(tween(200)) { it / 2 },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Favorite")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoCard(
    label: String,
    hint: String,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = rememberExpressiveBadgeShape(seed = label.hashCode()),
                    color = scheme.primaryContainer,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        maxLines = 1,
                    )
                }
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            content()
        }
    }
}

@Composable
private fun FocusRevealDemo() {
    val scheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var query by remember { mutableStateOf("") }
    var focused by remember { mutableStateOf(false) }
    val chipScroll = rememberScrollState()

    DemoCard(label = "Focus", hint = "Tap field → suggestions") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = scheme.surfaceContainerHighest,
        ) {
            Column(
                Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!focused && query.isEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            "Tap the field — chips appear only while focused",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused },
                    placeholder = { Text("Search demos…") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )
                AnimatedVisibility(
                    visible = focused,
                    enter = fadeIn() + slideInVertically { it / 3 },
                    exit = fadeOut() + slideOutVertically { it / 3 },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(chipScroll)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = "Suggestions visible"
                            },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SearchSuggestions.forEach { label ->
                            FilterChip(
                                selected = query == label,
                                onClick = {
                                    query = label
                                    focusManager.clearFocus()
                                    keyboard?.hide()
                                },
                                label = {
                                    Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = scheme.surfaceContainerHigh,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TapRevealDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    var visible by remember { mutableStateOf(false) }
    var tick by remember { mutableLongStateOf(0L) }
    val hideTimer = remember { Animatable(1f) }

    LaunchedEffect(visible, tick) {
        if (!visible) {
            hideTimer.snapTo(1f)
            return@LaunchedEffect
        }
        hideTimer.snapTo(1f)
        hideTimer.animateTo(0f, tween(AutoHideMs.toInt(), easing = FastOutSlowInEasing))
        visible = false
    }

    DemoCard(label = "Tap", hint = "Tap photo → timed actions") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(scheme.tertiaryContainer.copy(alpha = 0.55f))
                .pointerInput(Unit) {
                    detectTapGestures {
                        visible = !visible
                        tick = System.currentTimeMillis()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(scheme.tertiary.copy(alpha = 0.35f)),
                )
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "Sunset boardwalk",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onTertiaryContainer,
                        maxLines = 1,
                    )
                    Text(
                        if (visible) "Actions expand below · auto-hide"
                        else "Tap this card to reveal actions",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onTertiaryContainer,
                    )
                }
            }
            AnimatedVisibility(
                visible = visible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface,
                    tonalElevation = 2.dp,
                ) {
                    Column(
                        Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilledTonalButton(onClick = { tick = System.currentTimeMillis() }) {
                                Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                                Text("Share", modifier = Modifier.padding(start = 4.dp))
                            }
                            FilledTonalButton(onClick = { tick = System.currentTimeMillis() }) {
                                Icon(Icons.Default.Bookmark, null, Modifier.size(16.dp))
                                Text("Save", Modifier.padding(start = 4.dp))
                            }
                        }
                        LinearWavyProgressIndicator(
                            progress = { hideTimer.value.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionToolbarDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val clipboard = remember(context) {
        context.getSystemService(ClipboardManager::class.java)
    }
    var selected by remember { mutableStateOf(false) }
    val sample = "Long-press this paragraph to select it. Edit chrome appears only while selected."

    DemoCard(label = "Select", hint = "Long-press → toolbar") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = if (selected) {
                scheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                scheme.surfaceContainerHighest
            },
            contentColor = scheme.onSurface,
        ) {
            Column(
                Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!selected) {
                    Text(
                        "Hold your finger on the text below",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = sample,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    selected = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onTap = { if (selected) selected = false },
                            )
                        },
                )
                AnimatedVisibility(
                    visible = selected,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    ButtonGroup {
                        ToggleButton(
                            checked = false,
                            onCheckedChange = {
                                clipboard?.setPrimaryClip(ClipData.newPlainText("text", sample))
                                selected = false
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            },
                        ) { Text("Copy") }
                        ToggleButton(
                            checked = false,
                            onCheckedChange = { selected = false },
                        ) { Text("Done") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderEdgeDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    var chrome by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }

    DemoCard(label = "Reader", hint = "Tap page → edge toolbar") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(scheme.surfaceContainerHighest)
                .pointerInput(Unit) {
                    detectTapGestures {
                        chrome = !chrome
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Chapter 3 · Quiet interfaces",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "The page stays calm until you tap. Controls expand under the copy — they never cover it.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
            if (!chrome) {
                Text(
                    "Tap to show reader chrome",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.primary,
                )
            }
            AnimatedVisibility(
                visible = chrome,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                        }
                        IconButton(onClick = { playing = !playing }) {
                            Icon(
                                if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Bookmark")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdlePlayerDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    var controls by remember { mutableStateOf(false) }
    var touch by remember { mutableLongStateOf(0L) }
    var playing by remember { mutableStateOf(true) }
    val hideTimer = remember { Animatable(1f) }

    LaunchedEffect(controls, touch) {
        if (!controls) {
            hideTimer.snapTo(1f)
            return@LaunchedEffect
        }
        hideTimer.snapTo(1f)
        hideTimer.animateTo(
            0f,
            tween(if (reduceMotion) 1_800 else AutoHideMs.toInt(), easing = FastOutSlowInEasing),
        )
        controls = false
    }

    DemoCard(label = "Idle", hint = "Touch wakes player") {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(controls) {
                    detectTapGestures {
                        if (!controls) {
                            controls = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        touch = System.currentTimeMillis()
                    }
                },
            shape = MaterialTheme.shapes.large,
            color = scheme.surfaceContainerHighest,
        ) {
            Column(
                Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(scheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (playing) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = scheme.onPrimaryContainer,
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Midnight Patterns",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                        Text(
                            if (controls) "Controls fade after idle"
                            else "Tap artwork to wake controls",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                AnimatedVisibility(visible = controls) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearWavyProgressIndicator(
                            progress = { 0.58f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            IconButton(onClick = { touch = System.currentTimeMillis() }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                            }
                            IconButton(
                                onClick = {
                                    playing = !playing
                                    touch = System.currentTimeMillis()
                                },
                            ) {
                                Icon(
                                    if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playing) "Pause" else "Play",
                                )
                            }
                            IconButton(onClick = { touch = System.currentTimeMillis() }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next")
                            }
                        }
                        LinearWavyProgressIndicator(
                            progress = { hideTimer.value.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = scheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapChromeDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    var chrome by remember { mutableStateOf(true) }
    var panHint by remember { mutableStateOf(true) }
    var mapOffsetX by remember { mutableFloatStateOf(0f) }
    var mapOffsetY by remember { mutableFloatStateOf(0f) }
    var lastPan by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastPan) {
        if (lastPan == 0L) return@LaunchedEffect
        chrome = false
        panHint = false
        delay(1_400)
        chrome = true
    }

    DemoCard(label = "Map", hint = "Drag map → chrome hides") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnimatedVisibility(
                visible = chrome,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Outlined.Search, null, Modifier.size(18.dp))
                        Text(
                            "Search this area",
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onSurface,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.largeIncreased)
                    .background(scheme.secondaryContainer.copy(alpha = 0.45f))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                lastPan = System.currentTimeMillis()
                                panHint = false
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            onDrag = { change, drag ->
                                change.consume()
                                mapOffsetX = (mapOffsetX + drag.x * 0.35f).coerceIn(-48f, 48f)
                                mapOffsetY = (mapOffsetY + drag.y * 0.35f).coerceIn(-48f, 48f)
                                lastPan = System.currentTimeMillis()
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val step = 36.dp.toPx()
                    val stroke = scheme.onSecondaryContainer.copy(alpha = 0.18f)
                    var x = mapOffsetX % step
                    while (x < size.width) {
                        drawLine(stroke, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2f)
                        x += step
                    }
                    var y = mapOffsetY % step
                    while (y < size.height) {
                        drawLine(stroke, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
                        y += step
                    }
                    drawCircle(
                        color = scheme.primary.copy(alpha = 0.55f),
                        radius = 10.dp.toPx(),
                        center = Offset(
                            size.width * 0.5f + mapOffsetX,
                            size.height * 0.55f + mapOffsetY,
                        ),
                    )
                }
                if (panHint) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = scheme.surfaceContainerHigh,
                        contentColor = scheme.onSurface,
                        tonalElevation = 3.dp,
                    ) {
                        Column(
                            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Icons.Filled.TouchApp,
                                contentDescription = null,
                                tint = scheme.primary,
                            )
                            Text(
                                "Drag this map",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                "Search + location tuck away while you pan",
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = chrome,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    FloatingActionButton(
                        onClick = {
                            chrome = true
                            mapOffsetX = 0f
                            mapOffsetY = 0f
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        },
                        containerColor = scheme.primaryContainer,
                        contentColor = scheme.onPrimaryContainer,
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My location")
                    }
                }
            }
        }
    }
}

@Composable
private fun EdgeSwipeDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val openPx = with(density) { 132.dp.toPx() }
    val open = offsetX > openPx * 0.45f

    DemoCard(label = "Edge", hint = "Pull the handle ←") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .clip(MaterialTheme.shapes.large)
                .background(scheme.surfaceContainerHighest)
                .pointerInput(openPx) {
                    detectDragGestures(
                        onDragEnd = {
                            offsetX = if (offsetX > openPx * 0.45f) openPx else 0f
                            if (offsetX > openPx * 0.45f) {
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            }
                        },
                        onDrag = { change, drag ->
                            change.consume()
                            offsetX = (offsetX - drag.x).coerceIn(0f, openPx)
                        },
                    )
                },
        ) {
            val panelInsetDp = with(density) { offsetX.toDp() }
            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, end = 40.dp + panelInsetDp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    if (open) "Dock open — swipe right to close" else "Pull the edge handle left",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Hidden tools stay off-canvas until the edge gesture reveals them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .width(6.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.primary.copy(alpha = 0.65f)),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.42f)
                    .height(148.dp)
                    .padding(start = with(density) { (openPx - offsetX).toDp() }),
                color = scheme.primaryContainer,
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
            ) {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = scheme.onPrimaryContainer)
                    Text(
                        "Quick edit",
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.onPrimaryContainer,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun AmbientChipDemo() {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    var listening by remember { mutableStateOf(false) }
    val infinite = rememberInfiniteTransition(label = "ambient")
    val pulse by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = if (reduceMotion || !listening) 0.55f else 1f,
        animationSpec = infiniteRepeatable(tween(1_100), RepeatMode.Reverse),
        label = "pulse",
    )
    val wave by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion || !listening) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(2_400), RepeatMode.Restart),
        label = "wave",
    )

    DemoCard(label = "Ambient", hint = "Tap to listen") {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(listening) {
                    detectTapGestures {
                        listening = !listening
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = if (listening) "Assistant listening" else "Assistant idle"
                },
            shape = MaterialTheme.shapes.large,
            color = scheme.surfaceContainerHighest,
            contentColor = scheme.onSurface,
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (listening) {
                            Box(
                                Modifier
                                    .size((40 + 10 * pulse).dp)
                                    .clip(CircleShape)
                                    .background(scheme.primary.copy(alpha = 0.16f * pulse)),
                            )
                        }
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (listening) scheme.primaryContainer else scheme.surfaceContainerHigh,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.Mic,
                                contentDescription = null,
                                tint = if (listening) {
                                    scheme.onPrimaryContainer
                                } else {
                                    scheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (listening) "Listening…" else "Idle — tap to wake",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            if (listening) "Waveform expands below" else "No chrome until you ask",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                AnimatedVisibility(
                    visible = listening,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Canvas(
                        Modifier
                            .fillMaxWidth()
                            .height(22.dp),
                    ) {
                        val bars = 16
                        val gap = 3.dp.toPx()
                        val barW = (size.width - gap * (bars - 1)) / bars
                        val color = scheme.primary
                        repeat(bars) { i ->
                            val phase = wave * 6.28f + i * 0.45f
                            val h = size.height *
                                (0.25f + 0.75f * ((sin(phase.toDouble()) + 1f) / 2f).toFloat())
                            val left = i * (barW + gap)
                            drawRoundRect(
                                color = color.copy(alpha = 0.75f),
                                topLeft = Offset(left, (size.height - h) / 2f),
                                size = androidx.compose.ui.geometry.Size(barW, h),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2f),
                            )
                        }
                    }
                }
            }
        }
    }
}
