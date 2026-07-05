package com.example.noisetelemetry.ui.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.noisetelemetry.ui.thresholds.ThresholdRepository
import com.example.noisetelemetry.ui.settings.OverlayRepository
import com.example.noisetelemetry.ui.viewmodel.NoiseDashboardViewModel
import com.example.noisetelemetry.ui.viewmodel.ThresholdConfigViewModel
import com.example.noisetelemetry.ui.viewmodel.SettingsViewModel
import com.example.noisetelemetry.ui.viewmodel.CameraReviewViewModel

class NoiseDashboardViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NoiseDashboardViewModel::class.java) -> NoiseDashboardViewModel(application) as T
            modelClass.isAssignableFrom(ThresholdConfigViewModel::class.java) -> {
                ThresholdConfigViewModel(ThresholdRepository(application)) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(OverlayRepository(application)) as T
            }
            modelClass.isAssignableFrom(CameraReviewViewModel::class.java) -> CameraReviewViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
