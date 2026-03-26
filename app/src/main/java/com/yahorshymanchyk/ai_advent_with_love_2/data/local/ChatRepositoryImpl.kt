package com.yahorshymanchyk.ai_advent_with_love_2.data.local

import com.yahorshymanchyk.ai_advent_with_love_2.database.dao.ChatDao
import com.yahorshymanchyk.ai_advent_with_love_2.database.dao.MessageDao
import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.ChatEntity
import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.MessageEntity
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.Chat
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) : ChatRepository {

    override fun getAllChats(): Flow<List<Chat>> =
        chatDao.getAllChats().map { entities -> entities.map { it.toChat() } }

    override suspend fun getLatestChat(): Chat? =
        chatDao.getLatestChat()?.toChat()

    override suspend fun getChatById(chatId: Long): Chat? =
        chatDao.getChatById(chatId)?.toChat()

    override suspend fun createChat(): Chat {
        val now = System.currentTimeMillis()
        val entity = ChatEntity(
            name = "New chat",
            maxTokens = 512,
            systemPrompt = null,
            stopSequence = null,
            createdAt = now,
            updatedAt = now
        )
        val id = chatDao.insertChat(entity)
        return entity.copy(id = id).toChat()
    }

    override suspend fun updateChatName(chatId: Long, name: String) {
        val existing = chatDao.getChatById(chatId) ?: return
        chatDao.updateChat(existing.copy(name = name, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateChatSettings(
        chatId: Long,
        maxTokens: Int,
        systemPrompt: String?,
        stopSequence: String?
    ) {
        val existing = chatDao.getChatById(chatId) ?: return
        chatDao.updateChat(
            existing.copy(
                maxTokens = maxTokens,
                systemPrompt = systemPrompt,
                stopSequence = stopSequence,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun saveMessage(chatId: Long, role: ChatMessage.Role, content: String) {
        messageDao.insertMessage(
            MessageEntity(
                chatId = chatId,
                role = role.name.lowercase(),
                content = content,
                timestamp = System.currentTimeMillis()
            )
        )
        val chat = chatDao.getChatById(chatId) ?: return
        chatDao.updateChat(chat.copy(updatedAt = System.currentTimeMillis()))
    }

    override fun getMessagesForChat(chatId: Long): Flow<List<ChatMessage>> =
        messageDao.getMessagesForChat(chatId).map { entities -> entities.map { it.toChatMessage() } }
}

private fun ChatEntity.toChat() = Chat(
    id = id,
    name = name,
    maxTokens = maxTokens,
    systemPrompt = systemPrompt,
    stopSequence = stopSequence,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun MessageEntity.toChatMessage() = ChatMessage(
    role = if (role == "user") ChatMessage.Role.USER else ChatMessage.Role.ASSISTANT,
    content = content
)
