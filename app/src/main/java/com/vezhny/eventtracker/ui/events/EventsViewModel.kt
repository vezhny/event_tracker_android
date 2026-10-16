package com.vezhny.eventtracker.ui.events

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.eventtracker.data.Event
import com.vezhny.eventtracker.data.EventRepository
import com.vezhny.eventtracker.data.ImportResult
import com.vezhny.eventtracker.data.TextFiles
import com.vezhny.eventtracker.domain.EventTransfer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate

/** What goes into an export file. */
enum class ExportScope { EVENTS, EVERYTHING }

/** Result of import/export, turned into localized text by the screen. */
sealed interface TransferMessage {
    /** [historyCount] is null for an events-only export. */
    data class Exported(val eventCount: Int, val historyCount: Int?) : TransferMessage
    data class Imported(val result: ImportResult) : TransferMessage
    data object ExportFailed : TransferMessage
    data object ReadFailed : TransferMessage
    data object InvalidFile : TransferMessage
}

class EventsViewModel(
    private val repository: EventRepository,
    private val files: TextFiles,
) : ViewModel() {
    /** null while loading. */
    val events: StateFlow<List<Event>?> = repository.observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _message = MutableStateFlow<TransferMessage?>(null)
    /** One-shot result of import/export to show in a snackbar. */
    val message: StateFlow<TransferMessage?> = _message.asStateFlow()

    fun exportFileName(scope: ExportScope, today: LocalDate = LocalDate.now()) = when (scope) {
        ExportScope.EVENTS -> "event-tracker-events-$today.json"
        ExportScope.EVERYTHING -> "event-tracker-events-and-history-$today.json"
    }

    fun export(uri: Uri, scope: ExportScope) = viewModelScope.launch {
        _message.value = try {
            val events = repository.getAllEvents()
            val history = if (scope == ExportScope.EVERYTHING) repository.getHistoryForExport() else null
            files.write(uri, EventTransfer.encode(events, history))
            TransferMessage.Exported(events.size, history?.size)
        } catch (e: IOException) {
            TransferMessage.ExportFailed
        }
    }

    /** Imports everything the file contains: events, and history if it is a full export. */
    fun import(uri: Uri) = viewModelScope.launch {
        _message.value = try {
            val data = EventTransfer.decode(files.read(uri))
            TransferMessage.Imported(repository.importData(data.events, data.history))
        } catch (e: IOException) {
            TransferMessage.ReadFailed
        } catch (e: IllegalArgumentException) {
            TransferMessage.InvalidFile
        }
    }

    fun messageShown() {
        _message.value = null
    }
}
