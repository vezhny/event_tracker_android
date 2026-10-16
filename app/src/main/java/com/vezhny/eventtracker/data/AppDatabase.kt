package com.vezhny.eventtracker.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Event::class, EventRecord::class], version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)], // 2: event.icon
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun eventRecordDao(): EventRecordDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "event_tracker.db").build()
    }
}
