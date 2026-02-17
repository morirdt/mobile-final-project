package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.FragmentCustomerMyOrdersBinding
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.ui.driver.FinderDeliveryAdapter
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel
import kotlin.getValue

class CustomerMyOrdersFragment : Fragment() {
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()
    private val customerViewModel: CustomerViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer_my_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.my_orders_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val customer = customerViewModel.customer.value
        val mockDeliveries = MockDeliveryDataSource.getDeliveriesByCustomer(customer?.id ?: "")
        deliveryViewModel.setCustomerDeliveries(mockDeliveries)

        deliveryViewModel.customerDeliveries.observe(viewLifecycleOwner) { deliveries ->
            recyclerView.adapter = FinderDeliveryAdapter(deliveries)
        }
    }
}
