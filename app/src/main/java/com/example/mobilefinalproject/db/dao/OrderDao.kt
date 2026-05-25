package com.example.mobilefinalproject.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mobilefinalproject.db.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    /** Reactive stream of all cached orders for the given list type, newest first. */
    @Query("SELECT * FROM orders WHERE listType = :listType ORDER BY createdAt DESC")
    fun observeByListType(listType: String): Flow<List<OrderEntity>>

    /** One-shot snapshot (useful for background work that doesn't need reactivity). */
    @Query("SELECT * FROM orders WHERE listType = :listType ORDER BY createdAt DESC")
    suspend fun getByListType(listType: String): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(orders: List<OrderEntity>)

    @Query("DELETE FROM orders WHERE listType = :listType")
    suspend fun deleteByListType(listType: String)

    /**
     * Atomically replaces every row for the given list type with [orders].
     * This ensures the cache always mirrors the last server response exactly.
     */
    @Transaction
    suspend fun replaceListType(listType: String, orders: List<OrderEntity>) {
        deleteByListType(listType)
        if (orders.isNotEmpty()) upsertAll(orders)
    }

    /** Evict entries older than [olderThan] millis to keep the DB lean. */
    @Query("DELETE FROM orders WHERE cachedAt < :olderThan")
    suspend fun evictOlderThan(olderThan: Long)
}

