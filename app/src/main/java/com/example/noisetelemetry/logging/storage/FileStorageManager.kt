package com.example.noisetelemetry.logging.storage

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileStorageManager(private val baseDir: File) {
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

    init {
        baseDir.mkdirs()
    }

    fun getAudioFile(occurrenceId: String): File {
        val audioDir = File(baseDir, "audio").apply { mkdirs() }
        return File(audioDir, "event_${occurrenceId}_${System.currentTimeMillis()}.pcm")
    }

    fun getImageFile(occurrenceId: String): File {
        val imageDir = File(baseDir, "images").apply { mkdirs() }
        return File(imageDir, "capture_${occurrenceId}_${System.currentTimeMillis()}.jpg")
    }

    fun getCsvFile(): File {
        val csvDir = File(baseDir, "logs").apply { mkdirs() }
        return File(csvDir, "noise_events_${dateFormat.format(Date())}.csv")
    }

    fun getExportPackage(occurrenceId: String): File {
        val exportDir = File(baseDir, "exports").apply { mkdirs() }
        return File(exportDir, "package_${occurrenceId}_${System.currentTimeMillis()}.zip")
    }

    fun writePcmAudio(file: File, buffer: ByteArray, length: Int) {
        file.outputStream().use { it.write(buffer, 0, length) }
    }

    fun appendCsv(file: File, header: String, row: String) {
        if (!file.exists()) {
            file.writeText(header + "\n")
        }
        file.appendText(row + "\n")
    }

    fun prepareExport(metadata: OccurrenceMetadata, audioFile: File?, imageFile: File?, csvFile: File?): File? {
        val exportDir = File(baseDir, "exports").apply { mkdirs() }
        val exportFile = File(exportDir, "submission_${metadata.id}_${System.currentTimeMillis()}.zip")
        try {
            java.util.zip.ZipOutputStream(exportFile.outputStream()).use { zos ->
                listOfNotNull(audioFile, imageFile, csvFile).forEach { f ->
                    if (f.exists()) {
                        zos.putNextEntry(java.util.zip.ZipEntry(f.name))
                        f.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            return exportFile
        } catch (e: Exception) {
            return null
        }
    }
}
