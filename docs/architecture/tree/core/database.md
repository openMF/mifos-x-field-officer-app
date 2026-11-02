# `core/database`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATABASE.md`
> **Measured:** 54 Kotlin files, 11 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@DbEntity` | `database-ksp` | config/AppDatabase |
| `@DbDao` | `database-ksp` | di/GeneratedDaoBindings |
| `@DbConverters` | `database-ksp` | di/GeneratedConverterBindings |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`AlertDao`, `AlertEntity`, `BankingTypeConverters`, `BillReminderDao`, `BillReminderEntity`, `ChargeTypeConverters`, `CloudTodoDao`, `CloudTodoEntity`, `CoinDetailDao`, `CoinDetailEntity`, `CoinMarketDao`, `CoinMarketEntity`, `DatabaseConfig`, `ExchangeRatesDao`  …and 13 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/database sha=689148d1787588dfab291a843f7e2a06087345c2 -->
## API reference

_Generated from `core/database` at tree `689148d17875` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/database/src/commonMain/kotlin/kpt/core/database/alerts/AlertDao.kt`

```kotlin
interface AlertDao
```
Data-access object for the `alerts` table. All reads return reactive `Flow`s; all writes are `suspend` one-shots. Natural sort order is newest-first (`AlertEntity.createdAt` descending).

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/alerts/FakeAlertDao.kt:29</code></summary>

```kotlin
 * assert the absence of a mechanism production depends on.
 */
internal class FakeAlertDao : AlertDao {

    private val rows = MutableStateFlow<List<AlertEntity>>(emptyList())

    override fun observeAll(): Flow<List<AlertEntity>> =
```

</details>

- `fun observeAll(): Flow<List<AlertEntity>>` — Observe all alerts, ordered newest-first.
- `fun observeById(id: String): Flow<AlertEntity?>` — Observe a single alert by id — the per-item write store's SourceOfTruth reader.
- `suspend fun upsert(alert: AlertEntity)` — Insert or replace a single alert.
- `suspend fun upsertAll(alerts: List<AlertEntity>)` — Insert or replace a batch of alerts.
- `suspend fun deleteById(id: String)` — Delete a single alert by id. No-op if absent.
- `suspend fun deleteAll()` — Delete all alerts.

### `core/database/src/commonMain/kotlin/kpt/core/database/alerts/AlertEntity.kt`

```kotlin
data class AlertEntity(
```
Persistent row for a price alert. Stored in the `alerts` table (v9+). Each row represents a single threshold-based alert for a given symbol.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/alerts/FakeAlertDao.kt:31</code></summary>

```kotlin
internal class FakeAlertDao : AlertDao {

    private val rows = MutableStateFlow<List<AlertEntity>>(emptyList())

    override fun observeAll(): Flow<List<AlertEntity>> =
        rows.map { list -> list.sortedByDescending { it.createdAt } }
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/banking/converter/BankingTypeConverters.kt`

```kotlin
class BankingTypeConverters
```
Room 3 `TypeConverter` collection for the banking-domain tables (`banking_loans`, `banking_bill_reminders`). - Enums (`LoanKind`, `Recurrence`, `BillCategory`) persist as their `name` (TEXT).

<details><summary>Used in the template — <code>core/database/src/commonTest/kotlin/kpt/core/database/banking/converter/BankingTypeConvertersTest.kt:28</code></summary>

```kotlin
class BankingTypeConvertersTest {

    private val converters = BankingTypeConverters()

    @Test
    fun loanKindRoundTripCoversAllValues() {
        LoanKind.entries.forEach { kind ->
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/banking/dao/BillReminderDao.kt`

```kotlin
interface BillReminderDao
```
Data-access object for the `banking_bill_reminders` table. Reads are reactive `Flow`s; writes are `suspend`. Natural sort order is by `BillReminderEntity.dueDay` ascending — bills due earliest in the month appear first in the dashboard.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/banking/impl/BillReminderRepositoryImpl.kt:53</code></summary>

```kotlin
    @FromStore(AppStoreIds.BillRemindersMutable)
    private val billRemindersWriteStore: MutableStore<String, BillReminder>,
    private val billReminderDao: BillReminderDao,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : BillReminderRepository {
```

</details>

