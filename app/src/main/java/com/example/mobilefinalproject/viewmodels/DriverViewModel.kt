package com.example.mobilefinalproject.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobilefinalproject.network.dto.DriverProfile
import com.example.mobilefinalproject.network.dto.UserMe
import com.example.mobilefinalproject.network.dto.UserUpdateRequest
import com.example.mobilefinalproject.repository.ApiResult
import com.example.mobilefinalproject.repository.DriverRepository
import com.example.mobilefinalproject.repository.UploadRepository
import com.example.mobilefinalproject.repository.UserRepository
import com.example.mobilefinalproject.repository.friendlyMessage
import com.example.mobilefinalproject.session.UserSessionManager
import kotlinx.coroutines.launch

class DriverViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application)
    private val driverRepo = DriverRepository(application)
    private val uploadRepo = UploadRepository(application)

    // ── Room-backed reactive LiveData ─────────────────────────────────────

    private val _driverProfile = MutableLiveData<DriverProfile?>(null)
    /** The last-fetched [DriverProfile] from the network. */
    val driverProfile: LiveData<DriverProfile?> = _driverProfile

    /** Emits the cached [UserMe] from Room (common user fields like name / photo). */
    val userMe: LiveData<UserMe?> =
        repo.observeMe().asLiveData(viewModelScope.coroutineContext)

    // ── Local-only state ──────────────────────────────────────────────────

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    val driverName: String get() = userMe.value?.fullName ?: ""
    val driverId: Int get() = userMe.value?.id ?: 0

    // ── Operations ────────────────────────────────────────────────────────

    /** Fetches user profile from the network and updates the Room cache. */
    fun loadMe() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.getMe()
            if (result is ApiResult.Error) _error.value = result.friendlyMessage()
            val driverResult = driverRepo.getMe()
            if (driverResult is ApiResult.Success) _driverProfile.value = driverResult.data
            _loading.value = false
        }
    }

    fun setUserMe(user: UserMe) {
        viewModelScope.launch { repo.cacheUser(user) }
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
                    val ctx = getApplication<Application>()
                    UserSessionManager.getSession(ctx)?.let { session ->
                        UserSessionManager.saveSession(ctx, session.copy(fullName = result.data.fullName))
                    }
                    if (imageUri != null) {
                        val hadImageBefore = result.data.profileImageUrl != null
                        when (val uploadResult = uploadRepo.uploadProfileImage(imageUri, existing = hadImageBefore)) {
                            is ApiResult.Success -> {
                                repo.getMe()
                                onSuccess?.invoke()
                            }
                            is ApiResult.Error -> _error.value = uploadResult.friendlyMessage()
                        }
                    } else {
                        onSuccess?.invoke()
                    }
                }
                is ApiResult.Error -> _error.value = result.friendlyMessage()
            }
            _loading.value = false
        }
    }

    fun updateDriverStatus(status: String) {
        viewModelScope.launch {
            when (val result = driverRepo.updateStatus(status)) {
                is ApiResult.Success -> _driverProfile.value = result.data
                is ApiResult.Error -> _error.value = result.friendlyMessage()
            }
        }
    }

    fun setAvailable() = updateDriverStatus("available")

    fun setAvailableIfNotBusy() {
        viewModelScope.launch {
            when (val result = driverRepo.getMe()) {
                is ApiResult.Success -> {
                    if (result.data.status != "busy") updateDriverStatus("available")
                }
                is ApiResult.Error -> updateDriverStatus("available")
            }
        }
    }

    fun setOffline() = updateDriverStatus("offline")

    fun setOfflineAndLogout(onDone: () -> Unit) {
        viewModelScope.launch {
            val currentStatus = when (val result = driverRepo.getMe()) {
                is ApiResult.Success -> result.data.status
                is ApiResult.Error   -> driverProfile.value?.status
            }
            if (currentStatus != "busy") driverRepo.updateStatus("offline")
            onDone()
        }
    }

    fun clearDriver() {
        viewModelScope.launch {
            repo.clearCache()
            _driverProfile.postValue(null)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
