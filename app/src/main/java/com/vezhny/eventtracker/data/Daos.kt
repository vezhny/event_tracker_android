package com.vezhny.eventtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: Event): Long

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun getById(id: Long): Event?

    @Query("SELECT * FROM event ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Event>>

    @Query("SELECT * FROM event ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<Event>

    @Insert
    suspend fun insertAll(events: List<Event>)

    @Query(
        """
        SELECT event.*, MAX(event_record.occurredAt) AS lastOccurredAt
        FROM event LEFT JOIN event_record ON event_record.eventId = event.id
        GROUP BY event.id
        ORDER BY event.name COLLATE NOCASE
        """,
    )
    fun observeAllWithLast(): Flow<List<EventWithLast>>

    /** Every event with the number of its records in [from, to), most frequent first. */
    @Query(
        """
        SELECT event.*, COUNT(event_record.id) AS count
        FROM event LEFT JOIN event_record
            ON event_record.eventId = event.id AND event_record.occurredAt >= :from AND event_record.occurredAt < :to
        GROUP BY event.id
        ORDER BY count DESC, event.name COLLATE NOCASE
        """,
    )
    fun observeCounts(from: Long, to: Long): Flow<List<EventCount>>
}

@Dao
interface EventRecordDao {
    @Insert
    suspend fun insert(record: EventRecord): Long

    @Insert
    suspend fun insertAll(records: List<EventRecord>)

    @Query("SELECT * FROM event_record")
    suspend fun getAll(): List<EventRecord>

    /** All records with event names, oldest first. */
    @Query(
        """
        SELECT event.name AS eventName, event_record.occurredAt
        FROM event_record JOIN event ON event.id = event_record.eventId
        ORDER BY event_record.occurredAt
        """,
    )
    suspend fun getHistory(): List<NamedRecord>

    @Query("DELETE FROM event_record WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM event_record WHERE eventId = :eventId ORDER BY occurredAt DESC")
    suspend fun getForEvent(eventId: Long): List<EventRecord>
}
