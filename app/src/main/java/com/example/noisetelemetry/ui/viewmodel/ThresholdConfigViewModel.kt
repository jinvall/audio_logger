package com.example.noisetelemetry.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noisetelemetry.ui.thresholds.ThresholdProfile
import com.example.noisetelemetry.ui.thresholds.ThresholdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThresholdConfigViewModel(private val repository: ThresholdRepository) : ViewModel() {
    private val _profile = MutableStateFlow(ThresholdProfile.default())
    val profile: StateFlow<ThresholdProfile> = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            repository.profileFlow.collect { _profile.value = it }
        }
    }

    fun updateThreshold(newThreshold: Float) {
        viewModelScope.launch {
            repository.saveProfile(_profile.value.copy(thresholdDb = newThreshold))
        }
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch {
            repository.saveProfile(_profile.value.copy(notes = notes))
        }
    }

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveProfile(_profile.value.copy(isEnabled = enabled))
        }
    }
}
