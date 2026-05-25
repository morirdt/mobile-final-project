package com.example.mobilefinalproject.repository

/**
 * Simple result wrapper for repository calls.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
}

/**
 * Maps an [ApiResult.Error] to a user-friendly, human-readable message.
 * HTTP status codes are translated to clear explanations; network/unknown errors
 * are also normalised to avoid exposing raw technical strings to the user.
 */
fun ApiResult.Error.friendlyMessage(): String = when (code) {
    400 -> "Invalid request. Please check your input and try again."
    401 -> "invalid email or password. Please try again."
    403 -> "You don't have permission to perform this action."
    404 -> "The requested resource was not found."
    408, 504 -> "The request timed out. Please check your connection and try again."
    409 -> "A similar entity already exists!"
    422 -> if (!message.startsWith("Error ")) message else "Validation error. Please check your input."
    429 -> "Too many requests. Please wait a moment and try again."
    500, 502, 503 -> "A server error occurred. Please try again later."
    null -> when {
        message.contains("timeout", ignoreCase = true) ||
            message.contains("timed out", ignoreCase = true) ->
            "Connection timed out. Please check your internet connection."
        message.contains("UnknownHostException", ignoreCase = true) ||
            message.contains("unable to resolve", ignoreCase = true) ->
            "No internet connection. Please check your network and try again."
        message.contains("connect", ignoreCase = true) &&
            message.contains("failed", ignoreCase = true) ->
            "Failed to connect to the server. Please check your internet connection."
        message.startsWith("Error ") ->
            "An unexpected error occurred. Please try again."
        else -> message
    }
    else -> if (!message.startsWith("Error ")) message
            else "An unexpected error occurred. Please try again."
}

/**
 * Safely executes a suspend block and wraps the outcome in ApiResult.
 */
suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Empty response body", response.code())
            }
        } else {
            val errorMsg = try {
                val errorJson = response.errorBody()?.string()
                val gson = com.google.gson.Gson()
                val errorObj = gson.fromJson(errorJson, com.google.gson.JsonObject::class.java)
                val detail = errorObj?.get("detail")
                when {
                    detail == null -> "Error ${response.code()}"
                    detail.isJsonArray -> {
                        detail.asJsonArray.firstOrNull()?.asJsonObject?.get("msg")?.asString
                            ?: "Validation error"
                    }
                    detail.isJsonPrimitive -> detail.asString
                    else -> "Error ${response.code()}"
                }
            } catch (_: Exception) {
                "Error ${response.code()}"
            }
            ApiResult.Error(errorMsg, response.code())
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }
}
