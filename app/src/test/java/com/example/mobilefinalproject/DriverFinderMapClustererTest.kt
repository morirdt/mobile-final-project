package com.example.mobilefinalproject

import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.ui.driver.DriverFinderMapClusterer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverFinderMapClustererTest {

    @Test
    fun toMapPoints_createsPickupAndDropoffPointsForEveryDelivery() {
        val deliveries = MockDeliveryDataSource.deliveries

        val points = DriverFinderMapClusterer.toMapPoints(deliveries)

        assertEquals(deliveries.size * 2, points.size)
        assertTrue(points.any { it.markerType == DriverFinderMapClusterer.MarkerType.PICKUP })
        assertTrue(points.any { it.markerType == DriverFinderMapClusterer.MarkerType.DROPOFF })
    }

    @Test
    fun cluster_groupsMorePointsTogetherAtLowerZoom() {
        val points = DriverFinderMapClusterer.toMapPoints(MockDeliveryDataSource.deliveries)

        val lowZoomClusters = DriverFinderMapClusterer.cluster(points, zoom = 9.0)
        val highZoomClusters = DriverFinderMapClusterer.cluster(points, zoom = 17.0)

        assertTrue(lowZoomClusters.size < highZoomClusters.size)
        assertTrue(lowZoomClusters.any { it.points.size > 1 })
    }

    @Test
    fun nextClusterZoom_increasesByOneAndCapsAtTwenty() {
        assertEquals(11.0, DriverFinderMapClusterer.nextClusterZoom(10.0), 0.0)
        assertEquals(20.0, DriverFinderMapClusterer.nextClusterZoom(20.0), 0.0)
        assertEquals(20.0, DriverFinderMapClusterer.nextClusterZoom(19.7), 0.0)
    }
}
