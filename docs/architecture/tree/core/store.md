# `core/store`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_STORE.md`
> **Measured:** 86 Kotlin files, 6 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@StoreProvider` | `store-ksp` | config/AppStoreRegistry, config/AppStoreIds, di/GeneratedStoreBindings |
| `@CacheKey` | `store-ksp` | config/AppCacheKeys |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`CenterCollectionSheetKey`, `ChargeItemKey`, `ChargeKey`, `ClientCountKey`, `CollectionSheetKey`, `DataTableDataKey`, `DocumentContentKey`, `DocumentItemKey`, `DocumentKey`, `DueTodayKey`, `ErrorMessageOverrides`, `FixedDepositProductTemplateKey`, `ForkErrorMapper`, `ForkScreenStateDefaults`  …and 37 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/store sha=ad53f79b99d7e8945aeca9ed347e8a967f2c111e -->
## API reference

_Generated from `core/store` at tree `ad53f79b99d7` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/store/src/commonMain/kotlin/kpt/core/store/center/CenterKeyedReadStores.kt`

```kotlin
fun provideGetCenterAccountsStore(
```
Keyed reads for center with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

```kotlin
fun provideGetCenterWithGroupMembersAndCollectionMeetingCalendarStore(
```
Store reading the center with group members and collection meeting calendar.

```kotlin
fun provideGetAllGroupsForCenterStore(
```
Store reading all groups for center.

```kotlin
data class GetAllCentersInOfficeKey(
```
Addresses one `getAllCentersInOffice` read.

```kotlin
fun provideGetAllCentersInOfficeStore(
```
Store reading all centers in office.

```kotlin
data class CenterCollectionSheetKey(
```
Addresses one generated center collection sheet — every field Fineract's `?command=generateCollectionSheet` body carries, so a different meeting or date is a different sheet rather than the previous one served back.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/center/impl/CenterCommandRepositoryImpl.kt:60</code></summary>

```kotlin
    @FromStore(AppStoreIds.CenterItem) private val itemStore: MutableStore<Int, Center>,
    @FromStore(AppStoreIds.CenterCollectionSheet)
    private val sheetStore: Store<CenterCollectionSheetKey, CollectionSheet>,
) : CenterCommandRepository {

    /** A center state command (`close`, ...). The resulting status is the server's. */
    override suspend fun activate2(
```

</details>

```kotlin
fun provideCenterCollectionSheetStore(
```
A generated center sheet. POST-shaped, but a READ: the body only selects which sheet. Cached so `CenterCommandRepository` can record a saved sheet's outcome against the same row.

### `core/store/src/commonMain/kotlin/kpt/core/store/center/CenterStore.kt`

```kotlin
fun provideCenterStore(
```
Centers — READ-CACHE (`createStore`). A field officer opens the center list in the field, so this is the archetype that matters most for them: Room answers immediately from cache with no connectivity, and a reconnect refreshes.

```kotlin
fun provideCenterItemStore(
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/charge/ChargeKeyedReadStores.kt`

```kotlin
fun provideGetChargeTemplateStore(
```
Keyed reads for charge with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

### `core/store/src/commonMain/kotlin/kpt/core/store/charge/ChargeKeyedReadStores2.kt`

```kotlin
data class GetListOfPagingChargesKey(
```
Addresses one `getListOfPagingCharges` read.

```kotlin
data class GetListOfOtherAccountChargeKey(
```
Addresses one `getListOfOtherAccountCharge` read.

```kotlin
data class GetChargeKey(
```
Addresses one `getCharge` read.

```kotlin
fun provideGetListOfPagingChargesStore(
```
Store reading the list of paging charges.

```kotlin
fun provideGetListOfOtherAccountChargeStore(
```
Store reading the list of other account charge.

```kotlin
fun provideGetChargeStore(
```
Store reading the charge.

### `core/store/src/commonMain/kotlin/kpt/core/store/charge/ChargeStore.kt`

```kotlin
data class ChargeKey(val resourceType: String, val resourceId: Int)
```
The charges levied on one resource. `resourceType` is the Fineract path segment (`clients` / `loans` / `savingsaccounts`) — the same pair the endpoint and the table key on.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/charge/impl/ChargeCommandRepositoryImpl.kt:49</code></summary>

```kotlin
    private val chargeApi: ChargeApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Charges) private val charges: Store<ChargeKey, List<Charges>>,
    @FromStore(AppStoreIds.ChargeItem) private val chargeItem: MutableStore<ChargeItemKey, Charges>,
) : ChargeCommandRepository {

    override suspend fun createCharge(
```

</details>

```kotlin
data class ChargeItemKey(val resourceType: String, val resourceId: Int, val chargeId: Int)
```
One charge ROW — the owner plus the charge's id.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/charge/impl/ChargeCommandRepositoryImpl.kt:50</code></summary>

```kotlin
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Charges) private val charges: Store<ChargeKey, List<Charges>>,
    @FromStore(AppStoreIds.ChargeItem) private val chargeItem: MutableStore<ChargeItemKey, Charges>,
) : ChargeCommandRepository {

    override suspend fun createCharge(
        resource: ChargeResource,
```

</details>

```kotlin
fun provideChargeStore(
```
Charges on ANY resource — READ-CACHE (`createStore`), keyed by the owner. ONE store for clients, loans and savings, where there used to be a client store and a loan store (and nothing for savings).

```kotlin
fun provideChargeItemStore(
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/charge/GetChargeTemplateKey.kt`

```kotlin
data class GetChargeTemplateKey(
```
Addresses one `getChargeTemplate` read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/charge/impl/ChargeRepositoryImpl.kt:37</code></summary>

```kotlin
    @FromStore(AppStoreIds.Charges) private val store: Store<ChargeKey, List<Charges>>,
    @FromStore(AppStoreIds.ChargeGetChargeTemplate)
    private val templateStore: Store<GetChargeTemplateKey, ChargeTemplate>,
) : ChargeRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/checkerinbox/CheckerInboxCatalogStores.kt`

```kotlin
fun provideRescheduleLoanTasksStore(
```
Catalogue and template reads for checkerinbox — option lists and form templates with no table of their own, cached through the shared read cache so they still render with no signal.

```kotlin
fun provideCheckerSearchTemplateStore(
```
Store reading the checker search template.

### `core/store/src/commonMain/kotlin/kpt/core/store/checkerinbox/CheckerInboxKeyedReadStores.kt`

```kotlin
fun provideGetCheckerTasksFromResourceIdStore(
```
Keyed reads for checkerinbox with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

### `core/store/src/commonMain/kotlin/kpt/core/store/checkerinbox/CheckerInboxStore.kt`

```kotlin
fun provideCheckerInboxStore(
```
Maker-checker tasks awaiting approval. Unfiltered (all three query params null) because the screen filters client-side over the cached set — narrowing server-side would make the cache a partial view keyed by nothing.

```kotlin
fun provideCheckerTaskStore(
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/checkerinbox/GetCheckerTasksFromResourceIdKey.kt`

```kotlin
data class GetCheckerTasksFromResourceIdKey(
```
Addresses one `getCheckerTasksFromResourceId` read.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/checkerinbox/CheckerInboxKeyedReadStores.kt:31</code></summary>

```kotlin
    checkerInboxApi: CheckerInboxApi,
    cache: ApiResponseCacheDao,
): Store<GetCheckerTasksFromResourceIdKey, List<CheckerTask>> = cachedRead(
    dao = cache,
    keyOf = { key ->
        AppCacheKeys.CheckerinboxGetCheckerTasksFromResourceId.forKey(
            "${key.actionName}:${key.entityName}:${key.resourceId}",
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/client/ClientCatalogStores.kt`

```kotlin
fun provideAddressConfigurationStore(
```
Catalogue and template reads for client — option lists and form templates with no table of their own, cached through the shared read cache so they still render with no signal.

```kotlin
fun provideClientsTemplateStore(
```
The TENANT's client template — the gender / client-type / classification / legal-form code values a client form offers, plus its configured data tables.

```kotlin
fun provideAddressTemplateStore(
```
Store reading the address template.

```kotlin
fun provideClientCloseTemplateStore(
```
Store reading the client close template.

```kotlin
fun provideCollateralItemsStore(
```
Store reading the collateral items.

### `core/store/src/commonMain/kotlin/kpt/core/store/client/ClientKeyedReadStores.kt`

```kotlin
fun provideRetrieveAssociatedAccountsStore(
```
Keyed reads for client with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

```kotlin
fun provideGetClientStore(
```
Store reading the client.

```kotlin
fun provideGetClientPinpointLocationsStore(
```
Store reading the client pinpoint locations.

```kotlin
fun provideGetClientTemplateStore(
```
Store reading the client template.

```kotlin
fun provideGetClientCollateralItemsStore(
```
Store reading the client collateral items.

```kotlin
fun provideGetClientListIdentifiersStore(
```
Store reading the client list identifiers.

```kotlin
fun provideGetClientIdentifiersStore(
```
Store reading the client identifiers.

```kotlin
fun provideGetClientIdentifierTemplateStore(
```
Store reading the client identifier template.

```kotlin
fun provideGetAllAccountsOfClientStore(
```
Store reading all accounts of client.

```kotlin
fun provideGetClientAccountsStore(
```
Store reading the client accounts.

