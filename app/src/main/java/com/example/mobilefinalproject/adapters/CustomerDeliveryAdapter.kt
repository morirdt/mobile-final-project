package com.example.mobilefinalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.Delivery
import java.text.SimpleDateFormat

class CustomerDeliveryAdapter(
    private val items: List<Delivery>
) : RecyclerView.Adapter<CustomerDeliveryAdapter.CustomerDeliveryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerDeliveryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer_delivery, parent, false)
        return CustomerDeliveryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CustomerDeliveryViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CustomerDeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val customerNameTextView: TextView =
            itemView.findViewById(R.id.customer_delivery_customer_name_text_view)
        private val priceTextView: TextView =
            itemView.findViewById(R.id.customer_delivery_price_text_view)
        private val timeTextView: TextView =
            itemView.findViewById(R.id.customer_delivery_time_text_view)
        private val pickupAddressTextView: TextView =
            itemView.findViewById(R.id.customer_delivery_pickup_address_text_view)
        private val dropoffAddressTextView: TextView =
            itemView.findViewById(R.id.customer_delivery_dropoff_address_text_view)

        fun bind(item: Delivery) {
            customerNameTextView.text = item.customerName
            priceTextView.text = String.format("$%.2f", item.price)
            timeTextView.text = SimpleDateFormat("dd/MM/yyyy \u2022 HH:mm").format(item.date)
            pickupAddressTextView.text = item.pickupAddress
            dropoffAddressTextView.text = item.dropoffAddress
        }
    }
}