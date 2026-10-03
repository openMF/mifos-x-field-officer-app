# `core/store`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_STORE.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 35 Kotlin files (6 test) · source sets: `commonMain`, `commonTest`



## When implementing a feature

**Tier 4** of the codegen chain, written by **`kmp-store-gen`**.

**Does your feature need this module?** Use when /kmp-implement needs the Store5 keystone layer (StoreFactory.createStore / createMutableStore, declared by @StoreProvider + @CacheKey — store-ksp generates the registry, ids, cache keys, Koin binding and logout register; no registry or DI edit).

**Where the code goes**

```
core/store/src/commonMain/kotlin/kpt/core/store/<your-domain>/
```

One package per domain — the template's own `UserDataStore.kt` sits at `kpt/core/store/prefs/impl/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

Annotate — that is the whole registration. There is no DI file to edit:

- `@StoreProvider` → generates `config/AppStoreRegistry, config/AppStoreIds, di/GeneratedStoreBindings`
- `@CacheKey` → generates `config/AppCacheKeys`

The aggregates above are **build artifacts**. Authoring one in source produces
duplicate declarations that fail the build, so a feature adds its annotation and
nothing else.

**Next in the chain:** tier 5 (`core/data`, `core/designsystem`).

## Position in the module graph

**Exposes transitively** (`api`) — a consumer of this module also sees these:

- [`core-base/store`](../core-base/store.md)
- [`core-base/ui`](../core-base/ui.md)

**Uses internally** (`implementation`) — not visible to consumers:

`core-base/database`, `core/database`, `core/model`, `core/network`

**Consumed by** 17 module(s): `core/data`, `core/designsystem`, `feature/add-to-watchlist`, `feature/alerts`, `feature/amortization`, `feature/bills`, `feature/calculators`, `feature/cloudtodo`, `feature/crypto`, `feature/home`, `feature/loans`, `feature/macro` …and 5 more

## Codegen contracts owned here

These annotations **are** the declaration. Their aggregates are build artifacts;
authoring one in source produces duplicate declarations that fail the build.

| annotation | target | processor | generates |
|---|---|---|---|
| `@StoreProvider` | `FUNCTION` | `store-ksp` | config/AppStoreRegistry, config/AppStoreIds, di/GeneratedStoreBindings |
| `@CacheKey` | `FUNCTION` | `store-ksp` | config/AppCacheKeys |

**`@StoreProvider`** — Declares qualifier, TTL and the logout purge in one place. `logout` drives BOTH the binding and the purge, so they cannot disagree; `logout = false` only for a MutableStore, which Store5 5.1 cannot register.

**`@CacheKey`** — Keys nest per store because roles (LIST, item) repeat across stores and flat would collide. A duplicate key STRING is a build error: two streams sharing a key share a fetched-at stamp.

Declared in [`CONTRACT.yaml`](../../CONTRACT.yaml), which is verified equal to the
code in both directions by `framework-verify-architecture-contract.sh`.

## Principal types

- **`AmortizationCalcParams`** — Store key for one amortization calculation — the calculator's inputs.
- **`AmortizationCompute`** — The compute PORT for the amortization store.
- **`CloudTodoConflictResolver`** — The named conflict surface for the cloud-todo MUTABLE archetype (S5-CONFLICT).
- **`CloudTodoSyncOrchestrator`** — Drains the cloud-todo write backlog when connectivity returns (S5-SYNC).
- **`EmiCompute`** — The compute PORT for the EMI store.
- **`EmiParams`** — The Store key for a single EMI computation — the calculator's inputs.
- **`ErrorMessageOverrides`** — The fork's error-copy extension point, declared HERE so it is TEMPLATE-owned and full-copied by
- **`InterestRateSeriesKey`** — Composite key identifying a single FRED series request. FRED's
- **`MacroIndicatorKey`** — Composite key identifying a single World Bank macro-indicator request.
- **`ProfileInfoSource`** — The read PORT for the profile store.
- **`ProjectErrorMapper`** — THE FORK'S domain-error copy. Neutral on the template — this is yours to fill.
- **`ProjectScreenStateDefaults`** — THE FORK'S branding for the shared empty / error / no-network / loading visuals. Neutral on the
- …and 2 more documented types

Undocumented: `CloudTodoKey`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

6 test file(s) under `core/store/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module, showing the `@StoreProvider` contract in use.

```kotlin
/**
 * MEMORY_ONLY Store5 store over a pure computation (`feature_profile.combo_id: calculator_pure`).
 *
 * There is no network and no source of truth, so the Store contributes exactly one thing:
 * an in-memory cache keyed on [EmiParams]. Re-entering a parameter set the user already tried
 * — the common case while dragging a tenure slider back and forth — is served from cache
 * instead of recomputed, and the result reaches the screen as a `ScreenState` like every other
 * read surface, so the calculator renders through the same `ScreenContent` wrapper as the rest
 * of the app rather than a bespoke nullable `StateFlow`.
 *
 * MEMORY_ONLY is only legal because no `cache_strategy` is declared for this feature (SC2).
 */
@StoreProvider(id = "emi")
@CacheKey(
    fn = "of",
    key = "emi:{principal}:{ratePercent}:{tenureMonths}",
    params = ["principal:Double", "ratePercent:Double", "tenureMonths:Int"],
)
fun provideEmiStore(compute: EmiCompute): Store<EmiParams, EmiResult> =
    StoreFactory.createMemoryStore(
        fetcher = Fetcher.of { params: EmiParams -> compute(params) },
    )
```

Source: [`src/commonMain/kotlin/kpt/core/store/emi/impl/EmiStore.kt`](../../../../core/store/src/commonMain/kotlin/kpt/core/store/emi/impl/EmiStore.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
