package com.example.mobilefinalproject.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mobilefinalproject.db.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun observe(conversationId: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteByConversation(conversationId: Int)

    /**
     * Atomically replaces all messages for the given conversation.
     * Ensures deleted server-side messages don't linger in the local cache.
     */
    @Transaction
    suspend fun replaceConversationMessages(
        conversationId: Int,
        messages: List<ChatMessageEntity>
    ) {
        deleteByConversation(conversationId)
        if (messages.isNotEmpty()) upsertAll(messages)
    }

    @Query("DELETE FROM chat_messages WHERE cachedAt < :olderThan")
    suspend fun evictOlderThan(olderThan: Long)
}

