package com.vezhny.eventtracker.ui.settings

import androidx.lifecycle.ViewModel
import com.vezhny.eventtracker.data.AppLanguage
import com.vezhny.eventtracker.data.SettingsRepository
import com.vezhny.eventtracker.data.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val settings: SettingsRepository) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = settings.themeMode

    fun setThemeMode(mode: ThemeMode) = settings.setThemeMode(mode)

    // Read on demand: changing the language recreates the activity, while this ViewModel survives.
    fun language(): AppLanguage = AppLanguage.current()

    fun setLanguage(language: AppLanguage) {
        if (language != AppLanguage.current()) AppLanguage.apply(language)
    }
}
