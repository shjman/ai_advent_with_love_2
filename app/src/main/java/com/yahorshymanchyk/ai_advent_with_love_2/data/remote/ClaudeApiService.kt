package com.yahorshymanchyk.ai_advent_with_love_2.data.remote

import com.anthropic.client.AnthropicClient
import com.anthropic.models.messages.MessageCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClaudeApiService(private val client: AnthropicClient) {

    suspend fun sendMessage(userMessage: String): String = withContext(Dispatchers.IO) {
        val params = MessageCreateParams.builder()
            .model("claude-haiku-4-5-20251001")
            .maxTokens(1024)
            .addUserMessage(userMessage)
            .build()

        val response = client.messages().create(params)

        response.content()
            .filter { it.isText() }
            .joinToString("") { it.asText().text() }
    }
}
