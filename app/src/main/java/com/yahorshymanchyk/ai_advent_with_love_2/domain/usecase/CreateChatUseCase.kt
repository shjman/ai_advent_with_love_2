package com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.Chat
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ChatRepository
import javax.inject.Inject

class CreateChatUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): Result<Chat> =
        runCatching { repository.createChat() }
}
