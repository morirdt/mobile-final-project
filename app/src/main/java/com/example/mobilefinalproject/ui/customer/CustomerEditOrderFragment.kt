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
import androidx.navigation.fragment.navArgs
import com.example.mobilefinalproject.databinding.FragmentCustomerEditOrderBinding
import com.example.mobilefinalproject.models.Delivery
import com.example.mobilefinalproject.models.Location
import com.example.mobilefinalproject.models.MockDeliveryDataSource
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

class CustomerEditOrderFragment : Fragment() {
    private val calendar = Calendar.getInstance()
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()
    private val args: CustomerEditOrderFragmentArgs by navArgs()
    private var binding: FragmentCustomerEditOrderBinding? = null
    private var pickupLocation: Location? = null
    private var destinationLocation: Location? = null
    private var selectedImageUri: Uri? = null
    private var isPlacesAutocompleteEnabled: Boolean = false
    private lateinit var delivery: Delivery

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val mapsApiKey = com.example.mobilefinalproject.BuildConfig.MAPS_API_KEY
        if (mapsApiKey.isBlank()) {
            isPlacesAutocompleteEnabled = false
            return
        }
        Places.initialize(context, mapsApiKey)
        isPlacesAutocompleteEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerEditOrderBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get delivery from SafeArgs
        delivery = args.delivery

