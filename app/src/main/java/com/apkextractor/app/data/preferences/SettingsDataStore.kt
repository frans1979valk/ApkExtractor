package com.apkextractor.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.apkextractor.app.data.model.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val SHOW_SYSTEM_APPS = booleanPreferencesKey("show_system_apps")
        private val SORT_ORDER = stringPreferencesKey("sort_order")
        private val DEV_MODE_ENABLED = booleanPreferencesKey("dev_mode_enabled")
        private val DEV_FORCED_LOCALE = stringPreferencesKey("dev_forced_locale")
    }

    val showSystemApps: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_SYSTEM_APPS] ?: false
    }

    val sortOrder: Flow<SortOrder> = context.dataStore.data.map { preferences ->
        val name = preferences[SORT_ORDER] ?: SortOrder.NAME_AZ.name
        try {
            SortOrder.valueOf(name)
        } catch (_: IllegalArgumentException) {
            SortOrder.NAME_AZ
        }
    }

    val devModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DEV_MODE_ENABLED] ?: false
    }

    val devForcedLocale: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DEV_FORCED_LOCALE]
    }

    suspend fun setShowSystemApps(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_SYSTEM_APPS] = show
        }
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.dataStore.edit { preferences ->
            preferences[SORT_ORDER] = order.name
        }
    }

    suspend fun setDevModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEV_MODE_ENABLED] = enabled
        }
    }

    suspend fun setDevForcedLocale(locale: String?) {
        context.dataStore.edit { preferences ->
            if (locale != null) {
                preferences[DEV_FORCED_LOCALE] = locale
            } else {
                preferences.remove(DEV_FORCED_LOCALE)
            }
        }
    }
}
