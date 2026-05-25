package com.example.mobilefinalproject.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cached driver profile. Keyed by the server-side driver profile id. */
@Entity(tableName = "driver_profiles")
data class DriverProfileEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val licenseNumber: String,
    val vehicleType: String,
    val vehiclePlate: String,
    val vehicleCapacityKg: Double?,
    val status: String,
    val currentLat: Double?,
    val currentLng: Double?,
    val lastLocationAt: String?,
    val rating: Double,
    val cachedAt: Long = System.currentTimeMillis()
)

