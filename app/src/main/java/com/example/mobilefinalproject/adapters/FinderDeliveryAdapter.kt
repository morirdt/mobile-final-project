package com.example.mobilefinalproject.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.ItemDriverFinderDeliveryBinding
import com.example.mobilefinalproject.network.dto.OrderRead
import java.text.SimpleDateFormat
import java.util.Locale

class FinderDeliveryAdapter(
    var orders: List<OrderRead> = emptyList(),
    private val onAccept: ((OrderRead) -> Unit)? = null,
    private val onDetails: ((OrderRead) -> Unit)? = null
) : RecyclerView.Adapter<FinderDeliveryAdapter.FinderDeliveryViewHolder>() {

    fun submitList(newOrders: List<OrderRead>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = orders.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinderDeliveryViewHolder {
        val binding = ItemDriverFinderDeliveryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FinderDeliveryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FinderDeliveryViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    inner class FinderDeliveryViewHolder(private val binding: ItemDriverFinderDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderRead) {
            binding.finderDeliveryCustomerNameTextView.text = "Customer #${order.customerId}"
            binding.finderDeliveryPriceTextView.text =
                String.format(Locale.getDefault(), "$%.2f", order.priceCents / 100.0)

            val timeDisplay = try {
                val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val displayFmt = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
                val parsed = isoFmt.parse(order.createdAt.take(19))
                if (parsed != null) displayFmt.format(parsed) else order.createdAt
            } catch (_: Exception) { order.createdAt }
            binding.finderDeliveryTimeTextView.text = timeDisplay

            binding.finderDeliveryPickupAddressTextView.text = order.pickupAddress
            binding.finderDeliveryDestinationAddressTextView.text = order.dropoffAddress

            binding.finderDeliveryCardView.strokeColor = Color.parseColor("#FF9800")
            binding.finderDeliveryCardView.strokeWidth = 2.dpToPx()

            setupActionButtons(order)
        }

        private fun setupActionButtons(order: OrderRead) {
            binding.finderDeliveryButtonsContainerLinearLayout.removeAllViews()
            addButton(order, "Accept",  R.drawable.button_blue,         Color.WHITE,            0.5f)
            addButton(order, "Details", R.drawable.button_light_purple, "#FF9C27B0".toColorInt(), 0.5f)
        }

        private fun addButton(
            order: OrderRead, text: String, backgroundRes: Int,
            textColor: Int = Color.WHITE, weight: Float = 1f
        ) {
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
                        text.equals("Accept", ignoreCase = true) -> onAccept?.invoke(order)
                        text.equals("Details", ignoreCase = true) -> onDetails?.invoke(order)
                    }
                }
            }
            val textView = TextView(itemView.context).apply {
                this.text = text
                setTextColor(textColor)
                textSize = 12f
            }
            button.addView(textView)
            binding.finderDeliveryButtonsContainerLinearLayout.addView(button)
        }

        private fun Int.dpToPx(): Int =
            (this * itemView.context.resources.displayMetrics.density).toInt()
    }
}
