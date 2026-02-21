package com.example.mobilefinalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.ItemCustomerDeliveryBinding
import com.example.mobilefinalproject.models.Delivery
import java.text.SimpleDateFormat

class CustomerDeliveryAdapter(
    var deliveries: List<Delivery>?
) : RecyclerView.Adapter<CustomerDeliveryAdapter.CustomerDeliveryViewHolder>() {
    override fun getItemCount(): Int = deliveries?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerDeliveryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCustomerDeliveryBinding.inflate(inflater, parent, false)
        return CustomerDeliveryViewHolder(binding = binding)
    }

    override fun onBindViewHolder(
        holder: CustomerDeliveryViewHolder,
        position: Int
    ) {
        deliveries?.let {
            holder.bind(it[position], position)
        }
    }

    class CustomerDeliveryViewHolder(private val binding: ItemCustomerDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Delivery, position: Int) {
            binding.customerDeliveryNameTextView.text = item.customerName
            binding.customerDeliveryPriceTextView.text = String.format("$%.2f", item.price)
            binding.customerDeliveryDateTimeTextView.text =
                SimpleDateFormat("dd/MM/yyyy \u2022 HH:mm").format(item.date)
            binding.customerDeliveryPickupAddressTextView.text = item.pickupAddress
            binding.customerDeliveryDestinationAddressTextView.text = item.destinationAddress
        }
    }
}