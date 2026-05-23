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
import com.example.mobilefinalproject.BuildConfig
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.FragmentCustomerEditProfileBinding
import com.example.mobilefinalproject.ui.common.LoadingOverlayController
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.squareup.picasso.Picasso

class CustomerEditProfileFragment : Fragment() {

    private val customerViewModel: CustomerViewModel by activityViewModels()
    private var binding: FragmentCustomerEditProfileBinding? = null
    private var loadingOverlay: LoadingOverlayController? = null
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
        loadingOverlay = LoadingOverlayController(
            requireContext(),
            requireActivity().findViewById(android.R.id.content)
        )
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerViewModel.userMe.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding?.customerEditProfileFullNameEditText?.setText(user.fullName)
                // Show the user's email in the read-only field instead of numeric ID
                binding?.customerEditProfileEmailEditText?.setText(user.email)
                
                // Load profile image from server
                if (user.profileImageUrl != null) {
                    val profilePath = if (user.profileImageUrl.startsWith("/")) {
                        user.profileImageUrl.substring(1)
                    } else {
                        user.profileImageUrl
                    }
                    val imageUrl = "${BuildConfig.BASE_URL}${profilePath}"
                    binding?.customerEditProfileImageView?.let { imageView ->
                        Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(imageView)
                    }
                }
            }
        }

        customerViewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) loadingOverlay?.show() else loadingOverlay?.hide()
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

        customerViewModel.updateProfile(fullName = updatedFullName, imageUri = selectedImageUri, onSuccess = {
            findNavController().navigateUp()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingOverlay?.detach()
        loadingOverlay = null
        binding = null
    }
}
