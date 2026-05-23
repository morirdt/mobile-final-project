package com.example.mobilefinalproject.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.mobilefinalproject.BuildConfig
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.cache.ImageCacheManager
import com.example.mobilefinalproject.session.UserSessionManager
import com.example.mobilefinalproject.viewmodels.CustomerViewModel
import com.example.mobilefinalproject.databinding.FragmentCustomerProfileBinding
import kotlinx.coroutines.launch

class CustomerProfileFragment : Fragment() {
    private val customerViewModel: CustomerViewModel by activityViewModels()
    private var binding: FragmentCustomerProfileBinding? = null
    private var imageCacheManager: ImageCacheManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerProfileBinding.inflate(inflater, container, false)
        imageCacheManager = ImageCacheManager(requireContext().applicationContext)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerViewModel.userMe.observe(viewLifecycleOwner) { user ->
            binding?.customerProfileNameTextView?.text = user?.fullName
            
            // Load profile image from server
            if (user?.profileImageUrl != null) {
                val profilePath = if (user.profileImageUrl.startsWith("/")) {
                    user.profileImageUrl.substring(1)
                } else {
                    user.profileImageUrl
                }
                val imageUrl = "${BuildConfig.BASE_URL}${profilePath}"
                val imageView = binding?.customerProfileImageView
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

        binding?.customerProfileEditButton?.setOnClickListener {
            findNavController().navigate(R.id.action_customerProfileFragment_to_customerEditProfileFragment)
        }

        binding?.customerProfileLogoutButton?.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        customerViewModel.clearCustomer()
        UserSessionManager.clearSession(requireContext())
        val parentNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        parentNavController.navigate(R.id.action_global_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imageCacheManager = null
        binding = null
    }
}
