package com.vezhny.eventtracker.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.eventtracker.data.Event
import com.vezhny.eventtracker.data.EventRepository
import com.vezhny.eventtracker.ui.icons.EventIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventEditUiState(
    val name: String = "",
    val note: String = "",
    /** Stored as the key, so a key this version doesn't know survives editing the event. */
    val iconKey: String? = null,
    val isNew: Boolean = true,
    val done: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank()
    val icon: EventIcon? get() = EventIcon.fromKey(iconKey)
}

class EventEditViewModel(
    private val eventId: Long,
    private val repository: EventRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EventEditUiState(isNew = eventId == 0L))
    val state: StateFlow<EventEditUiState> = _state.asStateFlow()

    init {
        if (eventId != 0L) viewModelScope.launch {
            repository.getEvent(eventId)?.let { event ->
                _state.update { it.copy(name = event.name, note = event.note.orEmpty(), iconKey = event.icon) }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onNoteChange(value: String) = _state.update { it.copy(note = value) }
    fun onIconChange(icon: EventIcon?) = _state.update { it.copy(iconKey = icon?.key) }

    fun save() = viewModelScope.launch {
        val s = _state.value
        if (!s.canSave) return@launch
        repository.saveEvent(
            Event(id = eventId, name = s.name.trim(), note = s.note.trim().ifEmpty { null }, icon = s.iconKey),
        )
        _state.update { it.copy(done = true) }
    }

    fun delete() = viewModelScope.launch {
        repository.getEvent(eventId)?.let { repository.deleteEvent(it) }
        _state.update { it.copy(done = true) }
    }
}
