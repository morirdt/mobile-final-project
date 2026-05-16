package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilefinalproject.adapters.CustomerDeliveryAdapter
import com.example.mobilefinalproject.databinding.FragmentCustomerMyOrdersBinding
import com.example.mobilefinalproject.models.DeliveryStatus
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel
import kotlin.getValue

class CustomerMyOrdersFragment : Fragment() {
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()
    private val customerViewModel: CustomerViewModel by activityViewModels()

    private var binding: FragmentCustomerMyOrdersBinding? = null
    private var adapter: CustomerDeliveryAdapter? = null
    private var allCustomerDeliveries: List<com.example.mobilefinalproject.models.Delivery> = emptyList()
    private var selectedStatusFilter: String = ALL_STATUS_FILTER

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerMyOrdersBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupStatusFilter()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        binding?.customerMyOrdersRecyclerView?.layoutManager = LinearLayoutManager(context)
        binding?.customerMyOrdersRecyclerView?.setHasFixedSize(true)
        adapter = CustomerDeliveryAdapter(emptyList()) {
            // When delivery status changed (e.g., cancelled), refresh data
            refreshData()
        }
        binding?.customerMyOrdersRecyclerView?.adapter = adapter
        observeDeliveries()
    }

    private fun setupStatusFilter() {
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            com.example.mobilefinalproject.R.array.customer_order_status_filter_options,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding?.customerMyOrdersStatusSpinner?.adapter = spinnerAdapter
        binding?.customerMyOrdersStatusSpinner?.setSelection(0, false)
        binding?.customerMyOrdersStatusSpinner?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedStatusFilter = parent.getItemAtPosition(position)?.toString().orEmpty()
                applyFilter()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                selectedStatusFilter = ALL_STATUS_FILTER
                applyFilter()
            }
        }
    }

    private fun observeDeliveries() {
        deliveryViewModel.customerDeliveries.observe(viewLifecycleOwner) { deliveries ->
            allCustomerDeliveries = deliveries
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filteredDeliveries = when (selectedStatusFilter) {
            STATUS_PENDING -> allCustomerDeliveries.filter { it.status == DeliveryStatus.PENDING.label }
            STATUS_ACCEPTED -> allCustomerDeliveries.filter { it.status == DeliveryStatus.ACCEPTED.label }
            STATUS_IN_PROGRESS -> allCustomerDeliveries.filter { it.status == DeliveryStatus.IN_PROGRESS.label }
            STATUS_COMPLETED -> allCustomerDeliveries.filter { it.status == DeliveryStatus.COMPLETED.label }
            STATUS_CANCELLED -> allCustomerDeliveries.filter { it.status == DeliveryStatus.CANCELLED.label }
            else -> allCustomerDeliveries
        }

        adapter?.submitList(filteredDeliveries)
    }

    private fun refreshData() {
        val customer = customerViewModel.customer.value
        val mockDeliveries = MockDeliveryDataSource.getDeliveriesByCustomer(customer?.id ?: "")
        deliveryViewModel.setCustomerDeliveries(mockDeliveries)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val ALL_STATUS_FILTER = "All"
        private const val STATUS_PENDING = "Pending"
        private const val STATUS_ACCEPTED = "Accepted"
        private const val STATUS_IN_PROGRESS = "In Progress"
        private const val STATUS_COMPLETED = "Completed"
        private const val STATUS_CANCELLED = "Cancelled"
    }
}
