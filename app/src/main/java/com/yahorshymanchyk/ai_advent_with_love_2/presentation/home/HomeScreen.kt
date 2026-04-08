package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(paddingValues: PaddingValues, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeView(
        uiState = uiState,
        paddingValues = paddingValues,
        onSendMessage = viewModel::sendMessage,
        onInputTextChange = viewModel::updateInputText,
        onShowSettings = { viewModel.setShowSettings(true) },
        onDismissSettings = { viewModel.setShowSettings(false) },
        onShowNewChatDialog = { viewModel.setShowNewChatDialog(true) },
        onDismissNewChatDialog = { viewModel.setShowNewChatDialog(false) },
        onConfirmNewChat = {
            viewModel.startNewChat()
            viewModel.setShowNewChatDialog(false)
        },
        onChatNameChange = viewModel::updateChatName,
        onMaxTokensChange = viewModel::updateMaxTokens,
        onStopSequenceChange = viewModel::updateStopSequence,
        onSystemPromptChange = viewModel::updateSystemPrompt
    )
}
