package com.example.mobilefinalproject.ui.driver

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.BuildConfig
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.cache.ImageCacheManager
import com.example.mobilefinalproject.databinding.FragmentDriverProfileBinding
import com.example.mobilefinalproject.session.UserSessionManager
import com.example.mobilefinalproject.viewmodels.DriverViewModel
import android.widget.Toast
import kotlinx.coroutines.launch

class DriverProfileFragment : Fragment() {
    private val driverViewModel: DriverViewModel by activityViewModels()
    private var binding: FragmentDriverProfileBinding? = null
    private var imageCacheManager: ImageCacheManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverProfileBinding.inflate(inflater, container, false)
        imageCacheManager = ImageCacheManager(requireContext().applicationContext)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        driverViewModel.userMe.observe(viewLifecycleOwner) { user ->
            Log.d("DriverProfileFragment", user.toString())
            binding?.driverProfileNameTextView?.text = user?.fullName

            // Load profile image from server
            if (user?.profileImageUrl != null) {
                val profilePath = if (user.profileImageUrl.startsWith("/")) {
                    user.profileImageUrl.substring(1)
                } else {
                    user.profileImageUrl
                }
                val imageUrl = "${BuildConfig.BASE_URL}${profilePath}"
                val imageView = binding?.profileImageView
                val cacheManager = imageCacheManager
                if (imageView != null && cacheManager != null) {
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

        binding?.driverProfileEditButton?.setOnClickListener {
            findNavController().navigate(R.id.action_driverProfileFragment_to_driverEditProfileFragment)
        }

        binding?.driverProfileLogoutButton?.setOnClickListener {
            logout()
        }

        driverViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                driverViewModel.clearError()
            }
        }
    }

    private fun logout() {
        // Set driver offline on the backend first, then clear session and navigate
        driverViewModel.setOfflineAndLogout {
            driverViewModel.clearDriver()
            UserSessionManager.clearSession(requireContext())
            val parentNavController =
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
            parentNavController.navigate(R.id.action_global_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imageCacheManager = null
        binding = null
    }
}

