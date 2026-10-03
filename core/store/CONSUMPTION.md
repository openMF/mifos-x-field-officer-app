# Consuming `core/store` in a feature

> The Store5 keystone. Every read/write of app data goes through a `Store` built by
> `StoreFactory` (from `core-base/store`) and registered here. This module owns the app's
> store factories (`provide*Store`) annotated with `@StoreProvider`; qualifiers, cache keys and DI are generated.
>
> The archetype → factory → FetchPolicy catalogue and the write path are in
> [`docs/architecture/cross-cutting/store-architecture.md`](../../docs/architecture/cross-cutting/store-architecture.md) — this
> contract is the streamlined "what to call" summary. (Generator-side training guides live in the
> framework repo, not in this repository.)

## Call sequence

1. **Pick the archetype** (drives the factory — see FEATURE_AUTHORING.md):
   - `NETWORK_WITH_CACHE` → `StoreFactory.createStore(fetcher, sourceOfTruth)` — remote `Fetcher` +
     Room `SourceOfTruth` (canonical: `provideCoinMarketsStore`, `provideExchangeRatesStore`).
   - `MUTABLE` (offline write) → `StoreFactory.createMutableStore(fetcher, sourceOfTruth, updater, bookkeeper)`
     — the `Updater` runs the remote write, the `Bookkeeper` records failed offline writes for retry
     (canonical: `provideCloudTodoStore`).
   - `OFFLINE_LOCAL_ONLY` → `StoreFactory.createOfflineStore(sourceOfTruth)` — Room-only, no fetcher
     (canonical: `provideLoansStore`).
   - `MEMORY_ONLY` → `StoreFactory.createMemoryStore(fetcher)` — no SoT, in-memory cache
     (canonical: `provideMacroIndicatorStore`).
2. **Author `provide<Name>Store(...)`** returning `Store<Key, DomainOut>`. The entity→domain map
   lives INSIDE the `SourceOfTruth.reader` (the read-path contract) — the store emits the DOMAIN model.
3. **Annotate the provider** — `@StoreProvider(id = "yourStore")`. The qualifier, the Koin binding and
   the logout purge are generated; there is nothing to register by hand.
   Also register in the logout-clear list so sign-out purges it.
4. **`core/data` consumes the store**, not the feature — the repository turns it into a
   `ScreenDataStream` via `store.asScreenStream(...)` (see core/data/CONSUMPTION.md).

## Notes

- `StoreCacheManager` centralises TTL / invalidation; a store's TTL is `@StoreProvider(ttl = "5m")`.
- `AppErrorMapper` + `AppScreenStateDefaults` map Store5 errors → the `ScreenState` a screen renders.

Canonical example: feature/crypto (createStore), feature/loans (createOfflineStore), the cloud-todo showcase (createMutableStore).

Symbols: StoreProvider, CacheKey, StoreModule, provideCoinMarketsStore, provideExchangeRatesStore, provideLoansStore, provideCloudTodoStore, provideMacroIndicatorStore, StoreCacheManager, AppErrorMapper, AppScreenStateDefaults
