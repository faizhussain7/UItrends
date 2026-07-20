package com.mfhapps.trendingui.screens.pretext

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream

object PretextVideoActions {

    fun createOutputFile(context: Context): File {
        val dir = File(context.cacheDir, "pretext_videos").apply { mkdirs() }
        return File(dir, "pretext_${System.currentTimeMillis()}.mp4")
    }

    fun saveToMovies(context: Context, source: File): Boolean {
        return runCatching {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, source.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Video.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_MOVIES + "/UITrends/Pretext",
                    )
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: return false
            resolver.openOutputStream(uri)?.use { out ->
                FileInputStream(source).use { input -> input.copyTo(out) }
            } ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            true
        }.getOrDefault(false)
    }

    fun share(context: Context, source: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            source,
        )
        shareUriWithChooser(context, uri)
    }

    fun shareUriWithChooser(context: Context, uri: Uri) {
        val intent = buildVideoSendIntent(context, uri)
        context.startActivity(Intent.createChooser(intent, "Share recording"))
    }

    fun shareUriToPackage(context: Context, uri: Uri, packageName: String) {
        val intent = buildVideoSendIntent(context, uri).apply {
            setPackage(packageName)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { shareUriWithChooser(context, uri) }
    }

    @Deprecated("Use shareUriWithChooser or PretextVideoShareSheet")
    fun shareUri(context: Context, uri: Uri) {
        shareUriWithChooser(context, uri)
    }

    private fun buildVideoSendIntent(context: Context, uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri("pretext_recording", uri)
        }
    }
}

