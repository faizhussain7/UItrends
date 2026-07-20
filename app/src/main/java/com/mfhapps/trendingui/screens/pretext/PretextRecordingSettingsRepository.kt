package com.mfhapps.trendingui.screens.pretext

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class PretextRecordingSettings(
    val qualityMode: PretextRecordingQualityMode = PretextRecordingQualityMode.Auto,
)

private val Context.pretextRecordingSettingsStore: DataStore<Preferences> by preferencesDataStore(
    name = "pretext_recording_settings",
)

class PretextRecordingSettingsRepository(
    private val context: Context,
) {
    private val qualityModeKey = stringPreferencesKey("quality_mode")

    val settings: Flow<PretextRecordingSettings> =
        context.pretextRecordingSettingsStore.data.map { prefs ->
            PretextRecordingSettings(
                qualityMode = prefs[qualityModeKey]
                    ?.let { runCatching { PretextRecordingQualityMode.valueOf(it) }.getOrNull() }
                    ?: PretextRecordingQualityMode.Auto,
            )
        }

    suspend fun setQualityMode(mode: PretextRecordingQualityMode) {
        context.pretextRecordingSettingsStore.edit { prefs ->
            prefs[qualityModeKey] = mode.name
        }
    }

    fun resolveProfile(settings: PretextRecordingSettings): PretextRecordingProfile =
        PretextRecordingProfileResolver.resolve(context, settings.qualityMode)
}
