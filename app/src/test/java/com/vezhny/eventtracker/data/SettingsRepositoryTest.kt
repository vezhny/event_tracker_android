package com.vezhny.eventtracker.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], application = Application::class)
class SettingsRepositoryTest {
    private val prefs = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("test_settings", Context.MODE_PRIVATE)

    @Test
    fun `defaults to following the system`() {
        assertEquals(ThemeMode.SYSTEM, SettingsRepository(prefs).themeMode.value)
    }

    @Test
    fun `chosen mode is emitted and survives restart`() {
        val settings = SettingsRepository(prefs)
        settings.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, settings.themeMode.value)
        assertEquals(ThemeMode.DARK, SettingsRepository(prefs).themeMode.value)
    }

    @Test
    fun `unknown stored value falls back to system`() {
        prefs.edit().putString("theme_mode", "SEPIA").commit()
        assertEquals(ThemeMode.SYSTEM, SettingsRepository(prefs).themeMode.value)
    }
}
