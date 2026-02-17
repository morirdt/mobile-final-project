package com.example.mobilefinalproject.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.DeliveryStatus
import java.util.Date

class DeliveryViewModel : ViewModel() {
    private val _deliveries = MutableLiveData<List<Delivery>>()
    val deliveries: LiveData<List<Delivery>> = _deliveries

    private val _activeDeliveries = MutableLiveData<List<Delivery>>()
    val activeDeliveries: LiveData<List<Delivery>> = _activeDeliveries

    private val _completedDeliveries = MutableLiveData<List<Delivery>>()
    val completedDeliveries: LiveData<List<Delivery>> = _completedDeliveries

    private val _customerDeliveries = MutableLiveData<List<Delivery>>()
    val customerDeliveries: LiveData<List<Delivery>> = _customerDeliveries

    fun getDeliveriesByCustomer(customerId: String): List<Delivery> {
        return _deliveries.value?.filter { it.customerId == customerId } ?: emptyList()
    }
    fun setDeliveries(deliveries: List<Delivery>) {
        _deliveries.value = deliveries
    }

    fun setActiveDeliveries(activeDeliveries: List<Delivery>) {
        _activeDeliveries.value = activeDeliveries
    }

    fun setCompletedDeliveries(completedDeliveries: List<Delivery>) {
        _completedDeliveries.value = completedDeliveries
    }

    fun setCustomerDeliveries(customerDeliveries: List<Delivery>) {
        _customerDeliveries.value = customerDeliveries
    }


    fun addDelivery(delivery: Delivery) {
        val currentList = _deliveries.value?.toMutableList() ?: mutableListOf()
        currentList.add(delivery)
        _deliveries.value = currentList
    }

    fun removeDelivery(delivery: Delivery) {
        val currentList = _deliveries.value?.toMutableList() ?: mutableListOf()
        currentList.remove(delivery)
        _deliveries.value = currentList
    }

    fun updateDelivery(delivery: Delivery) {
        val currentList = _deliveries.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == delivery.id }
        if (index != -1) {
            currentList[index] = delivery
            _deliveries.value = currentList
        }
    }
}
