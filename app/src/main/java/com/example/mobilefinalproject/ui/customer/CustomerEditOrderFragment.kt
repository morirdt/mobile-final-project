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
import com.example.mobilefinalproject.network.dto.OrderUpdateRequest
import com.example.mobilefinalproject.viewmodels.OrderViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomerEditOrderFragment : Fragment() {
    private val calendar = Calendar.getInstance()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private val args: CustomerEditOrderFragmentArgs by navArgs()
    private var binding: FragmentCustomerEditOrderBinding? = null
    private var pickupAddress: String = ""
    private var pickupLat: Double = 0.0
    private var pickupLng: Double = 0.0
    private var dropoffAddress: String = ""
    private var dropoffLat: Double = 0.0
    private var dropoffLng: Double = 0.0
    private var selectedImageUri: Uri? = null
    private var isPlacesAutocompleteEnabled: Boolean = false

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

        val orderId = args.orderId

        // Observe selectedOrder (set by MyOrders when navigating here)
        orderViewModel.selectedOrder.observe(viewLifecycleOwner) { order ->
            if (order != null && order.id == orderId) {
                prefillFields(
                    pickup = order.pickupAddress,
                    pLat = order.pickupLat,
                    pLng = order.pickupLng,
                    dropoff = order.dropoffAddress,
                    dLat = order.dropoffLat,
                    dLng = order.dropoffLng,
                    description = order.cargoDescription ?: "",
                    priceCents = order.priceCents,
                    cargoImageUrl = order.cargoImageUrl
                )
            }
        }

        setupUi()
        setupButtonListeners(orderId)
        setupLocationPickers()
        setupImagePicker()
        setupDateTimePickers()
    }

    private fun prefillFields(
        pickup: String, pLat: Double, pLng: Double,
        dropoff: String, dLat: Double, dLng: Double,
        description: String, priceCents: Int,
        cargoImageUrl: String?
    ) {
        pickupAddress = pickup; pickupLat = pLat; pickupLng = pLng
        dropoffAddress = dropoff; dropoffLat = dLat; dropoffLng = dLng

        binding?.customerEditOrderPickupAddressEditText?.setText(pickup)
        binding?.customerEditOrderDestinationAddressEditText?.setText(dropoff)
        binding?.customerEditOrderDescriptionEditText?.setText(description)
        binding?.customerEditOrderBudgetEditText?.setText((priceCents / 100.0).toString())

        if (!cargoImageUrl.isNullOrBlank()) {
            Picasso.get().load(cargoImageUrl).into(binding?.customerEditOrderImagePreview)
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

    private fun setupButtonListeners(orderId: Int) {
        binding?.customerEditOrderSubmitButton?.setOnClickListener {
            if (validateForm()) {
                submitOrder(orderId)
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
                Picasso.get().load(selectedImageUri).into(imageView)
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
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(requireContext())
                pickupAddressLauncher.launch(intent)
            }
        }

        binding?.customerEditOrderDestinationAddressEditText?.setOnClickListener {
            if (isPlacesAutocompleteEnabled) {
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(requireContext())
                destinationAddressLauncher.launch(intent)
            }
        }
    }

    private fun setupDateTimePickers() {
        binding?.customerEditOrderPickupDateEditText?.setOnClickListener { showDatePicker() }
        binding?.customerEditOrderPickupTimeEditText?.setOnClickListener { time ->
            showTimePicker { binding?.customerEditOrderPickupTimeEditText?.setText(it) }
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
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED ->
                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            else -> permissionLauncher.launch(permission)
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
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
        ).show()
    }

    private fun showTimePicker(callback: (String) -> Unit) {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                callback(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val pickupText = binding?.customerEditOrderPickupAddressEditText?.text?.toString()?.trim().orEmpty()
        val destinationText = binding?.customerEditOrderDestinationAddressEditText?.text?.toString()?.trim().orEmpty()

        if (pickupAddress.isBlank() && pickupText.isBlank()) {
            binding?.customerEditOrderPickupAddressLayout?.error = "Pickup address is required"
            isValid = false
        } else {
            binding?.customerEditOrderPickupAddressLayout?.error = null
        }

        if (dropoffAddress.isBlank() && destinationText.isBlank()) {
            binding?.customerEditOrderDestinationAddressLayout?.error = "Destination address is required"
            isValid = false
        } else {
            binding?.customerEditOrderDestinationAddressLayout?.error = null
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

    private fun submitOrder(orderId: Int) {
        val finalPickupAddress = binding?.customerEditOrderPickupAddressEditText?.text?.toString()?.trim()
            ?.takeIf { it.isNotBlank() } ?: pickupAddress
        val finalDropoffAddress = binding?.customerEditOrderDestinationAddressEditText?.text?.toString()?.trim()
            ?.takeIf { it.isNotBlank() } ?: dropoffAddress

        if (finalPickupAddress.isBlank() || finalDropoffAddress.isBlank()) return

        val budgetDollars = binding?.customerEditOrderBudgetEditText?.text?.toString()?.trim()?.toDoubleOrNull() ?: return
        val priceCents = (budgetDollars * 100).toInt()
        val description = binding?.customerEditOrderDescriptionEditText?.text?.toString()?.trim()

        orderViewModel.updateOrder(
            orderId,
            OrderUpdateRequest(
                pickupAddress = finalPickupAddress,
                pickupLat = pickupLat.takeIf { it != 0.0 },
                pickupLng = pickupLng.takeIf { it != 0.0 },
                dropoffAddress = finalDropoffAddress,
                dropoffLat = dropoffLat.takeIf { it != 0.0 },
                dropoffLng = dropoffLng.takeIf { it != 0.0 },
                cargoDescription = description,
                priceCents = priceCents
            ),
            onSuccess = {
                findNavController().popBackStack()
            }
        )
    }

    private fun updatePickupLocation(address: String, latitude: Double, longitude: Double) {
        pickupAddress = address; pickupLat = latitude; pickupLng = longitude
        binding?.customerEditOrderPickupAddressEditText?.setText(address)
        binding?.customerEditOrderPickupAddressLayout?.error = null
    }

    private fun updateDestinationLocation(address: String, latitude: Double, longitude: Double) {
        dropoffAddress = address; dropoffLat = latitude; dropoffLng = longitude
        binding?.customerEditOrderDestinationAddressEditText?.setText(address)
        binding?.customerEditOrderDestinationAddressLayout?.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
