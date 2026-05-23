package com.example.mobilefinalproject.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobilefinalproject.network.dto.DriverProfile
import com.example.mobilefinalproject.network.dto.UserMe
import com.example.mobilefinalproject.network.dto.UserUpdateRequest
import com.example.mobilefinalproject.repository.ApiResult
import com.example.mobilefinalproject.repository.DriverRepository
import com.example.mobilefinalproject.repository.UserRepository
import com.example.mobilefinalproject.repository.UploadRepository
import android.net.Uri
import com.example.mobilefinalproject.session.UserSessionManager
import kotlinx.coroutines.launch

class DriverViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application)
    private val driverRepo = DriverRepository(application)
    private val uploadRepo = UploadRepository(application)

    private val _driverProfile = MutableLiveData<DriverProfile?>()
    val driverProfile: LiveData<DriverProfile?> = _driverProfile

    private val _userMe = MutableLiveData<UserMe?>()
    val userMe: LiveData<UserMe?> = _userMe

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    // ── Convenience accessors ─────────────────────────────────────────────────
    val driverName: String get() = _userMe.value?.fullName ?: ""
    val driverId: Int get() = _userMe.value?.id ?: 0

    fun loadMe() {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.getMe()) {
                is ApiResult.Success -> _userMe.value = result.data
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun setUserMe(user: UserMe) {
        _userMe.value = user
    }

    fun updateProfile(
        fullName: String? = null,
        phone: String? = null,
        imageUri: Uri? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.updateMe(UserUpdateRequest(fullName = fullName, phone = phone))) {
                is ApiResult.Success -> {
                    _userMe.value = result.data
                    val ctx = getApplication<Application>()
                    UserSessionManager.getSession(ctx)?.let { session ->
                        UserSessionManager.saveSession(ctx, session.copy(fullName = result.data.fullName))
                    }

                    if (imageUri != null) {
                        val hadImageBefore = _userMe.value?.profileImageUrl != null
                        when (val uploadResult = uploadRepo.uploadProfileImage(imageUri, existing = hadImageBefore)) {
                            is ApiResult.Success -> {
                                loadMe()
                                onSuccess?.invoke()
                            }
                            is ApiResult.Error -> _error.value = uploadResult.message
                        }
                    } else {
                        onSuccess?.invoke()
                    }
                }
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    /** Set the driver's availability status on the backend. */
    fun updateDriverStatus(status: String) {
        viewModelScope.launch {
            when (val result = driverRepo.updateStatus(status)) {
                is ApiResult.Success -> _driverProfile.value = result.data
                is ApiResult.Error -> _error.value = result.message
            }
        }
    }

    /** Marks driver as available — call when entering the Finder screen. */
    fun setAvailable() = updateDriverStatus("available")

    /**
     * Fetches the current driver profile and sets status to "available"
     * only if the driver is not currently "busy" (i.e. has no active order).
     */
    fun setAvailableIfNotBusy() {
        viewModelScope.launch {
            when (val result = driverRepo.getMe()) {
                is ApiResult.Success -> {
                    _driverProfile.value = result.data
                    if (result.data.status != "busy") {
                        updateDriverStatus("available")
                    }
                }
                is ApiResult.Error -> {
                    // If we can't fetch the profile, default to setting available
                    updateDriverStatus("available")
                }
            }
        }
    }

    /** Marks driver as offline — call on logout. */
    fun setOffline() = updateDriverStatus("offline")

    /**
     * Sets the driver offline via the API *before* invoking [onDone],
     * but only if the driver is not currently "busy" with an active order.
     * Ensures the auth token is still valid when the request is made.
     */
    fun setOfflineAndLogout(onDone: () -> Unit) {
        viewModelScope.launch {
            val currentStatus = when (val result = driverRepo.getMe()) {
                is ApiResult.Success -> result.data.status
                is ApiResult.Error -> _driverProfile.value?.status
            }
            if (currentStatus != "busy") {
                driverRepo.updateStatus("offline") // best-effort; ignore errors
            }
            onDone()
        }
    }

    fun clearDriver() {
        _userMe.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
