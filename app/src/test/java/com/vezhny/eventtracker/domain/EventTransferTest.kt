package com.vezhny.eventtracker.domain

import com.vezhny.eventtracker.data.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventTransferTest {
    @Test
    fun `events-only round trip keeps name and note but not ids`() {
        val events = listOf(Event(id = 5, name = "Пробежка", note = "5 км"), Event(id = 7, name = "Кофе"))

        val decoded = EventTransfer.decode(EventTransfer.encode(events))

        assertEquals(listOf(Event(name = "Пробежка", note = "5 км"), Event(name = "Кофе")), decoded.events)
        assertNull(decoded.history)
    }

    @Test
    fun `icon round trips and is optional`() {
        val events = listOf(Event(name = "Кофе", icon = "coffee"), Event(name = "Пробежка"))

        val text = EventTransfer.encode(events)

        assertEquals(events, EventTransfer.decode(text).events)
        assertEquals(1, Regex(""""icon""").findAll(text).count())
    }

    @Test
    fun `unknown and blank icons are read as given or dropped`() {
        val decoded = EventTransfer.decode(
            """{"version": 1, "events": [{"name": "Кофе", "icon": " unicorn "}, {"name": "Чай", "icon": " "}]}""",
        )
        assertEquals(listOf(Event(name = "Кофе", icon = "unicorn"), Event(name = "Чай")), decoded.events)
    }

    @Test
    fun `events-only file has no history key`() {
        assertFalse(EventTransfer.encode(listOf(Event(name = "Кофе"))).contains("history"))
    }

    @Test
    fun `full round trip keeps history with event names and exact time`() {
        val history = listOf(HistoryEntry("Кофе", 1_759_926_600_000), HistoryEntry("Пробежка", 1_760_000_000_123))

        val decoded = EventTransfer.decode(
            EventTransfer.encode(listOf(Event(name = "Кофе"), Event(name = "Пробежка")), history),
        )

        assertEquals(history, decoded.history)
    }

    @Test
    fun `history time is written as ISO-8601 UTC`() {
        val text = EventTransfer.encode(listOf(Event(name = "Кофе")), listOf(HistoryEntry("Кофе", 1_759_926_600_000)))
        assertTrue(text, text.contains(""""occurredAt": "2025-10-08T12:30:00Z""""))
    }

    @Test
    fun `version is always written`() {
        val text = EventTransfer.encode(listOf(Event(name = "Кофе")))
        assertTrue(text, text.contains(""""version": ${EventTransfer.VERSION}"""))
    }

    @Test
    fun `imported values are cleaned up`() {
        val text = """
            {"version": 1, "extra": true, "events": [
              {"name": "  Кофе ", "note": "  "},
              {"name": "   "},
              {"name": "Пробежка", "note": " утром "}
            ], "history": [
              {"event": " Кофе ", "occurredAt": "2025-10-08T12:30:00Z"},
              {"event": "  ", "occurredAt": "2026-10-08T12:30:00Z"}
            ]}
        """
        val decoded = EventTransfer.decode(text)
        assertEquals(listOf(Event(name = "Кофе"), Event(name = "Пробежка", note = "утром")), decoded.events)
        assertEquals(listOf(HistoryEntry("Кофе", 1_759_926_600_000)), decoded.history)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `bad history date is rejected`() {
        EventTransfer.decode("""{"version": 1, "events": [], "history": [{"event": "Кофе", "occurredAt": "yesterday"}]}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `garbage is rejected`() {
        EventTransfer.decode("not json at all")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `json of another shape is rejected`() {
        EventTransfer.decode("""{"items": []}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `newer version is rejected`() {
        EventTransfer.decode("""{"version": 99, "events": []}""")
    }
}
