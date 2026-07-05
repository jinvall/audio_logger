package com.example.noisetelemetry.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.noisetelemetry.camera.buffer.CameraFrameBuffer
import com.example.noisetelemetry.camera.metadata.OccurrenceMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CameraReviewViewModel : ViewModel() {
    private val frameBuffer = CameraFrameBuffer(30)
    private val _selectedMetadata = MutableStateFlow<OccurrenceMetadata?>(null)
    val selectedMetadata: StateFlow<OccurrenceMetadata?> = _selectedMetadata.asStateFlow()

    fun selectFrame(metadata: OccurrenceMetadata) {
        _selectedMetadata.value = metadata
    }

    fun getBuffer(): CameraFrameBuffer = frameBuffer
}
