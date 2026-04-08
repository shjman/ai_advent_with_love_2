package com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ChatRepository
import javax.inject.Inject

class SaveMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: Long, role: ChatMessage.Role, content: String): Result<Unit> =
        runCatching { repository.saveMessage(chatId, role, content) }
}
