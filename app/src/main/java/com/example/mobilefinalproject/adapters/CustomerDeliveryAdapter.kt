package com.example.mobilefinalproject.adapters

import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.DialogDeliveryImagePreviewBinding
import com.example.mobilefinalproject.databinding.ItemCustomerDeliveryBinding
import com.example.mobilefinalproject.models.driver.activeDeliveryConfigs
import com.example.mobilefinalproject.network.dto.OrderRead
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class CustomerDeliveryAdapter(
    var orders: List<OrderRead> = emptyList(),
    private val onCancel: ((OrderRead) -> Unit)? = null,
    private val onEdit: ((OrderRead) -> Unit)? = null,
    private val onDetails: ((OrderRead) -> Unit)? = null
) : RecyclerView.Adapter<CustomerDeliveryAdapter.CustomerDeliveryViewHolder>() {

    fun submitList(newOrders: List<OrderRead>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = orders.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerDeliveryViewHolder {
        val binding = ItemCustomerDeliveryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CustomerDeliveryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerDeliveryViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    inner class CustomerDeliveryViewHolder(private val binding: ItemCustomerDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderRead) {
            val status = order.status

            // Driver name not in OrderRead directly; show driver ID if assigned
            binding.customerDeliveryDriverNameTextView.text =
                if (order.driverId != null) "Driver #${order.driverId}" else "Not Assigned"

            binding.customerDeliveryPriceTextView.text =
                String.format(Locale.getDefault(), "$%.2f", order.priceCents / 100.0)

            val timeDisplay = try {
                val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val displayFmt = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
                val parsed = isoFmt.parse(order.createdAt.take(19))
                if (parsed != null) displayFmt.format(parsed) else order.createdAt
            } catch (_: Exception) { order.createdAt }
            binding.customerDeliveryDateTimeTextView.text = timeDisplay

            binding.customerDeliveryPickupAddressTextView.text = order.pickupAddress
            binding.customerDeliveryDestinationAddressTextView.text = order.dropoffAddress

            binding.customerDeliveryStatusTextView.text = status
            val badgeDrawable = activeDeliveryConfigs[status]?.badgeDrawable ?: R.drawable.badge_pending
            binding.customerDeliveryStatusBadgeLinearLayout.setBackgroundResource(badgeDrawable)

            binding.customerDeliveryCardView.strokeColor =
                activeDeliveryConfigs[status]?.strokeColor?.toColorInt() ?: "#FFC107".toColorInt()
            binding.customerDeliveryCardView.strokeWidth = 2.dpToPx()

            binding.customerDeliveryImageView.setOnClickListener {
                showImagePreview(order)
            }

            setupActionButtons(order)
        }

        private fun setupActionButtons(order: OrderRead) {
            binding.customerDeliveryButtonsContainerLinearLayout.removeAllViews()

            if (order.status == "pending") {
                addButton(order, "Edit",    R.drawable.button_blue,         Color.WHITE,            0.33f)
                addButton(order, "Details", R.drawable.button_light_purple, "#FF9C27B0".toColorInt(), 0.33f)
                addButton(order, "Cancel",  R.drawable.button_red,          Color.WHITE,            0.33f)
            } else {
                addButton(order, "Details", R.drawable.button_light_purple, "#FF9C27B0".toColorInt(), 1f)
            }
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
                        text.equals("Cancel", ignoreCase = true) -> {
                            onCancel?.invoke(order)
                            Toast.makeText(itemView.context, "Order cancelled", Toast.LENGTH_SHORT).show()
                        }
                        text.equals("Edit", ignoreCase = true) -> onEdit?.invoke(order)
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
            binding.customerDeliveryButtonsContainerLinearLayout.addView(button)
        }

        private fun showImagePreview(order: OrderRead) {
            val dialog = Dialog(binding.root.context)
            val previewBinding = DialogDeliveryImagePreviewBinding.inflate(
                LayoutInflater.from(binding.root.context)
            )
            dialog.setContentView(previewBinding.root)
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            if (!order.cargoImageUrl.isNullOrBlank()) {
                Picasso.get()
                    .load(order.cargoImageUrl)
                    .into(previewBinding.deliveryImagePreviewImageView)
            } else {
                previewBinding.deliveryImagePreviewImageView.setImageResource(R.drawable.ic_placeholder_image)
            }
            dialog.show()
        }

        private fun Int.dpToPx(): Int =
            (this * itemView.context.resources.displayMetrics.density).toInt()
    }
}
