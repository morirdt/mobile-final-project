package com.example.mobilefinalproject.repository

import android.content.Context
import com.example.mobilefinalproject.db.AppDatabase
import com.example.mobilefinalproject.db.messageEntities
import com.example.mobilefinalproject.db.toDto
import com.example.mobilefinalproject.db.toEntity
import com.example.mobilefinalproject.db.toSummaryDto
import com.example.mobilefinalproject.network.RetrofitClient
import com.example.mobilefinalproject.network.api.ChatApi
import com.example.mobilefinalproject.network.dto.ConversationDetail
import com.example.mobilefinalproject.network.dto.ConversationSummary
import com.example.mobilefinalproject.network.dto.SendMessageRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(private val context: Context) {

    private val api: ChatApi = RetrofitClient.createService(context)
    private val convDao = AppDatabase.getInstance(context).conversationDao()
    private val msgDao = AppDatabase.getInstance(context).chatMessageDao()

    // ── Reactive streams ───────────────────────────────────────────────────

    /** Emits all cached conversation summaries ordered by most recently updated. */
    fun observeConversations(): Flow<List<ConversationSummary>> =
        convDao.observeAll().map { list -> list.map { it.toSummaryDto() } }

    /** Emits cached messages for a specific conversation in chronological order. */
    fun observeMessages(conversationId: Int): Flow<List<com.example.mobilefinalproject.network.dto.ChatMessage>> =
        msgDao.observe(conversationId).map { list -> list.map { it.toDto() } }

    // ── Network operations (always write result to Room cache) ─────────────

    suspend fun listConversations(): ApiResult<List<ConversationSummary>> {
        val result = safeApiCall { api.listConversations() }
        if (result is ApiResult.Success) {
            convDao.upsertAll(result.data.map { it.toEntity() })
        }
        return result
    }

    suspend fun getConversation(conversationId: Int): ApiResult<ConversationDetail> {
        val result = safeApiCall { api.getConversation(conversationId) }
        if (result is ApiResult.Success) {
            msgDao.replaceConversationMessages(
                conversationId,
                result.data.messageEntities()
            )
        }
        return result
    }

    suspend fun sendMessage(conversationId: Int, body: String): ApiResult<Unit> =
        safeApiCall { api.sendMessage(conversationId, SendMessageRequest(body)) }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error   -> result
            }
        }

    suspend fun markRead(conversationId: Int): ApiResult<Unit> {
        val result = safeApiCall { api.markRead(conversationId) }
        if (result is ApiResult.Success) {
            // Update the local unread counter immediately so the UI doesn't flicker
            convDao.markRead(conversationId)
        }
        return result.let {
            when (it) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error   -> it
            }
        }
    }
}
