package com.yahorshymanchyk.ai_advent_with_love_2.domain.repository

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.Chat
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllChats(): Flow<List<Chat>>
    suspend fun getLatestChat(): Chat?
    suspend fun createChat(): Chat
    suspend fun updateChatSettings(chatId: Long, maxTokens: Int, systemPrompt: String?, stopSequence: String?)
    suspend fun saveMessage(chatId: Long, role: ChatMessage.Role, content: String)
    fun getMessagesForChat(chatId: Long): Flow<List<ChatMessage>>
}
