## 1. Build setup

- [x] 1.1 Add versions and library entries to `gradle/libs.versions.toml` for Room (`room-runtime`, `room-ktx`, `room-compiler`), KSP, Glance AppWidget, Navigation Compose, and Lifecycle ViewModel Compose — resolving versions compatible with AGP 9.2.1 / Kotlin 2.2.10 rather than assuming latest
- [x] 1.2 Apply the KSP plugin and wire the new dependencies into `app/build.gradle.kts`, with Room schema export configured (`exportSchema = true`, schema output dir)
- [x] 1.3 Verify the project still assembles (`./gradlew :app:assembleDebug`) before any feature code is written — this is the checkpoint for the new-toolchain risk in design.md

## 2. Data layer

- [x] 2.1 Define the `entries` entity with `id`, `text`, `createdAtEpochMillis`, and `localDate` (`yyyy-MM-dd` text), indexed on `localDate`
- [x] 2.2 Define the `labels` entity with a unique-indexed normalized `name` and a `displayName`, plus the `entry_labels` cross-reference entity with its foreign keys and cascade deletes
- [x] 2.3 Create the Room database (version 1) and DAOs: insert/update/delete entry with labels in a transaction, observe filtered entries as `Flow`, observe in-use labels as `Flow`, query distinct `localDate` values, and fetch one random entry
- [x] 2.4 Implement label normalization (trim, lowercase for the key, retain first-typed display form) as a pure function with unit tests covering the `Science`/`science` collision and the duplicate-label-on-one-entry case
- [x] 2.5 Implement the repository over the DAOs, exposing capture, edit, delete, undo-restore (re-insert preserving id, timestamp, and labels in one transaction), and the observable queries
- [x] 2.6 Create the `Application` subclass holding the database and repository, and register it in the manifest
- [x] 2.7 Write instrumented DAO tests for the date-range and label filter queries, including the multi-label OR case and the date-plus-label AND case

## 3. Visual system

- [x] 3.1 Define the palette once in `res/values/colors.xml` using the values fixed in design.md — ground `#0A0F1C`, card gradient `#141B2D` → `#1C2540`, accent `#2F6BFF`, gradient partner `#5FC8F5`, body `#EDF1F8`, muted `#7C8AA3` — so the app and the widget read one source
- [x] 3.2 Replace the scaffold's generated `Color.kt`/`Theme.kt` with `TiltTheme`: Compose color tokens referencing those resources, a populated Material3 `darkColorScheme`, and no call to `isSystemInDarkTheme()` — the theme is always dark
- [x] 3.3 Define the gradients: the card brush (`#141B2D` → `#1C2540`), the accent card fill, the scroll-edge scrim, and the accent-to-cyan sweep used by arcs and the streak ring
- [x] 3.4 Verify contrast against every surface the body and muted tones land on — the card gradient's lightest stop and the `#2F6BFF` accent fill — confirming the ≈4.8:1 white-on-accent figure and substituting a distinct on-accent tone if the muted tone fails
- [x] 3.5 Set the type scale in `Type.kt` for a Spotify-like hierarchy — heavy, large headings against restrained foot metadata — keeping entry text the most prominent element on a card
- [x] 3.6 Build the reusable ambient-arc backdrop as a `Canvas` of large-radius gradient arc strokes at low alpha, for use behind the stats screen only
- [x] 3.7 Add `LocalReducedMotion`, reading `Settings.Global.ANIMATOR_DURATION_SCALE`, plus animation-spec helpers that collapse to instant or cross-fade when it is set
- [x] 3.8 Update `res/values/themes.xml` and the launcher/system bar styling so the app window matches the navy ground with no light-theme flash on launch

## 4. App shell

- [x] 4.1 Replace `MainActivity`'s scaffold greeting with `TiltTheme` hosting a Navigation Compose graph for Timeline, Stats, and Capture (`entryId` argument, null meaning new)
- [x] 4.2 Configure per-destination enter/exit transitions that honor `LocalReducedMotion`
- [x] 4.3 Add navigation between Timeline and Stats, and a `ViewModel.Factory` that supplies the repository from the `Application` container

## 5. Capture

- [x] 5.1 Build the shared `CaptureContent` composable: multi-line text field, label field with suggestions from previously used labels, chips for attached labels, and a shared `CaptureSaveButton` — placed inline or pinned by the caller — disabled while the text is blank or whitespace-only
- [x] 5.2 Build `CaptureViewModel` handling both new and existing entries — trimming surrounding whitespace while preserving interior line breaks, preserving the original capture timestamp on edit, and rejecting a blank save
- [x] 5.3 Wire the in-app capture destination for creating a new entry and for editing an existing one, showing the entry's text in full with no clamp, pinning the save action to the bottom edge within thumb reach, and discarding changes when the user leaves without confirming
- [x] 5.4 Create `QuickCaptureActivity` hosting `CaptureContent`, with a translucent non-opaque theme, `excludeFromRecents`, `noHistory`, `adjustResize`, keyboard raised and field focused on entry, and finish-without-saving on outside tap or back
- [x] 5.5 Verify on device that the home screen stays visible behind `QuickCaptureActivity` and that the app's timeline is never brought to the foreground by it
- [x] 5.6 Animate the capture surface into view on both entry points

## 6. Timeline

