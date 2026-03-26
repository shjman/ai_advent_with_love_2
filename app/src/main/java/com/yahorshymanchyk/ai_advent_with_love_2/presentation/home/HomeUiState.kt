package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

sealed class HomeUiState {

    data object Loading : HomeUiState()

    data class Success(
        val chatId: Long,
        val chatName: String,
        val messages: List<MessageUiModel> = emptyList(),
        val maxTokensInput: String = "512",
        val stopSequenceInput: String = "",
        val systemPromptInput: String = "",
        val isSending: Boolean = false,
        val sendError: String? = null,
        val expectedInputTokens: Int? = null
    ) : HomeUiState()

    data class Error(val message: String) : HomeUiState()
}
