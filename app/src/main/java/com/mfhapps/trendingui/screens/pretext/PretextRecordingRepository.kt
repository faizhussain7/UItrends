package com.mfhapps.trendingui.screens.pretext

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.util.UUID

private val Context.pretextRecordingsStore: DataStore<Preferences> by preferencesDataStore(
    name = "pretext_recordings",
)

@Serializable
data class PretextRecording(
    val id: String,
    val displayName: String,
    val contentUri: String,
    val createdAtMillis: Long,
    val sizeBytes: Long,
) {
    fun uri(): Uri = Uri.parse(contentUri)
}

class PretextRecordingRepository(
    private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    val recordings: Flow<List<PretextRecording>> =
        context.pretextRecordingsStore.data.map { prefs ->
            val raw = prefs[Keys.index].orEmpty()
            if (raw.isBlank()) {
                emptyList()
            } else {
                runCatching {
                    json.decodeFromString<List<PretextRecording>>(raw)
                }.getOrDefault(emptyList())
                    .sortedByDescending { it.createdAtMillis }
            }
        }

    suspend fun persistRecording(sourceFile: File): PretextRecording? = withContext(Dispatchers.IO) {
        if (!sourceFile.exists() || sourceFile.length() <= 0L) return@withContext null
        val displayName = "pretext_${System.currentTimeMillis()}.mp4"
        val values = android.content.ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/UITrends/Pretext")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: return@withContext null
        resolver.openOutputStream(uri)?.use { out ->
            FileInputStream(sourceFile).use { input -> input.copyTo(out) }
        } ?: run {
            resolver.delete(uri, null, null)
            return@withContext null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        sourceFile.delete()
        val recording = PretextRecording(
            id = UUID.randomUUID().toString(),
            displayName = displayName,
            contentUri = uri.toString(),
            createdAtMillis = System.currentTimeMillis(),
            sizeBytes = querySize(uri),
        )
        appendRecording(recording)
        recording
    }

    suspend fun deleteRecording(recording: PretextRecording): Boolean = withContext(Dispatchers.IO) {
        val uri = recording.uri()
        val deleted = runCatching {
            context.contentResolver.delete(uri, null, null) > 0
        }.getOrDefault(false)
        removeRecording(recording.id)
        deleted
    }

    private suspend fun appendRecording(recording: PretextRecording) {
        context.pretextRecordingsStore.edit { prefs ->
            val current = runCatching {
                json.decodeFromString<List<PretextRecording>>(prefs[Keys.index].orEmpty())
            }.getOrDefault(emptyList())
            prefs[Keys.index] = json.encodeToString(current + recording)
        }
    }

    private suspend fun removeRecording(id: String) {
        context.pretextRecordingsStore.edit { prefs ->
            val current = runCatching {
                json.decodeFromString<List<PretextRecording>>(prefs[Keys.index].orEmpty())
            }.getOrDefault(emptyList())
            prefs[Keys.index] = json.encodeToString(current.filterNot { it.id == id })
        }
    }

    private fun querySize(uri: Uri): Long {
        val projection = arrayOf(MediaStore.Video.Media.SIZE)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getLong(index)
            }
        }
        return 0L
    }

    private companion object Keys {
        val index = stringPreferencesKey("recordings_index")
    }
}
