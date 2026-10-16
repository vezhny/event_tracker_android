# Event Tracker

An Android app for tracking events.

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
├── data/        Repositories, settings, language
├── di/          Koin module
└── ui/          Compose screens and ViewModels
    ├── home/       Home screen
    ├── settings/   Theme and language dialog
    └── theme/      Color schemes
```

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

[GitHub Actions](.github/workflows/android.yml) runs `./gradlew lint test assembleDebug` on every push and pull request. Lint and test reports are uploaded as build artifacts.

## Versioning

The app follows [Semantic Versioning](https://semver.org/). The version lives in one place, `appVersion` in [`gradle.properties`](gradle.properties):

- `versionName` is `appVersion` itself, e.g. `0.1.0`.
- `versionCode` is derived from it as `MAJOR * 10000 + MINOR * 100 + PATCH` (`0.1.0` → `100`). MINOR and PATCH must stay within 0–99.

To release: bump `appVersion`, move the `[Unreleased]` notes in [`CHANGELOG.md`](CHANGELOG.md) under the new version, merge, and tag the merge commit (`v0.1.0`).

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
