package com.mfhapps.trendingui.screens.masonry

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed class MasonryMediaResult {
    data object Success : MasonryMediaResult()
    data class Failure(val message: String) : MasonryMediaResult()
}

object MasonryMediaActions {

    suspend fun saveToGallery(
        context: Context,
        imageUrl: String,
        displayName: String,
    ): MasonryMediaResult = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = loadBitmap(context, imageUrl)
                ?: return@withContext MasonryMediaResult.Failure("Could not load image")
            val filename = "${sanitize(displayName)}_${System.currentTimeMillis()}.jpg"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/UITrends")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext MasonryMediaResult.Failure("Could not create media entry")
            resolver.openOutputStream(uri)?.use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)) {
                    return@withContext MasonryMediaResult.Failure("Could not write image")
                }
            } ?: return@withContext MasonryMediaResult.Failure("Could not open output stream")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            MasonryMediaResult.Success
        }.getOrElse { MasonryMediaResult.Failure(it.message ?: "Save failed") }
    }

    suspend fun shareImage(
        context: Context,
        imageUrl: String,
        title: String,
    ): MasonryMediaResult = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = loadBitmap(context, imageUrl)
                ?: return@withContext MasonryMediaResult.Failure("Could not load image")
            val cacheDir = File(context.cacheDir, "share").apply { mkdirs() }
            val file = File(cacheDir, "masonry_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)) {
                    return@withContext MasonryMediaResult.Failure("Could not prepare share file")
                }
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, title)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share image"))
            }
            MasonryMediaResult.Success
        }.getOrElse { MasonryMediaResult.Failure(it.message ?: "Share failed") }
    }

    private suspend fun loadBitmap(context: Context, imageUrl: String): Bitmap? {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()
        val result = loader.execute(request)
        val drawable = (result as? SuccessResult)?.drawable ?: return null
        return (drawable as? BitmapDrawable)?.bitmap
            ?: Bitmap.createBitmap(drawable.intrinsicWidth.coerceAtLeast(1), drawable.intrinsicHeight.coerceAtLeast(1), Bitmap.Config.ARGB_8888).also { bmp ->
                val canvas = android.graphics.Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
    }

    private fun sanitize(name: String): String =
        name.replace(Regex("[^A-Za-z0-9_-]"), "_").take(40).ifBlank { "masonry" }
}
