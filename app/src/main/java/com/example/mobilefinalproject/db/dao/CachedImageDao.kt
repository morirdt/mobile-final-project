package com.example.mobilefinalproject.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mobilefinalproject.db.entity.CachedImageEntity

@Dao
interface CachedImageDao {

    @Query("SELECT * FROM cached_images WHERE remoteUrl = :url")
    suspend fun get(url: String): CachedImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedImageEntity)

    @Query("DELETE FROM cached_images WHERE remoteUrl = :url")
    suspend fun delete(url: String)

    /** Remove entries older than [olderThan] millis. Caller is responsible for deleting the files. */
    @Query("SELECT * FROM cached_images WHERE cachedAt < :olderThan")
    suspend fun getOlderThan(olderThan: Long): List<CachedImageEntity>

    @Query("DELETE FROM cached_images WHERE cachedAt < :olderThan")
    suspend fun evictOlderThan(olderThan: Long)

    @Query("DELETE FROM cached_images")
    suspend fun deleteAll()
}

