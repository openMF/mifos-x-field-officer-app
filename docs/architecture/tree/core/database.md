# `core/database`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATABASE.md`
> **Measured:** 183 Kotlin files, 11 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@DbEntity` | `database-ksp` | config/AppDatabase |
| `@DbDao` | `database-ksp` | di/GeneratedDaoBindings |
| `@DbConverters` | `database-ksp` | di/GeneratedConverterBindings |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`ActualDisbursementDateEntity`, `AddressConfiguration`, `AddressTemplate`, `ApiResponseCacheDao`, `ApiResponseCacheEntity`, `CenterAccounts`, `CenterDao`, `CenterDateEntity`, `CenterDetail`, `CenterDetailEntity`, `CenterEntity`, `CenterPayloadEntity`, `CenterTypeConverters`, `CenterWithAssociations`  …and 143 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/database sha=6841035d386761aef046b1c5b34ce3c96efb2d47 -->
## API reference

_Generated from `core/database` at tree `6841035d3867` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/database/src/commonMain/kotlin/kpt/core/database/cache/dao/ApiResponseCacheDao.kt`

```kotlin
interface ApiResponseCacheDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/client/ClientRepositoryTest.kt:114</code></summary>

```kotlin

    private val cacheRows = MutableStateFlow<Map<String, ApiResponseCacheEntity>>(emptyMap())
    private val cache = object : ApiResponseCacheDao {
        override fun observe(cacheKey: String) = cacheRows.map { it[cacheKey] }
        override fun observeByPrefix(
            prefix: String,
        ) = cacheRows.map { rows -> rows.filterKeys { it.startsWith(prefix) }.values.toList() }
```

</details>

- `fun observe(cacheKey: String): Flow<ApiResponseCacheEntity?>`
- `fun observeByPrefix(prefix: String): Flow<List<ApiResponseCacheEntity>>` — Every cached response under one prefix, re-emitting on any write to the table — the read side of `deleteByPrefix`.
- `suspend fun upsert(row: ApiResponseCacheEntity)`
- `suspend fun delete(cacheKey: String)`
- `suspend fun deleteByPrefix(prefix: String)` — Drop every cached response under one prefix — the seam a feature uses to invalidate its own reads after a write, without knowing which keys exist.
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/cache/entity/ApiResponseCacheEntity.kt`

```kotlin
data class ApiResponseCacheEntity(
```
One row per cached read — the offline backing for reads that have no table of their own. The app's entity reads (clients, centers, loans, savings) each own a real Room table, because the app queries and relates them.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/client/ClientRepositoryTest.kt:113</code></summary>

```kotlin
    // ---- core/database ----

    private val cacheRows = MutableStateFlow<Map<String, ApiResponseCacheEntity>>(emptyMap())
    private val cache = object : ApiResponseCacheDao {
        override fun observe(cacheKey: String) = cacheRows.map { it[cacheKey] }
        override fun observeByPrefix(
            prefix: String,
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/center/converter/CenterTypeConverters.kt`

```kotlin
class CenterTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/center/dao/CenterDao.kt`

```kotlin
interface CenterDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/center/CenterStore.kt:40</code></summary>

```kotlin
fun provideCenterStore(
    service: CenterApi,
    dao: CenterDao,
): Store<Unit, List<Center>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit ->
        service.getCenters(b = true, offset = 0, limit = CENTER_PAGE_LIMIT).pageItems
    },
