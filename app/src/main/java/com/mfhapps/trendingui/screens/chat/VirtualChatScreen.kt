@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)

package com.mfhapps.trendingui.screens.chat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mfhapps.trendingui.core.text.MeasuredTextLayout
import com.mfhapps.trendingui.ui.components.ContainedLoadingIndicator
import com.mfhapps.trendingui.ui.components.FilledIconButton
import com.mfhapps.trendingui.ui.components.FilledTonalButton
import com.mfhapps.trendingui.ui.components.FloatingActionButton
import com.mfhapps.trendingui.ui.components.rememberExpressiveBadgeShape
import com.mfhapps.trendingui.ui.detail.DemoHeroTitleCollapsingScaffold
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import com.mfhapps.trendingui.ui.motion.expressiveSpatialSpec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val ComposerShape = RoundedCornerShape(28.dp)
private val SuggestionChipShape = RoundedCornerShape(50)

@Composable
fun VirtualChatScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
    viewModel: VirtualChatViewModel = viewModel(),
) {
    val density = LocalDensity.current
    val scheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lineHeightSp = 20.sp

    val measureConfig = remember(density.density, density.fontScale) {
        ChatMeasureConfig(
            fontSizeSp = with(density) { 15.sp.toPx() / density.density },
            lineHeightPx = with(density) { lineHeightSp.toPx() },
            maxWidthPx = with(density) { 280.dp.roundToPx() },
            minWidthPx = with(density) { 132.dp.roundToPx() },
            bubblePaddingPx = with(density) { 8.dp.toPx() },
        )
    }
    LaunchedEffect(measureConfig) {
        viewModel.onMeasureConfig(measureConfig)
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.loadState) {
        if (uiState.loadState !is ChatLoadState.Ready) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .first { it > 1 }
        listState.scrollToItem(0)
    }

    LaunchedEffect(uiState.messages.size, uiState.streamPhase) {
        if (uiState.loadState !is ChatLoadState.Ready) return@LaunchedEffect
        if (listState.firstVisibleItemIndex <= 2) {
            listState.animateScrollToItem(0)
        }
    }

    val nearBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex <= 1
        }
    }

    val heroTitleThresholdDp = 120.dp
    val heroThresholdPx = remember(density, heroTitleThresholdDp) {
        with(density) { heroTitleThresholdDp.roundToPx() }
    }
    val headerScrollPx by remember(listState, heroThresholdPx, uiState.displayItems.size) {
        derivedStateOf {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            if (total <= 1) return@derivedStateOf 0
            val lastVisible = info.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf 0
            val distanceFromTop = (total - 1 - lastVisible.index) * heroThresholdPx +
                (lastVisible.size - lastVisible.offset).coerceAtLeast(0)
            distanceFromTop
        }
    }

    DemoHeroTitleCollapsingScaffold(
        title = "Virtual Chat",
        onNavigateBack = onNavigateBack,
        guide = guide,
        scrollValuePx = headerScrollPx,
        heroTitleThresholdDp = heroTitleThresholdDp,
        modifier = Modifier.fillMaxSize(),
        bottomPadding = 0.dp,
    ) { contentPadding ->
        val topBarPadding = contentPadding.calculateTopPadding()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
            ) {
                when (val load = uiState.loadState) {
                    ChatLoadState.Idle, ChatLoadState.Loading -> {
                        ChatLoadingState(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = topBarPadding),
                        )
                    }
                    is ChatLoadState.Failed -> {
                        ChatErrorState(
                            message = load.message,
                            onRetry = viewModel::retryLoad,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = topBarPadding),
                        )
                    }
                    ChatLoadState.Ready -> {
                        val reversedItems = remember(uiState.displayItems) {
                            uiState.displayItems.asReversed()
                        }
                        LazyColumn(
                            state = listState,
                            reverseLayout = true,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(
                                top = 12.dp + topBarPadding,
                                bottom = 8.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            when (uiState.streamPhase) {
                                ChatStreamPhase.Thinking -> {
                                    item(key = "thinking", contentType = "typing") {
                                        TypingDotsBubble()
                                    }
                                }
                                ChatStreamPhase.Streaming -> {
                                    item(key = "stream", contentType = "stream_bubble") {
                                        val layout = uiState.streamLayout
                                        if (layout != null) {
                                            MeasuredStreamBubble(
                                                layout = layout,
                                                widthPx = uiState.streamWidthPx,
                                                lineHeightSp = lineHeightSp,
                                                modifier = Modifier.semantics {
                                                    liveRegion = LiveRegionMode.Polite
                                                    contentDescription = "AI streaming response"
                                                },
                                            )
                                        } else {
                                            TypingDotsBubble()
                                        }
                                    }
                                }
                                ChatStreamPhase.Idle -> Unit
                            }
                            items(
                                count = reversedItems.size,
                                key = { reversedItems[it].key },
                                contentType = {
                                    when (val item = reversedItems[it]) {
                                        is ChatListItem.DateDivider -> "date_divider"
                                        is ChatListItem.Bubble ->
                                            if (item.msg.isUser) "user_bubble" else "assistant_bubble"
                                    }
                                },
                            ) { index ->
                                when (val item = reversedItems[index]) {
                                    is ChatListItem.DateDivider -> DateDividerRow(label = item.label)
                                    is ChatListItem.Bubble -> MeasuredChatBubble(
                                        msg = item.msg,
                                        lineHeightSp = lineHeightSp,
                                    )
                                }
                            }
                            item(key = "hero", contentType = "hero") {
                                ChatHeroHeader()
                            }
                        }

                        val fabVisibility by animateFloatAsState(
                            targetValue = if (!nearBottom) 1f else 0f,
                            animationSpec = expressiveSpatialSpec(),
                            label = "fabVisibility",
                        )
                        if (fabVisibility > 0.01f) {
                            FloatingActionButton(
                                onClick = {
                                    scope.launch { listState.animateScrollToItem(0) }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .graphicsLayer {
                                        scaleX = fabVisibility
                                        scaleY = fabVisibility
                                        alpha = fabVisibility
                                    },
                                containerColor = scheme.primaryContainer,
                                contentColor = scheme.onPrimaryContainer,
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Jump to latest",
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.loadState is ChatLoadState.Ready) {
                ChatComposerBar(
                    draft = uiState.draft,
                    suggestions = uiState.suggestions,
                    isStreaming = uiState.isStreaming,
                    canSend = uiState.canSend,
                    onDraftChange = viewModel::onDraftChange,
                    onSend = viewModel::sendDraft,
                    onStop = viewModel::stopStreaming,
                    onSuggestionClick = viewModel::onSuggestionClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ChatHeroHeader(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp, start = 4.dp, end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Virtual Chat",
            style = MaterialTheme.typography.headlineLargeEmphasized,
            color = scheme.onBackground,
        )
        Text(
            text = "500 shrink-wrapped bubbles · expressive streaming composer",
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurfaceVariant,
        )
        Surface(
            shape = MaterialTheme.shapes.largeIncreased,
            color = scheme.secondaryContainer.copy(alpha = 0.55f),
            contentColor = scheme.onSecondaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Send a prompt to stream a measured reply — history stays virtualized.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ChatLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ContainedLoadingIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Measuring 500 bubbles...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChatErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Couldn't load chat",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        FilledTonalButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Retry")
        }
    }
}

@Composable
private fun ChatComposerBar(
    draft: String,
    suggestions: List<String>,
    isStreaming: Boolean,
    canSend: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        tonalElevation = 3.dp,
        shadowElevation = 0.dp,
        color = scheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                suggestions.forEach { suggestion ->
                    FilterChip(
                        selected = false,
                        onClick = { onSuggestionClick(suggestion) },
                        label = { Text(suggestion) },
                        shape = SuggestionChipShape,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = scheme.surfaceContainerHighest,
                            labelColor = scheme.onSurface,
                        ),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    enabled = true,
                    placeholder = { Text("Message Virtual Chat…") },
                    shape = ComposerShape,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { if (canSend || draft.isNotBlank()) onSend() },
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = scheme.surfaceContainerHighest,
                        unfocusedContainerColor = scheme.surfaceContainerHighest,
                        disabledContainerColor = scheme.surfaceContainerHighest,
                        focusedBorderColor = scheme.outlineVariant,
                        unfocusedBorderColor = scheme.outlineVariant.copy(alpha = 0.5f),
                    ),
                )
                FilledIconButton(
                    onClick = { if (isStreaming) onStop() else onSend() },
                    enabled = isStreaming || canSend,
                    modifier = Modifier.size(56.dp),
                    shape = MaterialTheme.shapes.largeIncreased,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary,
                        disabledContainerColor = scheme.surfaceContainerHighest,
                        disabledContentColor = scheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = if (isStreaming) {
                            Icons.Outlined.Stop
                        } else {
                            Icons.AutoMirrored.Outlined.Send
                        },
                        contentDescription = if (isStreaming) "Stop generating" else "Send",
                    )
                }
            }
        }
    }
}

