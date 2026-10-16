package com.vezhny.eventtracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.eventtracker.data.Event
import com.vezhny.eventtracker.data.EventRepository
import com.vezhny.eventtracker.data.EventWithLast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A record just made, kept so it can be undone from the snackbar. */
data class JustRecorded(val recordId: Long, val eventName: String)

data class HomeUiState(
    /** Event awaiting "Did it happen?" confirmation. */
    val confirming: Event? = null,
    val justRecorded: JustRecorded? = null,
)

class HomeViewModel(private val repository: EventRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    /** null while loading. */
    val events: StateFlow<List<EventWithLast>?> = repository.observeEventsWithLast()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun select(event: Event) = _state.update { it.copy(confirming = event, justRecorded = null) }

    fun cancelConfirm() = _state.update { it.copy(confirming = null) }

    fun confirm() = viewModelScope.launch {
        val event = _state.value.confirming ?: return@launch
        val recordId = repository.record(event.id)
        _state.update { HomeUiState(justRecorded = JustRecorded(recordId, event.name)) }
    }

    fun undo(record: JustRecorded) = viewModelScope.launch {
        repository.deleteRecord(record.recordId)
    }

    fun messageShown() = _state.update { it.copy(justRecorded = null) }
}