```kotlin
fun provideGetClientAddressesStore(
```
Store reading the client addresses.

### `core/store/src/commonMain/kotlin/kpt/core/store/client/ClientLocalStores.kt`

```kotlin
fun provideClientImageStore(
```
The client profile photo: `GET clients/{id}/images` → the shared read cache → this store → `ClientRepository.clientImage`.

```kotlin
data class PinpointItemKey(val clientId: Int, val datatableId: Int)
```
One pinpoint row — the client plus the datatable row's id.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/impl/ClientCommandRepositoryImpl.kt:85</code></summary>

```kotlin
    private val pinpointStore: Store<Int, List<ClientAddressResponse>>,
    @FromStore(AppStoreIds.ClientPinpointItem)
    private val pinpointItemStore: MutableStore<PinpointItemKey, ClientAddressResponse>,
    @FromStore(AppStoreIds.ClientGetClientAddresses)
    private val addressesStore: Store<Int, List<ClientAddressEntity>>,
    @FromStore(AppStoreIds.ClientGetClientCollateralItems)
    private val collateralStore: Store<Int, List<CollateralItemResult>>,
```

</details>

```kotlin
data class IdentifierItemKey(val clientId: Long, val identifierId: Long)
```
One identifier row — the client plus the identifier's id.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/impl/ClientCommandRepositoryImpl.kt:93</code></summary>

```kotlin
    private val identifiersStore: Store<Long, List<Identifier>>,
    @FromStore(AppStoreIds.ClientIdentifierItem)
    private val identifierItemStore: MutableStore<IdentifierItemKey, Identifier>,
) : ClientCommandRepository {

    override suspend fun activate1(
        clientId: Long,
```

</details>

```kotlin
fun provideClientLocalStore(
```
_No KDoc at source._

```kotlin
fun provideClientPinpointItemStore(
```
_No KDoc at source._

```kotlin
fun provideClientIdentifierItemStore(
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/client/ClientStore.kt`

```kotlin
fun provideClientStore(
```
Clients — READ-CACHE (`createStore`). The app's hub entity: most other features are reached THROUGH a client, so this is the store a field officer depends on most when offline.

```kotlin
fun provideClientRowStore(
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/client/GetClientIdentifiersKey.kt`

```kotlin
data class GetClientIdentifiersKey(
```
Addresses one `getClientIdentifiers` read.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/client/ClientKeyedReadStores.kt:129</code></summary>

```kotlin
    clientIdentifierApi: ClientIdentifierApi,
    cache: ApiResponseCacheDao,
): Store<GetClientIdentifiersKey, Identifier> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetClientIdentifiers.forKey("${key.clientId}:${key.identifierId}") },
    fetch = { key ->
        clientIdentifierApi.getClientIdentifiers(
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/collectionsheet/CollectionSheetKey.kt`

```kotlin
data class CollectionSheetKey(
```
The full query is the key: a centre sheet is only meaningful for one (office, staff, meeting date) triple, so date or staff changing must re-fetch rather than serve the previous sheet.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/impl/CollectionSheetRepositoryImpl.kt:35</code></summary>

```kotlin
internal class CollectionSheetRepositoryImpl(
    @FromStore(AppStoreIds.CollectionSheetCenters)
    private val store: Store<CollectionSheetKey, List<CenterDetail>>,
    @FromStore(AppStoreIds.CollectionsheetIndividual)
    private val individualStore: Store<RequestCollectionSheetPayload, IndividualCollectionSheet>,
    @FromStore(AppStoreIds.CollectionsheetProductive)
    private val productiveStore: Store<GeneratedSheetKey, CollectionSheetResponse>,
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/collectionsheet/CollectionSheetKeyedReadStores.kt`

```kotlin
fun provideFetchGroupsAssociatedWithCenterStore(
```
Keyed reads for collectionsheet with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

```kotlin
data class GeneratedSheetKey(val ownerId: Int, val request: CollectionSheetRequestPayload)
```
One generated sheet: the owning center/group plus the request that selects the meeting.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/impl/CollectionSheetCommandRepositoryImpl.kt:53</code></summary>

```kotlin
    private val individualStore: Store<RequestCollectionSheetPayload, IndividualCollectionSheet>,
    @FromStore(AppStoreIds.CollectionsheetProductive)
    private val productiveStore: Store<GeneratedSheetKey, CollectionSheetResponse>,
    @FromStore(AppStoreIds.CollectionsheetGroup)
    private val groupStore: Store<GeneratedSheetKey, CollectionSheetResponse>,
) : CollectionSheetCommandRepository {
```

</details>

```kotlin
fun provideIndividualCollectionSheetStore(
```
Store reading the individual collection sheet.

```kotlin
fun provideProductiveCollectionSheetStore(
```
Store reading the productive collection sheet.

```kotlin
fun provideGroupCollectionSheetStore(
```
Store reading the group collection sheet.

### `core/store/src/commonMain/kotlin/kpt/core/store/collectionsheet/CollectionSheetStore.kt`

```kotlin
fun provideCollectionSheetStore(
```
Store reading the collection sheet.

### `core/store/src/commonMain/kotlin/kpt/core/store/config/ForkErrorMapper.kt`

```kotlin
object ForkErrorMapper : ErrorMessageOverrides
```
THE FORK'S domain-error copy. Neutral on the template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
object ForkErrorMapper : ErrorMessageOverrides {
    override fun message(error: Throwable): String? = when (error) {
        is InsufficientFundsException -> "Not enough balance for this transfer."
        is CardDeclinedException -> "That card was declined. Try another payment method."
        else -> null
    }
}
```

</details>

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptErrorMapper.kt:101</code></summary>

```kotlin
    val clientCopy = stringResource(Res.string.error_category_client)
    val genericCopy = stringResource(Res.string.error_category_generic)
    val projectCopy = ForkErrorMapper::message
    return remember(
        networkCopy,
        connectTimeoutCopy,
        readTimeoutCopy,
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/config/ForkScreenStateDefaults.kt`

```kotlin
object ForkScreenStateDefaults : ScreenStateOverrides
```
THE FORK'S branding for the shared empty / error / no-network / loading visuals. Neutral on the template — this is yours to fill. Implements the TEMPLATE-owned `ScreenStateOverrides`.

<details><summary>Example</summary>

```kotlin
object ForkScreenStateDefaults : ScreenStateOverrides {
    override fun customize(defaults: ScreenStateDefaults): ScreenStateDefaults = defaults.copy(
        empty = defaults.empty.copy(visual = ScreenStateVisual.Lottie(spec = MyBrandAnimations.empty)),
        error = defaults.error.copy(onShown = { e -> AppTelemetry.recordError("screen_state_error", e) }),
    )
}
```

</details>

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt:102</code></summary>

```kotlin
                retryText = nonetRetry,
            ),
        ).let(ForkScreenStateDefaults::customize)
    }
}

/**
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/config/KptErrorMapper.kt`

```kotlin
fun errorCategoryToken(error: Throwable): String = when (val cat = categorize(error))
```
Application-level error → user-facing message mapper. ONE source of user-facing copy: `rememberAppErrorMessageFor`, which resolves every `ErrorCategory` through `stringResource(...)`.

```kotlin
interface ErrorMessageOverrides
```
The fork's error-copy extension point, declared HERE so it is TEMPLATE-owned and full-copied by every sync. `ForkErrorMapper` implements it and is the only fork-owned half.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/ForkErrorMapper.kt:52</code></summary>

```kotlin
 * stop refetching. Declare the key on its store instead.
 */
