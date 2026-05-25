package com.example.mobilefinalproject.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cached chat message. Keyed by the server-side message id. */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val senderName: String,
    val body: String,
    val createdAt: String,
    val isRead: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)

