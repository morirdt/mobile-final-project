package com.example.mobilefinalproject.ui.driver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.Delivery
import java.text.SimpleDateFormat

class FinderDeliveryAdapter(
    private val items: List<Delivery>
) : RecyclerView.Adapter<FinderDeliveryAdapter.FinderDeliveryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinderDeliveryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_driver_finder_delivery, parent, false)
        return FinderDeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: FinderDeliveryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class FinderDeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val customerNameTextView: TextView = itemView.findViewById(R.id.finder_delivery_customer_name_text_view)
        private val priceTextView: TextView = itemView.findViewById(R.id.finder_delivery_price_text_view)
        private val timeTextView: TextView = itemView.findViewById(R.id.finder_delivery_time_text_view)
        private val pickupAddressTextView: TextView = itemView.findViewById(R.id.finder_delivery_pickup_address_text_view)
        private val dropoffAddressTextView: TextView = itemView.findViewById(R.id.finder_delivery_dropoff_address_text_view)

        fun bind(item: Delivery) {
            customerNameTextView.text = item.customerName
            priceTextView.text = String.format("$%.2f", item.price)
            timeTextView.text = SimpleDateFormat("dd/MM/yyyy \u2022 HH:mm").format(item.date)
            pickupAddressTextView.text = item.pickupAddress
            dropoffAddressTextView.text = item.dropoffAddress
        }
    }
}


