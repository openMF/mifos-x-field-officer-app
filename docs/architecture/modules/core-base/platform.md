# `core-base/platform`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_PLATFORM.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 23 Kotlin files (0 test) · source sets: `androidMain`, `commonMain`, `nonAndroidMain`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

No module dependencies — this is a leaf.

**Consumed by** 1 module(s): `core/platform`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_PLATFORM.md`.

## Principal types

- **`AppContext`** — Represents an abstract context for the application that provides platform-specific
- **`AppReviewManager`** — Manages application review requests across platforms.
- **`AppReviewManagerImpl`** — The single implementation of [AppReviewManager], for every target.
- **`AppUpdateManager`** — In-app update check, with real behaviour on every target.
- **`AppUpdateManagerImpl`** — The one [AppUpdateManager], for every target.
- **`MimeType`** — Represents standardized MIME (Multipurpose Internet Mail Extensions) types for various file formats.
- **`ShareManager`** — Hands content to the platform share chooser.
- **`UrlLauncher`** — Opens a URL in whatever the platform considers the right handler.
- **`UrlLauncherImpl`** — The one [UrlLauncher], for every target.

Undocumented: `GarbageCollectionManager`, `GarbageCollectionManagerImpl`, `IntentManager`, `IntentManagerImpl`, `ShareManagerImpl`, `UpdateOutcome`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

No tests in this module. If you add behaviour here, add the test alongside it —
`CORE_TESTING.md` carries the shared idiom.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * In-app update check, with real behaviour on every target.
 *
 * ## Why this is one commonMain contract now
 * This used to be a per-target pair: a Play Core implementation in `androidMain` and a twin in
 * `nonAndroidMain` whose two methods were empty bodies. iOS, desktop and web therefore had no
 * update path at all, and no caller could tell — the calls compiled and silently did nothing.
 *
 * [AppUpdateManagerImpl] now delegates to the toolkit's `AppUpdate` engine, which ships real
 * `actual`s for 11 targets, so one implementation is honest on all of them. A target the engine
 * genuinely cannot serve says so through [UpdateOutcome.NotSupported] rather than by doing nothing.
 *
 * ## Why these suspend
 * The check is a network call on every target. The former signature was fire-and-forget, which is
 * exactly what let a no-op twin pass for a working implementation.
 */
interface AppUpdateManager {

    /** Check for an update and, if one is available, start the flow. */
    suspend fun checkForAppUpdate(): UpdateOutcome

    /**
     * Re-check after the app returns to the foreground, so an update the user backgrounded
     * mid-flow is offered again. Call from the host's resume hook.
     */
    suspend fun checkForResumeUpdateState(): UpdateOutcome

    /** Whether this target can perform an in-app update at all. */
    fun isSupported(): Boolean
}
```

Source: [`src/commonMain/kotlin/kpt/core/base/platform/update/AppUpdateManager.kt`](../../../../core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/update/AppUpdateManager.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
