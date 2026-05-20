package com.example.mobilefinalproject.repository

import android.content.Context
import com.example.mobilefinalproject.network.RetrofitClient
import com.example.mobilefinalproject.network.api.AiPriceApi
import com.example.mobilefinalproject.network.dto.AiPriceRequest
import com.example.mobilefinalproject.network.dto.AiPriceResponse

class AiPriceRepository(private val context: Context) {

    private val api: AiPriceApi = RetrofitClient.createService(context)

    suspend fun estimatePrice(message: String): ApiResult<AiPriceResponse> =
        safeApiCall { api.estimatePrice(AiPriceRequest(message)) }
}
