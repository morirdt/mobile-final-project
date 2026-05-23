package com.example.mobilefinalproject.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mobilefinalproject.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observe(userId: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun get(userId: Int): UserEntity?

    /** Returns the most recently cached user regardless of id — useful as a fallback. */
    @Query("SELECT * FROM users ORDER BY cachedAt DESC LIMIT 1")
    fun observeLatest(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun delete(userId: Int)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

