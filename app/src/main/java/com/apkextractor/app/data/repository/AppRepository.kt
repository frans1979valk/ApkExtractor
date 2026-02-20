package com.apkextractor.app.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.apkextractor.app.data.model.AppInfo
import java.io.File

class AppRepository(private val context: Context) {

    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }

        return packages.mapNotNull { packageInfo ->
            val appInfo = packageInfo.applicationInfo ?: return@mapNotNull null
            AppInfo(
                name = appInfo.loadLabel(pm).toString(),
                packageName = packageInfo.packageName,
                versionName = packageInfo.versionName ?: "",
                versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
                lastUpdateTime = packageInfo.lastUpdateTime,
                apkSize = try { File(appInfo.sourceDir).length() } catch (_: Exception) { 0L },
                sourceDir = appInfo.sourceDir ?: "",
                isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0,
                icon = try { appInfo.loadIcon(pm) } catch (_: Exception) { null }
            )
        }
    }

    fun getAppInfo(packageName: String): AppInfo? {
        val pm = context.packageManager
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            return null
        }

        val appInfo = packageInfo.applicationInfo ?: return null
        return AppInfo(
            name = appInfo.loadLabel(pm).toString(),
            packageName = packageInfo.packageName,
            versionName = packageInfo.versionName ?: "",
            versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
            lastUpdateTime = packageInfo.lastUpdateTime,
            apkSize = try { File(appInfo.sourceDir).length() } catch (_: Exception) { 0L },
            sourceDir = appInfo.sourceDir ?: "",
            isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0,
            icon = try { appInfo.loadIcon(pm) } catch (_: Exception) { null }
        )
    }
}
