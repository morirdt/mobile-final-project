package com.example.mobilefinalproject.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.databinding.ItemDriverCompletedDeliveryBinding
import com.example.mobilefinalproject.models.Delivery
import java.text.SimpleDateFormat
import java.util.Locale

class CompletedDeliveryAdapter(
    var deliveries: List<Delivery>?
) : RecyclerView.Adapter<CompletedDeliveryAdapter.CompletedDeliveryViewHolder>() {

    override fun getItemCount(): Int = deliveries?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedDeliveryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDriverCompletedDeliveryBinding.inflate(inflater, parent, false)
        return CompletedDeliveryViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: CompletedDeliveryViewHolder, position: Int) {
        deliveries?.let {
            holder.bind(it[position])
        }
    }

    class CompletedDeliveryViewHolder(private val binding: ItemDriverCompletedDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Delivery) {
            binding.completedDeliveryCustomerNameTextView.text = item.customerName
            binding.completedDeliveryPriceTextView.text = String.format(Locale.getDefault(), "$%.2f", item.price)
            binding.completedDeliveryTimeTextView.text =
                SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(item.date)
            binding.completedDeliveryPickupAddressTextView.text = item.pickupLocation.address
            binding.completedDeliveryDestinationAddressTextView.text = item.destinationLocation.address
        }
    }
}
