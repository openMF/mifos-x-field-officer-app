# `core/store` — Consumer Customization Seam

**End-to-end guide: [`docs/architecture/cross-cutting/store-architecture.md`](../../docs/architecture/cross-cutting/store-architecture.md)**
— store type catalogue (archetype → factory → FetchPolicy), read path, write path, lifecycle.

This is the consumer customization seam: edit freely in a fork.

## What you get for free

- **Store5 factories keyed by `store_archetype`** — declare a feature's `store_archetype` and codegen
  emits the matching `StoreFactory.create*` factory + `FetchPolicy`. Full archetype → factory →
  FetchPolicy mapping: [STORE_ARCHITECTURE.md §2](../../docs/architecture/cross-cutting/store-architecture.md#2-store-type-catalogue).
- **`AppScreenStateDefaults`** — brand your loading / empty / error / no-network visuals in one place.
- **`AppErrorMapper`** — domain-error → user-message mapping (`categorize()`).
- **`@StoreProvider` / `@CacheKey`** on each `provide*Store` — the ONLY store declaration. Generates
  `AppStoreRegistry` (qualifiers + `Ttl`), `AppCacheKeys`, and the binding. Deps come from the signature.
- **`AppCacheKeys`** — the single source of truth for every `asScreenStream` cacheKey; constants for
  whole-list streams, typed builders (`AppCacheKeys.MyThing.item(id)`) for per-key streams. Never inline a
  cacheKey string at a call site.
- **`MutationGateway`** — the single write door. A repository never calls a DAO write directly; see
  [STORE_ARCHITECTURE.md §4](../../docs/architecture/cross-cutting/store-architecture.md#4-write-path--store-as-the-single-write-sot).
- **`appStoreModule`** — framework wiring only; it `includes(GeneratedStoreBindings)`.
- **Write side** — the unified `BaseMutationViewModel` + `MutationMode` (`InSession` / `Draft`) idiom
  for every mutation screen, composed with `SubmitHandler` / `DraftSubmitHandler`.

See the archetype decision matrix + module chain in `FEATURE_AUTHORING.md` and
[`docs/architecture/cross-cutting/store-data-api.md`](../../docs/architecture/cross-cutting/store-data-api.md).
