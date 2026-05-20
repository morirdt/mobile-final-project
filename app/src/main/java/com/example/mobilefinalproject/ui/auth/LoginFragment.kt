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
import com.example.mobilefinalproject.databinding.FragmentLoginBinding
import com.example.mobilefinalproject.repository.ApiResult
import com.example.mobilefinalproject.repository.AuthRepository
import com.example.mobilefinalproject.repository.UserRepository
import com.example.mobilefinalproject.session.UserSessionManager
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.viewmodels.DriverViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null
    private val driverViewModel: DriverViewModel by activityViewModels()
    private val customerViewModel: CustomerViewModel by activityViewModels()

    private var selectedUserType: UserType = UserType.DRIVER

    enum class UserType { DRIVER, CUSTOMER }

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

        if (savedInstanceState == null && tryRestoreSavedSession()) return

        setupUserTypeToggle()
        setupRegisterLink()
        setupLoginButton()
    }

    private fun tryRestoreSavedSession(): Boolean {
        val session = UserSessionManager.getSession(requireContext()) ?: return false
        when (session.userType) {
            UserSessionManager.UserType.DRIVER ->
                findNavController().navigate(R.id.action_loginFragment_to_driverContainerFragment)
            UserSessionManager.UserType.CUSTOMER ->
                findNavController().navigate(R.id.action_loginFragment_to_customerContainerFragment)
        }
        return true
    }

    private fun setupLoginButton() {
        binding?.loginButton?.setOnClickListener {
            val email = binding?.idTextInputEditText?.text?.toString()?.trim().orEmpty()
            val password = binding?.passwordTextInputEditText?.text?.toString().orEmpty()

            if (email.isBlank()) {
                binding?.idTextInputEditText?.error = "Email is required"
                return@setOnClickListener
            }
            if (password.isBlank()) {
                binding?.passwordTextInputEditText?.error = "Password is required"
                return@setOnClickListener
            }

            val role = if (selectedUserType == UserType.DRIVER) "driver" else "customer"
            setLoading(true)

            viewLifecycleOwner.lifecycleScope.launch {
                val authRepo = AuthRepository(requireContext())
                val userRepo = UserRepository(requireContext())

                when (val tokenResult = authRepo.loginJson(email, password, role)) {
                    is ApiResult.Error -> {
                        setLoading(false)
                        Toast.makeText(requireContext(), tokenResult.message, Toast.LENGTH_LONG).show()
                    }
                    is ApiResult.Success -> {
                        // Fetch profile
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

    private fun setLoading(loading: Boolean) {
        binding?.loginButton?.isEnabled = !loading
        binding?.loginButton?.text = if (loading) "Logging in…" else "Login"
    }

    private fun setupRegisterLink() {
        binding?.registerLinkTextView?.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
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
