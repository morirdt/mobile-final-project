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
import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.DeliveryStatus
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.models.driver.ButtonConfig
import com.example.mobilefinalproject.models.driver.activeDeliveryConfigs
import com.example.mobilefinalproject.ui.dialogs.DeliveryDetailsDialog
import java.text.SimpleDateFormat
import java.util.Locale

class ActiveDeliveryAdapter(var deliveries: List<Delivery>?) :
    RecyclerView.Adapter<ActiveDeliveryAdapter.ActiveDeliveryViewHolder>() {
    fun refreshFromSource() {
        deliveries = MockDeliveryDataSource.getActiveDeliveries()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = deliveries?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveDeliveryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDriverActiveDeliveryBinding.inflate(inflater, parent, false)
        return ActiveDeliveryViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: ActiveDeliveryViewHolder, position: Int) {
        deliveries?.let {
            holder.bind(it[position])
        }
    }

    class ActiveDeliveryViewHolder(private val binding: ItemDriverActiveDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(delivery: Delivery) {
            binding.activeDeliveryCustomerNameTextView.text = delivery.customerName
            binding.activeDeliveryStatusTextView.text = delivery.status
            binding.activeDeliveryPriceTextView.text = String.format(Locale.getDefault(), "$%.2f", delivery.price)
            binding.activeDeliveryMaterialCardView.strokeColor =
                activeDeliveryConfigs[delivery.status]?.strokeColor?.toColorInt()
                    ?: "#FFC107".toColorInt()

            binding.activeDeliveryMaterialCardView.strokeWidth = 2.dpToPx()

            binding.activeDeliveryStatusBadgeLinearLayout.setBackgroundResource(
                activeDeliveryConfigs[delivery.status]?.badgeDrawable ?: R.drawable.badge_pending
            )

            val pickupTime = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(delivery.date)
            binding.activeDeliveryTimeTextView.text = pickupTime
            binding.activeDeliveryPickupAddressTextView.text = delivery.pickupLocation.address
            binding.activeDeliveryDestinationAddressTextView.text = delivery.destinationLocation.address

            setupActionButtons(delivery)
        }

        private fun setupActionButtons(delivery: Delivery) {
            binding.activeDeliveryButtonsContainerLinearLayout.removeAllViews()
            val buttons: List<ButtonConfig> =
                activeDeliveryConfigs[delivery.status]?.buttons ?: emptyList()
            buttons.forEach { buttonConfig ->
                addButton(
                    delivery = delivery,
                    text = buttonConfig.text,
                    backgroundRes = buttonConfig.backgroundRes,
                    textColor = buttonConfig.textColor,
                    weight = buttonConfig.weight
                )
            }
        }

        private fun addButton(
            delivery: Delivery,
            text: String,
            backgroundRes: Int,
            textColor: Int,
            weight: Float
        ) {
            val button = LinearLayout(itemView.context).apply {
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT).apply {
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
                        text.equals("Start", ignoreCase = true) && delivery.status == DeliveryStatus.ACCEPTED.label -> {
                            MockDeliveryDataSource.updateDeliveryStatus(delivery.id, DeliveryStatus.IN_PROGRESS.label)
                            Toast.makeText(context, "Order started", Toast.LENGTH_SHORT).show()
                            (bindingAdapter as? ActiveDeliveryAdapter)?.refreshFromSource()
                        }
                        text.equals("Complete", ignoreCase = true) && delivery.status == DeliveryStatus.IN_PROGRESS.label -> {
                            MockDeliveryDataSource.updateDeliveryStatus(delivery.id, DeliveryStatus.COMPLETED.label)
                            Toast.makeText(context, "Order completed", Toast.LENGTH_SHORT).show()
                            (bindingAdapter as? ActiveDeliveryAdapter)?.refreshFromSource()
                        }
                        text.equals("Details", ignoreCase = true) -> {
                            DeliveryDetailsDialog(context).show(delivery, onStatusChanged = {
                                (bindingAdapter as? ActiveDeliveryAdapter)?.refreshFromSource()
                            })
                        }
                        text.equals("Cancel", ignoreCase = true) -> {
                            MockDeliveryDataSource.updateDeliveryStatus(delivery.id, DeliveryStatus.PENDING.label)
                            Toast.makeText(context, "Delivery cancelled", Toast.LENGTH_SHORT).show()
                            (bindingAdapter as? ActiveDeliveryAdapter)?.refreshFromSource()
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

        private fun Int.dpToPx(): Int {
            return (this * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}
