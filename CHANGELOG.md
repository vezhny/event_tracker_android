# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Project skeleton: Compose + Material 3, Koin, Navigation Compose, Room wiring, GitHub Actions CI.
- Settings dialog: theme (system, light, dark), language (system, English, Russian), app version.
- Placeholder icon and color palette.
- Events: add, edit, and delete (with confirmation) on the Events tab.
- Home: events as a tile grid. Tap, confirm, and the event is recorded now; the snackbar offers undo.
- Room database with `event` and `event_record` tables.
- Each tile shows when the event last happened.
- Export of events, or events with history, to JSON; import merges without duplicates.
- Statistics tab: counts per event for the last week, month, year, or a custom range of days.
