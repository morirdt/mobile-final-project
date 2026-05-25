package com.example.mobilefinalproject.network.api

import com.example.mobilefinalproject.network.dto.AiPriceRequest
import com.example.mobilefinalproject.network.dto.AiPriceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiPriceApi {

    @POST("api/v1/aiprice")
    suspend fun estimatePrice(@Body body: AiPriceRequest): Response<AiPriceResponse>
}
