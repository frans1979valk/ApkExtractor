package com.apkextractor.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apkextractor.app.data.model.SortOrder
import com.apkextractor.app.data.preferences.SettingsDataStore
import com.apkextractor.app.util.LocaleManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)

    val showSystemApps: StateFlow<Boolean> = settingsDataStore.showSystemApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val sortOrder: StateFlow<SortOrder> = settingsDataStore.sortOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOrder.NAME_AZ)

    val devModeEnabled: StateFlow<Boolean> = settingsDataStore.devModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val devForcedLocale: StateFlow<String?> = settingsDataStore.devForcedLocale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setShowSystemApps(show: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setShowSystemApps(show)
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            settingsDataStore.setSortOrder(order)
        }
    }

    fun setDevModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDevModeEnabled(enabled)
        }
    }

    fun setDevForcedLocale(locale: String?) {
        viewModelScope.launch {
            settingsDataStore.setDevForcedLocale(locale)
            LocaleManager.applyLocale(getApplication())
        }
    }
}
