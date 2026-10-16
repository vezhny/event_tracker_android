package com.vezhny.eventtracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.eventtracker.data.EventCount
import com.vezhny.eventtracker.data.EventRepository
import com.vezhny.eventtracker.domain.DateRange
import com.vezhny.eventtracker.domain.StatsPeriod
import com.vezhny.eventtracker.domain.range
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId

data class StatsSelection(
    val period: StatsPeriod = StatsPeriod.WEEK,
    /** Last range chosen in the picker, kept while switching to fixed periods and back. */
    val custom: DateRange? = null,
)

data class StatsUiState(
    val period: StatsPeriod,
    val range: DateRange,
    val custom: DateRange?,
    val counts: List<EventCount>,
) {
    val total: Int get() = counts.sumOf { it.count }
    val maxCount: Int get() = counts.maxOfOrNull { it.count } ?: 0
}

class StatsViewModel(
    private val repository: EventRepository,
    private val today: () -> LocalDate = LocalDate::now,
    private val zone: () -> ZoneId = ZoneId::systemDefault,
) : ViewModel() {
    private val selection = MutableStateFlow(StatsSelection())

    /** null while loading. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<StatsUiState?> = selection
        .flatMapLatest { sel ->
            val range = sel.period.range(today(), sel.custom)
            val (from, to) = range.toMillis(zone())
            repository.observeCounts(from, to).map { StatsUiState(sel.period, range, sel.custom, it) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectPeriod(period: StatsPeriod) = selection.update { it.copy(period = period) }

    fun selectCustom(range: DateRange) = selection.update { StatsSelection(StatsPeriod.CUSTOM, range) }
}
