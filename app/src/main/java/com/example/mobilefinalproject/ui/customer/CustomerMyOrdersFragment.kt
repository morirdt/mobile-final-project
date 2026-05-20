package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.adapters.CustomerDeliveryAdapter
import com.example.mobilefinalproject.databinding.FragmentCustomerMyOrdersBinding
import com.example.mobilefinalproject.network.dto.OrderRead
import com.example.mobilefinalproject.viewmodels.OrderViewModel

class CustomerMyOrdersFragment : Fragment() {

    private val orderViewModel: OrderViewModel by activityViewModels()
    private var binding: FragmentCustomerMyOrdersBinding? = null
    private var adapter: CustomerDeliveryAdapter? = null
    private var allOrders: List<OrderRead> = emptyList()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderViewModel.loadMyOrders()
    }

    override fun onResume() {
        super.onResume()
        orderViewModel.loadMyOrders()
    }

    private fun setupRecyclerView() {
        binding?.customerMyOrdersRecyclerView?.layoutManager = LinearLayoutManager(context)
        binding?.customerMyOrdersRecyclerView?.setHasFixedSize(true)

        adapter = CustomerDeliveryAdapter(
            onCancel = { order ->
                orderViewModel.cancelOrder(order.id)
            },
            onEdit = { order ->
                val action = CustomerMyOrdersFragmentDirections
                    .actionCustomerMyOrdersFragmentToCustomerEditOrderFragment(order.id)
                findNavController().navigate(action)
            },
            onDetails = { order ->
                com.example.mobilefinalproject.ui.dialogs.DeliveryDetailsDialog(requireContext()).show(
                    order = order,
                    showActions = false,
                    showDriverInfo = true
                )
            }
        )
        binding?.customerMyOrdersRecyclerView?.adapter = adapter

        orderViewModel.customerOrders.observe(viewLifecycleOwner) { orders ->
            allOrders = orders
            applyFilter()
        }
    }

    private fun setupStatusFilter() {
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.customer_order_status_filter_options,
            android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding?.customerMyOrdersStatusSpinner?.adapter = spinnerAdapter
        binding?.customerMyOrdersStatusSpinner?.setSelection(0, false)
        binding?.customerMyOrdersStatusSpinner?.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, pos: Int, id: Long) {
                    selectedStatusFilter = parent.getItemAtPosition(pos)?.toString().orEmpty()
                    applyFilter()
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                    selectedStatusFilter = ALL_STATUS_FILTER
                    applyFilter()
                }
            }
    }

    private fun applyFilter() {
        val filtered = when (selectedStatusFilter) {
            STATUS_PENDING     -> allOrders.filter { it.status == "pending" }
            STATUS_ACCEPTED    -> allOrders.filter { it.status == "accepted" }
            STATUS_IN_PROGRESS -> allOrders.filter { it.status == "in_progress" }
            STATUS_COMPLETED   -> allOrders.filter { it.status == "completed" }
            STATUS_CANCELLED   -> allOrders.filter { it.status == "cancelled" }
            else -> allOrders
        }
        adapter?.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val ALL_STATUS_FILTER = "All"
        private const val STATUS_PENDING     = "Pending"
        private const val STATUS_ACCEPTED    = "Accepted"
        private const val STATUS_IN_PROGRESS = "In Progress"
        private const val STATUS_COMPLETED   = "Completed"
        private const val STATUS_CANCELLED   = "Cancelled"
    }
}
