package com.example.noisetelemetry.ui.thresholds

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "threshold_profiles")

class ThresholdRepository(private val context: Context) {
    private object PreferencesKeys {
        val PROFILE_ID = stringPreferencesKey("profile_id")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val THRESHOLD_DB = floatPreferencesKey("threshold_db")
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val HOLD_TIME_MS = longPreferencesKey("hold_time_ms")
        val NOTES = stringPreferencesKey("notes")
    }

    val profileFlow: Flow<ThresholdProfile> = context.dataStore.data.map { prefs ->
        ThresholdProfile(
            id = prefs[PreferencesKeys.PROFILE_ID] ?: "default",
            name = prefs[PreferencesKeys.PROFILE_NAME] ?: "Default",
            thresholdDb = prefs[PreferencesKeys.THRESHOLD_DB] ?: 60.0f,
            isEnabled = prefs[PreferencesKeys.IS_ENABLED] ?: true,
            holdTimeMs = prefs[PreferencesKeys.HOLD_TIME_MS] ?: 500L,
            notes = prefs[PreferencesKeys.NOTES] ?: ""
        )
    }

    suspend fun saveProfile(profile: ThresholdProfile) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PROFILE_ID] = profile.id
            prefs[PreferencesKeys.PROFILE_NAME] = profile.name
            prefs[PreferencesKeys.THRESHOLD_DB] = profile.thresholdDb
            prefs[PreferencesKeys.IS_ENABLED] = profile.isEnabled
            prefs[PreferencesKeys.HOLD_TIME_MS] = profile.holdTimeMs
            prefs[PreferencesKeys.NOTES] = profile.notes
        }
    }
}
