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
import com.example.mobilefinalproject.ui.common.LoadingOverlayController
import com.example.mobilefinalproject.viewmodels.OrderViewModel

class DriverActiveDeliveriesFragment : Fragment() {
    private val orderViewModel: OrderViewModel by activityViewModels()
    private var binding: FragmentDriverActiveDeliveriesBinding? = null
    private var loadingOverlay: LoadingOverlayController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverActiveDeliveriesBinding.inflate(inflater, container, false)
        loadingOverlay = LoadingOverlayController(
            requireContext(),
            requireActivity().findViewById(android.R.id.content)
        )
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.driverActiveDeliveriesRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        orderViewModel.activeOrders.observe(viewLifecycleOwner) { orders ->
            binding?.driverActiveDeliveriesRecyclerView?.adapter = ActiveDeliveryAdapter(
                orders,
                onStart = { order -> orderViewModel.startOrder(order.id) },
                onComplete = { order -> orderViewModel.completeOrderOptimistic(order) },
                onDetails = { order ->
                    com.example.mobilefinalproject.ui.dialogs.DeliveryDetailsDialog(requireContext()).show(
                        order = order,
                        showActions = false,
                        showDriverInfo = false
                    )
                },
                onCancel = { order -> orderViewModel.cancelOrder(order.id) }
            )
        }

        orderViewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) loadingOverlay?.show() else loadingOverlay?.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        orderViewModel.loadActiveDriverOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingOverlay?.detach()
        loadingOverlay = null
        binding = null
    }
}
