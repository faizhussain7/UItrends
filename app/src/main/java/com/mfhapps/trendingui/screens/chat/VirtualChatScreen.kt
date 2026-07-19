package com.mfhapps.trendingui.screens.chat

import android.graphics.Typeface
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import com.mfhapps.trendingui.ui.motion.expressiveSpatialSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mfhapps.trendingui.core.text.MeasuredTextLayout
import com.mfhapps.trendingui.core.text.PreparedText
import com.mfhapps.trendingui.core.text.TextMeasurementEngine
import com.mfhapps.trendingui.ui.components.FloatingActionButton
import com.mfhapps.trendingui.ui.components.LoadingIndicator
import com.mfhapps.trendingui.ui.detail.DemoHeroTitleCollapsingScaffold
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ChatListItem {
    val key: String
    data class DateDivider(val label: String) : ChatListItem {
        override val key: String = "date_$label"
    }
    data class Bubble(val msg: ChatMessage) : ChatListItem {
        override val key: String = "msg_${msg.id}"
    }
}

data class ChatMessage(
    val id: Int,
    val isUser: Boolean,
    val layout: MeasuredTextLayout,
    val bubbleWidthPx: Int,
    val sender: String,
    val timestamp: String,
    val read: Boolean,
    val dayBucket: Int,
)

private val DAYS = listOf("Yesterday", "Today")

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun VirtualChatScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
) {
    val density = LocalDensity.current
    val scheme = MaterialTheme.colorScheme
    val fontSize = with(density) { 15.sp.toPx() / density.density }
    val lineHeightSp = 20.sp
    val lineHeightPx = with(density) { lineHeightSp.toPx() }
    val maxWidthPx = with(density) { 280.dp.roundToPx() }

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var streamingText by remember { mutableStateOf("") }
    var thinking by remember { mutableStateOf(true) }
    var streamPrepared by remember { mutableStateOf<PreparedText?>(null) }
    var streamLayout by remember { mutableStateOf<MeasuredTextLayout?>(null) }
    var streamWidthPx by remember { mutableIntStateOf(maxWidthPx) }
    var didInitialScroll by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val built = withContext(Dispatchers.Default) {
            (0 until 500).map { i ->
                val text = if (i % 3 == 0) "Short reply $i"
                else "Message $i with enough text to wrap inside a shrink-wrapped bubble."
                val sender = if (i % 2 == 0) "You" else "AI"
                val hour = 9 + (i / 30) % 12
                val min = (i * 2) % 60
                val ts = "%d:%02d %s".format(hour, min, if (hour < 12) "AM" else "PM")
                val prep = TextMeasurementEngine.prepareSync(text, fontSize, Typeface.DEFAULT)
                val (width, layout) = TextMeasurementEngine.findTightestWidth(
                    prep, targetLines = 4, lineHeightPx, 80, maxWidthPx,
                )
                ChatMessage(
                    id = i,
                    isUser = i % 2 == 0,
                    layout = layout,
                    bubbleWidthPx = width,
                    sender = sender,
                    timestamp = ts,
                    read = i < 480,
                    dayBucket = i / 250,
                )
            }
        }
        messages = built
    }

    val displayItems: List<ChatListItem> = remember(messages) {
        val out = mutableListOf<ChatListItem>()
        var lastDay = -1
        messages.forEach { msg ->
            if (msg.dayBucket != lastDay) {
                lastDay = msg.dayBucket
                val label = DAYS.getOrElse(msg.dayBucket) { "Day $lastDay" }
                out.add(ChatListItem.DateDivider(label))
            }
            out.add(ChatListItem.Bubble(msg))
        }
        out
    }

    LaunchedEffect(Unit) {
        delay(1200)
        thinking = false
        val full = "Streaming uses layout() per tick — prepare() runs once, zero View reflow. " +
            "Layout passes are also debounced via snapshotFlow.debounce to keep things smooth."
        val prepared = withContext(Dispatchers.Default) {
            TextMeasurementEngine.prepareSync(full, fontSize, Typeface.DEFAULT)
        }
        streamPrepared = prepared
        val (width, _) = TextMeasurementEngine.findTightestWidth(
            prepared, targetLines = 3, lineHeightPx, 80, maxWidthPx,
        )
        streamWidthPx = width
        scope.launch {
            flow {
                full.forEach { ch -> delay(30); emit(ch) }
            }.collect { streamingText += it }
        }
    }

    LaunchedEffect(streamPrepared, streamWidthPx) {
        val prep = streamPrepared ?: return@LaunchedEffect
        snapshotFlow { streamingText }
            .debounce(60)
            .collect { text ->
                if (text.isEmpty()) { streamLayout = null; return@collect }
                streamLayout = withContext(Dispatchers.Default) {
                    TextMeasurementEngine.layoutForPrefix(prep, text, streamWidthPx, lineHeightPx)
                }
            }
    }

    LaunchedEffect(messages.size) {
        if (messages.isEmpty() || didInitialScroll) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .first { it > 1 }
        val total = listState.layoutInfo.totalItemsCount
        if (total > 0) {
            listState.scrollToItem(total - 1)
            didInitialScroll = true
        }
    }

    LaunchedEffect(streamLayout?.lineCount, thinking) {
        if (!didInitialScroll) return@LaunchedEffect
        val info = listState.layoutInfo
        val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@LaunchedEffect
        val nearBottom = info.totalItemsCount - lastVisible < 4
        if (nearBottom && info.totalItemsCount > 0) {
            listState.animateScrollToItem(info.totalItemsCount - 1)
        }
    }

    val nearBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf true
            val total = info.totalItemsCount
            total - lastVisible < 4
        }
    }

    val heroTitleThresholdDp = 120.dp
    val heroThresholdPx = remember(density, heroTitleThresholdDp) {
        with(density) { heroTitleThresholdDp.roundToPx() }
    }
    val headerScrollPx by remember(listState, heroThresholdPx) {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset +
                listState.firstVisibleItemIndex * heroThresholdPx
        }
    }

    DemoHeroTitleCollapsingScaffold(
        title = "Virtual Chat",
        onNavigateBack = onNavigateBack,
        guide = guide,
        scrollValuePx = headerScrollPx,
        heroTitleThresholdDp = heroTitleThresholdDp,
        modifier = Modifier.fillMaxSize(),
        bottomPadding = 88.dp,
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item(key = "hero", contentType = "hero") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Virtual Chat",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onBackground,
                        )
                        Text(
                            text = "500 shrink-wrapped bubbles · debounced streaming layout",
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }

                items(
                    count = displayItems.size,
                    key = { displayItems[it].key },
                    contentType = {
                        when (displayItems[it]) {
                            is ChatListItem.DateDivider -> "date_divider"
                            is ChatListItem.Bubble -> if ((displayItems[it] as ChatListItem.Bubble).msg.isUser) "user_bubble" else "assistant_bubble"
                        }
                    },
                ) { index ->
                    when (val item = displayItems[index]) {
                        is ChatListItem.DateDivider -> DateDividerRow(
                            label = item.label,
                            modifier = Modifier.animateItem(),
                        )
                        is ChatListItem.Bubble -> MeasuredChatBubble(
                            msg = item.msg,
                            lineHeightSp = lineHeightSp,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                if (thinking) {
                    item(key = "thinking", contentType = "loading") {
                        Box(
                            Modifier
                                .padding(8.dp)
                                .animateItem(),
                        ) { LoadingIndicator() }
                    }
                } else if (streamingText.isNotEmpty()) {
                    item(key = "stream", contentType = "stream_bubble") {
                        val layout = streamLayout
                        if (layout != null) {
                            MeasuredStreamBubble(
                                layout = layout,
                                widthPx = streamWidthPx,
                                lineHeightSp = lineHeightSp,
                                modifier = Modifier
                                    .animateItem()
                                    .semantics {
                                        liveRegion = LiveRegionMode.Polite
                                        contentDescription = "AI streaming response"
                                    },
                            )
                        } else {
                            TypingDotsBubble(modifier = Modifier.animateItem())
                        }
                    }
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
                        scope.launch {
                            val total = listState.layoutInfo.totalItemsCount
                            if (total > 0) listState.animateScrollToItem(total - 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .graphicsLayer {
                            scaleX = fabVisibility
                            scaleY = fabVisibility
                            alpha = fabVisibility
                        },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Jump to latest")
                }
            }
        }
    }
}

@Composable
private fun DateDividerRow(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        )
    }
}

