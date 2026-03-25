package com.yahorshymanchyk.ai_advent_with_love_2.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yahorshymanchyk.ai_advent_with_love_2.database.dao.ChatDao
import com.yahorshymanchyk.ai_advent_with_love_2.database.dao.MessageDao
import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.ChatEntity
import com.yahorshymanchyk.ai_advent_with_love_2.database.entity.MessageEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}
