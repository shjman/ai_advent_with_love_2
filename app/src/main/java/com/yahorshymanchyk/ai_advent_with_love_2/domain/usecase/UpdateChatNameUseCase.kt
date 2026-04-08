package com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase

import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ChatRepository
import javax.inject.Inject

class UpdateChatNameUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: Long, name: String): Result<Unit> =
        runCatching { repository.updateChatName(chatId, name) }
}
