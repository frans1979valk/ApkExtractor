package com.apkextractor.app.util

import android.content.Context
import java.io.File

object CacheCleanup {

    private const val MAX_AGE_MS = 24 * 60 * 60 * 1000L // 24 hours

    fun cleanOldExports(context: Context) {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) return

        val cutoff = System.currentTimeMillis() - MAX_AGE_MS
        exportDir.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }
}
