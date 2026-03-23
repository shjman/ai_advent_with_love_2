package com.yahorshymanchyk.ai_advent_with_love_2.presentation

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ClaudeRepository
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClaudeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty with no loading and no error`() {
        val viewModel = buildViewModel()

        val state = viewModel.uiState.value

        assertTrue(state.messages.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `sendMessage immediately adds user message and sets isLoading`() = runTest {
        val viewModel = buildViewModel()

        viewModel.sendMessage("Hello", 512, null, null)

        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertEquals(1, state.messages.size)
        assertEquals("Hello", state.messages[0].content)
        assertEquals(ChatMessage.Role.USER, state.messages[0].role)
    }

    @Test
    fun `sendMessage on success appends assistant message and clears loading`() = runTest {
        val response = ChatMessage(ChatMessage.Role.ASSISTANT, "Hi!")
        val viewModel = buildViewModel(result = Result.success(response))

        viewModel.sendMessage("Hello", 512, null, null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.messages.size)
        assertEquals("Hello", state.messages[0].content)
        assertEquals("Hi!", state.messages[1].content)
        assertEquals(ChatMessage.Role.ASSISTANT, state.messages[1].role)
    }

    @Test
    fun `sendMessage on failure sets error and preserves user message`() = runTest {
        val viewModel = buildViewModel(result = Result.failure(RuntimeException("Network error")))

        viewModel.sendMessage("Hello", 512, null, null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
        assertEquals(1, state.messages.size)
        assertEquals(ChatMessage.Role.USER, state.messages[0].role)
    }

    @Test
    fun `sendMessage on failure with no message uses fallback error text`() = runTest {
        val viewModel = buildViewModel(result = Result.failure(RuntimeException()))

        viewModel.sendMessage("Hello", 512, null, null)
        advanceUntilIdle()

        assertEquals("Unknown error", viewModel.uiState.value.error)
    }

    @Test
    fun `multi-turn conversation preserves full history`() = runTest {
        var callCount = 0
        val repo = object : ClaudeRepository {
            override suspend fun sendMessage(
                history: List<ChatMessage>, maxTokens: Int, stopSequence: String?, systemPrompt: String?
            ) = Result.success(ChatMessage(ChatMessage.Role.ASSISTANT, "Reply ${++callCount}"))
        }
        val viewModel = ClaudeViewModel(SendMessageUseCase(repo))

        viewModel.sendMessage("First", 512, null, null)
        advanceUntilIdle()
        viewModel.sendMessage("Second", 512, null, null)
        advanceUntilIdle()

        val messages = viewModel.uiState.value.messages
        assertEquals(4, messages.size)
        assertEquals("First", messages[0].content)
        assertEquals("Reply 1", messages[1].content)
        assertEquals("Second", messages[2].content)
        assertEquals("Reply 2", messages[3].content)
    }

    @Test
    fun `error is cleared on next successful send`() = runTest {
        var shouldFail = true
        val repo = object : ClaudeRepository {
            override suspend fun sendMessage(
                history: List<ChatMessage>, maxTokens: Int, stopSequence: String?, systemPrompt: String?
            ) = if (shouldFail) Result.failure(RuntimeException("fail"))
                else Result.success(ChatMessage(ChatMessage.Role.ASSISTANT, "ok"))
        }
        val viewModel = ClaudeViewModel(SendMessageUseCase(repo))

        viewModel.sendMessage("First", 512, null, null)
        advanceUntilIdle()
        assertEquals("fail", viewModel.uiState.value.error)

        shouldFail = false
        viewModel.sendMessage("Second", 512, null, null)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }

    private fun buildViewModel(
        result: Result<ChatMessage> = Result.success(
            ChatMessage(ChatMessage.Role.ASSISTANT, "response")
        )
    ): ClaudeViewModel {
        val repo = object : ClaudeRepository {
            override suspend fun sendMessage(
                history: List<ChatMessage>, maxTokens: Int, stopSequence: String?, systemPrompt: String?
            ) = result
        }
        return ClaudeViewModel(SendMessageUseCase(repo))
    }
}
