package com.yahorshymanchyk.ai_advent_with_love_2.domain.repository

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.Message

interface ClaudeRepository {
    suspend fun sendMessage(userMessage: String): Result<Message>
}
