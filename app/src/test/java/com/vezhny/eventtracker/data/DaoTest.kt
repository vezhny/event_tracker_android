package com.vezhny.eventtracker.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vezhny.eventtracker.domain.HistoryEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
// Plain Application: the real one starts Koin, which can't be started twice across tests.
@Config(sdk = [35], application = Application::class)
class DaoTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: EventRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        repository = EventRepository(db.eventDao(), db.eventRecordDao(), RoomTransactor(db), clock = { 500L })
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `insert, update and delete event`() = runTest {
        val id = repository.saveEvent(Event(name = "Пробежка"))
        assertEquals("Пробежка", repository.getEvent(id)?.name)

        repository.saveEvent(Event(id = id, name = "Пробежка 5 км", note = "в парке"))
        assertEquals(Event(id, "Пробежка 5 км", "в парке"), repository.getEvent(id))

        repository.deleteEvent(repository.getEvent(id)!!)
        assertNull(repository.getEvent(id))
    }

    @Test
    fun `events are sorted by name ignoring case`() = runTest {
        repository.saveEvent(Event(name = "b"))
        repository.saveEvent(Event(name = "A"))
        repository.saveEvent(Event(name = "c"))

        assertEquals(listOf("A", "b", "c"), repository.observeEvents().first().map { it.name })
    }

    @Test
    fun `record uses the clock by default and records are newest first`() = runTest {
        val id = repository.saveEvent(Event(name = "Пробежка"))
        repository.record(id, at = 100)
        repository.record(id)
        repository.record(id, at = 300)

        assertEquals(listOf(500L, 300L, 100L), repository.getRecords(id).map { it.occurredAt })
    }

    @Test
    fun `last occurrence is the max record time, null if never`() = runTest {
        val run = repository.saveEvent(Event(name = "Пробежка"))
        val swim = repository.saveEvent(Event(name = "Бассейн"))
        repository.record(run, at = 100)
        repository.record(run, at = 300)
        repository.record(run, at = 200)

        val events = repository.observeEventsWithLast().first()
        assertEquals(listOf("Бассейн", "Пробежка"), events.map { it.event.name })
        assertEquals(listOf(null, 300L), events.map { it.lastOccurredAt })
        assertEquals(listOf(swim, run), events.map { it.event.id })
    }

    @Test
    fun `deleting a record keeps the event and other records`() = runTest {
        val id = repository.saveEvent(Event(name = "Пробежка"))
        val first = repository.record(id, at = 100)
        repository.record(id, at = 200)

        repository.deleteRecord(first)

        assertEquals(listOf(200L), repository.getRecords(id).map { it.occurredAt })
        assertEquals("Пробежка", repository.getEvent(id)?.name)
    }

    @Test
    fun `deleting an event removes its records`() = runTest {
        val run = repository.saveEvent(Event(name = "Пробежка"))
        val swim = repository.saveEvent(Event(name = "Бассейн"))
        repository.record(run, at = 100)
        repository.record(swim, at = 200)

        repository.deleteEvent(repository.getEvent(run)!!)

        assertEquals(emptyList<EventRecord>(), repository.getRecords(run))
        assertEquals(1, repository.getRecords(swim).size)
    }

    @Test
    fun `history is exported oldest first with event names`() = runTest {
        val run = repository.saveEvent(Event(name = "Пробежка"))
        val coffee = repository.saveEvent(Event(name = "Кофе"))
        repository.record(coffee, at = 200)
        repository.record(run, at = 100)

        assertEquals(
            listOf(HistoryEntry("Пробежка", 100), HistoryEntry("Кофе", 200)),
            repository.getHistoryForExport(),
        )
    }

    @Test
    fun `import skips names that already exist or repeat, ignoring case`() = runTest {
        repository.saveEvent(Event(name = "Кофе", note = "старая заметка"))

        val result = repository.importData(
            listOf(Event(id = 42, name = "кофе", note = "новая"), Event(name = "Пробежка"), Event(name = "ПРОБЕЖКА")),
        )

        assertEquals(ImportResult(eventsAdded = 1, eventsSkipped = 2), result)
        val all = repository.getAllEvents()
        assertEquals(listOf("Кофе", "Пробежка"), all.map { it.name })
        assertEquals("старая заметка", all.first().note)
    }

    @Test
    fun `import links history to existing and new events by name`() = runTest {
        val coffee = repository.saveEvent(Event(name = "Кофе"))

        val result = repository.importData(
            events = listOf(Event(name = "Пробежка")),
            history = listOf(HistoryEntry("КОФЕ", 100), HistoryEntry("Пробежка", 200), HistoryEntry("Бассейн", 300)),
        )

        assertEquals(ImportResult(eventsAdded = 1, eventsSkipped = 0, historyAdded = 2, historySkipped = 1), result)
        assertEquals(listOf(100L), repository.getRecords(coffee).map { it.occurredAt })
    }

    @Test
    fun `importing the same backup twice adds nothing the second time`() = runTest {
        val coffee = repository.saveEvent(Event(name = "Кофе"))
        repository.record(coffee, at = 100)

        val result = repository.importData(repository.getAllEvents(), repository.getHistoryForExport())

        assertEquals(ImportResult(eventsAdded = 0, eventsSkipped = 1, historyAdded = 0, historySkipped = 1), result)
        assertEquals(1, repository.getRecords(coffee).size)
    }

    @Test
    fun `failed import leaves the database untouched`() = runTest {
        val failing = object : Transactor {
            override suspend fun <R> inTransaction(block: suspend () -> R): R = db.withTransaction {
                block()
                error("disk full")
            }
        }
        val repo = EventRepository(db.eventDao(), db.eventRecordDao(), failing)

        runCatching { repo.importData(listOf(Event(name = "Кофе")), listOf(HistoryEntry("Кофе", 100))) }

        assertEquals(emptyList<Event>(), repository.getAllEvents())
        assertEquals(emptyList<HistoryEntry>(), repository.getHistoryForExport())
    }
}
