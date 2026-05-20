package com.example.mobilefinalproject.ui.driver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilefinalproject.adapters.CompletedDeliveryAdapter
import com.example.mobilefinalproject.databinding.FragmentDriverCompletedDeliveriesBinding
import com.example.mobilefinalproject.viewmodels.OrderViewModel

class DriverCompletedDeliveriesFragment : Fragment() {
    private val orderViewModel: OrderViewModel by activityViewModels()
    private var binding: FragmentDriverCompletedDeliveriesBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverCompletedDeliveriesBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.driverCompletedDeliveriesRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        orderViewModel.completedOrders.observe(viewLifecycleOwner) { orders ->
            binding?.driverCompletedDeliveriesRecyclerView?.adapter = CompletedDeliveryAdapter(orders)
        }
    }

    override fun onResume() {
        super.onResume()
        orderViewModel.loadCompletedDriverOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
