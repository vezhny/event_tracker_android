package com.vezhny.eventtracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.vezhny.eventtracker.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

private fun Long.atLocalZone(zone: ZoneId = ZoneId.systemDefault()): ZonedDateTime =
    Instant.ofEpochMilli(this).atZone(zone)

/** A date in the UI language, e.g. "16 Oct 2026". */
@Composable
@ReadOnlyComposable
fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(LocalLocale.current.platformLocale))

/** "Today, 14:30", "Yesterday, 09:10", "5 days ago" or "Never". */
@Composable
@ReadOnlyComposable
fun formatLastOccurred(lastOccurredAt: Long?, today: LocalDate = LocalDate.now()): String {
    if (lastOccurredAt == null) return stringResource(R.string.last_never)
    val moment = lastOccurredAt.atLocalZone()
    val time = moment.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(LocalLocale.current.platformLocale))
    return when (val days = ChronoUnit.DAYS.between(moment.toLocalDate(), today).toInt()) {
        0 -> stringResource(R.string.last_today, time)
        1 -> stringResource(R.string.last_yesterday, time)
        else -> pluralStringResource(R.plurals.last_days_ago, days, days)
    }
}
