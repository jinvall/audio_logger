package com.example.noisetelemetry.camera.metadata

import android.graphics.*
import android.media.ExifInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImageOverlayRenderer(private val overlayConfig: OverlayConfig) {
    fun renderAndSave(source: Bitmap, metadata: OccurrenceMetadata, outputFile: File) {
        val result = if (overlayConfig.enabled) renderOverlay(source, metadata) else source
        saveWithExif(result, outputFile, metadata)
        if (result != source && !result.isRecycled) result.recycle()
    }

    private fun renderOverlay(source: Bitmap, metadata: OccurrenceMetadata): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(source, 0f, 0f, null)

        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 36f
            isAntiAlias = true
            setShadowLayer(4f, 0f, 0f, Color.BLACK)
        }

        val lines = mutableListOf<String>()
        lines.add("NOISE EVENT ${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(metadata.timestamp))}")
        lines.add("Max: ${"%.1f".format(metadata.maxRms)} dB | Thresh: ${"%.1f".format(metadata.thresholdDb)} dB")
        if (overlayConfig.showLocation && metadata.latitude != null) {
            lines.add("GPS: %.5f, %.5f".format(metadata.latitude, metadata.longitude))
        }
        lines.add("Event ID: ${metadata.id.takeLast(8)}")

        var y = 60f
        lines.forEach { line ->
            canvas.drawText(line, 20f, y, paint)
            y += 48f
        }

        return result
    }

    private fun saveWithExif(bitmap: Bitmap, file: File, metadata: OccurrenceMetadata) {
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, overlayConfig.jpegQuality, out)
        }
        try {
            val exif = ExifInterface(file.absolutePath)
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, "Noise event - max ${"%.2f".format(metadata.maxRms)} dB")
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, metadata.toCsvRow())
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, "NoiseTelemetryApp v1.0")
            exif.setAttribute(ExifInterface.TAG_DATETIME, SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(Date(metadata.timestamp)))
            metadata.latitude?.let { lat ->
                exif.setLatLong(lat, metadata.longitude ?: 0.0)
            }
            exif.saveAttributes()
        } catch (e: Exception) {
            // EXIF write failure - image still saved
        }
    }
}
