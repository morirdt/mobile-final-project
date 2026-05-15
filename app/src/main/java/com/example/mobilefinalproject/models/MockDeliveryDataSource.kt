package com.example.mobilefinalproject.models

import java.util.Date

object MockDeliveryDataSource {
    val deliveries: List<Delivery> = listOf(
        Delivery(
            id = "1",
            customerName = "Sarah Johnson",
            status = DeliveryStatus.PENDING.label,
            price = 45.00,
            date = Date(),
            pickupAddress = "123 Main St, Downtown",
            dropoffAddress = "456 Oak Ave, Uptown",
            phoneNumber = "+1-555-0199",
            rating = 0,
            pickupLat = 32.0853,
            pickupLng = 34.7818,
            dropoffLat = 32.0923,
            dropoffLng = 34.7901,
        ),
        Delivery(
            id = "2",
            customerName = "Mike Chen",
            status = DeliveryStatus.ACCEPTED.label,
            price = 30.00,
            date = Date(),
            pickupAddress = "789 Pine Rd, West Side",
            dropoffAddress = "321 Elm St, East Side",
            phoneNumber = "+1-555-0188",
            rating = 0,
            pickupLat = 32.0780,
            pickupLng = 34.7750,
            dropoffLat = 32.0810,
            dropoffLng = 34.7950,
        ),
        Delivery(
            id = "3",
            customerName = "Lisa Martinez",
            status = DeliveryStatus.IN_PROGRESS.label,
            price = 40.00,
            date = Date(),
            pickupAddress = "890 Cedar Blvd, North",
            dropoffAddress = "432 Maple Dr, South",
            phoneNumber = "+1-555-0166",
            rating = 0,
            pickupLat = 32.0970,
            pickupLng = 34.7845,
            dropoffLat = 32.0730,
            dropoffLng = 34.7820,
        ),
        Delivery(
            id = "4",
            customerName = "Mori Arditi",
            status = DeliveryStatus.COMPLETED.label,
            price = 40.00,
            date = Date(),
            pickupAddress = "890 Cedar Blvd, North",
            dropoffAddress = "432 Maple Dr, South",
            phoneNumber = "+1-555-6767",
            rating = 5,
            pickupLat = 32.0660,
            pickupLng = 34.7780,
            dropoffLat = 32.0700,
            dropoffLng = 34.7700,
        )
    )

    fun getPendingDeliveries(): List<Delivery> =
        deliveries.filter { it.status == DeliveryStatus.PENDING.label }

    fun getActiveDeliveries(): List<Delivery> =
        deliveries.filter { it.status == DeliveryStatus.ACCEPTED.label || it.status == DeliveryStatus.IN_PROGRESS.label }

    fun getCompletedDeliveries(): List<Delivery> =
        deliveries.filter { it.status == DeliveryStatus.COMPLETED.label }


}
