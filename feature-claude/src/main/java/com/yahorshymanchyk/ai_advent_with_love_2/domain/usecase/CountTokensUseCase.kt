package com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ClaudeRepository
import javax.inject.Inject

class CountTokensUseCase @Inject constructor(
    private val repository: ClaudeRepository
) {
    suspend operator fun invoke(history: List<ChatMessage>, systemPrompt: String?): Result<Int> =
        repository.countTokens(history, systemPrompt)
}
