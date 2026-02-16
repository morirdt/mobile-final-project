package com.example.mobilefinalproject.ui.driver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.driver.Driver
import com.example.mobilefinalproject.viewmodels.DriverViewModel

class DriverProfileFragment : Fragment() {
    private val driverViewModel: DriverViewModel by activityViewModels()
    private var driver: Driver? = null

    private lateinit var driverNameTextView: TextView
    private lateinit var driverIdTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_driver_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        driverNameTextView = view.findViewById(R.id.driver_name_text_view)
        driverIdTextView = view.findViewById(R.id.driver_id_text_view)
        editProfileButton = view.findViewById(R.id.edit_profile_button)
        logoutButton = view.findViewById(R.id.logout_button)

        driverViewModel.driver.observe(viewLifecycleOwner) { driver ->
            this.driver = driver
            updateUI()
        }

        // Setup edit profile button click listener
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_driverProfileFragment_to_driverEditProfileFragment)
        }

        // Setup logout button click listener
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun updateUI() {
        driverNameTextView.text = this.driver?.fullName
        driverIdTextView.text = this.driver?.id
    }

    private fun logout() {
        driverViewModel.clearDriver()
        val parentNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        parentNavController.navigate(R.id.action_global_loginFragment)
    }
}
