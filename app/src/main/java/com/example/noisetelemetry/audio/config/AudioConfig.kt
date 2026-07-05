package com.example.noisetelemetry.audio.config

import kotlinx.coroutines.flow.Flow

data class AudioConfig(
    val sampleRate: Int = 16000,
    val channelConfig: Int = android.media.AudioFormat.CHANNEL_IN_MONO,
    val audioFormat: Int = android.media.AudioFormat.ENCODING_PCM_16BIT,
    val bufferSizeMultiplier: Int = 2,
    val preTriggerDurationMs: Int = 1000,
    val postTriggerDurationMs: Int = 2000,
    val rmsWindowSizeSamples: Int = 512,
    val smoothingAlpha: Float = 0.1f
) {
    val bytesPerSample: Int = if (audioFormat == android.media.AudioFormat.ENCODING_PCM_16BIT) 2 else 1
    val channelCount: Int = if (channelConfig == android.media.AudioFormat.CHANNEL_IN_MONO) 1 else 2
    val preTriggerSamples: Int = (sampleRate * preTriggerDurationMs) / 1000
    val postTriggerSamples: Int = (sampleRate * postTriggerDurationMs) / 1000
    val minRecorderBufferSize: Int = android.media.AudioRecord.getMinBufferSize(
        sampleRate, channelConfig, audioFormat
    )
    val recordingBufferSize: Int = minRecorderBufferSize * bufferSizeMultiplier
}
