package com.example.mobilefinalproject.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mobilefinalproject.db.entity.DriverProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverProfileDao {

    @Query("SELECT * FROM driver_profiles WHERE userId = :userId")
    fun observeByUserId(userId: Int): Flow<DriverProfileEntity?>

    @Query("SELECT * FROM driver_profiles WHERE userId = :userId")
    suspend fun getByUserId(userId: Int): DriverProfileEntity?

    /** Returns the most recently cached profile — useful as a fallback. */
    @Query("SELECT * FROM driver_profiles ORDER BY cachedAt DESC LIMIT 1")
    fun observeLatest(): Flow<DriverProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: DriverProfileEntity)

    @Query("DELETE FROM driver_profiles WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Int)

    @Query("DELETE FROM driver_profiles")
    suspend fun deleteAll()
}

