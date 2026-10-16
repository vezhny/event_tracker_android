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

    @Query(
        """
        SELECT event.*, MAX(event_record.occurredAt) AS lastOccurredAt
        FROM event LEFT JOIN event_record ON event_record.eventId = event.id
        GROUP BY event.id
        ORDER BY event.name COLLATE NOCASE
        """,
    )
    fun observeAllWithLast(): Flow<List<EventWithLast>>
}

@Dao
interface EventRecordDao {
    @Insert
    suspend fun insert(record: EventRecord): Long

    @Query("DELETE FROM event_record WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM event_record WHERE eventId = :eventId ORDER BY occurredAt DESC")
    suspend fun getForEvent(eventId: Long): List<EventRecord>
}
