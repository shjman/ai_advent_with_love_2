package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

data class HomeUiState(
    val chatId: Long = -1L,
    val chatName: String = "",
    val messages: List<MessageUiModel> = emptyList(),
    val maxTokensInput: String = "512",
    val stopSequenceInput: String = "",
    val systemPromptInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val expectedInputTokens: Int? = null
)
