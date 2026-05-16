package com.example.mobilefinalproject.models

import android.net.Uri
import java.io.Serializable
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
) : Serializable
