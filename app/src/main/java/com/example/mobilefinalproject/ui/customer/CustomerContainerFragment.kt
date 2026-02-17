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
import com.example.mobilefinalproject.models.customer.Customer
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


        // TODO: Get driver from arguments or authentication
        val customer = Customer("123456789", "John Customer")

        // Set customer in ViewModel (accessible throughout the app)
        customerViewModel.setCustomer(customer)


        // Get NavController from nested NavHostFragment
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.customer_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup automatic navigation using customer_nav_graph
        bottomNavigation.setupWithNavController(navController)

        // Hide bottom navigation on edit profile screen
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.customerEditProfileFragment -> {
                    bottomNavigation.visibility = View.GONE
                }
                else -> {
                    bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }
}
