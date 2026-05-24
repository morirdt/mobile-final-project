package com.example.mobilefinalproject.ui.driver

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.BuildConfig
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.cache.ImageCacheManager
import com.example.mobilefinalproject.databinding.FragmentDriverEditProfileBinding
import com.example.mobilefinalproject.ui.common.LoadingOverlayController
import com.example.mobilefinalproject.viewmodels.DriverViewModel
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class DriverEditProfileFragment : Fragment() {
    private val driverViewModel: DriverViewModel by activityViewModels()
    private var binding: FragmentDriverEditProfileBinding? = null
    private var loadingOverlay: LoadingOverlayController? = null
    private var imageCacheManager: ImageCacheManager? = null
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            // Local URI preview — no caching needed, use Picasso directly.
            binding?.driverEditProfileImageView?.let { imageView ->
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
        binding = FragmentDriverEditProfileBinding.inflate(inflater, container, false)
        loadingOverlay = LoadingOverlayController(
            requireContext(),
            requireActivity().findViewById(android.R.id.content)
        )
        imageCacheManager = ImageCacheManager(requireContext().applicationContext)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        driverViewModel.userMe.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding?.driverEditProfileFullNameEditText?.setText(user.fullName)
                binding?.driverEditProfileEmailEditText?.setText(user.email)

                // Don't reload the image if the user already picked a new one from gallery.
                if (selectedImageUri == null && user.profileImageUrl != null) {
                    val profilePath = if (user.profileImageUrl.startsWith("/")) {
                        user.profileImageUrl.substring(1)
                    } else {
                        user.profileImageUrl
                    }
                    val imageUrl = "${BuildConfig.BASE_URL}${profilePath}"
                    Log.i("DriverEditProfileFragment", "Image URL: $imageUrl")
                    val imageView = binding?.driverEditProfileImageView ?: return@observe
                    val cacheManager = imageCacheManager ?: return@observe
                    viewLifecycleOwner.lifecycleScope.launch {
                        cacheManager.loadInto(
                            url = imageUrl,
                            imageView = imageView,
                            placeholderRes = R.drawable.ic_person
                        )
                    }
                }
            }
        }

        driverViewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) loadingOverlay?.show() else loadingOverlay?.hide()
        }

        driverViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                driverViewModel.clearError()
            }
        }

        binding?.driverEditProfileSaveButton?.setOnClickListener { saveProfile() }
        binding?.driverEditProfileCancelButton?.setOnClickListener { findNavController().navigateUp() }
        binding?.driverEditProfileChangePictureButton?.setOnClickListener { checkAndRequestGalleryPermission() }
    }

    private fun checkAndRequestGalleryPermission() {
        val permission = android.Manifest.permission.READ_MEDIA_IMAGES
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            else -> permissionLauncher.launch(permission)
        }
    }

    private fun saveProfile() {
        val currentBinding = binding ?: return
        val updatedFullName = currentBinding.driverEditProfileFullNameEditText.text.toString().trim()

        if (updatedFullName.isEmpty()) {
            currentBinding.driverEditProfileFullNameEditText.error = "Full name cannot be empty"
            return
        }

        driverViewModel.updateProfile(fullName = updatedFullName, imageUri = selectedImageUri, onSuccess = {
            findNavController().navigateUp()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingOverlay?.detach()
        loadingOverlay = null
        imageCacheManager = null
        binding = null
    }
}
