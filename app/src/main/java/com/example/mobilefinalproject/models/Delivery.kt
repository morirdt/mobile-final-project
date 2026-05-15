package com.example.mobilefinalproject.models

import java.util.Date

data class Delivery(
    val id: String,
    val customerName: String,
    val status: String,
    val price: Double,
    val date: Date,
    val pickupAddress: String,
    val dropoffAddress: String,
    val phoneNumber: String,
    val rating: Int,
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val dropoffLat: Double = 0.0,
    val dropoffLng: Double = 0.0,
)
