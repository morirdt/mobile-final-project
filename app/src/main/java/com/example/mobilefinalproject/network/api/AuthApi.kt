package com.example.mobilefinalproject.network.api

import com.example.mobilefinalproject.network.dto.CustomerRegisterRequest
import com.example.mobilefinalproject.network.dto.DriverRegisterRequest
import com.example.mobilefinalproject.network.dto.GoogleIdTokenRequest
import com.example.mobilefinalproject.network.dto.LoginRequest
import com.example.mobilefinalproject.network.dto.RefreshRequest
import com.example.mobilefinalproject.network.dto.TokenResponse
import com.example.mobilefinalproject.network.dto.UserMe
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/register/customer")
    suspend fun registerCustomer(@Body body: CustomerRegisterRequest): Response<UserMe>

    @POST("api/v1/auth/register/driver")
    suspend fun registerDriver(@Body body: DriverRegisterRequest): Response<UserMe>

    @POST("api/v1/auth/login/json")
    suspend fun loginJson(@Body body: LoginRequest): Response<TokenResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): Response<TokenResponse>

    @POST("api/v1/auth/google")
    suspend fun googleLogin(@Body body: GoogleIdTokenRequest): Response<TokenResponse>
}
