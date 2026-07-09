package com.smartfarm.app.util

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Helper for creating a temp image file + its content:// Uri (for camera capture via FileProvider). */
object ImageFileUtils {
    fun createImageFile(context: Context): Pair<File, android.net.Uri> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "IMG_${timestamp}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return file to uri
    }
}
