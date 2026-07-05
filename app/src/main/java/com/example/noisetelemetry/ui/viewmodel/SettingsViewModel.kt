package com.example.noisetelemetry.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.noisetelemetry.ui.settings.OverlayConfig
import com.example.noisetelemetry.ui.settings.OverlayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val overlayRepository: OverlayRepository) : ViewModel() {
    private val _config = MutableStateFlow(OverlayConfig.default())
    val config: StateFlow<OverlayConfig> = _config.asStateFlow()

    init {
        viewModelScope.launch {
            overlayRepository.overlayFlow.collect { _config.value = it }
        }
    }

    fun setOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            overlayRepository.saveConfig(_config.value.copy(enabled = enabled))
        }
    }

    fun setShowLocation(show: Boolean) {
        viewModelScope.launch {
            overlayRepository.saveConfig(_config.value.copy(showLocation = show))
        }
    }

    fun setJpegQuality(quality: Int) {
        viewModelScope.launch {
            overlayRepository.saveConfig(_config.value.copy(jpegQuality = quality.coerceIn(50, 100)))
        }
    }
}
