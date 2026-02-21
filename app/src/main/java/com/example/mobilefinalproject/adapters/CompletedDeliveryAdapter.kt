package com.example.mobilefinalproject.ui.driver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.Delivery
import java.text.SimpleDateFormat

class CompletedDeliveryAdapter(
    private val items: List<Delivery>
) : RecyclerView.Adapter<CompletedDeliveryAdapter.CompletedDeliveryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedDeliveryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_driver_completed_delivery, parent, false)
        return CompletedDeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompletedDeliveryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CompletedDeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val customerNameTextView: TextView = itemView.findViewById(R.id.completed_delivery_customer_name_text_view)
        private val priceTextView: TextView = itemView.findViewById(R.id.completed_delivery_price_text_view)
        private val timeTextView: TextView = itemView.findViewById(R.id.completed_delivery_time_text_view)
        private val pickupAddressTextView: TextView = itemView.findViewById(R.id.completed_delivery_pickup_address_text_view)
        private val destinationAddressTextView: TextView = itemView.findViewById(R.id.completed_delivery_destination_address_text_view)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.completed_delivery_rating_bar)

        fun bind(item: Delivery) {
            customerNameTextView.text = item.customerName
            priceTextView.text = String.format("$%.2f", item.price)
            timeTextView.text = SimpleDateFormat("dd/MM/yyyy \u2022 HH:mm").format(item.date)
            pickupAddressTextView.text = item.pickupAddress
            destinationAddressTextView.text = item.destinationAddress
            ratingBar.rating = item.rating.toFloat()
        }
    }
}
