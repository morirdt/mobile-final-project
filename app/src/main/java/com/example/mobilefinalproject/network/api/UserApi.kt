package com.example.mobilefinalproject.network.api

import com.example.mobilefinalproject.network.dto.UserMe
import com.example.mobilefinalproject.network.dto.UserPublic
import com.example.mobilefinalproject.network.dto.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {

    @GET("api/v1/users/me")
    suspend fun getMe(): Response<UserMe>

    @PUT("api/v1/users/me")
    suspend fun updateMe(@Body body: UserUpdateRequest): Response<UserMe>

    @GET("api/v1/users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: Int): Response<UserPublic>
}
