package com.example.mobilefinalproject.ui.dialogs

import android.content.Context
// ...existing imports...
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.DeliveryStatus
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.models.driver.ButtonConfig
import com.example.mobilefinalproject.models.driver.activeDeliveryConfigs
import java.text.SimpleDateFormat
import java.util.Locale


class DeliveryDetailsDialog(private val context: Context) {

    fun show(
        delivery: Delivery,
        onStatusChanged: (() -> Unit)? = null,
        showActions: Boolean = true,
        showDriverName: Boolean = false,
        onDismiss: (() -> Unit)? = null
    ) {
        val view = android.view.LayoutInflater.from(context)
            .inflate(R.layout.dialog_delivery_details, null)

        val dialog = AlertDialog.Builder(context, R.style.RoundedDialog)
            .setView(view)
            .create()

        dialog.setView(view, 0, 0, 0, 0)
        // Setup views
        val nameTextView = view.findViewById<TextView>(R.id.dialog_name_text_view)
        val priceTextView = view.findViewById<TextView>(R.id.dialog_price_text_view)
        val statusTextView = view.findViewById<TextView>(R.id.dialog_status_text_view)
        val statusBadge = view.findViewById<LinearLayout>(R.id.dialog_status_badge_linear_layout)
        val dateTimeTextView = view.findViewById<TextView>(R.id.dialog_date_time_text_view)
        val pickupAddressTextView = view.findViewById<TextView>(R.id.dialog_pickup_address_text_view)
        val destinationAddressTextView = view.findViewById<TextView>(R.id.dialog_destination_address_text_view)
        val descriptionTextView = view.findViewById<TextView>(R.id.dialog_description_text_view)
        val imageView = view.findViewById<ImageView>(R.id.dialog_delivery_image_view)
        val buttonsContainer = view.findViewById<LinearLayout>(R.id.dialog_buttons_container_linear_layout)
        val closeButton = view.findViewById<TextView>(R.id.dialog_close_button)

        // Populate data
        nameTextView.text = if (showDriverName) delivery.driverName else delivery.customerName
        priceTextView.text = String.format(Locale.getDefault(), "$%.2f", delivery.price)
        statusTextView.text = delivery.status

        // Set status badge styling
        val config = activeDeliveryConfigs[delivery.status]
        if (config != null) {
            statusBadge.setBackgroundResource(config.badgeDrawable)
        }

        // Set date and time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
        dateTimeTextView.text = dateFormat.format(delivery.date)

        // Set addresses
        pickupAddressTextView.text = delivery.pickupLocation.address
        destinationAddressTextView.text = delivery.destinationLocation.address

        // Set description
        descriptionTextView.text = delivery.description.ifBlank {
            "No description"
        }

        // Set image
        if (delivery.imageUri != null) {
            imageView.setImageURI(delivery.imageUri)
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder_image)
        }

        // Setup buttons (only when allowed)
        if (showActions) {
            setupActionButtons(delivery, buttonsContainer, dialog, onStatusChanged)
        } else {
            // hide the buttons container to avoid empty spacing
            buttonsContainer?.let { it.visibility = android.view.View.GONE }
        }

        // Close button
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // notify caller when dialog is dismissed (either via close button or user)
        dialog.setOnDismissListener {
            onDismiss?.invoke()
        }

        dialog.show()
    }

    private fun setupActionButtons(
        delivery: Delivery,
        container: LinearLayout,
        dialog: AlertDialog,
        onStatusChanged: (() -> Unit)?
    ) {
        val buttons = when {
            delivery.status == DeliveryStatus.PENDING.label -> {
                // For Pending deliveries, show only Accept button
                listOf(
                    ButtonConfig("Accept", R.drawable.button_green, android.graphics.Color.WHITE, 1f)
                )
            }
            else -> {
                activeDeliveryConfigs[delivery.status]?.buttons ?: emptyList()
            }
        }

        // Filter out the Details button from the modal
        buttons.filter { !it.text.equals("Details", ignoreCase = true) }.forEach { buttonConfig ->
            addButton(
                delivery = delivery,
                container = container,
                text = buttonConfig.text,
                backgroundRes = buttonConfig.backgroundRes,
                textColor = buttonConfig.textColor,
                weight = buttonConfig.weight,
                dialog = dialog,
                onStatusChanged = onStatusChanged
            )
        }
    }

    private fun addButton(
        delivery: Delivery,
        container: LinearLayout,
        text: String,
        backgroundRes: Int,
        textColor: Int,
        weight: Float,
        dialog: AlertDialog,
        onStatusChanged: (() -> Unit)?
    ) {
        val button = LinearLayout(context).apply {
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
                            onStatusChanged?.invoke()
                            dialog.dismiss()
                        }
                        text.equals("Complete", ignoreCase = true) && delivery.status == DeliveryStatus.IN_PROGRESS.label -> {
                            MockDeliveryDataSource.updateDeliveryStatus(delivery.id, DeliveryStatus.COMPLETED.label)
                            Toast.makeText(context, "Order completed", Toast.LENGTH_SHORT).show()
                            onStatusChanged?.invoke()
                            dialog.dismiss()
                        }
                        text.equals("Accept", ignoreCase = true) && delivery.status == DeliveryStatus.PENDING.label -> {
                            MockDeliveryDataSource.updateDeliveryStatus(delivery.id, DeliveryStatus.ACCEPTED.label)
                            Toast.makeText(context, "Delivery accepted!", Toast.LENGTH_SHORT).show()
                            onStatusChanged?.invoke()
                            dialog.dismiss()
                        }
                        text.equals("Cancel", ignoreCase = true) -> {
                            MockDeliveryDataSource.updateDeliveryStatus(delivery.id, DeliveryStatus.PENDING.label)
                            Toast.makeText(context, "Delivery cancelled", Toast.LENGTH_SHORT).show()
                            onStatusChanged?.invoke()
                            dialog.dismiss()
                        }
                    }
                }
        }

        val textView = TextView(context).apply {
            this.text = text
            setTextColor(textColor)
            textSize = 12f
        }

        button.addView(textView)
        container.addView(button)
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
