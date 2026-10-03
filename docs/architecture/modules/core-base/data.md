# `core-base/data`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_DATA.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 9 Kotlin files (0 test) · source sets: `androidMain`, `commonMain`, `nonAndroidMain`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

**Uses internally** (`implementation`) — not visible to consumers:

`core-base/common`, `core-base/datastore`

**Consumed by** 1 module(s): `core/data`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_DATA.md`.

## Principal types

- **`NetworkChange`** — Identified network record. Used by [changeListSync] to partition deletes
- **`NetworkMonitorContract`** — Framework contract for [NetworkMonitor] implementations. The bundled
- **`NetworkMonitorImpl`** — Singleton NetworkMonitor backed by cmp-network-monitor.
- **`SyncManager`** — Observer surface for the in-flight sync state. NiA-port.
- **`Syncable`** — Adopter contract. A [Syncable] knows how to bring its slice of local state
- **`Synchronizer`** — Synchronization contract — ports Now in Android's `core/data/SyncUtilities.kt`.
- **`TimeZoneMonitor`** — Utility for reporting current timezone the device has set.

Undocumented: `TimeZoneMonitorImpl`

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
 * Synchronization contract — ports Now in Android's `core/data/SyncUtilities.kt`.
 *
 * A [Synchronizer] bridges in-memory [ChangeListVersions] state with the
 * persisted store (see `SyncStatePersister`), so [Syncable] adopters can
 * read/write per-feature last-synced versions through a single seam.
 *
 * Adopters call the [Syncable.sync] extension which delegates to
 * [Syncable.syncWith], passing the [Synchronizer] as `this`. Inside `syncWith`
 * an adopter typically calls either [changeListSync] (delta APIs — server tells
 * you what changed) or [snapshotSync] (snapshot APIs — server returns the full
 * canonical set on every call).
 */
interface Synchronizer {
    suspend fun getChangeListVersions(): ChangeListVersions

    suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions)

    /** Convenience: call `someSyncable.sync()` to run [Syncable.syncWith] against this. */
    suspend fun Syncable.sync(): Boolean = this.syncWith(this@Synchronizer)
}
```

Source: [`src/commonMain/kotlin/kpt/core/base/data/infra/Synchronizer.kt`](../../../../core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/Synchronizer.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
