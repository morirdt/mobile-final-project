package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.FragmentCustomerEditProfileBinding
import com.example.mobilefinalproject.viewmodels.CustomerViewModel

class CustomerEditProfileFragment : Fragment() {

    private val customerViewModel: CustomerViewModel by activityViewModels()
    private var binding: FragmentCustomerEditProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerEditProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe customer data and populate fields
        customerViewModel.customer.observe(viewLifecycleOwner) { customer ->
            if (customer != null) {
                binding?.customerEditProfileFullNameEditText?.setText(customer.fullName)
                binding?.customerEditProfileIdEditText?.setText(customer.id)
            }
        }

        // Setup button click listeners
        binding?.customerEditProfileSaveButton?.setOnClickListener {
            saveProfile()
        }

        binding?.customerEditProfileCancelButton?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveProfile() {
        val binding = binding ?: return
        val updatedFullName = binding.customerEditProfileFullNameEditText.text.toString().trim()

        if (updatedFullName.isEmpty()) {
            binding.customerEditProfileFullNameEditText.error = "Full name cannot be empty"
            return
        }

        // Get current customer from ViewModel
        val currentCustomer = customerViewModel.customer.value
        if (currentCustomer != null) {
            val updatedCustomer = currentCustomer.copy(fullName = updatedFullName)
            customerViewModel.updateCustomer(updatedCustomer)
            findNavController().navigateUp()
        }
    }
}
