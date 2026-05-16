package com.example.mobilefinalproject.ui.driver

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.models.DeliveryStatus
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.NumberFormat
import java.util.Locale

class DeliveryDetailBottomSheet : BottomSheetDialogFragment() {

    private val deliveryViewModel: DeliveryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_delivery_detail_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val delivery = deliveryViewModel.selectedDelivery.value ?: run {
            dismiss()
            return
        }

        view.findViewById<TextView>(R.id.tv_order_id).text =
            getString(R.string.order_id_format, delivery.id)
        view.findViewById<TextView>(R.id.tv_customer_name).text = delivery.customerName
        view.findViewById<TextView>(R.id.tv_phone).text = delivery.phoneNumber
        view.findViewById<TextView>(R.id.tv_pickup_address).text = delivery.pickupAddress
        view.findViewById<TextView>(R.id.tv_dropoff_address).text = delivery.dropoffAddress

        val priceFormatted = NumberFormat.getCurrencyInstance(Locale.US).format(delivery.price)
        view.findViewById<TextView>(R.id.tv_price).text = priceFormatted

        val statusBadge = view.findViewById<TextView>(R.id.tv_status_badge)
        statusBadge.text = delivery.status
        statusBadge.backgroundTintList = ColorStateList.valueOf(statusColor(delivery.status))

        view.findViewById<ImageButton>(R.id.btn_close_sheet).setOnClickListener {
            deliveryViewModel.selectDelivery(null)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear selection only if the user dismissed by dragging the sheet down
        if (deliveryViewModel.selectedDelivery.value != null) {
            deliveryViewModel.selectDelivery(null)
        }
    }

    private fun statusColor(status: String): Int = when (status) {
        DeliveryStatus.PENDING.label -> Color.parseColor("#FF9800")
        DeliveryStatus.ACCEPTED.label -> Color.parseColor("#1565C0")
        DeliveryStatus.IN_PROGRESS.label -> Color.parseColor("#6A1B9A")
        DeliveryStatus.COMPLETED.label -> Color.parseColor("#2E7D32")
        DeliveryStatus.CANCELLED.label -> Color.parseColor("#B71C1C")
        else -> Color.parseColor("#757575")
    }

    companion object {
        const val TAG = "DeliveryDetailBottomSheet"
    }
}



