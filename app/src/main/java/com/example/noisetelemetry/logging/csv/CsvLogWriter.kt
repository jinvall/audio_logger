package com.example.noisetelemetry.logging.csv

import com.example.noisetelemetry.camera.metadata.OccurrenceMetadata

class CsvLogWriter(private val storageManager: FileStorageManager) {
    private var currentDay: String = ""

    fun logEvent(metadata: OccurrenceMetadata) {
        val day = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date(metadata.timestamp))
        if (day != currentDay) {
            currentDay = day
        }
        val csvFile = storageManager.getCsvFile()
        storageManager.appendCsv(csvFile, metadata.getCsvHeader(), metadata.toCsvRow())
    }
}
