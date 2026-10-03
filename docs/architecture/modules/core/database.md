# `core/database`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_DATABASE.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 51 Kotlin files (13 test) · source sets: `androidMain`, `androidUnitTest`, `commonMain`, `commonTest`, `desktopMain`, `desktopTest`, `jsMain`, `jsTest`, `nativeMain`, `nativeTest`, `wasmJsMain`, `wasmJsTest`



## When implementing a feature

**Tier 2** of the codegen chain, written by **`kmp-client-gen`**.

**Does your feature need this module?** Use when /kmp-implement or /kmp-client needs client networking layer generated.

**Where the code goes**

```
core/database/src/commonMain/kotlin/kpt/core/database/<your-domain>/
```

One package per domain — the template's own `FintechTypeConverters.kt` sits at `kpt/core/database/crypto/converter/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

Annotate — that is the whole registration. There is no DI file to edit:

- `@DbEntity` → generates `config/AppDatabase`
- `@DbDao` → generates `di/GeneratedDaoBindings`
- `@DbConverters` → generates `di/GeneratedConverterBindings`

The aggregates above are **build artifacts**. Authoring one in source produces
duplicate declarations that fail the build, so a feature adds its annotation and
nothing else.

**Next in the chain:** tier 3 (`core/network`).

## Position in the module graph

**Exposes transitively** (`api`) — a consumer of this module also sees these:

- [`core-base/database`](../core-base/database.md)
- [`core/common`](../core/common.md)

**Uses internally** (`implementation`) — not visible to consumers:

`core-base/crypto`, `core/model`

**Consumed by** 2 module(s): `core/data`, `core/store`

## Codegen contracts owned here

These annotations **are** the declaration. Their aggregates are build artifacts;
authoring one in source produces duplicate declarations that fail the build.

| annotation | target | processor | generates |
|---|---|---|---|
| `@DbEntity` | `CLASS` | `database-ksp` | config/AppDatabase |
| `@DbDao` | `CLASS` | `database-ksp` | di/GeneratedDaoBindings |
| `@DbConverters` | `CLASS` | `database-ksp` | di/GeneratedConverterBindings |

**`@DbEntity`** — The @Database entities list is built ONLY from annotated types. An entity with @Entity and no @DbEntity is absent from the generated database: it compiles, ships, and throws at first query. No build error, no file diff — the most expensive omission in the template.

Declared in [`CONTRACT.yaml`](../../CONTRACT.yaml), which is verified equal to the
code in both directions by `framework-verify-architecture-contract.sh`.

## Principal types

- **`ForkDatabaseConfig`** — The fork's Room schema version. `owner: fork` — PRESERVED across `/kmp-project-template-sync`.

Undocumented: `AlertDao`, `AlertEntity`, `BankingTypeConverters`, `BillReminderDao`, `BillReminderEntity`, `ChargeTypeConverters`, `CloudTodoDao`, `CloudTodoEntity`, `CoinDetailDao`, `CoinDetailEntity`, `CoinMarketDao`, `CoinMarketEntity`, `DatabaseConfig`, `ExchangeRatesDao` …and 12 more

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

13 test file(s) under `core/database/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module, showing the `@DbEntity` contract in use.

```kotlin
/**
 * Data-access object for the `interest_rate_series` table.
 *
 * All reads return reactive [Flow]s; all writes are `suspend` one-shots.
 * Default sort order within each series is newest-first ([InterestRateSeriesEntity.date]
 * descending) so the most recent observation is always the first element.
 */
@DbDao
@Dao
interface InterestRateSeriesDao {

    /**
     * Observe all data-points for a given [seriesId], ordered newest-first.
     *
     * @param seriesId FRED series identifier (e.g. "FEDFUNDS").
     */
    @Query("SELECT * FROM interest_rate_series WHERE seriesId = :seriesId ORDER BY date DESC")
    fun observeBySeriesId(seriesId: String): Flow<List<InterestRateSeriesEntity>>

    /** Observe all cached data-points across all series, ordered newest-first. */
    @Query("SELECT * FROM interest_rate_series ORDER BY date DESC")
    fun observeAll(): Flow<List<InterestRateSeriesEntity>>

    /** Insert or replace a batch of data-points (used during cache refresh). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<InterestRateSeriesEntity>)

    /** Delete all cached data-points for a given series. */
    @Query("DELETE FROM interest_rate_series WHERE seriesId = :seriesId")
    suspend fun deleteBySeriesId(seriesId: String)

    /** Insert or replace a single data-point. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: InterestRateSeriesEntity)

    /** Delete all cached data-points across all series. */
    @Query("DELETE FROM interest_rate_series")
    suspend fun deleteAll()
}
```

Source: [`src/commonMain/kotlin/kpt/core/database/economic/InterestRateSeriesDao.kt`](../../../../core/database/src/commonMain/kotlin/kpt/core/database/economic/InterestRateSeriesDao.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
