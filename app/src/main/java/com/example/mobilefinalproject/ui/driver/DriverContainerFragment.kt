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
import com.example.mobilefinalproject.databinding.FragmentDriverContainerBinding
import com.example.mobilefinalproject.viewmodels.DriverViewModel

class DriverContainerFragment : Fragment() {

    private var binding: FragmentDriverContainerBinding? = null
    private lateinit var driverViewModel: DriverViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverContainerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel at activity scope
        driverViewModel = ViewModelProvider(requireActivity())[DriverViewModel::class.java]

        // Load driver profile if not already loaded
        if (driverViewModel.userMe.value == null) {
            driverViewModel.loadMe()
        }

        // Mark driver as available (only if not currently busy with an order)
        driverViewModel.setAvailableIfNotBusy()

        // Get NavController from nested NavHostFragment
        val navHostFragment = binding?.let {
            childFragmentManager.findFragmentById(it.driverNavHostFragment.id) as NavHostFragment
        } ?: return
        val navController = navHostFragment.navController

        // Setup automatic navigation using driver_nav_graph
        binding?.driverBottomNavigation?.setupWithNavController(navController)

        // Hide bottom navigation on edit profile screen
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.driverEditProfileFragment -> {
                    binding?.driverBottomNavigation?.visibility = View.GONE
                }
                else -> {
                    binding?.driverBottomNavigation?.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
