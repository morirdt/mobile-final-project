package com.example.mobilefinalproject.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobilefinalproject.network.dto.UserMe
import com.example.mobilefinalproject.network.dto.UserUpdateRequest
import com.example.mobilefinalproject.repository.ApiResult
import com.example.mobilefinalproject.repository.UploadRepository
import com.example.mobilefinalproject.repository.UserRepository
import com.example.mobilefinalproject.repository.friendlyMessage
import com.example.mobilefinalproject.session.UserSessionManager
import kotlinx.coroutines.launch

class CustomerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application)
    private val uploadRepo = UploadRepository(application)

    // ── Room-backed reactive LiveData ─────────────────────────────────────

    /** Emits the cached [UserMe] from Room; updates automatically after any network sync. */
    val userMe: LiveData<UserMe?> =
        repo.observeMe().asLiveData(viewModelScope.coroutineContext)

    // ── Local-only state ──────────────────────────────────────────────────

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    val customerName: String get() = userMe.value?.fullName ?: ""
    val customerId: Int get() = userMe.value?.id ?: 0

    // ── Operations ────────────────────────────────────────────────────────

    /** Fetches user profile from the network and stores it in Room. */
    fun loadMe() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.getMe()
            if (result is ApiResult.Error) _error.value = result.friendlyMessage()
            _loading.value = false
        }
    }

    /**
     * Manually pushes a [UserMe] into the Room cache.
     * Useful when the login response already provides user data so we avoid
     * an extra network round-trip.
     */
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
                                // Sync updated profile image URL back to Room
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

    fun clearCustomer() {
        viewModelScope.launch { repo.clearCache() }
    }

    fun clearError() {
        _error.value = null
    }
}
