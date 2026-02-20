package com.apkextractor.app.ui.screens.about

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apkextractor.app.data.preferences.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AboutViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)

    val devModeEnabled: StateFlow<Boolean> = settingsDataStore.devModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var tapCount = 0
    private val requiredTaps = 7

    fun onVersionTap(): Int? {
        if (devModeEnabled.value) return null // Already enabled

        tapCount++
        val remaining = requiredTaps - tapCount

        return when {
            remaining <= 0 -> {
                enableDevMode()
                null // Fully enabled
            }
            remaining <= 3 -> remaining // Show hint when close
            else -> null
        }
    }

    private fun enableDevMode() {
        viewModelScope.launch {
            settingsDataStore.setDevModeEnabled(true)
            tapCount = 0
        }
    }
}
