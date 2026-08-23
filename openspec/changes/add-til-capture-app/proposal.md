## Why

Small realizations are learned and lost in the same minute. By the time an app is opened, navigated, and a form is filled in, the thought is gone — so it never gets written down and never gets revisited.

TILT ("Today I Learned Today") makes capture cost almost nothing: a home-screen widget is one tap from a text field, and that same widget quietly resurfaces something already learned so past entries stay alive instead of rotting in a list nobody opens.

## What Changes

The repository is currently an empty Android Studio scaffold (`com.mkdirchip.tilt`, minSdk 30, Compose). This change builds the whole first version of the app on top of it.

- **Capture** — a TIL entry is free text plus zero or more free-form labels (`science`, `music`, `general`, anything typed). Timestamped automatically. Entries can be edited and deleted, with undo on delete.
- **Home-screen widget** — a resizable Glance widget with two jobs: a tap target that opens a translucent quick-capture overlay floating over the home screen, and a rotating display of a random past entry. With no entries yet it prompts *"What did you learn today?"*.
- **Quick-capture overlay** — a transparent activity launched from the widget: text field, label field, save, dismiss-on-outside-tap. The home screen stays visible behind it, so capture never feels like leaving what you were doing.
- **Timeline** — the in-app list, rendered as a staggered grid of cards of uneven height, each showing the entry clamped to a six-line preview with its labels, date, and time at the card's foot. The full text is one tap away in the entry view.
- **Filtering** — preset date chips (Today / This week / This month / All) plus a custom date-range picker, combined with label filters.
- **Stats** — a calendar streak view plus simple weekly and monthly counts ("you learned this much this week").
- **Visual system** — a dark navy palette in a Spotify-like idiom (near-black ground, one vivid electric blue accent, heavy type hierarchy), with gradients, animated transitions, and motion through list, capture, and stats. The direction is set by the reference screenshots in `design-refs/`: a masonry feed carrying its metadata at the card's foot with roughly one card in five filled in accent, and sweeping gradient arcs behind the sparser screens.

Assumptions recorded here rather than asked, because they do not change the shape of the work:

- Storage is **local-only** (Room). No accounts, no cloud sync, no cross-device backup.
- No full-text search, no reminders or notifications, no sharing/export. Not requested; out of scope.
- A streak is consecutive calendar days with at least one entry, evaluated in the device's local timezone.
- Labels are free-form but normalized (trimmed, case-insensitive) so `Science` and `science` are the same label, and previously used labels are suggested during capture.

## Capabilities

### New Capabilities

- `til-entry`: Creating, editing, and deleting a TIL entry — its text, its labels, its timestamp — and the durable local persistence behind it. The core write path shared by the in-app editor and the widget overlay.
- `til-timeline`: Browsing captured entries as a staggered card feed, and narrowing that feed by date range and by label.
- `til-stats`: Streak and volume reporting — the calendar streak view and the weekly/monthly counts.
- `til-widget`: The home-screen widget surface: its quick-capture tap target, its rotating past-entry display, its empty-state prompt, and how it stays current as entries change.
- `til-visual-system`: The cross-cutting look and motion contract — dark navy palette, gradients, transitions, and animation behavior that every screen and the widget must honor.

### Modified Capabilities

None. `openspec/specs/` is empty; this is the project's first change.

## Impact

**New dependencies** (added to `gradle/libs.versions.toml`):
- Room (`room-runtime`, `room-ktx`, KSP compiler) — local persistence
- Glance AppWidget (`androidx.glance:glance-appwidget`) — home-screen widget
- Lifecycle ViewModel Compose, Navigation Compose — screen state and routing
- KSP and Kotlin serialization plugins as needed by the above
- `kotlinx-datetime` or `java.time` (minSdk 30 makes `java.time` available without desugaring)

**Modified files**:
- `app/build.gradle.kts` — new dependencies, KSP plugin, Room schema export
- `app/src/main/AndroidManifest.xml` — quick-capture activity (translucent, `noHistory`), widget receiver, widget metadata
- `app/src/main/java/com/mkdirchip/tilt/ui/theme/*` — scaffold's purple Material default replaced by the navy palette
- `MainActivity.kt` — hosts navigation instead of the scaffold's greeting

**New code areas**: `data/` (entity, DAO, database, repository), `ui/timeline/`, `ui/capture/`, `ui/stats/`, `widget/`.

**Risk areas**: widget-to-app data freshness (the widget must update after a save from either surface), and staggered-grid performance as the entry count grows.

**No breaking changes** — nothing ships yet.
