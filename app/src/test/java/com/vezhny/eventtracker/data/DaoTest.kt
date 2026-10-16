package com.vezhny.eventtracker.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        repository = EventRepository(db.eventDao(), db.eventRecordDao(), clock = { 500L })
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
}
