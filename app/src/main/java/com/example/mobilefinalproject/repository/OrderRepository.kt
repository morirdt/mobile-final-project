package com.example.mobilefinalproject.repository

import android.content.Context
import com.example.mobilefinalproject.network.RetrofitClient
import com.example.mobilefinalproject.network.api.OrderApi
import com.example.mobilefinalproject.network.dto.OrderCancelRequest
import com.example.mobilefinalproject.network.dto.OrderCreateRequest
import com.example.mobilefinalproject.network.dto.OrderRead
import com.example.mobilefinalproject.network.dto.OrderUpdateRequest
import com.example.mobilefinalproject.network.dto.PageResponse
import com.example.mobilefinalproject.network.dto.RatingCreate
import com.example.mobilefinalproject.network.dto.RatingRead
import com.example.mobilefinalproject.network.dto.RatingResponseCreate

class OrderRepository(context: Context) {

    private val api: OrderApi = RetrofitClient.createService(context)

    suspend fun createOrder(request: OrderCreateRequest): ApiResult<OrderRead> =
        safeApiCall { api.createOrder(request) }

    suspend fun listAvailable(limit: Int = 20, offset: Int = 0): ApiResult<PageResponse<OrderRead>> =
        safeApiCall { api.listAvailable(limit, offset) }

    suspend fun listHistory(limit: Int = 20, offset: Int = 0): ApiResult<PageResponse<OrderRead>> =
        safeApiCall { api.listHistory(limit, offset) }

    suspend fun listMyActive(): ApiResult<List<OrderRead>> =
        safeApiCall { api.listMyActive() }

    suspend fun getOrder(orderId: Int): ApiResult<OrderRead> =
        safeApiCall { api.getOrder(orderId) }

    suspend fun updateOrder(orderId: Int, request: OrderUpdateRequest): ApiResult<OrderRead> =
        safeApiCall { api.updateOrder(orderId, request) }

    suspend fun deleteOrder(orderId: Int): ApiResult<Unit> =
        safeApiCall { api.deleteOrder(orderId) }

    suspend fun acceptOrder(orderId: Int): ApiResult<OrderRead> =
        safeApiCall { api.acceptOrder(orderId) }

    suspend fun startOrder(orderId: Int): ApiResult<OrderRead> =
        safeApiCall { api.startOrder(orderId) }

    suspend fun pickupOrder(orderId: Int): ApiResult<OrderRead> =
        safeApiCall { api.pickupOrder(orderId) }

    suspend fun completeOrder(orderId: Int): ApiResult<OrderRead> =
        safeApiCall { api.completeOrder(orderId) }

    suspend fun cancelOrder(orderId: Int, reason: String? = null): ApiResult<OrderRead> =
        safeApiCall { api.cancelOrder(orderId, OrderCancelRequest(reason)) }

    suspend fun submitRating(orderId: Int, score: Int, comment: String?): ApiResult<RatingRead> =
        safeApiCall { api.submitRating(orderId, RatingCreate(score, comment)) }

    suspend fun getOrderRating(orderId: Int): ApiResult<RatingRead> =
        safeApiCall { api.getOrderRating(orderId) }

    suspend fun submitRatingResponse(orderId: Int, response: String): ApiResult<RatingRead> =
        safeApiCall { api.submitRatingResponse(orderId, RatingResponseCreate(response)) }

    suspend fun deleteRatingResponse(orderId: Int): ApiResult<RatingRead> =
        safeApiCall { api.deleteRatingResponse(orderId) }
}
