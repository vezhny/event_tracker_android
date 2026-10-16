package com.vezhny.eventtracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.eventtracker.R
import com.vezhny.eventtracker.data.EventWithLast
import com.vezhny.eventtracker.ui.EmptyState
import com.vezhny.eventtracker.ui.formatLastOccurred
import com.vezhny.eventtracker.ui.settings.SettingsDialog
import com.vezhny.eventtracker.ui.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddEvent: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
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

    state.confirming?.let { event ->
        AlertDialog(
            onDismissRequest = viewModel::cancelConfirm,
            title = { Text(event.name) },
            text = { Text(stringResource(R.string.confirm_record)) },
            confirmButton = { TextButton(onClick = { viewModel.confirm() }) { Text(stringResource(R.string.yes)) } },
            dismissButton = { TextButton(onClick = viewModel::cancelConfirm) { Text(stringResource(R.string.no)) } },
        )
    }

    val recordedText = state.justRecorded?.let { stringResource(R.string.recorded, it.eventName) }
    val undoText = stringResource(R.string.undo)
    LaunchedEffect(state.justRecorded) {
        val record = state.justRecorded ?: return@LaunchedEffect
        val result = snackbar.showSnackbar(recordedText!!, actionLabel = undoText, withDismissAction = true)
        if (result == SnackbarResult.ActionPerformed) viewModel.undo(record)
        // Clear only after showing: clearing changes the key, which would cancel the snackbar.
        viewModel.messageShown()
    }

    Scaffold(
        // The app-level Scaffold already applies system bar insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = { settingsOpen = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val list = events
            when {
                list == null -> Unit
                list.isEmpty() -> EmptyState(
                    icon = Icons.Filled.DateRange,
                    title = stringResource(R.string.home_empty_title),
                    text = stringResource(R.string.home_empty_text),
                ) {
                    Button(onClick = onAddEvent) { Text(stringResource(R.string.add_event)) }
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(list, key = { it.event.id }) { item ->
                        EventTile(item, onClick = { viewModel.select(item.event) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EventTile(item: EventWithLast, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                item.event.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                formatLastOccurred(item.lastOccurredAt),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.75f),
            )
        }
    }
}
