data class OccurrenceMetadata(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val maxRms: Float = 0f,
    val thresholdDb: Float = 0f,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val audioFilePath: String? = null,
    val imageFilePath: String? = null,
    val notes: String = "",
    val exportPackagePath: String? = null
) {
    fun getFormattedTimestamp(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(timestamp))
    }

    fun getCsvHeader(): String = "occurrence_id,timestamp_iso,max_rms_db,threshold_db,latitude,longitude,accuracy_m,audio_path,image_path,notes,export_package"

    fun toCsvRow(): String = listOf(
        id,
        getFormattedTimestamp(),
        "%.2f".format(maxRms),
        "%.2f".format(thresholdDb),
        latitude?.toString() ?: "",
        longitude?.toString() ?: "",
        accuracyMeters?.toString() ?: "",
        audioFilePath ?: "",
        imageFilePath ?: "",
        notes.replace(",", ";"),
        exportPackagePath ?: ""
    ).joinToString(",")
}
