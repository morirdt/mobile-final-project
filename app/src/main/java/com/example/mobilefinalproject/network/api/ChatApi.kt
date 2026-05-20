package com.example.mobilefinalproject.network.api

import com.example.mobilefinalproject.network.dto.ConversationDetail
import com.example.mobilefinalproject.network.dto.ConversationSummary
import com.example.mobilefinalproject.network.dto.SendMessageRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApi {

    @GET("api/v1/chat/conversations")
    suspend fun listConversations(): Response<List<ConversationSummary>>

    @GET("api/v1/chat/conversations/{order_id}")
    suspend fun getConversation(@Path("order_id") orderId: Int): Response<ConversationDetail>

    @POST("api/v1/chat/conversations/{order_id}/messages")
    suspend fun sendMessage(
        @Path("order_id") orderId: Int,
        @Body body: SendMessageRequest
    ): Response<Map<String, Any>>

    @POST("api/v1/chat/conversations/{order_id}/read")
    suspend fun markRead(@Path("order_id") orderId: Int): Response<Map<String, Any>>
}