```

</details>

- `suspend fun saveCenter(center: CenterEntity)`
- `suspend fun saveLoanAccount(loanAccount: LoanAccountEntity)`
- `suspend fun saveSavingsAccount(savingsAccount: SavingsAccountEntity)`
- `suspend fun saveMemberLoanAccount(loanAccount: LoanAccountEntity)`
- `suspend fun saveCenterPayload(centerPayload: CenterPayloadEntity)`
- `suspend fun updateCenterPayload(centerPayload: CenterPayloadEntity)`
- `suspend fun deleteCenterPayloadById(id: Int)`
- `fun readAllCenters(): Flow<List<CenterEntity>>`
- `fun readAllCenterPayload(): Flow<List<CenterPayloadEntity>>`
- `fun getCenterAssociateGroups(centerId: Int): Flow<List<GroupEntity>>`
- `suspend fun saveCenterWithDate(center: CenterEntity)` — Persist a center, deriving its split date columns from `activationDate`. Was `CenterDaoHelper.saveCenter`.
- `val id = center.id`
- `val date = if (center.activationDate.size >= 3 && id != null)`
- `suspend fun saveCenterAccounts(centerAccounts: CenterAccounts, centerId: Int)` — Persist a center's accounts across three tables, stamping the owning center onto each.
  _…more members; read the file._

### `core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterAccounts.kt`

```kotlin
data class CenterAccounts(
```
Created by mayankjindal on 11/07/17.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/offline/impl/OfflineSyncEngine.kt:100</code></summary>

```kotlin
internal class EntityStores(
    val centre: Store<Int, CenterWithAssociations>,
    val centreAccounts: Store<Int, CenterAccounts>,
    val centreItem: MutableStore<Int, Center>,
    val group: Store<Int, GroupWithAssociations>,
    val groupRow: Store<Int, Group>,
    val groupAccounts: Store<Int, GroupAccounts>,
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterDateEntity.kt`

```kotlin
data class CenterDateEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/converter/CenterTypeConverters.kt:23</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromCenterDate(centerDate: CenterDateEntity?): String? {
        return centerDate?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterEntity.kt`

```kotlin
data class CenterEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/dao/CenterDao.kt:32</code></summary>

```kotlin
interface CenterDao {

    @Insert(entity = CenterEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCenter(center: CenterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = LoanAccountEntity::class)
    suspend fun saveLoanAccount(loanAccount: LoanAccountEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterPayloadEntity.kt`

```kotlin
data class CenterPayloadEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/dao/CenterDao.kt:44</code></summary>

```kotlin
    suspend fun saveMemberLoanAccount(loanAccount: LoanAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = CenterPayloadEntity::class)
    suspend fun saveCenterPayload(centerPayload: CenterPayloadEntity)

    @Update(entity = CenterPayloadEntity::class, onConflict = OnConflictStrategy.NONE)
    suspend fun updateCenterPayload(centerPayload: CenterPayloadEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterWithAssociations.kt`

```kotlin
data class CenterWithAssociations(
```
Created by ishankhanna on 28/06/14.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/detail/CenterDetailScreen.kt:130</code></summary>

```kotlin
    groupsClickable: Boolean = true,
    canActivate: Boolean = false,
    meeting: ScreenState<CenterWithAssociations> = ScreenState.Loading,
    summary: ScreenState<CenterInfo> = ScreenState.Loading,
    canAddSavingsAccount: Boolean = false,
) {
    Scaffold(
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/charge/converter/ChargeTypeConverters.kt`

```kotlin
object ChargeTypeConverters
```
_No KDoc at source._

- `fun fromChargeTimeType(type: ChargeTimeTypeEntity?): String?`
- `fun toChargeTimeType(json: String?): ChargeTimeTypeEntity?`
- `fun fromChargeCalculationType(type: ChargeCalculationTypeEntity?): String?`
- `fun toChargeCalculationType(json: String?): ChargeCalculationTypeEntity?`
- `fun fromClientChargeCurrency(currency: ClientChargeCurrencyEntity?): String?`
- `fun toClientChargeCurrency(json: String?): ClientChargeCurrencyEntity?`

### `core/database/src/commonMain/kotlin/kpt/core/database/charge/dao/ChargeDao.kt`

```kotlin
interface ChargeDao
```
Created by Pronay Sarker on 14/02/2025 (3:32 PM) Every query is scoped by the OWNER (`resourceType` + `resourceId`), which is also the leading half of the table's primary key — see `ChargesEntity`.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/charge/ChargeStore.kt:55</code></summary>

```kotlin
fun provideChargeStore(
    service: ChargeApi,
    dao: ChargeDao,
): Store<ChargeKey, List<Charges>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: ChargeKey ->
        if (key.resourceType == CLIENTS) {
            service.getListOfClientCharges(resourceType = CLIENTS, resourceId = key.resourceId).pageItems
```

</details>

- `fun observeCharges(resourceType: String, resourceId: Int): Flow<List<ChargesEntity>>`
- `fun observeCharge(resourceType: String, resourceId: Int, chargeId: Int): Flow<ChargesEntity?>`
- `suspend fun insertAllCharges(charges: List<ChargesEntity>)`
- `suspend fun deleteCharges(resourceType: String, resourceId: Int)`
- `suspend fun deleteCharge(resourceType: String, resourceId: Int, chargeId: Int)`
- `suspend fun deleteAllCharges()` — What the logout purge reaches through the charge store's `deleteAll`.
- `suspend fun replaceCharges(resourceType: String, resourceId: Int, charges: List<ChargesEntity>)` — A fetch is the server's WHOLE list for the owner, so a row it no longer contains was deleted and must leave the table — an upsert-only write kept a waived or deleted charge on screen.
- `suspend fun upsertCharge(resourceType: String, resourceId: Int, charge: ChargesEntity) =` — One charge, stamped with its owner.

### `core/database/src/commonMain/kotlin/kpt/core/database/charge/entity/ChargeCalculationTypeEntity.kt`

```kotlin
data class ChargeCalculationTypeEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/charge/converter/ChargeTypeConverters.kt:33</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromChargeCalculationType(type: ChargeCalculationTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/charge/entity/ChargeTimeTypeEntity.kt`

```kotlin
data class ChargeTimeTypeEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/charge/converter/ChargeTypeConverters.kt:23</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromChargeTimeType(type: ChargeTimeTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/charge/entity/ChargesEntity.kt`

```kotlin
data class ChargesEntity(
```
Created by nellyk on 2/15/2016. ## Keyed by OWNER + id, not by id Client, loan and savings charges are three different Fineract tables (`m_client_charge`, `m_loan_charge`, `m_savings_account_charge`), so their ids coincide freely.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/charge/dao/ChargeDao.kt:34</code></summary>

```kotlin

    @Query("SELECT * FROM Charges WHERE resourceType = :resourceType AND resourceId = :resourceId")
    fun observeCharges(resourceType: String, resourceId: Int): Flow<List<ChargesEntity>>

    @Query(
        "SELECT * FROM Charges WHERE resourceType = :resourceType AND resourceId = :resourceId " +
            "AND id = :chargeId LIMIT 1",
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/charge/entity/ClientChargeCurrencyEntity.kt`

```kotlin
data class ClientChargeCurrencyEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/charge/converter/ChargeTypeConverters.kt:43</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromClientChargeCurrency(currency: ClientChargeCurrencyEntity?): String? {
        return currency?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/checkerinbox/dao/CheckerInboxDao.kt`

```kotlin
interface CheckerInboxDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/checkerinbox/CheckerInboxStore.kt:35</code></summary>

```kotlin
fun provideCheckerInboxStore(
    service: CheckerInboxApi,
    dao: CheckerInboxDao,
): Store<Unit, List<CheckerTask>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit -> service.getCheckerList() },
    sourceOfTruth = SourceOfTruth.of(
        // `[]` for no rows, not null-for-empty: Store5 emits NOTHING when a reader goes from rows
```

</details>

- `fun observeAll(): Flow<List<CheckerTaskEntity>>` — Observe the pending queue, newest first. Emits on every change.
- `fun observeById(id: Int): Flow<CheckerTaskEntity?>` — Observe one task. Emits `null` once it is approved/rejected and purged.
- `suspend fun getById(id: Int): CheckerTaskEntity?` — One-shot read for non-reactive callers.
- `suspend fun upsertAll(tasks: List<CheckerTaskEntity>)`
- `suspend fun deleteById(id: Int)` — A decided task leaves the queue — the server is the authority on what remains.
- `suspend fun deleteAll()`
- `suspend fun replaceAll(tasks: List<CheckerTaskEntity>)` — Make the table EXACTLY the server's pending queue. The store's writer used `upsertAll` alone, so a task decided elsewhere (the web app, another checker) stayed pending here for good.

### `core/database/src/commonMain/kotlin/kpt/core/database/checkerinbox/entity/CheckerTaskEntity.kt`

```kotlin
data class CheckerTaskEntity(
```
A maker-checker task awaiting approval. Cached so a field officer can REVIEW the pending queue offline; the approve/reject actions themselves stay online-only (D1 — an authorisation decision must reach Fineract to be real).

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/checkerinbox/dao/CheckerInboxDao.kt:26</code></summary>

```kotlin
    /** Observe the pending queue, newest first. Emits on every change. */
    @Query("SELECT * FROM checker_tasks ORDER BY madeOnDate DESC")
    fun observeAll(): Flow<List<CheckerTaskEntity>>

    /** Observe one task. Emits `null` once it is approved/rejected and purged. */
    @Query("SELECT * FROM checker_tasks WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<CheckerTaskEntity?>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/checkerinbox/mapper/CheckerTaskMapper.kt`

```kotlin
fun CheckerTaskEntity.toDomain(): CheckerTask = CheckerTask(
```
The entity columns are nullable because Fineract omits them on partially-processed tasks; the domain type is not, so the read direction substitutes empty rather than widening the domain.

```kotlin
fun CheckerTask.toEntity(): CheckerTaskEntity = CheckerTaskEntity(
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientOptionsTypeConverters.kt`

```kotlin
class ClientOptionsTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientTypeConverters.kt`

```kotlin
class ClientTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientAddressIdentifierDao.kt`

```kotlin
interface ClientAddressIdentifierDao
```
Client addresses and identifiers.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:33</code></summary>

```kotlin
    ClientDataTableDao,
    ClientPayloadDao,
    ClientAddressIdentifierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientEntity::class)
    suspend fun insertClient(client: ClientEntity)

    @Query("SELECT * FROM Client")
```

</details>

- `suspend fun insertAddress(address: ClientAddressEntity)`
- `suspend fun insertAddresses(addresses: List<ClientAddressEntity>)`
- `fun getAddressesByClientId(clientId: Int): Flow<List<ClientAddressEntity>>`
- `fun getAddressById(addressId: Int): Flow<ClientAddressEntity?>`
- `suspend fun deleteAddressesByClientId(clientId: Int)`
- `fun searchAddressesByQuery(query: String): Flow<List<ClientAddressEntity>>`
- `suspend fun insertIdentifiers(identifiers: List<ClientIdentifierEntity>)`
- `fun getIdentifiersByClientId(clientId: Int): Flow<List<ClientIdentifierEntity>>`
- `suspend fun deleteIdentifiersByClientId(clientId: Int)`
- `fun searchIdentifiersByQuery(query: String): Flow<List<ClientIdentifierEntity>>`
- `suspend fun replaceIdentifiersForClients(identifiers: List<ClientIdentifierEntity>)` — Replace every identifier belonging to the touched clients.
- `val valid = identifiers.filter { it.clientId != null }`

### `core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt`

```kotlin
interface ClientDao :
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/client/ClientStore.kt:37</code></summary>

```kotlin
fun provideClientStore(
    service: ClientApi,
    dao: ClientDao,
): Store<Unit, List<Client>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit ->
        service.getAllClients(b = true, offset = 0, limit = CLIENT_PAGE_LIMIT).pageItems
    },
```

</details>

- `suspend fun insertClient(client: ClientEntity)`
- `fun getAllClients(): Flow<List<ClientEntity>>`
- `fun getClientsByGroupId(groupId: Int): Flow<List<ClientEntity>>`
- `fun getClientByClientId(clientId: Int): Flow<ClientEntity?>`
- `suspend fun deleteClientById(clientId: Int)` — Per-key clear for the `clientRow` write store.
- `suspend fun deleteAllClients()` — The roster store's `deleteAll` — what the logout purge reaches the `Client` table through.
- `suspend fun insertLoanAccount(loanAccount: LoanAccountEntity)`
- `suspend fun insertSavingsAccount(savingsAccount: SavingsAccountEntity)`
- `fun getLoanAccountsByClientId(clientId: Long): Flow<List<LoanAccountEntity>>`
- `fun getSavingsAccountsByClientId(clientId: Long): Flow<List<SavingsAccountEntity>>`
- `const val GENDER_OPTIONS = "genderOptions"`
- `const val CLIENT_TYPE_OPTIONS = "clientTypeOptions"`
- `const val CLIENT_CLASSIFICATION_OPTIONS = "clientClassificationOptions"`
- `suspend fun saveClientWithDate(client: ClientEntity)` — Persist a client, deriving its split date columns. Was `ClientDaoHelper.saveClient`.
  _…more members; read the file._

### `core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDataTableDao.kt`

```kotlin
interface ClientDataTableDao
```
Datatable header/value rows that ride along a client template.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:31</code></summary>

```kotlin
interface ClientDao :
    ClientTemplateOptionsDao,
    ClientDataTableDao,
    ClientPayloadDao,
    ClientAddressIdentifierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientEntity::class)
    suspend fun insertClient(client: ClientEntity)
```

</details>

- `suspend fun insertColumnHeader(columnHeader: ColumnHeader)`
- `suspend fun insertColumnValue(columnValue: ColumnValue)`
- `suspend fun deleteDataTables()`
- `suspend fun deleteColumnHeaders()`
- `suspend fun deleteColumnValues()`
- `fun getDatatableByTableName(tableName: String): Flow<List<DataTableEntity>>`
- `fun getColumnHeadersByTableName(tableName: String): Flow<List<ColumnHeader>>`
- `fun getColumnValuesByTableName(tableName: String): Flow<List<ColumnValue>>`
- `suspend fun insertDataTable(dataTable: DataTableEntity)`
- `suspend fun replaceDataTables(dataTables: List<DataTableEntity>)` — Replace every datatable (and its headers/values) — cleared ONCE, then rewritten.

### `core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientPayloadDao.kt`

```kotlin
interface ClientPayloadDao
```
Offline client payload queue.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:32</code></summary>

```kotlin
    ClientTemplateOptionsDao,
    ClientDataTableDao,
    ClientPayloadDao,
    ClientAddressIdentifierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientEntity::class)
    suspend fun insertClient(client: ClientEntity)
```

</details>

- `suspend fun insertClientPayload(clientPayload: ClientPayloadEntity)`
- `suspend fun insertDataTablePayload(dataTablePayloads: DataTablePayload)`
- `fun getAllClientPayload(): Flow<List<ClientPayloadEntity>>`
- `fun getDataTablePayloadByCreationTime(clientCreationTime: Long): Flow<List<DataTablePayload>>`
- `suspend fun deleteClientPayloadById(id: Int)`
- `suspend fun deleteDataTablePayloadByCreationTime(clientCreationTime: Long)`
- `suspend fun updateDatabaseClientPayload(clientPayload: ClientPayloadEntity)`
- `suspend fun deleteClientPayloadWithData(id: Int, clientCreationTime: Long)` — Drop an offline client payload together with the datatable rows queued alongside it. Was `ClientDaoHelper.deleteAndUpdatePayloads` — now atomic, so a half-deleted payload cannot be replayed on the next sync.
- `fun observeClientPayload(id: Int): Flow<ClientPayloadEntity?>` — One queued offline client, for the write store's source of truth.
- `suspend fun deleteAllClientPayloads()` — The logout purge for the offline client queue — a capture must not outlive its officer.

### `core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientTemplateOptionsDao.kt`

```kotlin
interface ClientTemplateOptionsDao
```
Client template + option-table reads/writes.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:30</code></summary>

```kotlin
@Dao
interface ClientDao :
    ClientTemplateOptionsDao,
    ClientDataTableDao,
    ClientPayloadDao,
    ClientAddressIdentifierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientEntity::class)
```

</details>

- `suspend fun insertClientsTemplate(clientsTemplate: ClientsTemplateEntity)`
- `suspend fun insertOfficeOptions(officeOptions: List<OfficeOptionsEntity>)`
- `suspend fun insertStaffOptions(staffOptions: List<StaffOptionsEntity>)`
- `suspend fun insertSavingProductOptions(savingProductOptions: List<SavingProductOptionsEntity>)`
- `suspend fun insertOption(options: OptionsEntity)`
- `suspend fun insertInterestTypes(interestTypes: List<InterestTypeEntity>)`
- `suspend fun getClientsTemplate(): ClientsTemplateEntity`
- `fun getOfficeOptions(): Flow<List<OfficeOptionsEntity>>`
- `fun getStaffOptions(): Flow<List<StaffOptionsEntity>>`
- `fun getSavingProductOptions(): Flow<List<SavingProductOptionsEntity>>`
- `fun getOptions(optionType: String): Flow<List<OptionsEntity>>`
- `fun getAllInterestType(): Flow<List<InterestTypeEntity>>`

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/AddressTemplate.kt`

```kotlin
data class AddressTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addaddress/ClientAddAddressScreen.kt:130</code></summary>

```kotlin
internal fun ClientAddAddressContent(
    state: ClientAddAddressState,
    template: ScreenState<AddressTemplate>,
    onAction: (ClientAddAddressAction) -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
    offeredDraft: SavedFormDraft? = null,
```

</details>

```kotlin
data class AddressConfiguration(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientRepository.kt:106</code></summary>

```kotlin
     * validation made client creation impossible on such a tenant.
     */
    fun addressConfigurationStream(scope: CoroutineScope): ScreenDataStream<AddressConfiguration>

    fun addressTemplateStream(scope: CoroutineScope): ScreenDataStream<AddressTemplate>

    /** Where the officer has recorded this client — the pinpoint map/list. */
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientAccounts.kt`

```kotlin
data class ClientAccounts(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/general/ClientGeneralViewModel.kt:124</code></summary>

```kotlin
 * numbers under the officer without anyone asking for it.
 */
internal fun performanceHistory(accounts: ClientAccounts): PerformanceHistory {
    val loans = accounts.loanAccounts
    val savings = accounts.savingsAccounts

    return PerformanceHistory(
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientAddressEntity.kt`

```kotlin
data class ClientAddressEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/address/ClientAddressScreen.kt:129</code></summary>

```kotlin
@Composable
internal fun ClientAddressContent(
    addresses: ScreenState<List<ClientAddressEntity>>,
    onAction: (ClientAddressAction) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Insets from the host [AppScaffold]'s top bar.
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientClassificationEntity.kt`

```kotlin
data class ClientClassificationEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientOptionsTypeConverters.kt:26</code></summary>

```kotlin
class ClientOptionsTypeConverters {
    @ColumnTypeConverter
    fun fromClientClassificationEntity(clientClassification: ClientClassificationEntity?): String? {
        return clientClassification?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientDateEntity.kt`

```kotlin
data class ClientDateEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/charge/entity/ChargesEntity.kt:65</code></summary>

```kotlin
    val chargeTimeType: ChargeTimeTypeEntity? = null,

    val chargeDueDate: ClientDateEntity? = null,

    val dueDate: List<Int>? = null,

    val chargeCalculationType: ChargeCalculationTypeEntity? = null,
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientEntity.kt`

```kotlin
data class ClientEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:34</code></summary>

```kotlin
    ClientPayloadDao,
    ClientAddressIdentifierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientEntity::class)
    suspend fun insertClient(client: ClientEntity)

    @Query("SELECT * FROM Client")
    fun getAllClients(): Flow<List<ClientEntity>>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientGenderEntity.kt`

```kotlin
data class ClientGenderEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientTypeConverters.kt:66</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromClientGenderEntity(gender: ClientGenderEntity?): String? {
        return gender?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientIdentifierEntity.kt`

```kotlin
data class ClientIdentifierEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientAddressIdentifierDao.kt:53</code></summary>

```kotlin
    fun searchAddressesByQuery(query: String): Flow<List<ClientAddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientIdentifierEntity::class)
    suspend fun insertIdentifiers(identifiers: List<ClientIdentifierEntity>)

    @Query("SELECT * FROM ClientIdentifier WHERE clientId = :clientId")
    fun getIdentifiersByClientId(clientId: Int): Flow<List<ClientIdentifierEntity>>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientPayloadEntity.kt`

```kotlin
data class ClientPayloadEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientPayloadDao.kt:23</code></summary>

```kotlin
/** Offline client payload queue. */
interface ClientPayloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientPayloadEntity::class)
    suspend fun insertClientPayload(clientPayload: ClientPayloadEntity)

    @Insert(entity = DataTablePayload::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataTablePayload(dataTablePayloads: DataTablePayload)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientStatusEntity.kt`

```kotlin
data class ClientStatusEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterEntity.kt:55</code></summary>

```kotlin
    val hierarchy: String? = null,

    val status: ClientStatusEntity? = null,

    val active: Boolean? = null,

    val centerDate: CenterDateEntity? = null,
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientTypeEntity.kt`

```kotlin
data class ClientTypeEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientTypeConverters.kt:76</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromClientTypeEntity(clientType: ClientTypeEntity?): String? {
        return clientType?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/ClientsTemplateEntity.kt`

```kotlin
data class ClientsTemplateEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:127</code></summary>

```kotlin
     */
    @Transaction
    suspend fun saveClientTemplate(clientsTemplate: ClientsTemplateEntity) {
        insertClientsTemplate(clientsTemplate)
        clientsTemplate.officeOptions?.let { insertOfficeOptions(it) }
        clientsTemplate.staffOptions?.let { insertStaffOptions(it) }
        clientsTemplate.savingProductOptions?.let { insertSavingProductOptions(it) }
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/InterestTypeEntity.kt`

```kotlin
data class InterestTypeEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientOptionsTypeConverters.kt:66</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromListInterestType(date: List<InterestTypeEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/OfficeOptionsEntity.kt`

```kotlin
data class OfficeOptionsEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientOptionsTypeConverters.kt:36</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromListOfficeOptions(date: List<OfficeOptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/OptionsEntity.kt`

```kotlin
data class OptionsEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientOptionsTypeConverters.kt:56</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromListOptions(date: List<OptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/PageItem.kt`

```kotlin
data class PageItem(
```
Created by ishankhanna on 09/02/14.

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/SavingProductOptionsEntity.kt`

```kotlin
data class SavingProductOptionsEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientOptionsTypeConverters.kt:76</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromListSavingProductOptions(date: List<SavingProductOptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/client/entity/StaffOptionsEntity.kt`

```kotlin
data class StaffOptionsEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientOptionsTypeConverters.kt:46</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromListStaffOptions(date: List<StaffOptionsEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/converter/CollectionSheetTypeConverters.kt`

```kotlin
class CollectionSheetTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/dao/CollectionSheetDao.kt`

```kotlin
interface CollectionSheetDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/collectionsheet/CollectionSheetStore.kt:35</code></summary>

```kotlin
fun provideCollectionSheetStore(
    service: CollectionSheetApi,
    dao: CollectionSheetDao,
): Store<CollectionSheetKey, List<CenterDetailModel>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: CollectionSheetKey ->
        service.fetchCenterDetails(
            format = key.dateFormat,
```

</details>

- `fun observeAll(): Flow<List<CenterDetailEntity>>`
- `fun observeForStaff(staffId: Int): Flow<CenterDetailEntity?>` — The roster is read per staff member — that is the only access path the UI has.
- `suspend fun getForStaff(staffId: Int): CenterDetailEntity?`
- `suspend fun upsertAll(details: List<CenterDetailEntity>)`
- `suspend fun deleteForStaff(staffId: Int)`
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/CenterDetail.kt`

```kotlin
data class CenterDetail(
```
Created by Tarun on 25-07-2017.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/CollectionSheetRepository.kt:32</code></summary>

```kotlin
        meetingDate: String,
        scope: CoroutineScope,
    ): ScreenDataStream<List<CenterDetail>>

    /**
     * The individual collection sheet for one (office, staff, date) request — network first, and
     * offline the last sheet generated for it. A sheet with no loan due is still CONTENT here; a screen
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/CenterDetailEntity.kt`

```kotlin
data class CenterDetailEntity(
```
A staff member's centre-meeting roster — the ONE cacheable read on the collection-sheet API.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/dao/CollectionSheetDao.kt:24</code></summary>

```kotlin

    @Query("SELECT * FROM collection_sheet_center_details ORDER BY staffName ASC")
    fun observeAll(): Flow<List<CenterDetailEntity>>

    /** The roster is read per staff member — that is the only access path the UI has. */
    @Query("SELECT * FROM collection_sheet_center_details WHERE staffId = :staffId LIMIT 1")
    fun observeForStaff(staffId: Int): Flow<CenterDetailEntity?>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/ClientCollectionSheet.kt`

```kotlin
data class ClientCollectionSheet(
```
Created by Tarun on 06-07-2017.

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/CollectionSheetPreviews.kt:33</code></summary>

```kotlin
private val previewSheet = IndividualCollectionSheet(
    clients = arrayListOf(
        ClientCollectionSheet(
            clientId = 1,
            clientName = "Asha Rahman",
            loans = arrayListOf(
                LoanCollectionSheet(
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/CollectionSheetResponse.kt`

```kotlin
data class CollectionSheetResponse(
```
Created by Tarun on 25-07-2017.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/CollectionSheetRepository.kt:52</code></summary>

```kotlin
    suspend fun individualCollectionSheet(request: RequestCollectionSheetPayload): IndividualCollectionSheet?

    suspend fun productiveSheet(centerId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?

    suspend fun groupCollectionSheet(groupId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?
}
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/GroupCollectionSheet.kt`

```kotlin
data class GroupCollectionSheet(
```
Created by Tarun on 25-07-2017.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/CollectionSheetResponse.kt:26</code></summary>

```kotlin
    var dueDate: IntArray? = null,

    var groups: List<GroupCollectionSheet> = ArrayList(),

    var loanProducts: List<kpt.core.model.objects.organisations.LoanProducts> = ArrayList(),

    var paymentTypeOptions: List<PaymentTypeOptionEntity> = ArrayList(),
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/IndividualCollectionSheet.kt`

```kotlin
data class IndividualCollectionSheet(
```
Created by Tarun on 06-07-2017.

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/CollectionSheetPreviews.kt:31</code></summary>

```kotlin

// i18n:skip — preview fixtures
private val previewSheet = IndividualCollectionSheet(
    clients = arrayListOf(
        ClientCollectionSheet(
            clientId = 1,
            clientName = "Asha Rahman",
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/MeetingFallCalendar.kt`

```kotlin
data class MeetingFallCalendar(
```
Created by Tarun on 25-07-2017. `@Serializable` because it is both the wire shape of `GET centers?meetingDate=` and the JSON the `meetingFallCenters` column holds.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/converter/CollectionSheetTypeConverters.kt:21</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromMeetingFallCalendarList(meetings: List<MeetingFallCalendar>?): String =
        Json.encodeToString(meetings ?: emptyList())

    @ColumnTypeConverter
    fun toMeetingFallCalendarList(json: String): List<MeetingFallCalendar> =
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/SavingsCollectionSheet.kt`

```kotlin
data class SavingsCollectionSheet(
```
Created by Tarun on 31-07-17.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/ClientCollectionSheet.kt:27</code></summary>

```kotlin
    var attendanceType: AttendanceTypeOption? = null,

    var savings: ArrayList<SavingsCollectionSheet> = ArrayList(),
)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/mapper/CenterDetailMapper.kt`

```kotlin
fun CenterDetailEntity.toDomain(): CenterDetail = CenterDetail(
```
_No KDoc at source._

```kotlin
fun CenterDetail.toEntity(): CenterDetailEntity = CenterDetailEntity(
```
_No KDoc at source._

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

- `const val NAME = "com_mifos_mifosxdroid.db"` — On-disk SQLite file name (appId-derived).
- `const val DESKTOP_DIR_NAME = "MifosXDroid"` — Desktop (JVM) data directory under the OS app-data root (app_name-derived).

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

- `const val VERSION = 20`

### `core/database/src/commonMain/kotlin/kpt/core/database/converter/DueDateConverter.kt`

```kotlin
class DueDateConverter
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/converter/ListTypeConverters.kt`

```kotlin
class ListTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/converter/ServerTypesConverters.kt`

```kotlin
class ServerTypesConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/datatable/converter/DataTableTypeConverters.kt`

```kotlin
object DataTableTypeConverters
```
_No KDoc at source._

- `fun fromDataTable(date: DataTableEntity?): String?`
- `fun toDataTable(json: String?): DataTableEntity?`
- `fun fromListDataTable(date: List<DataTableEntity>): String`
- `fun toListDataTable(json: String): List<DataTableEntity>`
- `fun fromListColumnValue(date: List<ColumnValue>): String`
- `fun toListColumnValue(json: String?): List<ColumnValue>`
- `fun fromListColumnHeader(date: List<ColumnHeader>): String`
- `fun toListColumnHeader(json: String?): List<ColumnHeader>`
- `fun fromListDataTablePayload(date: List<DataTablePayload>): String`
- `fun toListDataTablePayload(json: String?): List<DataTablePayload>`

### `core/database/src/commonMain/kotlin/kpt/core/database/datatable/dao/ColumnValueDao.kt`

```kotlin
interface ColumnValueDao
```
_No KDoc at source._

- `suspend fun insert(columnValue: ColumnValue)`
- `suspend fun update(columnValue: ColumnValue)`
- `suspend fun delete(columnValue: ColumnValue)`
- `suspend fun getColumnValue(id: Int): ColumnValue?`

### `core/database/src/commonMain/kotlin/kpt/core/database/datatable/dao/DataTableDao.kt`

```kotlin
interface DataTableDao
```
`DataTable` had an entity and no DAO, so the registered-datatable list had no offline path at all — every screen that needed it went to the network or did without.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/datatable/DataTableStore.kt:34</code></summary>

```kotlin
fun provideDataTableStore(
    service: DataTableApi,
    dao: DataTableDao,
): Store<String, List<DataTable>> = StoreFactory.createStore(
    fetcher = Fetcher.of { appTable: String -> service.getTableOf(appTable) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { appTable: String -> dao.observeForAppTable(appTable).map { rows -> rows.map { it.toModel() } } },
```

</details>

- `fun observeForAppTable(appTable: String): Flow<List<DataTableEntity>>`
- `suspend fun deleteForAppTable(appTable: String)`
- `suspend fun insertAll(tables: List<DataTableEntity>)`
- `suspend fun replaceForAppTable(appTable: String, tables: List<DataTableEntity>)` — Replace rather than upsert: the primary key is auto-generated, so re-inserting a fetched list would append a second copy of every row instead of refreshing it.
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/datatable/entity/ColumnHeader.kt`

```kotlin
data class ColumnHeader(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/ClientDataTableFields.kt:83</code></summary>

```kotlin
@Composable
private fun DataTableFieldInput(
    column: ColumnHeader,
    value: String,
    error: DataTableFieldError?,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/datatable/entity/ColumnValue.kt`

```kotlin
data class ColumnValue(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/ClientDataTableSectionsPreview.kt:36</code></summary>

```kotlin
            dataTableColumnName = "occupation",
            columnDisplayType = "CODELOOKUP",
            columnValues = listOf(ColumnValue(id = 41, value = "Farmer"), ColumnValue(id = 42, value = "Trader")),
        ),
    ),
)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/datatable/entity/DataTableEntity.kt`

```kotlin
data class DataTableEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDataTableDao.kt:38</code></summary>

```kotlin

    @Query("SELECT * FROM DataTable where applicationTableName = :tableName")
    fun getDatatableByTableName(tableName: String): Flow<List<DataTableEntity>>

    @Query("SELECT * FROM ColumnHeader WHERE registeredTableName = :tableName")
    fun getColumnHeadersByTableName(tableName: String): Flow<List<ColumnHeader>>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/datatable/entity/DataTablePayload.kt`

```kotlin
data class DataTablePayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/ClientDataTables.kt:106</code></summary>

```kotlin
    tables: List<DataTable>,
    values: Map<DataTableField, String>,
): List<DataTablePayload> = tables.mapNotNull { table ->
    val name = table.registeredTableName ?: return@mapNotNull null
    val data = buildMap<String, Any> {
        // A DATE column is read with the payload's own format, so it is declared with it.
        put("dateFormat", ApiDateFormatter.DATE_FORMAT)
```

</details>

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

### `core/database/src/commonMain/kotlin/kpt/core/database/di/ForkDatabaseModule.kt`

```kotlin
val ForkDatabaseModule = module
```
THE FORK'S database DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
single { MyBackupScheduler(get<AppDatabase>()) }
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/document/dao/DocumentContentDao.kt`

```kotlin
interface DocumentContentDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/document/DocumentStore.kt:86</code></summary>

```kotlin
fun provideDocumentContentStore(
    service: DocumentApi,
    dao: DocumentContentDao,
): Store<DocumentContentKey, ByteArray> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: DocumentContentKey ->
        service.downloadDocument(key.entityType, key.entityId, key.documentId).readRawBytes()
    },
```

</details>

- `fun observe(entityType: String, entityId: Int, documentId: Int): Flow<DocumentContentEntity?>`
- `suspend fun upsert(content: DocumentContentEntity)`
- `suspend fun delete(entityType: String, entityId: Int, documentId: Int)`
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/document/dao/DocumentDao.kt`

```kotlin
interface DocumentDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/document/DocumentStore.kt:46</code></summary>

```kotlin
fun provideDocumentStore(
    service: DocumentApi,
    dao: DocumentDao,
): Store<DocumentKey, List<Document>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: DocumentKey -> service.getDocuments(key.entityType, key.entityId) },
    sourceOfTruth = SourceOfTruth.of(
        // An empty table reads as an empty LIST, never as null. A reader that maps "no rows" to null
```

</details>

- `fun observeForEntity(entityType: String, entityId: Int): Flow<List<DocumentEntity>>` — Observe the documents attached to one resource — the scoping every read uses.
- `fun observeById(id: Int): Flow<DocumentEntity?>`
- `suspend fun getById(id: Int): DocumentEntity?`
- `suspend fun upsertAll(documents: List<DocumentEntity>)`
- `suspend fun deleteById(id: Int)`
- `suspend fun deleteForEntity(entityType: String, entityId: Int)`
- `suspend fun deleteAll()`
- `suspend fun replaceForEntity(entityType: String, entityId: Int, documents: List<DocumentEntity>)` — Make the entity's rows EXACTLY the server's list. The store's writer used `upsertAll` alone, so a document deleted on the server (or through this app) stayed in the table forever and kept showing in the list after its own delete.

### `core/database/src/commonMain/kotlin/kpt/core/database/document/entity/DocumentContentEntity.kt`

```kotlin
class DocumentContentEntity(
```
One document's FILE — the bytes behind a `DocumentEntity` row — as the local source of truth for the `documentContent` store.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/document/dao/DocumentContentDao.kt:27</code></summary>

```kotlin
            "AND documentId = :documentId LIMIT 1",
    )
    fun observe(entityType: String, entityId: Int, documentId: Int): Flow<DocumentContentEntity?>

    @Upsert
    suspend fun upsert(content: DocumentContentEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/document/entity/DocumentEntity.kt`

```kotlin
data class DocumentEntity(
```
Document METADATA attached to a Fineract resource (client, loan, group…).

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/document/dao/DocumentDao.kt:28</code></summary>

```kotlin
        "SELECT * FROM documents WHERE parentEntityType = :entityType AND parentEntityId = :entityId ORDER BY name ASC",
    )
    fun observeForEntity(entityType: String, entityId: Int): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<DocumentEntity?>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/document/mapper/DocumentMapper.kt`

```kotlin
fun DocumentEntity.toDomain(): Document = Document(
```
_No KDoc at source._

```kotlin
fun Document.toEntity(parentEntityType: String, parentEntityId: Int): DocumentEntity = DocumentEntity(
```
`parentEntityType` / `parentEntityId` come from the REQUEST, not the response — Fineract's document payload does not echo the owner back, and they are the index this table is read by.

### `core/database/src/commonMain/kotlin/kpt/core/database/group/converter/GroupTypeConverters.kt`

```kotlin
class GroupTypeConverters
```
Created by Pronay Sarker on 17/02/2025 (7:45 AM)

### `core/database/src/commonMain/kotlin/kpt/core/database/group/dao/GroupPayloadDao.kt`

```kotlin
interface GroupPayloadDao
```
Offline group payload queue.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/group/dao/GroupsDao.kt:32</code></summary>

```kotlin
@Dao
interface GroupsDao :
    GroupPayloadDao {
    @Insert(entity = GroupEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Insert(entity = LoanAccountEntity::class, onConflict = OnConflictStrategy.REPLACE)
```

</details>

- `suspend fun insertGroupPayload(groupPayload: GroupPayloadEntity)`
- `suspend fun updateGroupPayload(payload: GroupPayloadEntity)`
- `suspend fun deleteGroupPayloadById(groupId: Int)`
- `fun getAllGroupPayloads(): Flow<List<GroupPayloadEntity>>`
- `fun observeGroupPayload(id: Int): Flow<GroupPayloadEntity?>` — One queued offline group, for the write store's source of truth.
- `suspend fun deleteAllGroupPayloads()` — The logout purge for the offline group queue.

### `core/database/src/commonMain/kotlin/kpt/core/database/group/dao/GroupsDao.kt`

```kotlin
interface GroupsDao :
```
Created by Pronay Sarker on 15/02/2025 (1:07 PM)

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/group/GroupStore.kt:36</code></summary>

```kotlin
fun provideGroupStore(
    service: GroupApi,
    dao: GroupsDao,
): Store<Unit, List<Group>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit ->
        service.getGroups(b = true, offset = 0, limit = GROUP_PAGE_LIMIT).pageItems
    },
```

</details>

- `suspend fun insertGroup(group: GroupEntity)`
- `suspend fun insertLoanAccount(loanAccount: LoanAccountEntity)`
- `suspend fun insertSavingsAccount(savingsAccount: SavingsAccountEntity)`
- `fun getAllGroups(): Flow<List<GroupEntity>>`
- `suspend fun getAllGroups(offset: Int, limit: Int): List<GroupEntity>`
- `fun getGroupById(groupId: Int): Flow<GroupEntity>`
- `fun getLoanAccountsByGroupId(groupId: Int): Flow<List<LoanAccountEntity>>`
- `fun getSavingsAccountsByGroupId(groupId: Int): Flow<List<SavingsAccountEntity>>`
- `suspend fun saveGroupWithDate(group: GroupEntity)` — Persist a group, deriving its split date columns. Was `GroupsDaoHelper.saveGroup`.
- `val id = group.id`
- `val date = if (group.activationDate.size >= 3 && id != null)`
- `fun observeGroupWithDate(groupId: Int): Flow<GroupEntity> =` — Read a group back with `activationDate` rebuilt from its columns — the inverse of `saveGroupWithDate`, so a stored group round-trips. Was `GroupsDaoHelper.getGroup`.
- `suspend fun saveGroupAccounts(groupAccounts: GroupAccounts, groupId: Int)` — Was `GroupsDaoHelper.saveGroupAccounts` — now atomic across both tables.
- `val owner = groupId.toLong()`
  _…more members; read the file._

### `core/database/src/commonMain/kotlin/kpt/core/database/group/entity/GroupAccounts.kt`

```kotlin
data class GroupAccounts(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/testing/FakeCenterRepositories.kt:83</code></summary>

```kotlin
    /** Members / accounts — Loading until a test sets them. */
    val membersState = MutableStateFlow<ScreenState<GroupWithAssociations>>(ScreenState.Loading)
    val accountsState = MutableStateFlow<ScreenState<GroupAccounts>>(ScreenState.Loading)

    override fun groupWithMembersStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupWithAssociations> =
        screenDataStreamForTesting(membersState)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/group/entity/GroupDateEntity.kt`

```kotlin
data class GroupDateEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/group/converter/GroupTypeConverters.kt:27</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromGroupDate(date: GroupDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/group/entity/GroupEntity.kt`

```kotlin
data class GroupEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/dao/CenterDao.kt:60</code></summary>

```kotlin

    @Query("SELECT * FROM GroupTable WHERE centerId = :centerId")
    fun getCenterAssociateGroups(centerId: Int): Flow<List<GroupEntity>>

    /**
     * Persist a center, deriving its split date columns from `activationDate`.
     *
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/group/entity/GroupPayloadEntity.kt`

```kotlin
data class GroupPayloadEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/group/dao/GroupPayloadDao.kt:21</code></summary>

```kotlin
/** Offline group payload queue. */
interface GroupPayloadDao {
    @Insert(entity = GroupPayloadEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupPayload(groupPayload: GroupPayloadEntity)

    @Update(entity = GroupPayloadEntity::class, onConflict = OnConflictStrategy.NONE)
    suspend fun updateGroupPayload(payload: GroupPayloadEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/group/entity/GroupWithAssociations.kt`

```kotlin
data class GroupWithAssociations(
```
Created by ishankhanna on 29/06/14.

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/testing/FakeCenterRepositories.kt:82</code></summary>

```kotlin

    /** Members / accounts — Loading until a test sets them. */
    val membersState = MutableStateFlow<ScreenState<GroupWithAssociations>>(ScreenState.Loading)
    val accountsState = MutableStateFlow<ScreenState<GroupAccounts>>(ScreenState.Loading)

    override fun groupWithMembersStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupWithAssociations> =
        screenDataStreamForTesting(membersState)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanScheduleTypeConverters.kt`

```kotlin
class LoanScheduleTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanTermTypeConverters.kt`

```kotlin
class LoanTermTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanTypeConverters.kt`

```kotlin
class LoanTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/dao/LoanDao.kt`

```kotlin
interface LoanDao :
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/loan/LoanStore.kt:35</code></summary>

```kotlin
fun provideLoanStore(
    service: LoanApi,
    dao: LoanDao,
): Store<Int, LoanWithAssociations> = StoreFactory.createStore(
    fetcher = Fetcher.of { loanId: Int -> service.getLoanByIdWithAllAssociations(loanId) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { loanId: Int ->
```

</details>

- `suspend fun saveLoanWithAssociations(loanWithAssociations: LoanWithAssociationsEntity)`
- `fun getLoanById(loanId: Int): Flow<LoanWithAssociationsEntity?>`
- `suspend fun deleteLoanById(loanId: Int)` — Per-key clear for the `loans` store (`Store.clear(loanId)`).
- `suspend fun deleteAllLoans()` — What the logout purge reaches through the `loans` store's `deleteAll`.
- `suspend fun saveLoanWithAssociationsShaped(loan: LoanWithAssociationsEntity)` — Persist a loan, stamping its id onto the summary and timeline rows and splitting the timeline's `actualDisbursementDate` into columns. Was `LoanDaoHelper.saveLoanById`.
- `fun observeLoanWithAssociationsShaped(loanId: Int): Flow<LoanWithAssociationsEntity?> =` — Read a loan back with `actualDisbursementDate` rebuilt — the inverse of `saveLoanWithAssociationsShaped`. Was `LoanDaoHelper.getLoanById`.

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/dao/LoanRepaymentDao.kt`

```kotlin
interface LoanRepaymentDao
```
Repayment queue, repayment template and payment-type options.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/dao/LoanDao.kt:26</code></summary>

```kotlin
@Dao
interface LoanDao :
    LoanRepaymentDao,
    LoanTemplateCacheDao {
    @Insert(entity = LoanWithAssociationsEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLoanWithAssociations(loanWithAssociations: LoanWithAssociationsEntity)
```

</details>

- `suspend fun insertLoanRepaymentTransaction(request: LoanRepaymentRequestEntity)`
- `suspend fun insertPaymentTypeOption(paymentTypeOption: PaymentTypeOptionEntity)`
- `fun getPaymentTypeOptions(): Flow<List<PaymentTypeOptionEntity>>`
- `suspend fun getLoanRepaymentRequest(loanId: Int): LoanRepaymentRequestEntity?`
- `suspend fun updateLoanRepaymentRequest(loanRepaymentRequest: LoanRepaymentRequestEntity)`
- `fun readAllLoanRepaymentTransaction(): Flow<List<LoanRepaymentRequestEntity>>`
- `suspend fun insertLoanRepaymentTemplate(template: LoanRepaymentTemplateEntity)`
- `suspend fun getLoanRepaymentTemplate(loanId: Int): LoanRepaymentTemplateEntity?`
- `suspend fun deleteLoanRepaymentByLoanId(loanId: Int)`
- `suspend fun deleteAllLoanRepaymentTemplates()`
- `suspend fun saveLoanRepaymentTransaction(loanId: Int, request: LoanRepaymentRequestEntity)` — Queue an offline repayment, stamping the loan and the submission time (seconds, which is what the sync payload expects). Was `LoanDaoHelper.saveLoanRepaymentTransaction`.
- `suspend fun saveLoanRepaymentTemplateFor(` — Persist a repayment template with its payment-type options. Was `LoanDaoHelper.saveLoanRepaymentTemplate` — now atomic, so the template cannot land without the options a repayment form needs to render.
- `val stamped = template.copy(loanId = loanId)`
- `fun observeLoanRepaymentTemplate(loanId: Int): Flow<LoanRepaymentTemplateEntity?> =` — Read a repayment template back with its payment-type options attached — they live in a separate table. Was `LoanDaoHelper.getLoanRepayTemplate`.
  _…more members; read the file._

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/dao/LoanTemplateCacheDao.kt`

```kotlin
interface LoanTemplateCacheDao
```
Application + disbursement template caches (payload-blob rows).

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/dao/LoanDao.kt:27</code></summary>

```kotlin
interface LoanDao :
    LoanRepaymentDao,
    LoanTemplateCacheDao {
    @Insert(entity = LoanWithAssociationsEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLoanWithAssociations(loanWithAssociations: LoanWithAssociationsEntity)

    @Query("SELECT * FROM LoanWithAssociations WHERE id = :loanId LIMIT 1")
```

</details>

- `fun observeLoanTemplate(clientId: Int, productId: Int): Flow<LoanTemplateCacheEntity?>`
- `suspend fun upsertLoanTemplate(template: LoanTemplateCacheEntity)`
- `fun observeLoanDisburseTemplate(loanId: Int): Flow<LoanDisburseTemplateCacheEntity?>`
- `suspend fun upsertLoanDisburseTemplate(template: LoanDisburseTemplateCacheEntity)`
- `suspend fun deleteLoanTemplate(clientId: Int, productId: Int)`
- `suspend fun deleteAllLoanTemplates()`
- `suspend fun deleteLoanDisburseTemplate(loanId: Int)`
- `suspend fun deleteAllLoanDisburseTemplates()`

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/ActualDisbursementDateEntity.kt`

```kotlin
data class ActualDisbursementDateEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanScheduleTypeConverters.kt:39</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromActualDisbursementDate(date: ActualDisbursementDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/Loan.kt`

```kotlin
data class Loan(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/list/CenterListScreenPreview.kt:92</code></summary>

```kotlin
                        kpt.core.model.offline.DownloadFailure(
                            "Riverside Centre · loan #42",
                            "Loan with identifier 42 does not exist",
                        ),
                    ),
                ),
            ),
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanAccountEntity.kt`

```kotlin
data class LoanAccountEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/dao/CenterDao.kt:35</code></summary>

```kotlin
    suspend fun saveCenter(center: CenterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = LoanAccountEntity::class)
    suspend fun saveLoanAccount(loanAccount: LoanAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SavingsAccountEntity::class)
    suspend fun saveSavingsAccount(savingsAccount: SavingsAccountEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanAccountSummaryEntity.kt`

```kotlin
data class LoanAccountSummaryEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanTermTypeConverters.kt:26</code></summary>

```kotlin
class LoanTermTypeConverters {
    @ColumnTypeConverter
    fun toSummary(json: String?): LoanAccountSummaryEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanRepaymentRequestEntity.kt`

```kotlin
data class LoanRepaymentRequestEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/dao/LoanRepaymentDao.kt:26</code></summary>

```kotlin
/** Repayment queue, repayment template and payment-type options. */
interface LoanRepaymentDao {
    @Insert(entity = LoanRepaymentRequestEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanRepaymentTransaction(request: LoanRepaymentRequestEntity)

    @Insert(entity = PaymentTypeOptionEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentTypeOption(paymentTypeOption: PaymentTypeOptionEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanRepaymentResponseEntity.kt`

```kotlin
data class LoanRepaymentResponseEntity(
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanRepaymentTemplateEntity.kt`

```kotlin
data class LoanRepaymentTemplateEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/dao/LoanRepaymentDao.kt:44</code></summary>

```kotlin
    fun readAllLoanRepaymentTransaction(): Flow<List<LoanRepaymentRequestEntity>>

    @Insert(entity = LoanRepaymentTemplateEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanRepaymentTemplate(template: LoanRepaymentTemplateEntity)

    @Query("SELECT * FROM LoanRepaymentTemplate WHERE loanId = :loanId LIMIT 1")
    suspend fun getLoanRepaymentTemplate(loanId: Int): LoanRepaymentTemplateEntity?
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanStatusEntity.kt`

```kotlin
data class LoanStatusEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/MeetingFallCalendar.kt:47</code></summary>

```kotlin
    val staffName: String? = null,

    val status: LoanStatusEntity? = null,

    val totalCollected: Int = 0,

    val totalOverdue: Int = 0,
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt`

```kotlin
data class LoanTemplate(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationViewModel.kt:231</code></summary>

```kotlin

        viewModelScope.launch {
            val template = repository.groupLoanTemplate(state.groupId, productId)

            mutableStateFlow.value = if (template == null) {
                state.copy(isLoadingTemplate = false, templateError = true)
            } else {
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplateCacheEntity.kt`

```kotlin
data class LoanTemplateCacheEntity(
```
The loan-application template, cached so an officer can start a loan application out of signal.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/dao/LoanTemplateCacheDao.kt:28</code></summary>

```kotlin
            "WHERE clientId = :clientId AND productId = :productId LIMIT 1",
    )
    fun observeLoanTemplate(clientId: Int, productId: Int): Flow<LoanTemplateCacheEntity?>

    @Upsert
    suspend fun upsertLoanTemplate(template: LoanTemplateCacheEntity)
```

</details>

```kotlin
data class LoanDisburseTemplateCacheEntity(
```
The disbursement template for one loan — what the disburse form needs (expected amounts, payment types, dates). Cached so a disbursement can be prepared offline and queued.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/dao/LoanTemplateCacheDao.kt:34</code></summary>

```kotlin

    @Query("SELECT * FROM loan_disburse_templates WHERE loanId = :loanId LIMIT 1")
    fun observeLoanDisburseTemplate(loanId: Int): Flow<LoanDisburseTemplateCacheEntity?>

    @Upsert
    suspend fun upsertLoanDisburseTemplate(template: LoanDisburseTemplateCacheEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTimelineEntity.kt`

```kotlin
data class LoanTimelineEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterEntity.kt:63</code></summary>

```kotlin
    val activationDate: List<Int?> = emptyList(),

    val timeline: LoanTimelineEntity? = null,

    val externalId: String? = null,
)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTransactionTemplate.kt`

```kotlin
data class LoanTransactionTemplate(
```
Created by Rajan Maurya on 14/02/17.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/loan/LoanCommandRepositoryTest.kt:192</code></summary>

```kotlin
        override suspend fun calculateLoanSchedule(loansPayload: LoansPayload?): RepaymentSchedule = unused()
        override suspend fun getLoansAccountTemplate(clientId: Int, productId: Int): LoanTemplate = unused()
        override suspend fun getLoanTransactionTemplate(
            loanId: Int,
            command: String?,
        ): LoanTransactionTemplate = unused()
        override suspend fun getGroupLoansAccountTemplate(groupId: Int, productId: Int): GroupLoanTemplate = unused()
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTypeEntity.kt`

```kotlin
data class LoanTypeEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanTypeConverters.kt:40</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromLoanType(type: LoanTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanWithAssociationsEntity.kt`

```kotlin
data class LoanWithAssociationsEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/dao/LoanDao.kt:28</code></summary>

```kotlin
    LoanRepaymentDao,
    LoanTemplateCacheDao {
    @Insert(entity = LoanWithAssociationsEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLoanWithAssociations(loanWithAssociations: LoanWithAssociationsEntity)

    @Query("SELECT * FROM LoanWithAssociations WHERE id = :loanId LIMIT 1")
    fun getLoanById(loanId: Int): Flow<LoanWithAssociationsEntity?>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/note/dao/NoteDao.kt`

```kotlin
interface NoteDao
```
Notes attached to a client (or other Fineract resource). `NoteEntity` shipped without a DAO, so notes had no read path out of Room and the feature was network-only.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/note/NoteStore.kt:44</code></summary>

```kotlin
fun provideNoteStore(
    service: NoteApi,
    dao: NoteDao,
): Store<NoteKey, List<Note>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: NoteKey ->
        service.retrieveListNotes(resourceType = key.resourceType, resourceId = key.resourceId)
            .map { it.toModel() }
```

</details>

- `suspend fun insertNotes(notes: List<NoteEntity>)`
- `fun getNotesFor(resourceType: String, resourceId: Long): Flow<List<NoteEntity>>`
- `fun observeById(id: Long): Flow<NoteEntity?>`
- `suspend fun deleteById(id: Long)`
- `suspend fun deleteNotesFor(resourceType: String, resourceId: Long)`
- `suspend fun deleteAll()`
- `suspend fun replaceFor(resourceType: String, resourceId: Long, notes: List<NoteEntity>)` — Make the client's rows EXACTLY the server's list. The store's writer used `insertNotes` alone, so a note deleted on the server (or on another device) stayed in the table forever.

### `core/database/src/commonMain/kotlin/kpt/core/database/note/entity/NoteEntity.kt`

```kotlin
data class NoteEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/note/dao/NoteDao.kt:32</code></summary>

```kotlin
interface NoteDao {

    @Insert(entity = NoteEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Query("SELECT * FROM Note WHERE resourceType = :resourceType AND clientId = :resourceId ORDER BY createdOn DESC")
    fun getNotesFor(resourceType: String, resourceId: Long): Flow<List<NoteEntity>>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/office/converter/OfficeTypeConverters.kt`

```kotlin
class OfficeTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/office/dao/OfficeDao.kt`

```kotlin
interface OfficeDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/office/OfficeStore.kt:42</code></summary>

```kotlin
fun provideOfficeStore(
    service: OfficeApi,
    dao: OfficeDao,
): Store<Unit, List<Office>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit -> service.allOffices() },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllOffices().map { rows -> rows.map { it.toModel() } } },
```

</details>

- `suspend fun insertOffices(officeEntity: List<OfficeEntity>)`
- `fun getAllOffices(): Flow<List<OfficeEntity>>`
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/office/entity/OfficeEntity.kt`

```kotlin
data class OfficeEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/office/dao/OfficeDao.kt:24</code></summary>

```kotlin
interface OfficeDao {

    @Insert(entity = OfficeEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffices(officeEntity: List<OfficeEntity>)

    @Query("SELECT * FROM Office")
    fun getAllOffices(): Flow<List<OfficeEntity>>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/office/entity/OfficeOpeningDateEntity.kt`

```kotlin
data class OfficeOpeningDateEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/office/converter/OfficeTypeConverters.kt:26</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromOfficeOpeningDate(status: OfficeOpeningDateEntity?): String? {
        return status?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/offline/dao/OfflineSyncDao.kt`

```kotlin
interface OfflineSyncDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/offline/impl/OfflineSyncRepositoryImpl.kt:45</code></summary>

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
internal class OfflineSyncRepositoryImpl(
    private val dao: OfflineSyncDao,
    private val engine: OfflineSyncEngine,
    private val scheduler: OfflineSyncScheduler,
    private val settingsStore: OfflineSyncSettingsStore,
    private val preferences: ForkPreferencesRepository,
```

</details>

- `fun observeRecords(userId: Long): Flow<List<OfflineRecordEntity>>`
- `suspend fun records(userId: Long): List<OfflineRecordEntity>`
- `suspend fun record(userId: Long, kind: String, id: Int): OfflineRecordEntity?`
- `suspend fun upsertRecord(record: OfflineRecordEntity)`
- `suspend fun setStatus(userId: Long, kind: String, id: Int, status: String)`
- `suspend fun moveStatus(userId: Long, from: String, to: String)`
- `suspend fun deleteRecord(userId: Long, kind: String, id: Int)`
- `suspend fun deleteChildren(userId: Long, parentKind: String, parentId: Int)`
- `suspend fun children(userId: Long, parentKind: String, parentId: Int): List<OfflineRecordEntity>`
- `fun observeLatestJob(userId: Long): Flow<SyncJobEntity?>`
- `suspend fun runningJob(userId: Long): SyncJobEntity?`
- `suspend fun insertJob(job: SyncJobEntity): Long`
- `suspend fun updateJob(job: SyncJobEntity)`
- `suspend fun trimJobs(userId: Long, keep: Int)` — Keep the newest few jobs per user — the screen shows the running or the last one.

### `core/database/src/commonMain/kotlin/kpt/core/database/offline/entity/OfflineSyncEntities.kt`

```kotlin
data class OfflineRecordEntity(
```
A centre, group or client held on the device for Offline sync — chosen by the officer or brought in through a parent. Keyed by user as well as record, so on a shared field device one officer's inventory is never shown to the next.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/offline/impl/OfflineSyncRepositoryImpl.kt:230</code></summary>

```kotlin
}

private fun OfflineRecord.toEntity(userId: Long) = OfflineRecordEntity(
    userId = userId, kind = kind.name, id = id, name = name,
    parentKind = parentKind?.name, parentId = parentId, parentName = parentName, chosen = chosen,
    groups = groups, clients = clients, loans = loans, savings = savings, documents = documents,
    status = status.name, refreshedAtMs = refreshedAtMs, failedCount = failedCount,
```

</details>

```kotlin
data class SyncJobEntity(
```
One background download — durable so its progress survives the screen and the process. `targets` and `failures` are line-encoded (`KIND\tid\tname`, `item\treason`): they are only ever read back whole, so a child table would be ceremony.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/offline/impl/OfflineSyncRepositoryImpl.kt:149</code></summary>

```kotlin
    private suspend fun runJob(user: Long, targets: List<SyncTarget>) {
        val started = nowMs()
        var job = SyncJobEntity(
            userId = user, status = SyncJobStatus.RUNNING.name, targets = targets.encodeTargets(),
            done = 0, total = targets.size, reads = 0, currentLabel = null, failures = "",
            startedAtMs = started, finishedAtMs = null,
        )
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/payment/entity/PaymentTypeOptionEntity.kt`

```kotlin
data class PaymentTypeOptionEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/CollectionSheetResponse.kt:30</code></summary>

```kotlin
    var loanProducts: List<kpt.core.model.objects.organisations.LoanProducts> = ArrayList(),

    var paymentTypeOptions: List<PaymentTypeOptionEntity> = ArrayList(),

    var savingsProducts: List<SavingsProduct> = ArrayList(),
)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/recent/dao/RecentRecordDao.kt`

```kotlin
interface RecentRecordDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/recent/RecentRecordsRepository.kt:33</code></summary>

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class RecentRecordsRepositoryImpl(
    private val dao: RecentRecordDao,
    private val preferences: ForkPreferencesRepository,
) : RecentRecordsRepository {

    override val recent: Flow<List<RecentRecord>> =
```

</details>

- `fun observe(userId: Long, limit: Int): Flow<List<RecentRecordEntity>>`
- `suspend fun upsert(record: RecentRecordEntity)`
- `suspend fun trim(userId: Long, keep: Int)` — Keep the newest `keep` per user — the card shows a handful, the table never grows.
- `suspend fun record(record: RecentRecordEntity, keep: Int)` — Reopening a record moves it to the top (same key, newer time) rather than duplicating it.

### `core/database/src/commonMain/kotlin/kpt/core/database/recent/entity/RecentRecordEntity.kt`

```kotlin
data class RecentRecordEntity(
```
A record the signed-in officer opened, for Home's "Recently opened". Keyed by user as well as record, so on a shared field device one officer's history is never shown to the next.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/recent/RecentRecordsRepository.kt:53</code></summary>

```kotlin

private fun RecentRecord.toEntity(userId: Long, at: Long) =
    RecentRecordEntity(
        userId = userId,
        kind = kind.name,
        id = id,
        title = title,
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/recurringdeposit/dao/RecurringDepositDao.kt`

```kotlin
interface RecurringDepositDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/recurringdeposit/RecurringDepositTemplateStore.kt:42</code></summary>

```kotlin
fun provideRecurringDepositTemplateStore(
    service: RecurringAccountApi,
    dao: RecurringDepositDao,
): Store<Int, RecurringDepositAccountTemplate> = StoreFactory.createStore(
    fetcher = Fetcher.of { clientId: Int ->
        service.getRecurringDepositAccountTemplate(clientId = clientId, productId = null)
    },
```

</details>

- `fun observeTemplate(clientId: Int): Flow<RecurringDepositTemplateEntity?>` — Observe the cached opening template for one client.
- `suspend fun getTemplate(clientId: Int): RecurringDepositTemplateEntity?`
- `suspend fun upsert(template: RecurringDepositTemplateEntity)`
- `suspend fun deleteForClient(clientId: Int)`
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/recurringdeposit/entity/RecurringDepositTemplateEntity.kt`

```kotlin
data class RecurringDepositTemplateEntity(
```
The recurring-deposit account-opening TEMPLATE, cached per client. `RecurringDepositAccountTemplate` is a form-options payload — ~20 nested objects (`accountChart`, `chargeOptions`, `fieldOfficerOptions`, the interest/period enums…).

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/recurringdeposit/dao/RecurringDepositDao.kt:25</code></summary>

```kotlin
    /** Observe the cached opening template for one client. */
    @Query("SELECT * FROM recurring_deposit_templates WHERE clientId = :clientId LIMIT 1")
    fun observeTemplate(clientId: Int): Flow<RecurringDepositTemplateEntity?>

    @Query("SELECT * FROM recurring_deposit_templates WHERE clientId = :clientId LIMIT 1")
    suspend fun getTemplate(clientId: Int): RecurringDepositTemplateEntity?
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/report/dao/ReportDao.kt`

```kotlin
interface ReportDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/report/ReportStore.kt:34</code></summary>

```kotlin
fun provideReportStore(
    service: RunReportsApi,
    dao: ReportDao,
): Store<Unit, List<ClientReportTypeItem>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit ->
        service.getReportCategories(category = null, genericResultSet = true, parameterType = false)
    },
```

</details>

- `fun observeAll(): Flow<List<ReportTypeEntity>>`
- `fun observeByCategory(category: String): Flow<List<ReportTypeEntity>>` — The catalogue is browsed by category, which is why that column is indexed.
- `fun observeParametersFor(reportId: Int): Flow<List<ReportTypeEntity>>`
- `suspend fun getById(reportId: Int): ReportTypeEntity?`
- `suspend fun upsertAll(reports: List<ReportTypeEntity>)`
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/report/entity/ReportTypeEntity.kt`

```kotlin
data class ReportTypeEntity(
```
A runnable report definition (the CATALOGUE, not a result set). Definitions are slow-changing server reference data, so caching them lets a field officer browse available reports offline.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/report/dao/ReportDao.kt:24</code></summary>

```kotlin

    @Query("SELECT * FROM report_types ORDER BY reportName ASC")
    fun observeAll(): Flow<List<ReportTypeEntity>>

    /** The catalogue is browsed by category, which is why that column is indexed. */
    @Query("SELECT * FROM report_types WHERE reportCategory = :category ORDER BY reportName ASC")
    fun observeByCategory(category: String): Flow<List<ReportTypeEntity>>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/report/mapper/ReportTypeMapper.kt`

```kotlin
fun ReportTypeEntity.toDomain(): ClientReportTypeItem = ClientReportTypeItem(
```
_No KDoc at source._

```kotlin
fun ClientReportTypeItem.toEntityOrNull(): ReportTypeEntity?
```
`(reportId, parameterId)` is the composite primary key, so a row missing either cannot be cached; the caller drops those rather than inventing a 0 that would collide.

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt`

```kotlin
class SavingsPeriodTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt`

```kotlin
class SavingsTypeConverters
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/dao/SavingsDao.kt`

```kotlin
interface SavingsDao :
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/queue/OfflineQueueStores.kt:117</code></summary>

```kotlin
@CacheKey(fn = "byAccount", key = "savingsTransactionQueue:{accountId}", params = ["accountId:Int"])
fun provideSavingsTransactionQueueStore(
    dao: SavingsDao,
): MutableStore<Int, SavingsAccountTransactionRequest> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { accountId: Int -> dao.getSavingsAccountTransactionRequest(accountId).map { it?.toModel() } },
        writer = { _: Int, request: SavingsAccountTransactionRequest ->
```

</details>

- `suspend fun insertAllTransactions(transaction: List<SavingsAccountTransactionEntity>)`
- `suspend fun insertSavingsAccountWithAssociations(`
- `suspend fun insertAllPaymentTypeOption(paymentTypeOption: List<PaymentTypeOptionEntity>)`
- `suspend fun insertSavingsAccountTransactionTemplate(transactionTemplate: SavingsAccountTransactionTemplateEntity)`
- `suspend fun getAllTransactions(savingsAccountId: Int): List<SavingsAccountTransactionEntity>`
- `fun getSavingsAccountTransactionTemplate(savingsAccountId: Int): Flow<SavingsAccountTransactionTemplateEntity?>`
- `fun getSavingsAccountWithAssociations(savingsAccountId: Int): Flow<SavingsAccountWithAssociationsEntity?>`
- `suspend fun deleteSavingsAccountWithAssociations(savingsAccountId: Int)` — Per-key clear for `savingsAccounts`' source of truth (`Store.clear(key)`).
- `suspend fun deleteAllSavingsAccountsWithAssociations()` — What the logout purge reaches through `savingsAccounts`' `deleteAll`.
- `fun getAllPaymentTypeOption(): Flow<List<PaymentTypeOptionEntity>>`
- `suspend fun saveSavingsAccountWithTransactions(` — Persist a savings account with its transactions, stamping the account onto each transaction and the summary, and splitting each transaction date into columns. Was `SavingsDaoHelper.saveSavingsAccount` — now atomic across both tables.
- `val transactions = account.transactions.map { transaction ->`
- `val id = transaction.id ?: return@map transaction`
- `val shaped = account.copy(`
  _…more members; read the file._

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/dao/SavingsTransactionRequestDao.kt`

```kotlin
interface SavingsTransactionRequestDao
```
Offline savings-transaction request queue.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/dao/SavingsDao.kt:30</code></summary>

```kotlin
@Dao
interface SavingsDao :
    SavingsTransactionRequestDao {
    @Insert(entity = SavingsAccountTransactionEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transaction: List<SavingsAccountTransactionEntity>)

    @Insert(entity = SavingsAccountWithAssociationsEntity::class, onConflict = OnConflictStrategy.REPLACE)
```

</details>

- `suspend fun insertSavingsAccountTransactionRequest(transactionRequest: SavingsAccountTransactionRequestEntity)`
- `suspend fun updateSavingsAccountTransactionRequest(transactionRequest: SavingsAccountTransactionRequestEntity)`
- `suspend fun deleteSavingsAccountTransactionRequest(savingsAccountId: Int)`
- `suspend fun deleteAllSavingsAccountTransactionRequests()` — The logout purge for the offline savings-transaction queue.
- `fun getAllSavingsAccountTransactionRequest(): Flow<List<SavingsAccountTransactionRequestEntity>>`
- `fun getSavingsAccountTransactionRequest(savingsAccountId: Int): Flow<SavingsAccountTransactionRequestEntity?>`
- `suspend fun saveTransactionRequestFor(` — Queue an offline savings transaction, stamping the account, its type and the transaction kind. Was `SavingsDaoHelper.saveSavingsAccountTransaction`.

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/Charge.kt`

```kotlin
data class Charge(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormScreen.kt:218</code></summary>

```kotlin

        AppTextFieldDropdown(
            value = state.selectedCharge?.name.orEmpty(),
            // The chosen CHARGE is dispatched, not its index.
            onOptionSelected = { index, _ ->
                state.chargeTypes.getOrNull(index)?.let { onAction(ChargeFormAction.OnChargeSelected(it)) }
            },
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingAccountCurrencyEntity.kt`

```kotlin
data class SavingAccountCurrencyEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/SavingsCollectionSheet.kt:25</code></summary>

```kotlin
    val accountStatusId: Int = 0,

    val currency: SavingAccountCurrencyEntity? = null,

    val depositAccountType: String? = null,

    val dueAmount: Int = 0,
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingAccountDepositTypeEntity.kt`

```kotlin
data class SavingAccountDepositTypeEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/converter/CenterTypeConverters.kt:33</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromDepositType(type: SavingAccountDepositTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingProductsTemplate.kt`

```kotlin
class SavingProductsTemplate(
```
Created by rajan on 13/3/16.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/application/SavingsApplicationViewModel.kt:417</code></summary>

```kotlin
 * survived a product change and selected a different option from the new list.
 */
private fun SavingProductsTemplate.defaultSelections(): Map<SavingsField, SavingsOption> = buildMap {
    interestCompoundingPeriodTypeOptions
        .orEmpty()
        .firstOrNull { it.id == interestCompoundingPeriodType?.id }
        ?.let { o -> o.id?.let { put(SavingsField.COMPOUNDING_PERIOD, SavingsOption(it, o.value.orEmpty())) } }
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsAccountEntity.kt`

```kotlin
data class SavingsAccountEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/dao/CenterDao.kt:38</code></summary>

```kotlin
    suspend fun saveLoanAccount(loanAccount: LoanAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SavingsAccountEntity::class)
    suspend fun saveSavingsAccount(savingsAccount: SavingsAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = LoanAccountEntity::class)
    suspend fun saveMemberLoanAccount(loanAccount: LoanAccountEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsAccountStatusEntity.kt`

```kotlin
data class SavingsAccountStatusEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:59</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromSavingAccountStatus(type: SavingsAccountStatusEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsAccountSummaryEntity.kt`

```kotlin
data class SavingsAccountSummaryEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:69</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromSavingAccountSummary(type: SavingsAccountSummaryEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsAccountTransactionEntity.kt`

```kotlin
data class SavingsAccountTransactionEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/converter/ListTypeConverters.kt:53</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromListOfTransactions(list: List<SavingsAccountTransactionEntity>?): String? {
        return list?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsAccountTransactionRequestEntity.kt`

```kotlin
data class SavingsAccountTransactionRequestEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/dao/SavingsTransactionRequestDao.kt:21</code></summary>

```kotlin
/** Offline savings-transaction request queue. */
interface SavingsTransactionRequestDao {
    @Insert(entity = SavingsAccountTransactionRequestEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsAccountTransactionRequest(transactionRequest: SavingsAccountTransactionRequestEntity)

    @Update(entity = SavingsAccountTransactionRequestEntity::class, onConflict = OnConflictStrategy.NONE)
    suspend fun updateSavingsAccountTransactionRequest(transactionRequest: SavingsAccountTransactionRequestEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsAccountTransactionTemplateEntity.kt`

```kotlin
data class SavingsAccountTransactionTemplateEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/dao/SavingsDao.kt:42</code></summary>

```kotlin
    suspend fun insertAllPaymentTypeOption(paymentTypeOption: List<PaymentTypeOptionEntity>)

    @Insert(entity = SavingsAccountTransactionTemplateEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsAccountTransactionTemplate(transactionTemplate: SavingsAccountTransactionTemplateEntity)

    @Query("SELECT * FROM TransactionTable WHERE savingsAccountId = :savingsAccountId")
    suspend fun getAllTransactions(savingsAccountId: Int): List<SavingsAccountTransactionEntity>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsAccountWithAssociationsEntity.kt`

```kotlin
data class SavingsAccountWithAssociationsEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/dao/SavingsDao.kt:34</code></summary>

```kotlin
    suspend fun insertAllTransactions(transaction: List<SavingsAccountTransactionEntity>)

    @Insert(entity = SavingsAccountWithAssociationsEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsAccountWithAssociations(
        savingsAccountWithAssociations: SavingsAccountWithAssociationsEntity,
    )
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsSummaryData.kt`

```kotlin
data class SavingsSummaryData(
```
Created by Pronay Sarker on 15/08/2024 (9:49 PM)

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsTransactionData.kt`

```kotlin
data class SavingsTransactionData(
```
Created by Pronay Sarker on 15/08/2024 (11:12 PM)

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsTransactionDateEntity.kt`

```kotlin
data class SavingsTransactionDateEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:36</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromSavingsTransactionDate(date: SavingsTransactionDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingsTransactionTypeEntity.kt`

```kotlin
data class SavingsTransactionTypeEntity(
```
A COLUMN, not a table. This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON string.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:26</code></summary>

```kotlin
class SavingsTypeConverters {
    @ColumnTypeConverter
    fun fromTransactionType(type: SavingsTransactionTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/serverconfig/dao/ServerInstanceDao.kt`

```kotlin
interface ServerInstanceDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/serverconfig/ServerInstancesTest.kt:48</code></summary>

```kotlin
    }

    private class MemoryDao : ServerInstanceDao {
        val rows = MutableStateFlow<List<ServerInstanceEntity>>(emptyList())
        override fun observeAll(): Flow<List<ServerInstanceEntity>> = rows
        override suspend fun insertAll(instances: List<ServerInstanceEntity>) {
            rows.value = rows.value + instances
```

</details>

- `fun observeAll(): Flow<List<ServerInstanceEntity>>`
- `suspend fun insertAll(instances: List<ServerInstanceEntity>)`
- `suspend fun deleteAll()`
- `suspend fun replaceAll(instances: List<ServerInstanceEntity>)` — The fetched list REPLACES the cached one: an instance removed remotely leaves the picker.

### `core/database/src/commonMain/kotlin/kpt/core/database/serverconfig/entity/ServerInstanceEntity.kt`

```kotlin
data class ServerInstanceEntity(
```
One server instance the officer can pick — the local source of truth for the `serverInstances` store, filled from Supabase `app_server_config` (only this app's rows).

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/serverconfig/ServerInstancesTest.kt:49</code></summary>

```kotlin

    private class MemoryDao : ServerInstanceDao {
        val rows = MutableStateFlow<List<ServerInstanceEntity>>(emptyList())
        override fun observeAll(): Flow<List<ServerInstanceEntity>> = rows
        override suspend fun insertAll(instances: List<ServerInstanceEntity>) {
            rows.value = rows.value + instances
        }
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/shared/entity/ClientAndClientAccounts.kt`

```kotlin
class ClientAndClientAccounts
```
Model for Observable.zip. This Model used to combine the Client and ClientAccount in response of RxAndroid. Created by Rajan Maurya on 01/07/16.

### `core/database/src/commonMain/kotlin/kpt/core/database/shared/entity/ClientListArgs.kt`

```kotlin
data class ClientListArgs(
```
/* Copyright 2024 Mifos Initiative This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

### `core/database/src/commonMain/kotlin/kpt/core/database/shared/entity/DataTableDataNavigationArg.kt`

```kotlin
data class DataTableDataNavigationArg(
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/shared/entity/GroupAndGroupAccounts.kt`

```kotlin
data class GroupAndGroupAccounts(
```
Created by Rajan Maurya on 11/09/16.

### `core/database/src/commonMain/kotlin/kpt/core/database/shared/entity/LoanAndLoanRepayment.kt`

```kotlin
class LoanAndLoanRepayment
```
Created by Rajan Maurya on 08/08/16.

### `core/database/src/commonMain/kotlin/kpt/core/database/shared/entity/SavingProductsAndTemplate.kt`

```kotlin
class SavingProductsAndTemplate(
```
Created by Rajan Maurya on 02/09/16.

### `core/database/src/commonMain/kotlin/kpt/core/database/shared/entity/SavingsAccountAndTransactionTemplate.kt`

```kotlin
class SavingsAccountAndTransactionTemplate
```
Created by Rajan Maurya on 21/08/16.

### `core/database/src/commonMain/kotlin/kpt/core/database/staff/dao/StaffDao.kt`

```kotlin
interface StaffDao
```
Created by Pronay Sarker on 14/02/2025 (1:54 AM)

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/staff/StaffStore.kt:37</code></summary>

```kotlin
fun provideStaffStore(
    service: StaffApi,
    dao: StaffDao,
): Store<Int, List<Staff>> = StoreFactory.createStore(
    fetcher = Fetcher.of { officeId: Int -> service.getStaffForOffice(officeId) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { officeId: Int -> dao.getAllStaff(officeId).map { rows -> rows.map { it.toModel() } } },
```

</details>

- `suspend fun insertStaffs(staffs: List<StaffEntity>)`
- `suspend fun insertStaff(staff: StaffEntity)`
- `fun getAllStaff(officeId: Int): Flow<List<StaffEntity>>`
- `suspend fun deleteForOffice(officeId: Int)`
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/staff/entity/StaffEntity.kt`

```kotlin
data class StaffEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/staff/dao/StaffDao.kt:27</code></summary>

```kotlin
interface StaffDao {

    @Insert(entity = StaffEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffs(staffs: List<StaffEntity>)

    @Insert(entity = StaffEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/survey/converter/SurveyTypeConverters.kt`

```kotlin
class SurveyTypeConverters
```
Created by Pronay Sarker on 13/02/2025 (2:53 AM)

### `core/database/src/commonMain/kotlin/kpt/core/database/survey/dao/SurveyDao.kt`

```kotlin
interface SurveyDao
```
Created by Pronay Sarker on 12/02/2025 (9:33 PM)

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/survey/SurveyStore.kt:37</code></summary>

```kotlin
fun provideSurveyStore(
    service: SurveyApi,
    dao: SurveyDao,
): Store<Unit, List<Survey>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit -> service.allSurveys() },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllSurveys().map { rows -> rows.map { it.toModel() } } },
```

</details>

- `suspend fun insertSurvey(survey: SurveyEntity)`
- `suspend fun insertQuestionData(questionData: QuestionDatasEntity)`
- `suspend fun insertResponseData(responseDatas: ResponseDatasEntity)`
- `fun getAllSurveys(): Flow<List<SurveyEntity>>`
- `suspend fun deleteAllSurveys()`
- `suspend fun replaceAllSurveys(surveys: List<SurveyEntity>)` — Make the table EXACTLY the server's survey list. The store's writer inserted one by one, so a survey retired on the server stayed offered in the field.
- `fun getQuestionDatas(surveyId: Int): Flow<List<QuestionDatasEntity>>`
- `fun getResponseDatas(questionId: Int): Flow<List<ResponseDatasEntity>>`
- `suspend fun saveQuestionDataFor(surveyId: Int, questionData: QuestionDatasEntity): QuestionDatasEntity` — Stamp the owning survey onto question data before persisting. Was `SurveyDaoHelper`.
- `val stamped = questionData.copy(surveyId = surveyId)`
- `suspend fun saveResponseDataFor(questionId: Int, responseData: ResponseDatasEntity): ResponseDatasEntity` — Stamp the owning question onto response data before persisting. Was `SurveyDaoHelper`.
- `val stamped = responseData.copy(id = questionId)`

### `core/database/src/commonMain/kotlin/kpt/core/database/survey/entity/ComponentDatasEntity.kt`

```kotlin
data class ComponentDatasEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/survey/converter/SurveyTypeConverters.kt:37</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromComponentDatasList(componentDatas: List<ComponentDatasEntity>?): String? {
        return componentDatas?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/survey/entity/QuestionDatasEntity.kt`

```kotlin
data class QuestionDatasEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/survey/converter/SurveyTypeConverters.kt:27</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromQuestionDatasList(questionDatas: List<QuestionDatasEntity>?): String? {
        return questionDatas?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/survey/entity/ResponseDatasEntity.kt`

```kotlin
data class ResponseDatasEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/survey/converter/SurveyTypeConverters.kt:47</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromResponseDatasList(responseDatas: List<ResponseDatasEntity>?): String? {
        return responseDatas?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/survey/entity/SurveyEntity.kt`

```kotlin
data class SurveyEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/survey/dao/SurveyDao.kt:30</code></summary>

```kotlin
interface SurveyDao {

    @Insert(entity = SurveyEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: SurveyEntity)

    @Insert(entity = QuestionDatasEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionData(questionData: QuestionDatasEntity)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/utils/GetCurrentTimeInMillis.kt`

```kotlin
expect fun getCurrentTimeInMillis(): Long
```
_No KDoc at source._

---

_150 type(s), 280 function(s)/property(ies); 129 carry KDoc at source; 1 authored example(s); 122 live call site(s)._
<!-- api-docs:end -->
