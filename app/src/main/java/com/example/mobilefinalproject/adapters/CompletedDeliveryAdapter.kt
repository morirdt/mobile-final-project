package com.example.mobilefinalproject.ui.driver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.ItemDriverCompletedDeliveryBinding
import com.example.mobilefinalproject.models.Delivery
import java.text.SimpleDateFormat

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
            holder.bind(it[position], position)
        }
    }

    class CompletedDeliveryViewHolder(private val binding: ItemDriverCompletedDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Delivery, position: Int) {
            binding.completedDeliveryCustomerNameTextView.text = item.customerName
            binding.completedDeliveryPriceTextView.text = String.format("$%.2f", item.price)
            binding.completedDeliveryTimeTextView.text =
                SimpleDateFormat("dd/MM/yyyy \u2022 HH:mm").format(item.date)
            binding.completedDeliveryPickupAddressTextView.text = item.pickupAddress
            binding.completedDeliveryDestinationAddressTextView.text = item.destinationAddress
            binding.completedDeliveryRatingBar.rating = item.rating.toFloat()


        }
    }
}
