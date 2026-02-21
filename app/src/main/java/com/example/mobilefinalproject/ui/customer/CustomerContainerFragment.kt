package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mobilefinalproject.databinding.FragmentCustomerContainerBinding
import com.example.mobilefinalproject.models.Customer
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.R

class CustomerContainerFragment : Fragment() {

    private var binding: FragmentCustomerContainerBinding? = null
    private lateinit var customerViewModel: CustomerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerContainerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel at activity scope
        customerViewModel = ViewModelProvider(requireActivity())[CustomerViewModel::class.java]

        // TODO: Get driver from arguments or authentication
        val customer = Customer("123456789", "John Customer")

        // Set customer in ViewModel (accessible throughout the app)
        customerViewModel.setCustomer(customer)

        // Get NavController from nested NavHostFragment
        val navHostFragment = binding?.let {
            childFragmentManager.findFragmentById(it.customerNavHostFragment.id) as NavHostFragment
        } ?: return
        val navController = navHostFragment.navController

        // Setup automatic navigation using customer_nav_graph
        binding?.customerBottomNavigation?.setupWithNavController(navController)

        // Hide bottom navigation on edit profile screen
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.customerEditProfileFragment -> {
                    binding?.customerBottomNavigation?.visibility = View.GONE
                }
                else -> {
                    binding?.customerBottomNavigation?.visibility = View.VISIBLE
                }
            }
        }
    }
}
