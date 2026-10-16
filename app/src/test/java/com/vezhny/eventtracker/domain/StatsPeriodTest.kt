package com.vezhny.eventtracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StatsPeriodTest {
    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun `fixed periods end today and include it`() {
        assertEquals(DateRange(LocalDate.of(2026, 10, 10), today), StatsPeriod.WEEK.range(today))
        assertEquals(DateRange(LocalDate.of(2026, 9, 17), today), StatsPeriod.MONTH.range(today))
        assertEquals(DateRange(LocalDate.of(2025, 10, 17), today), StatsPeriod.YEAR.range(today))
    }

    @Test
    fun `custom period uses the chosen range, or the last week without one`() {
        val custom = DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))
        assertEquals(custom, StatsPeriod.CUSTOM.range(today, custom))
        assertEquals(StatsPeriod.WEEK.range(today), StatsPeriod.CUSTOM.range(today))
    }

    @Test
    fun `millis cover whole days, end exclusive`() {
        val zone = ZoneId.of("Europe/Minsk") // UTC+3
        val (from, to) = DateRange(LocalDate.of(2026, 10, 16), LocalDate.of(2026, 10, 16)).toMillis(zone)
        assertEquals(java.time.Instant.parse("2026-10-15T21:00:00Z").toEpochMilli(), from)
        assertEquals(java.time.Instant.parse("2026-10-16T21:00:00Z").toEpochMilli(), to)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reversed range is rejected`() {
        DateRange(today, today.minusDays(1))
    }
}
