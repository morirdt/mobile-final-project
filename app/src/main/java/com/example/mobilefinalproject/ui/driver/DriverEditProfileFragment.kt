package com.example.mobilefinalproject.ui.driver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.viewmodels.DriverViewModel

class DriverEditProfileFragment : Fragment() {

    private val driverViewModel: DriverViewModel by activityViewModels()

    private lateinit var fullNameInput: TextInputEditText
    private lateinit var driverIdInput: TextInputEditText
    private lateinit var changeProfilePictureButton: FloatingActionButton
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_driver_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        fullNameInput = view.findViewById(R.id.edit_full_name_input)
        driverIdInput = view.findViewById(R.id.edit_driver_id_input)
        saveButton = view.findViewById(R.id.save_profile_button)
        cancelButton = view.findViewById(R.id.cancel_edit_button)

        // Observe driver data and populate fields
        driverViewModel.driver.observe(viewLifecycleOwner) { driver ->
            if (driver != null) {
                fullNameInput.setText(driver.fullName)
                driverIdInput.setText(driver.id)
            }
        }

        // Setup button click listeners
        saveButton.setOnClickListener {
            saveProfile()
        }

        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveProfile() {
        val updatedFullName = fullNameInput.text.toString().trim()

        if (updatedFullName.isEmpty()) {
            fullNameInput.error = "Full name cannot be empty"
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
