# `core/model`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_MODEL.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 24 Kotlin files (1 test) · source sets: `androidMain`, `commonMain`, `desktopTest`



## When implementing a feature

**Tier 1** of the codegen chain, written by **`kmp-dto-gen`**.

**Does your feature need this module?** Use when /kmp-implement or /kmp-client needs DTO layer generated.

**Where the code goes**

```
core/model/src/commonMain/kotlin/kpt/core/model/<your-domain>/
```

One package per domain — the template's own `CoinMarket.kt` sits at `kpt/core/model/crypto/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

No annotation contract in this module — follow the DI convention already present in
its `di/` module, and prefer extending an existing seam over adding a new one.

**Next in the chain:** tier 2 (`core/database`, `core/datastore`).

## Position in the module graph

**Uses internally** (`implementation`) — not visible to consumers:

`core/common`

**Consumed by** 21 module(s): `core/data`, `core/database`, `core/datastore`, `core/domain`, `core/network`, `core/store`, `core/ui`, `feature/add-to-watchlist`, `feature/alerts`, `feature/amortization`, `feature/bills`, `feature/calculators` …and 9 more

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_MODEL.md`.

## Principal types

- **`AmortizationBreakdown`** — One amortization calculation: the per-installment [rows] and the [summary] totals.
- **`AmortizationRow`** — A single monthly row in a reducing-balance amortization schedule.
- **`AuthState`** — Models high level auth state for the application.
- **`CloudTodo`** — A cloud-synced todo — the toolkit's MUTABLE (offline-write) Store5 archetype showcase.
- **`Country`** — Country reference for the Banking Utility Toolkit's macro-indicator screens.
- **`IndicatorKind`** — Macro indicators surfaced by the toolkit. Each kind maps to a stable World Bank
- **`IndicatorObservation`** — Single year-level macro observation.
- **`InterestRateSeries`** — Domain representation of an interest-rate time series sourced from FRED
- **`LanguageConfig`** — Every language the app can be switched to, in the user's OWN language.
- **`MacroIndicator`** — Domain representation of a country-level macro indicator sourced from the
- **`ProfileInfo`** — What the profile screen displays.
- **`RateObservation`** — Single observation in an interest-rate time series.
- …and 1 more documented types

Undocumented: `AlertDirection`, `BillCategory`, `BillReminder`, `CoinDetail`, `CoinMarket`, `CountryFlagUtils`, `DarkThemeConfig`, `EmiResult`, `ExchangeRates`, `Loan`, `LoanCalcScenario`, `LoanKind`, `PriceAlert`, `RateHistory` …and 6 more

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

1 test file(s) under `core/model/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Domain representation of a country-level macro indicator sourced from the
 * World Bank Open Data API.
 *
 * The default consumer is the Banking Utility Toolkit's "B8 Country Macro Snapshot"
 * screen. Country identifiers ([countryCode]) use ISO 3166-1 alpha-2 codes (e.g.
 * `US`, `IN`, `DE`). The World Bank also accepts alpha-3 (`USA`, `IND`, `DEU`)
 * — both are forwarded through unchanged.
 *
 * @property countryCode ISO 3166-1 alpha-2 country code, e.g. `"US"`.
 * @property countryName Human-readable country name from the World Bank metadata.
 * @property indicator Which macro series this row represents — see [IndicatorKind].
 * @property observations Per-year observations, ordered ascending by [IndicatorObservation.year].
 *   Empty when the World Bank has no published data for the requested span.
 * @property source Upstream data source — currently always `"World Bank Open Data"`.
 */
data class MacroIndicator(
    val countryCode: String,
    val countryName: String,
    val indicator: IndicatorKind,
    val observations: List<IndicatorObservation>,
    val source: String = "World Bank Open Data",
)

/**
 * Single year-level macro observation.
 *
 * @property year 4-digit calendar year (UTC). World Bank publishes annual data only.
 * @property value Observed value at [year], or `null` when the World Bank reports
 *   `null` (data sparsity is common — many countries don't report every indicator
 *   every year). UI should render missing values as "—" not "0".
 */
data class IndicatorObservation(
    val year: Int,
    val value: Double?,
)
```

Source: [`src/commonMain/kotlin/kpt/core/model/economic/MacroIndicator.kt`](../../../../core/model/src/commonMain/kotlin/kpt/core/model/economic/MacroIndicator.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
