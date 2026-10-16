package com.vezhny.eventtracker.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** UI language. User data is never translated. */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    ENGLISH("en"),
    RUSSIAN("ru"),
    ;

    companion object {
        /** Maps AppCompat's stored language tags ("" = follow the system) to a choice. */
        fun fromTags(tags: String): AppLanguage {
            val language = tags.substringBefore(',').substringBefore('-').lowercase()
            return entries.firstOrNull { it.tag == language } ?: SYSTEM
        }

        /** AppCompat persists the choice and applies it on every start (via LocaleManager on Android 13+). */
        fun current(): AppLanguage = fromTags(AppCompatDelegate.getApplicationLocales().toLanguageTags())

        /** Recreates the activity in the new language. */
        fun apply(language: AppLanguage) = AppCompatDelegate.setApplicationLocales(
            language.tag?.let(LocaleListCompat::forLanguageTags) ?: LocaleListCompat.getEmptyLocaleList(),
        )
    }
}
