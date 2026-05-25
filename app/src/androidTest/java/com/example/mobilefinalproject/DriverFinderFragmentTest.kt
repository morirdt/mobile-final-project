package com.example.mobilefinalproject

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.ui.driver.DriverFinderFragment
import com.example.mobilefinalproject.ui.driver.DriverFinderMapClusterer
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class DriverFinderFragmentTest {

    @Test
    fun selectingDeliveryShowsDetailBottomSheet() {
        val delivery = MockDeliveryDataSource.deliveries.first()
        val scenario = launchFinderFragment()

        scenario.onFragment { fragment ->
            fragment.handleDeliverySelection(delivery)
        }

        onView(withId(R.id.tv_order_id))
            .check(matches(withText(fragmentString(R.string.order_id_format, delivery.id))))
        onView(withId(R.id.tv_customer_name))
            .check(matches(withText(delivery.customerName)))
        onView(withId(R.id.tv_pickup_address))
            .check(matches(withText(delivery.pickupAddress)))
        onView(withId(R.id.tv_dropoff_address))
            .check(matches(withText(delivery.dropoffAddress)))
    }

    @Test
    fun zoomIntoClusterIncreasesMapZoom() {
        val scenario = launchFinderFragment()
        val cluster = DriverFinderMapClusterer.cluster(
            DriverFinderMapClusterer.toMapPoints(MockDeliveryDataSource.deliveries),
            zoom = 9.0,
        ).first { it.points.size > 1 }

        scenario.onFragment { fragment ->
            val initialZoom = fragment.currentZoomLevel()

            fragment.zoomIntoCluster(cluster.centerLatitude, cluster.centerLongitude)

            assertTrue(fragment.currentZoomLevel() > initialZoom)
        }
    }

    private fun launchFinderFragment() = launchFragmentInContainer<DriverFinderFragment>(
        fragmentArgs = bundleOf(
            DriverFinderFragment.ARG_SKIP_LOCATION_PERMISSION_CHECK to true,
        ),
        themeResId = R.style.Theme_MobileFinalProject,
    )

    private fun fragmentString(resId: Int, vararg args: Any): String {
        return androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getString(resId, *args)
    }
}
