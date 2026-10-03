# `core-base/store`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_STORE.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 108 Kotlin files (50 test) · source sets: `commonMain`, `commonTest`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

No module dependencies — this is a leaf.

**Consumed by** 4 module(s): `core-base/ui`, `core/data`, `core/store`, `core/ui`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_STORE.md`.

## Principal types

- **`BatchResult`** — Aggregate outcome of [BatchSubmitHandler.submitBatch].
- **`BatchSubmitHandler`** — Orchestrator for submitting a list of payloads via a single per-payload [submitBlock].
- **`BatchSubmitMode`** — Semantics for [BatchSubmitHandler.submitBatch] when one or more payloads fail.
- **`CombinedState`** — Snapshot pairing the read-side [ScreenState] with the write-side [SubmitState] —
- **`CommandSpec`** — Describes a command / RPC mutation for [MutationGateway.command].
- **`ConflictInbox`** — Durable inbox of write conflicts surfaced to the user in Settings.
- **`ConflictReport`** — A conflict the caller detected between its local payload and the server result — returned by a
- **`ConflictStrategy`** — Policy for reconciling a server-side value with a local-side value when both
- **`DataOrigin`** — Indicates where a [StoreData] emission originated.
- **`DecisionEngine`** — Pure function combining StoreData metadata + NetworkStatus into ScreenState.
- **`DefaultMutationGateway`** — The default [MutationGateway] — composes the existing Store5 write machinery.
- **`DefaultValidator`** — A TTL-based [Validator] that marks cached data as stale after a given duration.
- …and 39 more documented types

Undocumented: `BlockReason`, `ConflictEntry`, `ConflictResolution`, `FetchedAtRepository`, `FreshnessBands`, `FreshnessSignal`, `PagingScreenStream`, `PushStoreAdapter`, `RoomFetchedAtRepository`, `ScreenDataStream`, `StoreCacheManagerImpl`, `StoreData`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

50 test file(s) under `core-base/store/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Framework-shared, **cross-form** view over every draft the app is holding.
 *
 * [SubmitOutbox] is generic in a single payload type `P` (one instance per form). The
 * template-level **Sync & Drafts** screen (Settings) instead needs an *untyped* live feed of
 * EVERY non-terminal draft across ALL forms — a user-facing preview of what is pending sync,
 * retrying, or failed — plus the three cross-form actions that don't need the payload type:
 * discard a row, re-queue a failed row for sync, and the manual prune.
 *
 * This is the seam feature modules consume (feature UIs never touch [DraftDao] directly). It is
 * framework-owned (`core-base/store`) so it upgrades cleanly across template versions; forks get
 * the Sync & Drafts surface for free and never re-implement it.
 *
 * Backed by [kpt.core.base.database.infra.dao.DraftDao]; wired as a Koin `single` in the app's
 * store module (next to [StoreCacheManager]).
 */
interface DraftInventory {

    /**
     * Live cross-form feed of every non-terminal draft (PENDING / RETRYING / FAILED),
     * newest-first by last-update. Terminal SUBMITTED rows are excluded — they are not
     * actionable and are pruned by [StoreCacheManager.pruneExpiredDrafts].
     */
    fun observeAll(): Flow<List<DraftRecord>>

    /**
     * Permanently discards a single draft by [id] (the per-row Discard). Irreversible —
     * the caller confirms intent in the UI.
     */
    suspend fun discard(id: Long)

    /**
     * Re-queues a draft by [id] for sync: transitions it back to PENDING and clears its error,
     * so the per-form [kpt.core.base.store.submit.OfflineSubmitSyncer] re-attempts it on the next
     * online transition. Typically used on a FAILED row (the per-row Retry).
     */
    suspend fun retry(id: Long)

    /**
     * Manual counterpart to the app-start [StoreCacheManager.pruneExpiredDrafts] — deletes
     * SUBMITTED/FAILED rows older than [StoreCacheManager.DEFAULT_DRAFT_TTL_MS]. PENDING drafts
     * are never pruned. Surfaced as the Sync & Drafts screen's "Prune expired" action so the
     * user can reclaim space on demand without waiting for the next cold start.
     */
    suspend fun pruneExpired()
}
```

Source: [`src/commonMain/kotlin/kpt/core/base/store/infra/DraftInventory.kt`](../../../../core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/DraftInventory.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
