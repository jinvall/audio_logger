package com.example.noisetelemetry.audio.rms

import com.example.noisetelemetry.audio.config.AudioConfig

class RmsDetector(private val config: AudioConfig) {
    private var smoothedRms: Float = 0f
    private var triggerStartTime: Long = 0
    var isTriggered: Boolean = false
        private set

    @Volatile
    var maxRms: Float = 0f
        private set

    fun process(pcmData: ByteArray, offset: Int, length: Int): Float {
        val sampleCount = length / config.bytesPerSample
        if (sampleCount == 0) return smoothedRms

        var sumSquares = 0L
        val sampleStride = config.bytesPerSample
        for (i in 0 until sampleCount) {
            val idx = offset + i * sampleStride
            val sample = ((pcmData[idx + 1].toInt() shl 8) or (pcmData[idx].toInt() and 0xFF)).toShort()
            val normalized = sample / 32768.0f
            sumSquares += (normalized * normalized * 32768 * 32768).toLong()
        }

        val meanSquare = sumSquares / sampleCount.toDouble()
        val rms = kotlin.math.sqrt(meanSquare).toFloat()
        smoothedRms = if (smoothedRms == 0f) rms else (config.smoothingAlpha * rms + (1 - config.smoothingAlpha) * smoothedRms)
        if (smoothedRms > maxRms) maxRms = smoothedRms
        return smoothedRms
    }

    fun checkThreshold(thresholdLinear: Float, holdTimeMs: Long, currentTimeMs: Long): Boolean {
        val crossed = smoothedRms > thresholdLinear
        if (crossed) {
            if (triggerStartTime == 0L) {
                triggerStartTime = currentTimeMs
            } else if (currentTimeMs - triggerStartTime >= holdTimeMs) {
                isTriggered = true
                return true
            }
        } else {
            triggerStartTime = 0L
            isTriggered = false
        }
        return false
    }

    fun reset() {
        smoothedRms = 0f
        triggerStartTime = 0L
        isTriggered = false
        maxRms = 0f
    }
}
