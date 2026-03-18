package com.yahorshymanchyk.ai_advent_with_love_2.data.remote

import com.anthropic.client.AnthropicClient
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.MessageParam
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ClaudeApiService(private val client: AnthropicClient) {

    suspend fun sendMessage(history: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        Timber.d("Sending message: %s (history: %d messages)", history.last().content, history.size)

        val paramsBuilder = MessageCreateParams.builder()
            .model("claude-haiku-4-5-20251001")
            .maxTokens(1024)

        history.forEach { msg ->
            val role = when (msg.role) {
                ChatMessage.Role.USER -> MessageParam.Role.USER
                ChatMessage.Role.ASSISTANT -> MessageParam.Role.ASSISTANT
            }
            paramsBuilder.addMessage(
                MessageParam.builder()
                    .role(role)
                    .content(msg.content)
                    .build()
            )
        }

        val startMs = System.currentTimeMillis()
        val response = client.messages().create(paramsBuilder.build())
        val elapsedMs = System.currentTimeMillis() - startMs

        val responseText = response.content()
            .filter { it.isText() }
            .joinToString("") { it.asText().text() }

        Timber.d("Response received in %dms: %s", elapsedMs, responseText)

        responseText
    }
}
