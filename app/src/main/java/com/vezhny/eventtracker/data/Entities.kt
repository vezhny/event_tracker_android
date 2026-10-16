package com.vezhny.eventtracker.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A kind of event the user tracks, e.g. "Went running". */
@Entity(tableName = "event")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val note: String? = null,
)

/** An event together with the moment it last happened (null if never). */
data class EventWithLast(
    @Embedded val event: Event,
    @ColumnInfo(name = "lastOccurredAt") val lastOccurredAt: Long?,
)

/** An event with the number of times it happened in some period. */
data class EventCount(
    @Embedded val event: Event,
    @ColumnInfo(name = "count") val count: Int,
)

/** One occurrence of an [Event]. */
@Entity(
    tableName = "event_record",
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("eventId"), Index("occurredAt")],
)
data class EventRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    /** Epoch millis. */
    val occurredAt: Long,
)

/** A record joined with its event name. */
data class NamedRecord(val eventName: String, val occurredAt: Long)
