package com.example.mobilefinalproject.model

import java.io.Serializable

data class Location(
    val address: String,
    val latitude: Double,
    val longitude: Double
) : Serializable
