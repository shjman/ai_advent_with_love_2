package com.yahorshymanchyk.ai_advent_with_love_2.domain.model

data class ChatMessage(
    val role: Role,
    val content: String
) {
    enum class Role { USER, ASSISTANT }
}
