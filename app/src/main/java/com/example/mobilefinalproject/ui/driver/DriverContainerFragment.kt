package com.example.mobilefinalproject.ui.driver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.driver.Driver
import com.example.mobilefinalproject.viewmodels.DriverViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class DriverContainerFragment : Fragment() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var driverViewModel: DriverViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_driver_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel at activity scope (shared across all fragments)
        driverViewModel = ViewModelProvider(requireActivity())[DriverViewModel::class.java]

        bottomNavigation = view.findViewById(R.id.driver_bottom_navigation)

        // TODO: Get driver from arguments or authentication
        val driver = Driver("123456789", "John Driver")

        // Set driver in ViewModel (accessible throughout the app)
        driverViewModel.setDriver(driver)

        // Get NavController from nested NavHostFragment
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.driver_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup automatic navigation using driver_nav_graph
        bottomNavigation.setupWithNavController(navController)

        // Hide bottom navigation for edit profile fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.driverEditProfileFragment -> {
                    bottomNavigation.visibility = View.GONE
                }
                else -> {
                    bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }
}

