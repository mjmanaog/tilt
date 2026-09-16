---
paths: ["app/src/main/java/com/mkdirchip/tilt/data/**/*.kt"]
---

`TilRepository` is this app's equivalent of an API handler layer: it is the *only* path any
caller (in-app editor, timeline, stats, widget) is allowed to use for reads or writes.

- Never call `TilDao` directly from `ui/` or `widget/` code, and never add a second entry point
  into the database. If a screen needs a new query, add it to `TilDao` and expose it through
  `TilRepository`, the same way every existing method does.
- `capture()` and `edit()` trim text and `require(cleaned.isNotEmpty())` before the DAO is ever
  touched — never trust caller-provided text directly into a `TilEntryEntity`. Any new write
  method needs the same validate-before-persist shape.
- Every write ends with `onDataChanged()` (wired to `refreshTiltWidgets` in `TiltApplication`).
  Adding a new write path without calling it will leave the widget stale.
- Labels are normalized (trim/lowercase/dedupe) via `normalizeLabels()` before hitting the DB —
  route new label input through it rather than inserting raw strings.
- `TilEntryEntity.localDate` is a plain string stamped at capture time, not derived from
  `createdAtEpochMillis` at query time — don't let a new query compute "today" from the epoch
  millis column, since that breaks the timezone guarantee documented on the entity.
