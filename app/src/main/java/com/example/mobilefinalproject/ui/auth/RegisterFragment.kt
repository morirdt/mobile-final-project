package com.example.mobilefinalproject.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.FragmentRegisterBinding
import com.example.mobilefinalproject.repository.ApiResult
import com.example.mobilefinalproject.repository.AuthRepository
import com.example.mobilefinalproject.repository.UserRepository
import com.example.mobilefinalproject.session.UserSessionManager
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.viewmodels.DriverViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var binding: FragmentRegisterBinding? = null
    private val driverViewModel: DriverViewModel by activityViewModels()
    private val customerViewModel: CustomerViewModel by activityViewModels()

    private var selectedUserType: UserType = UserType.DRIVER

    enum class UserType { DRIVER, CUSTOMER }

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
        setupCreateAccountButton()
    }

    private fun setupLoginLink() {
        binding?.loginLinkTextView?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCreateAccountButton() {
        binding?.createAccountButton?.setOnClickListener {
            val fullName = binding?.fullNameTextInputEditText?.text?.toString()?.trim().orEmpty()
            val email    = binding?.idTextInputEditText?.text?.toString()?.trim().orEmpty()
            val password = binding?.passwordTextInputEditText?.text?.toString().orEmpty()

            var valid = true
            if (fullName.isBlank()) {
                binding?.fullNameTextInputEditText?.error = "Full name is required"
                valid = false
            }
            if (email.isBlank()) {
                binding?.idTextInputEditText?.error = "Email is required"
                valid = false
            }
            if (password.isBlank()) {
                binding?.passwordTextInputEditText?.error = "Password is required"
                valid = false
            }
            if (!valid) return@setOnClickListener

            setLoading(true)

            viewLifecycleOwner.lifecycleScope.launch {
                val authRepo = AuthRepository(requireContext())
                val userRepo = UserRepository(requireContext())

                val registerResult = if (selectedUserType == UserType.CUSTOMER) {
                    authRepo.registerCustomer(email, password, fullName, null)
                } else {
                    // Driver registration requires vehicle details not captured in this screen.
                    // Using placeholder values — a dedicated driver-registration screen should
                    // collect these fields in a future iteration.
                    authRepo.registerDriver(
                        email = email, password = password, fullName = fullName, phone = null,
                        licenseNumber = "PENDING", vehicleType = "truck",
                        vehiclePlate = "PENDING", vehicleCapacityKg = null
                    )
                }

                when (registerResult) {
                    is ApiResult.Error -> {
                        setLoading(false)
                        Toast.makeText(requireContext(), registerResult.message, Toast.LENGTH_LONG).show()
                    }
                    is ApiResult.Success -> {
                        // Auto-login after registration
                        val role = if (selectedUserType == UserType.DRIVER) "driver" else "customer"
                        when (val tokenResult = authRepo.loginJson(email, password, role)) {
                            is ApiResult.Error -> {
                                setLoading(false)
                                Toast.makeText(
                                    requireContext(),
                                    "Registered! Please log in.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController().popBackStack()
                            }
                            is ApiResult.Success -> {
                                when (val meResult = userRepo.getMe()) {
                                    is ApiResult.Error -> {
                                        setLoading(false)
                                        Toast.makeText(requireContext(), meResult.message, Toast.LENGTH_LONG).show()
                                    }
                                    is ApiResult.Success -> {
                                        val me = meResult.data
                                        val userType = if (me.role == "driver") UserSessionManager.UserType.DRIVER
                                                       else UserSessionManager.UserType.CUSTOMER
                                        UserSessionManager.saveSession(
                                            requireContext(),
                                            UserSessionManager.UserSession(
                                                userType = userType,
                                                userId = me.id.toString(),
                                                fullName = me.fullName,
                                                email = me.email,
                                                phone = me.phone,
                                                profileImageUrl = me.profileImageUrl
                                            )
                                        )
                                        setLoading(false)
                                        if (userType == UserSessionManager.UserType.DRIVER) {
                                            driverViewModel.setUserMe(me)
                                            findNavController().navigate(R.id.action_loginFragment_to_driverContainerFragment)
                                        } else {
                                            customerViewModel.setUserMe(me)
                                            findNavController().navigate(R.id.action_loginFragment_to_customerContainerFragment)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding?.createAccountButton?.isEnabled = !loading
        binding?.createAccountButton?.text = if (loading) "Creating account…" else "Create Account"
    }

    private fun setupUserTypeToggle() {
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
                setButtonFilled(binding?.driverButton)
                setButtonOutlined(binding?.customerButton)
            }
            UserType.CUSTOMER -> {
                setButtonFilled(binding?.customerButton)
                setButtonOutlined(binding?.driverButton)
            }
        }
    }

    private fun setButtonFilled(button: MaterialButton?) {
        button?.setBackgroundColor(resources.getColor(R.color.teal_700, null))
        button?.setTextColor(resources.getColor(android.R.color.white, null))
    }

    private fun setButtonOutlined(button: MaterialButton?) {
        button?.setBackgroundColor(resources.getColor(android.R.color.white, null))
        button?.setTextColor(resources.getColor(R.color.teal_700, null))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
