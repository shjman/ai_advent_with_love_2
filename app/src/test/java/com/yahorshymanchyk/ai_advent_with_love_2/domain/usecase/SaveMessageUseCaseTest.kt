package com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase

import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveMessageUseCaseTest {

    private val repository: ChatRepository = mockk()
    private lateinit var useCase: SaveMessageUseCase

    @Before
    fun setUp() {
        useCase = SaveMessageUseCase(repository)
    }

    @Test
    fun `invoke returns success and delegates to repository`() = runTest {
        coEvery { repository.saveMessage(any(), any(), any()) } returns Unit

        val result = useCase(chatId = 1L, role = ChatMessage.Role.USER, content = "Hello")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.saveMessage(1L, ChatMessage.Role.USER, "Hello") }
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        val exception = RuntimeException("DB error")
        coEvery { repository.saveMessage(any(), any(), any()) } throws exception

        val result = useCase(chatId = 2L, role = ChatMessage.Role.ASSISTANT, content = "Hi")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
