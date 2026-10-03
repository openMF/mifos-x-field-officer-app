# `core/data`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_DATA.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 62 Kotlin files (21 test) · source sets: `androidMain`, `commonMain`, `commonTest`, `nonAndroidMain`



## When implementing a feature

**Tier 5** of the codegen chain, written by **`kmp-client-gen`**.

**Does your feature need this module?** Use when /kmp-implement or /kmp-client needs client networking layer generated.

**Where the code goes**

```
core/data/src/commonMain/kotlin/kpt/core/data/<your-domain>/
```

One package per domain — the template's own `CryptoRepository.kt` sits at `kpt/core/data/crypto/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

Annotate — that is the whole registration. There is no DI file to edit:

- `@RepositoryBinding` → generates `di/GeneratedRepositoryBindings`
- `@DataProvider` → generates `di/GeneratedRepositoryBindings, config/AppOutboxQualifiers`
- `@FromStore` → generates `its binding`
- `@FromQualifier` → generates `its binding`

The aggregates above are **build artifacts**. Authoring one in source produces
duplicate declarations that fail the build, so a feature adds its annotation and
nothing else.

**Next in the chain:** tier 6 (`core/domain`, `core/ui`), then `feature/{f}`.

## Position in the module graph

**Exposes transitively** (`api`) — a consumer of this module also sees these:

- [`core-base/data`](../core-base/data.md)
- [`core/store`](../core/store.md)

**Uses internally** (`implementation`) — not visible to consumers:

`core-base/common`, `core-base/database`, `core-base/datastore`, `core-base/network`, `core-base/store`, `core/common`, `core/database`, `core/datastore`, `core/firebase`, `core/model`, `core/network`

**Consumed by** 15 module(s): `core/domain`, `feature/add-to-watchlist`, `feature/alerts`, `feature/amortization`, `feature/bills`, `feature/calculators`, `feature/cloudtodo`, `feature/crypto`, `feature/home`, `feature/loans`, `feature/macro`, `feature/profile` …and 3 more

## Codegen contracts owned here

These annotations **are** the declaration. Their aggregates are build artifacts;
authoring one in source produces duplicate declarations that fail the build.

| annotation | target | processor | generates |
|---|---|---|---|
| `@RepositoryBinding` | `CLASS` | `data-ksp` | di/GeneratedRepositoryBindings |
| `@DataProvider` | `FUNCTION` | `data-ksp` | di/GeneratedRepositoryBindings, config/AppOutboxQualifiers |
| `@FromStore` | `VALUE_PARAMETER` | `data-ksp` | — |
| `@FromQualifier` | `VALUE_PARAMETER` | `data-ksp` | — |

**`@RepositoryBinding`** — RepositoryModule carries ZERO domain imports by design — that is what lets a template sync blind-copy it, and lets a stripped fork generate fewer bindings because deleting a package takes its annotations with it. A hand-written single<XRepository> breaks both.

**`@FromStore`** — Injects a store by its generated AppStoreIds const. Replaces the retired get(qualifier = qualifier(AppStoreRegistry.X)) hand-wiring shape.

Declared in [`CONTRACT.yaml`](../../CONTRACT.yaml), which is verified equal to the
code in both directions by `framework-verify-architecture-contract.sh`.

## Principal types

- **`AlertsRepository`** — Repository for the Price Alerts feature.
- **`BillReminderRepository`** — User's bill reminders — purely local persistence, no remote sync.
- **`CloudTodoRepository`** — Read + offline-write surface over the cloud-todo [org.mobilenativefoundation.store.store5.MutableStore].
- **`CurrencyRepository`** — Repository surface for exchange rates + historical rate data.
- **`EconomicRatesRepository`** — Repository surface for FRED-sourced interest-rate time series.
- **`EmiCalculatorRepository`** — Read surface for the EMI calculator (`calculator_pure`, MEMORY_ONLY).
- **`LoanRepository`** — User's personal loan portfolio — purely local persistence, no remote sync.
- **`LoanSubmitSyncer`** — Marker wrapper around the Loan syncer.
- **`LogoutEvent`** — Result class to share the [loggedOutUserId] of a user
- **`LogoutReason`** — Indicates the reason that the user is being logged out.
- **`MacroIndicatorsRepository`** — Repository surface for World Bank macro-indicator series.
- **`OutboxQualifiers`** — Named qualifiers for the app's `SubmitOutbox<*>` bindings.
- …and 3 more documented types

Undocumented: `AlertsRepositoryImpl`, `AmortizationCalcRepository`, `AmortizationCalcRepositoryImpl`, `BillReminderRepositoryImpl`, `BillReminderSubmitSyncer`, `CloudTodoRepositoryImpl`, `CryptoRepository`, `CryptoRepositoryImpl`, `CurrencyRepositoryImpl`, `EconomicRatesRepositoryImpl`, `EmiCalculatorRepositoryImpl`, `LoanRepositoryImpl`, `MacroIndicatorsRepositoryImpl`, `ProfileRepository` …and 5 more

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

21 test file(s) under `core/data/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module, showing the `@RepositoryBinding` contract in use.

```kotlin
/**
 * Banking's submit-path wiring, declared where the feature lives.
 *
 * Each form payload gets its OWN outbox so a formKey collision across features is impossible, and
 * each outbox declares a qualifier: Koin matches `single<SubmitOutbox<*>>` by raw class, not full
 * KType, so unqualified outboxes collapse to whichever registered last.
 */
@DataProvider(qualifier = "outbox.loan")
fun provideLoanOutbox(dao: DraftDao): SubmitOutbox<Loan> =
    RoomSubmitOutbox(dao = dao, serializer = Loan.serializer())
```

Source: [`src/commonMain/kotlin/kpt/core/data/banking/BankingDataProviders.kt`](../../../../core/data/src/commonMain/kotlin/kpt/core/data/banking/BankingDataProviders.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
