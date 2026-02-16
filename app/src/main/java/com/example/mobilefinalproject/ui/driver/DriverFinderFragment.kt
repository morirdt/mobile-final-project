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
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel

class DriverFinderFragment : Fragment() {

    private val deliveryViewModel: DeliveryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_driver_finder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.finder_deliveries_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set mock data and observe deliveries LiveData
        val mockPendingDeliveries = MockDeliveryDataSource.getPendingDeliveries()
        deliveryViewModel.setDeliveries(mockPendingDeliveries)

        deliveryViewModel.deliveries.observe(viewLifecycleOwner) { deliveries ->
            recyclerView.adapter = FinderDeliveryAdapter(deliveries)
        }
    }

}
