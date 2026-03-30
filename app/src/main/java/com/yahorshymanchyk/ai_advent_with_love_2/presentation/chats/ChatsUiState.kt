package com.yahorshymanchyk.ai_advent_with_love_2.presentation.chats

sealed class ChatsUiState {

    data object Loading : ChatsUiState()

    data class Success(val chats: List<ChatUiModel>) : ChatsUiState()

    data class Error(val message: String) : ChatsUiState()
}
