package com.example.mobilefinalproject.ui.driver

import com.example.mobilefinalproject.network.dto.OrderRead

object DriverFinderMapClusterer {

    data class DeliveryMapPoint(
        val latitude: Double,
        val longitude: Double,
        val order: OrderRead,
        val markerType: MarkerType,
        val title: String,
        val snippet: String,
    )

    data class Cluster(
        val points: List<DeliveryMapPoint>,
        val centerLatitude: Double,
        val centerLongitude: Double,
    ) {
        val isSinglePoint: Boolean = points.size == 1
    }

    enum class MarkerType {
        PICKUP,
        DROPOFF,
    }

    fun toMapPoints(orders: List<OrderRead>): List<DeliveryMapPoint> = buildList {
        orders.forEach { order ->
            if (order.pickupLat != 0.0 || order.pickupLng != 0.0) {
                add(
                    DeliveryMapPoint(
                        latitude = order.pickupLat,
                        longitude = order.pickupLng,
                        order = order,
                        markerType = MarkerType.PICKUP,
                        title = "Pickup - Customer #${order.customerId}",
                        snippet = order.pickupAddress,
                    )
                )
            }

            if (order.dropoffLat != 0.0 || order.dropoffLng != 0.0) {
                add(
                    DeliveryMapPoint(
                        latitude = order.dropoffLat,
                        longitude = order.dropoffLng,
                        order = order,
                        markerType = MarkerType.DROPOFF,
                        title = "Drop-off - Customer #${order.customerId}",
                        snippet = order.dropoffAddress,
                    )
                )
            }
        }
    }

    fun cluster(points: List<DeliveryMapPoint>, zoom: Double): List<Cluster> {
        if (points.isEmpty()) return emptyList()

        val cellSizeDegrees = cellSizeDegrees(zoom)
        return points
            .groupBy { point ->
                val latBucket = (point.latitude / cellSizeDegrees).toInt()
                val lonBucket = (point.longitude / cellSizeDegrees).toInt()
                "$latBucket:$lonBucket"
            }
            .values
            .map { clusteredPoints ->
                Cluster(
                    points = clusteredPoints,
                    centerLatitude = clusteredPoints.map { it.latitude }.average(),
                    centerLongitude = clusteredPoints.map { it.longitude }.average(),
                )
            }
    }

    fun cellSizeDegrees(zoom: Double): Double = when {
        zoom < 10.0 -> 0.05
        zoom < 12.0 -> 0.02
        zoom < 14.0 -> 0.01
        zoom < 16.0 -> 0.005
        else -> 0.002
    }

    fun nextClusterZoom(currentZoom: Double): Double = (currentZoom + 1.0).coerceAtMost(20.0)
}
