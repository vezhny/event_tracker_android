package com.vezhny.eventtracker.domain

import com.vezhny.eventtracker.data.Event
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.format.DateTimeParseException

/** One history entry in a file: the event is referenced by name, so files are portable. */
data class HistoryEntry(val eventName: String, val occurredAt: Long)

/** Contents of an export file. [history] is null when only events were exported. */
data class TransferData(val events: List<Event>, val history: List<HistoryEntry>?)

/**
 * JSON format for exporting and importing data:
 * ```
 * {"version": 1,
 *  "events": [{"name": "Пробежка", "note": "..."}],
 *  "history": [{"event": "Пробежка", "occurredAt": "2026-10-16T07:30:00Z"}]}
 * ```
 * `history` is present only in full exports.
 */
object EventTransfer {
    const val VERSION = 1

    @Serializable
    private data class FileDto(
        val version: Int,
        val events: List<EventDto>,
        val history: List<HistoryDto>? = null,
    )

    @Serializable
    private data class EventDto(val name: String, val note: String? = null)

    @Serializable
    private data class HistoryDto(val event: String, val occurredAt: String)

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        explicitNulls = false // events-only files have no "history" key at all
    }

    /** Encodes [events] and, for a full export, [history]. */
    fun encode(events: List<Event>, history: List<HistoryEntry>? = null): String = json.encodeToString(
        FileDto.serializer(),
        FileDto(
            version = VERSION,
            events = events.map { EventDto(it.name, it.note) },
            history = history?.map { HistoryDto(it.eventName, Instant.ofEpochMilli(it.occurredAt).toString()) },
        ),
    )

    /**
     * Parses an export file into new (unsaved) events and history entries.
     * Events and history entries with a blank event name are skipped.
     * @throws IllegalArgumentException if the text is not a valid file.
     */
    fun decode(text: String): TransferData {
        val file = try {
            json.decodeFromString(FileDto.serializer(), text)
        } catch (e: SerializationException) {
            throw IllegalArgumentException("Not an Event Tracker file", e)
        }
        require(file.version <= VERSION) { "Unsupported version ${file.version}" }
        val events = file.events
            .filter { it.name.isNotBlank() }
            .map { Event(name = it.name.trim(), note = it.note?.trim()?.ifEmpty { null }) }
        val history = file.history
            ?.filter { it.event.isNotBlank() }
            ?.map { HistoryEntry(it.event.trim(), parseInstant(it.occurredAt)) }
        return TransferData(events, history)
    }

    private fun parseInstant(text: String): Long = try {
        Instant.parse(text).toEpochMilli()
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Bad occurredAt '$text'", e)
    }
}
