package com.apkextractor.app.ui.screens.appdetail

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.apkextractor.app.data.model.AppInfo
import com.apkextractor.app.data.preferences.SettingsDataStore
import com.apkextractor.app.data.repository.AppRepository
import com.apkextractor.app.util.DocumentFileHelper
import com.apkextractor.app.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class AppDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val packageName: String = savedStateHandle.get<String>("packageName") ?: ""
    private val repository = AppRepository(application)
    private val settingsDataStore = SettingsDataStore(application)

    private val _appInfo = MutableStateFlow<AppInfo?>(null)
    val appInfo: StateFlow<AppInfo?> = _appInfo.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadAppInfo()
    }

    private fun loadAppInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _appInfo.value = repository.getAppInfo(packageName)
        }
    }

    fun exportApk(destinationUri: Uri) {
        val app = _appInfo.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Exporting
            try {
                val context = getApplication<Application>()
                val sourceFile = File(app.sourceDir)
                if (!sourceFile.exists() || !sourceFile.canRead()) {
                    throw IOException("Cannot read source APK")
                }
                context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                } ?: throw IOException("Could not open output stream")
                _uiState.value = UiState.ExportSuccess
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun prepareShare() {
        val app = _appInfo.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Exporting
            try {
                val context = getApplication<Application>()
                val exportDir = File(context.cacheDir, "exports")
                exportDir.mkdirs()
                val fileName = FileUtils.sanitizeFileName(app.name, app.versionName, app.packageName)
                val destFile = File(exportDir, fileName)
                File(app.sourceDir).copyTo(destFile, overwrite = true)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destFile
                )
                _uiState.value = UiState.ShareReady(uri)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to prepare share")
            }
        }
    }

    fun saveToPhone(folderUri: Uri? = null) {
        val app = _appInfo.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.SavingToPhone
            try {
                val context = getApplication<Application>()

                // Get folder URI from parameter or settings
                val targetFolderUri = folderUri ?: run {
                    val savedUri = settingsDataStore.defaultSaveFolderUri.first()
                    savedUri?.let { Uri.parse(it) }
                } ?: run {
                    _uiState.value = UiState.NoFolderSelected
                    return@launch
                }

                // Check folder accessibility
                if (!DocumentFileHelper.isFolderAccessible(context, targetFolderUri)) {
                    _uiState.value = UiState.Error("Folder not accessible")
                    return@launch
                }

                val fileName = FileUtils.sanitizeFileName(app.name, app.versionName, app.packageName)

                val result = DocumentFileHelper.saveApkToFolder(
                    context = context,
                    folderUri = targetFolderUri,
                    sourceApkPath = app.sourceDir,
                    filename = fileName
                ) { progress ->
                    // Could emit progress updates here if needed
                }

                result.fold(
                    onSuccess = { savedUri ->
                        _uiState.value = UiState.SaveSuccess(targetFolderUri, savedUri)
                    },
                    onFailure = { error ->
                        _uiState.value = UiState.Error(error.message ?: "Save failed")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to save")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    fun getSuggestedFileName(): String {
        val app = _appInfo.value ?: return "app.apk"
        return FileUtils.sanitizeFileName(app.name, app.versionName, app.packageName)
    }

    sealed interface UiState {
        data object Idle : UiState
        data object Exporting : UiState
        data object ExportSuccess : UiState
        data class ShareReady(val uri: Uri) : UiState
        data object SavingToPhone : UiState
        data class SaveSuccess(val folderUri: Uri, val fileUri: Uri) : UiState
        data object NoFolderSelected : UiState
        data class Error(val message: String) : UiState
    }
}
