package com.example.mobilefinalproject.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobilefinalproject.network.dto.OrderCancelRequest
import com.example.mobilefinalproject.network.dto.OrderCreateRequest
import com.example.mobilefinalproject.network.dto.OrderRead
import com.example.mobilefinalproject.network.dto.OrderUpdateRequest
import com.example.mobilefinalproject.repository.ApiResult
import com.example.mobilefinalproject.repository.OrderRepository
import kotlinx.coroutines.launch

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = OrderRepository(application)

    // ── Customer: my orders ──────────────────────────────────────────────────
    private val _customerOrders = MutableLiveData<List<OrderRead>>(emptyList())
    val customerOrders: LiveData<List<OrderRead>> = _customerOrders

    // ── Driver: active orders assigned to me ────────────────────────────────
    private val _activeOrders = MutableLiveData<List<OrderRead>>(emptyList())
    val activeOrders: LiveData<List<OrderRead>> = _activeOrders

    // ── Driver: completed orders ─────────────────────────────────────────────
    private val _completedOrders = MutableLiveData<List<OrderRead>>(emptyList())
    val completedOrders: LiveData<List<OrderRead>> = _completedOrders

    // ── Driver Finder: pending (unassigned) orders ───────────────────────────
    private val _pendingOrders = MutableLiveData<List<OrderRead>>(emptyList())
    val pendingOrders: LiveData<List<OrderRead>> = _pendingOrders

    // ── Selected order (for map marker / detail dialog) ──────────────────────
    private val _selectedOrder = MutableLiveData<OrderRead?>(null)
    val selectedOrder: LiveData<OrderRead?> = _selectedOrder

    // ── Error / toast messages ────────────────────────────────────────────────
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // ── Loading ───────────────────────────────────────────────────────────────
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun selectOrder(order: OrderRead?) {
        _selectedOrder.value = order
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Customer operations
    // ─────────────────────────────────────────────────────────────────────────

    /** Load the customer's own order history (all statuses). */
    fun loadMyOrders() {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.listHistory()) {
                is ApiResult.Success -> _customerOrders.value = result.data.items
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun createOrder(request: OrderCreateRequest, onSuccess: ((OrderRead) -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.createOrder(request)) {
                is ApiResult.Success -> {
                    loadMyOrders()
                    onSuccess?.invoke(result.data)
                }
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun updateOrder(orderId: Int, request: OrderUpdateRequest, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.updateOrder(orderId, request)) {
                is ApiResult.Success -> {
                    loadMyOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun cancelOrder(orderId: Int, reason: String? = null, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.cancelOrder(orderId, reason)) {
                is ApiResult.Success -> {
                    loadMyOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Driver operations
    // ─────────────────────────────────────────────────────────────────────────

    /** Load all orders currently assigned to the driver (accepted/in-progress/picked-up). */
    fun loadActiveDriverOrders() {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.listMyActive()) {
                is ApiResult.Success -> _activeOrders.value = result.data
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun loadCompletedDriverOrders() {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.listHistory()) {
                is ApiResult.Success -> _completedOrders.value = result.data.items
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    /** Load pending (unassigned) orders visible to the driver for the Finder screen. */
    fun loadPendingOrders() {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.listAvailable()) {
                is ApiResult.Success -> _pendingOrders.value = result.data.items
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun acceptOrder(orderId: Int, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.acceptOrder(orderId)) {
                is ApiResult.Success -> {
                    loadPendingOrders()
                    loadActiveDriverOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun startOrder(orderId: Int, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.startOrder(orderId)) {
                is ApiResult.Success -> {
                    loadActiveDriverOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun pickupOrder(orderId: Int, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.pickupOrder(orderId)) {
                is ApiResult.Success -> {
                    loadActiveDriverOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun completeOrder(orderId: Int, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.completeOrder(orderId)) {
                is ApiResult.Success -> {
                    loadActiveDriverOrders()
                    loadCompletedDriverOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
