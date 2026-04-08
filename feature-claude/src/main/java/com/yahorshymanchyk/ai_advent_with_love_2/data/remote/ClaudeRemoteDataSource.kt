package com.yahorshymanchyk.ai_advent_with_love_2.data.remote

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage

interface ClaudeRemoteDataSource {
    suspend fun sendMessage(history: List<ChatMessage>, maxTokens: Int, stopSequence: String?, systemPrompt: String?): String
    suspend fun countTokens(history: List<ChatMessage>, systemPrompt: String?): Int
}