- `fun observeAll(): Flow<List<BillReminderEntity>>` — Observe all bill reminders, ordered by day-of-month then creation.
- `fun observeUpcoming(dueDays: Set<Int>): Flow<List<BillReminderEntity>>` — Observe reminders whose `dueDay` falls within the supplied set of day-of-month integers. The repository computes the set from today's date + `maxDays`. Returns only enabled reminders by default.
- `fun observeById(id: String): Flow<BillReminderEntity?>` — Observe a single bill reminder. Emits `null` after deletion.
- `suspend fun getById(id: String): BillReminderEntity?` — One-shot read for non-reactive callers.
- `fun count(): Flow<Int>` — Reactive row count — used by the dashboard badge.
- `suspend fun upsert(entity: BillReminderEntity)` — Insert-or-replace by primary key.
- `suspend fun deleteById(id: String)` — Delete a bill reminder by id. No-op if absent.
- `suspend fun deleteAll()` — Clears every row. Called on logout via `StoreCacheManager.clearAll()`.

### `core/database/src/commonMain/kotlin/kpt/core/database/banking/dao/LoanDao.kt`

```kotlin
interface LoanDao
```
Data-access object for the `banking_loans` table. Reads are reactive `Flow`s; writes are `suspend`. Standard Room 3 KMP shape.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/banking/impl/LoanRepositoryImpl.kt:42</code></summary>

```kotlin
    @FromStore(AppStoreIds.Loans) private val loansStore: Store<Unit, List<Loan>>,
    @FromStore(AppStoreIds.LoansMutable) private val loansWriteStore: MutableStore<String, Loan>,
    private val loanDao: LoanDao,
) : LoanRepository {

    override fun loansStream(scope: CoroutineScope): ScreenDataStream<List<Loan>> =
        loansStore.asScreenStream(
```

</details>

- `fun observeAll(): Flow<List<LoanEntity>>` — Observe all loans, soonest-due first. Emits on every change.
- `fun observeById(id: String): Flow<LoanEntity?>` — Observe a single loan by `id`. Emits `null` if it has been deleted.
- `suspend fun getById(id: String): LoanEntity?` — One-shot read for non-reactive callers (mappers, tests, migrations).
- `fun count(): Flow<Int>` — Reactive row count — used by the dashboard badge.
- `suspend fun upsert(entity: LoanEntity)` — Insert-or-replace. The `id` is the primary key, so re-saving overwrites.
- `suspend fun deleteById(id: String)` — Remove a loan by `id`. No-op if absent.
- `suspend fun deleteAll()` — Clears every row. Called on logout via `StoreCacheManager.clearAll()`.
- `fun observeAllByNextDue(): Flow<List<LoanEntity>> = observeAll()` — Loans ordered by next due-date. The ordering is in the `observeAll` query, so this is a naming alias rather than a second query to keep in step.
- `val UPSERT_STRATEGY = OnConflictStrategy.REPLACE` — REPLACE: a re-synced loan overwrites the local row wholesale. Safe here because the tracker has no field the server does not also own.

### `core/database/src/commonMain/kotlin/kpt/core/database/banking/entity/BillReminderEntity.kt`

```kotlin
data class BillReminderEntity(
```
Persistent row for a recurring (or one-time) bill reminder. Mirrors `kpt.core.model.banking.BillReminder`; mapping lives in `core/data/banking/`. Stored locally only — no remote sync.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/banking/FakeBillReminderDao.kt:26</code></summary>

```kotlin
internal class FakeBillReminderDao : BillReminderDao {

    private val state = MutableStateFlow<List<BillReminderEntity>>(emptyList())

    override fun observeAll(): Flow<List<BillReminderEntity>> = state.map { rows ->
        rows.sortedWith(compareBy({ it.dueDay }, { it.createdAtMs }))
    }
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/banking/entity/LoanEntity.kt`

```kotlin
data class LoanEntity(
```
Persistent row for a personal loan tracked by the user. Mirrors `kpt.core.model.banking.Loan`; mapping lives in the repository layer (`core/data/banking/`). Stored locally only — no remote sync.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/banking/FakeLoanDao.kt:26</code></summary>

```kotlin
internal class FakeLoanDao : LoanDao {

    private val state = MutableStateFlow<List<LoanEntity>>(emptyList())

    override fun observeAll(): Flow<List<LoanEntity>> = state.map { rows ->
        rows.sortedWith(compareBy({ it.nextDueDate }, { it.createdAtMs }))
    }
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/cloudtodo/CloudTodoDao.kt`

