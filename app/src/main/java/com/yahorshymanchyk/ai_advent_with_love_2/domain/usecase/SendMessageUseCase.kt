package com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.Message
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ClaudeRepository

class SendMessageUseCase(private val repository: ClaudeRepository) {
    suspend operator fun invoke(userMessage: String): Result<Message> =
        repository.sendMessage(userMessage)
}
