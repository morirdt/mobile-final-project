package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.adapters.CustomerDeliveryAdapter
import com.example.mobilefinalproject.databinding.FragmentCustomerMyOrdersBinding
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel
import kotlin.getValue

class CustomerMyOrdersFragment : Fragment() {
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()
    private val customerViewModel: CustomerViewModel by activityViewModels()

    private var binding: FragmentCustomerMyOrdersBinding? = null
    private var adapter: CustomerDeliveryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerMyOrdersBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        val layout = LinearLayoutManager(context)
        binding?.customerMyOrdersRecyclerView?.layoutManager = layout
        binding?.customerMyOrdersRecyclerView?.setHasFixedSize(true)

//        binding?.progressBar?.visibility = View.VISIBLE

        adapter = CustomerDeliveryAdapter(deliveryViewModel.customerDeliveries.value)

        binding?.customerMyOrdersRecyclerView?.adapter = adapter

//        binding?.swipeRefresh?.setOnRefreshListener {
//            binding?.swipeRefresh?.isRefreshing = true
//            refreshData()
//        }

        observeDeliveries()
    }

    private fun observeDeliveries() {

        return deliveryViewModel.customerDeliveries.observe(viewLifecycleOwner) {
            adapter?.deliveries = it
            adapter?.notifyDataSetChanged()
//            binding?.progressBar?.visibility = View.GONE
//            binding?.swipeRefresh?.isRefreshing = false
        }
    }

    private fun refreshData() {
        // TODO: Implement refresh logic
//        deliveryViewModel.refreshStudents()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding?.customerMyOrdersRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        val customer = customerViewModel.customer.value
        val mockDeliveries = MockDeliveryDataSource.getDeliveriesByCustomer(customer?.id ?: "")
        deliveryViewModel.setCustomerDeliveries(mockDeliveries)

        deliveryViewModel.customerDeliveries.observe(viewLifecycleOwner) { deliveries ->
            binding?.customerMyOrdersRecyclerView?.adapter = CustomerDeliveryAdapter(deliveries)
        }
    }

}