```kotlin
interface CloudTodoDao
```
Room DAO for the cloud-todo demo rows — the MUTABLE (offline-write) archetype's source of truth. Bound into the Store's `SourceOfTruth` — the reader/writer/delete lambdas are the ONLY callers of these members (S5-1).

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/CloudTodoDataProviders.kt:52</code></summary>

```kotlin
    bookkeeperDao: BookkeeperDao,
    bookkeeper: Bookkeeper<CloudTodoKey>,
    todoDao: CloudTodoDao,
    repository: CloudTodoRepository,
    crashReporter: CrashReporter?,
): CloudTodoSyncOrchestrator = CloudTodoSyncOrchestrator(
    scope = scope,
```

</details>

- `fun observeById(id: Int): Flow<CloudTodoEntity?>` — Streams one todo, emitting null while it is absent — the Store's local read.
- `suspend fun getById(id: Int): CloudTodoEntity?` — One-shot read, for the Updater's read-modify-write.
- `suspend fun upsert(entity: CloudTodoEntity)` — Inserts or replaces — the Store's local write.
- `suspend fun deleteById(id: Int)` — Removes one todo.
- `suspend fun deleteAll()` — Clears every row. Called on logout.

### `core/database/src/commonMain/kotlin/kpt/core/database/cloudtodo/CloudTodoEntity.kt`

```kotlin
data class CloudTodoEntity(
```
Room mirror of a `kpt.core.model.cloudtodo.CloudTodo` (the Store5 MutableStore SoT).

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/cloudtodo/CloudTodoRepositoryTest.kt:76</code></summary>

```kotlin

private class FakeCloudTodoDao : CloudTodoDao {
    private val rows = mutableMapOf<Int, CloudTodoEntity>()
    override fun observeById(id: Int): Flow<CloudTodoEntity?> = flow { emit(rows[id]) }
    override suspend fun getById(id: Int): CloudTodoEntity? = rows[id]
    override suspend fun upsert(entity: CloudTodoEntity) {
        rows[entity.id] = entity
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/cloudtodo/CloudTodoEntityMapper.kt`

```kotlin
fun CloudTodoEntity.toDomain(): CloudTodo = CloudTodo(id = id, title = title, completed = completed)
```
Row → domain. Called from the Store's `SourceOfTruth.reader`, which is what keeps the entity type out of every layer above `core/store`.

```kotlin
fun CloudTodo.toEntity(): CloudTodoEntity = CloudTodoEntity(id = id, title = title, completed = completed)
```
Domain → row. Called from the `SourceOfTruth.writer`.

### `core/database/src/commonMain/kotlin/kpt/core/database/config/DatabaseConfig.kt`

```kotlin
object DatabaseConfig
```
Fork-unique database naming — generated by syncForkConfig from app-profile/app.yaml.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/di/DatabaseModule.kt:24</code></summary>

```kotlin
 */
internal val appDatabaseNaming = DatabaseNaming(
    fileName = DatabaseConfig.NAME,
    desktopDirName = DatabaseConfig.DESKTOP_DIR_NAME,
)

/**
```

</details>

- `const val NAME = "org_mifos_kmp_template.db"` — On-disk SQLite file name (appId-derived).
- `const val DESKTOP_DIR_NAME = "MoneyToolkit"` — Desktop (JVM) data directory under the OS app-data root (app_name-derived).

### `core/database/src/commonMain/kotlin/kpt/core/database/config/ForkDatabaseConfig.kt`

```kotlin
object ForkDatabaseConfig
```
The fork's Room schema version. `owner: fork` — PRESERVED across `/kmp-project-template-sync`. GENERATED from `app-profile/migration-ledger.yaml#version` by `./gradlew syncForkConfig`; do not hand-edit.

<details><summary>Used in the template — <code>core/database/src/nonWebTest/kotlin/kpt/core/database/config/AppDatabaseTest.kt:48</code></summary>

```kotlin
        // ForkDatabaseConfig.VERSION, the one constant a fork bumps. The template's own v13 schema is
        // pinned in alerts/DemoDatabaseTest.
        assertEquals(ForkDatabaseConfig.VERSION, AppDatabase.VERSION)
    }

    @Test
    fun databaseNameIsCorrect() {
```

