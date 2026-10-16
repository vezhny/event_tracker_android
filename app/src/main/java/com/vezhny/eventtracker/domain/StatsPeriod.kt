package com.vezhny.eventtracker.domain

import java.time.LocalDate
import java.time.ZoneId

/** Statistics period. Fixed periods end today and include it: the last 7 days, month or year. */
enum class StatsPeriod { WEEK, MONTH, YEAR, CUSTOM }

/** Inclusive range of days. */
data class DateRange(val start: LocalDate, val end: LocalDate) {
    init {
        require(!end.isBefore(start)) { "Range end $end is before start $start" }
    }

    /** Half-open epoch millis range [from, to) covering whole days in [zone]. */
    fun toMillis(zone: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> =
        start.atStartOfDay(zone).toInstant().toEpochMilli() to end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
}

/** Days covered by this period. [custom] is used for [StatsPeriod.CUSTOM]; without it the last week is used. */
fun StatsPeriod.range(today: LocalDate, custom: DateRange? = null): DateRange = when (this) {
    StatsPeriod.WEEK -> DateRange(today.minusDays(6), today)
    StatsPeriod.MONTH -> DateRange(today.minusMonths(1).plusDays(1), today)
    StatsPeriod.YEAR -> DateRange(today.minusYears(1).plusDays(1), today)
    StatsPeriod.CUSTOM -> custom ?: StatsPeriod.WEEK.range(today)
}
