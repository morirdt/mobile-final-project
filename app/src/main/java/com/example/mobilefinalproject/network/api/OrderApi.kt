package com.example.mobilefinalproject.network.api

import com.example.mobilefinalproject.network.dto.OrderCancelRequest
import com.example.mobilefinalproject.network.dto.OrderCreateRequest
import com.example.mobilefinalproject.network.dto.OrderRead
import com.example.mobilefinalproject.network.dto.OrderUpdateRequest
import com.example.mobilefinalproject.network.dto.PageResponse
import com.example.mobilefinalproject.network.dto.RatingCreate
import com.example.mobilefinalproject.network.dto.RatingRead
import com.example.mobilefinalproject.network.dto.RatingResponseCreate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {

    @POST("api/v1/orders")
    suspend fun createOrder(@Body body: OrderCreateRequest): Response<OrderRead>

    @GET("api/v1/orders/available")
    suspend fun listAvailable(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<PageResponse<OrderRead>>

    @GET("api/v1/orders/history")
    suspend fun listHistory(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<PageResponse<OrderRead>>

    @GET("api/v1/orders/me/active")
    suspend fun listMyActive(): Response<List<OrderRead>>

    @GET("api/v1/orders/{order_id}")
    suspend fun getOrder(@Path("order_id") orderId: Int): Response<OrderRead>

    @PATCH("api/v1/orders/{order_id}")
    suspend fun updateOrder(
        @Path("order_id") orderId: Int,
        @Body body: OrderUpdateRequest
    ): Response<OrderRead>

    @DELETE("api/v1/orders/{order_id}")
    suspend fun deleteOrder(@Path("order_id") orderId: Int): Response<Unit>

    @POST("api/v1/orders/{order_id}/accept")
    suspend fun acceptOrder(@Path("order_id") orderId: Int): Response<OrderRead>

    @POST("api/v1/orders/{order_id}/start")
    suspend fun startOrder(@Path("order_id") orderId: Int): Response<OrderRead>

    @POST("api/v1/orders/{order_id}/pickup")
    suspend fun pickupOrder(@Path("order_id") orderId: Int): Response<OrderRead>

    @POST("api/v1/orders/{order_id}/complete")
    suspend fun completeOrder(@Path("order_id") orderId: Int): Response<OrderRead>

    @POST("api/v1/orders/{order_id}/cancel")
    suspend fun cancelOrder(
        @Path("order_id") orderId: Int,
        @Body body: OrderCancelRequest
    ): Response<OrderRead>

    @POST("api/v1/orders/{order_id}/rating")
    suspend fun submitRating(
        @Path("order_id") orderId: Int,
        @Body body: RatingCreate
    ): Response<RatingRead>

    @GET("api/v1/orders/{order_id}/rating")
    suspend fun getOrderRating(@Path("order_id") orderId: Int): Response<RatingRead>

    @POST("api/v1/orders/{order_id}/rating/response")
    suspend fun submitRatingResponse(
        @Path("order_id") orderId: Int,
        @Body body: RatingResponseCreate
    ): Response<RatingRead>

    @DELETE("api/v1/orders/{order_id}/rating/response")
    suspend fun deleteRatingResponse(@Path("order_id") orderId: Int): Response<RatingRead>
}
