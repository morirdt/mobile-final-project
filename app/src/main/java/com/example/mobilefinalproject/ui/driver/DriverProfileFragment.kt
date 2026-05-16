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
import com.example.mobilefinalproject.databinding.FragmentDriverProfileBinding
import com.example.mobilefinalproject.models.driver.Driver
import com.example.mobilefinalproject.viewmodels.DriverViewModel

class DriverProfileFragment : Fragment() {
    private val driverViewModel: DriverViewModel by activityViewModels()
    private var driver: Driver? = null
    private var binding: FragmentDriverProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        driverViewModel.driver.observe(viewLifecycleOwner) { driver ->
            this.driver = driver
            updateUI()
        }

        // Setup edit profile button click listener
        binding?.driverProfileEditButton?.setOnClickListener {
            findNavController().navigate(R.id.action_driverProfileFragment_to_driverEditProfileFragment)
        }

        // Setup logout button click listener
        binding?.driverProfileLogoutButton?.setOnClickListener {
            logout()
        }
    }

    private fun updateUI() {
        binding?.driverProfileNameTextView?.text = this.driver?.fullName
        binding?.driverProfileIdTextView?.text = this.driver?.id
    }

    private fun logout() {
        driverViewModel.clearDriver()
        val parentNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        parentNavController.navigate(R.id.action_global_loginFragment)
    }
}
