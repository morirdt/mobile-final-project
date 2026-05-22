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
import com.example.mobilefinalproject.databinding.FragmentCustomerEditProfileBinding
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.squareup.picasso.Picasso

class CustomerEditProfileFragment : Fragment() {

    private val customerViewModel: CustomerViewModel by activityViewModels()
    private var binding: FragmentCustomerEditProfileBinding? = null
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            binding?.customerEditProfileImageView?.let { imageView ->
                Picasso.get().load(selectedImageUri).into(imageView)
            }
        }
    }

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

        customerViewModel.userMe.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding?.customerEditProfileFullNameEditText?.setText(user.fullName)
                binding?.customerEditProfileIdEditText?.setText(user.id.toString())
            }
        }

        binding?.customerEditProfileSaveButton?.setOnClickListener { saveProfile() }
        binding?.customerEditProfileCancelButton?.setOnClickListener { findNavController().navigateUp() }
        binding?.customerEditProfileChangePictureButton?.setOnClickListener { checkAndRequestGalleryPermission() }
    }

    private fun checkAndRequestGalleryPermission() {
        val permission = android.Manifest.permission.READ_MEDIA_IMAGES
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED ->
                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            else -> permissionLauncher.launch(permission)
        }
    }

    private fun saveProfile() {
        val binding = binding ?: return
        val updatedFullName = binding.customerEditProfileFullNameEditText.text.toString().trim()

        if (updatedFullName.isEmpty()) {
            binding.customerEditProfileFullNameEditText.error = "Full name cannot be empty"
            return
        }

        customerViewModel.updateProfile(fullName = updatedFullName, onSuccess = {
            findNavController().navigateUp()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
