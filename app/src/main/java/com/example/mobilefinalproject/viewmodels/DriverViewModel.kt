package com.example.mobilefinalproject.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobilefinalproject.network.dto.UserMe
import com.example.mobilefinalproject.network.dto.UserUpdateRequest
import com.example.mobilefinalproject.repository.ApiResult
import com.example.mobilefinalproject.repository.UserRepository
import com.example.mobilefinalproject.session.UserSessionManager
import kotlinx.coroutines.launch

class DriverViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application)

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

    fun updateProfile(fullName: String? = null, phone: String? = null, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repo.updateMe(UserUpdateRequest(fullName = fullName, phone = phone))) {
                is ApiResult.Success -> {
                    _userMe.value = result.data
                    val ctx = getApplication<Application>()
                    UserSessionManager.getSession(ctx)?.let { session ->
                        UserSessionManager.saveSession(ctx, session.copy(fullName = result.data.fullName))
                    }
                    onSuccess?.invoke()
                }
                is ApiResult.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun clearDriver() {
        _userMe.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
