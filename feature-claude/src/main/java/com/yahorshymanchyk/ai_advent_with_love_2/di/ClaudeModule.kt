package com.yahorshymanchyk.ai_advent_with_love_2.di

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.yahorshymanchyk.ai_advent_with_love_2.data.remote.ClaudeApiService
import com.yahorshymanchyk.ai_advent_with_love_2.data.repository.ClaudeRepositoryImpl
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ClaudeRepository
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.SendMessageUseCase
import com.yahorshymanchyk.ai_advent_with_love_2.feature.claude.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClaudeModule {

    @Provides
    @Singleton
    fun provideAnthropicClient(): AnthropicClient =
        AnthropicOkHttpClient.builder()
            .apiKey(BuildConfig.CLAUDE_API_KEY)
            .build()

    @Provides
    @Singleton
    fun provideClaudeApiService(client: AnthropicClient): ClaudeApiService =
        ClaudeApiService(client)

    @Provides
    @Singleton
    fun provideClaudeRepository(apiService: ClaudeApiService): ClaudeRepository =
        ClaudeRepositoryImpl(apiService)

    @Provides
    fun provideSendMessageUseCase(repository: ClaudeRepository): SendMessageUseCase =
        SendMessageUseCase(repository)
}
