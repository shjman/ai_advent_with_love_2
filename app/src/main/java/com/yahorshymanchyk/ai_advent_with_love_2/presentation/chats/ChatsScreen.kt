package com.yahorshymanchyk.ai_advent_with_love_2.presentation.chats

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChatsScreen(
    paddingValues: PaddingValues,
    onChatSelected: (Long) -> Unit,
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatsView(
        uiState = uiState,
        paddingValues = paddingValues,
        onChatSelected = onChatSelected
    )
}
