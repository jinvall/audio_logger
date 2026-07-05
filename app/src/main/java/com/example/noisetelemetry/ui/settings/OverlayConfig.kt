package com.example.noisetelemetry.ui.settings

data class OverlayConfig(
    val enabled: Boolean = true,
    val showLocation: Boolean = true,
    val jpegQuality: Int = 90
) {
    companion object {
        fun default(): OverlayConfig = OverlayConfig()
    }
}
