package com.yahorshymanchyk.ai_advent_with_love_2.presentation

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage

data class ClaudeUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
