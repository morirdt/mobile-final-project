package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.Customer
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.databinding.FragmentCustomerProfileBinding

class CustomerProfileFragment : Fragment() {
    private val customerViewModel: CustomerViewModel by activityViewModels()
    private var customer: Customer? = null
    private var binding: FragmentCustomerProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerViewModel.customer.observe(viewLifecycleOwner) { customer ->
            this.customer = customer
            updateUI()
        }

        // Setup edit profile button click listener
        binding?.customerProfileEditButton?.setOnClickListener {
            findNavController().navigate(R.id.action_customerProfileFragment_to_customerEditProfileFragment)
        }

        // Setup logout button click listener
        binding?.customerProfileLogoutButton?.setOnClickListener {
            logout()
        }
    }

    private fun updateUI() {
        binding?.customerProfileNameTextView?.text = this.customer?.fullName
        binding?.customerProfileIdTextView?.text = this.customer?.id
    }

    private fun logout() {
        customerViewModel.clearCustomer()
        val parentNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        parentNavController.navigate(R.id.action_global_loginFragment)
    }
}
