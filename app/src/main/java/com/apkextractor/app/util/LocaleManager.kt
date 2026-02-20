package com.apkextractor.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.apkextractor.app.BuildConfig
import com.apkextractor.app.data.preferences.SettingsDataStore
import kotlinx.coroutines.flow.first
import java.util.Locale

object LocaleManager {

    suspend fun applyLocale(context: Context) {
        val settingsDataStore = SettingsDataStore(context)
        val devModeEnabled = settingsDataStore.devModeEnabled.first()
        val forcedLocale = settingsDataStore.devForcedLocale.first()

        // Only apply forced locale if dev mode is enabled (or debug build)
        if ((BuildConfig.DEBUG || devModeEnabled) && forcedLocale != null) {
            val localeList = LocaleListCompat.forLanguageTags(forcedLocale)
            AppCompatDelegate.setApplicationLocales(localeList)
        } else {
            // Use system language
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        }
    }

    fun getAvailableLocales(): List<LocaleOption> = listOf(
        LocaleOption(null, "use_system_language"),
        LocaleOption("en", "language_english"),
        LocaleOption("nl", "language_dutch"),
        LocaleOption("de", "language_german"),
        LocaleOption("hi", "language_hindi"),
        LocaleOption("es", "language_spanish"),
        LocaleOption("fr", "language_french")
    )

    data class LocaleOption(
        val code: String?, // null = system language
        val nameResKey: String
    )
}
