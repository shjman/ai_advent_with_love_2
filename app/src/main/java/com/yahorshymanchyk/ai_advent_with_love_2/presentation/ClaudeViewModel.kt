package com.yahorshymanchyk.ai_advent_with_love_2.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClaudeViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClaudeUiState())
    val uiState: StateFlow<ClaudeUiState> = _uiState.asStateFlow()

    fun sendMessage(userInput: String, maxTokens: Int, stopSequence: String?) {
        val historyWithUserMsg = _uiState.value.messages +
                ChatMessage(ChatMessage.Role.USER, userInput)

        _uiState.value = ClaudeUiState(messages = historyWithUserMsg, isLoading = true)

        viewModelScope.launch {
            sendMessageUseCase(historyWithUserMsg, maxTokens, stopSequence)
                .onSuccess { assistantMsg ->
                    _uiState.value = ClaudeUiState(messages = historyWithUserMsg + assistantMsg)
                }
                .onFailure { error ->
                    _uiState.value = ClaudeUiState(
                        messages = historyWithUserMsg,
                        error = error.message ?: "Unknown error"
                    )
                }
        }
    }
}
