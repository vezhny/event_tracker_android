package com.vezhny.eventtracker.ui.stats

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.eventtracker.R
import com.vezhny.eventtracker.data.EventCount
import com.vezhny.eventtracker.domain.DateRange
import com.vezhny.eventtracker.domain.StatsPeriod
import com.vezhny.eventtracker.ui.EmptyState
import com.vezhny.eventtracker.ui.formatDate
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

private val StatsPeriod.labelRes: Int
    get() = when (this) {
        StatsPeriod.WEEK -> R.string.period_week
        StatsPeriod.MONTH -> R.string.period_month
        StatsPeriod.YEAR -> R.string.period_year
        StatsPeriod.CUSTOM -> R.string.period_custom
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pickerOpen by rememberSaveable { mutableStateOf(false) }

    if (pickerOpen) {
        RangePickerDialog(
            initial = state?.custom,
            onConfirm = {
                pickerOpen = false
                viewModel.selectCustom(it)
            },
            onDismiss = { pickerOpen = false },
        )
    }

    Scaffold(
        // The app-level Scaffold already applies system bar insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_stats)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        },
    ) { padding ->
        val s = state ?: return@Scaffold
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatsPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = s.period == period,
                        // The custom chip always opens the picker, so a range can be changed in one tap.
                        onClick = { if (period == StatsPeriod.CUSTOM) pickerOpen = true else viewModel.selectPeriod(period) },
                        label = { Text(stringResource(period.labelRes)) },
                    )
                }
            }
            Text(
                stringResource(R.string.stats_range, formatDate(s.range.start), formatDate(s.range.end)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            if (s.counts.isEmpty()) {
                EmptyState(
                    icon = StatsIcon,
                    title = stringResource(R.string.stats_empty_title),
                    text = stringResource(R.string.stats_empty_text),
                )
            } else {
                Text(
                    stringResource(R.string.stats_total, s.total),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(s.counts, key = { it.event.id }) { item -> CountRow(item, s.maxCount) }
                }
            }
        }
    }
}

@Composable
private fun CountRow(item: EventCount, maxCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.event.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(item.count.toString(), style = MaterialTheme.typography.titleMedium)
        }
        LinearProgressIndicator(
            progress = { if (maxCount == 0) 0f else item.count.toFloat() / maxCount },
            drawStopIndicator = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangePickerDialog(initial: DateRange?, onConfirm: (DateRange) -> Unit, onDismiss: () -> Unit) {
    val today = LocalDate.now()
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initial?.start?.toPickerMillis(),
        initialSelectedEndDateMillis = initial?.end?.toPickerMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = !utcTimeMillis.fromPickerMillis().isAfter(today)
            override fun isSelectableYear(year: Int) = year <= today.year
        },
    )
    val start = pickerState.selectedStartDateMillis
    val end = pickerState.selectedEndDateMillis

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                // A single tapped day is a one-day range.
                enabled = start != null,
                onClick = { start?.let { onConfirm(DateRange(it.fromPickerMillis(), (end ?: it).fromPickerMillis())) } },
            ) { Text(stringResource(R.string.done)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    ) {
        Box(Modifier.weight(1f)) {
            DateRangePicker(state = pickerState, showModeToggle = false)
        }
    }
}

// The Material date picker works with UTC midnights.
private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
private fun LocalDate.toPickerMillis(): Long = toEpochDay() * DAY_MILLIS
private fun Long.fromPickerMillis(): LocalDate = LocalDate.ofEpochDay(Math.floorDiv(this, DAY_MILLIS))
