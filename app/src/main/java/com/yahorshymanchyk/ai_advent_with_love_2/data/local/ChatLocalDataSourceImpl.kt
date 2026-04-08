package com.yahorshymanchyk.ai_advent_with_love_2.data.local

import com.yahorshymanchyk.ai_advent_with_love_2.database.dao.ChatDao
import com.yahorshymanchyk.ai_advent_with_love_2.database.dao.MessageDao
import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.ChatEntity
import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class ChatLocalDataSourceImpl(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) : ChatLocalDataSource {
    override fun getAllChats(): Flow<List<ChatEntity>> = chatDao.getAllChats()
    override suspend fun getLatestChat(): ChatEntity? = chatDao.getLatestChat()
    override suspend fun getChatById(chatId: Long): ChatEntity? = chatDao.getChatById(chatId)
    override suspend fun insertChat(entity: ChatEntity): Long = chatDao.insertChat(entity)
    override suspend fun updateChat(entity: ChatEntity) = chatDao.updateChat(entity)
    override suspend fun insertMessage(entity: MessageEntity) = messageDao.insertMessage(entity)
    override fun getMessagesForChat(chatId: Long): Flow<List<MessageEntity>> = messageDao.getMessagesForChat(chatId)
}
