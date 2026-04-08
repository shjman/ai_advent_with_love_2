package com.yahorshymanchyk.ai_advent_with_love_2.data.remote

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage

class ClaudeRemoteDataSourceImpl(private val apiService: ClaudeApiService) : ClaudeRemoteDataSource {
    override suspend fun sendMessage(history: List<ChatMessage>, maxTokens: Int, stopSequence: String?, systemPrompt: String?): String =
        apiService.sendMessage(history, maxTokens, stopSequence, systemPrompt)
    override suspend fun countTokens(history: List<ChatMessage>, systemPrompt: String?): Int =
        apiService.countTokens(history, systemPrompt)
}
