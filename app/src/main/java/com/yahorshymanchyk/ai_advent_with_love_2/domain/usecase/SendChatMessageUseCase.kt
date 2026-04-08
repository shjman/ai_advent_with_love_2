package com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val saveMessageUseCase: SaveMessageUseCase,
    private val updateChatSettingsUseCase: UpdateChatSettingsUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) {
    suspend operator fun invoke(
        chatId: Long,
        userInput: String,
        history: List<ChatMessage>,
        maxTokens: Int,
        systemPrompt: String?,
        stopSequence: String?
    ): Result<ChatMessage> = runCatching {
        saveMessageUseCase(chatId, ChatMessage.Role.USER, userInput).getOrThrow()
        updateChatSettingsUseCase(chatId, maxTokens, systemPrompt, stopSequence)
        val response = sendMessageUseCase(history, maxTokens, stopSequence, systemPrompt).getOrThrow()
        saveMessageUseCase(chatId, ChatMessage.Role.ASSISTANT, response.content).getOrThrow()
        response
    }
}
