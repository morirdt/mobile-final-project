package com.example.mobilefinalproject.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobilefinalproject.network.dto.OrderCreateRequest
import com.example.mobilefinalproject.network.dto.OrderRead
import com.example.mobilefinalproject.network.dto.OrderUpdateRequest
import com.example.mobilefinalproject.repository.ApiResult
import com.example.mobilefinalproject.repository.OrderRepository
import com.example.mobilefinalproject.repository.UploadRepository
import kotlinx.coroutines.launch

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = OrderRepository(application)
    private val uploadRepo = UploadRepository(application)

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

    private fun List<OrderRead>.filterByStatus(vararg allowedStatuses: String): List<OrderRead> {
        val allowed = allowedStatuses.map { it.lowercase() }.toSet()
        return filter { it.status.lowercase() in allowed }
    }

    private fun isOrderAlreadyTracked(list: List<OrderRead>, orderId: Int): Boolean =
        list.any { it.id == orderId }

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
                            is ApiResult.Error -> _error.value = uploadResult.message
                        }
                    }

                    loadMyOrders()
                    onSuccess?.invoke(createdOrder)
                }
                is ApiResult.Error -> _error.value = result.message
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
                    // If a new image was selected, upload it (POST/PUT depending on previous state)
                    if (imageUri != null) {
                        when (val uploadResult = uploadRepo.uploadOrderImage(orderId, imageUri, existing = hadImageBefore)) {
                            is ApiResult.Success -> Unit
                            is ApiResult.Error -> _error.value = uploadResult.message
                        }
                    }

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
                is ApiResult.Success -> _activeOrders.value = result.data.filterByStatus(
                    "accepted",
                    "in_progress"
                )
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun loadCompletedDriverOrders() {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.listHistory()) {
                is ApiResult.Success -> _completedOrders.value = result.data.items.filterByStatus(
                    "completed"
                )
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
                is ApiResult.Success -> _pendingOrders.value = result.data.items.filterByStatus(
                    "pending"
                )
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

    @Suppress("unused")
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

    @Suppress("unused")
    fun completeOrder(orderId: Int, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.completeOrder(orderId)) {
                is ApiResult.Success -> {
                    loadActiveDriverOrders()
                    loadCompletedDriverOrders()
                    loadPendingOrders()
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    /**
     * Optimistically mark an order as completed in the local lists so the UI updates immediately.
     * Then call the API to persist the change; on failure we revert the optimistic update and
     * surface an error message.
     */
    fun completeOrderOptimistic(order: OrderRead) {
        // Keep snapshots so we can restore them if the API call fails.
        val previousActive = _activeOrders.value.orEmpty()
        val previousCompleted = _completedOrders.value.orEmpty()
        val previousPending = _pendingOrders.value.orEmpty()

        // Remove from active list immediately and add to completed list
        val currentActive = previousActive.toMutableList()
        currentActive.removeAll { it.id == order.id }
        _activeOrders.value = currentActive.filterByStatus("accepted", "in_progress")

        val completed = previousCompleted.toMutableList()
        if (!isOrderAlreadyTracked(completed, order.id)) {
            // Insert a copy with status set to completed so UI shows it correctly
            completed.add(0, order.copy(status = "completed"))
        }
        _completedOrders.value = completed.filterByStatus("completed")

        val pending = previousPending.toMutableList()
        pending.removeAll { it.id == order.id }
        _pendingOrders.value = pending.filterByStatus("pending")

        // Persist on background; revert if API call fails
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.completeOrder(order.id)) {
                is ApiResult.Success -> {
                    // Refresh from server to ensure canonical state
                    loadActiveDriverOrders()
                    loadCompletedDriverOrders()
                    loadPendingOrders()
                }
                is ApiResult.Error -> {
                    // Revert optimistic update
                    _activeOrders.value = previousActive.filterByStatus("accepted", "in_progress")
                    _completedOrders.value = previousCompleted.filterByStatus("completed")
                    _pendingOrders.value = previousPending.filterByStatus("pending")
                    _error.value = result.message
                }
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
