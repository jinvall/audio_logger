package com.example.noisetelemetry.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.noisetelemetry.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, NoiseDashboardFragment())
                        .commit()
                    true
                }
                R.id.nav_threshold -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ThresholdConfigFragment())
                        .commit()
                    true
                }
                R.id.nav_camera -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CameraReviewFragment())
                        .commit()
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SettingsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_dashboard
        }
    }
}
