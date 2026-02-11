package com.example.mobilefinalproject.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.databinding.FragmentLoginBinding
import com.example.mobilefinalproject.ui.driver.DriverHomeActivity
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null

    private var selectedUserType: UserType = UserType.DRIVER

    enum class UserType {
        DRIVER, CUSTOMER
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserTypeToggle()
        setupRegisterLink()
        setupLoginButton()
    }

    private fun setupLoginButton() {
        binding?.loginButton?.setOnClickListener {
            // TODO: Add validation and actual authentication
            // For now, assume successful login
            when (selectedUserType) {
                UserType.DRIVER -> {
                    // Navigate to Driver Home Activity
                    val intent = Intent(requireContext(), DriverHomeActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                UserType.CUSTOMER -> {
                    // TODO: Navigate to Customer Home when implemented
                    // For now, do nothing
                }
            }
        }
    }
    private fun setupRegisterLink() {
        binding?.registerLinkTextView?.setOnClickListener {
            findNavController().navigate(com.example.mobilefinalproject.R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun setupUserTypeToggle() {
        // Set initial state - Driver is selected by default
        updateButtonStates(UserType.DRIVER)

        binding?.driverButton?.setOnClickListener {
            if (selectedUserType != UserType.DRIVER) {
                selectedUserType = UserType.DRIVER
                updateButtonStates(UserType.DRIVER)
            }
        }

        binding?.customerButton?.setOnClickListener {
            if (selectedUserType != UserType.CUSTOMER) {
                selectedUserType = UserType.CUSTOMER
                updateButtonStates(UserType.CUSTOMER)
            }
        }
    }

    private fun updateButtonStates(selectedType: UserType) {
        when (selectedType) {
            UserType.DRIVER -> {
                // Driver button filled, Customer button outlined
                setButtonFilled(binding?.driverButton)
                setButtonOutlined(binding?.customerButton)
            }
            UserType.CUSTOMER -> {
                // Customer button filled, Driver button outlined
                setButtonFilled(binding?.customerButton)
                setButtonOutlined(binding?.driverButton)
            }
        }
    }

    private fun setButtonFilled(button: MaterialButton?) {
        button?.setBackgroundColor(resources.getColor(com.example.mobilefinalproject.R.color.teal_700, null))
        button?.setTextColor(resources.getColor(android.R.color.white, null))
    }

    private fun setButtonOutlined(button: MaterialButton?) {
        button?.setBackgroundColor(resources.getColor(android.R.color.white, null))
        button?.setTextColor(resources.getColor(com.example.mobilefinalproject.R.color.teal_700, null))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
