package com.example.noisetelemetry.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noisetelemetry.audio.config.AudioConfig
import com.example.noisetelemetry.audio.buffer.AudioRingBuffer
import com.example.noisetelemetry.audio.rms.RmsDetector
import com.example.noisetelemetry.audio.AudioCaptureService
import com.example.noisetelemetry.camera.buffer.CameraFrameBuffer
import com.example.noisetelemetry.camera.CameraCaptureService
import com.example.noisetelemetry.logging.TelemetryLogger
import com.example.noisetelemetry.logging.storage.FileStorageManager
import com.example.noisetelemetry.logging.csv.CsvLogWriter
import com.example.noisetelemetry.ui.thresholds.ThresholdRepository
import com.example.noisetelemetry.ui.settings.OverlayRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.io.File
import kotlin.math.pow

class NoiseDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val audioConfig = AudioConfig()
    private val ringBuffer = AudioRingBuffer(
        (audioConfig.preTriggerSamples + audioConfig.postTriggerSamples) * audioConfig.bytesPerSample
    )
    private val rmsDetector = RmsDetector(audioConfig)
    private val thresholdRepo = ThresholdRepository(application)
    private val overlayRepo = OverlayRepository(application)
    private val storage = FileStorageManager(File(application.getExternalFilesDir(null), "NoiseTelemetry"))
    private val csvWriter = CsvLogWriter(storage)
    private val overlayConfig = OverlayConfig.default()
    private val overlayRenderer = ImageOverlayRenderer(overlayConfig)
    private val cameraBuffer = CameraFrameBuffer(30)
    private val cameraService = CameraCaptureService(application, cameraBuffer)
    private val telemetryLogger = TelemetryLogger(audioConfig, ringBuffer, cameraBuffer, storage, csvWriter, overlayRenderer)
    private var audioService: AudioCaptureService? = null

    private val _rmsDb = MutableStateFlow(-96f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _isAudioRunning = MutableStateFlow(false)
    val isAudioRunning: StateFlow<Boolean> = _isAudioRunning.asStateFlow()

    private val _isCameraRunning = MutableStateFlow(false)
    val isCameraRunning: StateFlow<Boolean> = _isCameraRunning.asStateFlow()

    private val _triggerCount = MutableStateFlow(0)
    val triggerCount: StateFlow<Int> = _triggerCount.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                thresholdRepo.profileFlow,
                overlayRepo.overlayFlow
            ) { profile, overlay ->
                profile to overlay
            }.collect { (profile, _) ->
                if (profile.isEnabled) {
                    rmsDetector.reset()
                }
            }
        }
    }

    fun startAudio() {
        if (_isAudioRunning.value) return
        audioService = AudioCaptureService(
            context = getApplication(),
            config = audioConfig,
            ringBuffer = ringBuffer,
            rmsDetector = rmsDetector
        ) { rmsDb, _ ->
            _triggerCount.value++
            viewModelScope.launch {
                telemetryLogger.emitTrigger(10f.pow(rmsDb / 20f), rmsDb, "Threshold breach")
            }
        }
        audioService?.start()
        _isAudioRunning.value = true
        viewModelScope.launch {
            while (_isAudioRunning.value) {
                _rmsDb.value = audioService?.latestRmsDb ?: -96f
                delay(100)
            }
        }
    }

    fun stopAudio() {
        audioService?.stop()
        audioService = null
        _isAudioRunning.value = false
    }

    fun startCamera(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        if (_isCameraRunning.value) return
        cameraService.start(lifecycleOwner)
        cameraService.startContinuousCapture(5000L)
        _isCameraRunning.value = true
    }

    fun stopCamera() {
        cameraService.stopContinuousCapture()
        cameraService.shutdown()
        _isCameraRunning.value = false
    }

    fun clearBuffers() {
        ringBuffer.reset()
        cameraBuffer.clear()
        rmsDetector.reset()
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        stopCamera()
        telemetryLogger.shutdown()
    }
}
