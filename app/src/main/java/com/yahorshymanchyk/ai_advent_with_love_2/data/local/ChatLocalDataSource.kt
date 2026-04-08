package com.yahorshymanchyk.ai_advent_with_love_2.data.local

import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.ChatEntity
import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

interface ChatLocalDataSource {
    fun getAllChats(): Flow<List<ChatEntity>>
    suspend fun getLatestChat(): ChatEntity?
    suspend fun getChatById(chatId: Long): ChatEntity?
    suspend fun insertChat(entity: ChatEntity): Long
    suspend fun updateChat(entity: ChatEntity)
    suspend fun insertMessage(entity: MessageEntity)
    fun getMessagesForChat(chatId: Long): Flow<List<MessageEntity>>
}
