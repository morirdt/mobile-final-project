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
import com.example.mobilefinalproject.adapters.FinderDeliveryAdapter
import com.example.mobilefinalproject.databinding.FragmentDriverFinderBinding
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel

class DriverFinderFragment : Fragment() {
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()
    private var binding: FragmentDriverFinderBinding? = null
    private var adapter: FinderDeliveryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverFinderBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        val layout = LinearLayoutManager(context)
        binding?.driverFinderDeliveriesRecyclerView?.layoutManager = layout
        binding?.driverFinderDeliveriesRecyclerView?.setHasFixedSize(true)

//        binding?.progressBar?.visibility = View.VISIBLE

        adapter = FinderDeliveryAdapter(deliveryViewModel.pendingDeliveries.value)

        binding?.driverFinderDeliveriesRecyclerView?.adapter = adapter

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
        return deliveryViewModel.deliveries.observe(viewLifecycleOwner) {
            adapter?.deliveries = it
            adapter?.notifyDataSetChanged()
//            binding?.progressBar?.visibility = View.GONE
//            binding?.swipeRefresh?.isRefreshing = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.driverFinderDeliveriesRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        val mockFinderDeliveries = MockDeliveryDataSource.getPendingDeliveries()
        deliveryViewModel.setPendingDeliveries(mockFinderDeliveries)

        deliveryViewModel.pendingDeliveries.observe(viewLifecycleOwner) { deliveries ->
            binding?.driverFinderDeliveriesRecyclerView?.adapter = FinderDeliveryAdapter(deliveries)
        }
    }

}