@Composable
private fun chatBubbleShape(isUser: Boolean) = RoundedCornerShape(
    topStart = 22.dp,
    topEnd = 22.dp,
    bottomStart = if (isUser) 22.dp else 6.dp,
    bottomEnd = if (isUser) 6.dp else 22.dp,
)

@Composable
private fun DateDividerRow(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        )
        Surface(
            shape = SuggestionChipShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        )
    }
}

@Composable
private fun ChatAvatar(
    label: String,
    isUser: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = rememberExpressiveBadgeShape(seed = if (isUser) 11 else 71)
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(shape)
            .background(if (isUser) scheme.primary else scheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isUser) scheme.onPrimary else scheme.onSecondary,
        )
    }
}

@Composable
private fun TypingDotsBubble(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .semantics { contentDescription = "AI is typing" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        ChatAvatar(label = "AI", isUser = false)
        Spacer(modifier = Modifier.size(8.dp))
        Row(
            modifier = Modifier
                .clip(chatBubbleShape(isUser = false))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(3) { index ->
                val alpha by infinite.animateFloat(
                    initialValue = 0.25f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        tween(560, delayMillis = index * 140),
                        RepeatMode.Reverse,
                    ),
                    label = "dot$index",
                )
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = alpha),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun MeasuredChatBubble(
    msg: ChatMessage,
    lineHeightSp: TextUnit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val isUser = msg.isUser
    val color = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val onColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    val bubbleHeight = with(density) { (msg.layout.height + 22.dp.toPx()).toDp() }
    val bubbleWidth = with(density) { msg.bubbleWidthPx.toDp() }
    val receipt = if (msg.read) "Read" else "Sent"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "${msg.sender}: ${msg.layout.lines.joinToString(" ")} at ${msg.timestamp}. $receipt."
            },
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUser) {
            ChatAvatar(label = "AI", isUser = false)
            Spacer(modifier = Modifier.size(8.dp))
        }
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Column(
                modifier = Modifier
                    .widthIn(min = bubbleWidth, max = 280.dp)
                    .height(bubbleHeight)
                    .clip(chatBubbleShape(isUser))
                    .background(color)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                msg.layout.lines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onColor,
                        lineHeight = lineHeightSp,
                        maxLines = 1,
                    )
                }
            }
            Row(
                Modifier.padding(top = 3.dp, start = 6.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "${msg.sender}  ${msg.timestamp}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isUser) {
                    Text(
                        text = if (msg.read) "✓✓" else "✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (msg.read) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
        if (isUser) {
            Spacer(modifier = Modifier.size(8.dp))
            ChatAvatar(label = "Me", isUser = true)
        }
    }
}

@Composable
private fun MeasuredStreamBubble(
    layout: MeasuredTextLayout,
    widthPx: Int,
    lineHeightSp: TextUnit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val bubbleHeight = with(density) { (layout.height + 22.dp.toPx()).toDp() }
    val bubbleWidth = with(density) { widthPx.toDp() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        ChatAvatar(label = "AI", isUser = false)
        Spacer(modifier = Modifier.size(8.dp))
        Column(
            modifier = Modifier
                .widthIn(min = bubbleWidth, max = 280.dp)
                .height(bubbleHeight)
                .clip(chatBubbleShape(isUser = false))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            layout.lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = lineHeightSp,
                    maxLines = 1,
                )
            }
        }
    }
}
