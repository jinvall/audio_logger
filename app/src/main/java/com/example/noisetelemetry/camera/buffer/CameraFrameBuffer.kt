package com.example.noisetelemetry.camera.buffer

import android.graphics.Bitmap

class CameraFrameBuffer(private val maxFrames: Int) {
    private val frames = ArrayDeque<Bitmap>(maxFrames)
    private val lock = Any()

    fun addFrame(frame: Bitmap) = synchronized(lock) {
        if (frames.size >= maxFrames) {
            val oldest = frames.removeFirst()
            if (!oldest.isRecycled) oldest.recycle()
        }
        frames.addLast(frame.copy(Bitmap.Config.ARGB_8888, false))
    }

    fun getBestFrame(): Bitmap? = synchronized(lock) {
        if (frames.isEmpty()) return null
        return frames.last().copy(Bitmap.Config.ARGB_8888, false)
    }

    fun clear() = synchronized(lock) {
        while (frames.isNotEmpty()) {
            val bmp = frames.removeFirst()
            if (!bmp.isRecycled) bmp.recycle()
        }
    }

    fun size(): Int = frames.size
}
