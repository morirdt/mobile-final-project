package com.example.mobilefinalproject.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.databinding.ItemDriverCompletedDeliveryBinding
import com.example.mobilefinalproject.models.driver.activeDeliveryConfigs
import com.example.mobilefinalproject.network.dto.OrderRead
import java.text.SimpleDateFormat
import java.util.Locale

class CompletedDeliveryAdapter(
    var orders: List<OrderRead> = emptyList()
) : RecyclerView.Adapter<CompletedDeliveryAdapter.CompletedDeliveryViewHolder>() {

    fun submitList(newOrders: List<OrderRead>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = orders.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedDeliveryViewHolder {
        val binding = ItemDriverCompletedDeliveryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CompletedDeliveryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompletedDeliveryViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    class CompletedDeliveryViewHolder(private val binding: ItemDriverCompletedDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderRead) {
            binding.completedDeliveryCustomerNameTextView.text = "Customer #${order.customerId}"
            binding.completedDeliveryPriceTextView.text =
                String.format(Locale.getDefault(), "$%.2f", order.priceCents / 100.0)

            val timeDisplay = try {
                val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val displayFmt = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
                val parsed = isoFmt.parse(order.createdAt.take(19))
                if (parsed != null) displayFmt.format(parsed) else order.createdAt
            } catch (_: Exception) { order.createdAt }
            binding.completedDeliveryTimeTextView.text = timeDisplay

            binding.completedDeliveryPickupAddressTextView.text = order.pickupAddress
            binding.completedDeliveryDestinationAddressTextView.text = order.dropoffAddress

            binding.completedDeliveryCardView.strokeColor =
                activeDeliveryConfigs[order.status]?.strokeColor?.toColorInt() ?: "#388E3C".toColorInt()
            binding.completedDeliveryCardView.strokeWidth = 2.dpToPx()
        }

        private fun Int.dpToPx(): Int =
            (this * itemView.context.resources.displayMetrics.density).toInt()
    }
}
