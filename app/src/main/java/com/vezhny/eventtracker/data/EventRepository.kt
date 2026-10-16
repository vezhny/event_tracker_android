package com.vezhny.eventtracker.data

import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao,
    private val recordDao: EventRecordDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeEvents(): Flow<List<Event>> = eventDao.observeAll()

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
}
