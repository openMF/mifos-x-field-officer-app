# `core/store`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_STORE.md`
> **Measured:** 36 Kotlin files, 6 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@StoreProvider` | `store-ksp` | config/AppStoreRegistry, config/AppStoreIds, di/GeneratedStoreBindings |
| `@CacheKey` | `store-ksp` | config/AppCacheKeys |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`AmortizationCalcParams`, `AmortizationCompute`, `CloudTodoConflictResolver`, `CloudTodoKey`, `CloudTodoSyncOrchestrator`, `EmiCompute`, `EmiParams`, `ErrorMessageOverrides`, `InterestRateSeriesKey`, `MacroIndicatorKey`, `ProfileInfoSource`, `ProjectErrorMapper`, `ProjectScreenStateDefaults`, `ScreenStateOverrides`  …and 1 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/store sha=77a6e48e8dd64277120e6a49c735a34e9ca0d3fb -->
## API reference

_Generated from `core/store` at tree `77a6e48e8dd6` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/store/src/commonMain/kotlin/kpt/core/store/alerts/impl/AlertMappers.kt`

```kotlin
fun AlertEntity.toPriceAlert(): PriceAlert = PriceAlert(
```
Entity ⇄ domain mappers for the price-alerts feature.

```kotlin
fun PriceAlert.toAlertEntity(): AlertEntity = AlertEntity(
```
@see toPriceAlert — the persistable inverse used by the repository's write path.

### `core/store/src/commonMain/kotlin/kpt/core/store/alerts/impl/AlertsStore.kt`

```kotlin
fun provideAlertsStore(dao: AlertDao): Store<Unit, List<PriceAlert>> = StoreFactory.createOfflineStore(
```
Build an offline-only `Store` for price alerts. Backed exclusively by `AlertDao` — there is no remote fetcher.

```kotlin
fun provideAlertsWriteStore(dao: AlertDao): MutableStore<String, PriceAlert> =
```
Per-item WRITE store for price alerts (keyed by alert id).

### `core/store/src/commonMain/kotlin/kpt/core/store/banking/impl/BillReminderMappers.kt`

```kotlin
fun BillReminderEntity.toDomain(): BillReminder = BillReminder(
```
Entity ⇄ domain mappers for the bill-reminders feature.

```kotlin
fun BillReminder.toEntity(): BillReminderEntity = BillReminderEntity(
```
@see toDomain — the persistable inverse used by the repository's write path.

### `core/store/src/commonMain/kotlin/kpt/core/store/banking/impl/BillRemindersStore.kt`

```kotlin
fun provideBillRemindersStore(dao: BillReminderDao): Store<Unit, List<BillReminder>> =
```
Build an offline-only `Store` for recurring bill reminders. Backed exclusively by `BillReminderDao` (OFFLINE_LOCAL_ONLY archetype) — bill reminders are user-created local records with no remote source.

```kotlin
fun provideBillReminderDetailStore(dao: BillReminderDao): Store<String, BillReminder> =
```
Single bill reminder by id — `OFFLINE_LOCAL_ONLY`, Room is the authority. Keyed by id rather than reusing the list store so a detail screen re-reads only its own row.

```kotlin
fun provideBillRemindersWriteStore(dao: BillReminderDao): MutableStore<String, BillReminder> =
```
Per-item WRITE store for bill reminders (keyed by bill id). Every mutation flows through `store.write` / `store.clear`, so the repository never touches the DAO for writes — the SoT writer/delete are the single DAO callers.

### `core/store/src/commonMain/kotlin/kpt/core/store/banking/impl/LoanMappers.kt`

```kotlin
fun LoanEntity.toDomain(): Loan = Loan(
```
Entity ⇄ domain mappers for the loans feature.

```kotlin
fun Loan.toEntity(): LoanEntity = LoanEntity(
```
@see toDomain — the persistable inverse used by the repository's write path.

### `core/store/src/commonMain/kotlin/kpt/core/store/banking/impl/LoansStore.kt`

