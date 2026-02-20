package com.apkextractor.app.ui.screens.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apkextractor.app.data.model.SortOrder
import com.apkextractor.app.data.preferences.SettingsDataStore
import com.apkextractor.app.util.DocumentFileHelper
import com.apkextractor.app.util.LocaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    val defaultSaveFolderUri: StateFlow<String?> = settingsDataStore.defaultSaveFolderUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val folderName: StateFlow<String?> = defaultSaveFolderUri.combine(
        MutableStateFlow(Unit)
    ) { uri, _ ->
        uri?.let { uriString ->
            try {
                DocumentFileHelper.getFolderName(getApplication(), Uri.parse(uriString))
            } catch (_: Exception) {
                null
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    fun setDefaultSaveFolderUri(uri: Uri) {
        viewModelScope.launch {
            // Take persistable permission
            try {
                val contentResolver = getApplication<Application>().contentResolver
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                settingsDataStore.setDefaultSaveFolderUri(uri.toString())
            } catch (e: Exception) {
                // Permission grant failed
                settingsDataStore.setDefaultSaveFolderUri(null)
            }
        }
    }

    fun clearDefaultSaveFolder() {
        viewModelScope.launch {
            // Release persistable permission
            defaultSaveFolderUri.value?.let { uriString ->
                try {
                    val contentResolver = getApplication<Application>().contentResolver
                    contentResolver.releasePersistableUriPermission(
                        Uri.parse(uriString),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Ignore if permission was already released
                }
            }
            settingsDataStore.setDefaultSaveFolderUri(null)
        }
    }
}
