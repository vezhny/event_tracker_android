package com.vezhny.eventtracker.data

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * App settings in SharedPreferences: read synchronously at startup, so the first frame
 * already uses the chosen theme.
 */
class SettingsRepository(private val prefs: SharedPreferences) {
    private val _themeMode = MutableStateFlow(readThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        _themeMode.value = mode
    }

    private fun readThemeMode(): ThemeMode =
        prefs.getString(KEY_THEME_MODE, null)
            ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.SYSTEM

    companion object {
        const val PREFS_NAME = "settings"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