```kotlin
fun provideLoansStore(dao: LoanDao): Store<Unit, List<Loan>> = StoreFactory.createOfflineStore(
```
Build an offline-only `Store` for tracked personal loans. Backed exclusively by `LoanDao` (OFFLINE_LOCAL_ONLY archetype) — loans are user-managed local records with no remote sync.

```kotlin
fun provideLoanDetailStore(dao: LoanDao): Store<String, Loan> = StoreFactory.createOfflineStore(
```
Single loan by id — `OFFLINE_LOCAL_ONLY`, Room is the authority.

```kotlin
fun provideLoansWriteStore(dao: LoanDao): MutableStore<String, Loan> =
```
Per-item WRITE store for loans (keyed by loan id). Every mutation flows through `store.write` / `store.clear`, so the repository never touches the DAO — the SoT writer/delete are the single DAO callers.

### `core/store/src/commonMain/kotlin/kpt/core/store/calc/impl/AmortizationCalcStore.kt`

```kotlin
data class AmortizationCalcParams(
```
Store key for one amortization calculation — the calculator's inputs. An amortization schedule for a 240-month loan is a 240-row list rebuilt on every keystroke when it is derived straight off a form `StateFlow`.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/amortizationcalc/AmortizationViewModel.kt:65</code></summary>

```kotlin
    @OptIn(ExperimentalCoroutinesApi::class)
    val breakdownState: StateFlow<ScreenState<AmortizationBreakdown>> = stateFlow
        .map { AmortizationCalcParams(it.principal, it.ratePercent, it.tenureMonths) }
        .distinctUntilChanged()
        .flatMapLatest { params ->
            if (params.isComputable) {
                calcRepository.breakdownStream(params, viewModelScope)
```

</details>

```kotlin
fun interface AmortizationCompute
```
The compute PORT for the amortization store.

```kotlin
fun provideAmortizationCalcStore(
```
Amortization schedule for a loan — `MEMORY_ONLY`, computed rather than fetched.

### `core/store/src/commonMain/kotlin/kpt/core/store/cloudtodo/impl/CloudTodoConflictResolver.kt`

```kotlin
class CloudTodoConflictResolver(
```
The named conflict surface for the cloud-todo MUTABLE archetype (S5-CONFLICT).

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/cloudtodo/impl/CloudTodoStore.kt:78</code></summary>

```kotlin
    bookkeeper: Bookkeeper<CloudTodoKey>,
): MutableStore<CloudTodoKey, CloudTodo> {
    val conflictResolver = CloudTodoConflictResolver()

    val converter = Converter.Builder<CloudTodoDto, CloudTodoEntity, CloudTodo>()
        // fetch -> SoT: network DTO mapped through domain to the Room entity.
        .fromNetworkToLocal { network: CloudTodoDto -> network.toDomain().toEntity() }
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/cloudtodo/impl/CloudTodoKey.kt`

```kotlin
data class CloudTodoKey(val id: Int)
```
Single-todo Store5 key (jsonplaceholder addresses todos by numeric id).

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/CloudTodoDataProviders.kt:33</code></summary>

```kotlin
 */
@DataProvider
fun provideCloudTodoBookkeeper(dao: BookkeeperDao): Bookkeeper<CloudTodoKey> =
    RoomBookkeeper(dao = dao, keySerializer = { "$CLOUD_TODO_KEY_PREFIX${it.id}" })

/**
 * Eager: drains the cloud-todo write backlog on the offline -> online edge.
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/cloudtodo/impl/CloudTodoStore.kt`

```kotlin
fun provideCloudTodoReadStore(
```
cloud-todo READ store (`createStore`) — the offline-first read half of the MUTABLE archetype.

```kotlin
fun provideCloudTodoStore(
```
cloud-todo — the toolkit's **MUTABLE (offline-write) Store5 archetype** showcase, and the only demo that writes back to a server (jsonplaceholder `/todos` accepts PUT).

### `core/store/src/commonMain/kotlin/kpt/core/store/cloudtodo/impl/CloudTodoSyncOrchestrator.kt`

```kotlin
class CloudTodoSyncOrchestrator(
```
Drains the cloud-todo write backlog when connectivity returns (S5-SYNC). The MUTABLE archetype records a failed write through its `Bookkeeper` — but recording is only half of offline-first.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/CloudTodoDataProviders.kt:47</code></summary>

