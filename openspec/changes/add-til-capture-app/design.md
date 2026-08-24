## Context

The repo is an unmodified Android Studio Compose scaffold: single `:app` module, `com.mkdirchip.tilt`, AGP 9.2.1, Kotlin 2.2.10, Compose BOM 2026.02.01, minSdk 30 / targetSdk 36, and a `MainActivity` that greets. There is no data layer, no navigation, and the theme is the generated purple Material default. See `proposal.md` — Why for motivation, and `specs/` for the behavior being committed to.

Three constraints shape everything below:

1. **Glance cannot host a text field.** Android widgets render through RemoteViews; there is no focusable input and no keyboard. "Tap the widget and type" therefore has to resolve to *some* activity being launched — the only question is which kind (see Decisions).
2. **minSdk 30** means `java.time` is available natively with no core-library desugaring, and Glance's modern APIs are all in range.
3. **Two write surfaces, one store.** The widget capture surface and the in-app editor must produce identical entries and must both leave the widget showing fresh content. That makes the repository, not any ViewModel, the place where "data changed" is known.

Two reference screenshots now sit in `design-refs/`: `img.png`, a notes app whose masonry feed, foot-placed metadata, and single accent-filled card set the timeline's direction; and `img_1.png`, a navy fintech set supplying the gradient language and the gradient-stroke progress ring. The palette values and layout decisions below are read off those rather than invented.

## Goals / Non-Goals

**Goals:**

- One store of truth (Room), exposed as `Flow`, so the timeline, stats, and widget can never disagree about what exists.
- Capture from the home screen in one tap with the home screen still visible behind — capture must not *feel* like launching an app.
- Keep the aesthetic requirements (`til-visual-system`) enforceable in one place rather than re-decided per screen.
- Keep the date/streak logic in pure functions so the `til-stats` scenarios are unit-testable without a device.

