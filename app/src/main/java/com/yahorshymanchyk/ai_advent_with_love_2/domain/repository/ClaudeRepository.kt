package com.yahorshymanchyk.ai_advent_with_love_2.domain.repository

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage

interface ClaudeRepository {
    suspend fun sendMessage(history: List<ChatMessage>, maxTokens: Int): Result<ChatMessage>
}
