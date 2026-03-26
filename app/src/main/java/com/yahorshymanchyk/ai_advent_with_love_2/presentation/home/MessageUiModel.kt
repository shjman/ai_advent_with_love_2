package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage

data class MessageUiModel(
    val content: String,
    val isFromUser: Boolean
)

fun ChatMessage.toUiModel() = MessageUiModel(
    content = content,
    isFromUser = role == ChatMessage.Role.USER
)