**Non-Goals (design-level, beyond the proposal's scope exclusions):**

- No multi-module split, no `domain`/`data`/`presentation` layering with use-case classes per operation. This is a single-user local app; that structure would be ceremony.
- No DI framework. See Decisions.
- No offline-first sync machinery, no `WorkManager`. Nothing to sync to.
- No custom layout engine for the card feed; Foundation's staggered grid is sufficient.

## Decisions

### Single module, package-by-feature, repository over Room

`data/` (entities, DAOs, database, one repository), `ui/theme/`, `ui/timeline/`, `ui/capture/`, `ui/stats/`, `widget/`. One `ViewModel` per screen, state exposed as `StateFlow`, DAO queries returned as `Flow` so writes from any surface propagate automatically.

*Alternative considered:* Clean Architecture with use cases and separate domain models. Rejected — for roughly a dozen operations it triples the file count and adds a mapping layer whose only job is to copy fields.

### Manual dependency container, not Hilt

An `Application` subclass owns the database and repository; screens get the repository through a `ViewModel.Factory`, and the widget reads it straight off the `Application`.

*Rationale:* the widget is the deciding factor. Glance's `provideGlance` runs outside the activity/ViewModel graph, so with Hilt it needs `EntryPointAccessors` gymnastics; with a container it is one property access. Hilt would also add another KSP processor to a build already on a very new AGP.

*Alternative considered:* Hilt (rejected as above), Koin (a dependency to earn its keep across ~3 injection sites — it wouldn't).

### Normalized label schema

Three tables: `entries`, `labels`, and an `entry_labels` cross-reference.

`labels` stores `name` as the trimmed, lowercased normalization key with a unique index, plus `displayName` holding the form as first typed. That is what satisfies `til-entry` — "Science and science are the same label" becomes a uniqueness constraint rather than application logic, "only labels in use are offered" becomes a join, and "an unused label disappears from filters" falls out of the same join without a cleanup pass.

*Alternative considered:* labels as one delimited string column on `entries`. Tempting for a small app, but every label query degrades into `LIKE '%,science,%'` matching, the in-use label list needs a full-table scan and manual dedup in Kotlin, and case-insensitive identity has to be re-enforced at every write site. The join table is less code overall, not more.

### Store both an epoch timestamp and the local capture date

`entries` holds `createdAtEpochMillis` (UTC) **and** `localDate` as a `yyyy-MM-dd` text column, written from the device's timezone at capture.

*Rationale:* "the day I learned it" is a local-calendar fact, and it should not change later. Deriving the date from epoch millis under the *current* timezone means flying somewhere silently reshuffles which day entries belong to — and therefore silently rewrites streaks. Storing the local date at capture also turns every date-bucketing query (`til-timeline` ranges, `til-stats` calendar and counts) into a plain string comparison or `GROUP BY`, which SQLite indexes well.

*Trade-off:* a denormalized derived column, and time-of-day is still rendered from the epoch value in the device's current zone — so an entry captured abroad shows the local clock time of wherever you are now. Accepted: the date is what statistics depend on, and the time is decoration on the card.

*Alternative considered:* epoch millis only (rejected — travel-unstable streaks); also storing the zone offset (rejected — a third column to make card timestamps marginally more faithful).

### Widget capture is a translucent activity, not a route in the app

A dedicated `QuickCaptureActivity` with a translucent, non-opaque theme (`windowIsTranslucent`, transparent window background, `excludeFromRecents`, `noHistory`, `adjustResize` with the keyboard shown on entry). Glance launches it via `actionStartActivity`. Tapping outside or the back gesture finishes it without writing.

*Rationale:* `til-widget` requires the home screen to stay visible behind the capture surface and the timeline *not* to come to the foreground. A translucent activity is the only construct that does that — it never draws an opaque window, so the launcher keeps rendering underneath.

*Alternative considered:* deep-linking `MainActivity` to a bottom-sheet route. Rejected: `MainActivity` is opaque, so the user would see the app's timeline behind the sheet, not their home screen, and backing out would drop them into the app rather than back where they were.

The activity and the in-app editor share one `CaptureContent` composable and one `CaptureViewModel` so the two surfaces cannot drift apart in validation or label handling.

### The widget reads Room directly and is refreshed by the repository

`provideGlance` queries the repository for a random entry. Freshness has two triggers: the repository calls `GlanceAppWidget.updateAll` after every insert/update/delete, and the widget provider declares `updatePeriodMillis` of roughly 30 minutes to rotate which entry is shown.

*Rationale:* one source of truth. The alternative — mirroring a chosen entry into Glance's preferences state — introduces a second copy that can go stale exactly when the spec forbids it (the displayed entry being edited or deleted).

**Implementation correction, found on device.** The obvious shape — read the entry in `provideGlance`, pass it to the content, and call `updateAll` after each write — does not work. `GlanceAppWidget.update` recomposes an *existing* session without re-running `provideGlance`, so an entry captured outside the composition is frozen for the life of that session: placed widgets kept displaying entries that had been deleted, and only a system-sent update (a reinstall, or the periodic tick) ever refreshed them. Two further dead ends: broadcasting `ACTION_APPWIDGET_UPDATE` from the app is impossible because it is a protected broadcast only the system may send, and `getGlanceIds` returning ids is not evidence of anything — the ids resolve fine while the content still never changes.

What works is to collect the entry list as state *inside* `provideContent`, so the composition itself observes the repository. The random choice is then held in `remember` keyed on the collection's size, and the chosen entry's content is looked up fresh on every pass. That combination satisfies three requirements at once: the pick does not flicker between recompositions, a save or delete re-picks, and editing the displayed entry updates its text in place. Keying `remember` on the entry's identity alone is not enough — an edit changes neither the list size nor the first id, so the stale text would survive.

*Note:* `updatePeriodMillis` has a 30-minute platform floor, so "rotates over time" means roughly half-hourly, not per-glance. Faster rotation would need a scheduled worker waking the device — not worth the battery for a decorative refresh.

### Gradients: `Brush` in the app, a drawable in the widget

In-app surfaces use Compose `Brush` gradients. Glance does **not** support Compose brushes, so the widget's gradient is a `<gradient>` shape drawable referenced as its background.

This is called out because it is the one place `til-visual-system` cannot be satisfied by shared code: the palette must be defined once as color resources *and* as Compose tokens, and the widget's gradient drawable must be kept in step with the app's brush by hand.

### Forced dark theme with a fixed palette

`TiltTheme` never calls `isSystemInDarkTheme()`; it always applies the navy scheme. The palette is taken from the references rather than guessed:

| token | value | role |
| --- | --- | --- |
| `ground` | `#0A0F1C` | near-black navy; every screen's background |
| `surface` → `surfaceLift` | `#141B2D` → `#1C2540` | the two stops of the card gradient |
| `accent` | `#1D4ED8` | electric blue: FAB, selected chips, accent-filled cards |
| `accentPartner` | `#5FC8F5` | the cyan end of arc and ring gradients |
| `onGround` | `#EDF1F8` | body and entry text |
| `muted` | `#8593AC` | dates, times, and label chips at rest |
| `onAccentMuted` | `#C9D8FF` | metadata *on* an accent-filled card |

Contrast discipline for `til-visual-system`: the card gradient's stops sit inside a narrow luminance band, so the 4.5:1 check holds at the card's lightest stop rather than only on average.

These values were measured, not estimated, and the measurement changed them. An earlier draft of this document put `accent` at `#2F6BFF` and claimed white-on-accent at roughly 4.8:1; the actual computed ratio is **3.97:1**, which fails the 4.5:1 the spec commits to, and `#7C8AA3` metadata on that accent was 1.29:1 — the exact risk noted below. Darkening the accent to `#1D4ED8` puts body text on an accent-filled card at 5.92:1, lifting `muted` to `#8593AC` puts metadata at 4.87:1 against the lightest card stop, and metadata on the accent needs its own tone (`onAccentMuted`, 4.71:1) because no grey survives a blue fill.

The lesson generalises: no accent hue may be adopted on the strength of how vivid it looks. Compute every text pairing against it first, and re-measure if the hue is ever retuned.

Material3's `darkColorScheme` is still populated (so stock components inherit sane colors), but screens read the app's own token object for anything expressive.

### Card previews clamp at six lines

The feed clamps entry text to six lines with an ellipsis; the entry view renders the same text unclamped. Tapping a card is what reveals the rest.

*Rationale:* the reference feed earns its tidy rhythm by clamping, and an unclamped card can exceed the viewport on a single long entry, leaving one column absurdly longer than the other. Clamping in *lines* rather than at a fixed height is what keeps this correct under font scaling — a fixed height would clip large text mid-glyph, which `til-visual-system` forbids.

*Trade-off:* text is hidden until tapped. Accepted, because six lines is far more than a typical TIL needs, so the clamp will rarely engage at all.

### Accent-filled cards are chosen deterministically, not randomly

Whether a card is accent-filled is a pure function of the entry's stored id — roughly "every fifth id" by hash — evaluated at render time, with nothing persisted.

*Rationale:* `til-visual-system` requires the treatment to be stable across scroll, filter, and restart. A random draw per composition would reshuffle the accent cards as the user scrolls, which reads as a rendering bug rather than as rhythm. Deriving it from the id buys that stability with no column to store, migrate, or backfill.

*Alternatives considered:* a persisted `isAccent` flag (rejected — storage for a purely presentational fact); accenting today's entries so the blue means something (rejected by the user in favor of a neutral scatter, so the color carries no meaning that could mislead).

### Ambient arcs are a halo around the streak ring

The arcs are drawn in a single `Canvas` as arc strokes carrying a gradient from `accent` to `accentPartner`, at low alpha. The streak figure uses the same technique: an arc stroke with a gradient and a white round cap at its leading end, lifted from `design-refs/img_1.png`.

Where they go took two corrections, both after looking at them on a device:

- They are **concentric with the streak ring and confined to its box**, rather than drawn behind the whole screen. Full-screen arcs sat behind *scrolling* content, so they crossed the percentage numerals and cut a diagonal band through the calendar grid, and their apparent position changed as the screen scrolled. Orbiting the ring gives them a reason to be where they are.
- Their radii derive from the box — `(minDimension / 2) - strokeWidth` — so every arc terminates in a round cap inside the canvas instead of being clipped into a hard straight edge at its bounds. The innermost radius still clears the ring itself, so nothing overlaps the figure.

*Rationale:* they are decoration with no layout role, so a Canvas costs one draw pass and no measurement. Keeping them off the timeline and out of the empty states — as `til-visual-system` requires — also means those screens' contrast guarantees never have to account for a varying backdrop behind text.

### Navigation Compose for three destinations

Timeline, Stats, and Capture (`entryId` argument; null means "new"). Chosen over hand-rolled state switching because `til-visual-system` requires animated screen transitions, and per-destination enter/exit transitions come free here. The timeline's filter state lives in the Timeline `ViewModel`, which is why filters survive navigation but reset on cold start — exactly the behavior `til-timeline` specifies, with no persistence code.

### Reduced motion via an explicit CompositionLocal

Compose exposes no reduce-motion flag. Read `Settings.Global.ANIMATOR_DURATION_SCALE` once and publish it as `LocalReducedMotion`; animation helpers consult it and collapse to instant or a cross-fade when set.

*Rationale:* centralizing it means individual screens can't forget it. Scattering the check across call sites is how accessibility regressions happen.

### Undo delete by re-insertion, not a soft-delete flag

Delete removes the row; the entity and its label associations are held in memory for the snackbar's lifetime and re-inserted in a transaction with the original id, timestamp, and labels if undone.

*Alternative considered:* an `isDeleted` flag. Rejected — it taints every query, every count, and every streak calculation with a filter that must never be forgotten, in exchange for surviving a process death during a ~5-second window.

*Trade-off:* if the process is killed while the snackbar is showing, the deletion stands. Acceptable.

### Testing approach

Date-range bucketing and streak calculation are pure functions over `LocalDate` sets, unit-tested directly against the `til-stats` and `til-timeline` scenarios — including the ones easiest to get wrong (today-not-yet-captured, multiple entries in one day, longest streak surviving a break). DAO filter queries get instrumented tests. Label normalization gets unit tests for the case-collision scenarios. The visual system is verified by inspection plus a contrast check on the final palette.

### Empty states are treated by meaning, not uniformly

Both empty states initially shared one composable with the ambient arcs behind it. The arcs read as abstract decoration in the one place the screen has something to say, so the states were split:

- **Nothing captured yet** draws dimmed, non-interactive placeholders in the feed's own staggered arrangement, with the prompt over a softened scrim. It answers "what is this screen for" by showing the shape the screen is about to take.
- **Nothing matches the filters** is undecorated. Entries do exist; this is a dead end to back out of, and placeholders here would imply content that is not there.

The scrim over the placeholders is deliberately not opaque, so the silhouette continues faintly behind the text rather than splitting the screen into two disconnected bands. That choice is compositional, not a legibility one: body text over the placeholder tone measures about 13:1, so contrast holds with or without it.

### The widget defaults to a 4x1 strip, with a second layout for height

The widget targets 4x1 — one row — because that is all it needs to do its two jobs, and a
two-row block dominates a home screen for no gain. Below roughly 90dp of height it renders as a
strip: entry, date, and capture target on a single line. Above that it uses the stacked
arrangement, with the line budget growing as height allows.

Sizing follows the launcher's cell formula, `(70 * cells) - 30`: 250dp wide for four columns,
40dp tall for one row. `targetCellWidth`/`targetCellHeight` are honoured from API 31; `minWidth`
and `minHeight` are what API 30 reads, so both are set.

In the strip, the date is a **fixed-width sibling** of the entry text rather than appended to it.
Concatenating them would let a long entry push the date past the ellipsis and out of view, which
would break the requirement that the widget shows an entry *together with* its date. Giving the
text the weight and the date its natural width makes the text the only thing that can truncate.

### The in-app save action is pinned to the bottom edge

The editor's fields sit at the top and its save button is pinned to the bottom, within thumb
reach, rather than following the fields down the page — on a tall phone that left the primary
action stranded mid-screen. The widget's overlay keeps its button inline, because that sheet is
already bottom-anchored.

`CaptureSaveButton` is therefore its own composable shared by both surfaces, and `CaptureContent`
takes an `inlineSave` flag. The enabled/disabled rule stays derived from a single
`CaptureUiState.canSave`, so the two placements cannot diverge on validation.

### Platform details that cost time

Recorded because each one was found by running the app, not by reading:

- **KSP versus AGP 9's built-in Kotlin.** AGP 9 refuses KSP outright — *"Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin"* — because KSP registers its generated sources that way. `android.disallowKotlinSourceSets=false` in `gradle.properties` is the documented escape hatch and the only reason Room's processor runs at all.
- **`enableEdgeToEdge()` follows the system theme, not yours.** Its default picks bar appearance from the device's light/dark setting, so on a light-mode device a permanently dark app gets dark status-bar icons on a navy ground. `SystemBarStyle.dark(TRANSPARENT)` for both bars is required to honour the forced-dark requirement.
- **A screen without a `Scaffold` gets no window insets.** The statistics screen initially drew its heading under the status bar and its calendar under the navigation bar; `statusBarsPadding()` and `navigationBarsPadding()` have to be applied by hand.
- **Compose focus does not raise the keyboard.** `FocusRequester.requestFocus()` focuses the field but leaves the IME closed, which defeats the point of one-tap capture. `LocalSoftwareKeyboardController.show()` is also needed.
- **Material3's snackbar defaults to `Indefinite` when given an action label**, which would mean the undo offer never lapses and a deletion never becomes permanent. The duration has to be set explicitly.
- **Glance cannot round a view's corners below API 31.** `cornerRadius` is silently dropped with a log warning, so the widget's capture target is square on API 30 and rounded from 31 up. Accepted rather than worked around with a second drawable.
- **Glance's line budget must fit the space it is given.** Asking for more `maxLines` than the available height fits makes it clip the final line mid-glyph instead of ellipsising it, which reads as a rendering fault. The budget is deliberately conservative.

## Risks / Trade-offs

- **Glance's restricted component and modifier set** → The widget layout is built only from Glance primitives with a drawable-backed gradient; the widget is treated as its own small design surface rather than a port of a Compose card. Budget time for it to look different in kind from the in-app cards.
- **Very new toolchain (AGP 9.2.1, Compose BOM 2026.02.01)** → Room, KSP, Glance, and Navigation versions must be resolved against this AGP/Kotlin pair at implementation time, not assumed. Verify the build compiles after adding dependencies, before writing feature code.
- **Item animations in `LazyVerticalStaggeredGrid`** → `til-visual-system` requires filtered items to animate in, out, and to new positions. If `animateItem` proves unavailable or janky for staggered grids in this Compose version, fall back to a cross-fade of the grid content on filter change, which still satisfies "animates rather than jumps" without per-item placement animation.
- **Clamped previews hide text until tapped** → A long entry's tail is invisible in the feed, so the grid can no longer be skim-read in full. This is the accepted cost of the reference's tidy rhythm (`til-timeline`: a six-line clamp); it is mitigated by the clamp being generous enough that most entries never reach it.
- **Accent-filled cards must stay legible on every gradient they replace** → Confirmed and resolved: the muted tone measured 1.29:1 against the accent, so accent-filled cards use a dedicated `onAccentMuted` tone. Any future palette change must re-check both card treatments, not just the dark one.
- **Widget rotation is bounded by the 30-minute update floor** → Accepted; the widget also refreshes on every write, so it never feels frozen during active use.
- **Two definitions of the palette (Compose tokens and XML resources)** → Drift between the app's and the widget's colors. Mitigated by defining hex values once in `colors.xml` and having the Compose tokens reference those resource values, so the widget and the app read the same source.
- **Staggered-grid performance as entries accumulate** → Room returns a `Flow` of the filtered list; if the feed grows into the thousands, switch to `PagingSource` behind the same `ViewModel` state. Not built now — the entry count for a personal journal will not reach that for a long time, and paging changes no specified behavior.

## Migration Plan

Greenfield: no data to migrate and nothing deployed. Room starts at schema version 1 with `exportSchema = true` so later structural changes have a baseline to migrate from. Rollback during development is uninstall-and-reinstall; no `fallbackToDestructiveMigration` in release configuration.

The build order matters more than deployment: add and verify dependencies first, then the data layer, then the theme, then screens, then the widget — the widget depends on the palette, the repository, and the shared capture composable all existing.

## Open Questions

- Whether the widget's default size and the number of body lines it shows before truncating want tuning after living with it on a real home screen.
- Whether the stats screen should eventually compare a period against the previous one ("more than last week"). Deliberately out of scope now — the specs commit only to counts — and addable later without touching the data model.

## Verification note

On-device verification ran on an API 30 emulator (the project's `minSdk`); the instrumented suite also passed once on API 35. Every scenario across the five capabilities has now been observed on a device.

One caveat on `til-widget`'s resize scenarios. The small-size behaviour — the single-row layout, the entry text ellipsising while its date survives, the capture target still tappable and opening capture with the keyboard raised — was confirmed by placing the widget at its 4x1 default, not by dragging its resize handles: Launcher3 ignores injected touch streams on those handles (`draganddrop`, stepwise `motionevent`, and a slow `swipe` all left the widget's bounds unchanged), and its home screen is rotation-locked. The rendering path is the same one a manual resize exercises — `SizeMode.Exact` recomposing against `LocalSize` — but the drag interaction itself has not been driven.
