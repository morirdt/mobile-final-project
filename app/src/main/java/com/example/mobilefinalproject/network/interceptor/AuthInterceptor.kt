package com.example.mobilefinalproject.network.interceptor

import android.content.Context
import com.example.mobilefinalproject.session.TokenManager
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response

/**
 * Attaches the Bearer access token to every outgoing request.
 * On 401, attempts a single token refresh then retries.
 */
class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getAccessToken(context)
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            response.close()
            // Attempt token refresh inline (synchronous)
            val newToken = refreshTokenSync() ?: return chain.proceed(request)
            val retryRequest = chain.request().newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            return chain.proceed(retryRequest)
        }

        return response
    }

    /**
     * Performs a synchronous refresh call to get a new access token.
     * Returns the new access token on success, null on failure.
     */
    private fun refreshTokenSync(): String? {
        return try {
            val refreshToken = TokenManager.getRefreshToken(context) ?: return null
            val client = okhttp3.OkHttpClient()
            val body = okhttp3.RequestBody.create(
                "application/json".toMediaType(),
                """{"refresh_token":"$refreshToken"}"""
            )
            val req = okhttp3.Request.Builder()
                .url(com.example.mobilefinalproject.BuildConfig.BASE_URL + "api/v1/auth/refresh")
                .post(body)
                .build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val json = resp.body?.string() ?: return null
                val gson = com.google.gson.Gson()
                val tokenResponse = gson.fromJson(json, com.example.mobilefinalproject.network.dto.TokenResponse::class.java)
                TokenManager.saveTokens(context, tokenResponse.accessToken, tokenResponse.refreshToken)
                tokenResponse.accessToken
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
