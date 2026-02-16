package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mobilefinalproject.databinding.FragmentCustomerNewOrderBinding

class CustomerNewOrderFragment : Fragment() {

    private var binding: FragmentCustomerNewOrderBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerNewOrderBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Implement New Order fragment content
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
