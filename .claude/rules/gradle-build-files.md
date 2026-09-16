---
paths:
  - "**/build.gradle.kts"
  - "gradle/libs.versions.toml"
---

# Gradle build file conventions

- `gradle/libs.versions.toml` is the single source of truth for dependency versions. Never
  hardcode a version string directly in a `build.gradle.kts` — add or reuse a `[versions]` entry
  and reference it via `libs.*`.
- Don't add a new Gradle plugin or dependency unless the task actually needs it — this is a
  single-module app with a deliberately small dependency set (Compose, Room, Glance, Navigation,
  Lifecycle ViewModel). Check `libs.versions.toml` for an existing entry before adding one.
- `app/build.gradle.kts` sets `ksp { arg("room.schemaLocation", ...) }` so Room exports schemas to
  `app/schemas/`. Don't remove or bypass this — it's what lets Room validate migrations.
- This note is about editing build file *content*; how to actually *run* Gradle from this sandbox
  (never a bare `./gradlew`) is covered in the root `CLAUDE.md`.
