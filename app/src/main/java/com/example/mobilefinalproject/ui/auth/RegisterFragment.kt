package com.example.mobilefinalproject.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.databinding.FragmentRegisterBinding
import com.google.android.material.button.MaterialButton

class RegisterFragment : Fragment() {

    private var binding: FragmentRegisterBinding? = null

    private var selectedUserType: UserType = UserType.DRIVER

    enum class UserType {
        DRIVER, CUSTOMER
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserTypeToggle()
        setupLoginLink()
        // TODO: Implement registration logic
    }

    private fun setupLoginLink() {
        binding?.loginLinkTextView?.setOnClickListener {
            findNavController().popBackStack()
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

    private fun updateButtonStates(selectedType: RegisterFragment.UserType) {
        when (selectedType) {
            RegisterFragment.UserType.DRIVER -> {
                // Driver button filled, Customer button outlined
                setButtonFilled(binding?.driverButton)
                setButtonOutlined(binding?.customerButton)
            }
            RegisterFragment.UserType.CUSTOMER -> {
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
