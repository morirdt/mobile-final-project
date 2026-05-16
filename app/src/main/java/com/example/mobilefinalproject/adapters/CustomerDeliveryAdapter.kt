package com.example.mobilefinalproject.adapters

import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.DialogDeliveryImagePreviewBinding
import com.example.mobilefinalproject.databinding.ItemCustomerDeliveryBinding
import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.DeliveryStatus
import java.text.SimpleDateFormat
import java.util.Locale

class CustomerDeliveryAdapter(
    var deliveries: List<Delivery>?
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
            binding.customerDeliveryNameTextView.text = item.customerName
            binding.customerDeliveryPriceTextView.text = String.format(Locale.getDefault(), "$%.2f", item.price)
            binding.customerDeliveryDateTimeTextView.text =
                SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(item.date)
            binding.customerDeliveryPickupAddressTextView.text = item.pickupLocation.address
            binding.customerDeliveryDestinationAddressTextView.text = item.destinationLocation.address

            val statusText = when (item.status) {
                DeliveryStatus.PENDING.label -> DeliveryStatus.PENDING.label
                DeliveryStatus.ACCEPTED.label -> DeliveryStatus.ACCEPTED.label
                DeliveryStatus.IN_PROGRESS.label -> DeliveryStatus.IN_PROGRESS.label
                DeliveryStatus.COMPLETED.label -> DeliveryStatus.COMPLETED.label
                else -> item.status
            }

            binding.customerDeliveryStatusTextView.text = statusText
            val backgroundColor = when (item.status) {
                DeliveryStatus.PENDING.label -> ContextCompat.getColor(binding.root.context, com.example.mobilefinalproject.R.color.gray_700)
                DeliveryStatus.ACCEPTED.label -> ContextCompat.getColor(binding.root.context, com.example.mobilefinalproject.R.color.blue_700)
                DeliveryStatus.IN_PROGRESS.label -> ContextCompat.getColor(binding.root.context, com.example.mobilefinalproject.R.color.teal_700)
                DeliveryStatus.COMPLETED.label -> ContextCompat.getColor(binding.root.context, android.R.color.holo_green_dark)
                else -> ContextCompat.getColor(binding.root.context, com.example.mobilefinalproject.R.color.gray_700)
            }
            binding.customerDeliveryStatusTextView.setBackgroundColor(backgroundColor)
            binding.customerDeliveryStatusTextView.setTextColor(Color.WHITE)

            binding.customerDeliveryImageView.setOnClickListener {
                showImagePreview(item)
            }
        }

        private fun showImagePreview(delivery: Delivery) {
            val dialog = Dialog(binding.root.context)
            val previewBinding = DialogDeliveryImagePreviewBinding.inflate(LayoutInflater.from(binding.root.context))
            dialog.setContentView(previewBinding.root)
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            if (delivery.imageUri != null) {
                previewBinding.deliveryImagePreviewImageView.setImageURI(delivery.imageUri)
            } else {
                previewBinding.deliveryImagePreviewImageView.setImageResource(R.drawable.ic_placeholder_image)
            }

            dialog.show()
        }
    }
}