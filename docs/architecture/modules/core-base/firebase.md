# `core-base/firebase`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_FIREBASE.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 1 Kotlin files (0 test) · source sets: `commonMain`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

No module dependencies — this is a leaf.

**Consumed by** 1 module(s): `core/firebase`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_FIREBASE.md`.

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
 * Base Firebase integration (the "main implementation", template-owned in core-base/firebase).
 *
 * Binds the published `cmp-firebase` engine's **real Firebase-backed** [AnalyticsHelper] (GitLive on
 * supported targets, Measurement-Protocol/NoOp fallback elsewhere) + its [PerformanceTracker]. This
 * is the production wiring: a fork includes [firebaseModule] to turn analytics ON.
 *
 * The project layer (`core/firebase`) supplies the toolkit **default** (NoOp — privacy-respecting
 * demo build) plus the app-owned domain event catalog; last-binding-wins lets a fork swap in a
 * different provider. Crashlytics wiring will join this module as core-base/firebase grows into the
 * single host for all Firebase services.
 */
val firebaseModule: Module = module {
    single<AnalyticsHelper> {
        AnalyticsModule.analyticsHelper(
            mode = AnalyticsModule.Mode.Firebase,
            config = AnalyticsConfig(),
        )
    }
    single { AnalyticsModule.performanceTracker(get()) }
}
```

Source: [`src/commonMain/kotlin/kpt/core/base/firebase/di/FirebaseModule.kt`](../../../../core-base/firebase/src/commonMain/kotlin/kpt/core/base/firebase/di/FirebaseModule.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
