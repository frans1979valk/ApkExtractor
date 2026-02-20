package com.apkextractor.app.ui.screens.applist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apkextractor.app.data.model.AppInfo
import com.apkextractor.app.data.model.SortOrder
import com.apkextractor.app.data.preferences.SettingsDataStore
import com.apkextractor.app.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val settingsDataStore = SettingsDataStore(application)

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val showSystemApps: StateFlow<Boolean> = settingsDataStore.showSystemApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val sortOrder: StateFlow<SortOrder> = settingsDataStore.sortOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOrder.NAME_AZ)

    val filteredApps: StateFlow<List<AppInfo>> = combine(
        _allApps, _searchQuery, showSystemApps, sortOrder
    ) { apps, query, showSystem, sort ->
        apps.filter { app ->
            (showSystem || !app.isSystemApp) &&
                    (query.isBlank() ||
                            app.name.contains(query, ignoreCase = true) ||
                            app.packageName.contains(query, ignoreCase = true))
        }.let { filtered ->
            when (sort) {
                SortOrder.NAME_AZ -> filtered.sortedBy { it.name.lowercase() }
                SortOrder.RECENTLY_UPDATED -> filtered.sortedByDescending { it.lastUpdateTime }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val apps = repository.getInstalledApps()
            _allApps.value = apps
            _isLoading.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            settingsDataStore.setSortOrder(order)
        }
    }
}
