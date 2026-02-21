package com.example.mobilefinalproject.ui.customer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.mobilefinalproject.databinding.FragmentCustomerNewOrderBinding
import com.example.mobilefinalproject.model.Location
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.text.SimpleDateFormat
import java.util.*

class CustomerNewOrderFragment : Fragment() {
    private val calendar = Calendar.getInstance()
    private var binding: FragmentCustomerNewOrderBinding? = null
    private var pickupLocation: Location? = null
    private var destinationLocation: Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "YOUR_API_KEY_HERE")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerNewOrderBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAddressPickers()
        setupDateTimePickers()
    }

    private val pickupAddressLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                result.data?.let { data ->
                    val place = Autocomplete.getPlaceFromIntent(data)
                    place.latLng?.let { latLng ->
                        pickupLocation = Location(
                            address = place.address ?: place.name ?: "",
                            latitude = latLng.latitude,
                            longitude = latLng.longitude
                        )

                        binding?.customerNewOrderPickupAddressEditText?.setText(pickupLocation?.address)
                        Log.d(
                            "NewOrder",
                            "Pickup: ${pickupLocation?.address} - Lat: ${pickupLocation?.latitude}, Lng: ${pickupLocation?.longitude}"
                        )
                    }
                }
            }

            AutocompleteActivity.RESULT_ERROR -> {
                result.data?.let { data ->
                    val status = Autocomplete.getStatusFromIntent(data)
                    Log.e("NewOrder", "Pickup address error: ${status.statusMessage}")
                }
            }
        }
    }

    // Activity result launcher for destination address
    private val destinationAddressLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                result.data?.let { data ->
                    val place = Autocomplete.getPlaceFromIntent(data)
                    place.latLng?.let { latLng ->
                        destinationLocation = Location(
                            address = place.address ?: place.name ?: "",
                            latitude = latLng.latitude,
                            longitude = latLng.longitude
                        )
                        binding?.customerNewOrderDestinationAddressEditText?.setText(
                            destinationLocation?.address
                        )
                        Log.d(
                            "NewOrder",
                            "Destination: ${destinationLocation?.address} - Lat: ${destinationLocation?.latitude}, Lng: ${destinationLocation?.longitude}"
                        )
                    }
                }
            }

            AutocompleteActivity.RESULT_ERROR -> {
                result.data?.let { data ->
                    val status = Autocomplete.getStatusFromIntent(data)
                    Log.e("NewOrder", "Destination address error: ${status.statusMessage}")
                }
            }
        }
    }


    private fun setupAddressPickers() {
        apply {
            // Setup pickup address autocomplete
            binding?.customerNewOrderPickupAddressEditText?.isFocusable = false
            binding?.customerNewOrderPickupAddressEditText?.isClickable = true
            binding?.customerNewOrderPickupAddressEditText?.setOnClickListener {
                openPlacesAutocomplete(pickupAddressLauncher)
            }

            // Setup delivery address autocomplete
            binding?.customerNewOrderDestinationAddressEditText?.isFocusable = false
            binding?.customerNewOrderDestinationAddressEditText?.isClickable = true
            binding?.customerNewOrderDestinationAddressEditText?.setOnClickListener {
                openPlacesAutocomplete(destinationAddressLauncher)
            }

        }
    }

    private fun openPlacesAutocomplete(launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>) {
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(requireContext())

        launcher.launch(intent)
    }

    private fun setupDateTimePickers() {
        // Date picker for delivery date
        binding?.customerNewOrderPickupDateEditText?.setOnClickListener {
            showDatePicker()
        }

        // Time picker for pickup time
        binding?.customerNewOrderPickupTimeEditText?.setOnClickListener {
            showTimePicker { time ->
                binding?.customerNewOrderPickupTimeEditText?.setText(time)
            }
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding?.customerNewOrderPickupDateEditText?.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                onTimeSelected(time)
            },
            currentHour,
            currentMinute,
            true
        )
        timePickerDialog.show()
    }
}
