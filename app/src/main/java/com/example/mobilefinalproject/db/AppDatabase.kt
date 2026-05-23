package com.example.mobilefinalproject.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mobilefinalproject.db.dao.CachedImageDao
import com.example.mobilefinalproject.db.dao.ChatMessageDao
import com.example.mobilefinalproject.db.dao.ConversationDao
import com.example.mobilefinalproject.db.dao.DriverProfileDao
import com.example.mobilefinalproject.db.dao.OrderDao
import com.example.mobilefinalproject.db.dao.UserDao
import com.example.mobilefinalproject.db.entity.CachedImageEntity
import com.example.mobilefinalproject.db.entity.ChatMessageEntity
import com.example.mobilefinalproject.db.entity.ConversationEntity
import com.example.mobilefinalproject.db.entity.DriverProfileEntity
import com.example.mobilefinalproject.db.entity.OrderEntity
import com.example.mobilefinalproject.db.entity.UserEntity

@Database(
    entities = [
        OrderEntity::class,
        UserEntity::class,
        DriverProfileEntity::class,
        ConversationEntity::class,
        ChatMessageEntity::class,
        CachedImageEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao
    abstract fun driverProfileDao(): DriverProfileDao
    abstract fun conversationDao(): ConversationDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun cachedImageDao(): CachedImageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_cache.db"
                )
                    // Allow destructive migration for development.  Replace with proper
                    // Migration objects before shipping to production.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

