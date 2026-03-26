package com.yahorshymanchyk.ai_advent_with_love_2.data.remote

import com.anthropic.client.AnthropicClient
import com.anthropic.models.messages.MessageCountTokensParams
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.MessageParam
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ClaudeApiService(private val client: AnthropicClient) {

    suspend fun sendMessage(
        history: List<ChatMessage>,
        maxTokens: Int,
        stopSequence: String?,
        systemPrompt: String?
    ): String = withContext(Dispatchers.IO) {
        val paramsBuilder = MessageCreateParams.builder()
            .model(MODEL)
            .maxTokens(maxTokens.toLong())

        if (!systemPrompt.isNullOrBlank()) paramsBuilder.system(systemPrompt)
        if (!stopSequence.isNullOrBlank()) paramsBuilder.addStopSequence(stopSequence)

        history.forEach { msg ->
            paramsBuilder.addMessage(
                MessageParam.builder()
                    .role(msg.toSdkRole())
                    .content(msg.content)
                    .build()
            )
        }

        val startMs = System.currentTimeMillis()
        val response = client.messages().create(paramsBuilder.build())
        val elapsedMs = System.currentTimeMillis() - startMs

        val inputTokens = response.usage().inputTokens()
        val outputTokens = response.usage().outputTokens()

        Timber.d(
            "tokens | in=%d | out=%d | elapsed=%dms | stop_reason=%s",
            inputTokens,
            outputTokens,
            elapsedMs,
            response.stopReason().orElse(null)
        )

        response.content()
            .filter { it.isText() }
            .joinToString("") { it.asText().text() }
    }

    suspend fun countTokens(history: List<ChatMessage>, systemPrompt: String?): Int =
        withContext(Dispatchers.IO) {
            val paramsBuilder = MessageCountTokensParams.builder().model(MODEL)

            if (!systemPrompt.isNullOrBlank()) paramsBuilder.system(systemPrompt)

            history.forEach { msg ->
                paramsBuilder.addMessage(
                    MessageParam.builder()
                        .role(msg.toSdkRole())
                        .content(msg.content)
                        .build()
                )
            }

            val result = client.messages().countTokens(paramsBuilder.build())
            result.inputTokens().toInt().also { count ->
                Timber.d("countTokens | history=%d messages | expected_input=%d", history.size, count)
            }
        }

    private fun ChatMessage.toSdkRole() = when (role) {
        ChatMessage.Role.USER -> MessageParam.Role.USER
        ChatMessage.Role.ASSISTANT -> MessageParam.Role.ASSISTANT
    }

    companion object {
        private const val MODEL = "claude-haiku-4-5-20251001"
    }
}
