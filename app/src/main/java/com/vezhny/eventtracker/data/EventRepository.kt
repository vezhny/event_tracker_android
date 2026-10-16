package com.vezhny.eventtracker.data

import com.vezhny.eventtracker.domain.HistoryEntry
import kotlinx.coroutines.flow.Flow

data class ImportResult(
    val eventsAdded: Int,
    val eventsSkipped: Int,
    val historyAdded: Int = 0,
    val historySkipped: Int = 0,
)

class EventRepository(
    private val eventDao: EventDao,
    private val recordDao: EventRecordDao,
    private val transactor: Transactor,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeEvents(): Flow<List<Event>> = eventDao.observeAll()

    fun observeEventsWithLast(): Flow<List<EventWithLast>> = eventDao.observeAllWithLast()

    /** Counts of records per event in [from, to) epoch millis. */
    fun observeCounts(from: Long, to: Long): Flow<List<EventCount>> = eventDao.observeCounts(from, to)

    suspend fun getEvent(id: Long): Event? = eventDao.getById(id)

    /** Inserts a new event when [Event.id] is 0, otherwise updates it. */
    suspend fun saveEvent(event: Event): Long =
        if (event.id == 0L) eventDao.insert(event) else event.id.also { eventDao.update(event) }

    /** Deletes the event together with all its records. */
    suspend fun deleteEvent(event: Event) = eventDao.delete(event)

    /** Records that the event happened [at] the given moment (now by default). Returns the record id. */
    suspend fun record(eventId: Long, at: Long = clock()): Long =
        recordDao.insert(EventRecord(eventId = eventId, occurredAt = at))

    suspend fun deleteRecord(recordId: Long) = recordDao.deleteById(recordId)

    /** Records of one event, newest first. */
    suspend fun getRecords(eventId: Long): List<EventRecord> = recordDao.getForEvent(eventId)

    suspend fun getAllEvents(): List<Event> = eventDao.getAll()

    /** The whole history, oldest first, with events referenced by name. */
    suspend fun getHistoryForExport(): List<HistoryEntry> =
        recordDao.getHistory().map { HistoryEntry(it.eventName, it.occurredAt) }

    /**
     * Imports events and, if given, history, all in one transaction:
     * - an event is skipped if its name (ignoring case) already exists or repeats within [events];
     * - a history entry is matched to an event by name (existing or just imported) and skipped
     *   if that event already has a record at the same moment, or if no event has that name.
     */
    suspend fun importData(events: List<Event>, history: List<HistoryEntry>? = null): ImportResult =
        transactor.inTransaction {
            val seen = eventDao.getAll().mapTo(HashSet()) { it.key }
            val freshEvents = events.filter { seen.add(it.key) }.map { it.copy(id = 0) }
            eventDao.insertAll(freshEvents)

            var historyAdded = 0
            if (history != null) {
                val idByName = eventDao.getAll().associate { it.key to it.id }
                val existing = recordDao.getAll().mapTo(HashSet()) { it.eventId to it.occurredAt }
                val freshRecords = history.mapNotNull { entry ->
                    val eventId = idByName[entry.eventName.trim().lowercase()] ?: return@mapNotNull null
                    EventRecord(eventId = eventId, occurredAt = entry.occurredAt)
                        .takeIf { existing.add(eventId to entry.occurredAt) }
                }
                recordDao.insertAll(freshRecords)
                historyAdded = freshRecords.size
            }

            ImportResult(
                eventsAdded = freshEvents.size,
                eventsSkipped = events.size - freshEvents.size,
                historyAdded = historyAdded,
                historySkipped = (history?.size ?: 0) - historyAdded,
            )
        }

    /** Name key for matching events: trimmed, case-insensitive. */
    private val Event.key: String get() = name.trim().lowercase()
}
