package com.example.mobilefinalproject.ui.driver

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
import com.example.mobilefinalproject.databinding.FragmentDriverEditProfileBinding
import com.example.mobilefinalproject.viewmodels.DriverViewModel
import com.squareup.picasso.Picasso

class DriverEditProfileFragment : Fragment() {
    private val driverViewModel: DriverViewModel by activityViewModels()
    private var binding: FragmentDriverEditProfileBinding? = null
    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            binding?.driverEditProfileImageView?.let { imageView ->
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverEditProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe driver data and populate fields
        driverViewModel.driver.observe(viewLifecycleOwner) { driver ->
            if (driver != null) {
                binding?.driverEditProfileFullNameEditText?.setText(driver.fullName)
                binding?.driverEditProfileIdEditText?.setText(driver.id)
            }
        }

        // Setup button click listeners
        binding?.driverEditProfileSaveButton?.setOnClickListener {
            saveProfile()
        }

        binding?.driverEditProfileCancelButton?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.driverEditProfileChangePictureButton?.setOnClickListener {
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
        val updatedFullName = binding.driverEditProfileFullNameEditText.text.toString().trim()

        if (updatedFullName.isEmpty()) {
            binding.driverEditProfileFullNameEditText.error = "Full name cannot be empty"
            return
        }

        // Get current driver from ViewModel
        val currentDriver = driverViewModel.driver.value
        if (currentDriver != null) {
            val updatedDriver = currentDriver.copy(fullName = updatedFullName)
            driverViewModel.updateDriver(updatedDriver)
            findNavController().navigateUp()
        }
    }
}
