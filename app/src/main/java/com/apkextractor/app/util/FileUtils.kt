package com.apkextractor.app.util

import java.text.DateFormat
import java.util.Date

object FileUtils {

    fun sanitizeFileName(appName: String, versionName: String, packageName: String): String {
        val raw = "${appName}_${versionName}_${packageName}.apk"
        return raw.replace(Regex("[^a-zA-Z0-9._\\-]"), "_")
            .replace(Regex("_+"), "_")
            .trimStart('_')
            .trimEnd('_')
            .let { if (it.endsWith(".apk")) it else "$it.apk" }
    }

    fun formatDate(timestamp: Long): String {
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        return dateFormat.format(Date(timestamp))
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            else -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
