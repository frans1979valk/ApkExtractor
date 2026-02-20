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
}
