package com.yahorshymanchyk.ai_advent_with_love_2.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val maxTokens: Int,
    val systemPrompt: String?,
    val stopSequence: String?,
    val createdAt: Long,
    val updatedAt: Long
)
