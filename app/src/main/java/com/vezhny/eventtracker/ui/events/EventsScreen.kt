package com.vezhny.eventtracker.ui.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.eventtracker.R
import com.vezhny.eventtracker.ui.EmptyState
import org.koin.androidx.compose.koinViewModel

@Suppress("DEPRECATION") // Icons.Filled.List: AutoMirrored variant lives in material-icons-extended.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onEditEvent: (Long) -> Unit,
    viewModel: EventsViewModel = koinViewModel(),
) {
    val events by viewModel.events.collectAsStateWithLifecycle()

    Scaffold(
        // The app-level Scaffold already applies system bar insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_events)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        },
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
                }
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(list, key = { it.id }) { event ->
                        ListItem(
                            headlineContent = { Text(event.name) },
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
