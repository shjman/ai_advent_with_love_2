package com.yahorshymanchyk.ai_advent_with_love_2.presentation

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.Message

sealed class ClaudeUiState {
    data object Idle : ClaudeUiState()
    data object Loading : ClaudeUiState()
    data class Success(val message: Message) : ClaudeUiState()
    data class Error(val message: String) : ClaudeUiState()
}
