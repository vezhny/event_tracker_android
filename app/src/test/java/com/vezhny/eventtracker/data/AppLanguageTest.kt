package com.vezhny.eventtracker.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `empty tags follow the system`() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTags(""))
    }

    @Test
    fun `language is matched ignoring region and extra locales`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTags("en"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTags("en-GB"))
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.fromTags("ru-RU,en-US"))
    }

    @Test
    fun `unsupported language falls back to system`() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTags("de-DE"))
    }
}
