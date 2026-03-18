package com.yahorshymanchyk.ai_advent_with_love_2.data.repository

import com.yahorshymanchyk.ai_advent_with_love_2.data.remote.ClaudeApiService
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ClaudeRepository

class ClaudeRepositoryImpl(private val apiService: ClaudeApiService) : ClaudeRepository {

    override suspend fun sendMessage(history: List<ChatMessage>, maxTokens: Int, stopSequence: String?): Result<ChatMessage> =
        runCatching {
            ChatMessage(
                role = ChatMessage.Role.ASSISTANT,
                content = apiService.sendMessage(history, maxTokens, stopSequence)
            )
        }
}
