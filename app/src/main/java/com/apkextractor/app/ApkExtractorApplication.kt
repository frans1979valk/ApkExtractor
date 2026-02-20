package com.apkextractor.app

import android.app.Application
import com.apkextractor.app.util.CacheCleanup

class ApkExtractorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CacheCleanup.cleanOldExports(this)
    }
}
