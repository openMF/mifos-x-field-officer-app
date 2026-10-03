# `core/common`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_COMMON.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 3 Kotlin files (0 test) · source sets: `androidMain`, `commonMain`



## When implementing a feature

**Tier 0** of the codegen chain, written by **`kmp-common-gen`**.

**Does your feature need this module?** Use when /kmp-client (the manager) needs a shared utility that lives strictly below every core/* consumer — no Compose, no Ktor, no Room, no platform imports.

**Where the code goes**

```
core/common/src/commonMain/kotlin/kpt/core/common/<your-domain>/
```

One package per domain — the template's own `FormatDate.kt` sits at `kpt/core/common/format/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

No annotation contract in this module — follow the DI convention already present in
its `di/` module, and prefer extending an existing seam over adding a new one.

**Next in the chain:** tier 1 (`core/model`).

## Position in the module graph

**Exposes transitively** (`api`) — a consumer of this module also sees these:

- [`core-base/common`](../core-base/common.md)

**Consumed by** 19 module(s): `core/data`, `core/database`, `core/datastore`, `core/domain`, `core/model`, `core/network`, `core/ui`, `feature/add-to-watchlist`, `feature/alerts`, `feature/amortization`, `feature/bills`, `feature/calculators` …and 7 more

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_COMMON.md`.

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
 * Returns a human-readable "X ago" label for a past [instant], e.g. "just now", "5m ago",
 * "2h ago", "3d ago". Returns `null` when [instant] is null.
 */
@OptIn(ExperimentalTime::class)
fun formatTimeAgo(instant: Instant?): String? {
    instant ?: return null
    val seconds = (Clock.System.now() - instant).inWholeSeconds
    return when {
        seconds < 60 -> "just now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        else -> "${seconds / 86400}d ago"
    }
}
```

Source: [`src/commonMain/kotlin/kpt/core/common/format/FormatDuration.kt`](../../../../core/common/src/commonMain/kotlin/kpt/core/common/format/FormatDuration.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
