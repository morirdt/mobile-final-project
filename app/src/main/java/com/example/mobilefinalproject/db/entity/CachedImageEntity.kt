package com.example.mobilefinalproject.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Maps a remote image URL to a local file path inside the app's filesDir.
 * Used by [com.example.mobilefinalproject.cache.ImageCacheManager] to serve images fully offline.
 */
@Entity(tableName = "cached_images")
data class CachedImageEntity(
    /** The full remote URL of the image. Used as the primary key / lookup key. */
    @PrimaryKey val remoteUrl: String,
    /** Absolute path to the locally downloaded image file. */
    val localFilePath: String,
    /** Unix millis when this entry was last refreshed — used for eviction. */
    val cachedAt: Long = System.currentTimeMillis()
)

