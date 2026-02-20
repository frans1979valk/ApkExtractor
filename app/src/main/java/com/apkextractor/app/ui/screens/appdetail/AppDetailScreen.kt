package com.apkextractor.app.ui.screens.appdetail

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.apkextractor.app.util.DocumentFileHelper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apkextractor.app.R
import com.apkextractor.app.ui.components.DrawableImage
import com.apkextractor.app.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    onBack: () -> Unit,
    viewModel: AppDetailViewModel = viewModel()
) {
    val appInfo by viewModel.appInfo.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportSuccessMsg = stringResource(R.string.export_success)
    val exportErrorMsg = stringResource(R.string.export_error)
    val shareErrorMsg = stringResource(R.string.share_error)
    val saveSuccessMsg = stringResource(R.string.save_success)
    val selectFolderFirstMsg = stringResource(R.string.select_folder_first)

    var showSystemAppDialog by remember { mutableStateOf(false) }
    var showUninstallDialog by remember { mutableStateOf(false) }
    var savedFolderUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri ->
        uri?.let { viewModel.exportApk(it) }
    }

    val folderPickerForSave = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.saveToPhone(it) }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AppDetailViewModel.UiState.ExportSuccess -> {
                snackbarHostState.showSnackbar(exportSuccessMsg)
                viewModel.resetUiState()
            }
            is AppDetailViewModel.UiState.ShareReady -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, state.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, null))
                viewModel.resetUiState()
            }
            is AppDetailViewModel.UiState.SaveSuccess -> {
                savedFolderUri = state.folderUri
                snackbarHostState.showSnackbar(saveSuccessMsg)
                viewModel.resetUiState()
            }
            is AppDetailViewModel.UiState.NoFolderSelected -> {
                snackbarHostState.showSnackbar(selectFolderFirstMsg)
                viewModel.resetUiState()
                // Optionally open folder picker
                folderPickerForSave.launch(null)
            }
            is AppDetailViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    // System app warning dialog
    if (showSystemAppDialog) {
        AlertDialog(
            onDismissRequest = { showSystemAppDialog = false },
            title = { Text(stringResource(R.string.system_app_warning_title)) },
            text = { Text(stringResource(R.string.system_app_warning_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showSystemAppDialog = false
                    val pkg = appInfo?.packageName ?: return@TextButton
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$pkg")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.open_app_info))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSystemAppDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Uninstall confirmation dialog
    val currentApp = appInfo
    if (showUninstallDialog && currentApp != null) {
        AlertDialog(
            onDismissRequest = { showUninstallDialog = false },
            title = { Text(stringResource(R.string.confirm_uninstall_title)) },
            text = {
                Text(stringResource(R.string.confirm_uninstall_message, currentApp.name))
            },
            confirmButton = {
                TextButton(onClick = {
                    showUninstallDialog = false
                    val intent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${currentApp.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        // Fallback: open system app info
                        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${currentApp.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(fallback)
                    }
                }) {
                    Text(
                        stringResource(R.string.confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appInfo?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        appInfo?.let { app ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DrawableImage(
                    drawable = app.icon,
                    contentDescription = app.name,
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = app.name,
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (app.isSystemApp) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AssistChip(
                        onClick = { showSystemAppDialog = true },
                        label = { Text(stringResource(R.string.system_app_badge)) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(
                            label = stringResource(R.string.version_name),
                            value = app.versionName
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = stringResource(R.string.version_code),
                            value = app.versionCode.toString()
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = stringResource(R.string.last_updated),
                            value = FileUtils.formatDate(app.lastUpdateTime)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = stringResource(R.string.apk_size),
                            value = FileUtils.formatFileSize(app.apkSize)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Export button
                Button(
                    onClick = { exportLauncher.launch(viewModel.getSuggestedFileName()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState is AppDetailViewModel.UiState.Idle
                ) {
                    if (uiState is AppDetailViewModel.UiState.Exporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.SaveAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (uiState is AppDetailViewModel.UiState.Exporting)
                            stringResource(R.string.exporting)
                        else
                            stringResource(R.string.export_apk)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Share button
                OutlinedButton(
                    onClick = { viewModel.prepareShare() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState is AppDetailViewModel.UiState.Idle
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.share_apk))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save to phone button
                OutlinedButton(
                    onClick = { viewModel.saveToPhone() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState is AppDetailViewModel.UiState.Idle
                ) {
                    if (uiState is AppDetailViewModel.UiState.SavingToPhone) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.SaveAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (uiState is AppDetailViewModel.UiState.SavingToPhone)
                            stringResource(R.string.saving_to_phone)
                        else
                            stringResource(R.string.save_to_phone)
                    )
                }

                // Open folder button (shown after successful save)
                if (savedFolderUri != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val intent = DocumentFileHelper.createOpenFolderIntent(savedFolderUri!!)
                            if (intent != null) {
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    // Fallback: show message
                                    androidx.compose.runtime.rememberCoroutineScope().launch {
                                        snackbarHostState.showSnackbar(
                                            "Open your Files app and navigate to the selected folder"
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.open_folder))
                    }
                }

                // Remove button — only for user apps
                if (!app.isSystemApp) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showUninstallDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.remove_app))
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
