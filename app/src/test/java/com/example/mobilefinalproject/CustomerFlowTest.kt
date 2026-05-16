package com.example.mobilefinalproject

import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.DeliveryStatus
import com.example.mobilefinalproject.models.Location
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import java.util.UUID

class CustomerFlowTest {

    @Test
    fun getDeliveriesByCustomer_returnsOnlyMatchingCustomerOrders() {
        val customerId = "123456789"

        val result = MockDeliveryDataSource.getDeliveriesByCustomer(customerId)

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.customerId == customerId })
    }

    @Test
    fun addDelivery_makesOrderVisibleForCustomerAndPendingLists() {
        val snapshot = MockDeliveryDataSource.deliveries.toList()
        try {
            val customerId = "customer-test-${UUID.randomUUID()}"
            val newDelivery = Delivery(
                id = "delivery-test-${UUID.randomUUID()}",
                customerName = "Test Customer",
                customerId = customerId,
                status = DeliveryStatus.PENDING.label,
                price = 19.99,
                date = Date(),
                pickupLocation = Location("Test Pickup", 0.0, 0.0),
                destinationLocation = Location("Test Destination", 0.0, 0.0),
                description = "test",
            )

            MockDeliveryDataSource.addDelivery(newDelivery)

            val customerOrders = MockDeliveryDataSource.getDeliveriesByCustomer(customerId)
            val pending = MockDeliveryDataSource.getPendingDeliveries()

            assertEquals(newDelivery.id, customerOrders.first().id)
            assertTrue(pending.any { it.id == newDelivery.id })
        } finally {
            MockDeliveryDataSource.deliveries.clear()
            MockDeliveryDataSource.deliveries.addAll(snapshot)
        }
    }
}

