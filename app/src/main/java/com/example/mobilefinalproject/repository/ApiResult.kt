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