- [x] 6.1 Build the entry card: gradient surface, text clamped to six lines with an ellipsis, and the label chips, date, and time at the card's foot below the text
- [x] 6.2 Add the accent-filled card variant, and select it deterministically from the entry id so roughly one card in five is accent-filled — with a unit test proving the same id always yields the same treatment
- [x] 6.3 Build the feed with `LazyVerticalStaggeredGrid` so card heights follow text length up to the clamp, ordered by capture timestamp newest first, with the scroll-edge scrim over the bottom of the list
- [x] 6.4 Build the filter bar: preset date chips (Today / This week / This month / All) and a custom date-range picker, with week boundaries taken from the device locale's first day of week
- [x] 6.5 Add multi-select label filtering to the filter bar, sourced from the in-use labels query
- [x] 6.6 Implement pure date-range bucketing functions (preset resolution and inclusive custom range) with unit tests covering the inclusive endpoints and the single-day range
- [x] 6.7 Build `TimelineViewModel` combining date range and label selection into the filtered query — labels OR'd together, then AND'ed with the date range — defaulting to All with no labels on cold start and retaining selections across navigation
- [x] 6.8 Implement the two distinct empty states — dimmed placeholder cards previewing the feed when nothing has ever been captured, versus an undecorated message when the filters exclude everything — with a clear-filters action on the latter that resets to All with no labels
- [x] 6.9 Add item animations for cards entering the feed and for filter-driven insertions, removals, and repositioning, falling back to a content cross-fade if `animateItem` misbehaves on staggered grids in this Compose version
- [x] 6.10 Wire card tap to open the entry for editing, and a delete action offering an undo snackbar that restores the entry to its original position
- [x] 6.11 Verify a new entry appears at the top of the feed with no manual refresh, that an entry under six lines shows with no ellipsis, and that a longer one clamps with an ellipsis and opens in full when tapped

## 7. Stats

- [x] 7.1 Implement streak calculation as pure functions over a set of `LocalDate` values — current streak (counting a run ending today, or ending yesterday when today has no entry) and longest streak ever
- [x] 7.2 Unit-test the streak functions against every `til-stats` scenario: run ending today, today not yet captured, run broken by a missed day, several entries in one day counting once, and longest streak surviving a break
- [x] 7.3 Implement weekly and monthly summaries — entry count and count of distinct active days — with the week starting on the device locale's first day of week, plus unit tests including week rollover
- [x] 7.4 Build the calendar streak view: a month grid marking days that hold entries, greater visual weight for busier days, inactive unmarked future dates, and navigation between months
- [x] 7.5 Build the streak ring as a gradient arc stroke with a white round cap at its leading end, over the ambient arc backdrop
- [x] 7.6 Build the summary section presenting the weekly and monthly figures in the reference's large, heavy numerals
- [x] 7.7 Build `StatsViewModel` deriving everything from the observable distinct-dates and count queries so adds, edits, and deletes are reflected without manual refresh
- [x] 7.8 Implement the no-entries state: zeroed streaks and counts with an inviting message, and the current month rendered with nothing marked

## 8. Widget

- [x] 8.1 Create the gradient shape drawable for the widget background from the shared `colors.xml` values, matching the in-app card gradient by eye
- [x] 8.2 Build the Glance widget composable: a capture tap target plus a display area for a past entry's text and capture date, styled from the shared palette
- [x] 8.3 Implement `provideGlance` to read one random entry from the repository, computing the random pick once per update so the widget cannot flicker between entries across recompositions
- [x] 8.4 Implement the empty state — the "What did you learn today?" prompt in place of an entry — keeping the capture target functional
- [x] 8.5 Wire the capture target to launch `QuickCaptureActivity` via `actionStartActivity`, and the displayed entry to open the app on that entry
- [x] 8.6 Register the widget receiver and metadata in the manifest, with `updatePeriodMillis` at the 30-minute floor for rotation, plus a preview image and a 4×1 default size (250dp × 40dp, with `targetCell*` for API 31+)
- [x] 8.7 Call `updateAll` from the repository after every insert, update, and delete so the widget reflects saves from either surface
- [x] 8.8 Implement responsive sizing so the layout adapts across supported widget sizes — a single-row strip where there is no height to stack, the stacked arrangement above that — truncating long entry text with a visible ellipsis while keeping the date and the capture target visible at the smallest size
- [x] 8.9 Verify on device: saving from the widget, saving in the app then seeing the widget refresh, deleting the displayed entry, editing the displayed entry, and deleting the last entry to return to the prompt

## 9. Verification

- [x] 9.1 Run the full unit and instrumented test suites and confirm they pass
- [x] 9.2 Walk the scenarios in each spec file on a device and confirm each one behaves as written, noting any deviation rather than quietly accepting it
- [x] 9.3 Verify the app renders dark with the system set to light mode, and that no screen presents a light variant
- [x] 9.4 Verify accent-filled cards keep the same treatment across scrolling away and back, across a filter change, and across an app restart
- [x] 9.5 Verify at the largest system font scale that text grows, that cards clamp at six lines of the larger text rather than a fixed height, and that nothing is clipped or overlapped
- [x] 9.6 Verify no ambient arcs are drawn behind the timeline feed or either empty state, and that text over the arcs on the stats screen and over the first-run placeholders holds its contrast
- [x] 9.7 Verify with system animations turned off that transitions resolve instantly or as cross-fades and that every screen, control, and entry stays reachable
- [x] 9.8 Confirm entries survive a force-stop and relaunch, and that capture, browsing, filtering, and stats all work with the device offline
