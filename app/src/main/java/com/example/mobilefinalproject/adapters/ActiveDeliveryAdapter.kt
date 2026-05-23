package com.example.mobilefinalproject.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.ItemDriverActiveDeliveryBinding
import com.example.mobilefinalproject.models.driver.activeDeliveryConfigs
import com.example.mobilefinalproject.network.dto.OrderRead
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.mobilefinalproject.models.toStatusLabel

class ActiveDeliveryAdapter(
    var orders: List<OrderRead> = emptyList(),
    private val onStart: ((OrderRead) -> Unit)? = null,
    private val onComplete: ((OrderRead) -> Unit)? = null,
    private val onDetails: ((OrderRead) -> Unit)? = null,
    private val onCancel: ((OrderRead) -> Unit)? = null
) : RecyclerView.Adapter<ActiveDeliveryAdapter.ActiveDeliveryViewHolder>() {

    fun submitList(newOrders: List<OrderRead>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = orders.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveDeliveryViewHolder {
        val binding = ItemDriverActiveDeliveryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ActiveDeliveryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActiveDeliveryViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    inner class ActiveDeliveryViewHolder(private val binding: ItemDriverActiveDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderRead) {
            val status = order.status
            // display a user-friendly title-case label (e.g. "in_progress" -> "In Progress")
            binding.activeDeliveryStatusTextView.text = status.toStatusLabel()
            binding.activeDeliveryPriceTextView.text =
                String.format(Locale.getDefault(), "$%.2f", order.priceCents / 100.0)

            // Customer name not directly available in OrderRead; show customer ID as fallback
            binding.activeDeliveryCustomerNameTextView.text = "Customer #${order.customerId}"

            binding.activeDeliveryMaterialCardView.strokeColor =
                activeDeliveryConfigs[status]?.strokeColor?.toColorInt() ?: "#FFC107".toColorInt()
            binding.activeDeliveryMaterialCardView.strokeWidth = 2.dpToPx()

            binding.activeDeliveryStatusBadgeLinearLayout.setBackgroundResource(
                activeDeliveryConfigs[status]?.badgeDrawable ?: R.drawable.badge_pending
            )

            // Use createdAt as the display timestamp
            val timeDisplay = try {
                val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val displayFmt = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
                val parsed = isoFmt.parse(order.createdAt.take(19))
                if (parsed != null) displayFmt.format(parsed) else order.createdAt
            } catch (_: Exception) { order.createdAt }
            binding.activeDeliveryTimeTextView.text = timeDisplay

            binding.activeDeliveryPickupAddressTextView.text = order.pickupAddress
            binding.activeDeliveryDestinationAddressTextView.text = order.dropoffAddress

            setupActionButtons(order)
        }

        private fun setupActionButtons(order: OrderRead) {
            binding.activeDeliveryButtonsContainerLinearLayout.removeAllViews()
            val buttons = activeDeliveryConfigs[order.status]?.buttons ?: emptyList()
            buttons.forEach { buttonConfig ->
                addButton(
                    order = order,
                    text = buttonConfig.text,
                    backgroundRes = buttonConfig.backgroundRes,
                    textColor = buttonConfig.textColor,
                    weight = buttonConfig.weight
                )
            }
        }

        private fun addButton(order: OrderRead, text: String, backgroundRes: Int, textColor: Int, weight: Float) {
            val button = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    this.weight = weight
                    marginEnd = 4.dpToPx()
                    marginStart = 4.dpToPx()
                }
                setBackgroundResource(backgroundRes)
                gravity = android.view.Gravity.CENTER
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    when {
                        text.equals("Start", ignoreCase = true) -> {
                            onStart?.invoke(order)
                            Toast.makeText(context, "Order started", Toast.LENGTH_SHORT).show()
                        }
                        text.equals("Complete", ignoreCase = true) -> {
                            onComplete?.invoke(order)
                            Toast.makeText(context, "Order completed", Toast.LENGTH_SHORT).show()
                        }
                        text.equals("Details", ignoreCase = true) -> onDetails?.invoke(order)
                        text.equals("Cancel", ignoreCase = true) -> {
                            onCancel?.invoke(order)
                            Toast.makeText(context, "Order cancelled", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            val textView = TextView(itemView.context).apply {
                this.text = text
                setTextColor(textColor)
                textSize = 12f
            }
            button.addView(textView)
            binding.activeDeliveryButtonsContainerLinearLayout.addView(button)
        }

        private fun Int.dpToPx(): Int =
            (this * itemView.context.resources.displayMetrics.density).toInt()
    }
}
