package com.example.mobilefinalproject.adapters

import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
// ...existing imports...
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.DialogDeliveryImagePreviewBinding
import com.example.mobilefinalproject.databinding.ItemCustomerDeliveryBinding
import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.DeliveryStatus
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.models.driver.activeDeliveryConfigs
import com.example.mobilefinalproject.ui.dialogs.DeliveryDetailsDialog
import java.text.SimpleDateFormat
import java.util.Locale

class CustomerDeliveryAdapter(
    var deliveries: List<Delivery>?,
    private val onDeliveryStatusChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<CustomerDeliveryAdapter.CustomerDeliveryViewHolder>() {
    fun submitList(newDeliveries: List<Delivery>) {
        deliveries = newDeliveries
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = deliveries?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerDeliveryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCustomerDeliveryBinding.inflate(inflater, parent, false)
        return CustomerDeliveryViewHolder(binding = binding)
    }

    override fun onBindViewHolder(
        holder: CustomerDeliveryViewHolder,
        position: Int
    ) {
        deliveries?.let {
            holder.bind(it[position])
        }
    }

    class CustomerDeliveryViewHolder(private val binding: ItemCustomerDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Delivery) {
            binding.customerDeliveryDriverNameTextView.text = item.driverName
            binding.customerDeliveryPriceTextView.text = String.format(Locale.getDefault(), "$%.2f", item.price)
            binding.customerDeliveryDateTimeTextView.text =
                SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(item.date)
            binding.customerDeliveryPickupAddressTextView.text = item.pickupLocation.address
            binding.customerDeliveryDestinationAddressTextView.text = item.destinationLocation.address

            // Set status text and badge background to match driver UI
            binding.customerDeliveryStatusTextView.text = item.status

            // Apply badge drawable based on status
            val badgeDrawable = activeDeliveryConfigs[item.status]?.badgeDrawable ?: com.example.mobilefinalproject.R.drawable.badge_pending
            binding.customerDeliveryStatusBadgeLinearLayout.setBackgroundResource(badgeDrawable)

            // Set card border color based on status
            binding.customerDeliveryCardView.strokeColor =
                activeDeliveryConfigs[item.status]?.strokeColor?.toColorInt() ?: "#FFC107".toColorInt()
            binding.customerDeliveryCardView.strokeWidth = 2.dpToPx()

            binding.customerDeliveryImageView.setOnClickListener {
                showImagePreview()
            }

            setupActionButtons(item)
        }

        private fun setupActionButtons(delivery: Delivery) {
            binding.customerDeliveryButtonsContainerLinearLayout.removeAllViews()

            if (delivery.status == DeliveryStatus.PENDING.label) {

                addButton(
                    delivery = delivery,
                    text = "Edit",
                    backgroundRes = R.drawable.button_blue,
                    weight = 0.33f
                )

                addButton(
                    delivery = delivery,
                    text = "Details",
                    backgroundRes = R.drawable.button_light_purple,
                    textColor = "#FF9C27B0".toColorInt(),
                    weight = 0.33f
                )

                addButton(
                    delivery = delivery,
                    text = "Cancel",
                    backgroundRes = R.drawable.button_red,
                    weight = 0.33f
                )


            } else {
                addButton(
                    delivery = delivery,
                    text = "Details",
                    textColor = "#FF9C27B0".toColorInt(),
                    backgroundRes = R.drawable.button_light_purple,
                    weight = 1f
                )
            }
        }

        private fun addButton(
            delivery: Delivery,
            text: String,
            backgroundRes: Int,
            textColor: Int = Color.WHITE,
            weight: Float = 1f
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
                        text.equals("Cancel", ignoreCase = true) -> {
                            MockDeliveryDataSource.updateDeliveryStatus(delivery.id, DeliveryStatus.CANCELLED.label)
                            Toast.makeText(itemView.context, "Delivery cancelled", Toast.LENGTH_SHORT).show()
                            // Notify fragment to refresh data
                            (bindingAdapter as? CustomerDeliveryAdapter)?.onDeliveryStatusChanged?.invoke()
                        }
                        text.equals("Edit", ignoreCase = true) -> {
                            Toast.makeText(itemView.context, "Edit not implemented", Toast.LENGTH_SHORT).show()
                        }
                        text.equals("Details", ignoreCase = true) -> {
                            DeliveryDetailsDialog(itemView.context).show(
                                delivery,
                                onStatusChanged = {
                                    (bindingAdapter as? CustomerDeliveryAdapter)?.submitList(MockDeliveryDataSource.getDeliveriesByCustomer(delivery.customerId))
                                },
                                showActions = false,
                                showDriverName = true
                            )
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
            binding.customerDeliveryButtonsContainerLinearLayout.addView(button)
        }

        private fun showImagePreview() {
            val dialog = Dialog(binding.root.context)
            val previewBinding = DialogDeliveryImagePreviewBinding.inflate(LayoutInflater.from(binding.root.context))
            dialog.setContentView(previewBinding.root)
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            previewBinding.deliveryImagePreviewImageView.setImageResource(com.example.mobilefinalproject.R.drawable.ic_person)
            dialog.show()
        }

        private fun Int.dpToPx(): Int {
            return (this * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}