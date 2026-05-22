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
import com.example.mobilefinalproject.session.UserSessionManager
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.databinding.FragmentCustomerProfileBinding

class CustomerProfileFragment : Fragment() {
    private val customerViewModel: CustomerViewModel by activityViewModels()
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

        customerViewModel.userMe.observe(viewLifecycleOwner) { user ->
            binding?.customerProfileNameTextView?.text = user?.fullName
            binding?.customerProfileIdTextView?.text = user?.id?.toString()
        }

        binding?.customerProfileEditButton?.setOnClickListener {
            findNavController().navigate(R.id.action_customerProfileFragment_to_customerEditProfileFragment)
        }

        binding?.customerProfileLogoutButton?.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        customerViewModel.clearCustomer()
        UserSessionManager.clearSession(requireContext())
        val parentNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        parentNavController.navigate(R.id.action_global_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
