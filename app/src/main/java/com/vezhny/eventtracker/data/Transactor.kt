package com.vezhny.eventtracker.data

import androidx.room.withTransaction

/** Runs a block of database work atomically. */
interface Transactor {
    suspend fun <R> inTransaction(block: suspend () -> R): R
}

class RoomTransactor(private val db: AppDatabase) : Transactor {
    override suspend fun <R> inTransaction(block: suspend () -> R): R = db.withTransaction(block)
}
