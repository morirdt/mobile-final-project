package com.example.mobilefinalproject.repository

import android.content.Context
import com.example.mobilefinalproject.network.RetrofitClient
import com.example.mobilefinalproject.network.api.UserApi
import com.example.mobilefinalproject.network.dto.UserMe
import com.example.mobilefinalproject.network.dto.UserPublic
import com.example.mobilefinalproject.network.dto.UserUpdateRequest

class UserRepository(context: Context) {

    private val api: UserApi = RetrofitClient.createService(context)

    suspend fun getMe(): ApiResult<UserMe> = safeApiCall { api.getMe() }

    suspend fun updateMe(request: UserUpdateRequest): ApiResult<UserMe> =
        safeApiCall { api.updateMe(request) }

    suspend fun getUser(userId: Int): ApiResult<UserPublic> = safeApiCall { api.getUser(userId) }
}
