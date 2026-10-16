package com.vezhny.eventtracker.di

import android.content.Context
import com.vezhny.eventtracker.data.AppDatabase
import com.vezhny.eventtracker.data.EventRepository
import com.vezhny.eventtracker.data.SettingsRepository
import com.vezhny.eventtracker.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.create(androidContext()) }
    single { get<AppDatabase>().eventDao() }
    single { get<AppDatabase>().eventRecordDao() }
    single { EventRepository(get(), get()) }
    single {
        SettingsRepository(androidContext().getSharedPreferences(SettingsRepository.PREFS_NAME, Context.MODE_PRIVATE))
    }

    viewModel { SettingsViewModel(get()) }
}
