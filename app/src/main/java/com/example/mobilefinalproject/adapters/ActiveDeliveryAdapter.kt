package com.example.mobilefinalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.driver.ButtonConfig
import com.example.mobilefinalproject.models.driver.activeDeliveryConfigs
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat

class ActiveDeliveryAdapter(private val deliveries: List<Delivery>) :
    RecyclerView.Adapter<ActiveDeliveryAdapter.DeliveryViewHolder>() {

    inner class DeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView =
            itemView.findViewById(R.id.active_delivery_material_card_view)
        private val customerNameTextView: TextView =
            itemView.findViewById(R.id.active_delivery_customer_name_text_view)
        private val statusTextView: TextView = itemView.findViewById(R.id.active_delivery_status_text_view)
        private val statusBadge: LinearLayout =
            itemView.findViewById(R.id.active_delivery_status_badge_linear_layout)
        private val pickupAddressTextView: TextView =
            itemView.findViewById(R.id.active_delivery_pickup_address_time_text_view)
        private val destinationAddressTextView: TextView =
            itemView.findViewById(R.id.active_delivery_destination_address_text_view)
        private val phoneTextView: TextView = itemView.findViewById(R.id.active_delivery_customer_phone_text_view)
        private val buttonsContainer: LinearLayout =
            itemView.findViewById(R.id.active_delivery_buttons_container_linear_layout)

        fun bind(delivery: Delivery) {
            customerNameTextView.text = delivery.customerName
            phoneTextView.text = delivery.phoneNumber
            statusTextView.text = delivery.status

            cardView.strokeColor =
                activeDeliveryConfigs[delivery.status]?.strokeColor?.toColorInt()
                    ?: "#FFC107".toColorInt()
            cardView.strokeWidth = 2.dpToPx()

            statusBadge.setBackgroundResource(
                activeDeliveryConfigs[delivery.status]?.badgeDrawable ?: R.drawable.badge_pending
            )

            pickupAddressTextView.text =
                delivery.pickupAddress + " • " + SimpleDateFormat("dd/MM/yyyy \u2022 HH:mm").format(
                    delivery.date
                )
            destinationAddressTextView.text = delivery.destinationAddress

            setupActionButtons(delivery)
        }

        private fun setupActionButtons(delivery: Delivery) {
            buttonsContainer.removeAllViews()
            val buttons: List<ButtonConfig> =
                activeDeliveryConfigs[delivery.status]?.buttons ?: emptyList()
            buttons.forEach { buttonConfig ->
                addButton(
                    buttonConfig.text,
                    buttonConfig.backgroundRes,
                    buttonConfig.textColor,
                    buttonConfig.weight
                )
            }
        }

        private fun addButton(text: String, backgroundRes: Int, textColor: Int, weight: Float) {
            val button = LinearLayout(itemView.context).apply {
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                        this.weight = weight
                        marginEnd = 4.dpToPx()
                        marginStart = 4.dpToPx()
                    }
                setBackgroundResource(backgroundRes)
                gravity = android.view.Gravity.CENTER
            }

            val textView = TextView(itemView.context).apply {
                this.text = text
                setTextColor(textColor)
                textSize = 12f
            }

            button.addView(textView)
            buttonsContainer.addView(button)
        }

        private fun Int.dpToPx(): Int {
            return (this * itemView.context.resources.displayMetrics.density).toInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_driver_active_delivery, parent, false)
        return DeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        holder.bind(deliveries[position])
    }

    override fun getItemCount(): Int = deliveries.size
}
