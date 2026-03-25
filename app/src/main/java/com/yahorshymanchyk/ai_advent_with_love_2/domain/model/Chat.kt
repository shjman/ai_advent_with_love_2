package com.yahorshymanchyk.ai_advent_with_love_2.domain.model

data class Chat(
    val id: Long,
    val name: String,
    val maxTokens: Int,
    val systemPrompt: String?,
    val stopSequence: String?,
    val createdAt: Long,
    val updatedAt: Long
)
