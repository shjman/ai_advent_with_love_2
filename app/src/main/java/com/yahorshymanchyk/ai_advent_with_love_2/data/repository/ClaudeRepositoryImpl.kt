package com.yahorshymanchyk.ai_advent_with_love_2.data.repository

import com.yahorshymanchyk.ai_advent_with_love_2.data.remote.ClaudeApiService
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.Message
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ClaudeRepository

class ClaudeRepositoryImpl(private val apiService: ClaudeApiService) : ClaudeRepository {

    override suspend fun sendMessage(userMessage: String): Result<Message> =
        runCatching { Message(apiService.sendMessage(userMessage)) }
}
