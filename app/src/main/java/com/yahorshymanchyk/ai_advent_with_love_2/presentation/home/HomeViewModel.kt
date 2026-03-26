package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ChatRepository
import com.yahorshymanchyk.ai_advent_with_love_2.presentation.home.toUiModel
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val currentChatId = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            val chat = chatRepository.getLatestChat() ?: chatRepository.createChat()
            _uiState.update {
                it.copy(
                    chatId = chat.id,
                    chatName = chat.name,
                    maxTokensInput = chat.maxTokens.toString(),
                    systemPromptInput = chat.systemPrompt ?: "",
                    stopSequenceInput = chat.stopSequence ?: ""
                )
            }
            currentChatId.value = chat.id
        }

        viewModelScope.launch {
            currentChatId
                .filterNotNull()
                .flatMapLatest { chatId -> chatRepository.getMessagesForChat(chatId) }
                .collect { messages -> _uiState.update { it.copy(messages = messages.map { it.toUiModel() }) } }
        }
    }

    fun updateChatName(name: String) {
        _uiState.update { it.copy(chatName = name) }
        val chatId = _uiState.value.chatId
        if (chatId == -1L) return
        viewModelScope.launch { chatRepository.updateChatName(chatId, name) }
    }

    fun updateMaxTokens(value: String) = _uiState.update { it.copy(maxTokensInput = value) }
    fun updateStopSequence(value: String) = _uiState.update { it.copy(stopSequenceInput = value) }
    fun updateSystemPrompt(value: String) = _uiState.update { it.copy(systemPromptInput = value) }

    fun sendMessage(userInput: String) {
        val state = _uiState.value
        val chatId = state.chatId
        if (chatId == -1L) return

        val maxTokens = state.maxTokensInput.toIntOrNull() ?: 512
        val stopSequence = state.stopSequenceInput.takeIf { it.isNotBlank() }
        val systemPrompt = state.systemPromptInput.takeIf { it.isNotBlank() }
        val historyForApi = state.messages.map {
            ChatMessage(
                role = if (it.isFromUser) ChatMessage.Role.USER else ChatMessage.Role.ASSISTANT,
                content = it.content
            )
        } + ChatMessage(ChatMessage.Role.USER, userInput)

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            chatRepository.saveMessage(chatId, ChatMessage.Role.USER, userInput)
            chatRepository.updateChatSettings(chatId, maxTokens, systemPrompt, stopSequence)

            sendMessageUseCase(historyForApi, maxTokens, stopSequence, systemPrompt)
                .onSuccess { assistantMsg ->
                    chatRepository.saveMessage(chatId, ChatMessage.Role.ASSISTANT, assistantMsg.content)
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Unknown error") }
                }
        }
    }

    fun startNewChat() {
        viewModelScope.launch {
            val chat = chatRepository.createChat()
            _uiState.value = HomeUiState(
                chatId = chat.id,
                chatName = chat.name
            )
            currentChatId.value = chat.id
        }
    }
}
