package com.example.mobilefinalproject.repository

import android.content.Context
import com.example.mobilefinalproject.network.RetrofitClient
import com.example.mobilefinalproject.network.api.ChatApi
import com.example.mobilefinalproject.network.dto.ConversationDetail
import com.example.mobilefinalproject.network.dto.ConversationSummary
import com.example.mobilefinalproject.network.dto.SendMessageRequest

class ChatRepository(private val context: Context) {

    private val api: ChatApi = RetrofitClient.createService(context)

    suspend fun listConversations(): ApiResult<List<ConversationSummary>> =
        safeApiCall { api.listConversations() }

    suspend fun getConversation(conversationId: Int): ApiResult<ConversationDetail> =
        safeApiCall { api.getConversation(conversationId) }

    suspend fun sendMessage(conversationId: Int, body: String): ApiResult<Unit> =
        safeApiCall { api.sendMessage(conversationId, SendMessageRequest(body)) }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> result
            }
        }

    suspend fun markRead(conversationId: Int): ApiResult<Unit> =
        safeApiCall { api.markRead(conversationId) }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> result
            }
        }
}
