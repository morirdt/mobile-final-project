package com.example.mobilefinalproject.ui.driver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mobilefinalproject.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class DriverHomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_home)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Set default fragment (Active Orders)
        if (savedInstanceState == null) {
            loadFragment(ActiveOrdersFragment())
        }

        // Handle bottom navigation item selection
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_active -> {
                    loadFragment(ActiveOrdersFragment())
                    true
                }
                R.id.nav_past -> {
                    loadFragment(PastDeliveriesFragment())
                    true
                }
                R.id.nav_map -> {
                    loadFragment(MapFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.driver_fragment_container, fragment)
            .commit()
    }
}
