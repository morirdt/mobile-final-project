package com.example.mobilefinalproject.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached conversation summary. The last-message fields are flattened to avoid
 * a nested object (Room doesn't support nested entity types without @Embedded).
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: Int,
    val orderId: Int,
    val lastMessageBody: String?,
    val lastMessageSenderId: Int?,
    val lastMessageSenderName: String?,
    val lastMessageCreatedAt: String?,
    val unreadCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val cachedAt: Long = System.currentTimeMillis()
)

