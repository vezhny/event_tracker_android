package com.vezhny.eventtracker.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.eventtracker.R
import com.vezhny.eventtracker.ui.EmptyState
import com.vezhny.eventtracker.ui.settings.SettingsDialog
import com.vezhny.eventtracker.ui.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(settingsViewModel: SettingsViewModel = koinViewModel()) {
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    // Saveable: the dialog stays open while a language change recreates the activity.
    var settingsOpen by rememberSaveable { mutableStateOf(false) }

    if (settingsOpen) {
        SettingsDialog(
            themeMode = themeMode,
            language = settingsViewModel.language(),
            onThemeMode = settingsViewModel::setThemeMode,
            onLanguage = settingsViewModel::setLanguage,
            onDismiss = { settingsOpen = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { settingsOpen = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
        },
    ) { padding ->
        EmptyState(
            icon = Icons.Filled.DateRange,
            title = stringResource(R.string.events_empty_title),
            text = stringResource(R.string.events_empty_text),
            modifier = Modifier.padding(padding),
        )
    }
}
