package com.yahorshymanchyk.ai_advent_with_love_2.data.remote

import com.anthropic.client.AnthropicClient
import com.anthropic.models.messages.MessageCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ClaudeApiService(private val client: AnthropicClient) {

    suspend fun sendMessage(userMessage: String): String = withContext(Dispatchers.IO) {
        Timber.d("Sending message: %s", userMessage)

        val params = MessageCreateParams.builder()
            .model("claude-haiku-4-5-20251001")
            .maxTokens(1024)
            .addUserMessage(userMessage)
            .build()

        val startMs = System.currentTimeMillis()
        val response = client.messages().create(params)
        val elapsedMs = System.currentTimeMillis() - startMs

        val responseText = response.content()
            .filter { it.isText() }
            .joinToString("") { it.asText().text() }

        Timber.d("Response received in %dms: %s", elapsedMs, responseText)

        responseText
    }
}
