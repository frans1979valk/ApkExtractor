package com.apkextractor.app

import android.app.Application
import com.apkextractor.app.util.CacheCleanup
import com.apkextractor.app.util.LocaleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ApkExtractorApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Apply locale from preferences
        applicationScope.launch {
            LocaleManager.applyLocale(this@ApkExtractorApplication)
        }

        CacheCleanup.cleanOldExports(this)
    }
}
