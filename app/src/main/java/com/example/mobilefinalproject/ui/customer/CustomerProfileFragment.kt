package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.customer.Customer
import com.example.mobilefinalproject.viewmodels.CustomerViewModel


class CustomerProfileFragment : Fragment() {
    private val customerViewModel: CustomerViewModel by activityViewModels()
    private var customer: Customer? = null

    private lateinit var customerNameTextView: TextView
    private lateinit var customerIdTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customerNameTextView = view.findViewById(R.id.customer_name_text_view)
        customerIdTextView = view.findViewById(R.id.customer_id_text_view)
        editProfileButton = view.findViewById(R.id.edit_profile_icon_button
        )
        logoutButton = view.findViewById(R.id.logout_button)

        customerViewModel.customer.observe(viewLifecycleOwner) { customer ->
            this.customer = customer
            updateUI()
        }

        // Setup edit profile button click listener
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_customerProfileFragment_to_customerEditProfileFragment)
        }

        // Setup logout button click listener
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun updateUI() {
        customerNameTextView.text = this.customer?.fullName
        customerIdTextView.text = this.customer?.id
    }

    private fun logout() {
        customerViewModel.clearCustomer()
        val parentNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        parentNavController.navigate(R.id.action_global_loginFragment)
    }
}
