package com.vezhny.eventtracker.di

import android.content.Context
import com.vezhny.eventtracker.data.SettingsRepository
import com.vezhny.eventtracker.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        SettingsRepository(androidContext().getSharedPreferences(SettingsRepository.PREFS_NAME, Context.MODE_PRIVATE))
    }

    viewModel { SettingsViewModel(get()) }
}
