package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.FragmentCustomerHomeBinding
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.viewmodels.OrderViewModel
import android.widget.Toast

class CustomerHomeFragment : Fragment() {

    private val customerViewModel: CustomerViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private var binding: FragmentCustomerHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.customerHomeNewOrderButton?.setOnClickListener {
            findNavController().navigate(R.id.action_customerHomeFragment_to_customerNewOrderFragment)
        }

        binding?.customerHomeMyOrdersButton?.setOnClickListener {
            findNavController().navigate(R.id.action_customerHomeFragment_to_customerMyOrdersFragment)
        }

        customerViewModel.userMe.observe(viewLifecycleOwner) { user ->
            val name = user?.fullName ?: "Customer"
            binding?.customerHomeTitleTextView?.text =
                getString(R.string.customer_home_greeting, name)
        }

        orderViewModel.customerOrders.observe(viewLifecycleOwner) { orders ->
            val pendingCount = orders.count { it.status == "pending" }
            val activeCount  = orders.count { it.status == "accepted" || it.status == "in_progress" || it.status == "picked_up" }
            binding?.customerHomePendingCountTextView?.text = pendingCount.toString()
            binding?.customerHomeActiveCountTextView?.text  = activeCount.toString()
        }

        orderViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                orderViewModel.clearError()
            }
        }

        customerViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                customerViewModel.clearError()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Load profile if not loaded yet
        if (customerViewModel.userMe.value == null) customerViewModel.loadMe()
        orderViewModel.loadMyOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
