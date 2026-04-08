package com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase

import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ChatRepository
import javax.inject.Inject

class UpdateChatSettingsUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: Long, maxTokens: Int, systemPrompt: String?, stopSequence: String?): Result<Unit> =
        runCatching { repository.updateChatSettings(chatId, maxTokens, systemPrompt, stopSequence) }
}
