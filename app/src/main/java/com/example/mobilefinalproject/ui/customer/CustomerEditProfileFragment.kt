package com.example.mobilefinalproject.ui.customer

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.FragmentCustomerEditProfileBinding
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.squareup.picasso.Picasso

class CustomerEditProfileFragment : Fragment() {

    private val customerViewModel: CustomerViewModel by activityViewModels()
    private var binding: FragmentCustomerEditProfileBinding? = null
    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            binding?.customerEditProfileImageView?.let { imageView ->
                Picasso.get()
                    .load(selectedImageUri)
                    .into(imageView)
            }
        }
    }

    // Permission request launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerEditProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe customer data and populate fields
        customerViewModel.customer.observe(viewLifecycleOwner) { customer ->
            if (customer != null) {
                binding?.customerEditProfileFullNameEditText?.setText(customer.fullName)
                binding?.customerEditProfileIdEditText?.setText(customer.id)
            }
        }

        // Setup button click listeners
        binding?.customerEditProfileSaveButton?.setOnClickListener {
            saveProfile()
        }

        binding?.customerEditProfileCancelButton?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.customerEditProfileChangePictureButton?.setOnClickListener {
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

    private fun saveProfile() {
        val binding = binding ?: return
        val updatedFullName = binding.customerEditProfileFullNameEditText.text.toString().trim()

        if (updatedFullName.isEmpty()) {
            binding.customerEditProfileFullNameEditText.error = "Full name cannot be empty"
            return
        }

        // Get current customer from ViewModel
        val currentCustomer = customerViewModel.customer.value
        if (currentCustomer != null) {
            val updatedCustomer = currentCustomer.copy(fullName = updatedFullName)
            customerViewModel.updateCustomer(updatedCustomer)
            findNavController().navigateUp()
        }
    }
}
