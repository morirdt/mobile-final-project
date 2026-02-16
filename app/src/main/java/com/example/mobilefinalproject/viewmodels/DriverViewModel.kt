package com.example.mobilefinalproject.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobilefinalproject.models.driver.Driver

class DriverViewModel : ViewModel() {

    private val _driver = MutableLiveData<Driver?>()
    val driver: LiveData<Driver?> = _driver

    fun setDriver(driver: Driver) {
        _driver.value = driver
    }

    fun updateDriver(driver: Driver) {
        _driver.value = driver
    }

    fun clearDriver() {
        _driver.value = null
    }
}
