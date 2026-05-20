package com.example.mobilefinalproject

import com.example.mobilefinalproject.network.dto.OrderRead
import com.example.mobilefinalproject.ui.driver.DriverFinderMapClusterer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverFinderMapClustererTest {

    private fun makeOrder(id: Int, pickupLat: Double, pickupLng: Double, dropoffLat: Double, dropoffLng: Double) =
        OrderRead(
            id = id,
            customerId = 1,
            driverId = null,
            status = "pending",
            pickupAddress = "Pickup $id",
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            dropoffAddress = "Dropoff $id",
            dropoffLat = dropoffLat,
            dropoffLng = dropoffLng,
            cargoDescription = null,
            cargoWeightKg = null,
            notes = null,
            priceCents = 1000,
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

    private val sampleOrders = listOf(
        makeOrder(1, 32.08, 34.78, 32.10, 34.80),
        makeOrder(2, 32.09, 34.79, 32.11, 34.81),
        makeOrder(3, 32.50, 34.90, 32.60, 35.00),
    )

    @Test
    fun toMapPoints_createsPickupAndDropoffPointsForEveryOrder() {
        val points = DriverFinderMapClusterer.toMapPoints(sampleOrders)

        // 3 orders × 2 points each = 6
        assertEquals(sampleOrders.size * 2, points.size)
        assertTrue(points.any { it.markerType == DriverFinderMapClusterer.MarkerType.PICKUP })
        assertTrue(points.any { it.markerType == DriverFinderMapClusterer.MarkerType.DROPOFF })
    }

    @Test
    fun cluster_groupsMorePointsTogetherAtLowerZoom() {
        val points = DriverFinderMapClusterer.toMapPoints(sampleOrders)

        val lowZoomClusters = DriverFinderMapClusterer.cluster(points, zoom = 9.0)
        val highZoomClusters = DriverFinderMapClusterer.cluster(points, zoom = 17.0)

        assertTrue(lowZoomClusters.size <= highZoomClusters.size)
    }

    @Test
    fun nextClusterZoom_increasesByOneAndCapsAtTwenty() {
        assertEquals(11.0, DriverFinderMapClusterer.nextClusterZoom(10.0), 0.0)
        assertEquals(20.0, DriverFinderMapClusterer.nextClusterZoom(20.0), 0.0)
        assertEquals(20.0, DriverFinderMapClusterer.nextClusterZoom(19.7), 0.0)
    }

    @Test
    fun toMapPoints_skipsZeroCoordinatePoints() {
        val orderWithZeroCoords = makeOrder(99, 0.0, 0.0, 0.0, 0.0)
        val points = DriverFinderMapClusterer.toMapPoints(listOf(orderWithZeroCoords))
        assertTrue(points.isEmpty())
    }
}
