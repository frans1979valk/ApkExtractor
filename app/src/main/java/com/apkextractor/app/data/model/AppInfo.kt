package com.apkextractor.app.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val lastUpdateTime: Long,
    val apkSize: Long,
    val sourceDir: String,
    val isSystemApp: Boolean,
    val icon: Drawable?
)
