package com.yahorshymanchyk.ai_advent_with_love_2.data.repository

import com.yahorshymanchyk.ai_advent_with_love_2.data.remote.ClaudeRemoteDataSource
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ClaudeRepository

class ClaudeRepositoryImpl(private val dataSource: ClaudeRemoteDataSource) : ClaudeRepository {

    override suspend fun sendMessage(
        history: List<ChatMessage>,
        maxTokens: Int,
        stopSequence: String?,
        systemPrompt: String?
    ): Result<ChatMessage> =
        runCatching {
            val content = dataSource.sendMessage(history, maxTokens, stopSequence, systemPrompt)
            ChatMessage(role = ChatMessage.Role.ASSISTANT, content = content)
        }

    override suspend fun countTokens(history: List<ChatMessage>, systemPrompt: String?): Result<Int> =
        runCatching { dataSource.countTokens(history, systemPrompt) }
}
