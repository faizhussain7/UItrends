package com.mfhapps.trendingui.screens.chat

import android.graphics.Typeface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mfhapps.trendingui.core.text.MeasuredTextLayout
import com.mfhapps.trendingui.core.text.PreparedText
import com.mfhapps.trendingui.core.text.TextMeasurementEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil

class VirtualChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VirtualChatUiState())
    val uiState: StateFlow<VirtualChatUiState> = _uiState.asStateFlow()

    private var measureConfig: ChatMeasureConfig? = null
    private var historyLoaded = false
    private var nextMessageId = 0
    private var historyJob: Job? = null
    private var streamJob: Job? = null
    private var streamLayoutJob: Job? = null
    private var streamPrepared: PreparedText? = null

    fun onMeasureConfig(config: ChatMeasureConfig) {
        if (measureConfig == config && historyLoaded) return
        measureConfig = config
        if (!historyLoaded) {
            loadHistory()
        }
    }

    fun onDraftChange(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun onSuggestionClick(suggestion: String) {
        sendMessage(suggestion, cancelStream = true)
    }

    fun sendDraft() {
        val draft = _uiState.value.draft
        if (draft.isBlank()) return
        _uiState.update { it.copy(draft = "") }
        sendMessage(draft, cancelStream = true)
    }

    fun stopStreaming() {
        cancelStreamJobs()
        val partial = _uiState.value.streamingText
        val layout = _uiState.value.streamLayout
        val width = _uiState.value.streamWidthPx
        streamPrepared = null
        if (partial.isNotBlank() && layout != null) {
            commitAssistantMessage(partial, layout, width)
        } else {
            _uiState.update {
                it.copy(
                    streamPhase = ChatStreamPhase.Idle,
                    streamingText = "",
                    streamLayout = null,
                )
            }
        }
    }

    fun retryLoad() {
        historyLoaded = false
        loadHistory()
    }

    private fun sendMessage(raw: String, cancelStream: Boolean) {
        val config = measureConfig ?: return
        val text = raw.trim()
        if (text.isEmpty()) return
        if (_uiState.value.loadState !is ChatLoadState.Ready) return

        if (cancelStream && _uiState.value.isStreaming) {
            cancelStreamJobs()
            streamPrepared = null
            _uiState.update {
                it.copy(
                    streamPhase = ChatStreamPhase.Idle,
                    streamingText = "",
                    streamLayout = null,
                )
            }
        } else if (_uiState.value.isStreaming) {
            return
        }

        viewModelScope.launch {
            appendUserMessage(text, config)
            startAssistantStream(replyFor(text), config)
        }
    }

    private fun cancelStreamJobs() {
        streamJob?.cancel()
        streamLayoutJob?.cancel()
        streamJob = null
        streamLayoutJob = null
    }

    private fun loadHistory() {
        val config = measureConfig ?: return
        historyJob?.cancel()
        cancelStreamJobs()
        streamPrepared = null
        val keepMessages = _uiState.value.messages
        _uiState.update {
            it.copy(
                loadState = if (keepMessages.isEmpty()) {
                    ChatLoadState.Loading
                } else {
                    it.loadState
                },
                draft = it.draft,
                streamPhase = ChatStreamPhase.Idle,
                streamingText = "",
                streamLayout = null,
            )
        }
        historyJob = viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    buildHistory(config)
                }
            }.onSuccess { built ->
                historyLoaded = true
                nextMessageId = built.size
                _uiState.update {
                    it.copy(
                        loadState = ChatLoadState.Ready,
                        messages = built,
                        displayItems = toDisplayItems(built),
                        streamPhase = ChatStreamPhase.Idle,
                        streamingText = "",
                        streamLayout = null,
                    )
                }
            }.onFailure { error ->
                historyLoaded = false
                _uiState.update {
                    it.copy(
                        loadState = ChatLoadState.Failed(
                            error.message ?: "Could not build chat history",
                        ),
                    )
                }
            }
        }
    }

    private suspend fun appendUserMessage(text: String, config: ChatMeasureConfig) {
        val message = withContext(Dispatchers.Default) {
            measureMessage(
                id = nextMessageId++,
                text = text,
                isUser = true,
                sender = "You",
                timestamp = nowTimestamp(),
                read = true,
                dayBucket = 1,
                config = config,
            )
        }
        _uiState.update { state ->
            val messages = state.messages + message
            state.copy(
                messages = messages,
                displayItems = toDisplayItems(messages),
            )
        }
    }

    private fun startAssistantStream(
        text: String,
        config: ChatMeasureConfig,
        thinkingDelayMs: Long = 500,
    ) {
        cancelStreamJobs()
        _uiState.update {
            it.copy(
                streamPhase = ChatStreamPhase.Thinking,
                streamingText = "",
                streamLayout = null,
                streamWidthPx = config.maxWidthPx,
            )
        }
        streamJob = viewModelScope.launch {
            delay(thinkingDelayMs)
            val prepared = withContext(Dispatchers.Default) {
                TextMeasurementEngine.prepareSync(text, config.fontSizeSp, Typeface.DEFAULT)
            }
            streamPrepared = prepared
            val layoutAtMax = withContext(Dispatchers.Default) {
                TextMeasurementEngine.layout(prepared, config.maxWidthPx, config.lineHeightPx)
            }
            val width = ceil(
                (layoutAtMax.lineWidthsPx.maxOrNull() ?: 0f) + config.bubblePaddingPx,
            ).toInt().coerceIn(config.minWidthPx, config.maxWidthPx)
            _uiState.update {
                it.copy(
                    streamPhase = ChatStreamPhase.Streaming,
                    streamWidthPx = width,
                )
            }
            observeStreamLayout(width, config.lineHeightPx)
            flow {
                text.forEach { ch ->
                    delay(28)
                    emit(ch)
                }
            }.collect { ch ->
                _uiState.update { it.copy(streamingText = it.streamingText + ch) }
            }
            val finalText = _uiState.value.streamingText
            val finalLayout = _uiState.value.streamLayout
                ?: withContext(Dispatchers.Default) {
                    TextMeasurementEngine.layoutForPrefix(
                        prepared,
                        finalText,
                        width,
                        config.lineHeightPx,
                    )
                }
            commitAssistantMessage(finalText, finalLayout, width)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeStreamLayout(widthPx: Int, lineHeightPx: Float) {
        streamLayoutJob?.cancel()
        streamLayoutJob = viewModelScope.launch {
            _uiState
                .map { it.streamingText }
                .distinctUntilChanged()
                .debounce(60)
                .collect { text ->
                    val prepared = streamPrepared
                    if (prepared == null || text.isEmpty()) {
                        _uiState.update { it.copy(streamLayout = null) }
                        return@collect
                    }
                    val layout = withContext(Dispatchers.Default) {
                        TextMeasurementEngine.layoutForPrefix(
                            prepared,
                            text,
                            widthPx,
                            lineHeightPx,
                        )
                    }
                    _uiState.update { it.copy(streamLayout = layout) }
                }
        }
    }

    private fun commitAssistantMessage(
        text: String,
        layout: MeasuredTextLayout,
        widthPx: Int,
    ) {
        streamLayoutJob?.cancel()
        streamPrepared = null
        val message = ChatMessage(
            id = nextMessageId++,
            isUser = false,
            layout = layout,
            bubbleWidthPx = widthPx,
            sender = "AI",
            timestamp = nowTimestamp(),
            read = true,
            dayBucket = 1,
        )
        _uiState.update { state ->
            val messages = state.messages + message
            state.copy(
                messages = messages,
                displayItems = toDisplayItems(messages),
                streamPhase = ChatStreamPhase.Idle,
                streamingText = "",
                streamLayout = null,
            )
        }
    }

    private fun buildHistory(config: ChatMeasureConfig): List<ChatMessage> =
        (0 until VirtualChatDefaults.HISTORY_COUNT).map { i ->
            val text = if (i % 3 == 0) {
                "Short reply $i"
            } else {
                "Message $i with enough text to wrap inside a shrink-wrapped bubble."
            }
            val hour = 9 + (i / 30) % 12
            val min = (i * 2) % 60
            val ts = "%d:%02d %s".format(hour, min, if (hour < 12) "AM" else "PM")
            measureMessage(
                id = i,
                text = text,
                isUser = i % 2 == 0,
                sender = if (i % 2 == 0) "You" else "AI",
                timestamp = ts,
                read = i < VirtualChatDefaults.HISTORY_COUNT - 20,
                dayBucket = i / 250,
                config = config,
            )
        }

    private fun measureMessage(
        id: Int,
        text: String,
        isUser: Boolean,
        sender: String,
        timestamp: String,
        read: Boolean,
        dayBucket: Int,
        config: ChatMeasureConfig,
    ): ChatMessage {
        val prep = TextMeasurementEngine.prepareSync(text, config.fontSizeSp, Typeface.DEFAULT)
        val layoutAtMax = TextMeasurementEngine.layout(
            prep,
            config.maxWidthPx,
            config.lineHeightPx,
        )
        val contentWidth = ceil(
            (layoutAtMax.lineWidthsPx.maxOrNull() ?: 0f) + config.bubblePaddingPx,
        ).toInt()
        val width = contentWidth.coerceIn(config.minWidthPx, config.maxWidthPx)
        val layout = if (width >= config.maxWidthPx) {
            layoutAtMax
        } else {
            TextMeasurementEngine.layout(prep, width, config.lineHeightPx)
        }
        return ChatMessage(
            id = id,
            isUser = isUser,
            layout = layout,
            bubbleWidthPx = width,
            sender = sender,
            timestamp = timestamp,
            read = read,
            dayBucket = dayBucket,
        )
    }

    private fun toDisplayItems(messages: List<ChatMessage>): List<ChatListItem> {
        val out = mutableListOf<ChatListItem>()
        var lastDay = -1
        messages.forEach { msg ->
            if (msg.dayBucket != lastDay) {
                lastDay = msg.dayBucket
                val label = VirtualChatDefaults.dayLabels.getOrElse(msg.dayBucket) { "Day $lastDay" }
                out.add(ChatListItem.DateDivider(label))
            }
            out.add(ChatListItem.Bubble(msg))
        }
        return out
    }

    private fun replyFor(prompt: String): String =
        "You asked: \"$prompt\". " +
            "Shrink-wrapped bubbles measure once with prepare(), then layout() " +
            "only the visible prefix while tokens stream — keeping 500+ rows smooth."

    private fun nowTimestamp(): String {
        val millis = System.currentTimeMillis()
        val minutes = ((millis / 60_000) % (12 * 60)).toInt()
        val hour = (minutes / 60).coerceAtLeast(1)
        val min = minutes % 60
        return "%d:%02d %s".format(hour, min, if (hour < 12) "AM" else "PM")
    }

    override fun onCleared() {
        historyJob?.cancel()
        cancelStreamJobs()
        super.onCleared()
    }
}
