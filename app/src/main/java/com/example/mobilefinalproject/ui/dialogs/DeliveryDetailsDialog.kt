package com.example.mobilefinalproject.ui.dialogs

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.mobilefinalproject.BuildConfig
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.driver.ButtonConfig
import com.example.mobilefinalproject.models.driver.activeDeliveryConfigs
import com.example.mobilefinalproject.models.toStatusLabel
import com.example.mobilefinalproject.network.dto.OrderRead
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

import android.util.Log

class DeliveryDetailsDialog(private val context: Context) {

    fun show(
        order: OrderRead,
        onAccept: (() -> Unit)? = null,
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
        showActions: Boolean = true,
        showDriverInfo: Boolean = false,
        onDismiss: (() -> Unit)? = null
    ) {
        val view = android.view.LayoutInflater.from(context)
            .inflate(R.layout.dialog_delivery_details, null)

        val dialog = AlertDialog.Builder(context, R.style.RoundedDialog)
            .setView(view)
            .create()

        dialog.setView(view, 0, 0, 0, 0)

        val nameTextView       = view.findViewById<TextView>(R.id.dialog_name_text_view)
        val priceTextView      = view.findViewById<TextView>(R.id.dialog_price_text_view)
        val statusTextView     = view.findViewById<TextView>(R.id.dialog_status_text_view)
        val statusBadge        = view.findViewById<LinearLayout>(R.id.dialog_status_badge_linear_layout)
        val dateTimeTextView   = view.findViewById<TextView>(R.id.dialog_date_time_text_view)
        val pickupTextView     = view.findViewById<TextView>(R.id.dialog_pickup_address_text_view)
        val destTextView       = view.findViewById<TextView>(R.id.dialog_destination_address_text_view)
        val descriptionTextView= view.findViewById<TextView>(R.id.dialog_description_text_view)
        val imageView          = view.findViewById<ImageView>(R.id.dialog_delivery_image_view)
        val imageCard          = imageView.parent as? View
        val buttonsContainer   = view.findViewById<LinearLayout>(R.id.dialog_buttons_container_linear_layout)
        val closeButton        = view.findViewById<TextView>(R.id.dialog_close_button)

        Log.d("DeliveryDetailsDialog", order.toString())
        // Populate
        nameTextView.text = if (showDriverInfo && order.driverId != null) "Driver #${order.driverId}"
                            else "Customer #${order.customerId}"
        priceTextView.text = String.format(Locale.getDefault(), "$%.2f", order.priceCents / 100.0)
        statusTextView.text = order.status.toStatusLabel()

        activeDeliveryConfigs[order.status]?.let { statusBadge.setBackgroundResource(it.badgeDrawable) }

        val timeDisplay = try {
            val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val displayFmt = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
            val parsed = isoFmt.parse(order.createdAt.take(19))
            if (parsed != null) displayFmt.format(parsed) else order.createdAt
        } catch (_: Exception) { order.createdAt }
        dateTimeTextView.text = timeDisplay

        pickupTextView.text = order.pickupAddress
        destTextView.text   = order.dropoffAddress
        descriptionTextView.text = order.cargoDescription?.takeIf { it.isNotBlank() } ?: "No description"

        if (!order.cargoImageUrl.isNullOrBlank()) {
            imageCard?.visibility = View.VISIBLE
            val cargoPath = if (order.cargoImageUrl.startsWith("/")) {
                order.cargoImageUrl.substring(1)
            } else {
                order.cargoImageUrl
            }
            val imageUrl = "${BuildConfig.BASE_URL}${cargoPath}"
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(imageView)
        } else {
            Picasso.get().cancelRequest(imageView)
            imageView.setImageDrawable(null)
            imageCard?.visibility = View.GONE
        }

        if (showActions) {
            setupActionButtons(order, buttonsContainer, dialog, onAccept, onStart, onComplete, onCancel)
        } else {
            buttonsContainer?.visibility = View.GONE
        }

        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener { onDismiss?.invoke() }
        dialog.show()
    }

    private fun setupActionButtons(
        order: OrderRead,
        container: LinearLayout,
        dialog: AlertDialog,
        onAccept: (() -> Unit)?,
        onStart: (() -> Unit)?,
        onComplete: (() -> Unit)?,
        onCancel: (() -> Unit)?
    ) {
        val buttons: List<ButtonConfig> = when (order.status) {
            "pending" -> listOf(
                ButtonConfig("Accept", R.drawable.button_green, android.graphics.Color.WHITE, 1f)
            )
            else -> activeDeliveryConfigs[order.status]?.buttons ?: emptyList()
        }

        buttons.filter { !it.text.equals("Details", ignoreCase = true) }.forEach { cfg ->
            addButton(container, cfg.text, cfg.backgroundRes, cfg.textColor, cfg.weight, dialog,
                onAccept, onStart, onComplete, onCancel)
        }
    }

    private fun addButton(
        container: LinearLayout,
        text: String, backgroundRes: Int, textColor: Int, weight: Float,
        dialog: AlertDialog,
        onAccept: (() -> Unit)?,
        onStart: (() -> Unit)?,
        onComplete: (() -> Unit)?,
        onCancel: (() -> Unit)?
    ) {
        val button = LinearLayout(context).apply {
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
                    text.equals("Accept", ignoreCase = true) -> {
                        onAccept?.invoke()
                        dialog.dismiss()
                    }
                    text.equals("Start", ignoreCase = true) -> {
                        onStart?.invoke()
                        Toast.makeText(context, "Order started", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    text.equals("Complete", ignoreCase = true) -> {
                        onComplete?.invoke()
                        Toast.makeText(context, "Order completed", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    text.equals("Cancel", ignoreCase = true) -> {
                        onCancel?.invoke()
                        Toast.makeText(context, "Order cancelled", Toast.LENGTH_SHORT).show()
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

    private fun Int.dpToPx(): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
