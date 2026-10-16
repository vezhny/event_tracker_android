| Serialization | kotlinx.serialization JSON (import/export) |
# Event Tracker

A small Android app for marking things that happened to you.

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="160" alt="Event Tracker icon">
</p>

## Features

- **Home.** All your events as a grid of tiles with their icons, each showing when the event last happened ("Today, 14:30", "5 days ago", "Never"). Tap a tile, confirm, and the event is recorded with the current time. A mistaken record can be undone from the snackbar.
- **Events.** Add, edit, and delete events with a name, a note, and an optional icon from a built-in set of 28. Deleting asks for confirmation and also removes the event's records.
- **Statistics.** How many times each event happened over the last week, month, or year (each ending today), or over any range of days picked in a calendar. Events are listed most frequent first, with bars relative to the leader.
- **Import / export.** Export either the event list or everything (events plus history) to a JSON file through the system file picker, and import it back on any device. Existing events and records are never duplicated.
- **Theme.** Light, dark, or system default.
- **Language.** English or Russian, or the system default. Only the interface is translated: event names and notes stay as you typed them.

## Tech stack

| Area | Library |
|---|---|
| Language | Kotlin 2.4 |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM: `ViewModel` + `StateFlow` |
| Database | Room (KSP), schema exported to `app/schemas/` |
| DI | Koin |
| Navigation | Navigation Compose with type-safe routes (kotlinx.serialization) |
| Localization | Android resources + AppCompat per-app locales |
| Tests | JUnit 4, Robolectric, kotlinx-coroutines-test |

SDK: `minSdk 26`, `targetSdk 37`. Build: Gradle 9.8 wrapper, AGP 9.4, version catalog in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Project structure

```
app/src/main/java/com/vezhny/eventtracker/
├── data/        Room entities and DAOs, repository, settings, language, file I/O
├── domain/      Pure logic: JSON import/export, statistics periods
├── di/          Koin module
└── ui/          Compose screens and ViewModels
    ├── home/       Tile grid and recording
    ├── events/     Event list and edit form, import/export
    ├── icons/      Event icon set
    ├── stats/      Statistics by period
    ├── settings/   Theme and language dialog
    └── theme/      Color schemes
```

### Data model

Two tables:

- `event`: `id`, `name`, `note`, `icon` (a stable key such as `coffee`, null for no icon; added in database version 2 by a Room auto-migration).
- `event_record`: `id`, `eventId`, `occurredAt` (epoch millis). Rows are deleted together with their event.

## Getting started

Requirements: Android Studio (or JDK 17+ and the Android SDK with platform 37).

```bash
./gradlew installDebug
```

### Common tasks

```bash
./gradlew test             # unit tests (Robolectric for Android APIs)
./gradlew lint             # Android lint
./gradlew assembleDebug    # debug APK in app/build/outputs/apk/debug/
```

## Continuous integration

[GitHub Actions](.github/workflows/android.yml) runs `./gradlew lint test assembleDebug` on every pull request. Lint and test reports are uploaded as build artifacts.

## Versioning

The app follows [Semantic Versioning](https://semver.org/). The version lives in one place, `appVersion` in [`gradle.properties`](gradle.properties):

- `versionName` is `appVersion` itself, e.g. `0.1.0`.
- `versionCode` is derived from it as `MAJOR * 10000 + MINOR * 100 + PATCH` (`0.1.0` → `100`). MINOR and PATCH must stay within 0–99.

To release: bump `appVersion`, move the `[Unreleased]` notes in [`CHANGELOG.md`](CHANGELOG.md) under the new version, merge, and tag the merge commit (`v0.1.0`).

## Import / export format

There are two kinds of export, both in the same format:

- **Events**: only the event list (`event-tracker-events-<date>.json`).
- **Everything**: events plus the whole history (`event-tracker-events-and-history-<date>.json`).

```json
{
  "version": 1,
  "events": [
    { "name": "Running", "note": "5 km in the park", "icon": "run" },
    { "name": "Coffee" }
  ],
  "history": [
    { "event": "Running", "occurredAt": "2026-10-16T06:49:51.684Z" }
  ]
}
```

- `history` is present only in a full export. Entries reference events by name, so the file is portable between devices. `occurredAt` is an ISO-8601 instant in UTC.
- `note` and `icon` are optional, and unknown fields are ignored. `icon` is a stable icon key such as `coffee` or `run`; a key the app doesn't know is kept, and the event shows no icon. Events with a blank name are skipped.
- Files with a newer `version` than the app supports are rejected.

Import loads everything the file contains, in a single transaction:

- An event is skipped if an event with the same name already exists, ignoring case. The existing event is kept as is.
- A history entry is linked to an event by name, either an existing event or one imported from the same file. It is skipped if that event already has a record at the same moment, so importing the same backup twice adds nothing. It is also skipped if no event has that name.

## Localization

| Locale | Resources |
|---|---|
| English (default and fallback) | `app/src/main/res/values/strings.xml` |
| Russian | `app/src/main/res/values-ru/strings.xml` |

To add a language: add `values-<lang>/strings.xml`, add the locale to [`res/xml/locales_config.xml`](app/src/main/res/xml/locales_config.xml) and to `localeFilters` in [`app/build.gradle.kts`](app/build.gradle.kts), then add an entry to `AppLanguage` and a label in `SettingsDialog`.

## Troubleshooting

**`Unable to establish loopback connection` when running Gradle on Windows.** This is a JDK issue when the temp directory path is in short 8.3 form (e.g. `C:\Users\ABCDEF~1.XYZ\...`). Point the JDK at a plain directory before running Gradle:

```bash
export JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=$HOME/.gradle/tmp"
```
