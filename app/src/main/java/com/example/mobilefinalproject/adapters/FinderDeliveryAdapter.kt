package com.example.mobilefinalproject.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.ItemDriverFinderDeliveryBinding
import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.DeliveryStatus
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.ui.dialogs.DeliveryDetailsDialog
import java.text.SimpleDateFormat
import java.util.Locale

class FinderDeliveryAdapter(
    var deliveries: List<Delivery>?
) : RecyclerView.Adapter<FinderDeliveryAdapter.FinderDeliveryViewHolder>() {

    fun refreshFromSource() {
        deliveries = MockDeliveryDataSource.getPendingDeliveries()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = deliveries?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinderDeliveryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDriverFinderDeliveryBinding.inflate(inflater, parent, false)
        return FinderDeliveryViewHolder(binding = binding)
    }

    override fun onBindViewHolder(
        holder: FinderDeliveryViewHolder,
        position: Int
    ) {
        deliveries?.let {
            holder.bind(it[position])
        }
    }

    class FinderDeliveryViewHolder(private val binding: ItemDriverFinderDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Delivery) {
            binding.finderDeliveryCustomerNameTextView.text = item.customerName
            binding.finderDeliveryPriceTextView.text = String.format(Locale.getDefault(), "$%.2f", item.price)
            binding.finderDeliveryTimeTextView.text =
                SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(item.date)
            binding.finderDeliveryPickupAddressTextView.text = item.pickupLocation.address
            binding.finderDeliveryDestinationAddressTextView.text = item.destinationLocation.address

            binding.finderDeliveryCardView.strokeColor = Color.parseColor("#FF9800")
            binding.finderDeliveryCardView.strokeWidth = 2.dpToPx()

            setupActionButtons(item)
        }

        private fun setupActionButtons(delivery: Delivery) {
            binding.finderDeliveryButtonsContainerLinearLayout.removeAllViews()

            addButton(
                delivery = delivery,
                text = "Accept",
                backgroundRes = R.drawable.button_green,
                weight = 0.5f
            )

            addButton(
                delivery = delivery,
                text = "Details",
                backgroundRes = R.drawable.button_light_purple,
                textColor = "#FF9C27B0".toColorInt(),
                weight = 0.5f
            )
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
                        text.equals("Accept", ignoreCase = true) && delivery.status == DeliveryStatus.PENDING.label -> {
                            MockDeliveryDataSource.updateDeliveryStatus(delivery.id, DeliveryStatus.ACCEPTED.label)
                            Toast.makeText(context, "Delivery accepted!", Toast.LENGTH_SHORT).show()
                            (bindingAdapter as? FinderDeliveryAdapter)?.refreshFromSource()
                        }
                        text.equals("Details", ignoreCase = true) -> {
                            DeliveryDetailsDialog(context).show(delivery) {
                                (bindingAdapter as? FinderDeliveryAdapter)?.refreshFromSource()
                            }
                        }
                        text.equals("Cancel", ignoreCase = true) -> {
                            Toast.makeText(context, "Delivery cancelled", Toast.LENGTH_SHORT).show()
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
            binding.finderDeliveryButtonsContainerLinearLayout.addView(button)
        }

        private fun Int.dpToPx(): Int {
            return (this * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}
