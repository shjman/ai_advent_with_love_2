package com.yahorshymanchyk.ai_advent_with_love_2.presentation.chats

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.Chat

data class ChatUiModel(
    val id: Long,
    val name: String
)

fun Chat.toUiModel() = ChatUiModel(id = id, name = name)
