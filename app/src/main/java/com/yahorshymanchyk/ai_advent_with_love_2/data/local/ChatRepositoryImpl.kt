package com.yahorshymanchyk.ai_advent_with_love_2.data.local

import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.ChatEntity
import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.MessageEntity
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.Chat
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val dataSource: ChatLocalDataSource
) : ChatRepository {

    override fun getAllChats(): Flow<List<Chat>> =
        dataSource.getAllChats().map { entities -> entities.map { it.toChat() } }

    override suspend fun getLatestChat(): Chat? =
        dataSource.getLatestChat()?.toChat()

    override suspend fun getChatById(chatId: Long): Chat? =
        dataSource.getChatById(chatId)?.toChat()

    override suspend fun createChat(): Chat {
        val now = System.currentTimeMillis()
        val entity = ChatEntity(
            name = "New chat", maxTokens = 512, systemPrompt = null,
            stopSequence = null, createdAt = now, updatedAt = now
        )
        val id = dataSource.insertChat(entity)
        return entity.copy(id = id).toChat()
    }

    override suspend fun updateChatName(chatId: Long, name: String) {
        val existing = dataSource.getChatById(chatId) ?: return
        dataSource.updateChat(existing.copy(name = name, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateChatSettings(
        chatId: Long,
        maxTokens: Int,
        systemPrompt: String?,
        stopSequence: String?
    ) {
        val existing = dataSource.getChatById(chatId) ?: return
        dataSource.updateChat(
            existing.copy(
                maxTokens = maxTokens,
                systemPrompt = systemPrompt,
                stopSequence = stopSequence,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun saveMessage(chatId: Long, role: ChatMessage.Role, content: String) {
        val messageEntity = MessageEntity(
            chatId = chatId,
            role = role.name.lowercase(),
            content = content,
            timestamp = System.currentTimeMillis()
        )
        dataSource.insertMessage(messageEntity)
        val chat = dataSource.getChatById(chatId) ?: return
        dataSource.updateChat(chat.copy(updatedAt = System.currentTimeMillis()))
    }

    override fun getMessagesForChat(chatId: Long): Flow<List<ChatMessage>> =
        dataSource.getMessagesForChat(chatId).map { entities -> entities.map { it.toChatMessage() } }
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
