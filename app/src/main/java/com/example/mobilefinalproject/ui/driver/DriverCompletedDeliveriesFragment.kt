package com.example.mobilefinalproject.ui.driver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.FragmentDriverCompletedDeliveriesBinding
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel

class DriverCompletedDeliveriesFragment : Fragment() {
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()
    private var binding: FragmentDriverCompletedDeliveriesBinding? = null
    private var adapter: CompletedDeliveryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverCompletedDeliveriesBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        val layout = LinearLayoutManager(context)
        binding?.driverCompletedDeliveriesRecyclerView?.layoutManager = layout
        binding?.driverCompletedDeliveriesRecyclerView?.setHasFixedSize(true)

//        binding?.progressBar?.visibility = View.VISIBLE

        adapter = CompletedDeliveryAdapter(deliveryViewModel.completedDeliveries.value)

        binding?.driverCompletedDeliveriesRecyclerView?.adapter = adapter

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
        return deliveryViewModel.completedDeliveries.observe(viewLifecycleOwner) {
            adapter?.deliveries = it
            adapter?.notifyDataSetChanged()
//            binding?.progressBar?.visibility = View.GONE
//            binding?.swipeRefresh?.isRefreshing = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.driverCompletedDeliveriesRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        val mockCompletedDeliveries = MockDeliveryDataSource.getCompletedDeliveries()
        deliveryViewModel.setCompletedDeliveries(mockCompletedDeliveries)

        deliveryViewModel.completedDeliveries.observe(viewLifecycleOwner) { deliveries ->
            binding?.driverCompletedDeliveriesRecyclerView?.adapter = CompletedDeliveryAdapter(deliveries)
        }
    }
}