        prefillFields()
        setupUi()
        setupButtonListeners()
        setupLocationPickers()
        setupImagePicker()
        setupDateTimePickers()
    }

    private fun prefillFields() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Pre-fill location fields
        binding?.customerEditOrderPickupAddressEditText?.setText(delivery.pickupLocation.address)
        binding?.customerEditOrderDestinationAddressEditText?.setText(delivery.destinationLocation.address)
        pickupLocation = delivery.pickupLocation
        destinationLocation = delivery.destinationLocation

        // Pre-fill date and time
        binding?.customerEditOrderPickupDateEditText?.setText(dateFormat.format(delivery.date))
        binding?.customerEditOrderPickupTimeEditText?.setText(timeFormat.format(delivery.date))

        // Pre-fill package details
        binding?.customerEditOrderDescriptionEditText?.setText(delivery.description)
        binding?.customerEditOrderBudgetEditText?.setText(delivery.price.toString())

        // Pre-fill image if available
        if (delivery.imageUri != null) {
            selectedImageUri = delivery.imageUri
            Picasso.get()
                .load(selectedImageUri)
                .into(binding?.customerEditOrderImagePreview)
            binding?.customerEditOrderImagePreview?.visibility = View.VISIBLE
        }
    }

    private fun setupUi() {
        binding?.customerEditOrderPickupAddressEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !binding?.customerEditOrderPickupAddressEditText?.text.isNullOrBlank()) {
                binding?.customerEditOrderPickupAddressLayout?.error = null
            }
        }

        binding?.customerEditOrderDestinationAddressEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !binding?.customerEditOrderDestinationAddressEditText?.text.isNullOrBlank()) {
                binding?.customerEditOrderDestinationAddressLayout?.error = null
            }
        }

        binding?.customerEditOrderDescriptionEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !binding?.customerEditOrderDescriptionEditText?.text.isNullOrBlank()) {
                binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
                    com.example.mobilefinalproject.R.id.customer_edit_order_description_layout
                )?.error = null
            }
        }

        binding?.customerEditOrderBudgetEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !binding?.customerEditOrderBudgetEditText?.text.isNullOrBlank()) {
                binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
                    com.example.mobilefinalproject.R.id.customer_edit_order_budget_layout
                )?.error = null
            }
        }
    }

    private fun setupButtonListeners() {
        binding?.customerEditOrderSubmitButton?.setOnClickListener {
            if (validateForm()) {
                submitOrder()
            }
        }

        binding?.customerEditOrderCancelButton?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            Log.e("EditOrder", "Gallery permission denied")
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            binding?.customerEditOrderImagePreview?.let { imageView ->
                Picasso.get()
                    .load(selectedImageUri)
                    .into(imageView)
            }
            binding?.customerEditOrderImagePreview?.visibility = View.VISIBLE
            Log.d("EditOrder", "Image selected: $it")
        }
    }

    private val pickupAddressLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            AutocompleteActivity.RESULT_OK -> {
                val place = Autocomplete.getPlaceFromIntent(result.data ?: return@registerForActivityResult)
                val address = place.address ?: ""
                val lat = place.latLng?.latitude ?: 0.0
                val lng = place.latLng?.longitude ?: 0.0
                updatePickupLocation(address, lat, lng)
            }
            AutocompleteActivity.RESULT_ERROR -> {
                val status = Autocomplete.getStatusFromIntent(result.data ?: return@registerForActivityResult)
                Log.e("Places", status.statusMessage ?: "Unknown error")
            }
        }
    }

    private val destinationAddressLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            AutocompleteActivity.RESULT_OK -> {
                val place = Autocomplete.getPlaceFromIntent(result.data ?: return@registerForActivityResult)
                val address = place.address ?: ""
                val lat = place.latLng?.latitude ?: 0.0
                val lng = place.latLng?.longitude ?: 0.0
                updateDestinationLocation(address, lat, lng)
            }
            AutocompleteActivity.RESULT_ERROR -> {
                val status = Autocomplete.getStatusFromIntent(result.data ?: return@registerForActivityResult)
                Log.e("Places", status.statusMessage ?: "Unknown error")
            }
        }
    }

    private fun setupLocationPickers() {
        binding?.customerEditOrderPickupAddressEditText?.setOnClickListener {
            if (isPlacesAutocompleteEnabled) {
                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(requireContext())
                pickupAddressLauncher.launch(intent)
            }
        }

        binding?.customerEditOrderDestinationAddressEditText?.setOnClickListener {
            if (isPlacesAutocompleteEnabled) {
                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(requireContext())
                destinationAddressLauncher.launch(intent)
            }
        }
    }

    private fun setupDateTimePickers() {
        binding?.customerEditOrderPickupDateEditText?.setOnClickListener {
            showDatePicker()
        }

        binding?.customerEditOrderPickupTimeEditText?.setOnClickListener {
            showTimePicker { time ->
                binding?.customerEditOrderPickupTimeEditText?.setText(time)
            }
        }
    }

    private fun setupImagePicker() {
        binding?.customerEditOrderAddImageButton?.setOnClickListener {
            checkAndRequestGalleryPermission()
        }
    }

    private fun checkAndRequestGalleryPermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

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
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding?.customerEditOrderPickupDateEditText?.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(callback: (String) -> Unit) {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                callback(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val pickupText = binding?.customerEditOrderPickupAddressEditText?.text?.toString()?.trim().orEmpty()
        val destinationText = binding?.customerEditOrderDestinationAddressEditText?.text?.toString()?.trim().orEmpty()

        if (pickupLocation == null && pickupText.isBlank()) {
            binding?.customerEditOrderPickupAddressLayout?.error = "Pickup address is required"
            isValid = false
        } else {
            binding?.customerEditOrderPickupAddressLayout?.error = null
        }

        if (destinationLocation == null && destinationText.isBlank()) {
            binding?.customerEditOrderDestinationAddressLayout?.error = "Destination address is required"
            isValid = false
        } else {
            binding?.customerEditOrderDestinationAddressLayout?.error = null
        }

        val dateText = binding?.customerEditOrderPickupDateEditText?.text?.toString()?.trim()
        val dateLayout = binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
            com.example.mobilefinalproject.R.id.customer_edit_order_pickup_date_layout
        )
        if (dateText.isNullOrEmpty()) {
            dateLayout?.error = "Delivery date is required"
            isValid = false
        } else {
            dateLayout?.error = null
        }

        val timeText = binding?.customerEditOrderPickupTimeEditText?.text?.toString()?.trim()
        if (timeText.isNullOrEmpty()) {
            binding?.customerEditOrderPickupTimeLayout?.error = "Pickup time is required"
            isValid = false
        } else {
            binding?.customerEditOrderPickupTimeLayout?.error = null
        }

        val descriptionText = binding?.customerEditOrderDescriptionEditText?.text?.toString()?.trim()
        val descriptionLayout = binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
            com.example.mobilefinalproject.R.id.customer_edit_order_description_layout
        )
        if (descriptionText.isNullOrEmpty()) {
            descriptionLayout?.error = "Package description is required"
            isValid = false
        } else {
            descriptionLayout?.error = null
        }

        val budgetText = binding?.customerEditOrderBudgetEditText?.text?.toString()?.trim()
        val budgetLayout = binding?.root?.findViewById<com.google.android.material.textfield.TextInputLayout>(
            com.example.mobilefinalproject.R.id.customer_edit_order_budget_layout
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
        val pickup = pickupLocation ?: Location(
            address = binding?.customerEditOrderPickupAddressEditText?.text?.toString()?.trim().orEmpty(),
            latitude = 0.0,
            longitude = 0.0,
        )
        val destination = destinationLocation ?: Location(
            address = binding?.customerEditOrderDestinationAddressEditText?.text?.toString()?.trim().orEmpty(),
            latitude = 0.0,
            longitude = 0.0,
        )

        if (pickup.address.isBlank() || destination.address.isBlank()) {
            return
        }

        val budget = binding?.customerEditOrderBudgetEditText?.text?.toString()?.trim()?.toDoubleOrNull() ?: return
        val description = binding?.customerEditOrderDescriptionEditText?.text?.toString()?.trim().orEmpty()
        val dateText = binding?.customerEditOrderPickupDateEditText?.text?.toString()?.trim().orEmpty()
        val timeText = binding?.customerEditOrderPickupTimeEditText?.text?.toString()?.trim().orEmpty()
        val dateTimeString = "$dateText $timeText"
        val deliveryDate = try {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(dateTimeString) ?: Date()
        } catch (_: Exception) {
            Date()
        }

        val updatedDelivery = delivery.copy(
            price = budget,
            date = deliveryDate,
            pickupLocation = pickup,
            destinationLocation = destination,
            description = description,
            imageUriString = selectedImageUri?.toString() ?: delivery.imageUriString
        )

        MockDeliveryDataSource.updateDelivery(updatedDelivery)
        deliveryViewModel.setCustomerDeliveries(
            MockDeliveryDataSource.getDeliveriesByCustomer(delivery.customerId)
        )
        findNavController().popBackStack()
    }

    private fun updatePickupLocation(address: String, latitude: Double, longitude: Double) {
        pickupLocation = Location(address = address, latitude = latitude, longitude = longitude)
        binding?.customerEditOrderPickupAddressEditText?.setText(address)
        binding?.customerEditOrderPickupAddressLayout?.error = null
    }

    private fun updateDestinationLocation(address: String, latitude: Double, longitude: Double) {
        destinationLocation = Location(address = address, latitude = latitude, longitude = longitude)
        binding?.customerEditOrderDestinationAddressEditText?.setText(address)
        binding?.customerEditOrderDestinationAddressLayout?.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}