```kotlin
 */
@DataProvider(createdAtStart = true)
fun provideCloudTodoSyncOrchestrator(
    scope: CoroutineScope,
    networkMonitor: NetworkMonitor,
    bookkeeperDao: BookkeeperDao,
    bookkeeper: Bookkeeper<CloudTodoKey>,
```

</details>

```kotlin
fun String.toCloudTodoKeyOrNull(): CloudTodoKey? =
```
Parse a bookkeeper key back into a `CloudTodoKey`, or `null` when it belongs to a different store.

### `core/store/src/commonMain/kotlin/kpt/core/store/config/AppErrorMapper.kt`

```kotlin
fun errorCategoryToken(error: Throwable): String = when (val cat = categorize(error))
```
Application-level error → user-facing message mapper. ONE source of user-facing copy: `rememberAppErrorMessageFor`, which resolves every `ErrorCategory` through `stringResource(...)`.

```kotlin
interface ErrorMessageOverrides
```
The fork's error-copy extension point, declared HERE so it is TEMPLATE-owned and full-copied by every sync. `ProjectErrorMapper` implements it and is the only fork-owned half.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/ProjectErrorMapper.kt:50</code></summary>

```kotlin
 * stop refetching. Declare the key on its store instead.
 */
object ProjectErrorMapper : ErrorMessageOverrides
```

</details>

- `fun message(error: Throwable): String? = null` — Fork copy for `error`, or null to fall through to the framework's categorised message.

```kotlin
fun rememberAppErrorMessageFor(): (Throwable) -> String
```
Composable factory that resolves per-category copy via `stringResource(...)`, returning a pure-Kotlin `(Throwable) -> String` lambda safe to pass through to `kpt.core.base.ui.screen.ScreenStateError.messageFor` (which is invoked from inside composition where these strings are already memoised). Reuses `categorize` to bucket the error, then picks the localized string.

### `core/store/src/commonMain/kotlin/kpt/core/store/config/AppScreenStateDefaults.kt`

```kotlin
fun appScreenStateDefaults(): ScreenStateDefaults
```
TEMPLATE-OWNED framework defaults. Brand your app in `ProjectScreenStateDefaults` instead — do NOT edit this file. It used to say "!!

```kotlin
interface ScreenStateOverrides
```
The fork's screen-state branding extension point, declared HERE so it is TEMPLATE-owned and full-copied by every sync. `ProjectScreenStateDefaults` implements it and is the only fork-owned half.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/ProjectScreenStateDefaults.kt:39</code></summary>

```kotlin
 * that introduces it.
 */
object ProjectScreenStateDefaults : ScreenStateOverrides
```

</details>

- `fun customize(defaults: ScreenStateDefaults): ScreenStateDefaults = defaults` — Return `defaults` with the fork's branding applied, or `defaults` unchanged.

### `core/store/src/commonMain/kotlin/kpt/core/store/config/ProjectErrorMapper.kt`

```kotlin
object ProjectErrorMapper : ErrorMessageOverrides
```
THE FORK'S domain-error copy. Neutral on the template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
object ProjectErrorMapper : ErrorMessageOverrides {
    override fun message(error: Throwable): String? = when (error) {
        is InsufficientFundsException -> "Not enough balance for this transfer."
        is CardDeclinedException -> "That card was declined. Try another payment method."
        else -> null
    }
}
```

</details>

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/AppErrorMapper.kt:101</code></summary>

```kotlin
    val clientCopy = stringResource(Res.string.error_category_client)
    val genericCopy = stringResource(Res.string.error_category_generic)
    val projectCopy = ProjectErrorMapper::message
    return remember(
        networkCopy,
        connectTimeoutCopy,
        readTimeoutCopy,
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/config/ProjectScreenStateDefaults.kt`

```kotlin
object ProjectScreenStateDefaults : ScreenStateOverrides
```
THE FORK'S branding for the shared empty / error / no-network / loading visuals. Neutral on the template — this is yours to fill. Implements the TEMPLATE-owned `ScreenStateOverrides`.

<details><summary>Example</summary>

```kotlin
object ProjectScreenStateDefaults : ScreenStateOverrides {
    override fun customize(defaults: ScreenStateDefaults): ScreenStateDefaults = defaults.copy(
        empty = defaults.empty.copy(visual = ScreenStateVisual.Lottie(spec = MyBrandAnimations.empty)),
        error = defaults.error.copy(onShown = { e -> AppTelemetry.recordError("screen_state_error", e) }),
    )
}
```

</details>

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/AppScreenStateDefaults.kt:102</code></summary>

```kotlin
                retryText = nonetRetry,
            ),
        ).let(ProjectScreenStateDefaults::customize)
    }
}

