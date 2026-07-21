package com.mfhapps.trendingui.screens.chat

import com.mfhapps.trendingui.core.text.MeasuredTextLayout

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

sealed interface ChatLoadState {
    data object Idle : ChatLoadState
    data object Loading : ChatLoadState
    data object Ready : ChatLoadState
    data class Failed(val message: String) : ChatLoadState
}

sealed interface ChatStreamPhase {
    data object Idle : ChatStreamPhase
    data object Thinking : ChatStreamPhase
    data object Streaming : ChatStreamPhase
}

data class ChatMeasureConfig(
    val fontSizeSp: Float,
    val lineHeightPx: Float,
    val maxWidthPx: Int,
    val minWidthPx: Int,
    val bubblePaddingPx: Float,
)

data class VirtualChatUiState(
    val loadState: ChatLoadState = ChatLoadState.Idle,
    val messages: List<ChatMessage> = emptyList(),
    val displayItems: List<ChatListItem> = emptyList(),
    val draft: String = "",
    val streamPhase: ChatStreamPhase = ChatStreamPhase.Idle,
    val streamingText: String = "",
    val streamLayout: MeasuredTextLayout? = null,
    val streamWidthPx: Int = 0,
    val suggestions: List<String> = VirtualChatDefaults.suggestions,
) {
    val isStreaming: Boolean
        get() = streamPhase == ChatStreamPhase.Thinking ||
            streamPhase == ChatStreamPhase.Streaming

    val canSend: Boolean
        get() = draft.isNotBlank() &&
            loadState is ChatLoadState.Ready &&
            !isStreaming
}

object VirtualChatDefaults {
    const val HISTORY_COUNT = 500
    val dayLabels = listOf("Yesterday", "Today")
    val suggestions = listOf(
        "Explain shrink-wrap",
        "Why debounce layout?",
        "Show a short reply",
        "What is LazyColumn?",
    )
}
