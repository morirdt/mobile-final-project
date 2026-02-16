package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class CustomerContainerFragment : Fragment() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var customerViewModel: CustomerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel at activity scope
        customerViewModel = ViewModelProvider(requireActivity())[CustomerViewModel::class.java]

        bottomNavigation = view.findViewById(R.id.customer_bottom_navigation)

        // Get NavController from nested NavHostFragment
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.customer_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup automatic navigation using customer_nav_graph
        bottomNavigation.setupWithNavController(navController)
    }
}
