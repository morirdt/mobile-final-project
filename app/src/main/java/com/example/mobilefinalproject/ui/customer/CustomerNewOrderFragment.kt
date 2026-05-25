package com.example.mobilefinalproject.ui.customer

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
import com.example.mobilefinalproject.network.dto.OrderCreateRequest
import com.example.mobilefinalproject.ui.common.LoadingOverlayController
import com.example.mobilefinalproject.viewmodels.OrderViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.squareup.picasso.Picasso

class CustomerNewOrderFragment : Fragment() {
    private val orderViewModel: OrderViewModel by activityViewModels()
    private var binding: FragmentCustomerNewOrderBinding? = null
    private var loadingOverlay: LoadingOverlayController? = null
    private var pickupLat: Double = 0.0
    private var pickupLng: Double = 0.0
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

        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, mapsApiKey)
        }
        isPlacesAutocompleteEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerNewOrderBinding.inflate(inflater, container, false)
        loadingOverlay = LoadingOverlayController(
            requireContext(),
            requireActivity().findViewById(android.R.id.content)
        )
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAddressPickers()
        setupImagePicker()
        setupFieldErrorClearing()
        setupButtonListeners()
        orderViewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) loadingOverlay?.show() else loadingOverlay?.hide()
        }

        orderViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                android.widget.Toast.makeText(requireContext(), error, android.widget.Toast.LENGTH_LONG).show()
                orderViewModel.clearError()
            }
        }
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
                    }                }
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
        if (isPlacesAutocompleteEnabled) {
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
        } else {
            binding?.customerNewOrderPickupAddressEditText?.isFocusableInTouchMode = true
            binding?.customerNewOrderPickupAddressEditText?.isClickable = true
            binding?.customerNewOrderPickupAddressEditText?.isFocusable = true

            binding?.customerNewOrderDestinationAddressEditText?.isFocusableInTouchMode = true
            binding?.customerNewOrderDestinationAddressEditText?.isClickable = true
            binding?.customerNewOrderDestinationAddressEditText?.isFocusable = true
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

    private fun validateForm(): Boolean {
        var isValid = true

        val pickupText = binding?.customerNewOrderPickupAddressEditText?.text?.toString()?.trim().orEmpty()
        val destinationText = binding?.customerNewOrderDestinationAddressEditText?.text?.toString()?.trim().orEmpty()

        if (pickupLat == 0.0 && pickupLng == 0.0 && pickupText.isBlank()) {
            binding?.customerNewOrderPickupAddressLayout?.error = "Pickup address is required"
            isValid = false
        } else {
            binding?.customerNewOrderPickupAddressLayout?.error = null
        }

        if (dropoffLat == 0.0 && dropoffLng == 0.0 && destinationText.isBlank()) {
            binding?.customerNewOrderDestinationAddressLayout?.error = "Destination address is required"
            isValid = false
        } else {
            binding?.customerNewOrderDestinationAddressLayout?.error = null
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
        val pickupAddress = binding?.customerNewOrderPickupAddressEditText?.text?.toString()?.trim().orEmpty()
        val dropoffAddress = binding?.customerNewOrderDestinationAddressEditText?.text?.toString()?.trim().orEmpty()

        if (pickupAddress.isBlank() || dropoffAddress.isBlank()) return

        val budgetDollars = binding?.customerNewOrderBudgetEditText?.text?.toString()?.trim()?.toDoubleOrNull() ?: return
        val priceCents = (budgetDollars * 100).toInt()
        val description = binding?.customerNewOrderDescriptionEditText?.text?.toString()?.trim()

        orderViewModel.createOrder(
            OrderCreateRequest(
                pickupAddress = pickupAddress,
                pickupLat = pickupLat,
                pickupLng = pickupLng,
                dropoffAddress = dropoffAddress,
                dropoffLat = dropoffLat,
                dropoffLng = dropoffLng,
                cargoDescription = description,
                priceCents = priceCents
            ),
            imageUri = selectedImageUri,
            onSuccess = {
                findNavController().navigate(com.example.mobilefinalproject.R.id.action_customerNewOrderFragment_to_customerHomeFragment)
            }
        )
    }

    private fun updatePickupLocation(address: String, latitude: Double, longitude: Double) {
        pickupLat = latitude
        pickupLng = longitude
        binding?.customerNewOrderPickupAddressEditText?.setText(address)
        binding?.customerNewOrderPickupAddressLayout?.error = null
    }

    private fun updateDestinationLocation(address: String, latitude: Double, longitude: Double) {
        dropoffLat = latitude
        dropoffLng = longitude
        binding?.customerNewOrderDestinationAddressEditText?.setText(address)
        binding?.customerNewOrderDestinationAddressLayout?.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingOverlay?.detach()
        loadingOverlay = null
        binding = null
    }
}
