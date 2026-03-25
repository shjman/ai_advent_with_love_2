package com.yahorshymanchyk.ai_advent_with_love_2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: Long): Flow<List<MessageEntity>>

    @Insert
    suspend fun insertMessage(message: MessageEntity)
}
