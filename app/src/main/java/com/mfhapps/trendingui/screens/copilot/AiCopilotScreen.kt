@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mfhapps.trendingui.screens.copilot

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import com.mfhapps.trendingui.ui.components.ButtonGroup
import com.mfhapps.trendingui.ui.components.ContainedLoadingIndicator
import com.mfhapps.trendingui.ui.components.FilledTonalButton
import com.mfhapps.trendingui.ui.detail.DemoHeroTitleCollapsingScaffold
import com.mfhapps.trendingui.ui.detail.LocalDetailChromeStyle
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

private val CopilotDockShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val CopilotCardShape = RoundedCornerShape(22.dp)
private val CopilotChipShape = RoundedCornerShape(50)

private enum class CopilotAction(val label: String) {
    Summarize("Summarize"),
    Rewrite("Rewrite"),
}

private val MOCK_RESPONSES = mapOf(
    CopilotAction.Summarize to """
        ## Summary
        Compose apps stream LLM tokens **word-by-word**. The layout engine re-measures
        on a *debounced cadence* so UI stays smooth even at high token rates.

        Key points:
        - Token chunks are buffered until whitespace
        - Layout pass runs once every `80ms`
        - The live region announces incremental progress to TalkBack
    """.trimIndent(),
    CopilotAction.Rewrite to """
        ## Rewrite
        Modern AI copilots emit words **progressively**. To keep the interface responsive,
        layout measurements are batched via a short debounce window rather than running
        on every character.

        ```kotlin
        snapshotFlow { response }
            .debounce(80)
            .collect { measureAndShow(it) }
        ```
    """.trimIndent(),
)

private enum class StreamPhase { Thinking, Searching, Generating, Done }

@Composable
fun AiCopilotScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val docScroll = rememberScrollState()

    var response by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf(StreamPhase.Done) }
    var actionIndex by remember { mutableIntStateOf(0) }
    var streamJob by remember { mutableStateOf<Job?>(null) }
    var prompt by remember { mutableStateOf("") }
    var lastUserPrompt by remember { mutableStateOf<String?>(null) }
    var dockExpanded by remember { mutableStateOf(false) }

    val isStreaming = phase == StreamPhase.Thinking ||
        phase == StreamPhase.Searching ||
        phase == StreamPhase.Generating

    fun expandDock() {
        dockExpanded = true
    }

    fun collapseDock() {
        dockExpanded = false
    }

    fun stream(text: String) {
        streamJob?.cancel()
        response = ""
        phase = StreamPhase.Thinking
        expandDock()
        streamJob = scope.launch {
            delay(500)
            phase = StreamPhase.Searching
            delay(600)
            phase = StreamPhase.Generating
            val wordBuffer = StringBuilder()
            flow {
                text.forEach { ch ->
                    delay(40)
                    wordBuffer.append(ch)
                    if (ch.isWhitespace() || ch == '.' || ch == '\n') {
                        emit(wordBuffer.toString())
                        wordBuffer.clear()
                    }
                }
                if (wordBuffer.isNotEmpty()) emit(wordBuffer.toString())
            }.collect { word ->
                response += word
            }
            phase = StreamPhase.Done
        }
    }

    fun startAction(action: CopilotAction) {
        lastUserPrompt = null
        stream(MOCK_RESPONSES[action] ?: "")
    }

    fun sendPrompt(userPrompt: String) {
        val trimmed = userPrompt.trim()
        if (trimmed.isEmpty()) return
        lastUserPrompt = trimmed
        prompt = ""
        val reply = "**You asked:** $trimmed\n\n" +
            "Here's a mocked answer with `inline code`, a *italic* phrase and a bullet:\n\n" +
            "- Streaming continues to render markdown progressively"
        stream(reply)
    }

    BackHandler(enabled = dockExpanded) {
        collapseDock()
    }

    DemoHeroTitleCollapsingScaffold(
        title = "AI Copilot",
        onNavigateBack = onNavigateBack,
        guide = guide,
        scrollValuePx = docScroll.value,
        heroTitleThresholdDp = 48.dp,
        chromeStyle = LocalDetailChromeStyle.current,
        bottomPadding = 0.dp,
        modifier = Modifier.background(Color.Transparent),
    ) { contentPadding ->
        CompositionLocalProvider(LocalContentColor provides scheme.onSurface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
            ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(docScroll)
                    .padding(top = contentPadding.calculateTopPadding())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "AI Copilot",
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    CopilotLiveStatusChip()
                }
                Text(
                    text = "Document stays readable. Assistant lives in the dock.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                CopilotIndexingBanner()
                CopilotDocumentPreview()
                CopilotContextRail()
                CopilotSuggestedEdits()
                CopilotSourcesCard()
                CopilotOutlineCard()
            }

            CopilotAssistantDock(
                expanded = dockExpanded,
                onToggleExpanded = {
                    if (dockExpanded) collapseDock() else expandDock()
                },
                onExpand = { expandDock() },
                onCollapse = { collapseDock() },
                phase = phase,
                response = response,
                isStreaming = isStreaming,
                actionIndex = actionIndex,
                lastUserPrompt = lastUserPrompt,
                prompt = prompt,
                onPromptChange = { prompt = it },
                onSendPrompt = { sendPrompt(prompt) },
                onActionSelected = { index ->
                    actionIndex = index
                    startAction(CopilotAction.entries[index])
                },
                onStop = {
                    streamJob?.cancel()
                    phase = StreamPhase.Done
                },
            )
            }
        }
    }
}

