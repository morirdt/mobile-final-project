package com.example.mobilefinalproject.db.entity

import androidx.room.Entity

/**
 * Cached order row.
 *
 * Because the same server order can appear in multiple UI lists (e.g. a completed order shows
 * in both customer history and driver completed), the primary key is a composite of
 * [orderId] + [listType].  All lists are refreshed independently and stored under one table.
 *
 * List-type constants live in the companion object.
 */
@Entity(tableName = "orders", primaryKeys = ["orderId", "listType"])
data class OrderEntity(
    val orderId: Int,
    /** One of the LIST_TYPE_* constants. */
    val listType: String,
    val customerId: Int,
    val driverId: Int?,
    val status: String,
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffAddress: String,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val cargoDescription: String?,
    val cargoWeightKg: Double?,
    val notes: String?,
    val priceCents: Int,
    val currency: String,
    val acceptedAt: String?,
    val startedAt: String?,
    val pickedUpAt: String?,
    val completedAt: String?,
    val cancelledAt: String?,
    val cancellationReason: String?,
    val cargoImageUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    /** Unix millis — used for eviction of stale cache entries. */
    val cachedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val LIST_CUSTOMER_HISTORY = "customer_history"
        const val LIST_DRIVER_PENDING   = "driver_pending"
        const val LIST_DRIVER_ACTIVE    = "driver_active"
        const val LIST_DRIVER_COMPLETED = "driver_completed"
    }
}

