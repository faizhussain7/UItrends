package com.mfhapps.trendingui.screens.copilot

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import com.mfhapps.trendingui.ui.components.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import com.mfhapps.trendingui.ui.components.appHazeSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

private val CopilotSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCopilotScreen() {
    val scheme = MaterialTheme.colorScheme
    val configuration = LocalConfiguration.current
    val sheetPeekHeight = (configuration.screenHeightDp * 0.52f).dp.coerceIn(280.dp, 420.dp)

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        enabledValues = setOf(SheetValue.PartiallyExpanded, SheetValue.Expanded),
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val scope = rememberCoroutineScope()
    var response by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf(StreamPhase.Thinking) }
    var actionIndex by remember { mutableIntStateOf(0) }
    var streamJob by remember { mutableStateOf<Job?>(null) }
    var prompt by remember { mutableStateOf("") }
    var lastUserPrompt by remember { mutableStateOf<String?>(null) }

    val isStreaming = phase == StreamPhase.Thinking ||
        phase == StreamPhase.Searching ||
        phase == StreamPhase.Generating

    fun stream(text: String) {
        streamJob?.cancel()
        response = ""
        phase = StreamPhase.Thinking
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

    LaunchedEffect(Unit) {
        startAction(CopilotAction.entries[actionIndex])
        if (sheetState.currentValue != SheetValue.PartiallyExpanded) {
            sheetState.partialExpand()
        }
    }

    BackHandler(enabled = sheetState.currentValue == SheetValue.Expanded) {
        scope.launch { sheetState.partialExpand() }
    }

    BottomSheetScaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        containerColor = Color.Transparent,
        sheetContainerColor = scheme.surface.copy(alpha = 0.97f),
        sheetShape = CopilotSheetShape,
        sheetSwipeEnabled = true,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            CopilotSheetContent(
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
                minHeight = sheetPeekHeight,
            )
        },
    ) { innerPadding ->
        CompositionLocalProvider(LocalContentColor provides scheme.onSurface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .appHazeSource()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .padding(top = 56.dp)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CopilotWorkspaceHeader()
                CopilotDocumentPreview()
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
            .background(scheme.surfaceContainerHigh.copy(alpha = 0.88f))
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
private fun CopilotSheetContent(
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
    minHeight: androidx.compose.ui.unit.Dp,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
            CopilotSheetHeader(phase = phase)

            if (phase == StreamPhase.Thinking || phase == StreamPhase.Searching) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ThinkingDots()
                    StopButton(onStop = onStop)
                }
            } else if (phase == StreamPhase.Generating) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    StopButton(onStop = onStop)
                }
            }

            if (response.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    shape = CopilotCardShape,
                    color = scheme.primaryContainer.copy(alpha = 0.22f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        scheme.primary.copy(alpha = 0.18f),
                    ),
                    tonalElevation = 0.dp,
                ) {
                    MarkdownText(
                        text = response,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CopilotAction.entries.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = actionIndex == index && lastUserPrompt == null,
                        onClick = { onActionSelected(index) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index,
                            CopilotAction.entries.size,
                        ),
                        label = { Text(item.label) },
                    )
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
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask about this document…") },
                    singleLine = true,
                    enabled = !isStreaming,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = scheme.primary.copy(alpha = 0.55f),
                        unfocusedBorderColor = scheme.outlineVariant.copy(alpha = 0.65f),
                        focusedContainerColor = scheme.surfaceContainerHighest.copy(alpha = 0.45f),
                        unfocusedContainerColor = scheme.surfaceContainerHighest.copy(alpha = 0.28f),
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
    }
}

@Composable
private fun CopilotWorkspaceHeader() {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "AI Copilot",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
        )
        Text(
            text = "Document stays visible above the sheet peek · drag up to expand",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CopilotContextChip(
    label: String,
    accent: Color,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(CopilotChipShape)
            .background(scheme.surface.copy(alpha = 0.82f))
            .border(1.dp, accent.copy(alpha = 0.35f), CopilotChipShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )
    }
}

@Composable
private fun CopilotSheetHeader(phase: StreamPhase) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CopilotCardShape)
            .background(scheme.surfaceContainerHigh.copy(alpha = 0.55f))
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.35f), CopilotCardShape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = scheme.primaryContainer.copy(alpha = 0.65f),
            tonalElevation = 0.dp,
        ) {
            DecorativeIcon(
                Icons.Outlined.AutoAwesome,
                tint = scheme.primary,
                modifier = Modifier.padding(10.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                "Copilot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            AnimatedContent(
                targetState = phase,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "phaseStatus",
            ) { p ->
                Text(
                    when (p) {
                        StreamPhase.Thinking -> "Thinking…"
                        StreamPhase.Searching -> "Searching context…"
                        StreamPhase.Generating -> "Generating…"
                        StreamPhase.Done -> "Ready"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
        CopilotPhaseBadge(phase = phase)
    }
}

@Composable
private fun CopilotPhaseBadge(phase: StreamPhase) {
    val scheme = MaterialTheme.colorScheme
    val (label, color) = when (phase) {
        StreamPhase.Thinking -> "Think" to scheme.tertiary
        StreamPhase.Searching -> "Search" to scheme.secondary
        StreamPhase.Generating -> "Stream" to scheme.primary
        StreamPhase.Done -> "Done" to scheme.outline
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
    )
}

@Composable
private fun CopilotDocumentPreview(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CopilotCardShape,
        color = scheme.surface.copy(alpha = 0.90f),
        shadowElevation = 10.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            scheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.surfaceContainerHighest.copy(alpha = 0.55f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DecorativeIcon(
                    imageVector = Icons.Outlined.Description,
                    tint = scheme.primary,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        "Product requirements · v3",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                    )
                    Text(
                        "1,248 words · last edited 2h ago",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                CopilotContextChip(label = "In context", accent = scheme.tertiary)
            }

            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.35f))

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
                    "The copilot reads this canvas while you chat. Replies render **bold**, " +
                        "*italic*, `inline code`, and fenced blocks as tokens arrive.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.surfaceContainerHighest.copy(alpha = 0.85f),
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "fun measureStreaming(text: String) {\n" +
                            "    snapshotFlow { text }.debounce(80)\n" +
                            "}",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = scheme.onSurface,
                    )
                }
                Text(
                    "Drag the sheet up for the full transcript · peek keeps this document visible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.primary,
                    fontWeight = FontWeight.Medium,
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
        Spacer(Modifier.size(6.dp))
        Text("Stop", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ThinkingDots() {
    val infinite = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
