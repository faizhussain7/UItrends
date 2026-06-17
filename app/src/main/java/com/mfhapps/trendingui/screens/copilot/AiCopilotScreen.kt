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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

private val CopilotSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val CopilotCardShape = RoundedCornerShape(20.dp)

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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        sheetContainerColor = scheme.surface.copy(alpha = 0.97f),
        sheetShape = CopilotSheetShape,
        sheetSwipeEnabled = true,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = sheetPeekHeight)
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
                        StopButton { streamJob?.cancel(); phase = StreamPhase.Done }
                    }
                } else if (phase == StreamPhase.Generating) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        StopButton { streamJob?.cancel(); phase = StreamPhase.Done }
                    }
                }

                if (response.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { liveRegion = LiveRegionMode.Polite },
                        shape = CopilotCardShape,
                        color = scheme.surfaceContainerHighest.copy(alpha = 0.65f),
                        tonalElevation = 0.dp,
                    ) {
                        MarkdownText(
                            text = response,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    CopilotAction.entries.forEachIndexed { index, item ->
                        SegmentedButton(
                            selected = actionIndex == index && lastUserPrompt == null,
                            onClick = {
                                actionIndex = index
                                startAction(CopilotAction.entries[index])
                            },
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
                        onValueChange = { prompt = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask anything…") },
                        singleLine = true,
                        enabled = !isStreaming,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendPrompt(prompt) }),
                    )
                    FilledIconButton(
                        onClick = { sendPrompt(prompt) },
                        enabled = !isStreaming && prompt.isNotBlank(),
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
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 56.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "AI Copilot",
                style = MaterialTheme.typography.headlineLarge,
                color = scheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Document stays visible above the sheet peek · drag up to expand copilot · sheet cannot be hidden",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )

            CopilotDocumentPreview(
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun CopilotSheetHeader(phase: StreamPhase) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CopilotCardShape,
        color = scheme.surfaceContainerHigh.copy(alpha = 0.85f),
        tonalElevation = 0.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DecorativeIcon(
                Icons.Outlined.AutoAwesome,
                tint = scheme.primary,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "Copilot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
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
                            StreamPhase.Done -> "Done"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
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
        color = scheme.surface.copy(alpha = 0.88f),
        shadowElevation = 6.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Product requirements · v3",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "The copilot reads this canvas while you chat. Streamed replies support " +
                    "**bold**, *italic*, `inline code`, and fenced blocks in real time.",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = scheme.surfaceContainerHighest,
                tonalElevation = 0.dp,
            ) {
                Text(
                    "fun measureStreaming(text: String) {\n" +
                        "    snapshotFlow { text }.debounce(80)\n" +
                        "}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = scheme.onSurface,
                )
            }
            Text(
                "Swipe the sheet down to partially expand and keep editing context visible.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.primary,
            )
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
