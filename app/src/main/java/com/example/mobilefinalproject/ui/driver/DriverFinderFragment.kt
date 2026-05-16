package com.example.mobilefinalproject.ui.driver

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class DriverFinderFragment : Fragment() {
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()

    private lateinit var mapView: MapView
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private val deliveryMarkers = mutableListOf<Marker>()

    private val defaultCenter = GeoPoint(32.0853, 34.7818)

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                enableMyLocation()
            } else {
                showLocationDeniedBanner()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Configuration.getInstance().userAgentValue = requireContext().packageName
        return inflater.inflate(R.layout.fragment_driver_finder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.map_view)
        setupMap()
        loadDeliveryMarkers()
        if (shouldSkipLocationPermissionCheck()) {
            showLocationDeniedBanner()
        } else {
            checkLocationPermission()
        }

        mapView.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                refreshClusteredMarkers(deliveryViewModel.deliveries.value.orEmpty())
                mapView.invalidate()
            }
            false
        }

        view.findViewById<MaterialButton>(R.id.btn_my_location).setOnClickListener {
            val location = myLocationOverlay?.myLocation
            if (location != null) {
                mapView.controller.animateTo(location)
                mapView.controller.setZoom(16.0)
            } else {
                checkLocationPermission()
            }
        }

        view.findViewById<ImageButton>(R.id.btn_dismiss_banner).setOnClickListener {
            view.findViewById<MaterialCardView>(R.id.location_denied_banner).visibility = View.GONE
        }

        deliveryViewModel.selectedDelivery.observe(viewLifecycleOwner) { delivery ->
            if (delivery != null) {
                val tag = DeliveryDetailBottomSheet.TAG
                if (parentFragmentManager.findFragmentByTag(tag) == null) {
                    DeliveryDetailBottomSheet().show(parentFragmentManager, tag)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        myLocationOverlay?.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        myLocationOverlay?.disableMyLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(13.5)
        mapView.controller.setCenter(defaultCenter)
        mapView.minZoomLevel = 5.0
        mapView.maxZoomLevel = 20.0
    }

    private fun loadDeliveryMarkers() {
        deliveryViewModel.setDeliveries(MockDeliveryDataSource.deliveries)
        deliveryViewModel.setPendingDeliveries(MockDeliveryDataSource.getPendingDeliveries())

        deliveryViewModel.deliveries.observe(viewLifecycleOwner) { deliveries ->
            refreshClusteredMarkers(deliveries)
            mapView.invalidate()
        }
    }

    private fun refreshClusteredMarkers(deliveries: List<Delivery>) {
        clearDeliveryMarkers()

        val clusters = DriverFinderMapClusterer.cluster(
            DriverFinderMapClusterer.toMapPoints(deliveries),
            mapView.zoomLevelDouble,
        )

        clusters.forEach { cluster ->
            if (cluster.isSinglePoint) {
                addSingleOrderMarker(cluster.points.first())
            } else {
                addClusterMarker(cluster)
            }
        }
    }

    private fun addSingleOrderMarker(point: DriverFinderMapClusterer.DeliveryMapPoint) {
        val marker = Marker(mapView).apply {
            position = GeoPoint(point.latitude, point.longitude)
            icon = makeCircleMarker(markerColor(point.markerType), markerLabel(point.markerType))
            title = point.title
            snippet = point.snippet
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { _, _ ->
                handleDeliverySelection(point.delivery)
                true
            }
        }
        mapView.overlays.add(marker)
        deliveryMarkers.add(marker)
    }

    private fun addClusterMarker(cluster: DriverFinderMapClusterer.Cluster) {
        val marker = Marker(mapView).apply {
            position = GeoPoint(cluster.centerLatitude, cluster.centerLongitude)
            icon = makeCircleMarker(Color.parseColor("#1565C0"), cluster.points.size.toString())
            title = getString(R.string.locations_nearby, cluster.points.size)
            snippet = getString(R.string.zoom_in_for_orders)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { _, _ ->
                zoomIntoCluster(cluster.centerLatitude, cluster.centerLongitude)
                true
            }
        }
        mapView.overlays.add(marker)
        deliveryMarkers.add(marker)
    }

    private fun clearDeliveryMarkers() {
        if (deliveryMarkers.isEmpty()) {
            return
        }
        mapView.overlays.removeAll(deliveryMarkers.toSet())
        deliveryMarkers.clear()
    }

    private fun markerColor(markerType: DriverFinderMapClusterer.MarkerType): Int = when (markerType) {
        DriverFinderMapClusterer.MarkerType.PICKUP -> Color.parseColor("#2E7D32")
        DriverFinderMapClusterer.MarkerType.DROPOFF -> Color.parseColor("#C62828")
    }

    private fun markerLabel(markerType: DriverFinderMapClusterer.MarkerType): String = when (markerType) {
        DriverFinderMapClusterer.MarkerType.PICKUP -> "P"
        DriverFinderMapClusterer.MarkerType.DROPOFF -> "D"
    }

    private fun checkLocationPermission() {
        val hasFine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            enableMyLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun enableMyLocation() {
        mapView.overlays.removeAll { it is MyLocationNewOverlay }

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView).apply {
            enableMyLocation()
            enableFollowLocation()
            runOnFirstFix {
                requireActivity().runOnUiThread {
                    disableFollowLocation()
                    mapView.controller.animateTo(myLocation)
                    mapView.controller.setZoom(15.0)
                }
            }
        }
        mapView.overlays.add(0, myLocationOverlay)
        mapView.invalidate()
    }

    private fun showLocationDeniedBanner() {
        view?.findViewById<MaterialCardView>(R.id.location_denied_banner)?.visibility = View.VISIBLE
    }

    private fun shouldSkipLocationPermissionCheck(): Boolean {
        return arguments?.getBoolean(ARG_SKIP_LOCATION_PERMISSION_CHECK, false) == true
    }

    internal fun handleDeliverySelection(delivery: Delivery) {
        deliveryViewModel.selectDelivery(delivery)
    }

    internal fun zoomIntoCluster(centerLatitude: Double, centerLongitude: Double) {
        mapView.controller.animateTo(GeoPoint(centerLatitude, centerLongitude))
        mapView.controller.setZoom(DriverFinderMapClusterer.nextClusterZoom(mapView.zoomLevelDouble))
        refreshClusteredMarkers(deliveryViewModel.deliveries.value.orEmpty())
        mapView.invalidate()
    }

    internal fun currentZoomLevel(): Double = mapView.zoomLevelDouble

    private fun makeCircleMarker(bgColor: Int, label: String): BitmapDrawable {
        val size = 80
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 36f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val cx = size / 2f
        val cy = size / 2f
        val radius = size / 2f - 4f

        canvas.drawCircle(cx, cy, radius, bgPaint)
        canvas.drawCircle(cx, cy, radius, borderPaint)
        val textY = cy - (textPaint.ascent() + textPaint.descent()) / 2
        canvas.drawText(label, cx, textY, textPaint)

        return BitmapDrawable(resources, bitmap)
    }

    companion object {
        const val ARG_SKIP_LOCATION_PERMISSION_CHECK = "skip_location_permission_check"
    }
}
