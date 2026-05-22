package com.example.mobilefinalproject.network.api

import com.example.mobilefinalproject.network.dto.DriverLocationUpdateRequest
import com.example.mobilefinalproject.network.dto.DriverProfile
import com.example.mobilefinalproject.network.dto.DriverProfileUpdateRequest
import com.example.mobilefinalproject.network.dto.DriverStatusUpdateRequest
import com.example.mobilefinalproject.network.dto.PageResponse
import com.example.mobilefinalproject.network.dto.RatingRead
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DriverApi {

    @GET("api/v1/drivers/me")
    suspend fun getMe(): Response<DriverProfile>

    @PUT("api/v1/drivers/me/profile")
    suspend fun updateProfile(@Body body: DriverProfileUpdateRequest): Response<DriverProfile>

    @PUT("api/v1/drivers/me/status")
    suspend fun updateStatus(@Body body: DriverStatusUpdateRequest): Response<DriverProfile>

    @POST("api/v1/drivers/me/location")
    suspend fun updateLocation(@Body body: DriverLocationUpdateRequest): Response<DriverProfile>

    @GET("api/v1/drivers/{driver_id}/ratings")
    suspend fun listRatings(
        @Path("driver_id") driverId: Int,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<PageResponse<RatingRead>>
}
