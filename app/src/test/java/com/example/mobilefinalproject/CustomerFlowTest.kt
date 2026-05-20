package com.example.mobilefinalproject

import com.example.mobilefinalproject.network.dto.OrderRead
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomerFlowTest {

    private fun makeOrder(
        id: Int,
        customerId: Int,
        status: String = "pending"
    ) = OrderRead(
        id = id,
        customerId = customerId,
        driverId = null,
        status = status,
        pickupAddress = "Test Pickup",
        pickupLat = 32.0,
        pickupLng = 34.7,
        dropoffAddress = "Test Dropoff",
        dropoffLat = 32.1,
        dropoffLng = 34.8,
        cargoDescription = "Test cargo",
        cargoWeightKg = null,
        notes = null,
        priceCents = 1999,
        currency = "USD",
        acceptedAt = null,
        startedAt = null,
        pickedUpAt = null,
        completedAt = null,
        cancelledAt = null,
        cancellationReason = null,
        cargoImageUrl = null,
        createdAt = "2024-01-15T10:30:00",
        updatedAt = "2024-01-15T10:30:00"
    )

    @Test
    fun filterByCustomerId_returnsOnlyMatchingOrders() {
        val orders = listOf(
            makeOrder(1, customerId = 10),
            makeOrder(2, customerId = 10),
            makeOrder(3, customerId = 99),
        )

        val result = orders.filter { it.customerId == 10 }

        assertEquals(2, result.size)
        assertTrue(result.all { it.customerId == 10 })
    }

    @Test
    fun filterPendingOrders_returnsOnlyPendingStatus() {
        val orders = listOf(
            makeOrder(1, customerId = 10, status = "pending"),
            makeOrder(2, customerId = 10, status = "accepted"),
            makeOrder(3, customerId = 10, status = "completed"),
        )

        val pending = orders.filter { it.status == "pending" }

        assertEquals(1, pending.size)
        assertEquals("pending", pending.first().status)
    }

    @Test
    fun priceCentsConversion_displaysDollarsCorrectly() {
        val order = makeOrder(1, customerId = 10).copy(priceCents = 2550)
        val dollars = order.priceCents / 100.0
        assertEquals(25.50, dollars, 0.001)
    }
}
