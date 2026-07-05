package com.example.noisetelemetry.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import com.example.noisetelemetry.audio.buffer.AudioRingBuffer
import com.example.noisetelemetry.audio.config.AudioConfig
import com.example.noisetelemetry.audio.rms.RmsDetector
import kotlinx.coroutines.*
import kotlin.math.log10
import kotlin.math.pow

class AudioCaptureService(
    private val context: Context,
    private val config: AudioConfig,
    private val ringBuffer: AudioRingBuffer,
    private val rmsDetector: RmsDetector,
    private val onTrigger: (Float, Float) -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private var isCapturing = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val buffer = ByteArray(config.recordingBufferSize)

    @Volatile
    var latestRmsDb: Float = -96f
        private set

    @Volatile
    var isTriggered: Boolean = false
        private set

    fun start() {
        if (isCapturing) return
        isCapturing = true
        ringBuffer.start()

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            config.sampleRate,
            config.channelConfig,
            config.audioFormat,
            config.recordingBufferSize
        )

        scope.launch(Dispatchers.IO) {
            captureLoop()
        }
    }

    @SuppressLint("MissingPermission")
    private fun captureLoop() {
        val recorder = audioRecord ?: return
        try {
            recorder.startRecording()
            while (isCapturing) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) {
                    ringBuffer.write(buffer, 0, read)
                    val rms = rmsDetector.process(buffer, 0, read)
                    latestRmsDb = if (rms > 0f) (20 * log10(rms.toDouble())).toFloat() else -96f
                    val thresholdLinear = 10.0.pow(latestRmsDb / 20.0).toFloat()
                    val crossed = rmsDetector.checkThreshold(thresholdLinear, 500L, System.currentTimeMillis())
                    if (crossed) {
                        isTriggered = true
                        rmsDetector.reset()
                        onTrigger(latestRmsDb, latestRmsDb)
                    } else {
                        isTriggered = false
                    }
                }
            }
        } catch (e: Exception) {
            // Handle capture error
        } finally {
            try { recorder.stop(); recorder.release() } catch (e: Exception) {}
            audioRecord = null
        }
    }

    fun stop() {
        isCapturing = false
        ringBuffer.stop()
        scope.cancel()
        try { audioRecord?.stop(); audioRecord?.release() } catch (e: Exception) {}
        audioRecord = null
    }
}
