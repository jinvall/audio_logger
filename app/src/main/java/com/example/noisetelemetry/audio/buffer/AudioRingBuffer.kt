package com.example.noisetelemetry.audio.buffer

import android.media.AudioFormat
import android.media.AudioRecord

class AudioRingBuffer(private val capacityBytes: Int) {
    private val buffer = ByteArray(capacityBytes)
    private var writePos = 0
    private var totalWritten = 0L

    @Volatile
    private var isRunning = false

    fun start() { isRunning = true }
    fun stop() { isRunning = false }

    fun write(data: ByteArray, offset: Int, length: Int) {
        if (!isRunning || length <= 0) return
        var remaining = length
        var srcOffset = offset
        while (remaining > 0) {
            val chunk = minOf(remaining, capacityBytes - writePos)
            System.arraycopy(data, srcOffset, buffer, writePos, chunk)
            writePos = (writePos + chunk) % capacityBytes
            totalWritten += chunk
            remaining -= chunk
            srcOffset += chunk
        }
    }

    fun readLatest(destination: ByteArray, length: Int): Int {
        if (totalWritten < length) return 0
        val bytesToRead = minOf(length, destination.size)
        val startReadPos = (writePos - bytesToRead + capacityBytes) % capacityBytes
        if (startReadPos + bytesToRead <= capacityBytes) {
            System.arraycopy(buffer, startReadPos, destination, 0, bytesToRead)
        } else {
            val firstChunk = capacityBytes - startReadPos
            System.arraycopy(buffer, startReadPos, destination, 0, firstChunk)
            val secondChunk = bytesToRead - firstChunk
            System.arraycopy(buffer, 0, destination, firstChunk, secondChunk)
        }
        return bytesToRead
    }

    fun availableBytes(): Long = minOf(totalWritten, capacityBytes.toLong())
    fun isFull(): Boolean = totalWritten >= capacityBytes
    fun reset() {
        writePos = 0
        totalWritten = 0
    }
}
