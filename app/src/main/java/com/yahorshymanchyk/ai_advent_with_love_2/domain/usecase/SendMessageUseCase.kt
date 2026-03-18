package com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ClaudeRepository

class SendMessageUseCase(private val repository: ClaudeRepository) {
    suspend operator fun invoke(history: List<ChatMessage>, maxTokens: Int, stopSequence: String?): Result<ChatMessage> =
        repository.sendMessage(history, maxTokens, stopSequence)
}
