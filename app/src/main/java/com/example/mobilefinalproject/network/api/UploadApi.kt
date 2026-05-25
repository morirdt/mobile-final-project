package com.example.mobilefinalproject.network.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UploadApi {

    @GET("api/v1/uploads/image/profile")
    suspend fun getMyProfileImage(): Response<Map<String, String?>>

    @Multipart
    @POST("api/v1/uploads/image/profile")
    suspend fun uploadProfileImage(@Part file: MultipartBody.Part): Response<Map<String, String?>>

    @Multipart
    @PUT("api/v1/uploads/image/profile")
    suspend fun replaceProfileImage(@Part file: MultipartBody.Part): Response<Map<String, String?>>

    @DELETE("api/v1/uploads/image/profile")
    suspend fun deleteProfileImage(): Response<ResponseBody>

    @GET("api/v1/uploads/image/profile/{user_id}")
    suspend fun getUserProfileImage(@Path("user_id") userId: Int): Response<Map<String, String?>>

    @Multipart
    @POST("api/v1/uploads/image/order/{order_id}")
    suspend fun uploadOrderImage(
        @Path("order_id") orderId: Int,
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    @Multipart
    @PUT("api/v1/uploads/image/order/{order_id}")
    suspend fun replaceOrderImage(
        @Path("order_id") orderId: Int,
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    @DELETE("api/v1/uploads/image/order/{order_id}")
    suspend fun deleteOrderImage(@Path("order_id") orderId: Int): Response<ResponseBody>

    @GET("api/v1/uploads/image/order/{order_id}")
    suspend fun getOrderImage(@Path("order_id") orderId: Int): Response<Map<String, String?>>
}
