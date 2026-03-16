package com.yahorshymanchyk.ai_advent_with_love_2.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _uiState = MutableStateFlow<ClaudeUiState>(ClaudeUiState.Idle)
    val uiState: StateFlow<ClaudeUiState> = _uiState.asStateFlow()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _uiState.value = ClaudeUiState.Loading
            sendMessageUseCase(message)
                .onSuccess { _uiState.value = ClaudeUiState.Success(it) }
                .onFailure { _uiState.value = ClaudeUiState.Error(it.message ?: "Unknown error") }
        }
    }
}
