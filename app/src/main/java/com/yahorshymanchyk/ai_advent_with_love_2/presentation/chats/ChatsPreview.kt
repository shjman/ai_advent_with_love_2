package com.yahorshymanchyk.ai_advent_with_love_2.presentation.chats

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
private fun ChatsLoadingPreview() {
    ChatsView(uiState = ChatsUiState.Loading, paddingValues = PaddingValues(), onChatSelected = {})
}

@Preview(showBackground = true)
@Composable
private fun ChatsSuccessPreview() {
    ChatsView(
        uiState = ChatsUiState.Success(
            chats = listOf(
                ChatUiModel(id = 1, name = "Chat with Claude"),
                ChatUiModel(id = 2, name = "Recipe ideas"),
                ChatUiModel(id = 3, name = "Kotlin questions"),
            )
        ),
        paddingValues = PaddingValues(),
        onChatSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatsEmptyPreview() {
    ChatsView(uiState = ChatsUiState.Success(chats = emptyList()), paddingValues = PaddingValues(), onChatSelected = {})
}

@Preview(showBackground = true)
@Composable
private fun ChatsErrorPreview() {
    ChatsView(uiState = ChatsUiState.Error(message = "Failed to load chats"), paddingValues = PaddingValues(), onChatSelected = {})
}