</details>

- `const val VERSION = 13` — The fork's Room schema version — kept equal to `app-profile/migration-ledger.yaml#version` by product-health check LG-5.

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/converter/FintechTypeConverters.kt`

```kotlin
class FintechTypeConverters
```
Room type converters for the composite values these tables store (rate-point lists, maps).

```kotlin
data class RatePointPair(
```
Serialisable (date, value) pair — the wire form a rate-point list is stored as.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/currency/mapper/RateHistoryEntityMapper.kt:27</code></summary>

```kotlin
    startDate = startDate,
    endDate = endDate,
    ratesJson = Json.encodeToString(rates.map { RatePointPair(it.date, it.value) }),
    fetchedAt = Clock.System.now().toEpochMilliseconds(),
)

/**
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/dao/CoinDetailDao.kt`

```kotlin
interface CoinDetailDao
```
Room DAO for cached coin detail — the offline fallback behind `NETWORK_WITH_CACHE`. Bound into the Store's `SourceOfTruth` — the reader/writer/delete lambdas are the ONLY callers of these members (S5-1).

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/crypto/impl/CoinDetailStore.kt:38</code></summary>

```kotlin
    api: CoinGeckoApi,
    networkMonitor: NetworkMonitor,
    dao: CoinDetailDao,
): Store<String, CoinDetail> {
    val validator = DefaultValidator.withTtl<CoinDetail>(AppStoreRegistry.Ttl.COIN_DETAIL)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { coinId: String ->
```

</details>

- `suspend fun upsert(entity: CoinDetailEntity)` — Inserts or replaces the cached detail row.
- `fun getById(coinId: String): Flow<CoinDetailEntity?>` — Streams one coin's detail, emitting null when nothing is cached.
- `suspend fun delete(coinId: String)` — Evicts one coin's cached detail. Used for a targeted refresh; the list cache in `coin_market` is unaffected, so the row stays visible while its detail re-fetches.
- `suspend fun deleteAll()` — Clears the cache. Called on logout.

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/dao/CoinMarketDao.kt`

```kotlin
interface CoinMarketDao
```
Room DAO for the paged coin-market list. `replacePage` is `@Transaction` on purpose: delete-then-upsert as two calls lets an in-flight reader observe an empty page (S5-PAGE-ATOMIC).

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/crypto/impl/CoinMarketsStore.kt:41</code></summary>

```kotlin
    api: CoinGeckoApi,
    networkMonitor: NetworkMonitor,
    dao: CoinMarketDao,
): Store<PageKey, List<CoinMarket>> {
    val validator = DefaultValidator.withTtl<List<CoinMarket>>(AppStoreRegistry.Ttl.COIN_MARKETS)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: PageKey ->
```

</details>

- `suspend fun upsertAll(entities: List<CoinMarketEntity>)` — Inserts or replaces a whole page in one transaction, so a partial page never becomes visible.
- `fun getPage(limit: Int, offset: Int): Flow<List<CoinMarketEntity>>` — One page of the market list, by limit/offset.
- `fun getAll(): Flow<List<CoinMarketEntity>>` — Every cached row across all pages — what the paging stream accumulates over.
- `suspend fun deleteByPage(page: Int)` — Evicts one page, for a targeted refresh.
- `suspend fun deleteAll()` — Clears the cache. Called on logout.
- `suspend fun count(): Int` — How many rows are cached — used to decide whether another page exists.
- `suspend fun replacePage(page: Int, entities: List<CoinMarketEntity>)` — Atomically swap the rows of one page (S5-3 PAGINATION_RACE).

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/entity/CoinDetailEntity.kt`

```kotlin
data class CoinDetailEntity(
```
Room row for a cached coin detail. An ENTITY, not the domain model — the entity↔domain mapping lives in the Store's `SourceOfTruth`, so nothing above `core/store` sees this type.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/crypto/dao/CoinDetailDao.kt:32</code></summary>

```kotlin
    /** Inserts or replaces the cached detail row. */
    @Upsert
    suspend fun upsert(entity: CoinDetailEntity)

    /** Streams one coin's detail, emitting null when nothing is cached. */
    @Query("SELECT * FROM coin_detail WHERE id = :coinId LIMIT 1")
    fun getById(coinId: String): Flow<CoinDetailEntity?>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/entity/CoinMarketEntity.kt`

```kotlin
data class CoinMarketEntity(
```
Room row for one cached coin-market row; `page` is the cursor bucket `replacePage` swaps atomically.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/crypto/dao/CoinMarketDao.kt:36</code></summary>

```kotlin
    /** Inserts or replaces a whole page in one transaction, so a partial page never becomes visible. */
    @Upsert
    suspend fun upsertAll(entities: List<CoinMarketEntity>)

    /** One page of the market list, by limit/offset. */
    @Query("SELECT * FROM coin_markets ORDER BY marketCapRank ASC LIMIT :limit OFFSET :offset")
    fun getPage(limit: Int, offset: Int): Flow<List<CoinMarketEntity>>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/mapper/CoinDetailEntityMapper.kt`

```kotlin
fun CoinDetail.toEntity(): CoinDetailEntity = CoinDetailEntity(
```
Domain → row, for the Store's writer.

```kotlin
fun CoinDetailEntity.toDomain(): CoinDetail = CoinDetail(
```
Row → domain, for the Store's reader.

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/mapper/CoinMarketEntityMapper.kt`

```kotlin
fun CoinMarket.toEntity(page: Int): CoinMarketEntity = CoinMarketEntity(
```
Domain → row, stamping the `page` this item belongs to so `replacePage` can swap one bucket.

```kotlin
fun CoinMarketEntity.toDomain(): CoinMarket = CoinMarket(
```
Row → domain, dropping the paging bookkeeping.

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/converter/ChargeTypeConverters.kt`

```kotlin
class ChargeTypeConverters
```
Room 3 `TypeConverter` collection for JSON-backed column types. Supports optional field-level encryption via `FieldEncryptor`.

<details><summary>Used in the template — <code>core/database/src/commonTest/kotlin/kpt/core/database/currency/ChargeTypeConvertersTest.kt:18</code></summary>

```kotlin
class ChargeTypeConvertersTest {

    private val converters = ChargeTypeConverters()

    @Test
    fun intListRoundTripPreservesData() {
        val original = arrayListOf<Int?>(1, 2, 3, null, 5)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/dao/ExchangeRatesDao.kt`

```kotlin
interface ExchangeRatesDao
```
Room DAO for cached FX rates, keyed by base currency. Bound into the Store's `SourceOfTruth` — the reader/writer/delete lambdas are the ONLY callers of these members (S5-1).

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/currency/impl/ExchangeRatesStore.kt:39</code></summary>

```kotlin
    api: FrankfurterApi,
    networkMonitor: NetworkMonitor,
    dao: ExchangeRatesDao,
): Store<String, ExchangeRates> {
    val validator = DefaultValidator.withTtl<ExchangeRates>(AppStoreRegistry.Ttl.EXCHANGE_RATES)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { baseCurrency: String ->
```

</details>

- `suspend fun upsert(entity: ExchangeRatesEntity)` — Inserts or replaces the cached rates for one base currency.
- `fun getByBase(currency: String): Flow<ExchangeRatesEntity?>` — Streams the cached rates for one base currency, emitting null when nothing is cached.
- `suspend fun deleteByBase(currency: String)` — Evicts the cached rates for one base currency, leaving every other base intact — so switching base re-fetches only what changed.
- `suspend fun deleteAll()` — Clears the cache. Called on logout.
- `suspend fun deleteOlderThan(epochMillis: Long)` — Prunes rows cached before `epochMillis` — the cold-start sweep, so a stale day's rates are not served as current.

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/dao/RateHistoryDao.kt`

```kotlin
interface RateHistoryDao
```
Room DAO for cached historical FX series, keyed by pair and window. Bound into the Store's `SourceOfTruth` — the reader/writer/delete lambdas are the ONLY callers of these members (S5-1).

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/currency/impl/RateHistoryStore.kt:51</code></summary>

```kotlin
    api: FrankfurterApi,
    networkMonitor: NetworkMonitor,
    dao: RateHistoryDao,
): Store<RateHistoryKey, RateHistory> {
    val validator = DefaultValidator.withTtl<RateHistory>(AppStoreRegistry.Ttl.RATE_HISTORY)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: RateHistoryKey ->
```

</details>

- `suspend fun upsert(entity: RateHistoryEntity)` — Inserts or replaces one cached series.
- `fun get(from: String, to: String, startDate: String, endDate: String): Flow<RateHistoryEntity?>` — Streams one exact series. The date range is part of the lookup because a widened window is a different key, not a page append.
- `suspend fun delete(from: String, to: String)` — Evicts every series for one pair.
- `suspend fun deleteAll()` — Clears the cache. Called on logout.

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/entity/ExchangeRatesEntity.kt`

```kotlin
data class ExchangeRatesEntity(
```
Room row for a cached FX rate set for one base currency. An ENTITY, not the domain model — the entity↔domain mapping lives in the Store's `SourceOfTruth`, so nothing above `core/store` sees this type.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/currency/dao/ExchangeRatesDao.kt:32</code></summary>

```kotlin
    /** Inserts or replaces the cached rates for one base currency. */
    @Upsert
    suspend fun upsert(entity: ExchangeRatesEntity)

    /** Streams the cached rates for one base currency, emitting null when nothing is cached. */
    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :currency LIMIT 1")
    fun getByBase(currency: String): Flow<ExchangeRatesEntity?>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/entity/RateHistoryEntity.kt`

```kotlin
data class RateHistoryEntity(
```
Room row for a cached historical FX series. An ENTITY, not the domain model — the entity↔domain mapping lives in the Store's `SourceOfTruth`, so nothing above `core/store` sees this type.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/currency/dao/RateHistoryDao.kt:32</code></summary>

```kotlin
    /** Inserts or replaces one cached series. */
    @Upsert
    suspend fun upsert(entity: RateHistoryEntity)

    /**
     * Streams one exact series. The date range is part of the lookup because a widened window is a different key, not
     * a page append.
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/mapper/ExchangeRatesEntityMapper.kt`

```kotlin
fun ExchangeRates.toEntity(baseCurrency: String): ExchangeRatesEntity = ExchangeRatesEntity(
```
Domain → row, keyed by `baseCurrency`.

```kotlin
fun ExchangeRatesEntity.toDomain(): ExchangeRates = ExchangeRates(
```
Row → domain.

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/mapper/RateHistoryEntityMapper.kt`

```kotlin
fun RateHistory.toEntity(): RateHistoryEntity = RateHistoryEntity(
```
Domain → row, serialising the sample list through the type converters.

```kotlin
fun RateHistoryEntity.toDomain(): RateHistory = RateHistory(
```
Row → domain, deserialising the sample list.

### `core/database/src/commonMain/kotlin/kpt/core/database/di/DatabaseModule.kt`

```kotlin
val DatabaseModule = module
```
Koin module that provides the `AppDatabase` instance and the framework-infra DAO singletons.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:39</code></summary>

```kotlin
 */
val DataModule = module {
    includes(platformModule, CommonModule, DatabaseModule, DatastoreModule, NetworkModule)

    // Every repository's Koin binding, GENERATED from `@RepositoryBinding` on the implementation.
    // Emitted into this same package, so this file needs no import and keeps its zero-demo-reference
    // property — which is what lets a template sync blind-copy it. A stripped fork simply generates
```

</details>

```kotlin
expect val platformModule: Module
```
Platform-specific Koin module that provides the `AppDatabase` singleton.

### `core/database/src/commonMain/kotlin/kpt/core/database/di/ProjectDatabaseModule.kt`

```kotlin
val ProjectDatabaseModule = module
```
THE FORK'S database DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
single { MyBackupScheduler(get<AppDatabase>()) }
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/economic/InterestRateSeriesDao.kt`

```kotlin
interface InterestRateSeriesDao
```
Data-access object for the `interest_rate_series` table. All reads return reactive `Flow`s; all writes are `suspend` one-shots.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/economic/impl/InterestRateSeriesStore.kt:55</code></summary>

```kotlin
    config: FredApiConfig,
    networkMonitor: NetworkMonitor,
    dao: InterestRateSeriesDao,
): Store<InterestRateSeriesKey, InterestRateSeries> {
    val validator = DefaultValidator.withTtl<InterestRateSeries>(AppStoreRegistry.Ttl.INTEREST_RATE_SERIES)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: InterestRateSeriesKey ->
```

</details>

- `fun observeBySeriesId(seriesId: String): Flow<List<InterestRateSeriesEntity>>` — Observe all data-points for a given `seriesId`, ordered newest-first.
- `fun observeAll(): Flow<List<InterestRateSeriesEntity>>` — Observe all cached data-points across all series, ordered newest-first.
- `suspend fun upsertAll(entries: List<InterestRateSeriesEntity>)` — Insert or replace a batch of data-points (used during cache refresh).
- `suspend fun deleteBySeriesId(seriesId: String)` — Delete all cached data-points for a given series.
- `suspend fun upsert(entry: InterestRateSeriesEntity)` — Insert or replace a single data-point.
- `suspend fun deleteAll()` — Delete all cached data-points across all series.

### `core/database/src/commonMain/kotlin/kpt/core/database/economic/InterestRateSeriesEntity.kt`

```kotlin
data class InterestRateSeriesEntity(
```
Persistent row for a single data-point in an interest-rate time series. Stored in the `interest_rate_series` table (v10+).

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/economic/InterestRateSeriesDao.kt:36</code></summary>

```kotlin
     */
    @Query("SELECT * FROM interest_rate_series WHERE seriesId = :seriesId ORDER BY date DESC")
    fun observeBySeriesId(seriesId: String): Flow<List<InterestRateSeriesEntity>>

    /** Observe all cached data-points across all series, ordered newest-first. */
    @Query("SELECT * FROM interest_rate_series ORDER BY date DESC")
    fun observeAll(): Flow<List<InterestRateSeriesEntity>>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/migrations/MigrationSpec8to10.kt`

```kotlin
class MigrationSpec8to10 : AutoMigrationSpec
```
Auto-migration spec for the demo showcase's v8 → v10 hop (collapsed from the never-shipped v8→9 + v9→10 path). Instructs Room to DROP the `samples` table that was present in v1–v8 and is no longer part of the schema.

### `core/database/src/commonMain/kotlin/kpt/core/database/watchlist/dao/WatchlistDao.kt`

```kotlin
interface WatchlistDao
```
Data-access object for the `personal_watchlist` table. Reads are reactive `Flow`s; writes are `suspend`. Standard Room 3 KMP shape.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/watchlist/impl/WatchlistRepositoryImpl.kt:45</code></summary>

```kotlin
    @FromStore(AppStoreIds.Watchlist) private val watchlistStore: Store<Unit, List<WatchlistItem>>,
    @FromStore(AppStoreIds.WatchlistMutable) private val watchlistWriteStore: MutableStore<String, WatchlistItem>,
    private val dao: WatchlistDao,
) : WatchlistRepository {

    override fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>> =
        watchlistStore.asScreenStream(
```

</details>

- `fun observeAll(): Flow<List<WatchlistEntity>>` — Observe the full watchlist, ordered by addition time (newest first).
- `fun observeContains(coinId: String): Flow<Boolean>` — Reactive in-membership check for a given coin — emits whenever the watchlist changes.
- `fun observeById(coinId: String): Flow<WatchlistEntity?>` — Observe a single watchlist row by coin id — the per-item write store's SourceOfTruth reader.
- `suspend fun insert(entry: WatchlistEntity)` — Add a coin to the watchlist. If already present, replaces the row (no-op effectively).
- `suspend fun delete(coinId: String)` — Remove a coin from the watchlist. No-op if absent.
- `suspend fun deleteAll()` — Clear the whole watchlist — used by the Store's `deleteAll` (logout cache clear).

### `core/database/src/commonMain/kotlin/kpt/core/database/watchlist/entity/WatchlistEntity.kt`

```kotlin
data class WatchlistEntity(
```
Persistent row representing a coin in the user's personal watchlist. Local-only: no Store5 caching layer, no remote sync.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/watchlist/FakeWatchlistDao.kt:33</code></summary>

```kotlin
internal class FakeWatchlistDao : WatchlistDao {

    private val rows = MutableStateFlow<List<WatchlistEntity>>(emptyList())

    override fun observeAll(): Flow<List<WatchlistEntity>> =
        rows.map { list -> list.sortedByDescending { it.addedAtMs } }
```

</details>

---

_27 type(s), 76 function(s)/property(ies); 103 carry KDoc at source; 1 authored example(s); 26 live call site(s)._
<!-- api-docs:end -->
