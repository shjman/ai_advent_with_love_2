package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.CountTokensUseCase
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.CreateChatUseCase
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.GetChatByIdUseCase
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.GetMessagesUseCase
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.GetOrCreateLatestChatUseCase
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.SaveMessageUseCase
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.UpdateChatNameUseCase
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.UpdateChatSettingsUseCase
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getOrCreateLatestChatUseCase: GetOrCreateLatestChatUseCase,
    private val createChatUseCase: CreateChatUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val countTokensUseCase: CountTokensUseCase,
    private val updateChatNameUseCase: UpdateChatNameUseCase,
    private val updateChatSettingsUseCase: UpdateChatSettingsUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val currentChatId = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            getOrCreateLatestChatUseCase()
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
                .flatMapLatest { chatId -> getMessagesUseCase(chatId) }
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
        val systemPrompt = (_uiState.value as? HomeUiState.Success)?.systemPromptInput?.takeIf { it.isNotBlank() }
        viewModelScope.launch {
            countTokensUseCase(history, systemPrompt)
                .onSuccess { count -> updateSuccess { it.copy(expectedInputTokens = count) } }
        }
    }

    fun updateInputText(text: String) = updateSuccess { it.copy(inputText = text) }
    fun setShowSettings(show: Boolean) = updateSuccess { it.copy(showSettings = show) }
    fun setShowNewChatDialog(show: Boolean) = updateSuccess { it.copy(showNewChatDialog = show) }

    fun updateChatName(name: String) {
        updateSuccess { it.copy(chatName = name) }
        val chatId = (_uiState.value as? HomeUiState.Success)?.chatId ?: return
        viewModelScope.launch { updateChatNameUseCase(chatId, name) }
    }

    fun updateMaxTokens(value: String) = updateSuccess { it.copy(maxTokensInput = value) }
    fun updateStopSequence(value: String) = updateSuccess { it.copy(stopSequenceInput = value) }
    fun updateSystemPrompt(value: String) = updateSuccess { it.copy(systemPromptInput = value) }

    fun sendMessage() {
        val state = _uiState.value as? HomeUiState.Success ?: return
        val userInput = state.inputText.trim()
        if (userInput.isBlank()) return
        val chatId = state.chatId
        val maxTokens = state.maxTokensInput.toIntOrNull() ?: DEFAULT_MAX_TOKENS
        val stopSequence = state.stopSequenceInput.takeIf { it.isNotBlank() }
        val systemPrompt = state.systemPromptInput.takeIf { it.isNotBlank() }
        val historyForApi = state.messages.map {
            ChatMessage(role = if (it.isFromUser) ChatMessage.Role.USER else ChatMessage.Role.ASSISTANT, content = it.content)
        } + ChatMessage(ChatMessage.Role.USER, userInput)

        updateSuccess { it.copy(isSending = true, sendError = null, inputText = "") }

        viewModelScope.launch {
            saveMessageUseCase(chatId, ChatMessage.Role.USER, userInput)
                .onFailure {
                    updateSuccess { s -> s.copy(isSending = false, sendError = "Failed to save message") }
                    return@launch
                }

            updateChatSettingsUseCase(chatId, maxTokens, systemPrompt, stopSequence)

            sendMessageUseCase(historyForApi, maxTokens, stopSequence, systemPrompt)
                .onSuccess { assistantMsg ->
                    saveMessageUseCase(chatId, ChatMessage.Role.ASSISTANT, assistantMsg.content)
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
            getChatByIdUseCase(chatId)
                .onSuccess { chat ->
                    if (chat == null) { _uiState.value = HomeUiState.Error("Chat not found"); return@launch }
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
            createChatUseCase()
                .onSuccess { chat ->
                    _uiState.value = HomeUiState.Success(chatId = chat.id, chatName = chat.name)
                    currentChatId.value = chat.id
                }
                .onFailure { _uiState.value = HomeUiState.Error(it.message ?: "Failed to create chat") }
        }
    }

    companion object {
        private const val DEFAULT_MAX_TOKENS = 512
    }
}
