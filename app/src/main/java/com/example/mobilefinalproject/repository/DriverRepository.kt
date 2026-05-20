package com.example.mobilefinalproject.repository

import android.content.Context
import com.example.mobilefinalproject.network.RetrofitClient
import com.example.mobilefinalproject.network.api.DriverApi
import com.example.mobilefinalproject.network.dto.DriverLocationUpdateRequest
import com.example.mobilefinalproject.network.dto.DriverProfile
import com.example.mobilefinalproject.network.dto.DriverProfileUpdateRequest
import com.example.mobilefinalproject.network.dto.DriverStatusUpdateRequest
import com.example.mobilefinalproject.network.dto.PageResponse
import com.example.mobilefinalproject.network.dto.RatingRead

class DriverRepository(context: Context) {

    private val api: DriverApi = RetrofitClient.createService(context)

    suspend fun updateProfile(
        vehicleType: String? = null,
        vehiclePlate: String? = null,
        vehicleCapacityKg: Double? = null
    ): ApiResult<DriverProfile> = safeApiCall {
        api.updateProfile(DriverProfileUpdateRequest(vehicleType, vehiclePlate, vehicleCapacityKg))
    }

    suspend fun updateStatus(status: String): ApiResult<DriverProfile> = safeApiCall {
        api.updateStatus(DriverStatusUpdateRequest(status))
    }

    suspend fun updateLocation(lat: Double, lng: Double): ApiResult<DriverProfile> = safeApiCall {
        api.updateLocation(DriverLocationUpdateRequest(lat, lng))
    }

    suspend fun listRatings(
        driverId: Int,
        limit: Int = 20,
        offset: Int = 0
    ): ApiResult<PageResponse<RatingRead>> = safeApiCall {
        api.listRatings(driverId, limit, offset)
    }
}
