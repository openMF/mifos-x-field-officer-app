# `core-base/store` — Framework-Shared State Infrastructure

**End-to-end guide: [`docs/architecture/cross-cutting/store-architecture.md`](../../docs/architecture/cross-cutting/store-architecture.md)**

Framework-shared — do not edit in a fork. Push fork pressure to `core/store`; a genuine fix here goes
upstream to `openMF/kmp-project-template`.

## What lives here

| Area | Symbols |
|---|---|
| Store construction | `StoreFactory` — `createStore`, `createMemoryStore`, `createOfflineStore`, `createOfflineMutableStore`, `createMutableStore`, `createScreenWithMutation` |
| Read path | `asScreenStream`, `ScreenStreamContext`, `asLoadOnceStream`, `PagingScreenStream`, `FetchPolicy`, `DecisionEngine` |
| Write path | `MutationGateway`, `MutationPolicy`, `MutationResult`, `CommandSpec`, `ConflictInbox`, `DeleteSync` |
| Room-backed defaults | `RoomBookkeeper`, `RoomFetchedAtRepository`, `RoomConflictInbox`, `StoreCacheManagerImpl` |

Call sequence + which primitive backs which archetype: [`CONSUMPTION.md`](./CONSUMPTION.md).
Read-path internals (`StoreData<T>`, paging, submit outbox):
[`docs/architecture/cross-cutting/store-data-api.md`](../../docs/architecture/cross-cutting/store-data-api.md).
