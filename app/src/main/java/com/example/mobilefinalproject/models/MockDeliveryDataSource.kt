package com.example.mobilefinalproject.models

import java.util.Date

object MockDeliveryDataSource {
    val deliveries: MutableList<Delivery> = mutableListOf(
        Delivery(
            id = "1",
            customerName = "Sarah Johnson",
            customerId = "123456789",
            status = DeliveryStatus.PENDING.label,
            price = 45.00,
            date = Date(),
            pickupLocation = Location("123 Main St, Downtown", 40.7128, -74.0060),
            destinationLocation = Location("456 Oak Ave, Uptown", 40.7306, -73.9866),
            description = "Fragile glassware - handle with care",
        ),
        Delivery(
            id = "2",
            customerName = "Mike Chen",
            customerId = "123456789",
            status = DeliveryStatus.ACCEPTED.label,
            price = 30.00,
            date = Date(),
            pickupLocation = Location("789 Pine Rd, West Side", 40.7580, -73.9855),
            destinationLocation = Location("321 Elm St, East Side", 40.7484, -73.9857),
            description = "Electronics package - requires signature",
        ),
        Delivery(
            id = "3",
            customerName = "Lisa Martinez",
            customerId = "123456789",
            status = DeliveryStatus.IN_PROGRESS.label,
            price = 40.00,
            date = Date(),
            pickupLocation = Location("890 Cedar Blvd, North", 40.7060, -74.0086),
            destinationLocation = Location("432 Maple Dr, South", 40.7527, -73.9772),
            description = "Office supplies and documents",
        ),
        Delivery(
            id = "4",
            customerName = "Mori Arditi",
            customerId = "123456789",
            status = DeliveryStatus.COMPLETED.label,
            price = 40.00,
            date = Date(),
            pickupLocation = Location("890 Cedar Blvd, North", 40.7060, -74.0086),
            destinationLocation = Location("432 Maple Dr, South", 40.7527, -73.9772),
            description = "Books and magazines collection",
        )
    )

    fun addDelivery(delivery: Delivery) {
        deliveries.add(0, delivery)
    }

    fun updateDeliveryStatus(deliveryId: String, newStatus: String) {
        val index = deliveries.indexOfFirst { it.id == deliveryId }
        if (index != -1) {
            deliveries[index] = deliveries[index].copy(status = newStatus)
        }
    }

    fun getPendingDeliveries(): List<Delivery> =
        deliveries.filter { it.status == DeliveryStatus.PENDING.label }

    fun getActiveDeliveries(): List<Delivery> =
        deliveries.filter { it.status == DeliveryStatus.ACCEPTED.label || it.status == DeliveryStatus.IN_PROGRESS.label }

    fun getCompletedDeliveries(): List<Delivery> =
        deliveries.filter { it.status == DeliveryStatus.COMPLETED.label }

    fun getDeliveriesByCustomer(customerId: String): List<Delivery> =
        deliveries.filter { it.customerId == customerId }
}
