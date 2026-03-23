package com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ClaudeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SendMessageUseCaseTest {

    private val history = listOf(ChatMessage(ChatMessage.Role.USER, "Hello"))

    @Test
    fun `returns success result from repository`() = runTest {
        val expected = ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there")
        val useCase = SendMessageUseCase(FakeRepository(Result.success(expected)))

        val result = useCase(history, 512, null, null)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `returns failure result from repository`() = runTest {
        val error = RuntimeException("API error")
        val useCase = SendMessageUseCase(FakeRepository(Result.failure(error)))

        val result = useCase(history, 512, null, null)

        assertTrue(result.isFailure)
        assertEquals("API error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `passes all parameters to repository`() = runTest {
        val repo = CapturingRepository()
        val useCase = SendMessageUseCase(repo)

        useCase(history, 256, "STOP", "You are helpful")

        assertEquals(history, repo.capturedHistory)
        assertEquals(256, repo.capturedMaxTokens)
        assertEquals("STOP", repo.capturedStopSequence)
        assertEquals("You are helpful", repo.capturedSystemPrompt)
    }

    @Test
    fun `passes null optional parameters to repository`() = runTest {
        val repo = CapturingRepository()
        val useCase = SendMessageUseCase(repo)

        useCase(history, 512, null, null)

        assertNull(repo.capturedStopSequence)
        assertNull(repo.capturedSystemPrompt)
    }
}

private class FakeRepository(
    private val result: Result<ChatMessage> = Result.success(
        ChatMessage(ChatMessage.Role.ASSISTANT, "response")
    )
) : ClaudeRepository {
    override suspend fun sendMessage(
        history: List<ChatMessage>, maxTokens: Int, stopSequence: String?, systemPrompt: String?
    ): Result<ChatMessage> = result
}

private class CapturingRepository : ClaudeRepository {
    var capturedHistory: List<ChatMessage>? = null
    var capturedMaxTokens: Int? = null
    var capturedStopSequence: String? = null
    var capturedSystemPrompt: String? = null

    override suspend fun sendMessage(
        history: List<ChatMessage>, maxTokens: Int, stopSequence: String?, systemPrompt: String?
    ): Result<ChatMessage> {
        capturedHistory = history
        capturedMaxTokens = maxTokens
        capturedStopSequence = stopSequence
        capturedSystemPrompt = systemPrompt
        return Result.success(ChatMessage(ChatMessage.Role.ASSISTANT, "ok"))
    }
}
