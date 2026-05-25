package com.example.mobilefinalproject.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobilefinalproject.db.entity.OrderEntity
import com.example.mobilefinalproject.network.dto.OrderCreateRequest
import com.example.mobilefinalproject.network.dto.OrderRead
import com.example.mobilefinalproject.network.dto.OrderUpdateRequest
import com.example.mobilefinalproject.repository.ApiResult
import com.example.mobilefinalproject.repository.friendlyMessage
import com.example.mobilefinalproject.repository.OrderRepository
import com.example.mobilefinalproject.repository.UploadRepository
import kotlinx.coroutines.launch

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = OrderRepository(application)
    private val uploadRepo = UploadRepository(application)

    // ── Room-backed reactive LiveData (auto-updates when DB changes) ──────

    /** Customer's full order history, served from Room cache. */
    val customerOrders: LiveData<List<OrderRead>> =
        repo.observeCustomerOrders().asLiveData(viewModelScope.coroutineContext)

    /** Driver: active (accepted / in-progress) orders, served from Room cache. */
    val activeOrders: LiveData<List<OrderRead>> =
        repo.observeActiveDriverOrders().asLiveData(viewModelScope.coroutineContext)

    /** Driver: completed orders, served from Room cache. */
    val completedOrders: LiveData<List<OrderRead>> =
        repo.observeCompletedDriverOrders().asLiveData(viewModelScope.coroutineContext)

    /** Driver Finder: pending (unassigned) orders, served from Room cache. */
    val pendingOrders: LiveData<List<OrderRead>> =
        repo.observePendingOrders().asLiveData(viewModelScope.coroutineContext)

    // ── Local-only state (not persisted) ───────────────────────────────────

    private val _selectedOrder = MutableLiveData<OrderRead?>(null)
    val selectedOrder: LiveData<OrderRead?> = _selectedOrder

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun selectOrder(order: OrderRead?) {
        _selectedOrder.value = order
    }

    // ── Customer operations ────────────────────────────────────────────────

    /** Triggers a network refresh; Room cache updates automatically → LiveData notifies UI. */
    fun loadMyOrders() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.refreshCustomerOrders()
            if (result is ApiResult.Error) _error.value = result.friendlyMessage()
            _loading.value = false
        }
    }

    fun createOrder(
        request: OrderCreateRequest,
        imageUri: Uri? = null,
        onSuccess: ((OrderRead) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.createOrder(request)) {
                is ApiResult.Success -> {
                    val createdOrder = result.data
                    if (imageUri != null) {
                        when (val uploadResult = uploadRepo.uploadOrderImage(createdOrder.id, imageUri)) {
                            is ApiResult.Success -> Unit
                            is ApiResult.Error -> _error.value = uploadResult.friendlyMessage()
                        }
                    }
                    // Refresh cache so the new order appears in the list
                    repo.refreshCustomerOrders()
                    onSuccess?.invoke(createdOrder)
                }
                is ApiResult.Error -> _error.value = result.friendlyMessage()
            }
            _loading.value = false
        }
    }

    fun updateOrder(
        orderId: Int,
        request: OrderUpdateRequest,
        imageUri: android.net.Uri? = null,
        hadImageBefore: Boolean = false,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.updateOrder(orderId, request)) {
                is ApiResult.Success -> {
                    if (imageUri != null) {
                        when (val uploadResult = uploadRepo.uploadOrderImage(orderId, imageUri, existing = hadImageBefore)) {
                            is ApiResult.Success -> Unit
                            is ApiResult.Error -> _error.value = uploadResult.friendlyMessage()
                        }
                    }
                    repo.refreshCustomerOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.friendlyMessage()
            }
            _loading.value = false
        }
    }

    fun cancelOrder(orderId: Int, reason: String? = null, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.cancelOrder(orderId, reason)) {
                is ApiResult.Success -> {
                    repo.refreshCustomerOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.friendlyMessage()
            }
            _loading.value = false
        }
    }

    // ── Driver operations ─────────────────────────────────────────────────

    fun loadActiveDriverOrders() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.refreshActiveDriverOrders()
            if (result is ApiResult.Error) _error.value = result.friendlyMessage()
            _loading.value = false
        }
    }

    fun loadCompletedDriverOrders() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.refreshCompletedDriverOrders()
            if (result is ApiResult.Error) _error.value = result.friendlyMessage()
            _loading.value = false
        }
    }

    fun loadPendingOrders() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.refreshPendingOrders()
            if (result is ApiResult.Error) _error.value = result.friendlyMessage()
            _loading.value = false
        }
    }

    fun acceptOrder(orderId: Int, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.acceptOrder(orderId)) {
                is ApiResult.Success -> {
                    repo.refreshPendingOrders()
                    repo.refreshActiveDriverOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.friendlyMessage()
            }
            _loading.value = false
        }
    }

    fun startOrder(orderId: Int, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.startOrder(orderId)) {
                is ApiResult.Success -> {
                    repo.refreshActiveDriverOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.friendlyMessage()
            }
            _loading.value = false
        }
    }

    /**
     * Optimistically marks an order as completed in the local Room cache so the UI
     * updates immediately, then calls the API to persist the change; reverts on failure.
     */
    fun completeOrderOptimistic(order: OrderRead) {
        viewModelScope.launch {
            // Optimistic DB writes — Room notifies LiveData observers instantly
            repo.optimisticRemove(order.id, OrderEntity.LIST_DRIVER_ACTIVE)
            repo.optimisticUpsert(order.copy(status = "completed"), OrderEntity.LIST_DRIVER_COMPLETED)
            repo.optimisticRemove(order.id, OrderEntity.LIST_DRIVER_PENDING)

            _loading.value = true
            when (val result = repo.completeOrder(order.id)) {
                is ApiResult.Success -> {
                    // Sync authoritative state from server
                    repo.refreshActiveDriverOrders()
                    repo.refreshCompletedDriverOrders()
                    repo.refreshPendingOrders()
                }
                is ApiResult.Error -> {
                    // Revert optimistic changes
                    repo.optimisticUpsert(order, OrderEntity.LIST_DRIVER_ACTIVE)
                    repo.optimisticRemove(order.id, OrderEntity.LIST_DRIVER_COMPLETED)
                    _error.value = result.friendlyMessage()
                }
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