@Composable
fun CopilotLiveStatusChip(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .clip(CopilotChipShape)
            .background(scheme.surfaceContainerHighest)
            .border(1.dp, scheme.primary.copy(alpha = 0.35f), CopilotChipShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(scheme.primary),
        )
        Text(
            text = "Live",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )
    }
}

@Composable
private fun CopilotAssistantDock(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    phase: StreamPhase,
    response: String,
    isStreaming: Boolean,
    actionIndex: Int,
    lastUserPrompt: String?,
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSendPrompt: () -> Unit,
    onActionSelected: (Int) -> Unit,
    onStop: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var dragAccum by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(240)),
        shape = CopilotDockShape,
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp)
                    .pointerInput(expanded) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                when {
                                    dragAccum < -48f -> onExpand()
                                    dragAccum > 48f -> onCollapse()
                                }
                                dragAccum = 0f
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragAccum += dragAmount
                            },
                        )
                    }
                    .clickable(onClick = onToggleExpanded),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(width = 40.dp, height = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(scheme.onSurfaceVariant.copy(alpha = 0.35f)),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Icon(
                            if (expanded) Icons.Outlined.KeyboardArrowDown
                            else Icons.Outlined.KeyboardArrowUp,
                            contentDescription = if (expanded) "Collapse assistant" else "Expand assistant",
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = if (expanded) "Swipe down to collapse" else "Swipe up or tap to expand",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DecorativeIcon(Icons.Outlined.AutoAwesome, tint = scheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    AnimatedContent(
                        targetState = phase,
                        transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(160)) },
                        label = "phase",
                    ) { p ->
                        Text(
                            text = when (p) {
                                StreamPhase.Thinking -> "Thinking…"
                                StreamPhase.Searching -> "Searching…"
                                StreamPhase.Generating -> "Streaming…"
                                StreamPhase.Done -> if (expanded) "Ready" else "Peek dock"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                CopilotPhaseBadge(phase = phase)
                if (isStreaming) {
                    StopButton(onStop = onStop)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { if (it.isFocused) onExpand() },
                    placeholder = { Text("Ask about this document…") },
                    singleLine = true,
                    enabled = !isStreaming,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = scheme.onSurface,
                        unfocusedTextColor = scheme.onSurface,
                        focusedPlaceholderColor = scheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = scheme.onSurfaceVariant,
                        cursorColor = scheme.primary,
                        focusedBorderColor = scheme.primary.copy(alpha = 0.55f),
                        unfocusedBorderColor = scheme.outlineVariant,
                        focusedContainerColor = scheme.surfaceContainerHighest,
                        unfocusedContainerColor = scheme.surfaceContainerHigh,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendPrompt() }),
                )
                FilledIconButton(
                    onClick = onSendPrompt,
                    enabled = !isStreaming && prompt.isNotBlank(),
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary,
                    ),
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send prompt",
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        CopilotAction.entries.forEachIndexed { index, item ->
                            SegmentedButton(
                                selected = actionIndex == index && lastUserPrompt == null,
                                onClick = { onActionSelected(index) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index,
                                    CopilotAction.entries.size,
                                ),
                                enabled = !isStreaming,
                                label = { Text(item.label) },
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 260.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                        shape = CopilotCardShape,
                        color = scheme.primaryContainer,
                        contentColor = scheme.onPrimaryContainer,
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(14.dp),
                        ) {
                            when {
                                phase == StreamPhase.Thinking || phase == StreamPhase.Searching -> {
                                    ThinkingDots()
                                }
                                response.isNotEmpty() -> {
                                    MarkdownText(text = response)
                                }
                                    else -> {
                                    Text(
                                        text = "Summarize, rewrite, or ask a question to stream a reply here.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = scheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CopilotPhaseBadge(phase: StreamPhase) {
    val scheme = MaterialTheme.colorScheme
    val (label, color) = when (phase) {
        StreamPhase.Thinking -> "Think" to scheme.tertiary
        StreamPhase.Searching -> "Search" to scheme.secondary
        StreamPhase.Generating -> "Stream" to scheme.primary
        StreamPhase.Done -> "Ready" to scheme.outline
    }
    Text(
        text = label,
        modifier = Modifier
            .clip(CopilotChipShape)
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = scheme.onSurface,
        maxLines = 1,
    )
}

@Composable
private fun CopilotIndexingBanner() {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        color = scheme.secondaryContainer,
        contentColor = scheme.onSecondaryContainer,
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ContainedLoadingIndicator()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Indexing workspace",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSecondaryContainer,
                    maxLines = 1,
                )
                Text(
                    "Embeddings refresh in the background so the dock can cite this doc.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSecondaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                LinearWavyProgressIndicator(
                    progress = { 0.72f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CopilotContextRail() {
    val scheme = MaterialTheme.colorScheme
    var tone by remember { mutableIntStateOf(1) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        color = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurface,
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Reply tone",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                listOf("Concise", "Balanced", "Detailed").forEachIndexed { index, label ->
                    ToggleButton(
                        checked = tone == index,
                        onCheckedChange = { tone = index },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Text(
                when (tone) {
                    0 -> "Short answers · bullets preferred"
                    2 -> "Longer explanations · keep citations"
                    else -> "Default assistant voice for this workspace"
                },
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CopilotSuggestedEdits() {
    val scheme = MaterialTheme.colorScheme
    val edits = listOf(
        "Tighten the streaming debounce section",
        "Add TalkBack live-region note",
        "Move code sample under Key points",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DecorativeIcon(Icons.Outlined.AutoAwesome, tint = scheme.primary)
                Text(
                    "Suggested edits",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${edits.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary,
                )
            }
            edits.forEach { edit ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        edit,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun CopilotSourcesCard() {
    val scheme = MaterialTheme.colorScheme
    val sources = listOf(
        "M3 Bottom sheets" to "Material Design",
        "TalkBack live regions" to "Android A11y",
        "snapshotFlow debounce" to "Compose runtime",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        color = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurface,
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Sources in context",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            sources.forEach { (title, meta) ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(scheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Description,
                            contentDescription = null,
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            meta,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CopilotOutlineCard() {
    val scheme = MaterialTheme.colorScheme
    val sections = listOf(
        "01" to "Problem · latency vs layout thrash",
        "02" to "Token buffering strategy",
        "03" to "Live region announcements",
        "04" to "Dock peek / expand behavior",
        "05" to "Acceptance criteria",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Document outline",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            sections.forEach { (num, title) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        num,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                    )
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CopilotDocumentPreview(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CopilotCardShape,
        color = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurface,
        shadowElevation = 6.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            scheme.outlineVariant,
        ),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.surfaceContainerHighest)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DecorativeIcon(
                    imageVector = Icons.Outlined.Description,
                    tint = scheme.primary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Product requirements · v3",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "1,248 words · last edited 2h ago",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "In context",
                    modifier = Modifier
                        .clip(CopilotChipShape)
                        .background(scheme.tertiaryContainer)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onTertiaryContainer,
                    maxLines = 1,
                )
            }

            HorizontalDivider(color = scheme.outlineVariant)

            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Streaming copilot UI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Text(
                    "Keep the document canvas primary. The assistant peeks as a dock so reading is never covered by a modal sheet. Expand on focus, swipe, or tap — then stream with explicit phases.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    "Layout measurements must debounce while tokens arrive. Prefer word-boundary flushes and a polite live region so TalkBack hears progress without flooding the queue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "fun measureStreaming(text: String) {\n" +
                            "    snapshotFlow { text }.debounce(80)\n" +
                            "        .collect { measureAndShow(it) }\n" +
                            "}",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = scheme.onSurface,
                    )
                }
                Text(
                    "Acceptance: peek dock always shows the ask field; expand reveals actions + reply; system back collapses without leaving the demo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StopButton(onStop: () -> Unit) {
    FilledTonalButton(
        onClick = onStop,
        modifier = Modifier.height(36.dp),
    ) {
        Icon(
            Icons.Outlined.Stop,
            contentDescription = "Stop generating",
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text("Stop", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ThinkingDots() {
    val infinite = rememberInfiniteTransition(label = "dots")
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 8.dp),
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
            Text("•", color = MaterialTheme.colorScheme.primary.copy(alpha = alpha))
        }
    }
}
