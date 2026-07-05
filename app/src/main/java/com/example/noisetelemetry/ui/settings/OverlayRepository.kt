package com.example.noisetelemetry.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "overlay_settings")

class OverlayRepository(private val context: Context) {
    private object Keys {
        val ENABLED = booleanPreferencesKey("overlay_enabled")
        val SHOW_LOCATION = booleanPreferencesKey("overlay_show_location")
        val JPEG_QUALITY = intPreferencesKey("overlay_jpeg_quality")
    }

    val overlayFlow: Flow<OverlayConfig> = context.dataStore.data.map { prefs ->
        OverlayConfig(
            enabled = prefs[Keys.ENABLED] ?: true,
            showLocation = prefs[Keys.SHOW_LOCATION] ?: true,
            jpegQuality = prefs[Keys.JPEG_QUALITY] ?: 90
        )
    }

    suspend fun saveConfig(config: OverlayConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ENABLED] = config.enabled
            prefs[Keys.SHOW_LOCATION] = config.showLocation
            prefs[Keys.JPEG_QUALITY] = config.jpegQuality
        }
    }
}
