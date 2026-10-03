# `core/datastore`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_DATASTORE.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 9 Kotlin files (2 test) · source sets: `androidMain`, `commonMain`, `commonTest`



## When implementing a feature

**Tier 2** of the codegen chain, written by **`kmp-datastore-gen`**.

**Does your feature need this module?** Use when /kmp-implement determines a feature is local-only (no api / no remote data-flow) and needs persisted prefs.

**Where the code goes**

```
core/datastore/src/commonMain/kotlin/kpt/core/datastore/<your-domain>/
```

One package per domain — the template's own `AppReviewPromptStore.kt` sits at `kpt/core/datastore/prefs/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

No annotation contract in this module — follow the DI convention already present in
its `di/` module, and prefer extending an existing seam over adding a new one.

**Next in the chain:** tier 3 (`core/network`).

## Position in the module graph

**Uses internally** (`implementation`) — not visible to consumers:

`core-base/common`, `core-base/datastore`, `core/common`, `core/model`

**Consumed by** 2 module(s): `core/data`, `core/network`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_DATASTORE.md`.

## Principal types

- **`AppReviewPromptState`** — What the review policy needs to know at app open, derived from [AppReviewPromptStore].
- **`ProjectPreferencesRepository`** — THE FORK'S preferences. Extends the framework's — this is yours to fill.
- **`ProjectPreferencesRepositoryImpl`** — Fork implementation of [ProjectPreferencesRepository]. `owner: fork` — never synced.
- **`SettingsAppReviewPromptStore`** — [Settings]-backed [AppReviewPromptStore], using the same plain store as user preferences.
- **`UserPreferencesRepository`** — Repository interface for managing user preferences with reactive
- **`UserPreferencesRepositoryImpl`** — Splits user data storage between plain (UI preferences) and secure

Undocumented: `AppReviewPromptStore`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

2 test file(s) under `core/datastore/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * What the review policy needs to know at app open, derived from [AppReviewPromptStore].
 *
 * @property launchCount completed app launches, including this one.
 * @property daysSinceInstall whole days since first launch.
 * @property daysSinceLastPrompt whole days since the last prompt, or `null` if never prompted.
 */
data class AppReviewPromptState(
    val launchCount: Int,
    val daysSinceInstall: Int,
    val daysSinceLastPrompt: Int?,
)

/**
 * Tracks the three counters that gate the automatic review prompt.
 *
 * Deliberately NOT part of [UserPreferencesRepository]: this is DEVICE state, not user state. It
 * must survive sign-out — a user who declined a prompt yesterday should not be asked again today
 * because they logged out in between — whereas `clearUserData()` exists to discard per-user state.
 * Keeping it separate means a fork that later makes `clearUserData()` actually clear the blob
 * cannot silently reset everyone's cooldown.
 *
 * It stores COUNTERS only. Whether those counters justify a prompt is
 * `AppReviewConfig.shouldPromptForReview(...)`, whose thresholds are generated from app-profile —
 * so the policy lives with the config and this stays a dumb ledger.
 */
// datastore-scope: per-device — launch count, install date and last-prompt date gate a
// cooldown that must survive sign-out; a user who declined yesterday must not be asked again
// today merely because they logged out in between. clearUserData() deliberately does NOT
// reach these, which is why they are not fields on UserData.
interface AppReviewPromptStore {

    /**
     * Record an app launch and return the resulting state.
     *
     * Called once per launch from the app shell. On first ever call it stamps the install date, so
     * `daysSinceInstall` is 0 rather than "since the epoch" — without the stamp every fresh install
     * would read as decades old and clear the install-age threshold immediately.
     */
    fun recordLaunch(): AppReviewPromptState

    /**
     * Stamp that a prompt was just requested, starting the cooldown.
     *
     * Recorded on REQUEST, not on a completed review, because neither Play nor StoreKit reports
     * whether the user actually reviewed. Treating "asked" as the cooldown trigger is the only
    // … (excerpt)
```

Source: [`src/commonMain/kotlin/kpt/core/datastore/prefs/AppReviewPromptStore.kt`](../../../../core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/AppReviewPromptStore.kt) — excerpt; read the file for the full implementation.

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
