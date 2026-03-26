package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ChatRepository
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ClaudeRepository
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val chatRepository: ChatRepository,
    private val claudeRepository: ClaudeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val currentChatId = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            runCatching { chatRepository.getLatestChat() ?: chatRepository.createChat() }
                .onSuccess { chat ->
                    _uiState.value = HomeUiState.Success(
                        chatId = chat.id,
                        chatName = chat.name,
                        maxTokensInput = chat.maxTokens.toString(),
                        systemPromptInput = chat.systemPrompt ?: "",
                        stopSequenceInput = chat.stopSequence ?: ""
                    )
                    currentChatId.value = chat.id
                }
                .onFailure { _uiState.value = HomeUiState.Error(it.message ?: "Failed to load chat") }
        }

        viewModelScope.launch {
            currentChatId
                .filterNotNull()
                .flatMapLatest { chatId -> chatRepository.getMessagesForChat(chatId) }
                .collect { messages ->
                    updateSuccess { it.copy(messages = messages.map { msg -> msg.toUiModel() }) }
                    refreshTokenCount(messages)
                }
        }
    }

    private inline fun updateSuccess(update: (HomeUiState.Success) -> HomeUiState.Success) {
        val current = _uiState.value as? HomeUiState.Success ?: return
        _uiState.value = update(current)
    }

    private fun refreshTokenCount(history: List<ChatMessage>) {
        if (history.isEmpty()) {
            updateSuccess { it.copy(expectedInputTokens = null) }
            return
        }
        val systemPrompt = (_uiState.value as? HomeUiState.Success)
            ?.systemPromptInput?.takeIf { it.isNotBlank() }
        viewModelScope.launch {
            claudeRepository.countTokens(history, systemPrompt)
                .onSuccess { count -> updateSuccess { it.copy(expectedInputTokens = count) } }
        }
    }

    fun updateChatName(name: String) {
        updateSuccess { it.copy(chatName = name) }
        val chatId = (_uiState.value as? HomeUiState.Success)?.chatId ?: return
        viewModelScope.launch { chatRepository.updateChatName(chatId, name) }
    }

    fun updateMaxTokens(value: String) = updateSuccess { it.copy(maxTokensInput = value) }
    fun updateStopSequence(value: String) = updateSuccess { it.copy(stopSequenceInput = value) }
    fun updateSystemPrompt(value: String) = updateSuccess { it.copy(systemPromptInput = value) }

    fun sendMessage(userInput: String) {
        val state = _uiState.value as? HomeUiState.Success ?: return
        val chatId = state.chatId

        val maxTokens = state.maxTokensInput.toIntOrNull() ?: 512
        val stopSequence = state.stopSequenceInput.takeIf { it.isNotBlank() }
        val systemPrompt = state.systemPromptInput.takeIf { it.isNotBlank() }
        val historyForApi = state.messages.map {
            ChatMessage(
                role = if (it.isFromUser) ChatMessage.Role.USER else ChatMessage.Role.ASSISTANT,
                content = it.content
            )
        } + ChatMessage(ChatMessage.Role.USER, userInput)

        updateSuccess { it.copy(isSending = true, sendError = null) }

        viewModelScope.launch {
            runCatching { chatRepository.saveMessage(chatId, ChatMessage.Role.USER, userInput) }
                .onFailure { updateSuccess { it.copy(isSending = false, sendError = it.sendError ?: "Failed to save message") }; return@launch }

            chatRepository.updateChatSettings(chatId, maxTokens, systemPrompt, stopSequence)

            sendMessageUseCase(historyForApi, maxTokens, stopSequence, systemPrompt)
                .onSuccess { assistantMsg ->
                    chatRepository.saveMessage(chatId, ChatMessage.Role.ASSISTANT, assistantMsg.content)
                    updateSuccess { it.copy(isSending = false, sendError = null) }
                }
                .onFailure { error ->
                    updateSuccess { it.copy(isSending = false, sendError = error.message ?: "Failed to get response") }
                }
        }
    }

    fun loadChat(chatId: Long) {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            runCatching { chatRepository.getChatById(chatId) }
                .onSuccess { chat ->
                    if (chat == null) {
                        _uiState.value = HomeUiState.Error("Chat not found")
                        return@launch
                    }
                    _uiState.value = HomeUiState.Success(
                        chatId = chat.id,
                        chatName = chat.name,
                        maxTokensInput = chat.maxTokens.toString(),
                        systemPromptInput = chat.systemPrompt ?: "",
                        stopSequenceInput = chat.stopSequence ?: ""
                    )
                    currentChatId.value = chat.id
                }
                .onFailure { _uiState.value = HomeUiState.Error(it.message ?: "Failed to load chat") }
        }
    }

    fun startNewChat() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            runCatching { chatRepository.createChat() }
                .onSuccess { chat ->
                    _uiState.value = HomeUiState.Success(chatId = chat.id, chatName = chat.name)
                    currentChatId.value = chat.id
                }
                .onFailure { _uiState.value = HomeUiState.Error(it.message ?: "Failed to create chat") }
        }
    }
}
