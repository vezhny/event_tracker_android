package com.vezhny.eventtracker.di

import android.content.Context
import com.vezhny.eventtracker.data.AppDatabase
import com.vezhny.eventtracker.data.EventRepository
import com.vezhny.eventtracker.data.RoomTransactor
import com.vezhny.eventtracker.data.SettingsRepository
import com.vezhny.eventtracker.data.TextFiles
import com.vezhny.eventtracker.data.Transactor
import com.vezhny.eventtracker.ui.events.EventEditViewModel
import com.vezhny.eventtracker.ui.events.EventsViewModel
import com.vezhny.eventtracker.ui.home.HomeViewModel
import com.vezhny.eventtracker.ui.settings.SettingsViewModel
import com.vezhny.eventtracker.ui.stats.StatsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.create(androidContext()) }
    single { get<AppDatabase>().eventDao() }
    single { get<AppDatabase>().eventRecordDao() }
    single<Transactor> { RoomTransactor(get()) }
    single { EventRepository(get(), get(), get()) }
    single { TextFiles(androidContext().contentResolver) }
    single {
        SettingsRepository(androidContext().getSharedPreferences(SettingsRepository.PREFS_NAME, Context.MODE_PRIVATE))
    }

    viewModel { SettingsViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { EventsViewModel(get(), get()) }
    viewModel { params -> EventEditViewModel(params.get(), get()) }
    viewModel { StatsViewModel(get()) }
}
