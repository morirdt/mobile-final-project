package com.example.mobilefinalproject.repository

import android.content.Context
import com.example.mobilefinalproject.db.AppDatabase
import com.example.mobilefinalproject.db.toDto
import com.example.mobilefinalproject.db.toEntity
import com.example.mobilefinalproject.network.RetrofitClient
import com.example.mobilefinalproject.network.api.UserApi
import com.example.mobilefinalproject.network.dto.UserMe
import com.example.mobilefinalproject.network.dto.UserPublic
import com.example.mobilefinalproject.network.dto.UserUpdateRequest
import com.example.mobilefinalproject.session.UserSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val context: Context) {

    private val api: UserApi = RetrofitClient.createService(context)
    private val dao = AppDatabase.getInstance(context).userDao()

    // ── Reactive stream ────────────────────────────────────────────────────

    /**
     * Returns a [Flow] that emits the currently cached [UserMe] from Room.
     * It resolves the user's id from the active session; if no session exists it
     * falls back to the most recently cached row.
     */
    fun observeMe(): Flow<UserMe?> {
        val userId = UserSessionManager.getSession(context)?.userId?.toIntOrNull()
        return if (userId != null) {
            dao.observe(userId).map { it?.toDto() }
        } else {
            dao.observeLatest().map { it?.toDto() }
        }
    }

    // ── Network operations (always write result to Room cache) ─────────────

    suspend fun getMe(): ApiResult<UserMe> {
        val result = safeApiCall { api.getMe() }
        if (result is ApiResult.Success) {
            dao.upsert(result.data.toEntity())
        }
        return result
    }

    suspend fun updateMe(request: UserUpdateRequest): ApiResult<UserMe> {
        val result = safeApiCall { api.updateMe(request) }
        if (result is ApiResult.Success) {
            dao.upsert(result.data.toEntity())
        }
        return result
    }

    /** Writes a [UserMe] directly to the Room cache without hitting the network. */
    suspend fun cacheUser(user: UserMe) {
        dao.upsert(user.toEntity())
    }

    suspend fun getUser(userId: Int): ApiResult<UserPublic> =
        safeApiCall { api.getUser(userId) }

    // ── Cache management ──────────────────────────────────────────────────

    /** Clears the cached user data on logout. */
    suspend fun clearCache() = dao.deleteAll()
}
