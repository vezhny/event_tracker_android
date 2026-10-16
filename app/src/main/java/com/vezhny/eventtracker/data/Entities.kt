package com.vezhny.eventtracker.data

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
