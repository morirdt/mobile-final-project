package com.example.mobilefinalproject.ui.driver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.databinding.FragmentDriverEditProfileBinding
import com.example.mobilefinalproject.viewmodels.DriverViewModel

class DriverEditProfileFragment : Fragment() {
    private val driverViewModel: DriverViewModel by activityViewModels()
    private var binding: FragmentDriverEditProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverEditProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe driver data and populate fields
        driverViewModel.driver.observe(viewLifecycleOwner) { driver ->
            if (driver != null) {
                binding?.driverEditProfileFullNameEditText?.setText(driver.fullName)
                binding?.driverEditProfileIdEditText?.setText(driver.id)
            }
        }

        // Setup button click listeners
        binding?.driverEditProfileSaveButton?.setOnClickListener {
            saveProfile()
        }

        binding?.driverEditProfileCancelButton?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveProfile() {
        val binding = binding ?: return
        val updatedFullName = binding.driverEditProfileFullNameEditText.text.toString().trim()

        if (updatedFullName.isEmpty()) {
            binding.driverEditProfileFullNameEditText.error = "Full name cannot be empty"
            return
        }

        // Get current driver from ViewModel
        val currentDriver = driverViewModel.driver.value
        if (currentDriver != null) {
            val updatedDriver = currentDriver.copy(fullName = updatedFullName)
            driverViewModel.updateDriver(updatedDriver)
            findNavController().navigateUp()
        }
    }
}