@Composable
private fun TypingDotsBubble(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center,
        ) {
            Text("AI", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondary)
        }
        Spacer(Modifier.size(6.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(3) { index ->
                val alpha by infinite.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        tween(600, delayMillis = index * 150),
                        RepeatMode.Reverse,
                    ),
                    label = "dot$index",
                )
                Text(
                    "•",
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = alpha),
                )
            }
        }
    }
}

@Composable
private fun MeasuredChatBubble(
    msg: ChatMessage,
    lineHeightSp: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val isUser = msg.isUser
    val color = if (isUser) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.secondaryContainer
    val onColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSecondaryContainer
    val avatarColor = if (isUser) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.secondary

    val bubbleHeight = with(density) { (msg.layout.height + 20.dp.toPx()).toDp() }
    val bubbleWidth = with(density) { msg.bubbleWidthPx.toDp() }
    val receipt = if (msg.read) "Read" else "Sent"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${msg.sender}: ${msg.layout.lines.joinToString(" ")} at ${msg.timestamp}. $receipt."
            },
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center,
            ) {
                Text("AI", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondary)
            }
            Spacer(Modifier.size(6.dp))
        }

        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .height(bubbleHeight)
                    .then(if (msg.layout.lineCount <= 2) Modifier.widthIn(max = bubbleWidth) else Modifier)
                    .clip(RoundedCornerShape(18.dp))
                    .background(color)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                msg.layout.lines.forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodyMedium,
                        color = onColor, lineHeight = lineHeightSp, maxLines = 1)
                }
            }
            Row(
                Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
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
                        color = if (msg.read) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (isUser) {
            Spacer(Modifier.size(6.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center,
            ) {
                Text("Me", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun MeasuredStreamBubble(
    layout: MeasuredTextLayout,
    widthPx: Int,
    lineHeightSp: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val color = MaterialTheme.colorScheme.secondaryContainer
    val bubbleHeight = with(density) { (layout.height + 20.dp.toPx()).toDp() }
    val bubbleWidth = with(density) { widthPx.toDp() }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center,
        ) {
            Text("AI", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondary)
        }
        Spacer(Modifier.size(6.dp))
        Column(
            modifier = Modifier
                .widthIn(max = bubbleWidth)
                .height(bubbleHeight)
                .clip(RoundedCornerShape(18.dp))
                .background(color)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            layout.lines.forEach { line ->
                Text(line, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = lineHeightSp, maxLines = 1)
            }
        }
    }
}
