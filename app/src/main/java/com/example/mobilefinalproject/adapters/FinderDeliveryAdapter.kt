package com.example.mobilefinalproject.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.ItemDriverFinderDeliveryBinding
import com.example.mobilefinalproject.models.Delivery
import java.text.SimpleDateFormat

class FinderDeliveryAdapter(
    var deliveries: List<Delivery>?
) : RecyclerView.Adapter<FinderDeliveryAdapter.FinderDeliveryViewHolder>() {
    override fun getItemCount(): Int = deliveries?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinderDeliveryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDriverFinderDeliveryBinding.inflate(inflater, parent, false)
        return FinderDeliveryViewHolder(binding = binding)
    }

    override fun onBindViewHolder(
        holder: FinderDeliveryViewHolder,
        position: Int
    ) {
        deliveries?.let {
            holder.bind(it[position], position)
        }
    }

    class FinderDeliveryViewHolder(private val binding: ItemDriverFinderDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Delivery, position: Int) {
            binding.finderDeliveryCustomerNameTextView.text = item.customerName
            binding.finderDeliveryPriceTextView.text = String.format("$%.2f", item.price)
            binding.finderDeliveryTimeTextView.text = SimpleDateFormat("dd/MM/yyyy \u2022 HH:mm").format(item.date)
            binding.finderDeliveryPickupAddressTextView.text = item.pickupAddress
            binding.finderDeliveryDestinationAddressTextView.text = item.destinationAddress
        }
    }
}


