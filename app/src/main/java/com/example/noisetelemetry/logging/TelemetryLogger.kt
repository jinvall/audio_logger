package com.example.noisetelemetry.logging

import com.example.noisetelemetry.audio.config.AudioConfig
import com.example.noisetelemetry.audio.buffer.AudioRingBuffer
import com.example.noisetelemetry.camera.metadata.OccurrenceMetadata
import com.example.noisetelemetry.camera.metadata.ImageOverlayRenderer
import com.example.noisetelemetry.camera.buffer.CameraFrameBuffer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.File

class TelemetryLogger(
    private val audioConfig: AudioConfig,
    private val audioRingBuffer: AudioRingBuffer,
    private val cameraFrameBuffer: CameraFrameBuffer,
    private val fileStorageManager: FileStorageManager,
    private val csvLogWriter: CsvLogWriter,
    private val imageOverlayRenderer: ImageOverlayRenderer
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val triggerFlow: SharedFlow<OccurrenceMetadata> = MutableSharedFlow(extraBufferCapacity = 1)

    companion object {
        const val RMS_FLOOR_DB = -96.0f
    }

    init {
        scope.launch {
            triggerFlow.collect { metadata -> processEvent(metadata) }
        }
    }

    fun linearToDb(linearRms: Float): Float {
        if (linearRms <= 0f) return RMS_FLOOR_DB
        val clamped = linearRms.coerceAtLeast(1.0E-10f)
        return (20 * kotlin.math.log10(clamped.toDouble())).toFloat()
    }

    fun dbToLinear(db: Float): Float = kotlin.math.pow(10.0, (db / 20.0)).toFloat()

    fun emitTrigger(maxRmsLinear: Float, thresholdDb: Float, notes: String = "") {
        val metadata = OccurrenceMetadata(
            maxRms = linearToDb(maxRmsLinear),
            thresholdDb = thresholdDb,
            notes = notes
        )
        (triggerFlow as MutableSharedFlow).tryEmit(metadata)
    }

    private fun processEvent(metadata: OccurrenceMetadata) {
        try {
            val preTriggerBytes = audioConfig.preTriggerSamples * audioConfig.bytesPerSample
            val postTriggerBytes = audioConfig.postTriggerSamples * audioConfig.bytesPerSample
            val totalBuffer = ByteArray(preTriggerBytes + postTriggerBytes)
            val bytesRead = audioRingBuffer.readLatest(totalBuffer, totalBuffer.size)

            val preTriggerPcm = if (bytesRead >= preTriggerBytes) {
                totalBuffer.copyOfRange(0, preTriggerBytes)
            } else {
                totalBuffer.copyOfRange(0, bytesRead)
            }

            val postTriggerPcm = if (bytesRead > preTriggerBytes) {
                totalBuffer.copyOfRange(preTriggerBytes, bytesRead)
            } else {
                ByteArray(0)
            }

            val totalAudio = ByteArray(preTriggerPcm.size + postTriggerPcm.size)
            System.arraycopy(preTriggerPcm, 0, totalAudio, 0, preTriggerPcm.size)
            System.arraycopy(postTriggerPcm, 0, totalAudio, preTriggerPcm.size, postTriggerPcm.size)

            val audioFile = fileStorageManager.getAudioFile(metadata.id)
            fileStorageManager.writePcmAudio(audioFile, totalAudio, totalAudio.size)
            val updated = metadata.copy(audioFilePath = audioFile.absolutePath)

            val bestFrame = cameraFrameBuffer.getBestFrame()
            if (bestFrame != null) {
                val imageFile = fileStorageManager.getImageFile(metadata.id)
                imageOverlayRenderer.renderAndSave(bestFrame, updated, imageFile)
                val finalMetadata = updated.copy(imageFilePath = imageFile.absolutePath)
                csvLogWriter.logEvent(finalMetadata)
                fileStorageManager.prepareExport(finalMetadata, audioFile, imageFile, fileStorageManager.getCsvFile())
            } else {
                csvLogWriter.logEvent(updated)
            }
        } catch (e: Exception) {
            // Log processing error silently for production use
        }
    }

    fun shutdown() {
        scope.cancel()
    }
}
