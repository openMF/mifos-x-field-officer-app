# `core/firebase`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_FIREBASE.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 11 Kotlin files (0 test) · source sets: `androidMain`, `commonMain`



## When implementing a feature

**Tier 0** of the codegen chain, written by **`kmp-firebase-gen`**.

**Does your feature need this module?** Use when /kmp-client (the manager) determines a feature needs its own tracking vocabulary.

**Where the code goes**

```
core/firebase/src/commonMain/kotlin/kpt/core/firebase/<your-domain>/
```

One package per domain — the template's own `LoansAnalyticsEvents.kt` sits at `kpt/core/firebase/loans/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

No annotation contract in this module — follow the DI convention already present in
its `di/` module, and prefer extending an existing seam over adding a new one.

**Next in the chain:** tier 1 (`core/model`).

## Position in the module graph

**Exposes transitively** (`api`) — a consumer of this module also sees these:

- [`core-base/firebase`](../core-base/firebase.md)

**Consumed by** 3 module(s): `core/data`, `core/ui`, `feature/settings`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_FIREBASE.md`.

## Principal types

- **`KptAnalyticsTracker`** — CROSS-CUTTING analytics tracker — TEMPLATE-OWNED, full-copied by every sync.
- **`KptCrashKeys`** — CROSS-CUTTING crash context — TEMPLATE-OWNED, full-copied by every sync.
- **`KptEventTypes`** — CROSS-CUTTING analytics event keys — TEMPLATE-OWNED, full-copied by every sync.
- **`KptParamKeys`** — CROSS-CUTTING parameter keys — TEMPLATE-OWNED, full-copied by every sync.
- **`KptParamValues`** — CROSS-CUTTING parameter values — TEMPLATE-OWNED, full-copied by every sync.
- **`LoansAnalyticsTracker`** — `loans` feature tracker — DEMO-SHOWCASE. It is the template's worked example of the per-feature
- **`LoansCrashKeys`** — `loans` crash context — DEMO-SHOWCASE, deleted by `--clean` with the loans feature.
- **`LoansEventTypes`** — `loans` feature analytics keys — DEMO-SHOWCASE, deleted by `--clean` with the loans feature.
- **`LoansParamKeys`** — `loans` parameter keys.
- **`LoansParamValues`** — `loans` parameter values.

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
 * `loans` [CrashReporter] breadcrumbs — DEMO-SHOWCASE, deleted by `--clean` with the loans feature.
 * The same file in a fork's own feature package is fork-owned.
 *
 * ## Banding is not optional here
 * A crash report is more exposed than an analytics event: it carries a stack trace, and console
 * access is usually broader than analytics access. So this file reuses [LoansParamValues] bands from
 * the analytics package rather than defining its own — one vocabulary, one place to audit, and no
 * chance of the crash path leaking a precision the analytics path deliberately dropped.
 */

/**
 * The loan being edited or viewed when a crash occurs.
 *
 * [principal] is banded on the way in — the caller passes the real figure and this function decides
 * what reaches the report, exactly as `LoansAnalyticsTracker` does.
 */
fun CrashReporter.setLoanContext(
    kind: String,
    principal: Double,
    tenureMonths: Int,
) {
    setCustomKey(LoansCrashKeys.LOAN_KIND, kind)
    setCustomKey(LoansCrashKeys.PRINCIPAL_BAND, principalBand(principal))
    setCustomKey(LoansCrashKeys.TENURE_MONTHS, tenureMonths.toString())
    log("loans -> $kind / ${tenureMonths}mo")
}
```

Source: [`src/commonMain/kotlin/kpt/core/firebase/loans/LoansCrashExtensions.kt`](../../../../core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansCrashExtensions.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
