package com.example.mobilefinalproject.models

import android.net.Uri
import java.util.Date

data class Delivery(
    val id: String,
    val customerName: String,
    val driverName: String,
    val customerId: String,
    val status: String,
    val price: Double,
    val date: Date,
    val pickupLocation: Location,
    val destinationLocation: Location,
    val description: String = "",
    val imageUri: Uri? = null,
    val phoneNumber: String = "",
    val rating: Int = 0,
) {
    // Compatibility accessors for code paths that still expect address/lat/lng fields.
    val pickupAddress: String get() = pickupLocation.address
    val dropoffAddress: String get() = destinationLocation.address
    val pickupLat: Double get() = pickupLocation.latitude
    val pickupLng: Double get() = pickupLocation.longitude
    val dropoffLat: Double get() = destinationLocation.latitude
    val dropoffLng: Double get() = destinationLocation.longitude
}
