package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.databinding.FragmentCustomerHomeBinding
import com.example.mobilefinalproject.models.DeliveryStatus
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel

class CustomerHomeFragment : Fragment() {

    private val customerViewModel: CustomerViewModel by activityViewModels()
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()
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
            findNavController().navigate(com.example.mobilefinalproject.R.id.customerNewOrderFragment)
        }

        binding?.customerHomeMyOrdersButton?.setOnClickListener {
            findNavController().navigate(com.example.mobilefinalproject.R.id.customerMyOrdersFragment)
        }

        customerViewModel.customer.observe(viewLifecycleOwner) { customer ->
            val name = customer?.fullName ?: "Customer"
            binding?.customerHomeTitleTextView?.text = getString(com.example.mobilefinalproject.R.string.customer_home_greeting, name)
            refreshCounters()
        }

    }

    override fun onResume() {
        super.onResume()
        refreshCounters()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun refreshCounters() {
        val customerId = customerViewModel.customer.value?.id.orEmpty()
        val customerOrders = MockDeliveryDataSource.getDeliveriesByCustomer(customerId)

        deliveryViewModel.setCustomerDeliveries(customerOrders)

        val pendingCount = customerOrders.count { it.status == DeliveryStatus.PENDING.label }
        val activeCount = customerOrders.count {
            it.status == DeliveryStatus.ACCEPTED.label || it.status == DeliveryStatus.IN_PROGRESS.label
        }

        binding?.customerHomePendingCountTextView?.text = pendingCount.toString()
        binding?.customerHomeActiveCountTextView?.text = activeCount.toString()
    }
}