/**
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/crypto/impl/CoinDetailStore.kt`

```kotlin
fun provideCoinDetailStore(
```
Single coin's detail from CoinGecko — `NETWORK_WITH_CACHE`, Room as the offline fallback.

### `core/store/src/commonMain/kotlin/kpt/core/store/crypto/impl/CoinMarketsStore.kt`

```kotlin
fun provideCoinMarketsStore(
```
Paged coin-market list — `NETWORK_WITH_CACHE`, keyed by `PageKey` and read via `asPagingScreenStream`. The write path replaces a page atomically, so a scrolling reader never observes a half-empty page.

### `core/store/src/commonMain/kotlin/kpt/core/store/currency/impl/ExchangeRatesStore.kt`

```kotlin
fun provideExchangeRatesStore(
```
FX rates by base currency — `NETWORK_WITH_CACHE`; the TTL is what keeps call volume inside the provider's free quota.

### `core/store/src/commonMain/kotlin/kpt/core/store/currency/impl/RateHistoryStore.kt`

```kotlin
fun provideRateHistoryStore(
```
Historical FX series for a (from, to, window) key — `NETWORK_WITH_CACHE`. Widening the window is a NEW key and therefore a full re-fetch, not a page append: the series is windowed, never paged.

### `core/store/src/commonMain/kotlin/kpt/core/store/di/ProjectStoreModule.kt`

```kotlin
val ProjectStoreModule = module
```
THE FORK'S store-layer DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
@StoreProvider(id = "myThing", ttl = "5m")
@CacheKey(name = "LIST", key = "myThing")
fun provideMyThingStore(api: MyApi, dao: MyDao): Store<Unit, List<MyThing>> = …
val ProjectStoreModule = module {
    single<DraftInventory> { MyForkDraftInventory(draftDao = get(), audit = get()) }
}
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/di/StoreModule.kt`

```kotlin
val appStoreModule: Module = module
```
App-level Store wiring — FRAMEWORK ONLY.

<details><summary>Example</summary>

```kotlin
@StoreProvider(id = "myThing", ttl = "5m")
@CacheKey(name = "LIST", key = "myThing")
fun provideMyThingStore(api: MyApi, dao: MyDao): Store<Unit, List<MyThing>> = …
startKoin { modules(appStoreModule, /* … */) }
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/economic/impl/InterestRateSeriesKey.kt`

```kotlin
data class InterestRateSeriesKey(
```
Composite key identifying a single FRED series request.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/ui/HomeViewModel.kt:207</code></summary>

```kotlin

        /** Effective Federal Funds Rate — the overnight bank-to-bank lending rate. */
        val FedFundsKey: InterestRateSeriesKey = InterestRateSeriesKey(
            seriesId = "DFF",
            name = "Federal Funds Rate",
            unit = "%",
            days = 30,
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/economic/impl/InterestRateSeriesStore.kt`

```kotlin
fun provideInterestRateSeriesStore(
```
Build a `Store` for FRED interest-rate series with `InterestRateSeriesDao`-backed offline cache (NETWORK_WITH_CACHE archetype). Previously in-memory only.

### `core/store/src/commonMain/kotlin/kpt/core/store/economic/impl/MacroIndicatorKey.kt`

```kotlin
data class MacroIndicatorKey(
```
Composite key identifying a single World Bank macro-indicator request.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroViewModel.kt:64</code></summary>

```kotlin

        TRACKED_INDICATORS.forEach { kind ->
            val key = MacroIndicatorKey(countryCode = countryCode, indicator = kind)
            val stream = repository.macroIndicatorStream(key = key, scope = viewModelScope)
            streams[kind] = stream
            // Reset the per-cell state to Loading on re-subscription so the
            // card visibly indicates "fetching for new country" instead of
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/economic/impl/MacroIndicatorStore.kt`

```kotlin
fun provideMacroIndicatorStore(
```
World Bank macro indicator for a (country, indicator) key — `NETWORK_WITH_CACHE` with a long TTL, since the underlying series updates at most quarterly. Build an in-memory `Store` for World Bank macro indicators.

### `core/store/src/commonMain/kotlin/kpt/core/store/emi/impl/EmiStore.kt`

```kotlin
data class EmiParams(
```
The Store key for a single EMI computation — the calculator's inputs. A calculator has no remote resource to key on, so the INPUTS are the key: two identical parameter sets are the same cache entry, and changing any field is a new entry.

<details><summary>Used in the template — <code>feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/ui/EmiCalculatorViewModel.kt:46</code></summary>

```kotlin
    @OptIn(ExperimentalCoroutinesApi::class)
    val emiState: StateFlow<ScreenState<EmiResult>> = stateFlow
        .map { EmiParams(it.principal, it.ratePercent, it.tenureMonths) }
        .distinctUntilChanged()
        .flatMapLatest { params ->
            if (params.isComputable) {
                repository.emiStream(params, viewModelScope)
```

</details>

```kotlin
fun interface EmiCompute
```
The compute PORT for the EMI store.

```kotlin
fun provideEmiStore(compute: EmiCompute): Store<EmiParams, EmiResult> =
```
EMI calculation — `MEMORY_ONLY` over a pure `core/domain` use-case. The parameter set IS the cache key, so a repeated calculation is served from cache and the result reaches the screen as a `ScreenState` like every other read surface.

### `core/store/src/commonMain/kotlin/kpt/core/store/exchange/impl/SpotRateLookupStore.kt`

```kotlin
fun provideSpotRateLookupStore(
```
Build a network-backed `Store` for spot (current) exchange-rate lookups.

### `core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt`

```kotlin
fun interface UserDataSource
```
The read PORT for the user-preferences store. The preferences themselves live in `core/datastore`, which `core/store` does not depend on (and must not — `core/datastore` sits beside it in the layer order, not below).

```kotlin
fun provideUserDataStore(source: UserDataSource): Store<Unit, UserData> =
```
OFFLINE_LOCAL_ONLY Store5 store over user preferences (`feature_profile.combo_id: local_only_prefs`).

### `core/store/src/commonMain/kotlin/kpt/core/store/profile/impl/ProfileStore.kt`

```kotlin
fun interface ProfileInfoSource
```
The read PORT for the profile store. The template's profile data is the app display name, which comes from `AppInfo` in `core-base/ui` — a Compose-side module `core/store` does not depend on.

```kotlin
fun provideProfileStore(source: ProfileInfoSource): Store<Unit, ProfileInfo> =
```
MEMORY_ONLY Store5 store for the profile screen (`feature_profile.combo_id: static_content`). The template's profile is a static local placeholder, so this store is deliberately thin — its value is the SEAM, not the caching.

### `core/store/src/commonMain/kotlin/kpt/core/store/watchlist/impl/WatchlistStore.kt`

```kotlin
fun provideWatchlistStore(dao: WatchlistDao): Store<Unit, List<WatchlistItem>> = StoreFactory.createOfflineStore(
```
Offline-only `Store` for the personal watchlist (`read_local_list` demo). Backed exclusively by `WatchlistDao` — no remote fetcher.

```kotlin
fun provideWatchlistWriteStore(dao: WatchlistDao): MutableStore<String, WatchlistItem> =
```
Per-item WRITE store for the watchlist (keyed by coin id).

---

_11 type(s), 41 function(s)/property(ies); 52 carry KDoc at source; 4 authored example(s); 11 live call site(s)._
<!-- api-docs:end -->
