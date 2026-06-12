package com.eim.callapp.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.util.concurrent.TimeUnit

object FileUtils {

    fun getRecordingsDirectory(context: Context): File {
        val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use app-specific external storage on Android 10+
            File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "EIM_Recordings")
        } else {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "EIM_Recordings"
            )
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "%.1f MB".format(bytes / (1024.0 * 1024))
        }
    }

    fun formatDuration(seconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(seconds)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val secs = seconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, secs)
        } else {
            "%d:%02d".format(minutes, secs)
        }
    }

    fun deleteFile(filePath: String): Boolean {
        if (filePath.isEmpty()) return false
        val file = File(filePath)
        return if (file.exists()) file.delete() else false
    }
}
