package com.example.mobilefinalproject.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cached user profile (UserMe). Keyed by the server-side user id. */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val fullName: String,
    val role: String,
    val profileImageUrl: String?,
    val createdAt: String,
    val email: String,
    val phone: String?,
    val isActive: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)