object ForkErrorMapper : ErrorMessageOverrides {
    /**
     * Fineract's own sentence when the failure carries one ("Client with identifier 2 does not
     * exist"), so a read error says what the server said rather than the category's generic copy.
     * Null for everything else — no network, timeouts, a 500 without a Fineract body — and the
```

</details>

- `fun message(error: Throwable): String? = null` — Fork copy for `error`, or null to fall through to the framework's categorised message.

```kotlin
fun rememberAppErrorMessageFor(): (Throwable) -> String
```
Composable factory that resolves per-category copy via `stringResource(...)`, returning a pure-Kotlin `(Throwable) -> String` lambda safe to pass through to `kpt.core.base.ui.screen.ScreenStateError.messageFor` (which is invoked from inside composition where these strings are already memoised). Reuses `categorize` to bucket the error, then picks the localized string.

### `core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt`

```kotlin
fun appScreenStateDefaults(): ScreenStateDefaults
```
TEMPLATE-OWNED framework defaults. Brand your app in `ForkScreenStateDefaults` instead — do NOT edit this file. It used to say "!!

```kotlin
interface ScreenStateOverrides
```
The fork's screen-state branding extension point, declared HERE so it is TEMPLATE-owned and full-copied by every sync. `ForkScreenStateDefaults` implements it and is the only fork-owned half.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/ForkScreenStateDefaults.kt:39</code></summary>

```kotlin
 * that introduces it.
 */
object ForkScreenStateDefaults : ScreenStateOverrides
```

</details>

- `fun customize(defaults: ScreenStateDefaults): ScreenStateDefaults = defaults` — Return `defaults` with the fork's branding applied, or `defaults` unchanged.

### `core/store/src/commonMain/kotlin/kpt/core/store/dashboard/DashboardStores.kt`

```kotlin
data class ClientCountKey(val officeId: Long, val status: String)
```
Key of a client-count read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/dashboard/impl/DashboardRepositoryImpl.kt:38</code></summary>

```kotlin
    @FromStore(AppStoreIds.DashboardBusinessDate) private val businessDateStore: Store<Unit, String>,
    @FromStore(AppStoreIds.DashboardLoans) private val loansStore: Store<Int, List<DashboardLoan>>,
    @FromStore(AppStoreIds.DashboardClientCount) private val clientCountStore: Store<ClientCountKey, Int>,
    @FromStore(AppStoreIds.DashboardSavings) private val savingsStore: Store<Unit, List<DashboardSavings>>,
    @FromStore(AppStoreIds.DashboardDueToday) private val dueTodayStore: Store<DueTodayKey, List<DueTodayLoan>>,
) : DashboardRepository {
```

</details>

```kotlin
data class DueTodayKey(val date: String, val loanIds: List<Long>)
```
The business date (`yyyy-MM-dd`) and the officer's active loans to read the schedules of.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/dashboard/impl/DashboardRepositoryImpl.kt:40</code></summary>

```kotlin
    @FromStore(AppStoreIds.DashboardClientCount) private val clientCountStore: Store<ClientCountKey, Int>,
    @FromStore(AppStoreIds.DashboardSavings) private val savingsStore: Store<Unit, List<DashboardSavings>>,
    @FromStore(AppStoreIds.DashboardDueToday) private val dueTodayStore: Store<DueTodayKey, List<DueTodayLoan>>,
) : DashboardRepository {

    override fun userStream(userId: Long, scope: CoroutineScope): ScreenDataStream<DashboardUser> =
        userStore.asScreenStream(
```

</details>

```kotlin
fun provideDashboardUserStore(
```
Store reading the dashboard user.

```kotlin
fun provideDashboardBusinessDateStore(
```
Store reading the dashboard business date.

```kotlin
fun provideDashboardLoansStore(
```
Store reading the dashboard loans.

```kotlin
fun provideDashboardClientCountStore(
```
Store reading the dashboard client count.

```kotlin
fun provideDashboardDueTodayStore(
```
_No KDoc at source._

```kotlin
fun provideDashboardSavingsStore(
```
Store reading the dashboard savings.

### `core/store/src/commonMain/kotlin/kpt/core/store/datatable/DataTableKeyedReadStores.kt`

```kotlin
data class GetDatatablesKey(
```
Addresses one `getDatatables` read.

```kotlin
fun provideGetDatatablesStore(
```
Store reading the datatables.

```kotlin
fun provideGetUserPathTrackingStore(
```
Store reading the user path tracking.

```kotlin
data class DataTableDataKey(
```
Addresses one `getDataOfDataTable` read — a datatable's ROWS for one entity.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/datatable/impl/DataTableCommandRepositoryImpl.kt:49</code></summary>

```kotlin
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.DatatableGetDataOfDataTable)
    private val rowsStore: Store<DataTableDataKey, JsonArray>,
    @FromStore(AppStoreIds.DatatableGetUserPathTracking)
    private val pathStore: Store<Int, List<UserLocation>>,
) : DataTableCommandRepository {
```

</details>

```kotlin
fun provideGetDataOfDataTableStore(
```
A datatable's rows for one entity. The output is a raw `JsonArray` because a datatable's SHAPE is tenant-defined — the columns are configured per deployment, so there is no DTO to deserialize into.

### `core/store/src/commonMain/kotlin/kpt/core/store/datatable/DataTableStore.kt`

```kotlin
fun provideDataTableStore(
```
Registered datatables for one application table ("m_client", "m_loan", ...). The key is stamped onto each row on write: it is the column the cache is read back by, and Fineract does not reliably echo it on every row.

### `core/store/src/commonMain/kotlin/kpt/core/store/di/ForkStoreModule.kt`

```kotlin
val ForkStoreModule = module
```
THE FORK'S store-layer DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
@StoreProvider(id = "myThing", ttl = "5m")
@CacheKey(name = "LIST", key = "myThing")
fun provideMyThingStore(api: MyApi, dao: MyDao): Store<Unit, List<MyThing>> = …
val ForkStoreModule = module {
    single<DraftInventory> { MyForkDraftInventory(draftDao = get(), audit = get()) }
}
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/di/KptStoreModule.kt`

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

### `core/store/src/commonMain/kotlin/kpt/core/store/document/DocumentStore.kt`

```kotlin
data class DocumentKey(val entityType: String, val entityId: Int)
```
Documents hang off any entity (client, loan, group, ...), so the owner pair is the key.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addidentifier/ClientAddIdentifierViewModel.kt:224</code></summary>

```kotlin
                // step one. The document's description is how the identifiers list FINDS this scan
                // again (`identifierDocumentKey`); writing anything else here stranded it.
                description = identifierDocumentKey(
                    documentTypeName = state.documentTypeLabel,
                    documentKey = state.documentKey.trim(),
                    status = getClientIdentifierStatus(state.status.trim().ifEmpty { null }),
                ),
```

</details>

```kotlin
data class DocumentContentKey(val entityType: String, val entityId: Int, val documentId: Int)
```
One document's FILE — the owner pair plus the document's id.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/document/impl/DocumentCommandRepositoryImpl.kt:49</code></summary>

```kotlin
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.DocumentItem) private val itemStore: MutableStore<DocumentItemKey, Document>,
    @FromStore(AppStoreIds.DocumentContentLocal) private val contentStore: MutableStore<DocumentContentKey, ByteArray>,
) : DocumentCommandRepository {

    override suspend fun createDocument(entityType: String, entityId: Int, upload: DocumentUpload) =
        gateway.command(
```

</details>

```kotlin
data class DocumentItemKey(val entityType: String, val entityId: Int, val documentId: Int)
```
One document ROW — the owner pair plus the document's id.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/document/impl/DocumentCommandRepositoryImpl.kt:48</code></summary>

```kotlin
    private val documentApi: DocumentApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.DocumentItem) private val itemStore: MutableStore<DocumentItemKey, Document>,
    @FromStore(AppStoreIds.DocumentContentLocal) private val contentStore: MutableStore<DocumentContentKey, ByteArray>,
) : DocumentCommandRepository {

    override suspend fun createDocument(entityType: String, entityId: Int, upload: DocumentUpload) =
```

</details>

```kotlin
fun provideDocumentStore(
```
Store reading the document.

```kotlin
fun provideDocumentContentStore(
```
A document's file bytes: `DocumentApi.downloadDocument` → `document_contents` → this store → `DocumentRepository.downloadDocument`. Cached so a document viewed once can be reopened offline.

```kotlin
fun provideDocumentItemStore(
```
_No KDoc at source._

```kotlin
fun provideDocumentContentLocalStore(
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/group/GetAllGroupsInOfficeKey.kt`

```kotlin
data class GetAllGroupsInOfficeKey(
```
Addresses one `getAllGroupsInOffice` read.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/group/GroupKeyedReadStores.kt:45</code></summary>

```kotlin
    groupApi: GroupApi,
    cache: ApiResponseCacheDao,
): Store<GetAllGroupsInOfficeKey, List<Group>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.GroupGetAllGroupsInOffice.forKey("${key.officeId}:${key.params}") },
    fetch = { key -> groupApi.getAllGroupsInOffice(officeId = key.officeId, params = key.params) },
)
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/group/GroupKeyedReadStores.kt`

```kotlin
fun provideGetGroupWithAssociationsStore(
```
Keyed reads for group with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

```kotlin
fun provideGetAllGroupsInOfficeStore(
```
Store reading all groups in office.

```kotlin
fun provideGetGroupStore(
```
Store reading the group.

```kotlin
fun provideGetGroupAccountsStore(
```
Store reading the group accounts.

### `core/store/src/commonMain/kotlin/kpt/core/store/group/GroupStore.kt`

```kotlin
fun provideGroupStore(
```
Groups — READ-CACHE (`createStore`). Same shape as centers; groups sit under a center in the Fineract hierarchy and are read on the same offline paths.

```kotlin
fun provideGroupItemStore(
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/loan/LoanAccountMapper.kt`

```kotlin
object LoanAccountMapper : AbstractMapper<LoanWithAssociationsEntity, LoanWithAssociations>()
```
Maps a stored loan with associations to its domain model.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/loan/LoanStore.kt:40</code></summary>

```kotlin
    sourceOfTruth = SourceOfTruth.of(
        reader = { loanId: Int ->
            dao.getLoanById(loanId).map { row -> row?.let(LoanAccountMapper::mapFromEntity) }
        },
        writer = { _: Int, dto: LoanWithAssociationsDto ->
            dao.saveLoanWithAssociations(LoanAccountMapper.mapToEntity(dto.toDomain()))
        },
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/loan/LoanCatalogStores.kt`

```kotlin
fun provideLoanProductsStore(
```
Catalogue and template reads for loan — option lists and form templates with no table of their own, cached through the shared read cache so they still render with no signal.

```kotlin
fun provideLoanRescheduleTemplateStore(
```
Store reading the loan reschedule template.

### `core/store/src/commonMain/kotlin/kpt/core/store/loan/LoanKeyedReadStores.kt`

```kotlin
data class GetGuarantorAccountTemplateKey(
```
Addresses one `getGuarantorAccountTemplate` read.

```kotlin
data class GetLoanTransactionTemplateKey(
```
Addresses one `getLoanTransactionTemplate` read.

```kotlin
data class GetGroupLoansAccountTemplateKey(
```
Addresses one `getGroupLoansAccountTemplate` read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/impl/LoanRepositoryImpl.kt:52</code></summary>

```kotlin
    @FromStore(AppStoreIds.LoanProducts) private val loanProductsStore: Store<Unit, List<LoanProducts>>,
    @FromStore(AppStoreIds.LoanGetGroupLoansAccountTemplate)
    private val groupLoanTemplateStore: Store<GetGroupLoansAccountTemplateKey, GroupLoanTemplate>,
) : LoanRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
```

</details>

```kotlin
data class GetAccountTransferTemplateKey(
```
Addresses one `getAccountTransferTemplate` read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/impl/LoanRepositoryImpl.kt:45</code></summary>

```kotlin
    @FromStore(AppStoreIds.LoanDisburseTemplates) private val disburseTemplateStore: Store<Int, LoanDisburseTemplate>,
    @FromStore(AppStoreIds.LoanGetAccountTransferTemplate)
    private val transferTemplateStore: Store<GetAccountTransferTemplateKey, AccountTransferTemplate>,
    @FromStore(AppStoreIds.LoanGetLoanReschedules)
    private val reschedulesStore: Store<Int, List<LoanRescheduleResponse>>,
    @FromStore(AppStoreIds.LoanRescheduleTemplate)
    private val rescheduleTemplateStore: Store<Unit, LoanRescheduleTemplate>,
```

</details>

```kotlin
fun provideGetChargeOffTemplateStore(
```
Store reading the charge off template.

```kotlin
fun provideGetLoanRepaymentScheduleStore(
```
Store reading the loan repayment schedule.

```kotlin
fun provideGetLoanWithTransactionsStore(
```
Store reading the loan with transactions.

```kotlin
fun provideGetGuarantorTemplateStore(
```
Store reading the guarantor template.

```kotlin
fun provideGetGuarantorAccountTemplateStore(
```
Store reading the guarantor account template.

```kotlin
fun provideGetLoanTransactionTemplateStore(
```
Store reading the loan transaction template.

```kotlin
fun provideGetGroupLoansAccountTemplateStore(
```
Store reading the group loans account template.

```kotlin
fun provideGetAccountTransferTemplateStore(
```
Store reading the account transfer template.

```kotlin
fun provideGetLoanReschedulesStore(
```
Store reading the loan reschedules.

```kotlin
fun provideGetLoanOfficerTemplateStore(
```
Store reading the loan officer template.

### `core/store/src/commonMain/kotlin/kpt/core/store/loan/LoanKeyedReadStores2.kt`

```kotlin
fun provideGetListOfLoanChargesStore(
```
Keyed reads for loan with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

```kotlin
fun provideGetListOfChargesStore(
```
Store reading the list of charges.

### `core/store/src/commonMain/kotlin/kpt/core/store/loan/LoanStore.kt`

```kotlin
fun provideLoanStore(
```
The one place the loan's three representations meet: the wire DTO (175 fields), the Room entity (56 — the subset worth persisting) and the domain model the UI reads.

### `core/store/src/commonMain/kotlin/kpt/core/store/loan/LoanTemplateStores.kt`

```kotlin
data class LoanTemplateKey(val clientId: Int, val productId: Int?)
```
A loan application template is only valid for one (client, product) pair.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/impl/LoanRepositoryImpl.kt:40</code></summary>

```kotlin
internal class LoanRepositoryImpl(
    @FromStore(AppStoreIds.Loans) private val store: Store<Int, LoanWithAssociations>,
    @FromStore(AppStoreIds.LoanTemplates) private val templateStore: Store<LoanTemplateKey, LoanTemplate>,
    @FromStore(AppStoreIds.LoanRepaymentTemplates)
    private val repaymentTemplateStore: Store<Int, LoanRepaymentTemplate>,
    @FromStore(AppStoreIds.LoanDisburseTemplates) private val disburseTemplateStore: Store<Int, LoanDisburseTemplate>,
    @FromStore(AppStoreIds.LoanGetAccountTransferTemplate)
```

</details>

```kotlin
fun provideLoanTemplateStore(
```
The loan-application template — what the "new loan" form renders from.

```kotlin
fun provideLoanRepaymentTemplateStore(
```
The repayment template — the payment-type options a repayment form needs, cached per loan.

```kotlin
fun provideLoanDisburseTemplateStore(
```
The disbursement template — cached so a disbursement can be prepared offline and queued.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/CenterMappers.kt`

```kotlin
fun CenterDateRow.toModel(): CenterDate = CenterDate(
```
Maps the stored row to its `CenterDate` model.

```kotlin
fun CenterDate.toEntity(): CenterDateRow = CenterDateRow(
```
Maps the model to its stored `CenterDateRow`.

```kotlin
fun CenterRow.toModel(): Center = Center(
```
Maps the stored row to its `Center` model.

```kotlin
fun Center.toEntity(): CenterRow = CenterRow(
```
Maps the model to its stored `CenterRow`.

```kotlin
fun CenterPayloadRow.toModel(): CenterPayload = CenterPayload(
```
Maps the stored row to its `CenterPayload` model.

```kotlin
fun CenterPayload.toEntity(): CenterPayloadRow = CenterPayloadRow(
```
Maps the model to its stored `CenterPayloadRow`.

```kotlin
fun CenterDetailRow.toModel(): CenterDetail = CenterDetail(
```
Maps the stored row to its `CenterDetail` model.

```kotlin
fun CenterDetail.toEntity(): CenterDetailRow = CenterDetailRow(
```
Maps the model to its stored `CenterDetailRow`.

```kotlin
fun CenterAccountsRow.toModel(): CenterAccounts = CenterAccounts(
```
Maps the stored row to its `CenterAccounts` model.

```kotlin
fun CenterAccounts.toEntity(): CenterAccountsRow = CenterAccountsRow(
```
Maps the model to its stored `CenterAccountsRow`.

```kotlin
fun CenterWithAssociationsRow.toModel(): CenterWithAssociations = CenterWithAssociations(
```
Maps the stored row to its `CenterWithAssociations` model.

```kotlin
fun CenterWithAssociations.toEntity(): CenterWithAssociationsRow = CenterWithAssociationsRow(
```
Maps the model to its stored `CenterWithAssociationsRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/ChargeMappers.kt`

```kotlin
fun ChargeRow.toModel(): Charge = Charge(
```
Maps the stored row to its `Charge` model.

```kotlin
fun Charge.toEntity(): ChargeRow = ChargeRow(
```
Maps the model to its stored `ChargeRow`.

```kotlin
fun ChargeCalculationTypeRow.toModel(): ChargeCalculationType = ChargeCalculationType(
```
Maps the stored row to its `ChargeCalculationType` model.

```kotlin
fun ChargeCalculationType.toEntity(): ChargeCalculationTypeRow = ChargeCalculationTypeRow(
```
Maps the model to its stored `ChargeCalculationTypeRow`.

```kotlin
fun ChargeTimeTypeRow.toModel(): ChargeTimeType = ChargeTimeType(
```
Maps the stored row to its `ChargeTimeType` model.

```kotlin
fun ChargeTimeType.toEntity(): ChargeTimeTypeRow = ChargeTimeTypeRow(
```
Maps the model to its stored `ChargeTimeTypeRow`.

```kotlin
fun ChargesRow.toModel(): Charges = Charges(
```
Maps the stored row to its `Charges` model.

```kotlin
fun Charges.toEntity(): ChargesRow = ChargesRow(
```
Maps the model to its stored `ChargesRow`.

```kotlin
fun PaymentTypeOptionRow.toModel(): PaymentTypeOption = PaymentTypeOption(
```
Maps the stored row to its `PaymentTypeOption` model.

```kotlin
fun PaymentTypeOption.toEntity(): PaymentTypeOptionRow = PaymentTypeOptionRow(
```
Maps the model to its stored `PaymentTypeOptionRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/ClientMappers.kt`

```kotlin
fun ClientAccountsRow.toModel(): ClientAccounts = ClientAccounts(
```
Maps the stored row to its `ClientAccounts` model.

```kotlin
fun ClientAccounts.toEntity(): ClientAccountsRow = ClientAccountsRow(
```
Maps the model to its stored `ClientAccountsRow`.

```kotlin
fun ClientChargeCurrencyRow.toModel(): ClientChargeCurrency = ClientChargeCurrency(
```
Maps the stored row to its `ClientChargeCurrency` model.

```kotlin
fun ClientChargeCurrency.toEntity(): ClientChargeCurrencyRow = ClientChargeCurrencyRow(
```
Maps the model to its stored `ClientChargeCurrencyRow`.

```kotlin
fun ClientDateRow.toModel(): ClientDate = ClientDate(
```
Maps the stored row to its `ClientDate` model.

```kotlin
fun ClientDate.toEntity(): ClientDateRow = ClientDateRow(
```
Maps the model to its stored `ClientDateRow`.

```kotlin
fun ClientRow.toModel(): Client = Client(
```
Maps the stored row to its `Client` model.

```kotlin
fun Client.toEntity(): ClientRow = ClientRow(
```
Maps the model to its stored `ClientRow`.

```kotlin
fun ClientPayloadRow.toModel(): ClientPayload = ClientPayload(
```
Maps the stored row to its `ClientPayload` model.

```kotlin
fun ClientPayload.toEntity(): ClientPayloadRow = ClientPayloadRow(
```
Maps the model to its stored `ClientPayloadRow`.

```kotlin
fun ClientStatusRow.toModel(): ClientStatus = ClientStatus(
```
Maps the stored row to its `ClientStatus` model.

```kotlin
fun ClientStatus.toEntity(): ClientStatusRow = ClientStatusRow(
```
Maps the model to its stored `ClientStatusRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/ClientTemplateMappers.kt`

```kotlin
fun AddressTemplateRow.toModel(): AddressTemplate = AddressTemplate(
```
Maps the stored row to its `AddressTemplate` model.

```kotlin
fun AddressTemplate.toEntity(): AddressTemplateRow = AddressTemplateRow(
```
Maps the model to its stored `AddressTemplateRow`.

```kotlin
fun ClientClassificationRow.toModel(): ClientClassification = ClientClassification(
```
Maps the stored row to its `ClientClassification` model.

```kotlin
fun ClientClassification.toEntity(): ClientClassificationRow = ClientClassificationRow(
```
Maps the model to its stored `ClientClassificationRow`.

```kotlin
fun ClientGenderRow.toModel(): ClientGender = ClientGender(
```
Maps the stored row to its `ClientGender` model.

```kotlin
fun ClientGender.toEntity(): ClientGenderRow = ClientGenderRow(
```
Maps the model to its stored `ClientGenderRow`.

```kotlin
fun ClientTypeRow.toModel(): ClientType = ClientType(
```
Maps the stored row to its `ClientType` model.

```kotlin
fun ClientType.toEntity(): ClientTypeRow = ClientTypeRow(
```
Maps the model to its stored `ClientTypeRow`.

```kotlin
fun ClientsTemplateRow.toModel(): ClientsTemplate = ClientsTemplate(
```
Maps the stored row to its `ClientsTemplate` model.

```kotlin
fun ClientsTemplate.toEntity(): ClientsTemplateRow = ClientsTemplateRow(
```
Maps the model to its stored `ClientsTemplateRow`.

```kotlin
fun AddressConfigurationRow.toModel(): AddressConfiguration = AddressConfiguration(
```
Maps the stored row to its `AddressConfiguration` model.

```kotlin
fun AddressConfiguration.toEntity(): AddressConfigurationRow = AddressConfigurationRow(
```
Maps the model to its stored `AddressConfigurationRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/CollectionSheetMappers.kt`

```kotlin
fun CollectionSheetResponseRow.toModel(): CollectionSheetResponse = CollectionSheetResponse(
```
Maps the stored row to its `CollectionSheetResponse` model.

```kotlin
fun CollectionSheetResponse.toEntity(): CollectionSheetResponseRow = CollectionSheetResponseRow(
```
Maps the model to its stored `CollectionSheetResponseRow`.

```kotlin
fun IndividualCollectionSheetRow.toModel(): IndividualCollectionSheet = IndividualCollectionSheet(
```
Maps the stored row to its `IndividualCollectionSheet` model.

```kotlin
fun IndividualCollectionSheet.toEntity(): IndividualCollectionSheetRow = IndividualCollectionSheetRow(
```
Maps the model to its stored `IndividualCollectionSheetRow`.

```kotlin
fun GroupCollectionSheetRow.toModel(): GroupCollectionSheet = GroupCollectionSheet(
```
Maps the stored row to its `GroupCollectionSheet` model.

```kotlin
fun GroupCollectionSheet.toEntity(): GroupCollectionSheetRow = GroupCollectionSheetRow(
```
Maps the model to its stored `GroupCollectionSheetRow`.

```kotlin
fun ClientCollectionSheetRow.toModel(): ClientCollectionSheet = ClientCollectionSheet(
```
Maps the stored row to its `ClientCollectionSheet` model.

```kotlin
fun ClientCollectionSheet.toEntity(): ClientCollectionSheetRow = ClientCollectionSheetRow(
```
Maps the model to its stored `ClientCollectionSheetRow`.

```kotlin
fun SavingsCollectionSheetRow.toModel(): SavingsCollectionSheet = SavingsCollectionSheet(
```
Maps the stored row to its `SavingsCollectionSheet` model.

```kotlin
fun SavingsCollectionSheet.toEntity(): SavingsCollectionSheetRow = SavingsCollectionSheetRow(
```
Maps the model to its stored `SavingsCollectionSheetRow`.

```kotlin
fun MeetingFallCalendarRow.toModel(): MeetingFallCalendar = MeetingFallCalendar(
```
Maps the stored row to its `MeetingFallCalendar` model.

```kotlin
fun MeetingFallCalendar.toEntity(): MeetingFallCalendarRow = MeetingFallCalendarRow(
```
Maps the model to its stored `MeetingFallCalendarRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/DataTableMappers.kt`

```kotlin
fun ColumnHeaderRow.toModel(): ColumnHeader = ColumnHeader(
```
Maps the stored row to its `ColumnHeader` model.

```kotlin
fun ColumnHeader.toEntity(): ColumnHeaderRow = ColumnHeaderRow(
```
Maps the model to its stored `ColumnHeaderRow`.

```kotlin
fun ColumnValueRow.toModel(): ColumnValue = ColumnValue(
```
Maps the stored row to its `ColumnValue` model.

```kotlin
fun ColumnValue.toEntity(): ColumnValueRow = ColumnValueRow(
```
Maps the model to its stored `ColumnValueRow`.

```kotlin
fun DataTableRow.toModel(): DataTable = DataTable(
```
Maps the stored row to its `DataTable` model.

```kotlin
fun DataTable.toEntity(): DataTableRow = DataTableRow(
```
Maps the model to its stored `DataTableRow`.

```kotlin
fun DataTablePayloadRow.toModel(): DataTablePayload = DataTablePayload(
```
Maps the stored row to its `DataTablePayload` model.

```kotlin
fun DataTablePayload.toEntity(): DataTablePayloadRow = DataTablePayloadRow(
```
Maps the model to its stored `DataTablePayloadRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/GroupMappers.kt`

```kotlin
fun GroupDateRow.toModel(): GroupDate = GroupDate(
```
Maps the stored row to its `GroupDate` model.

```kotlin
fun GroupDate.toEntity(): GroupDateRow = GroupDateRow(
```
Maps the model to its stored `GroupDateRow`.

```kotlin
fun GroupRow.toModel(): Group = Group(
```
Maps the stored row to its `Group` model.

```kotlin
fun Group.toEntity(): GroupRow = GroupRow(
```
Maps the model to its stored `GroupRow`.

```kotlin
fun GroupPayloadRow.toModel(): GroupPayload = GroupPayload(
```
Maps the stored row to its `GroupPayload` model.

```kotlin
fun GroupPayload.toEntity(): GroupPayloadRow = GroupPayloadRow(
```
Maps the model to its stored `GroupPayloadRow`.

```kotlin
fun GroupAccountsRow.toModel(): GroupAccounts = GroupAccounts(
```
Maps the stored row to its `GroupAccounts` model.

```kotlin
fun GroupAccounts.toEntity(): GroupAccountsRow = GroupAccountsRow(
```
Maps the model to its stored `GroupAccountsRow`.

```kotlin
fun GroupWithAssociationsRow.toModel(): GroupWithAssociations = GroupWithAssociations(
```
Maps the stored row to its `GroupWithAssociations` model.

```kotlin
fun GroupWithAssociations.toEntity(): GroupWithAssociationsRow = GroupWithAssociationsRow(
```
Maps the model to its stored `GroupWithAssociationsRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/LoanMappers.kt`

```kotlin
fun ActualDisbursementDateRow.toModel(): ActualDisbursementDate = ActualDisbursementDate(
```
Maps the stored row to its `ActualDisbursementDate` model.

```kotlin
fun ActualDisbursementDate.toEntity(): ActualDisbursementDateRow = ActualDisbursementDateRow(
```
Maps the model to its stored `ActualDisbursementDateRow`.

```kotlin
fun LoanAccountRow.toModel(): LoanAccount = LoanAccount(
```
Maps the stored row to its `LoanAccount` model.

```kotlin
fun LoanAccount.toEntity(): LoanAccountRow = LoanAccountRow(
```
Maps the model to its stored `LoanAccountRow`.

```kotlin
fun LoanStatusRow.toModel(): LoanStatus = LoanStatus(
```
Maps the stored row to its `LoanStatus` model.

```kotlin
fun LoanStatus.toEntity(): LoanStatusRow = LoanStatusRow(
```
Maps the model to its stored `LoanStatusRow`.

```kotlin
fun LoanTimelineRow.toModel(): LoanTimeline = LoanTimeline(
```
Maps the stored row to its `LoanTimeline` model.

```kotlin
fun LoanTimeline.toEntity(): LoanTimelineRow = LoanTimelineRow(
```
Maps the model to its stored `LoanTimelineRow`.

```kotlin
fun LoanAccountTypeRow.toModel(): LoanAccountType = LoanAccountType(
```
Maps the stored row to its `LoanAccountType` model.

```kotlin
fun LoanAccountType.toEntity(): LoanAccountTypeRow = LoanAccountTypeRow(
```
Maps the model to its stored `LoanAccountTypeRow`.

```kotlin
fun LoanRow.toModel(): Loan = Loan(
```
Maps the stored row to its `Loan` model.

```kotlin
fun Loan.toEntity(): LoanRow = LoanRow(
```
Maps the model to its stored `LoanRow`.

```kotlin
fun LoanAccountSummaryRow.toModel(): LoanAccountSummary = LoanAccountSummary(
```
Maps the stored row to its `LoanAccountSummary` model.

```kotlin
fun LoanAccountSummary.toEntity(): LoanAccountSummaryRow = LoanAccountSummaryRow(
```
Maps the model to its stored `LoanAccountSummaryRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/LoanTemplateMappers.kt`

```kotlin
fun InterestTypeOptionRow.toModel(): InterestTypeOption = InterestTypeOption(
```
Maps the stored row to its `InterestTypeOption` model.

```kotlin
fun InterestTypeOption.toEntity(): InterestTypeOptionRow = InterestTypeOptionRow(
```
Maps the model to its stored `InterestTypeOptionRow`.

```kotlin
fun LoanRepaymentRequestRow.toModel(): LoanRepaymentRequest = LoanRepaymentRequest(
```
Maps the stored row to its `LoanRepaymentRequest` model.

```kotlin
fun LoanRepaymentRequest.toEntity(): LoanRepaymentRequestRow = LoanRepaymentRequestRow(
```
Maps the model to its stored `LoanRepaymentRequestRow`.

```kotlin
fun LoanRepaymentTemplateRow.toModel(): LoanRepaymentTemplate = LoanRepaymentTemplate(
```
Maps the stored row to its `LoanRepaymentTemplate` model.

```kotlin
fun LoanRepaymentTemplate.toEntity(): LoanRepaymentTemplateRow = LoanRepaymentTemplateRow(
```
Maps the model to its stored `LoanRepaymentTemplateRow`.

```kotlin
fun LoanTemplateRow.toModel(): LoanTemplate = LoanTemplate(
```
Maps the stored row to its `LoanTemplate` model.

```kotlin
fun LoanTemplate.toEntity(): LoanTemplateRow = LoanTemplateRow(
```
Maps the model to its stored `LoanTemplateRow`.

```kotlin
fun LoanRepaymentResponseRow.toModel(): LoanRepaymentResponse = LoanRepaymentResponse(
```
Maps the stored row to its `LoanRepaymentResponse` model.

```kotlin
fun LoanRepaymentResponse.toEntity(): LoanRepaymentResponseRow = LoanRepaymentResponseRow(
```
Maps the model to its stored `LoanRepaymentResponseRow`.

```kotlin
fun LoanTransactionTemplateRow.toModel(): LoanTransactionTemplate = LoanTransactionTemplate(
```
Maps the stored row to its `LoanTransactionTemplate` model.

```kotlin
fun LoanTransactionTemplate.toEntity(): LoanTransactionTemplateRow = LoanTransactionTemplateRow(
```
Maps the model to its stored `LoanTransactionTemplateRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/OfficeStaffMappers.kt`

```kotlin
fun OfficeRow.toModel(): Office = Office(
```
Maps the stored row to its `Office` model.

```kotlin
fun Office.toEntity(): OfficeRow = OfficeRow(
```
Maps the model to its stored `OfficeRow`.

```kotlin
fun OfficeOpeningDateRow.toModel(): OfficeOpeningDate = OfficeOpeningDate(
```
Maps the stored row to its `OfficeOpeningDate` model.

```kotlin
fun OfficeOpeningDate.toEntity(): OfficeOpeningDateRow = OfficeOpeningDateRow(
```
Maps the model to its stored `OfficeOpeningDateRow`.

```kotlin
fun OfficeOptionsRow.toModel(): OfficeOptions = OfficeOptions(
```
Maps the stored row to its `OfficeOptions` model.

```kotlin
fun OfficeOptions.toEntity(): OfficeOptionsRow = OfficeOptionsRow(
```
Maps the model to its stored `OfficeOptionsRow`.

```kotlin
fun OptionsRow.toModel(): Options = Options(
```
Maps the stored row to its `Options` model.

```kotlin
fun Options.toEntity(): OptionsRow = OptionsRow(
```
Maps the model to its stored `OptionsRow`.

```kotlin
fun StaffRow.toModel(): Staff = Staff(
```
Maps the stored row to its `Staff` model.

```kotlin
fun Staff.toEntity(): StaffRow = StaffRow(
```
Maps the model to its stored `StaffRow`.

```kotlin
fun StaffOptionsRow.toModel(): StaffOptions = StaffOptions(
```
Maps the stored row to its `StaffOptions` model.

```kotlin
fun StaffOptions.toEntity(): StaffOptionsRow = StaffOptionsRow(
```
Maps the model to its stored `StaffOptionsRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/SavingsMappers.kt`

```kotlin
fun SavingAccountCurrencyRow.toModel(): SavingAccountCurrency = SavingAccountCurrency(
```
Maps the stored row to its `SavingAccountCurrency` model.

```kotlin
fun SavingAccountCurrency.toEntity(): SavingAccountCurrencyRow = SavingAccountCurrencyRow(
```
Maps the model to its stored `SavingAccountCurrencyRow`.

```kotlin
fun SavingAccountDepositTypeRow.toModel(): SavingAccountDepositType = SavingAccountDepositType(
```
Maps the stored row to its `SavingAccountDepositType` model.

```kotlin
fun SavingAccountDepositType.toEntity(): SavingAccountDepositTypeRow = SavingAccountDepositTypeRow(
```
Maps the model to its stored `SavingAccountDepositTypeRow`.

```kotlin
fun SavingsAccountRow.toModel(): SavingsAccount = SavingsAccount(
```
Maps the stored row to its `SavingsAccount` model.

```kotlin
fun SavingsAccount.toEntity(): SavingsAccountRow = SavingsAccountRow(
```
Maps the model to its stored `SavingsAccountRow`.

```kotlin
fun SavingsAccountStatusRow.toModel(): SavingsAccountStatus = SavingsAccountStatus(
```
Maps the stored row to its `SavingsAccountStatus` model.

```kotlin
fun SavingsAccountStatus.toEntity(): SavingsAccountStatusRow = SavingsAccountStatusRow(
```
Maps the model to its stored `SavingsAccountStatusRow`.

```kotlin
fun SavingsAccountSummaryRow.toModel(): SavingsAccountSummary = SavingsAccountSummary(
```
Maps the stored row to its `SavingsAccountSummary` model.

```kotlin
fun SavingsAccountSummary.toEntity(): SavingsAccountSummaryRow = SavingsAccountSummaryRow(
```
Maps the model to its stored `SavingsAccountSummaryRow`.

```kotlin
fun SavingsAccountWithAssociationsRow.toModel(): SavingsAccountWithAssociations = SavingsAccountWithAssociations(
```
Maps the stored row to its `SavingsAccountWithAssociations` model.

```kotlin
fun SavingsAccountWithAssociations.toEntity(): SavingsAccountWithAssociationsRow = SavingsAccountWithAssociationsRow(
```
Maps the model to its stored `SavingsAccountWithAssociationsRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/SavingsTransactionMappers.kt`

```kotlin
fun SavingProductOptionsRow.toModel(): SavingProductOptions = SavingProductOptions(
```
Maps the stored row to its `SavingProductOptions` model.

```kotlin
fun SavingProductOptions.toEntity(): SavingProductOptionsRow = SavingProductOptionsRow(
```
Maps the model to its stored `SavingProductOptionsRow`.

```kotlin
fun SavingProductsTemplateRow.toModel(): SavingProductsTemplate = SavingProductsTemplate(
```
Maps the stored row to its `SavingProductsTemplate` model.

```kotlin
fun SavingProductsTemplate.toEntity(): SavingProductsTemplateRow = SavingProductsTemplateRow(
```
Maps the model to its stored `SavingProductsTemplateRow`.

```kotlin
fun SavingsAccountTransactionRow.toModel(): SavingsAccountTransaction = SavingsAccountTransaction(
```
Maps the stored row to its `SavingsAccountTransaction` model.

```kotlin
fun SavingsAccountTransaction.toEntity(): SavingsAccountTransactionRow = SavingsAccountTransactionRow(
```
Maps the model to its stored `SavingsAccountTransactionRow`.

```kotlin
fun SavingsAccountTransactionTemplateRow.toModel(): SavingsAccountTransactionTemplate =
```
Maps the stored row to its `SavingsAccountTransactionTemplate` model.

```kotlin
fun SavingsAccountTransactionTemplate.toEntity(): SavingsAccountTransactionTemplateRow =
```
Maps the model to its stored `SavingsAccountTransactionTemplateRow`.

```kotlin
fun SavingsTransactionDateRow.toModel(): SavingsTransactionDate = SavingsTransactionDate(
```
Maps the stored row to its `SavingsTransactionDate` model.

```kotlin
fun SavingsTransactionDate.toEntity(): SavingsTransactionDateRow = SavingsTransactionDateRow(
```
Maps the model to its stored `SavingsTransactionDateRow`.

```kotlin
fun SavingsTransactionTypeRow.toModel(): SavingsTransactionType = SavingsTransactionType(
```
Maps the stored row to its `SavingsTransactionType` model.

```kotlin
fun SavingsTransactionType.toEntity(): SavingsTransactionTypeRow = SavingsTransactionTypeRow(
```
Maps the model to its stored `SavingsTransactionTypeRow`.

```kotlin
fun SavingsAccountTransactionRequestRow.toModel(): SavingsAccountTransactionRequest = SavingsAccountTransactionRequest(
```
Maps the stored row to its `SavingsAccountTransactionRequest` model.

```kotlin
fun SavingsAccountTransactionRequest.toEntity(): SavingsAccountTransactionRequestRow =
```
Maps the model to its stored `SavingsAccountTransactionRequestRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/mapping/SurveyMappers.kt`

```kotlin
fun SurveyRow.toModel(): Survey = Survey(
```
Maps the stored row to its `Survey` model.

```kotlin
fun Survey.toEntity(): SurveyRow = SurveyRow(
```
Maps the model to its stored `SurveyRow`.

```kotlin
fun ComponentDatasRow.toModel(): ComponentDatas = ComponentDatas(
```
Maps the stored row to its `ComponentDatas` model.

```kotlin
fun ComponentDatas.toEntity(): ComponentDatasRow = ComponentDatasRow(
```
Maps the model to its stored `ComponentDatasRow`.

```kotlin
fun QuestionDatasRow.toModel(): QuestionDatas = QuestionDatas(
```
Maps the stored row to its `QuestionDatas` model.

```kotlin
fun QuestionDatas.toEntity(): QuestionDatasRow = QuestionDatasRow(
```
Maps the model to its stored `QuestionDatasRow`.

```kotlin
fun ResponseDatasRow.toModel(): ResponseDatas = ResponseDatas(
```
Maps the stored row to its `ResponseDatas` model.

```kotlin
fun ResponseDatas.toEntity(): ResponseDatasRow = ResponseDatasRow(
```
Maps the model to its stored `ResponseDatasRow`.

### `core/store/src/commonMain/kotlin/kpt/core/store/note/NoteKeyedReadStores.kt`

```kotlin
fun provideRetrieveNoteStore(
```
Keyed reads for note with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

### `core/store/src/commonMain/kotlin/kpt/core/store/note/NoteMappers.kt`

```kotlin
fun NoteEntity.toModel() = Note(
```
Maps the stored row to its `Note` model.

```kotlin
fun Note.toEntity() = NoteEntity(
```
Maps the model to its stored `NoteEntity`.

### `core/store/src/commonMain/kotlin/kpt/core/store/note/NoteStore.kt`

```kotlin
data class NoteKey(val resourceType: String, val resourceId: Long)
```
One owner's notes — Fineract's `{resourceType}/{resourceId}/notes`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/note/impl/NoteCommandRepositoryImpl.kt:57</code></summary>

```kotlin
    private val noteApi: NoteApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Notes) private val notesStore: Store<NoteKey, List<Note>>,
    @FromStore(AppStoreIds.NoteItem) private val itemStore: MutableStore<NoteItemKey, Note>,
) : NoteCommandRepository {

    override suspend fun addNewNote(
```

</details>

```kotlin
fun provideNoteStore(
```
Store reading the note.

```kotlin
data class NoteItemKey(val resourceType: String, val resourceId: Long, val noteId: Long)
```
One note ROW — the owner it hangs off plus the note's id.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/note/impl/NoteCommandRepositoryImpl.kt:58</code></summary>

```kotlin
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Notes) private val notesStore: Store<NoteKey, List<Note>>,
    @FromStore(AppStoreIds.NoteItem) private val itemStore: MutableStore<NoteItemKey, Note>,
) : NoteCommandRepository {

    override suspend fun addNewNote(
        resourceType: String,
```

</details>

```kotlin
fun provideNoteItemStore(
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/note/RetrieveNoteKey.kt`

```kotlin
data class RetrieveNoteKey(
```
Addresses one `retrieveNote` read.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/note/NoteKeyedReadStores.kt:31</code></summary>

```kotlin
    noteApi: NoteApi,
    cache: ApiResponseCacheDao,
): Store<RetrieveNoteKey, NoteDto> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.NoteRetrieveNote.forKey("${key.resourceType}:${key.resourceId}:${key.noteId}") },
    fetch = { key ->
        noteApi.retrieveNote(
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/notification/NotificationStores.kt`

```kotlin
fun provideNotificationsStore(
```
The signed-in user's notifications, unread first then the recent read history — two reads, because Fineract's `GET notifications` returns only one side of `isRead` per call.

### `core/store/src/commonMain/kotlin/kpt/core/store/office/OfficeKeyedReadStores.kt`

```kotlin
fun provideRetrieveOfficesStore(
```
Keyed reads for office with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

### `core/store/src/commonMain/kotlin/kpt/core/store/office/OfficeStore.kt`

```kotlin
fun provideOfficeStore(
```
Offices — READ-CACHE archetype (`createStore`). The first vertical of the DataManager→Store5 collapse.

### `core/store/src/commonMain/kotlin/kpt/core/store/office/RetrieveOfficesKey.kt`

```kotlin
data class RetrieveOfficesKey(
```
Addresses one `retrieveOffices` read.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/office/OfficeKeyedReadStores.kt:31</code></summary>

```kotlin
    officeApi: OfficeApi,
    cache: ApiResponseCacheDao,
): Store<RetrieveOfficesKey, List<GetOfficesResponse>> = cachedRead(
    dao = cache,
    keyOf = { key ->
        AppCacheKeys.OfficeRetrieveOffices.forKey("${key.includeAllOffices}:${key.orderBy}:${key.sortOrder}")
    },
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/pathtracking/PathTrackingStores.kt`

```kotlin
fun provideUserPathTrackingStore(
```
The paths recorded for one user — the `m_staff_path_tracking` datatable the original's `PathTrackingRepositoryImp` read (`6b66e8a43`). Offline-first through the shared read cache.

### `core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt`

```kotlin
fun interface UserDataSource
```
The read PORT for the user-preferences store. The preferences themselves live in `core/datastore`, which `core/store` does not depend on (and must not — `core/datastore` sits beside it in the layer order, not below).

```kotlin
fun provideUserDataStore(source: UserDataSource): Store<Unit, UserData> =
```
OFFLINE_LOCAL_ONLY Store5 store over user preferences (`feature_profile.combo_id: local_only_prefs`).

### `core/store/src/commonMain/kotlin/kpt/core/store/queue/OfflineQueueStores.kt`

```kotlin
fun provideClientPayloadQueueStore(
```
_No KDoc at source._

```kotlin
fun provideCenterPayloadQueueStore(
```
_No KDoc at source._

```kotlin
fun provideGroupPayloadQueueStore(
```
_No KDoc at source._

```kotlin
fun provideLoanRepaymentQueueStore(
```
_No KDoc at source._

```kotlin
fun provideSavingsTransactionQueueStore(
```
_No KDoc at source._

```kotlin
fun providePendingClientPayloadsStore(
```
Store reading the pending client payloads.

```kotlin
fun providePendingCenterPayloadsStore(
```
Store reading the pending center payloads.

```kotlin
fun providePendingGroupPayloadsStore(
```
Store reading the pending group payloads.

```kotlin
fun providePendingLoanRepaymentsStore(
```
Store reading the pending loan repayments.

```kotlin
fun providePendingSavingsTransactionsStore(
```
Store reading the pending savings transactions.

### `core/store/src/commonMain/kotlin/kpt/core/store/recurringdeposit/RecurringDepositTemplateStore.kt`

```kotlin
fun provideRecurringDepositTemplateStore(
```
Store reading the recurring deposit template.

### `core/store/src/commonMain/kotlin/kpt/core/store/report/ReportKeyedReadStores.kt`

```kotlin
data class GetReportFullParameterListKey(
```
Addresses one `getReportFullParameterList` read.

```kotlin
data class GetReportParameterDetailsKey(
```
Addresses one `getReportParameterDetails` read.

```kotlin
data class GetReportOfficeKey(
```
Addresses one `getReportOffice` read.

```kotlin
data class GetReportProductKey(
```
Addresses one `getReportProduct` read.

```kotlin
data class GetRunReportWithQueryKey(
```
Addresses one `getRunReportWithQuery` read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/report/impl/ReportRepositoryImpl.kt:39</code></summary>

```kotlin
    @FromStore(AppStoreIds.ReportParameters) private val parameterStore: Store<String, List<ReportParameter>>,
    @FromStore(AppStoreIds.ReportGetRunReportWithQuery)
    private val runStore: Store<GetRunReportWithQueryKey, FullParameterListResponse>,
) : ReportRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
```

</details>

```kotlin
data class GetCenterSummaryInfoKey(
```
Addresses one `getCenterSummaryInfo` read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/center/impl/CenterRepositoryImpl.kt:42</code></summary>

```kotlin
    private val withMeetingStore: Store<Int, CenterWithAssociations>,
    @FromStore(AppStoreIds.ReportGetCenterSummaryInfo)
    private val summaryStore: Store<GetCenterSummaryInfoKey, List<CenterInfo>>,
) : CenterRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
```

</details>

```kotlin
fun provideGetReportFullParameterListStore(
```
Store reading the report full parameter list.

```kotlin
fun provideGetReportParameterDetailsStore(
```
Store reading the report parameter details.

```kotlin
fun provideGetReportOfficeStore(
```
Store reading the report office.

```kotlin
fun provideGetReportProductStore(
```
Store reading the report product.

```kotlin
fun provideGetRunReportWithQueryStore(
```
Store reading the run report with query.

```kotlin
fun provideGetCenterSummaryInfoStore(
```
Store reading the center summary info.

```kotlin
fun provideReportParametersStore(
```
A report's parameters as data — `FullParameterList` mapped by `toReportParameters`. Keyed by the report's plain name; the SQL-literal quoting `R_reportListing` needs is applied here, once.

### `core/store/src/commonMain/kotlin/kpt/core/store/report/ReportParameterMapping.kt`

```kotlin
fun FullParameterListResponse.toReportParameters(): List<ReportParameter>
```
Reads the report's parameters from the result set, finding each column by name and falling back to its usual position.

### `core/store/src/commonMain/kotlin/kpt/core/store/report/ReportStore.kt`

```kotlin
fun provideReportStore(
```
The full report catalogue. `genericResultSet = true` is what makes Fineract return the rows as data rather than a rendered report, and `parameterType = false` keeps the response to report definitions instead of their parameter metadata.

### `core/store/src/commonMain/kotlin/kpt/core/store/savings/SavingsAccountKey.kt`

```kotlin
data class SavingsAccountKey(
```
`savingsAccountType` is a path segment on Fineract ("savingsaccounts" / "recurringdepositaccounts" / ...), so the same numeric id means different accounts under different types — it belongs in the key, not as a caller-side constant.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/offline/impl/OfflineSyncEngine.kt:133</code></summary>

```kotlin
    val loanCharges: Store<Int, List<Charges>>,
    val loanReschedules: Store<Int, List<LoanRescheduleResponse>>,
    val savings: Store<SavingsAccountKey, SavingsAccountWithAssociations>,
    val savingsTemplate: Store<GetSavingsAccountTransactionTemplateKey, SavingsAccountTransactionTemplate>,
)

/**
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/savings/SavingsAccountStore.kt`

```kotlin
fun provideSavingsAccountStore(
```
Store reading the savings account.

### `core/store/src/commonMain/kotlin/kpt/core/store/savings/SavingsCatalogStores.kt`

```kotlin
fun provideSavingsProductsStore(
```
Catalogue and template reads for savings — option lists and form templates with no table of their own, cached through the shared read cache so they still render with no signal.

```kotlin
fun provideSavingsProductTemplateStore(
```
Store reading the savings product template.

### `core/store/src/commonMain/kotlin/kpt/core/store/savings/SavingsKeyedReadStores.kt`

```kotlin
data class FixedDepositProductTemplateKey(
```
Addresses one `fixedDepositProductTemplate` read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/impl/SavingsAccountRepositoryImpl.kt:61</code></summary>

```kotlin
    private val shareTemplateStore: Store<ShareProductTemplateKey, ShareTemplate>,
    @FromStore(AppStoreIds.SavingsFixedDepositProductTemplate)
    private val fixedDepositTemplateStore: Store<FixedDepositProductTemplateKey, FixedDepositTemplate>,
) : SavingsAccountRepository {

    override fun savingsAccountStream(
        savingsAccountType: String,
```

</details>

```kotlin
data class GetClientSavingsAccountTemplateByProductKey(
```
Addresses one `getClientSavingsAccountTemplateByProduct` read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/impl/SavingsAccountRepositoryImpl.kt:50</code></summary>

```kotlin
    @FromStore(AppStoreIds.SavingsGetClientSavingsAccountTemplateByProduct)
    private val clientApplicationTemplateStore: Store<
        GetClientSavingsAccountTemplateByProductKey,
        SavingProductsTemplate,
        >,
    @FromStore(AppStoreIds.SavingsGetGroupSavingsAccountTemplateByProduct)
    private val groupApplicationTemplateStore: Store<
```

</details>

```kotlin
data class GetGroupSavingsAccountTemplateByProductKey(
```
Addresses one `getGroupSavingsAccountTemplateByProduct` read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/impl/SavingsAccountRepositoryImpl.kt:55</code></summary>

```kotlin
    @FromStore(AppStoreIds.SavingsGetGroupSavingsAccountTemplateByProduct)
    private val groupApplicationTemplateStore: Store<
        GetGroupSavingsAccountTemplateByProductKey,
        SavingProductsTemplate,
        >,
    @FromStore(AppStoreIds.SavingsShareProductTemplate)
    private val shareTemplateStore: Store<ShareProductTemplateKey, ShareTemplate>,
```

</details>

```kotlin
data class ShareProductTemplateKey(
```
Addresses one `shareProductTemplate` read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/impl/SavingsAccountRepositoryImpl.kt:59</code></summary>

```kotlin
        >,
    @FromStore(AppStoreIds.SavingsShareProductTemplate)
    private val shareTemplateStore: Store<ShareProductTemplateKey, ShareTemplate>,
    @FromStore(AppStoreIds.SavingsFixedDepositProductTemplate)
    private val fixedDepositTemplateStore: Store<FixedDepositProductTemplateKey, FixedDepositTemplate>,
) : SavingsAccountRepository {
```

</details>

```kotlin
fun provideFixedDepositProductTemplateStore(
```
Store reading the fixed deposit product template.

```kotlin
fun provideGetClientSavingsAccountTemplateByProductStore(
```
Store reading the client savings account template by product.

```kotlin
fun provideGetGroupSavingsAccountTemplateByProductStore(
```
Store reading the group savings account template by product.

```kotlin
fun provideShareProductTemplateStore(
```
Store reading the share product template.

```kotlin
data class GetSavingsAccountTransactionTemplateKey(
```
Addresses one `getSavingsAccountTransactionTemplate` read.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/offline/impl/OfflineSyncEngine.kt:134</code></summary>

```kotlin
    val loanReschedules: Store<Int, List<LoanRescheduleResponse>>,
    val savings: Store<SavingsAccountKey, SavingsAccountWithAssociations>,
    val savingsTemplate: Store<GetSavingsAccountTransactionTemplateKey, SavingsAccountTransactionTemplate>,
)

/**
 * The download walk of Settings → Offline sync, grown out of the original `OfflineDownloadRepository`:
```

</details>

```kotlin
fun provideGetSavingsAccountTransactionTemplateStore(
```
Store reading the savings account transaction template.

### `core/store/src/commonMain/kotlin/kpt/core/store/search/SearchKeyedReadStores.kt`

```kotlin
fun provideSearchResourcesStore(
```
Keyed reads for search with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

### `core/store/src/commonMain/kotlin/kpt/core/store/searchrecord/SearchRecordStore.kt`

```kotlin
fun provideSearchRecordStore(cache: ApiResponseCacheDao): Store<SearchRecordKey, List<GenericSearchRecord>> =
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/serverconfig/ServerConfigStore.kt`

```kotlin
fun interface ServerConfigSource
```
The read PORT for the server configuration, which lives in `core/datastore` — a layer `core/store` does not depend on. `core/data` binds it, as it does `UserDataSource`.

```kotlin
fun provideServerConfigStore(source: ServerConfigSource): Store<Unit, ServerConfig> =
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/serverconfig/ServerInstancesStore.kt`

```kotlin
fun provideServerInstancesStore(api: ServerInstanceApi, dao: ServerInstanceDao): Store<Unit, List<ServerInstance>> =
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/session/SessionStore.kt`

```kotlin
fun interface SessionSource
```
The read PORT for the stored Fineract session, bound in core/data (as `ServerConfigSource` is).

```kotlin
fun provideSessionStore(source: SessionSource): Store<Unit, User> =
```
_No KDoc at source._

### `core/store/src/commonMain/kotlin/kpt/core/store/staff/StaffKeyedReadStores2.kt`

```kotlin
fun provideAllStaffStore(
```
Keyed reads for staff with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

```kotlin
fun provideFieldStaffForOfficeStore(
```
Store reading the field staff for office.

### `core/store/src/commonMain/kotlin/kpt/core/store/staff/StaffStore.kt`

```kotlin
fun provideStaffStore(
```
Staff for an office — READ-CACHE (`createStore`), keyed by office. The key is the officeId because both sides are office-scoped: `getStaffForOffice(officeId)` on the wire, `getAllStaff(officeId)` in Room.

### `core/store/src/commonMain/kotlin/kpt/core/store/survey/SurveyKeyedReadStores2.kt`

```kotlin
fun provideGetSurveyStore(
```
Keyed reads for survey with no table of their own — cached through the shared read cache so the last-known answer still renders with no signal.

### `core/store/src/commonMain/kotlin/kpt/core/store/survey/SurveyStore.kt`

```kotlin
fun provideSurveyStore(
```
Surveys — READ-CACHE (`createStore`). Survey definitions are server-authoritative reference data that a field officer must be able to OPEN offline, since surveys are administered in the field where connectivity is the exception.

---

_48 type(s), 289 function(s)/property(ies); 316 carry KDoc at source; 4 authored example(s); 37 live call site(s)._
<!-- api-docs:end -->
