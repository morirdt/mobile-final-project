package com.example.mobilefinalproject.repository

import android.content.Context
import com.example.mobilefinalproject.network.RetrofitClient
import com.example.mobilefinalproject.network.api.AuthApi
import com.example.mobilefinalproject.network.dto.CustomerRegisterRequest
import com.example.mobilefinalproject.network.dto.DriverRegisterRequest
import com.example.mobilefinalproject.network.dto.GoogleIdTokenRequest
import com.example.mobilefinalproject.network.dto.LoginRequest
import com.example.mobilefinalproject.network.dto.RefreshRequest
import com.example.mobilefinalproject.network.dto.TokenResponse
import com.example.mobilefinalproject.network.dto.UserMe
import com.example.mobilefinalproject.session.TokenManager
import com.example.mobilefinalproject.session.UserSessionManager

class AuthRepository(private val context: Context) {

    private val api: AuthApi = RetrofitClient.createService(context)

    suspend fun loginJson(email: String, password: String, role: String): ApiResult<TokenResponse> {
        return safeApiCall { api.loginJson(LoginRequest(email, password, role)) }.also { result ->
            if (result is ApiResult.Success) {
                TokenManager.saveTokens(context, result.data.accessToken, result.data.refreshToken)
            }
        }
    }

    suspend fun registerCustomer(
        email: String, password: String, fullName: String, phone: String?
    ): ApiResult<UserMe> = safeApiCall {
        api.registerCustomer(CustomerRegisterRequest(email, password, fullName, phone))
    }

    suspend fun registerDriver(
        email: String, password: String, fullName: String, phone: String?,
        licenseNumber: String, vehicleType: String, vehiclePlate: String,
        vehicleCapacityKg: Double?
    ): ApiResult<UserMe> = safeApiCall {
        api.registerDriver(
            DriverRegisterRequest(email, password, fullName, phone,
                licenseNumber, vehicleType, vehiclePlate, vehicleCapacityKg)
        )
    }

    suspend fun refresh(): ApiResult<TokenResponse> {
        val token = TokenManager.getRefreshToken(context) ?: return ApiResult.Error("No refresh token")
        return safeApiCall { api.refresh(RefreshRequest(token)) }.also { result ->
            if (result is ApiResult.Success) {
                TokenManager.saveTokens(context, result.data.accessToken, result.data.refreshToken)
            }
        }
    }

    suspend fun googleLogin(idToken: String, role: String): ApiResult<TokenResponse> =
        safeApiCall { api.googleLogin(GoogleIdTokenRequest(idToken, role)) }.also { result ->
            if (result is ApiResult.Success) {
                TokenManager.saveTokens(context, result.data.accessToken, result.data.refreshToken)
            }
        }

    fun logout() {
        TokenManager.clearTokens(context)
        UserSessionManager.clearSession(context)
    }
}
