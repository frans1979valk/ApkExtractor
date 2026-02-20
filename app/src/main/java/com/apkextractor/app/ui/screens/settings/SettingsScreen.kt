package com.apkextractor.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apkextractor.app.BuildConfig
import com.apkextractor.app.R
import com.apkextractor.app.data.model.SortOrder
import com.apkextractor.app.util.LocaleManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val showSystemApps by viewModel.showSystemApps.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val devModeEnabled by viewModel.devModeEnabled.collectAsStateWithLifecycle()
    val devForcedLocale by viewModel.devForcedLocale.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.show_system_apps)) },
                trailingContent = {
                    Switch(
                        checked = showSystemApps,
                        onCheckedChange = { viewModel.setShowSystemApps(it) }
                    )
                },
                modifier = Modifier.clickable { viewModel.setShowSystemApps(!showSystemApps) }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = {
                    Text(
                        stringResource(R.string.sort_selection),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )

            RadioButtonItem(
                text = stringResource(R.string.sort_name_az),
                selected = sortOrder == SortOrder.NAME_AZ,
                onClick = { viewModel.setSortOrder(SortOrder.NAME_AZ) }
            )

            RadioButtonItem(
                text = stringResource(R.string.sort_recently_updated),
                selected = sortOrder == SortOrder.RECENTLY_UPDATED,
                onClick = { viewModel.setSortOrder(SortOrder.RECENTLY_UPDATED) }
            )

            // Developer mode (only show in debug builds or if already enabled)
            if (BuildConfig.DEBUG || devModeEnabled) {
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text(stringResource(R.string.developer_mode)) },
                    trailingContent = {
                        Switch(
                            checked = devModeEnabled,
                            onCheckedChange = { viewModel.setDevModeEnabled(it) }
                        )
                    },
                    modifier = Modifier.clickable { viewModel.setDevModeEnabled(!devModeEnabled) }
                )
            }

            // Language picker (only when dev mode is enabled)
            if (devModeEnabled) {
                HorizontalDivider()

                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(R.string.language_dev),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                LocaleManager.getAvailableLocales().forEach { localeOption ->
                    val nameResId = when (localeOption.nameResKey) {
                        "use_system_language" -> R.string.use_system_language
                        "language_english" -> R.string.language_english
                        "language_dutch" -> R.string.language_dutch
                        "language_german" -> R.string.language_german
                        "language_hindi" -> R.string.language_hindi
                        "language_spanish" -> R.string.language_spanish
                        "language_french" -> R.string.language_french
                        else -> R.string.use_system_language
                    }

                    RadioButtonItem(
                        text = stringResource(nameResId),
                        selected = devForcedLocale == localeOption.code,
                        onClick = { viewModel.setDevForcedLocale(localeOption.code) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RadioButtonItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
