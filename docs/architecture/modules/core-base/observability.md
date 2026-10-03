# `core-base/observability`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_OBSERVABILITY.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 4 Kotlin files (1 test) · source sets: `androidMain`, `commonMain`, `commonTest`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

No module dependencies — this is a leaf.

**Consumed by** no other module in the template — it is either an app-level entry
or currently unused by the shipped demos.

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_OBSERVABILITY.md`.

## Principal types

- **`ConsoleCrashReporter`** — Default [CrashReporter] binding that writes events to stdout.
- **`CrashReporter`** — Fork-customization seam for crash + non-fatal-error reporting.
- **`CrashSeverity`** — Severity vocabulary for [CrashReporter.recordMessage].

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

1 test file(s) under `core-base/observability/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Fork-customization seam for crash + non-fatal-error reporting.
 *
 * The toolkit ships [ConsoleCrashReporter] which writes everything to stdout — sufficient
 * for local development but not for shipped builds. Forks override the
 * [kpt.core.base.observability.di.observabilityModule] binding in their app module
 * with a real implementation (Firebase Crashlytics, Sentry, Bugsnag, Datadog RUM, etc.).
 *
 * ## Why a separate seam from the cmp-firebase `AnalyticsHelper`?
 *
 * Crash reporting and analytics serve different audiences:
 * - **Analytics** is high-volume, sampled, event-stream (button clicks, screen views) routed
 *   to product owners.
 * - **Crash reporting** is low-volume, exception-detail-heavy, stack-trace-bearing data
 *   routed to engineering.
 *
 * Most production stacks (Firebase Analytics + Firebase Crashlytics, Mixpanel + Sentry)
 * keep them in separate SDKs with separate ingestion pipelines and different retention
 * policies. Modeling them as separate seams matches the wire reality and lets each fork
 * choose providers independently.
 *
 * ## Contract notes
 *
 * - All methods **must** be safe to call from any thread / coroutine. No suspending APIs —
 *   crash reporters typically queue + flush asynchronously.
 * - [setUser] accepts a nullable userId for sign-out flows; pass `null` to clear.
 * - [recordException] is for **caught** exceptions (e.g. retry-loop exhaustion); uncaught
 *   exceptions are reported by the platform-level handler the fork installs.
 * - [isConfigured] is `false` for the no-op default; UI / startup checks can decide whether
 *   to surface "send crash logs?" prompts based on this.
 *
 * @see ConsoleCrashReporter Default development-only implementation
 */
interface CrashReporter {

    /**
     * Record a caught exception with optional context message. Use for:
     * - Retry-loop exhaustion (e.g. [OfflineSubmitSyncer] failing after N attempts)
     * - Recovered errors that nonetheless deserve engineering visibility
     * - Logic violations that didn't crash but indicate a bug
     *
     * @param throwable The exception (including its stack trace)
     * @param message Optional human-readable label (e.g. `"OfflineSubmitSyncer.retryAll: gave up after 5 retries"`)
     */
    fun recordException(throwable: Throwable, message: String? = null)
    // … (excerpt)
```

Source: [`src/commonMain/kotlin/kpt/core/base/observability/CrashReporter.kt`](../../../../core-base/observability/src/commonMain/kotlin/kpt/core/base/observability/CrashReporter.kt) — excerpt; read the file for the full implementation.

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
