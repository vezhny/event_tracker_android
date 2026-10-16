package com.vezhny.eventtracker.ui.events

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.eventtracker.R
import com.vezhny.eventtracker.ui.EmptyState
import com.vezhny.eventtracker.ui.icons.EventIcon
import org.koin.androidx.compose.koinViewModel

@Suppress("DEPRECATION") // Icons.Filled.List: AutoMirrored variant lives in material-icons-extended.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onEditEvent: (Long) -> Unit,
    viewModel: EventsViewModel = koinViewModel(),
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val messageText = message?.let { transferMessageText(it) }
    val snackbar = remember { SnackbarHostState() }
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    // Saveable: the file picker is another activity, our process may be killed meanwhile.
    var exportScope by rememberSaveable { mutableStateOf(ExportScope.EVENTS) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        uri -> uri?.let { viewModel.export(it, exportScope) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri -> uri?.let(viewModel::import)
    }

    fun startExport(scope: ExportScope) {
        menuOpen = false
        exportScope = scope
        exportLauncher.launch(viewModel.exportFileName(scope))
    }

    LaunchedEffect(message) {
        // Clear only after showing: clearing changes the key, which would cancel the snackbar.
        val text = messageText ?: return@LaunchedEffect
        snackbar.showSnackbar(text)
        viewModel.messageShown()
    }

    Scaffold(
        // The app-level Scaffold already applies system bar insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_events)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_events)) },
                                enabled = !events.isNullOrEmpty(),
                                onClick = { startExport(ExportScope.EVENTS) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_everything)) },
                                enabled = !events.isNullOrEmpty(),
                                onClick = { startExport(ExportScope.EVERYTHING) },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_from_file)) },
                                onClick = {
                                    menuOpen = false
                                    importLauncher.launch(importMimeTypes)
                                },
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            // The empty state has its own "add" button.
            if (!events.isNullOrEmpty()) {
                FloatingActionButton(onClick = { onEditEvent(0) }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_event))
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val list = events
            when {
                list == null -> Unit
                list.isEmpty() -> EmptyState(
                    icon = Icons.Filled.List,
                    title = stringResource(R.string.events_list_empty_title),
                    text = stringResource(R.string.events_list_empty_text),
                ) {
                    Button(onClick = { onEditEvent(0) }) { Text(stringResource(R.string.add_event)) }
                    TextButton(onClick = { importLauncher.launch(importMimeTypes) }) { Text(stringResource(R.string.import_from_file)) }
                }
                else -> {
                    // Keeps names aligned when only some events have an icon.
                    val anyIcon = list.any { EventIcon.fromKey(it.icon) != null }
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(list, key = { it.id }) { event ->
                            ListItem(
                                headlineContent = { Text(event.name) },
                                leadingContent = if (!anyIcon) null else {
                                    { EventIcon.fromKey(event.icon)?.let { Icon(it.vector, contentDescription = null) } ?: Spacer(Modifier.size(24.dp)) }
                                },
                                supportingContent = event.note?.takeIf { it.isNotBlank() }?.let { note ->
                                    { Text(note, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                                },
                                trailingContent = {
                                    IconButton(onClick = { onEditEvent(event.id) }) {
                                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
                                    }
                                },
                                modifier = Modifier.clickable { onEditEvent(event.id) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

// Some file managers report JSON as plain text or as a generic binary.
private val importMimeTypes = arrayOf("application/json", "text/plain", "application/octet-stream")

@Composable
private fun transferMessageText(message: TransferMessage): String = when (message) {
    is TransferMessage.Exported -> {
        val events = pluralStringResource(R.plurals.export_done, message.eventCount, message.eventCount)
        message.historyCount?.let { stringResource(R.string.export_done_with_history, events, it) } ?: events
    }
    is TransferMessage.Imported -> {
        val r = message.result
        var text = pluralStringResource(R.plurals.import_added, r.eventsAdded, r.eventsAdded)
        if (r.eventsSkipped > 0) text = stringResource(R.string.import_added_with_skipped, text, r.eventsSkipped)
        if (r.historyAdded + r.historySkipped > 0) text = stringResource(R.string.import_history_added, text, r.historyAdded)
        text
    }
    TransferMessage.ExportFailed -> stringResource(R.string.export_failed)
    TransferMessage.ReadFailed -> stringResource(R.string.import_read_failed)
    TransferMessage.InvalidFile -> stringResource(R.string.import_invalid)
}
