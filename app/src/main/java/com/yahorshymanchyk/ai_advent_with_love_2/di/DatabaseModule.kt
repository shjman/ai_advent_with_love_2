package com.yahorshymanchyk.ai_advent_with_love_2.di

import android.content.Context
import androidx.room.Room
import com.yahorshymanchyk.ai_advent_with_love_2.data.local.ChatLocalDataSource
import com.yahorshymanchyk.ai_advent_with_love_2.data.local.ChatLocalDataSourceImpl
import com.yahorshymanchyk.ai_advent_with_love_2.data.local.ChatRepositoryImpl
import com.yahorshymanchyk.ai_advent_with_love_2.database.AppDatabase
import com.yahorshymanchyk.ai_advent_with_love_2.database.dao.ChatDao
import com.yahorshymanchyk.ai_advent_with_love_2.database.dao.MessageDao
import com.yahorshymanchyk.ai_advent_with_love_2.domain.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app_database").build()

    @Provides
    @Singleton
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    @Singleton
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    @Singleton
    fun provideChatLocalDataSource(chatDao: ChatDao, messageDao: MessageDao): ChatLocalDataSource =
        ChatLocalDataSourceImpl(chatDao, messageDao)

    @Provides
    @Singleton
    fun provideChatRepository(dataSource: ChatLocalDataSource): ChatRepository =
        ChatRepositoryImpl(dataSource)
}
