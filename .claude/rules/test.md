---
paths: ["app/src/{test,androidTest}/**/*.kt"]
---

# Test conventions

- Name `@Test` methods as backtick-quoted plain-English sentences describing the behavior
  (e.g. `` `a missed day breaks the streak` ``), not `camelCaseDescriptions` — except DAO tests,
  which use `snake_case` method names instead (see `TilDaoTest`).
- No mocking framework is used anywhere in this codebase. Don't introduce one (Mockito, MockK,
  etc.) — test through real objects (plain functions, or an in-memory Room DB for DAO/repository
  tests via `Room.inMemoryDatabaseBuilder(...)`).
- Instrumented DAO/database tests (`app/src/androidTest/`) build a fresh in-memory `TiltDatabase`
  in `@Before` and `close()` it in `@After`; suspend calls run inside `runBlocking`.
- Add a one-line comment above a test's inputs when the data itself doesn't make the scenario
  obvious (e.g. `// 20, 21, 22, 23 — four consecutive days ending today.`).
