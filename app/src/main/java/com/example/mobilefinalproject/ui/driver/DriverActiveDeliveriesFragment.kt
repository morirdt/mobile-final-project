package com.example.mobilefinalproject.ui.driver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilefinalproject.adapters.ActiveDeliveryAdapter
import com.example.mobilefinalproject.databinding.FragmentDriverActiveDeliveriesBinding
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel

class DriverActiveDeliveriesFragment : Fragment() {
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()
    private var binding: FragmentDriverActiveDeliveriesBinding? = null
    private var adapter: ActiveDeliveryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverActiveDeliveriesBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        val layout = LinearLayoutManager(context)
        binding?.driverActiveDeliveriesRecyclerView?.layoutManager = layout
        binding?.driverActiveDeliveriesRecyclerView?.setHasFixedSize(true)

//        binding?.progressBar?.visibility = View.VISIBLE

        adapter = ActiveDeliveryAdapter(deliveryViewModel.activeDeliveries.value)

        binding?.driverActiveDeliveriesRecyclerView?.adapter = adapter

//        binding?.swipeRefresh?.setOnRefreshListener {
//            binding?.swipeRefresh?.isRefreshing = true
//            refreshData()
//        }

        observeDeliveries()
    }

    private fun refreshData() {
        // TODO: Implement refresh logic
//        deliveryViewModel.refreshStudents()
    }

    private fun observeDeliveries() {

        return deliveryViewModel.activeDeliveries.observe(viewLifecycleOwner) {
            adapter?.deliveries = it
            adapter?.notifyDataSetChanged()
//            binding?.progressBar?.visibility = View.GONE
//            binding?.swipeRefresh?.isRefreshing = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.driverActiveDeliveriesRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        val mockActiveDeliveries = MockDeliveryDataSource.getActiveDeliveries()
        deliveryViewModel.setActiveDeliveries(mockActiveDeliveries)

        deliveryViewModel.activeDeliveries.observe(viewLifecycleOwner) { deliveries ->
            binding?.driverActiveDeliveriesRecyclerView?.adapter = ActiveDeliveryAdapter(deliveries)
        }
    }
}


