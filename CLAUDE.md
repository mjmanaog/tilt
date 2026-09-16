# CLAUDE.md

Guidance for Claude Code in this repo.

## Guidelines

*Bias toward caution over speed; use judgment for trivial tasks.*

- **Think first**: state assumptions, surface tradeoffs, ask if unclear — don't pick silently among interpretations. Push back when a simpler approach exists.
- **Simplicity**: minimum code for the task; no speculative abstractions, flexibility, or error handling for impossible cases.
- **Surgical changes**: touch only what the task requires; match existing style; don't refactor or clean up unrelated code (mention dead code, don't delete it); only remove imports/vars your own change orphaned.
- **Goal-driven**: turn tasks into verifiable checks (e.g., "fix bug" → reproduce with a test, then make it pass); state a short plan with a verify step per item on multi-step work.

## Critical: never run bare `./gradlew`

A sandboxed Gradle daemon writes into `.gradle/`/`app/build/`; macOS then ACL-locks those paths
(`com.apple.macl`) to the sandbox, breaking the user's own Android Studio/Terminal builds
(`fileHashes.lock (Operation not permitted)`) — and the attribute can't be cleared from inside
the sandbox.

Always redirect build output and cache outside the repo:

```sh
R=/tmp/tiltbuild/$(date +%s)$$; mkdir -p "$R"
cat > "$R/init.gradle" <<EOF
gradle.beforeProject { p -> p.layout.buildDirectory.set(new File("$R/out/" + p.name)) }
EOF
./gradlew "$@" --init-script "$R/init.gradle" --project-cache-dir "$R/cache" --console=plain
```

If it already happened, fix from Terminal.app (not the sandbox): `pkill -f GradleDaemon; rm -rf .gradle app/build` (both gitignored).

## Never touch signing/local config files

`*.jks` (keystore), `keys.properties` (signing credentials), and `local.properties` (local SDK
path/secrets) must never be read, edited, or written — treat them as opaque even if asked.
Enforced in `.claude/settings.json` (`permissions.deny`: `Read`/`Edit`/`Write` on all three); if
one needs a value read or changed, ask the user to do it directly.

## Commands

(prefix all with the wrapper above)

```bash
./gradlew build
./gradlew installDebug
./gradlew test                                                    # unit tests (app/src/test)
./gradlew test --tests "com.mkdirchip.tilt.ui.stats.StreakTest"   # single test class
./gradlew connectedAndroidTest                                    # instrumented, needs device/emulator
```

## Architecture

Single-module Android app (`com.mkdirchip.tilt`, minSdk 30, Compose, no DI framework).

- **Repository-centric**: `TilRepository` is the sole read/write path (editor, timeline, stats,
  widget). Writes trigger `onDataChanged` → `refreshTiltWidgets`, wired in `TiltApplication` so
  the repository never imports UI/widget code. DI is hand-rolled via `Context.tiltContainer`
  (lazy `database`/`repository`), since the Glance widget runs outside the Activity/ViewModel
  graph.
- **Single NavHost**: `MainActivity` hosts routes `timeline`/`stats`/`capture` (optional
  `entryId`, absent = new entry). Screens get ViewModels via `factory(repository, ...)`. Widget
  deep-links via the `EXTRA_ENTRY_ID` intent extra, consumed once via `LaunchedEffect`.
- **Widget** (`widget/`): `TiltWidget` collects `repository.observeEntries()` inside composition
  (not `provideGlance`) so a live session sees edits without Glance re-invoking `provideGlance`.
  Layout (`StripBody`/`StackedBody`) switches on `LocalSize.current.height`. The capture button
  opens `QuickCaptureActivity` (translucent overlay), never `MainActivity`.
- **Domain rules**: labels normalized (trim/lowercase/dedupe) in `LabelNormalizer` before DB
  write. Streak = consecutive *calendar days* with ≥1 entry, local timezone
  (`ui/stats/Streak.kt`). `TilEntryEntity.localDate` (stamped at capture) drives date
  filtering/streaks independently of `createdAtEpochMillis`.

## Specs

`openspec/changes/add-til-capture-app/`: `proposal.md` (scope, non-goals: no cloud sync/search/
reminders/export), `design.md` (navy `#0a0e27` + blue `#00d9ff`), `specs/` per capability
(`til-entry`, `til-timeline`, `til-stats`, `til-widget`, `til-visual-system`). Update via the
`openspec-*` skills, not by hand.
