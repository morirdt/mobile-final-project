package com.example.mobilefinalproject.ui.customer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.databinding.FragmentCustomerNewOrderBinding
import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.DeliveryStatus
import com.example.mobilefinalproject.models.Location
import com.example.mobilefinalproject.models.MockDeliveryDataSource
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.viewmodels.DeliveryViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class CustomerNewOrderFragment : Fragment() {
    private val calendar = Calendar.getInstance()
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()
    private val customerViewModel: CustomerViewModel by activityViewModels()
    private var binding: FragmentCustomerNewOrderBinding? = null
    private var pickupLocation: Location? = null
    private var destinationLocation: Location? = null
    private var selectedImageUri: Uri? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, com.example.mobilefinalproject.BuildConfig.MAPS_API_KEY)
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
        setupImagePicker()
        setupFieldErrorClearing()
        setupButtonListeners()
    }

    private fun setupFieldErrorClearing() {
        binding?.customerNewOrderPickupAddressEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !binding?.customerNewOrderPickupAddressEditText?.text.isNullOrBlank()) {
                binding?.customerNewOrderPickupAddressLayout?.error = null
            }
        }
        binding?.customerNewOrderDestinationAddressEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !binding?.customerNewOrderDestinationAddressEditText?.text.isNullOrBlank()) {
                binding?.customerNewOrderDestinationAddressLayout?.error = null
            }
        }
        binding?.customerNewOrderPickupDateEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !binding?.customerNewOrderPickupDateEditText?.text.isNullOrBlank()) {
                binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
                    com.example.mobilefinalproject.R.id.customer_new_order_pickup_date_layout
                )?.error = null
            }
        }
        binding?.customerNewOrderPickupTimeEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !binding?.customerNewOrderPickupTimeEditText?.text.isNullOrBlank()) {
                binding?.customerNewOrderPickupTimeLayout?.error = null
            }
        }
        binding?.customerNewOrderDescriptionEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !binding?.customerNewOrderDescriptionEditText?.text.isNullOrBlank()) {
                binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
                    com.example.mobilefinalproject.R.id.customer_new_order_description_layout
                )?.error = null
            }
        }
        binding?.customerNewOrderBudgetEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !binding?.customerNewOrderBudgetEditText?.text.isNullOrBlank()) {
                binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
                    com.example.mobilefinalproject.R.id.customer_new_order_budget_layout
                )?.error = null
            }
        }
    }

    private fun setupButtonListeners() {
        binding?.customerNewOrderSubmitButton?.setOnClickListener {
            if (validateForm()) {
                submitOrder()
            }
        }

        binding?.customerNewOrderCancelButton?.setOnClickListener {
            findNavController().navigate(com.example.mobilefinalproject.R.id.action_customerNewOrderFragment_to_customerHomeFragment)
        }
    }

    // Permission request launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, open image picker
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            Log.e("NewOrder", "Gallery permission denied")
        }
    }

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            binding?.customerNewOrderImagePreview?.let { imageView ->
                Picasso.get()
                    .load(selectedImageUri)
                    .into(imageView)
            }
            binding?.customerNewOrderImagePreview?.visibility = View.VISIBLE
            Log.d("NewOrder", "Image selected: $it")
        }
    }

    private val pickupAddressLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                result.data?.let { data ->
                    val place = Autocomplete.getPlaceFromIntent(data)
                    place.latLng?.let { latLng ->
                        updatePickupLocation(
                            address = place.address ?: place.name ?: "",
                            latitude = latLng.latitude,
                            longitude = latLng.longitude
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
                        updateDestinationLocation(
                            address = place.address ?: place.name ?: "",
                            latitude = latLng.latitude,
                            longitude = latLng.longitude
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
        binding?.customerNewOrderPickupAddressEditText?.isFocusable = false
        binding?.customerNewOrderPickupAddressEditText?.isClickable = true
        binding?.customerNewOrderPickupAddressEditText?.setOnClickListener {
            openPlacesAutocomplete(pickupAddressLauncher)
        }

        binding?.customerNewOrderDestinationAddressEditText?.isFocusable = false
        binding?.customerNewOrderDestinationAddressEditText?.isClickable = true
        binding?.customerNewOrderDestinationAddressEditText?.setOnClickListener {
            openPlacesAutocomplete(destinationAddressLauncher)
        }
    }

    private fun openPlacesAutocomplete(launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>) {
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())

        launcher.launch(intent)
    }

    private fun setupDateTimePickers() {
        binding?.customerNewOrderPickupDateEditText?.setOnClickListener {
            showDatePicker()
        }

        binding?.customerNewOrderPickupTimeEditText?.setOnClickListener {
            showTimePicker { time ->
                binding?.customerNewOrderPickupTimeEditText?.setText(time)
            }
        }
    }

    private fun setupImagePicker() {
        binding?.customerNewOrderAddImageButton?.setOnClickListener {
            checkAndRequestGalleryPermission()
        }
    }

    private fun checkAndRequestGalleryPermission() {
        val permission = android.Manifest.permission.READ_MEDIA_IMAGES

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            else -> {
                permissionLauncher.launch(permission)
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

    private fun validateForm(): Boolean {
        var isValid = true

        val pickupText = binding?.customerNewOrderPickupAddressEditText?.text?.toString()?.trim().orEmpty()
        val destinationText = binding?.customerNewOrderDestinationAddressEditText?.text?.toString()?.trim().orEmpty()

        if (pickupLocation == null && pickupText.isBlank()) {
            binding?.customerNewOrderPickupAddressLayout?.error = "Pickup address is required"
            isValid = false
        } else {
            binding?.customerNewOrderPickupAddressLayout?.error = null
        }

        if (destinationLocation == null && destinationText.isBlank()) {
            binding?.customerNewOrderDestinationAddressLayout?.error = "Destination address is required"
            isValid = false
        } else {
            binding?.customerNewOrderDestinationAddressLayout?.error = null
        }

        val dateText = binding?.customerNewOrderPickupDateEditText?.text?.toString()?.trim()
        val dateLayout = binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
            com.example.mobilefinalproject.R.id.customer_new_order_pickup_date_layout
        )
        if (dateText.isNullOrEmpty()) {
            dateLayout?.error = "Delivery date is required"
            isValid = false
        } else {
            dateLayout?.error = null
        }

        val timeText = binding?.customerNewOrderPickupTimeEditText?.text?.toString()?.trim()
        if (timeText.isNullOrEmpty()) {
            binding?.customerNewOrderPickupTimeLayout?.error = "Pickup time is required"
            isValid = false
        } else {
            binding?.customerNewOrderPickupTimeLayout?.error = null
        }

        val descriptionText = binding?.customerNewOrderDescriptionEditText?.text?.toString()?.trim()
        val descriptionLayout = binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
            com.example.mobilefinalproject.R.id.customer_new_order_description_layout
        )
        if (descriptionText.isNullOrEmpty()) {
            descriptionLayout?.error = "Package description is required"
            isValid = false
        } else {
            descriptionLayout?.error = null
        }

        val budgetText = binding?.customerNewOrderBudgetEditText?.text?.toString()?.trim()
        val budgetLayout = binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
            com.example.mobilefinalproject.R.id.customer_new_order_budget_layout
        )
        if (budgetText.isNullOrEmpty()) {
            budgetLayout?.error = "Budget is required"
            isValid = false
        } else {
            val budget = budgetText.toDoubleOrNull()
            if (budget == null || budget <= 0) {
                budgetLayout?.error = "Budget must be a valid positive amount"
                isValid = false
            } else {
                budgetLayout?.error = null
            }
        }

        return isValid
    }

    private fun submitOrder() {
        val customer = customerViewModel.customer.value ?: run {
            Log.e("NewOrder", "Cannot submit order without a customer")
            return
        }

        val pickup = pickupLocation ?: return
        val destination = destinationLocation ?: return
        val budget = binding?.customerNewOrderBudgetEditText?.text?.toString()?.trim()?.toDoubleOrNull() ?: return
        val description = binding?.customerNewOrderDescriptionEditText?.text?.toString()?.trim().orEmpty()
        val dateText = binding?.customerNewOrderPickupDateEditText?.text?.toString()?.trim().orEmpty()
        val timeText = binding?.customerNewOrderPickupTimeEditText?.text?.toString()?.trim().orEmpty()
        val dateTimeString = "$dateText $timeText"
        val deliveryDate = try {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(dateTimeString) ?: Date()
        } catch (_: Exception) {
            Date()
        }

        val newDelivery = Delivery(
            id = UUID.randomUUID().toString(),
            customerName = customer.fullName,
            customerId = customer.id,
            status = DeliveryStatus.PENDING.label,
            price = budget,
            date = deliveryDate,
            pickupLocation = pickup,
            destinationLocation = destination,
            description = description,
            imageUri = selectedImageUri
        )

        MockDeliveryDataSource.addDelivery(newDelivery)
        deliveryViewModel.setDeliveries(MockDeliveryDataSource.deliveries.toList())
        deliveryViewModel.setCustomerDeliveries(MockDeliveryDataSource.getDeliveriesByCustomer(customer.id))
        findNavController().navigate(com.example.mobilefinalproject.R.id.action_customerNewOrderFragment_to_customerHomeFragment)
    }

    private fun updatePickupLocation(address: String, latitude: Double, longitude: Double) {
        pickupLocation = Location(address = address, latitude = latitude, longitude = longitude)
        binding?.customerNewOrderPickupAddressEditText?.setText(address)
        binding?.customerNewOrderPickupAddressLayout?.error = null
    }

    private fun updateDestinationLocation(address: String, latitude: Double, longitude: Double) {
        destinationLocation = Location(address = address, latitude = latitude, longitude = longitude)
        binding?.customerNewOrderDestinationAddressEditText?.setText(address)
        binding?.customerNewOrderDestinationAddressLayout?.error = null
    }
}
