package com.vezhny.eventtracker.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.eventtracker.data.Event
import com.vezhny.eventtracker.data.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class EventsViewModel(repository: EventRepository) : ViewModel() {
    /** null while loading. */
    val events: StateFlow<List<Event>?> = repository.observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
