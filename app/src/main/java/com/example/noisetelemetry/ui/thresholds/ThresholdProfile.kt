data class ThresholdProfile(
    val id: String,
    val name: String,
    val thresholdDb: Float,
    val isEnabled: Boolean = true,
    val holdTimeMs: Long = 500,
    val notes: String = ""
) {
    companion object {
        fun default(): ThresholdProfile = ThresholdProfile(
            id = "default",
            name = "Default",
            thresholdDb = 60.0f
        )
    }
}
