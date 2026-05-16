package com.example.mobilefinalproject.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobilefinalproject.models.Customer

class CustomerViewModel : ViewModel() {

    private val _customer = MutableLiveData<Customer?>()
    val customer: LiveData<Customer?> = _customer

    fun setCustomer(customer: Customer) {
        _customer.value = customer
    }

    fun updateCustomer(customer: Customer) {
        _customer.value = customer
    }

    fun clearCustomer() {
        _customer.value = null
    }
}
