package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mobilefinalproject.databinding.FragmentCustomerMyOrdersBinding

class CustomerMyOrdersFragment : Fragment() {

    private var binding: FragmentCustomerMyOrdersBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerMyOrdersBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Implement My Orders fragment content
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
