package com.example.mobilefinalproject.repository

import android.content.Context
import com.example.mobilefinalproject.db.AppDatabase
import com.example.mobilefinalproject.db.entity.OrderEntity
import com.example.mobilefinalproject.db.toDto
import com.example.mobilefinalproject.db.toEntity
import com.example.mobilefinalproject.network.RetrofitClient
import com.example.mobilefinalproject.network.api.OrderApi
import com.example.mobilefinalproject.network.dto.OrderCancelRequest
import com.example.mobilefinalproject.network.dto.OrderCreateRequest
import com.example.mobilefinalproject.network.dto.OrderRead
import com.example.mobilefinalproject.network.dto.OrderUpdateRequest
import com.example.mobilefinalproject.network.dto.PageResponse
import com.example.mobilefinalproject.network.dto.RatingCreate
import com.example.mobilefinalproject.network.dto.RatingRead
import com.example.mobilefinalproject.network.dto.RatingResponseCreate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepository(private val context: Context) {

    private val api: OrderApi = RetrofitClient.createService(context)
    private val dao = AppDatabase.getInstance(context).orderDao()

    // ── Reactive cache streams (Room → Flow → DTO) ────────────────────────

    /** Emits the customer's cached order history from Room — always up to date after a refresh. */
    fun observeCustomerOrders(): Flow<List<OrderRead>> =
        dao.observeByListType(OrderEntity.LIST_CUSTOMER_HISTORY)
            .map { list -> list.map { it.toDto() } }

    /** Emits cached pending (unassigned) orders for the Driver Finder screen. */
    fun observePendingOrders(): Flow<List<OrderRead>> =
        dao.observeByListType(OrderEntity.LIST_DRIVER_PENDING)
            .map { list -> list.map { it.toDto() } }

    /** Emits the driver's cached active (accepted/in-progress) orders. */
    fun observeActiveDriverOrders(): Flow<List<OrderRead>> =
        dao.observeByListType(OrderEntity.LIST_DRIVER_ACTIVE)
            .map { list -> list.map { it.toDto() } }

    /** Emits the driver's cached completed orders. */
    fun observeCompletedDriverOrders(): Flow<List<OrderRead>> =
        dao.observeByListType(OrderEntity.LIST_DRIVER_COMPLETED)
            .map { list -> list.map { it.toDto() } }

    // ── Network sync (fetch → write to Room) ──────────────────────────────

    /** Fetches customer order history from the server and updates the Room cache. */
    suspend fun refreshCustomerOrders(): ApiResult<Unit> {
        val result = safeApiCall { api.listHistory() }
        if (result is ApiResult.Success) {
            dao.replaceListType(
                OrderEntity.LIST_CUSTOMER_HISTORY,
                result.data.items.map { it.toEntity(OrderEntity.LIST_CUSTOMER_HISTORY) }
            )
        }
        return result.toUnit()
    }

    /** Fetches available orders from the server and updates the Room cache. */
    suspend fun refreshPendingOrders(): ApiResult<Unit> {
        val result = safeApiCall { api.listAvailable() }
        if (result is ApiResult.Success) {
            val pending = result.data.items.filter { it.status.lowercase() == "pending" }
            dao.replaceListType(
                OrderEntity.LIST_DRIVER_PENDING,
                pending.map { it.toEntity(OrderEntity.LIST_DRIVER_PENDING) }
            )
        }
        return result.toUnit()
    }

    /** Fetches driver active orders from the server and updates the Room cache. */
    suspend fun refreshActiveDriverOrders(): ApiResult<Unit> {
        val result = safeApiCall { api.listMyActive() }
        if (result is ApiResult.Success) {
            val active = result.data.filter {
                it.status.lowercase() in setOf("accepted", "in_progress")
            }
            dao.replaceListType(
                OrderEntity.LIST_DRIVER_ACTIVE,
                active.map { it.toEntity(OrderEntity.LIST_DRIVER_ACTIVE) }
            )
        }
        return result.toUnit()
    }

    /** Fetches driver completed orders from the server and updates the Room cache. */
    suspend fun refreshCompletedDriverOrders(): ApiResult<Unit> {
        val result = safeApiCall { api.listHistory() }
        if (result is ApiResult.Success) {
            val completed = result.data.items.filter { it.status.lowercase() == "completed" }
            dao.replaceListType(
                OrderEntity.LIST_DRIVER_COMPLETED,
                completed.map { it.toEntity(OrderEntity.LIST_DRIVER_COMPLETED) }
            )
        }
        return result.toUnit()
    }

    // ── Optimistic local mutations ─────────────────────────────────────────

    /** Writes a single order to the given list-type bucket without hitting the network. */
    suspend fun optimisticUpsert(order: OrderRead, listType: String) {
        dao.upsertAll(listOf(order.toEntity(listType)))
    }

    /** Removes a single order from the given list-type bucket locally. */
    suspend fun optimisticRemove(orderId: Int, listType: String) {
        val current = dao.getByListType(listType)
        dao.replaceListType(listType, current.filter { it.orderId != orderId })
    }

    // ── Existing network-only operations (unchanged) ──────────────────────

    suspend fun createOrder(request: OrderCreateRequest): ApiResult<OrderRead> =
        safeApiCall { api.createOrder(request) }

    suspend fun listAvailable(limit: Int = 20, offset: Int = 0): ApiResult<PageResponse<OrderRead>> =
        safeApiCall { api.listAvailable(limit, offset) }

    suspend fun listHistory(limit: Int = 20, offset: Int = 0): ApiResult<PageResponse<OrderRead>> =
        safeApiCall { api.listHistory(limit, offset) }

    suspend fun listMyActive(): ApiResult<List<OrderRead>> =
        safeApiCall { api.listMyActive() }

    suspend fun getOrder(orderId: Int): ApiResult<OrderRead> =
        safeApiCall { api.getOrder(orderId) }

    suspend fun updateOrder(orderId: Int, request: OrderUpdateRequest): ApiResult<OrderRead> =
        safeApiCall { api.updateOrder(orderId, request) }

    suspend fun deleteOrder(orderId: Int): ApiResult<Unit> =
        safeApiCall { api.deleteOrder(orderId) }

    suspend fun acceptOrder(orderId: Int): ApiResult<OrderRead> =
        safeApiCall { api.acceptOrder(orderId) }

    suspend fun startOrder(orderId: Int): ApiResult<OrderRead> =
        safeApiCall { api.startOrder(orderId) }

    suspend fun pickupOrder(orderId: Int): ApiResult<OrderRead> =
        safeApiCall { api.pickupOrder(orderId) }

    suspend fun completeOrder(orderId: Int): ApiResult<OrderRead> {
        safeApiCall { api.pickupOrder(orderId) }
        return safeApiCall { api.completeOrder(orderId) }
    }

    suspend fun cancelOrder(orderId: Int, reason: String? = null): ApiResult<OrderRead> =
        safeApiCall { api.cancelOrder(orderId, OrderCancelRequest(reason)) }

    suspend fun submitRating(orderId: Int, score: Int, comment: String?): ApiResult<RatingRead> =
        safeApiCall { api.submitRating(orderId, RatingCreate(score, comment)) }

    suspend fun getOrderRating(orderId: Int): ApiResult<RatingRead> =
        safeApiCall { api.getOrderRating(orderId) }

    suspend fun submitRatingResponse(orderId: Int, response: String): ApiResult<RatingRead> =
        safeApiCall { api.submitRatingResponse(orderId, RatingResponseCreate(response)) }

    suspend fun deleteRatingResponse(orderId: Int): ApiResult<RatingRead> =
        safeApiCall { api.deleteRatingResponse(orderId) }
}

// ── Private helpers ────────────────────────────────────────────────────────

private fun <T> ApiResult<T>.toUnit(): ApiResult<Unit> = when (this) {
    is ApiResult.Success -> ApiResult.Success(Unit)
    is ApiResult.Error   -> this
}

