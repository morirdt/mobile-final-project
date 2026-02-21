package com.example.mobilefinalproject.models

import java.util.Date

object MockDeliveryDataSource {
    val deliveries: List<Delivery> = listOf(
        Delivery(
            id = "1",
            customerName = "Sarah Johnson",
            customerId = "123456789",
            status = DeliveryStatus.PENDING.label,
            price = 45.00,
            date = Date(),
            pickupAddress = "123 Main St, Downtown",
            destinationAddress = "456 Oak Ave, Uptown",
            phoneNumber = "+1-555-0199",
            rating = 0,
        ),
        Delivery(
            id = "2",
            customerName = "Mike Chen",
            customerId = "123456789",
            status = DeliveryStatus.ACCEPTED.label,
            price = 30.00,
            date = Date(),
            pickupAddress = "789 Pine Rd, West Side",
            destinationAddress = "321 Elm St, East Side",
            phoneNumber = "+1-555-0188",
            rating = 0,

            ),
        Delivery(
            id = "3",
            customerName = "Lisa Martinez",
            customerId = "123456789",
            status = DeliveryStatus.IN_PROGRESS.label,
            price = 40.00,
            date = Date(),
            pickupAddress = "890 Cedar Blvd, North",
            destinationAddress = "432 Maple Dr, South",
            phoneNumber = "+1-555-0166",
            rating = 0,

            ),
        Delivery(
            id = "4",
            customerName = "Mori Arditi",
            customerId = "11111111",
            status = DeliveryStatus.COMPLETED.label,
            price = 40.00,
            date = Date(),
            pickupAddress = "890 Cedar Blvd, North",
            destinationAddress = "432 Maple Dr, South",
            phoneNumber = "+1-555-6767",
            rating = 5,

            )
    )

    fun getPendingDeliveries(): List<Delivery> =
        deliveries.filter { it.status == DeliveryStatus.PENDING.label }

    fun getActiveDeliveries(): List<Delivery> =
        deliveries.filter { it.status == DeliveryStatus.ACCEPTED.label || it.status == DeliveryStatus.IN_PROGRESS.label }

    fun getCompletedDeliveries(): List<Delivery> =
        deliveries.filter { it.status == DeliveryStatus.COMPLETED.label }

    fun getDeliveriesByCustomer(customerId: String): List<Delivery> =
        deliveries.filter { it.customerId == customerId }


}
