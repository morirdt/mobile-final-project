package com.example.mobilefinalproject.repository

import android.content.Context
import android.net.Uri
import com.example.mobilefinalproject.network.RetrofitClient
import com.example.mobilefinalproject.network.api.UploadApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UploadRepository(private val context: Context) {

    private val api: UploadApi = RetrofitClient.createService(context)

    /** Returns the profile image URL for the current user, or null if none. */
    suspend fun getMyProfileImageUrl(): ApiResult<String?> {
        return safeApiCall { api.getMyProfileImage() }.mapSuccess { it["url"] }
    }

    /**
     * Uploads or replaces the profile image.
     * Pass [existing] = true to use PUT instead of POST (image already uploaded before).
     */
    suspend fun uploadProfileImage(uri: Uri, existing: Boolean = false): ApiResult<String?> {
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            ?: return ApiResult.Error("Cannot read image")
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "profile.jpg", body)
        return if (existing) {
            safeApiCall { api.replaceProfileImage(part) }.mapSuccess { it["url"] }
        } else {
            safeApiCall { api.uploadProfileImage(part) }.mapSuccess { it["url"] }
        }
    }

    suspend fun deleteProfileImage(): ApiResult<Unit> =
        safeApiCall { api.deleteProfileImage() }.mapSuccess { Unit }

    suspend fun getUserProfileImageUrl(userId: Int): ApiResult<String?> =
        safeApiCall { api.getUserProfileImage(userId) }.mapSuccess { it["url"] }

    /** Upload (POST) or replace (PUT) an order cargo image. */
    suspend fun uploadOrderImage(orderId: Int, uri: Uri, existing: Boolean = false): ApiResult<String> {
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            ?: return ApiResult.Error("Cannot read image")
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "cargo.jpg", body)
        return if (existing) {
            safeApiCall { api.replaceOrderImage(orderId, part) }.mapSuccess { it["url"] ?: "" }
        } else {
            safeApiCall { api.uploadOrderImage(orderId, part) }.mapSuccess { it["url"] ?: "" }
        }
    }

    suspend fun deleteOrderImage(orderId: Int): ApiResult<Unit> =
        safeApiCall { api.deleteOrderImage(orderId) }.mapSuccess { Unit }

    suspend fun getOrderImageUrl(orderId: Int): ApiResult<String?> =
        safeApiCall { api.getOrderImage(orderId) }.mapSuccess { it["url"] }
}

/** Convenience: transform a successful value inside ApiResult without re-wrapping errors. */
private fun <T, R> ApiResult<T>.mapSuccess(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Error -> ApiResult.Error(message, code)
}
