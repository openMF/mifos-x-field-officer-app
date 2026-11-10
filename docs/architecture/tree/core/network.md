# `core/network`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_NETWORK.md`
> **Measured:** 112 Kotlin files, 19 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@ApiBinding` | `network-ksp` | di/GeneratedApiBindings |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`AbstractMapper`, `AppAccessPoints`, `AppMultiUrlConfigProvider`, `AppServerConfig`, `AppUrlTypes`, `AppUserDto`, `AuthApi`, `BatchRequestDto`, `BatchResponseDto`, `BundledInstanceConfigLoader`, `BusinessDateDto`, `CenterApi`, `CenterMapper`, `ChargeApi`  …and 118 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/network sha=f60e15d9f6c0442966242fb05812035df8b78ca1 -->
## API reference

_Generated from `core/network` at tree `f60e15d9f6c0` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt`

```kotlin
object AppAccessPoints
```
The per-fork list of network access points this app talks to — REST and Supabase in ONE place.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:76</code></summary>

```kotlin
    // The restApi("<id>") DSL and AppMultiUrlConfigProvider both resolve transports/base-URLs from this.
    // + the app-wide network privacy policy (logging per build type, query redaction, web CORS relay).
    single { AccessPointRegistry(AppAccessPoints.points, AppAccessPoints.policy) }

    // Unified access-point provider — resolves every named UrlType to its REST base URL from the
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
```

</details>

- `val points: List<AccessPoint> = listOf(` — This project's declared access points, in app-profile order.
- `val policy: NetworkPolicy = NetworkPolicy(` — Network privacy policy — app-profile `network.logging` / `network.web_cors_proxy`.

### `core/network/src/commonMain/kotlin/kpt/core/network/config/AppMultiUrlConfigProvider.kt`

```kotlin
class AppMultiUrlConfigProvider(
```
Concrete `MultiUrlConfigProvider` backed by the declarative `AccessPointRegistry`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:81</code></summary>

```kotlin
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
    single<MultiUrlConfigProvider> { AppMultiUrlConfigProvider(get()) }

    // Supabase clients are bound in core-base NetworkBaseModule (included above): each point's publishable
    // key is on its generated AccessPoint, so they need nothing from here.
    // Map<id, client> — every declared Supabase access point materializes exactly one client.
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/config/AppUrlTypes.kt`

```kotlin
object AppUrlTypes
```
Project-level catalogue of named API endpoints for runtime base-URL switching (see `kpt.core.base.network.MultiUrlConfigProvider` / `kpt.core.base.network.DynamicBaseUrlPlugin`).

- `val MIFOS: UrlType = UrlType("MIFOS")` — `mifos` — REST access point.
- `val LWMSWHOXVVOAGZKQXIYD: UrlType = UrlType("LWMSWHOXVVOAGZKQXIYD")` — `lwmswhoxvvoagzkqxiyd` — Supabase access point.
- `val all: List<UrlType> = listOf(` — Every declared endpoint type, in app-profile order.

### `core/network/src/commonMain/kotlin/kpt/core/network/config/KptNetworkHeaders.kt`

```kotlin
object KptNetworkHeaders : DefaultHeaderProvider
```
THE FORK'S default request headers. Neutral on the template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
object KptNetworkHeaders : DefaultHeaderProvider {
    override fun headersFor(accessPointId: String): Map<String, String> = when (accessPointId) {
        "main" -> mapOf(
            "X-Client-Version" to BuildKonfig.VERSION_NAME,
            "X-Platform" to platformName(),
        )
        else -> emptyMap()
    }
}
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:61</code></summary>

```kotlin
    // built by `restApi(...)` resolves this, so a fork adds an app-wide header without hand-building
    // a Ktorfit and giving up the generated @ApiBinding wiring. Neutral on the template.
    single<DefaultHeaderProvider> { KptNetworkHeaders }

    // Every declared endpoint's Koin binding, GENERATED from app-profile#network.access_points into
    // the sibling [GeneratedApiBindings] (same package — no import, so this file keeps its zero-demo
    // -imports property and stays blind-copyable on a template sync). It lives HERE rather than in the
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/di/ForkNetworkModule.kt`

```kotlin
val ForkNetworkModule = module
```
THE FORK'S network DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
single<MyApiConfig> { MyApiConfig(apiKey = BuildKonfig.MY_API_KEY.takeIf { it.isNotBlank() }) }
single<SupabaseExtrasProvider> {
    SupabaseExtrasProvider { id ->
        when (id) {
            "<your-project-ref>" -> {
                {
                    install(Auth) { /* scheme/host for the OAuth redirect */ }
                    install(ComposeAuth) { /* native Google/Apple sign-in */ }
                }
            }
            // Several projects sharing a module — one branch, no repetition.
            "<project-b>", "<project-c>" -> {
                { install(Realtime) }
            }
            else -> {
                {}
            }
        }
    }
}
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt`

```kotlin
val NetworkModule = module
```
Koin module for `core/network` — the Ktorfit API bindings over the shared client.

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

### `core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/ServerInstanceApi.kt`

```kotlin
interface ServerInstanceApi
```
The `app_server_config` table's API — one row per server instance. The CONTRACT, no Supabase types: a consumer is tested with a plain fake; `impl.ServerInstanceApiImpl` is the only Supabase-aware part.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/serverconfig/ServerInstancesTest.kt:39</code></summary>

```kotlin
        override val isConfigured: Boolean = true,
        private val failure: Throwable? = null,
    ) : ServerInstanceApi {
        val asked = mutableListOf<String>()
        override suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto> {
            asked += app
            failure?.let { throw it }
```

</details>

- `val isConfigured: Boolean` — True when a real project URL + publishable key are configured.
- `suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto>` — The enabled instances whose `supported_apps` contains `app`, in `sort_order` — filtered BY THE SERVER, so one app never downloads another's servers. Empty when unconfigured.

### `core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/impl/ServerInstanceApiImpl.kt`

```kotlin
class ServerInstanceApiImpl(
```
Supabase implementation of `ServerInstanceApi`. Row-level security limits the publishable key (the `anon` role) to enabled rows, read-only; the `supported_apps` containment filter runs in Postgres (GIN-indexed).

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/config/SupabaseAccessPointTest.kt:95</code></summary>

```kotlin
        // (`supabaseApi<ServerInstanceApi>("<id>") { ServerInstanceApiImpl(it) }`), so this asserts against
        // the contract a consumer injects rather than against the implementation.
        val api: ServerInstanceApi = ServerInstanceApiImpl(factory().requireClientFor(point.id))
        assertFalse(api.isConfigured)
        // The guard matters: without it this would reach `client`, build a Supabase client on an empty
        // key, and fail at app start-up on every unconfigured fork.
        assertTrue(api.fetchInstances("field-officer").isEmpty(), "an unconfigured client reads nothing")
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/dto/RemoteServerInstanceDto.kt`

```kotlin
data class RemoteServerInstanceDto(
```
One row of `public.app_server_config` — the Mifos server list, ONE ROW PER INSTANCE. It replaces the deprecated `app_config` table (a single row holding an `instances` blob).

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/serverconfig/ServerInstancesTest.kt:36</code></summary>

```kotlin

    private class FakeServerInstanceApi(
        private val rows: List<RemoteServerInstanceDto>,
        override val isConfigured: Boolean = true,
        private val failure: Throwable? = null,
    ) : ServerInstanceApi {
        val asked = mutableListOf<String>()
```

</details>

```kotlin
data class RemoteDemoAccountDto(val role: String, val username: String, val password: String)
```
A PUBLISHED demo sign-in of a public demo instance — public by design, not a credential to protect.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/serverconfig/ServerInstancesTest.kt:68</code></summary>

```kotlin
            platformTenantId = "t",
            supportedApps = listOf("field-officer"),
            demoAccounts = listOf(RemoteDemoAccountDto("Officer", "u", "p")),
        ),
        RemoteServerInstanceDto(
            label = "Second",
            protocol = "http://",
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/auth/api/AuthApi.kt`

```kotlin
interface AuthApi
```
Fineract authentication.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/auth/impl/AuthCommandRepositoryImpl.kt:36</code></summary>

```kotlin
@RepositoryBinding(binds = AuthCommandRepository::class)
internal class AuthCommandRepositoryImpl(
    private val authApi: AuthApi,
    private val gateway: MutationGateway,
    private val preferences: ForkPreferencesRepository,
    private val storeCacheManager: StoreCacheManager,
) : AuthCommandRepository {
```

</details>

- `suspend fun authenticate(` — Signs in with a username and password; the response carries the user, their roles and the Basic-auth key.
- `suspend fun changePassword(` — The signed-in user's own password. A validation refusal (policy, mismatch) is a 400 whose `errors[]` carry the reasons; the success body (a command result) is not read.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/api/CenterApi.kt`

```kotlin
interface CenterApi
```
Fineract endpoints for center.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/center/impl/CenterCommandRepositoryImpl.kt:54</code></summary>

```kotlin
@RepositoryBinding(binds = CenterCommandRepository::class)
internal class CenterCommandRepositoryImpl(
    private val centerApi: CenterApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.CenterGetCenterWithGroupMembersAndCollectionMeetingCalendar)
    private val centerStore: Store<Int, CenterWithAssociations>,
    @FromStore(AppStoreIds.CenterItem) private val itemStore: MutableStore<Int, Center>,
```

</details>

- `suspend fun retrieveAll23(` — List Centers The default implementation supports pagination and sorting with the default pagination size set to 200 records. The parameter limit with description -1 will return all entries.
- `suspend fun activate2(` — Activate a Center | Generate Collection Sheet | Save Collection Sheet | Close a Center | Associate Groups | Disassociate Groups Activate a Center: Centers can be created in a Pending state. This API exists to enable center activation.
- `suspend fun getCenters(` — Reads one page of centres.
- `suspend fun getCenterAccounts(@Path("centerId") centerId: Int): CenterAccounts` — Reads a centre's loan and savings accounts.
- `suspend fun getCenterWithGroupMembersAndCollectionMeetingCalendar(` — Reads a centre with its groups and its collection-meeting calendar.
- `suspend fun getAllCentersInOffice(` — Reads the centres of one office.
- `suspend fun getAllGroupsForCenter(@Path("centerId") centerId: Int): CenterWithAssociations` — Reads a centre with its groups.
- `suspend fun getCollectionSheet(` — Generates the centre's collection sheet for a meeting.
- `suspend fun saveCollectionSheet(` — Saves a centre's collection sheet.
- `suspend fun saveCollectionSheetAsync(` — Saves a centre's collection sheet and returns the raw response.
- `suspend fun createCenter(@Body centerPayload: CenterPayload?): SaveResponse` — Creates a centre.
- `suspend fun getCenterList(` — Reads the centre list with the given filters.
- `suspend fun activateCenter(` — This is the service to activate the center REST ENT POINT https://demo.openmf.org/fineract-provider/api/v1/centers/{centerId}?command=activate

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/dto/GetCentersPageItems.kt`

```kotlin
data class GetCentersPageItems(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/dto/GetCentersResponse.kt:24</code></summary>

```kotlin
data class GetCentersResponse(

    val pageItems: Set<GetCentersPageItems>? = null,

    val totalFilteredRecords: Int? = null,

)
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/dto/GetCentersResponse.kt`

```kotlin
data class GetCentersResponse(
```
GetCentersResponse

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/api/CenterApi.kt:78</code></summary>

```kotlin
        @Query("dateFormat") dateFormat: String? = null,
        @Query("locale") locale: String? = null,
    ): GetCentersResponse

    /**
     * Activate a Center | Generate Collection Sheet | Save Collection Sheet | Close a Center | Associate Groups |
     * Disassociate Groups
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/dto/GetCentersStatus.kt`

```kotlin
data class GetCentersStatus(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/dto/GetCentersPageItems.kt:41</code></summary>

```kotlin
    val officeName: String? = null,

    val status: GetCentersStatus? = null,

)
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/mapper/CenterMapper.kt`

```kotlin
object CenterMapper : AbstractMapper<GetCentersPageItems, Center>()
```
Maps center between its network, stored and domain shapes.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/mapper/GetCentersResponseMapper.kt:23</code></summary>

```kotlin
        return Page<Center>().apply {
            totalFilteredRecords = entity.totalFilteredRecords!!
            pageItems = CenterMapper.mapFromEntityList(entity.pageItems!!.toList())
        }
    }

    override fun mapToEntity(domainModel: Page<Center>): GetCentersResponse {
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/mapper/GetCentersResponseMapper.kt`

```kotlin
object GetCentersResponseMapper : AbstractMapper<GetCentersResponse, Page<Center>>()
```
Maps get centers response between its network, stored and domain shapes.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/charge/api/ChargeApi.kt`

```kotlin
interface ChargeApi
```
Service interface that defines all API endpoints related to charges. This includes fetching charge templates, listing charges for different resource types (clients, loans, savings accounts, etc.), creating, updating, and deleting charges.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/charge/impl/ChargeCommandRepositoryImpl.kt:47</code></summary>

```kotlin
@RepositoryBinding(binds = ChargeCommandRepository::class)
internal class ChargeCommandRepositoryImpl(
    private val chargeApi: ChargeApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Charges) private val charges: Store<ChargeKey, List<Charges>>,
    @FromStore(AppStoreIds.ChargeItem) private val chargeItem: MutableStore<ChargeItemKey, Charges>,
) : ChargeCommandRepository {
```

</details>

- `suspend fun listAllCharges(): HttpResponse` — Retrieves a list of all available charges in the system.
- `suspend fun getChargeTemplate(` — Retrieves a charge template for a specific resource type and ID.
- `suspend fun getListOfPagingCharges(` — Retrieves a paginated list of charges for a given resource type and ID.
- `suspend fun getListOfClientCharges(` — Retrieves all client-specific charges.
- `suspend fun getListOfOtherAccountCharge(` — Retrieves charges for other account types (e.g. loans, savings).
- `suspend fun getCharge(` — Retrieves a single charge by its ID.
- `suspend fun createCharges(` — Creates a new charge for the given resource type and ID.
- `suspend fun deleteCharge(` — Deletes an existing charge.
- `suspend fun updateCharge(` — Updates an existing charge.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/checkerinbox/api/CheckerInboxApi.kt`

```kotlin
interface CheckerInboxApi
```
Fineract endpoints for checker inbox.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/checkerinbox/impl/CheckerInboxCommandRepositoryImpl.kt:40</code></summary>

```kotlin
@RepositoryBinding(binds = CheckerInboxCommandRepository::class)
internal class CheckerInboxCommandRepositoryImpl(
    private val checkerInboxApi: CheckerInboxApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.CheckerTask) private val taskStore: MutableStore<Int, CheckerTask>,
) : CheckerInboxCommandRepository {
```

</details>

- `suspend fun getCheckerList(` — Reads the maker-checker entries, optionally filtered.
- `suspend fun approveCheckerEntry(@Path("auditId") auditId: Int): GenericResponse` — Approves a maker-checker entry.
- `suspend fun rejectCheckerEntry(@Path("auditId") auditId: Int): GenericResponse` — Rejects a maker-checker entry.
- `suspend fun deleteCheckerEntry(@Path("auditId") auditId: Int): GenericResponse` — Deletes a maker-checker entry.
- `suspend fun getRescheduleLoansTaskList(): List<RescheduleLoansTask>` — Reads the loan reschedule requests waiting for a decision.
- `suspend fun getCheckerInboxSearchTemplate(): CheckerInboxSearchTemplate` — Reads the entity and action names the inbox can be filtered by.
- `suspend fun getCheckerTasksFromResourceId(` — Reads the maker-checker entries for one resource.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/checkerinbox/dto/GetMakerCheckerResponse.kt`

```kotlin
data class GetMakerCheckerResponse(
```
GetMakerCheckerResponse

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/api/ClientAccountsApi.kt`

```kotlin
interface ClientAccountsApi
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/client/ClientKeyedReadStores.kt:156</code></summary>

```kotlin
@CacheKey(fn = "forKey", key = "clientGetAllAccountsOfClient:{key}", params = ["key:String"])
fun provideGetAllAccountsOfClientStore(
    clientAccountsApi: ClientAccountsApi,
    cache: ApiResponseCacheDao,
): Store<Int, ClientAccountsModel> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ClientGetAllAccountsOfClient.forKey(key.toString()) },
```

</details>

- `suspend fun getAllAccountsOfClient(@Path("clientId") clientId: Int): ClientAccounts` — Reads every account of a client.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/api/ClientApi.kt`

```kotlin
interface ClientApi : ClientDetailsApi
```
Fineract endpoints for client.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/impl/ClientCommandRepositoryImpl.kt:75</code></summary>

```kotlin
@RepositoryBinding(binds = ClientCommandRepository::class)
internal class ClientCommandRepositoryImpl(
    private val clientApi: ClientApi,
    private val clientIdentifierApi: ClientIdentifierApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.ClientGetClient) private val clientStore: Store<Int, Client>,
    @FromStore(AppStoreIds.ClientLocal) private val clientLocalStore: MutableStore<Int, Client>,
```

</details>

- `suspend fun activate1(` — Activate a Client | Close a Client | Reject a Client | Withdraw a Client | Reactivate a Client | UndoReject a Client | UndoWithdraw a Client | Assign a Staff | Unassign a Staff | Update Default Savings Account | Propose a Client Transfer | Withdraw a Client Transfer | Reject a Client Transfer | Accept a Client Transfer | Propose and Accept a Client Transfer Activate a Client: Clients can be created in a Pending state. This API exists to enable client activation (for when a client becomes an approved member of the financial Institution). If the client happens to be already active this API will result in an error. Close a Client: Clients can be closed if they do not have any non-closed loans/savingsAccount. This API exists to close a client . If the client have any active loans/savingsAccount this API will result in an error. Reject a Client: Clients can be rejected when client is in pending for activation status. If the client is any other status, this API throws an error. Mandatory Fields: rejectionDate, rejectionReasonId Withdraw a Client: Client applications can be withdrawn when client is in a pending for activation status. If the client is any other status, this API throws an error. Mandatory Fields: withdrawalDate, withdrawalReasonId Reactivate a Client: Clients can be reactivated after they have been closed. Trying to reactivate a client in any other state throws an error. Mandatory Fields: reactivationDate UndoReject a Client: Clients can be reactivated after they have been rejected. Trying to reactivate a client in any other state throws an error. Mandatory Fields: reopenedDateUndoWithdraw a Client: Clients can be reactivated after they have been withdrawn. Trying to reactivate a client in any other state throws an error. Mandatory Fields: reopenedDate Assign a Staff: Allows you to assign a Staff for existed Client. The selected Staff should belong to the same office (or an officer higher up in the hierarchy) as the Client he manages. Unassign a Staff: Allows you to unassign the Staff assigned to a Client. Update Default Savings Account: Allows you to modify or assign a default savings account for an existing Client. The selected savings account should be one among the existing savings account for a particular customer. Propose a Client Transfer: Allows you to propose the transfer of a Client to a different Office. Withdraw a Client Transfer: Allows you to withdraw the proposed transfer of a Client to a different Office. Withdrawal can happen only if the destination Branch (to which the transfer was proposed) has not already accepted the transfer proposal Reject a Client Transfer: Allows the Destination Branch to reject the proposed Client Transfer. Accept a Client Transfer: Allows the Destination Branch to accept the proposed Client Transfer. The destination branch may also choose to link this client to a group (in which case, any existing active JLG loan of the client is rescheduled to match the meeting frequency of the group) and loan Officer at the time of accepting the transfer Propose and Accept a Client Transfer: Abstraction over the Propose and Accept Client Transfer API&#39;s which enable a user with Data Scope over both the Target and Destination Branches to directly transfer a Client to the destination Office. Showing request/response for &#39;Reject a Client Transfer&#39; Responses: - 200: OK
- `suspend fun retrieveAll21(` — List Clients The list capability of clients can support pagination and sorting.
- `suspend fun retrieveAssociatedAccounts(@Path("clientId") clientId: Long): GetClientsClientIdAccountsResponse` — Retrieve client accounts overview An example of how a loan portfolio summary can be provided. This is requested in a specific use case of the community application.
- `suspend fun getAllClients(`
- `suspend fun getClient(@Path("clientId") clientId: Int): Client` — Reads a client.
- `suspend fun uploadClientImage(` — Uploads the client's photo.
- `suspend fun deleteClientImage(@Path("clientId") clientId: Int)` — Deletes the client's photo.
- `suspend fun getClientImage(@Path("clientId") clientId: Int): HttpResponse` — Downloads the client's photo.
- `suspend fun createClient(@Body clientPayload: ClientPayload?): Client?` — Creates a client.
- `suspend fun updateClient(` — Updates a client's details.
- `suspend fun getClientTemplate(): ClientsTemplate` — Reads the new-client template: offices, staff, genders, types and classifications.
- `suspend fun getClientAccounts(@Path("clientId") clientId: Int): ClientAccounts` — Reads the client's accounts.
- `suspend fun activateClient(` — This is the service to activate the client REST ENT POINT https://demo.openmf.org/fineract-provider/api/v1/clients/{clientId}?command=activate
- `suspend fun getClientTemplate(@Path("clientId") clientId: Int): GetClientsPageItemsResponse` — Reads the edit template of a client, with staff from the client's office only.
  _…more members; read the file._

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/api/ClientDetailsApi.kt`

```kotlin
interface ClientDetailsApi
```
Pinpoint locations, addresses and collateral of a client — split from `ClientApi`, which extends it.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/api/ClientApi.kt:41</code></summary>

```kotlin
/** Fineract endpoints for client. */
@ApiBinding("mifos")
interface ClientApi : ClientDetailsApi {

    /**
     * Activate a Client | Close a Client | Reject a Client | Withdraw a Client | Reactivate a Client | UndoReject a
     * Client | UndoWithdraw a Client | Assign a Staff | Unassign a Staff | Update Default Savings Account | Propose a
```

</details>

- `suspend fun getClientPinpointLocations(` — This is the service for fetching the client pinpoint locations from the dataTable "client_pinpoint_location". This DataTable entries are 1. Place Id 2. Place Address 3. latitude 4.
- `suspend fun addClientPinpointLocation(` — This is the service for adding the new Client Pinpoint Location in dataTable "client_pinpoint_location".
- `suspend fun deleteClientPinpointLocation(` — This is the service for deleting the pinpoint location from the DataTable "client_pinpoint_location".
- `suspend fun updateClientPinpointLocation(` — This is the service for updating the pinpoint location from DataTable "client_pinpoint_location" REST ENT POINT: https://demo.openmf.org/fineract-provider/api/v1/datatables/client_pinpoint_location /{appTableId}/{datatableId}
- `suspend fun getAddressConfiguration(): AddressConfiguration` — Retrieves address configuration from Global Configuration.
- `suspend fun getAddressTemplate(): AddressTemplate` — Retrieves an address template. This template can be used to pre-fill address forms or guide users in providing address information.
- `suspend fun getClientAddresses(@Path("clientId") clientId: Int): List<ClientAddressEntity>` — Reads a client's addresses.
- `suspend fun createClientAddress(` — Adds an address of one type to a client.
- `suspend fun getClientCloseTemplate(): ClientCloseTemplateResponse` — Reads the close-client template: the closure reasons.
- `suspend fun getCollateralItems(): List<CollateralItem>` — Reads the collateral types the organisation accepts.
- `suspend fun getClientCollateralItems(@Path("clientId") clientId: Int): List<CollateralItemResult>` — Reads the collateral a client may pledge.
- `suspend fun createCollateral(` — Pledges collateral for a client.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/api/ClientIdentifierApi.kt`

```kotlin
interface ClientIdentifierApi
```
Fineract endpoints for client identifier.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/impl/ClientCommandRepositoryImpl.kt:76</code></summary>

```kotlin
internal class ClientCommandRepositoryImpl(
    private val clientApi: ClientApi,
    private val clientIdentifierApi: ClientIdentifierApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.ClientGetClient) private val clientStore: Store<Int, Client>,
    @FromStore(AppStoreIds.ClientLocal) private val clientLocalStore: MutableStore<Int, Client>,
    @FromStore(AppStoreIds.ClientRow) private val clientRowStore: MutableStore<Int, Client>,
```

</details>

- `suspend fun getClientListIdentifiers(@Path("clientId") clientId: Long): List<Identifier>` — Fetches the list of identifiers for a given client.
- `suspend fun getClientIdentifiers(` — Retrieves a specific client identifier.
- `suspend fun getClientIdentifierTemplate(@Path("clientId") clientId: Long): IdentifierTemplate` — Fetches the client identifier template for a given client.
- `suspend fun deleteClientIdentifier(` — Deletes a client identifier for a given client.
- `suspend fun createClientIdentifier(` — Creates a new identifier for a given client.
- `suspend fun updateClientIdentifier(` — Updates an existing client identifier for a given client.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientClassificationOptions.kt`

```kotlin
data class GetClientClassificationOptions(
```
A client classification offered by the client template.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsPageItemsResponse.kt:84</code></summary>

```kotlin

    /** The client's classification. */
    val clientClassification: GetClientClassificationOptions? = null,

)
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientStatus.kt`

```kotlin
data class GetClientStatus(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsPageItemsResponse.kt:57</code></summary>

```kotlin
    val groupName: String? = null,

    val status: GetClientStatus? = null,

    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientTypeOptions.kt`

```kotlin
data class GetClientTypeOptions(
```
A client type offered by the client template.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsPageItemsResponse.kt:81</code></summary>

```kotlin

    /** The client's type. */
    val clientType: GetClientTypeOptions? = null,

    /** The client's classification. */
    val clientClassification: GetClientClassificationOptions? = null,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsAllowedDocumentTypes.kt`

```kotlin
data class GetClientsAllowedDocumentTypes(
```
_No KDoc at source._

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsClientIdAccountsResponse.kt`

```kotlin
data class GetClientsClientIdAccountsResponse(
```
GetClientsClientIdAccountsResponse

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/api/ClientApi.kt:142</code></summary>

```kotlin
     */
    @GET("clients/{clientId}/accounts")
    suspend fun retrieveAssociatedAccounts(@Path("clientId") clientId: Long): GetClientsClientIdAccountsResponse

    /**
     * @param b      True Enabling the Pagination of the API
     * @param offset Value give from which position Fetch ClientList
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsDocumentType.kt`

```kotlin
data class GetClientsDocumentType(
```
_No KDoc at source._

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsLoanAccounts.kt`

```kotlin
data class GetClientsLoanAccounts(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsClientIdAccountsResponse.kt:25</code></summary>

```kotlin
data class GetClientsClientIdAccountsResponse(

    val loanAccounts: Set<GetClientsLoanAccounts>? = null,

    val savingsAccounts: Set<GetClientsSavingsAccounts>? = null,

    /** The share accounts. */
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsLoanAccountsStatus.kt`

```kotlin
data class GetClientsLoanAccountsStatus(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsLoanAccounts.kt:44</code></summary>

```kotlin
    val productName: String? = null,

    val status: GetClientsLoanAccountsStatus? = null,

    /** The original loan amount. */
    val originalLoan: Double? = null,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsLoanAccountsType.kt`

```kotlin
data class GetClientsLoanAccountsType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsLoanAccounts.kt:38</code></summary>

```kotlin
    val loanCycle: Int? = null,

    val loanType: GetClientsLoanAccountsType? = null,

    val productId: Long? = null,

    val productName: String? = null,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsPageItemsResponse.kt`

```kotlin
data class GetClientsPageItemsResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/api/ClientApi.kt:211</code></summary>

```kotlin
    /** Reads the edit template of a client, with staff from the client's office only. */
    @GET("clients/{clientId}?template=true&staffInSelectedOfficeOnly=true")
    suspend fun getClientTemplate(@Path("clientId") clientId: Int): GetClientsPageItemsResponse

    /** Assigns a loan officer to a client. */
    @POST("clients/{clientId}?command=assignStaff")
    suspend fun assignStaff(
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsResponse.kt`

```kotlin
data class GetClientsResponse(
```
GetClientsResponse

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/api/ClientApi.kt:127</code></summary>

```kotlin
        @Query("sortOrder") sortOrder: String? = null,
        @Query("orphansOnly") orphansOnly: Boolean? = null,
    ): GetClientsResponse

    /**
     * Retrieve client accounts overview
     * An example of how a loan portfolio summary can be provided. This is requested in a specific use case of the
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsSavingsAccounts.kt`

```kotlin
data class GetClientsSavingsAccounts(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsClientIdAccountsResponse.kt:27</code></summary>

```kotlin
    val loanAccounts: Set<GetClientsLoanAccounts>? = null,

    val savingsAccounts: Set<GetClientsSavingsAccounts>? = null,

    /** The share accounts. */
    val shareAccounts: Set<ShareAccountResponse>? = null,
)
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsSavingsAccountsCurrency.kt`

```kotlin
data class GetClientsSavingsAccountsCurrency(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsSavingsAccounts.kt:35</code></summary>

```kotlin
    val accountBalance: Double? = null,

    val currency: GetClientsSavingsAccountsCurrency? = null,

    val depositType: GetClientsSavingsAccountsDepositType? = null,

    val id: Long? = null,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsSavingsAccountsDepositType.kt`

```kotlin
data class GetClientsSavingsAccountsDepositType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsSavingsAccounts.kt:37</code></summary>

```kotlin
    val currency: GetClientsSavingsAccountsCurrency? = null,

    val depositType: GetClientsSavingsAccountsDepositType? = null,

    val id: Long? = null,

    val productId: Long? = null,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsSavingsAccountsStatus.kt`

```kotlin
data class GetClientsSavingsAccountsStatus(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsSavingsAccounts.kt:47</code></summary>

```kotlin
    val shortProductName: String? = null,

    val status: GetClientsSavingsAccountsStatus? = null,

)
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetGenderOptions.kt`

```kotlin
data class GetGenderOptions(
```
A gender offered by the client template.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsPageItemsResponse.kt:78</code></summary>

```kotlin

    /** The client's gender. */
    val gender: GetGenderOptions? = null,

    /** The client's type. */
    val clientType: GetClientTypeOptions? = null,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/mapper/ClientMapper.kt`

```kotlin
object ClientMapper : AbstractMapper<GetClientsPageItemsResponse, Client>()
```
Maps client between its network, stored and domain shapes.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/mapper/GetClientResponseMapper.kt:23</code></summary>

```kotlin
        return Page<Client>().apply {
            totalFilteredRecords = entity.totalFilteredRecords!!
            pageItems = ClientMapper.mapFromEntityList(entity.pageItems!!.toList())
        }
    }

    override fun mapToEntity(domainModel: Page<Client>): GetClientsResponse {
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/mapper/GetClientResponseMapper.kt`

```kotlin
object GetClientResponseMapper : AbstractMapper<GetClientsResponse, Page<Client>>()
```
Maps get client response between its network, stored and domain shapes.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/mapper/GetClientsClientIdAccountMapper.kt`

```kotlin
object GetClientsClientIdAccountMapper :
```
Created by Aditya Gupta on 30/08/23.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/collectionsheet/api/CollectionSheetApi.kt`

```kotlin
interface CollectionSheetApi
```
Created by Tarun on 06-07-2017.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/impl/CollectionSheetCommandRepositoryImpl.kt:48</code></summary>

```kotlin
@RepositoryBinding(binds = CollectionSheetCommandRepository::class)
internal class CollectionSheetCommandRepositoryImpl(
    private val collectionSheetApi: CollectionSheetApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.CollectionsheetIndividual)
    private val individualStore: Store<RequestCollectionSheetPayload, IndividualCollectionSheet>,
    @FromStore(AppStoreIds.CollectionsheetProductive)
```

</details>

- `suspend fun getIndividualCollectionSheet(` — Generates an individual collection sheet for the given office, officer and date.
- `suspend fun saveIndividualCollectionSheet(` — Saves an individual collection sheet.
- `suspend fun fetchCenterDetails(` — Endpoint to fetch Productive CollectionSheet
- `suspend fun fetchProductiveSheet(` — Request Endpoint to fetch Productive CollectionSheet
- `suspend fun submitProductiveSheet(` — Saves a centre's productive collection sheet.
- `suspend fun fetchCollectionSheet(` — Generates a group's collection sheet.
- `suspend fun submitCollectionSheet(` — Saves a group's collection sheet.
- `suspend fun fetchGroupsAssociatedWithCenter(` — Reads a centre's groups and its collection-meeting calendar.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/dashboard/api/DashboardApi.kt`

```kotlin
interface DashboardApi
```
Reads behind the home dashboard that no other feature needed. Every figure is computed from these, never from a report.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/dashboard/DashboardStores.kt:66</code></summary>

```kotlin
@CacheKey(fn = "forKey", key = "dashboardUser:{key}", params = ["key:String"])
fun provideDashboardUserStore(
    dashboardApi: DashboardApi,
    cache: ApiResponseCacheDao,
): Store<Long, DashboardUser> = cachedRead(
    dao = cache,
    keyOf = { userId -> AppCacheKeys.DashboardUser.forKey("$userId") },
```

</details>

- `suspend fun getUser(@Path("userId") userId: Long): AppUserDto` — A user may always read their own record — it carries the staff link that scopes the dashboard.
- `suspend fun getBusinessDate(): BusinessDateDto` — The date every "today" on the dashboard means — never the device clock.
- `suspend fun getLoans(` — Loans by status (100 pending approval, 200 approved, 300 active). No officer filter exists on this endpoint.
- `suspend fun batch(` — Fineract's batch API: many reads in ONE round-trip, each answered independently (`enclosingTransaction=false`).
- `suspend fun getSavingsAccounts(` — Reads one page of savings accounts.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/dashboard/dto/DashboardDtos.kt`

```kotlin
data class AppUserDto(
```
`GET users/{userId}` — the signed-in user's own record; `staff` is the link that scopes their book.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/dashboard/api/DashboardApi.kt:34</code></summary>

```kotlin
    /** A user may always read their own record — it carries the staff link that scopes the dashboard. */
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: Long): AppUserDto

    /** The date every "today" on the dashboard means — never the device clock. */
    @GET("businessdate/BUSINESS_DATE")
    suspend fun getBusinessDate(): BusinessDateDto
```

</details>

```kotlin
data class RoleRefDto(val id: Long = 0, val name: String? = null)
```
Reference to a role embedded in another payload.

```kotlin
data class StaffRefDto(val id: Long = 0, val displayName: String? = null)
```
Reference to a staff embedded in another payload.

```kotlin
data class BusinessDateDto(val type: String? = null, val date: List<Int> = emptyList())
```
`GET businessdate/BUSINESS_DATE` — `date` is Fineract's `[year, month, day]` array.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/dashboard/api/DashboardApi.kt:38</code></summary>

```kotlin
    /** The date every "today" on the dashboard means — never the device clock. */
    @GET("businessdate/BUSINESS_DATE")
    suspend fun getBusinessDate(): BusinessDateDto

    /** Loans by status (100 pending approval, 200 approved, 300 active). No officer filter exists on this endpoint. */
    @GET("loans")
    suspend fun getLoans(
```

</details>

```kotlin
data class CurrencyRefDto(val code: String? = null, val displaySymbol: String? = null)
```
Reference to a currency embedded in another payload.

<details><summary>Used in the template — <code>core/store/src/commonTest/kotlin/kpt/core/store/dashboard/DueTodayTest.kt:26</code></summary>

```kotlin
        id = 7,
        clientId = 42,
        currency = CurrencyRefDto(code = "MXN", displaySymbol = "$"),
        repaymentSchedule = RepaymentScheduleDto(periods.toList()),
    )

    @Test
```

</details>

```kotlin
data class LoanSummaryRefDto(
```
Reference to a loan summary embedded in another payload.

```kotlin
data class LoanTimelineRefDto(val submittedByUsername: String? = null)
```
Reference to a loan timeline embedded in another payload.

```kotlin
data class LoanListItemDto(
```
Wire shape of a loan list item.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/dashboard/DashboardStores.kt:117</code></summary>

```kotlin
            do {
                val page = dashboardApi.getLoans(status = status, offset = offset, limit = PAGE)
                addAll(page.pageItems.map(LoanListItemDto::toDashboardLoan))
                offset += PAGE
            } while (offset < page.totalFilteredRecords && offset < MAX_ROWS && page.pageItems.isNotEmpty())
        }
    },
```

</details>

```kotlin
data class GroupRefDto(val id: Long = 0, val name: String? = null)
```
Reference to a group embedded in another payload.

```kotlin
data class LoanListPageDto(val totalFilteredRecords: Int = 0, val pageItems: List<LoanListItemDto> = emptyList())
```
One page of loan list results.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/dashboard/api/DashboardApi.kt:48</code></summary>

```kotlin
        @Query("orderBy") orderBy: String = "id",
        @Query("sortOrder") sortOrder: String = "DESC",
    ): LoanListPageDto

    /**
     * Fineract's batch API: many reads in ONE round-trip, each answered independently
     * (`enclosingTransaction=false`). Used for the loans' repayment schedules behind Today's collections
```

</details>

```kotlin
data class SavingsSummaryRefDto(val accountBalance: Double = 0.0)
```
Reference to a savings summary embedded in another payload.

```kotlin
data class SavingsListItemDto(
```
Wire shape of a savings list item.

```kotlin
data class SavingsListPageDto(val totalFilteredRecords: Int = 0, val pageItems: List<SavingsListItemDto> = emptyList())
```
One page of savings list results.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/dashboard/api/DashboardApi.kt:67</code></summary>

```kotlin
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): SavingsListPageDto
}
```

</details>

```kotlin
data class LoanScheduleDto(
```
`GET loans/{id}?associations=repaymentSchedule` — only what Today's collections reads.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/dashboard/DashboardStores.kt:169</code></summary>

```kotlin
            error("Client request(POST batches) invalid: ${first.statusCode}. Text: \"${first.body.orEmpty()}\"")
        }
        ok.mapNotNull { batchJson.decodeFromString(LoanScheduleDto.serializer(), it.body!!).dueOn(key.date) }
    },
)

/** The installment(s) of this loan falling due on [date] (`yyyy-MM-dd`), or null when none does. */
```

</details>

```kotlin
data class RepaymentScheduleDto(val periods: List<SchedulePeriodDto> = emptyList())
```
Wire shape of a repayment schedule.

<details><summary>Used in the template — <code>core/store/src/commonTest/kotlin/kpt/core/store/dashboard/DueTodayTest.kt:27</code></summary>

```kotlin
        clientId = 42,
        currency = CurrencyRefDto(code = "MXN", displaySymbol = "$"),
        repaymentSchedule = RepaymentScheduleDto(periods.toList()),
    )

    @Test
    fun theInstallmentDueOnTheDateIsWhatIsOwedAndPaidToday() {
```

</details>

```kotlin
data class SchedulePeriodDto(
```
One installment; `period` is null on the disbursement row. `dueDate` is Fineract's `[y, m, d]`.

<details><summary>Used in the template — <code>core/store/src/commonTest/kotlin/kpt/core/store/dashboard/DueTodayTest.kt:23</code></summary>

```kotlin
class DueTodayTest {

    private fun loan(vararg periods: SchedulePeriodDto) = LoanScheduleDto(
        id = 7,
        clientId = 42,
        currency = CurrencyRefDto(code = "MXN", displaySymbol = "$"),
        repaymentSchedule = RepaymentScheduleDto(periods.toList()),
```

</details>

```kotlin
data class BatchRequestDto(val requestId: Long, val relativeUrl: String, val method: String = "GET")
```
One request inside `POST batches`; `relativeUrl` is relative to the API root (`loans/7?…`).

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/dashboard/api/DashboardApi.kt:58</code></summary>

```kotlin
    @POST("batches")
    suspend fun batch(
        @Body requests: List<BatchRequestDto>,
        @Query("enclosingTransaction") enclosingTransaction: Boolean = false,
    ): List<BatchResponseDto>

    /** Reads one page of savings accounts. */
```

</details>

```kotlin
data class BatchResponseDto(val requestId: Long = 0, val statusCode: Int = 0, val body: String? = null)
```
Its answer: `body` is the JSON the single request would have returned, as a string.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/dashboard/api/DashboardApi.kt:60</code></summary>

```kotlin
        @Body requests: List<BatchRequestDto>,
        @Query("enclosingTransaction") enclosingTransaction: Boolean = false,
    ): List<BatchResponseDto>

    /** Reads one page of savings accounts. */
    @GET("savingsaccounts")
    suspend fun getSavingsAccounts(
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/datatable/api/DataTableApi.kt`

```kotlin
interface DataTableApi
```
Fineract endpoints for data table.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/datatable/impl/DataTableCommandRepositoryImpl.kt:46</code></summary>

```kotlin
@RepositoryBinding(binds = DataTableCommandRepository::class)
internal class DataTableCommandRepositoryImpl(
    private val dataTableApi: DataTableApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.DatatableGetDataOfDataTable)
    private val rowsStore: Store<DataTableDataKey, JsonArray>,
    @FromStore(AppStoreIds.DatatableGetUserPathTracking)
```

</details>

- `suspend fun getDatatables(@Query("apptable") apptable: String? = null): List<GetDataTablesResponse>` — List Data Tables Lists registered data tables and the Apache Fineract Core application table they are registered to. ARGUMENTS apptable - optional The Apache Fineract core application table.
- `suspend fun deleteDatatableEntry(` — Delete Entry in Datatable (One to Many) Deletes the entry (if it exists) for data tables that are one to many with the application table.
- `suspend fun getTableOf(@Query("apptable") table: String?): List<DataTable>` — Reads the data tables registered against an application table.
- `suspend fun getDataOfDataTable(` — Reads an entity's rows in a data table.
- `suspend fun createEntryInDataTable(` — Adds a row for an entity to a data table.
- `suspend fun deleteEntryOfDataTableManyToMany(` — Deletes one row of a multi-row data table.
- `suspend fun addUserPathTracking(` — Records a tracked path for a user.
- `suspend fun getUserPathTracking(@Path("userId") userId: Int): List<UserLocation>` — Reads the paths tracked for a user.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/datatable/dto/GetDataTablesResponse.kt`

```kotlin
data class GetDataTablesResponse(
```
GetDataTablesResponse

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/datatable/DataTableRepositoryTest.kt:74</code></summary>

```kotlin

    private val api = object : DataTableApi {
        override suspend fun getDatatables(apptable: String?): List<GetDataTablesResponse> = error("not used")
        override suspend fun getTableOf(table: String?): List<DataTable> = error("not used")
        override suspend fun getDataOfDataTable(dataTableName: String, entityId: Int): JsonArray =
            error("reads go through the store")
        override suspend fun getUserPathTracking(userId: Int): List<UserLocation> = error("reads go through the store")
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/datatable/mapper/GetDataTablesResponseMapper.kt`

```kotlin
object GetDataTablesResponseMapper : AbstractMapper<GetDataTablesResponse, DataTable>()
```
Created by Aditya Gupta on 31/08/23.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/document/DocumentUploadMultipart.kt`

```kotlin
fun DocumentUpload.toMultipart(): MultiPartFormDataContent = MultiPartFormDataContent(
```
The wire encoding of a `DocumentUpload` for Fineract's `POST|PUT {entity}/{id}/documents`.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/document/api/DocumentApi.kt`

```kotlin
interface DocumentApi
```
Fineract endpoints for document.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/document/impl/DocumentCommandRepositoryImpl.kt:46</code></summary>

```kotlin
@RepositoryBinding(binds = DocumentCommandRepository::class)
internal class DocumentCommandRepositoryImpl(
    private val documentApi: DocumentApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.DocumentItem) private val itemStore: MutableStore<DocumentItemKey, Document>,
    @FromStore(AppStoreIds.DocumentContentLocal) private val contentStore: MutableStore<DocumentContentKey, ByteArray>,
) : DocumentCommandRepository {
```

</details>

- `suspend fun getDocuments(` — Reads the documents attached to an entity.
- `suspend fun createDocument(`
- `suspend fun downloadDocument(` — This Service is for downloading the Document with EntityType and EntityId and Document Id Rest End Point : https://demo.openmf.org/fineract-provider/api/v1/{entityType}/{entityId}/documents/ {documentId}/attachment
- `suspend fun removeDocument(` — This Service is for Deleting the Document with EntityType and EntityId and Document Id.
- `suspend fun updateDocument(` — This Service for Updating the Document with EntityType and EntityId and Document Id.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/group/api/GroupApi.kt`

```kotlin
interface GroupApi
```
Fineract endpoints for group.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/group/impl/GroupCommandRepositoryImpl.kt:46</code></summary>

```kotlin
@RepositoryBinding(binds = GroupCommandRepository::class)
internal class GroupCommandRepositoryImpl(
    private val groupApi: GroupApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.GroupGetGroup) private val groupStore: Store<Int, Group>,
    @FromStore(AppStoreIds.GroupItem) private val itemStore: MutableStore<Int, Group>,
) : GroupCommandRepository {
```

</details>

- `suspend fun retrieveAll24(` — List Groups The default implementation of listing Groups returns 200 entries with support for pagination and sorting. Using the parameter limit with description -1 returns all entries.
- `suspend fun getGroups(` — Reads one page of groups.
- `suspend fun getGroupWithAssociations(@Path("groupId") groupId: Int): GroupWithAssociations` — Reads a group with all its associations.
- `suspend fun getAllGroupsInOffice(` — Reads the groups of one office.
- `suspend fun createGroup(@Body groupPayload: GroupPayload?): SaveResponse` — Creates a group.
- `suspend fun getGroup(@Path("groupId") groupId: Int): Group` — Reads a group.
- `suspend fun getGroupAccounts(@Path("groupId") groupId: Int): GroupAccounts` — Reads a group's accounts.
- `suspend fun activateGroup(` — This is the service to activate the Group REST ENT POINT https://demo.openmf.org/fineract-provider/api/v1/groups/{groupId}?command=activate

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/group/dto/GetGroupsPageItems.kt`

```kotlin
data class GetGroupsPageItems(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/group/dto/GetGroupsResponse.kt:24</code></summary>

```kotlin
data class GetGroupsResponse(

    val pageItems: Set<GetGroupsPageItems>? = null,

    val totalFilteredRecords: Int? = null,

)
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/group/dto/GetGroupsResponse.kt`

```kotlin
data class GetGroupsResponse(
```
GetGroupsResponse

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/group/api/GroupApi.kt:68</code></summary>

```kotlin
        @Query("sortOrder") sortOrder: String? = null,
        @Query("orphansOnly") orphansOnly: Boolean? = null,
    ): GetGroupsResponse

    /** Reads one page of groups. */
    @GET("groups")
    suspend fun getGroups(
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/group/dto/GetGroupsStatus.kt`

```kotlin
data class GetGroupsStatus(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/group/dto/GetGroupsPageItems.kt:41</code></summary>

```kotlin
    val officeName: String? = null,

    val status: GetGroupsStatus? = null,

)
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/group/mapper/GetGroupsResponseMapper.kt`

```kotlin
object GetGroupsResponseMapper : AbstractMapper<GetGroupsResponse, Page<Group>>()
```
Maps get groups response between its network, stored and domain shapes.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/group/mapper/GroupMapper.kt`

```kotlin
object GroupMapper : AbstractMapper<GetGroupsPageItems, Group>()
```
Maps group between its network, stored and domain shapes.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/group/mapper/GetGroupsResponseMapper.kt:23</code></summary>

```kotlin
        return Page<Group>().apply {
            totalFilteredRecords = entity.totalFilteredRecords!!
            pageItems = GroupMapper.mapFromEntityList(entity.pageItems!!.toList())
        }
    }

    override fun mapToEntity(domainModel: Page<Group>): GetGroupsResponse {
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/api/LoanApi.kt`

```kotlin
interface LoanApi : LoanServicingApi
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/impl/LoanCommandRepositoryImpl.kt:61</code></summary>

```kotlin
@RepositoryBinding(binds = LoanCommandRepository::class)
internal class LoanCommandRepositoryImpl(
    private val api: LoanApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Loans) private val loanStore: Store<Int, LoanWithAssociations>,
    @FromStore(AppStoreIds.LoanGetLoanReschedules)
    private val reschedulesStore: Store<Int, List<LoanRescheduleResponse>>,
```

</details>

- `suspend fun getLoanByIdWithAllAssociations(@Path("loanId") loanId: Int): LoanWithAssociationsDto` — Reads a loan with every association except guarantors and the future schedule.
- `suspend fun getLoanRepaymentTemplate(@Path("loanId") loanId: Int): LoanRepaymentTemplate` — Reads the repayment template of a loan: the amount due and the payment types.
- `suspend fun approveLoanApplication(` — Approves a loan application.
- `suspend fun submitPayment(` — Records a loan repayment.
- `suspend fun rejectLoan(` — Rejects a loan application.
- `suspend fun getLoanRepaymentSchedule(@Path("loanId") loanId: Int): LoanWithAssociationsDto` — Reads a loan with its repayment schedule.
- `suspend fun getLoanWithTransactions(@Path("loanId") loanId: Int): LoanWithAssociationsDto` — Reads a loan with its transactions.
- `suspend fun getAllLoans(): List<LoanProducts>` — Reads every loan product.
- `suspend fun createLoansAccount(@Body loansPayload: LoansPayload?): SaveResponse` — Typed as `SaveResponse`, not `HttpResponse`: Fineract answers `{"loanId": N, "resourceId": N, ...}`, and the new id is what the command repository ingests the created loan by.
- `suspend fun calculateLoanSchedule(@Body loansPayload: LoansPayload?): RepaymentSchedule` — Calculate loan repayment schedule without creating the loan. Used to preview the schedule before submitting the loan application.
- `suspend fun getLoansAccountTemplate(` — Reads the individual-loan template for a client.
- `suspend fun getLoanTransactionTemplate(` — For fetching any type of loan template. Example: 1. repayment 2. disburse 3. waiver 4. refundbycash 5.
- `suspend fun createGroupLoansAccount(@Body loansPayload: GroupLoanPayload?): SaveResponse` — `SaveResponse`, not `Loan`: the body is Fineract's command result, whose new id is `resourceId`. Decoded as a `Loan` it had no `id` key, so the created loan's id was always null.
- `suspend fun getGroupLoansAccountTemplate(` — Reads the group-loan template for a group.
  _…more members; read the file._

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/api/LoanServicingApi.kt`

```kotlin
interface LoanServicingApi
```
Guarantors, reschedules, officer assignment, disbursement and charge-off — split from `LoanApi`, which extends it.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/api/LoanApi.kt:44</code></summary>

```kotlin
 */
@ApiBinding("mifos")
interface LoanApi : LoanServicingApi {

    /** Reads a loan with every association except guarantors and the future schedule. */
    @GET("loans/{loanId}?associations=all&exclude=guarantors,futureSchedule")
    suspend fun getLoanByIdWithAllAssociations(@Path("loanId") loanId: Int): LoanWithAssociationsDto
```

</details>

- `suspend fun getChargeOffTemplate(` — Reads the charge-off template of a loan.
- `suspend fun chargeOff(` — Charges off a loan.
- `suspend fun getGuarantorTemplate(@Path("loanId") loanId: Int): GuarantorTemplateDto` — Reads the guarantor template of a loan.
- `suspend fun createGuarantor(` — Adds a guarantor to a loan.
- `suspend fun getGuarantorAccountTemplate(` — Reads the accounts a guarantor may pledge funds from.
- `suspend fun getLoanReschedules(` — Reads the reschedule requests of a loan.
- `suspend fun getLoanRescheduleTemplate(): LoanRescheduleTemplate` — Reads the reschedule template: the reschedule reasons.
- `suspend fun submitLoanReschedule(` — Proposes a loan reschedule.
- `suspend fun approveLoanReschedule(` — Approves a loan reschedule.
- `suspend fun rejectLoanReschedule(` — Rejects a loan reschedule.
- `suspend fun getLoanOfficerTemplate(` — Reads the loan-officer template of a loan.
- `suspend fun assignLoanOfficer(` — Assigns or changes a loan's officer.
- `suspend fun getDisburseTemplate(` — Reads the disbursement template of a loan.
- `suspend fun disburse(` — Disburses a loan.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/GuarantorTemplateDto.kt`

```kotlin
data class GuarantorTemplateDto(
```
Server template for a guarantor: the options its form offers.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/loan/LoanCommandRepositoryTest.kt:180</code></summary>

```kotlin
        override suspend fun getLoanRepaymentSchedule(loanId: Int): LoanWithAssociationsDto = unused()
        override suspend fun getLoanWithTransactions(loanId: Int): LoanWithAssociationsDto = unused()
        override suspend fun getGuarantorTemplate(loanId: Int): GuarantorTemplateDto = unused()
        override suspend fun createGuarantor(
            loanId: Int,
            request: GuarantorRequestDto,
        ): CreateGuarantorResponseDto = unused()
```

</details>

```kotlin
data class GuarantorRelationshipOptionDto(
```
One guarantor relationship choice offered by a template.

```kotlin
data class GuarantorAccountTemplateDto(
```
Server template for a guarantor account: the options its form offers.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/loan/LoanCommandRepositoryTest.kt:188</code></summary>

```kotlin
            loanId: Int,
            clientId: Int,
        ): GuarantorAccountTemplateDto = unused()
        override suspend fun getAllLoans(): List<LoanProducts> = unused()
        override suspend fun calculateLoanSchedule(loansPayload: LoansPayload?): RepaymentSchedule = unused()
        override suspend fun getLoansAccountTemplate(clientId: Int, productId: Int): LoanTemplate = unused()
        override suspend fun getLoanTransactionTemplate(
```

</details>

```kotlin
data class GuarantorTypeDto(
```
Wire shape of a guarantor type.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoanChargeOffTemplateDto.kt`

```kotlin
data class LoanChargeOffTemplateDto(
```
Server template for a loan charge off: the options its form offers.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/loan/LoanCommandRepositoryTest.kt:172</code></summary>

```kotlin
        override suspend fun getLoanByIdWithAllAssociations(loanId: Int): LoanWithAssociationsDto = unused()
        override suspend fun getLoanRepaymentTemplate(loanId: Int): LoanRepaymentTemplate = unused()
        override suspend fun getChargeOffTemplate(loanId: Int): LoanChargeOffTemplateDto = unused()
        override suspend fun chargeOff(
            loanId: Int,
            loanChargeOffRequest: LoanChargeOffRequestDto,
        ): LoanChargeOffResponseDto = unused()
```

</details>

```kotlin
data class ChargeOffReasonOptionDto(
```
One charge off reason choice offered by a template.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoanDisburseRequestDto.kt`

```kotlin
data class LoanDisburseRequestDto(
```
Request body for a loan disburse.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/impl/LoanCommandRepositoryImpl.kt:226</code></summary>

```kotlin

/** The one place the disbursement's domain input becomes a wire payload. */
private fun LoanDisburseInput.toDto() = LoanDisburseRequestDto(
    actualDisbursementDate = actualDisbursementDate,
    transactionAmount = transactionAmount,
    paymentTypeId = paymentTypeId,
    note = note,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoanDisburseResponseDto.kt`

```kotlin
data class LoanDisburseResponseDto(
```
Server response to a loan disburse request.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/loan/LoanCommandRepositoryTest.kt:124</code></summary>

```kotlin
            loanId: Int,
            loanDisburseRequest: LoanDisburseRequestDto,
        ): LoanDisburseResponseDto {
            sent += "disburse"
            serverLoans[loanId] = serverLoans.getValue(loanId).copy(loanOfficerName = "disbursed")
            return LoanDisburseResponseDto(1, 1, loanId.toLong(), 50, 0)
        }
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoanDisburseTemplateDto.kt`

```kotlin
data class LoanDisburseTemplateDto(
```
Server template for a loan disburse: the options its form offers.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/loan/LoanCommandRepositoryTest.kt:227</code></summary>

```kotlin
            request: AssignLoanOfficerRequestDto,
        ): AssignLoanOfficerResponseDto = unused()
        override suspend fun getDisburseTemplate(loanId: Int): LoanDisburseTemplateDto = unused()

        private fun unused(): Nothing = error("not exercised; reads go through the stores")
    }
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoanOfficerOptionsTemplateDto.kt`

```kotlin
data class LoanOfficerOptionsTemplateDto(
```
Server template for assigning a loan officer: the officers on offer.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/loan/LoanCommandRepositoryTest.kt:221</code></summary>

```kotlin
            staffInSelectedOfficeOnly: Boolean,
            template: Boolean,
        ): LoanOfficerOptionsTemplateDto = unused()
        override suspend fun assignLoanOfficer(
            loanId: Int,
            command: String,
            request: AssignLoanOfficerRequestDto,
```

</details>

```kotlin
data class LoanOfficerOptionDto(
```
One loan officer choice offered by a template.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoanWithAssociationsDto.kt`

```kotlin
data class LoanWithAssociationsDto(
```
Wire shape of a loan with associations.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/loan/LoanCommandRepositoryTest.kt:170</code></summary>

```kotlin
        }

        override suspend fun getLoanByIdWithAllAssociations(loanId: Int): LoanWithAssociationsDto = unused()
        override suspend fun getLoanRepaymentTemplate(loanId: Int): LoanRepaymentTemplate = unused()
        override suspend fun getChargeOffTemplate(loanId: Int): LoanChargeOffTemplateDto = unused()
        override suspend fun chargeOff(
            loanId: Int,
```

</details>

```kotlin
data class LoanStatusDto(
```
Wire shape of a loan status.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/mapper/LoanWithAssociationsMapper.kt:80</code></summary>

```kotlin

/** Maps the network loan status to the domain status. */
fun LoanStatusDto.toDomain() = LoanStatus(
    id = this.id,
    code = this.code,
    value = this.value,
    pendingApproval = this.pendingApproval,
```

</details>

```kotlin
data class LoanTypeDto(val id: Int, val code: String? = null, val value: String? = null)
```
Wire shape of a loan type.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/mapper/LoanWithAssociationsMapper.kt:95</code></summary>

```kotlin

/** Maps the network loan type to the domain type. */
fun LoanTypeDto.toDomain() = LoanType(
    id = this.id,
    code = this.code,
    value = this.value,
)
```

</details>

```kotlin
data class LoanCurrencyDto(
```
Wire shape of a loan currency.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/mapper/LoanWithAssociationsMapper.kt:102</code></summary>

```kotlin

/** Maps the network loan currency to the domain currency. */
fun LoanCurrencyDto.toDomain() = SavingAccountCurrency(
    code = this.code,
    name = this.name,
    decimalPlaces = this.decimalPlaces,
    displaySymbol = this.displaySymbol,
```

</details>

```kotlin
data class LoanTimelineDto(
```
Wire shape of a loan timeline.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/mapper/LoanWithAssociationsMapper.kt:111</code></summary>

```kotlin

/** Maps the network loan timeline to the domain timeline. */
fun LoanTimelineDto.toDomain() = LoanTimeline(
    submittedOnDate = this.submittedOnDate,
    approvedOnDate = this.approvedOnDate,
    expectedDisbursementDate = this.expectedDisbursementDate,
    actualDisbursementDate = this.actualDisbursementDate,
```

</details>

```kotlin
data class LoanSummaryDto(
```
Wire shape of a loan summary.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/mapper/LoanWithAssociationsMapper.kt:122</code></summary>

```kotlin

/** Maps the network loan summary to the domain summary. */
fun LoanSummaryDto.toDomain() = LoanAccountSummary(
    loanId = this.loanId,
    currency = this.currency?.toDomain(),
    principalDisbursed = this.principalDisbursed,
    principalPaid = this.principalPaid,
```

</details>

```kotlin
data class LoanRepaymentScheduleDto(
```
Wire shape of a loan repayment schedule.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/mapper/LoanWithAssociationsMapper.kt:162</code></summary>

```kotlin

/** Maps the network repayment schedule to the domain schedule. */
fun LoanRepaymentScheduleDto.toDomain() = RepaymentSchedule(
    loanTermInDays = this.loanTermInDays,
    totalPrincipalDisbursed = this.totalPrincipalDisbursed,
    totalOutstanding = this.totalOutstanding,
    periods = this.periods?.map {
```

</details>

```kotlin
data class LoanPeriodDto(
```
Wire shape of a loan period.

```kotlin
data class LoanTransactionDto(
```
Wire shape of a loan transaction.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/mapper/LoanWithAssociationsMapper.kt:182</code></summary>

```kotlin

/** Maps a network loan transaction to the domain transaction. */
fun LoanTransactionDto.toDomain() = Transaction(
    id = this.id.toInt(),
    officeId = this.officeId?.toInt(),
    officeName = this.officeName,
    date = this.date?.let { ArrayList(it) } ?: ArrayList(),
```

</details>

```kotlin
data class LoanTransactionTypeDto(
```
Wire shape of a loan transaction type.

```kotlin
data class PaymentDetailDataDto(
```
Wire shape of a payment detail data.

```kotlin
data class PaymentTypeDto(
```
Wire shape of a payment type.

```kotlin
data class LoanChargeDto(
```
Wire shape of a loan charge.

```kotlin
data class LoanNoteDto(val id: Long, val note: String? = null, val createdOn: String? = null)
```
Wire shape of a loan note.

```kotlin
data class LoanDelinquentDto(
```
Wire shape of a loan delinquent.

```kotlin
data class NoteDto(
```
Wire shape of a note.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/note/NoteDraftReplayTest.kt:42</code></summary>

```kotlin
        var created: Int = 0
        var updated: Int = 0
        val notes = existing.mapIndexed { i, t -> NoteDto(id = i.toLong(), note = t) }.toMutableList()

        override suspend fun retrieveListNotes(resourceType: String, resourceId: Long) = notes.toList()

        override suspend fun addNewNote(
```

</details>

```kotlin
data class DelinquentDto(
```
Wire shape of a delinquent.

```kotlin
data class LoanGroupDto(val id: Long? = null, val name: String? = null)
```
The owner of a group loan, as `loans/{id}` returns it.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoansPayload.kt`

```kotlin
data class LoansPayload(
```
Created by nellyk on 2/20/2016.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/impl/LoanCommandRepositoryImpl.kt:248</code></summary>

```kotlin
 * them, and the original's `addLoanAccountScreen` left its datatable callback commented out.
 */
private fun LoanApplicationInput.toPayload() = LoansPayload(
    clientId = clientId,
    productId = productId,
    loanType = loanType,
    submittedOnDate = submittedOnDate,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/mapper/LoanDisburseTemplateMapper.kt`

```kotlin
fun LoanDisburseTemplateDto.toDomain(): LoanDisburseTemplate = LoanDisburseTemplate(
```
Wire → domain for the disbursement template.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/mapper/LoanWithAssociationsMapper.kt`

```kotlin
fun LoanWithAssociationsDto.toDomain(): LoanWithAssociations = LoanWithAssociations(
```
Maps the network loan to the domain loan.

```kotlin
fun LoanStatusDto.toDomain() = LoanStatus(
```
Maps the network loan status to the domain status.

```kotlin
fun LoanTypeDto.toDomain() = LoanType(
```
Maps the network loan type to the domain type.

```kotlin
fun LoanCurrencyDto.toDomain() = SavingAccountCurrency(
```
Maps the network loan currency to the domain currency.

```kotlin
fun LoanTimelineDto.toDomain() = LoanTimeline(
```
Maps the network loan timeline to the domain timeline.

```kotlin
fun LoanSummaryDto.toDomain() = LoanAccountSummary(
```
Maps the network loan summary to the domain summary.

```kotlin
fun LoanRepaymentScheduleDto.toDomain() = RepaymentSchedule(
```
Maps the network repayment schedule to the domain schedule.

```kotlin
fun LoanTransactionDto.toDomain() = Transaction(
```
Maps a network loan transaction to the domain transaction.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/mapper/AbstractMapper.kt`

```kotlin
abstract class AbstractMapper<Entity, DomainModel> : EntityMapper<Entity, DomainModel>
```
Base for mappers between a network shape and a domain shape.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/mapper/CenterMapper.kt:19</code></summary>

```kotlin

/** Maps center between its network, stored and domain shapes. */
object CenterMapper : AbstractMapper<GetCentersPageItems, Center>() {

    override fun mapFromEntity(entity: GetCentersPageItems): Center {
        return Center(
            id = entity.id?.toInt(),
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/mapper/EntityMapper.kt`

```kotlin
interface EntityMapper<Entity, DomainModel>
```
Maps between a stored entity and its domain model.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/mapper/AbstractMapper.kt:13</code></summary>

```kotlin

/** Base for mappers between a network shape and a domain shape. */
abstract class AbstractMapper<Entity, DomainModel> : EntityMapper<Entity, DomainModel> {

    override fun mapFromEntityList(entities: List<Entity>): List<DomainModel> {
        return entities.map { mapFromEntity(it) }
    }
```

</details>

- `fun mapFromEntity(entity: Entity): DomainModel` — Maps a stored entity to its domain model.
- `fun mapToEntity(domainModel: DomainModel): Entity` — Maps a domain model to its stored entity.
- `fun mapFromEntityList(entities: List<Entity>): List<DomainModel>` — Maps stored entities to domain models.
- `fun mapToEntityList(domainModels: List<DomainModel>): List<Entity>` — Maps domain models to stored entities.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/note/api/NoteApi.kt`

```kotlin
interface NoteApi
```
Fineract endpoints for note.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/note/impl/NoteCommandRepositoryImpl.kt:55</code></summary>

```kotlin
@RepositoryBinding(binds = NoteCommandRepository::class)
internal class NoteCommandRepositoryImpl(
    private val noteApi: NoteApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Notes) private val notesStore: Store<NoteKey, List<Note>>,
    @FromStore(AppStoreIds.NoteItem) private val itemStore: MutableStore<NoteItemKey, Note>,
) : NoteCommandRepository {
```

</details>

- `suspend fun addNewNote(` — Add a Resource Note Adds a new note to a supported resource.
- `suspend fun deleteNote(` — Delete a Resource Note Deletes a Resource Note Responses: - 200: OK
- `suspend fun retrieveNote(` — Retrieve a single Note Responses: - 200: OK
- `suspend fun retrieveListNotes(` — Retrieve List of notes Note: Notes are returned in descending createOn order.
- `suspend fun updateNote(` — Update a Note Responses: - 200: OK

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/note/dto/NoteDto.kt`

```kotlin
data class NoteDto(
```
Data Transfer Object (DTO) representing a Note returned by the API. This model is used in the network layer to deserialize note-related responses. It may represent a single note or be part of a list of notes.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/note/NoteDraftReplayTest.kt:42</code></summary>

```kotlin
        var created: Int = 0
        var updated: Int = 0
        val notes = existing.mapIndexed { i, t -> NoteDto(id = i.toLong(), note = t) }.toMutableList()

        override suspend fun retrieveListNotes(resourceType: String, resourceId: Long) = notes.toList()

        override suspend fun addNewNote(
```

</details>

```kotlin
data class NoteTypeDto(
```
DTO representing the type/category of a Note. Typically nested inside `NoteDto` and provides metadata about the note type.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/note/mapper/NoteMapper.kt`

```kotlin
fun NoteDto.toModel(): Note = Note(
```
Wire → model. `NoteDto.note` is the body text; `Note` names the same column `noteContent`, which is the one field whose name differs across the boundary. `noteType` is dropped: it is Fineract classification metadata the app never reads.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/notification/api/NotificationApi.kt`

```kotlin
interface NotificationApi
```
Fineract's in-app notifications — what the top bar's bell opens. No original: the app being ported had none.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/notification/impl/NotificationRepositoryImpl.kt:43</code></summary>

```kotlin
@RepositoryBinding(binds = NotificationRepository::class)
internal class NotificationRepositoryImpl(
    private val notificationApi: NotificationApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.Notifications) private val store: Store<Unit, List<AppNotification>>,
    @FromStore(AppStoreIds.NotificationsLocal) private val localStore: MutableStore<Unit, List<AppNotification>>,
) : NotificationRepository {
```

</details>

- `suspend fun getNotifications(` — Unread only by default; pass `isRead` `true` for the read history. Newest first.
- `suspend fun markAllRead()` — Marks every notification of the signed-in user read. Fineract answers with an empty body.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/notification/dto/NotificationDtos.kt`

```kotlin
data class NotificationPageDto(val totalFilteredRecords: Int = 0, val pageItems: List<NotificationDto> = emptyList())
```
`GET notifications` — one page; Fineract returns only unread rows unless `isRead=true`.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/notification/NotificationRepositoryTest.kt:60</code></summary>

```kotlin
            orderBy: String,
            sortOrder: String,
        ) = NotificationPageDto(pageItems = server.filter { it.isRead == isRead })

        override suspend fun markAllRead() {
            markRefused?.let { throw it }
            marked++
```

</details>

```kotlin
data class NotificationDto(
```
A Fineract notification: `objectType` / `objectId` name the record it is about (`loan` 1439).

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/notification/NotificationRepositoryTest.kt:46</code></summary>

```kotlin

    private var server = listOf(
        NotificationDto(id = 1, objectType = "loan", objectId = 10, content = "Loan approved"),
        NotificationDto(id = 2, objectType = "client", objectId = 20, content = "Client activated"),
        NotificationDto(id = 3, objectType = "loan", objectId = 11, content = "Repayment", isRead = true),
    )
    private var markRefused: Throwable? = null
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/office/api/OfficeApi.kt`

```kotlin
interface OfficeApi
```
Fineract endpoints for office.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/office/OfficeKeyedReadStores.kt:29</code></summary>

```kotlin
@CacheKey(fn = "forKey", key = "officeRetrieveOffices:{key}", params = ["key:String"])
fun provideRetrieveOfficesStore(
    officeApi: OfficeApi,
    cache: ApiResponseCacheDao,
): Store<RetrieveOfficesKey, List<GetOfficesResponse>> = cachedRead(
    dao = cache,
    keyOf = { key ->
```

</details>

- `suspend fun retrieveOffices(` — List Offices Example Requests: offices offices?fields&#x3D;id,name,openingDate Responses: - 200: OK
- `suspend fun allOffices(): List<Office>` — Fetches List of All the Offices

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/office/dto/GetOfficesResponse.kt`

```kotlin
data class GetOfficesResponse(
```
GetOfficesResponse

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/office/api/OfficeApi.kt:38</code></summary>

```kotlin
        @Query("orderBy") orderBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null,
    ): List<GetOfficesResponse>

    /**
     * Fetches List of All the Offices
     *
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/office/mapper/GetOfficeResponseMapper.kt`

```kotlin
object GetOfficeResponseMapper : AbstractMapper<GetOfficesResponse, Office>()
```
Maps get office response between its network, stored and domain shapes.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/recurringdeposit/api/RecurringAccountApi.kt`

```kotlin
interface RecurringAccountApi
```
Fineract endpoints for recurring account.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/recurringdeposit/impl/RecurringDepositCommandRepositoryImpl.kt:42</code></summary>

```kotlin
@RepositoryBinding(binds = RecurringDepositCommandRepository::class)
internal class RecurringDepositCommandRepositoryImpl(
    private val recurringAccountApi: RecurringAccountApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.ClientGetClientAccounts)
    private val clientAccountsStore: Store<Int, ClientAccounts>,
) : RecurringDepositCommandRepository {
```

</details>

- `suspend fun createRecurringDepositAccount(` — Opens a recurring-deposit account.
- `suspend fun getRecurringDepositAccountTemplate(` — Reads the recurring-deposit template for a client.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/report/api/RunReportsApi.kt`

```kotlin
interface RunReportsApi
```
Created by Rajan Maurya on 05/02/17.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/report/ReportKeyedReadStores.kt:84</code></summary>

```kotlin
@CacheKey(fn = "forKey", key = "reportGetReportFullParameterList:{key}", params = ["key:String"])
fun provideGetReportFullParameterListStore(
    runReportsApi: RunReportsApi,
    cache: ApiResponseCacheDao,
): Store<GetReportFullParameterListKey, FullParameterListResponse> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.ReportGetReportFullParameterList.forKey("${key.reportName}:${key.parameterType}") },
```

</details>

- `suspend fun getReportCategories(` — Endpoint to fetch Categories of the reports
- `suspend fun getReportFullParameterList(` — Endpoint to fetch FullParameter list after fetching the categories.
- `suspend fun getReportParameterDetails(` — Endpoint to fetch the details for a particular parameter.
- `suspend fun getReportOffice(` — Runs a report's office-parameter query.
- `suspend fun getReportProduct(` — Runs a report's product-parameter query.
- `suspend fun getRunReportWithQuery(` — Endpoint to retrieve final Report based on the parameters.
- `suspend fun getCenterSummaryInfo(` — Runs the group-summary report for a centre.
- `suspend fun getSavingsAccountTransactionReceipt(` — Fetches the savings account transaction receipt from the server. This endpoint generates a receipt for the given transaction and returns it in the requested output format (default: PDF).

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/savings/api/FixedDepositApi.kt`

```kotlin
interface FixedDepositApi
```
Fineract endpoints for fixed deposit.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/impl/SavingsCommandRepositoryImpl.kt:61</code></summary>

```kotlin
@RepositoryBinding(binds = SavingsCommandRepository::class)
internal class SavingsCommandRepositoryImpl(
    private val fixedDepositApi: FixedDepositApi,
    private val savingsAccountApi: SavingsAccountApi,
    private val shareAccountApi: ShareAccountApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.SavingsAccounts)
```

</details>

- `suspend fun fixedDepositProductTemplate(` — Reads the fixed-deposit template for a client.
- `suspend fun createFixedDepositAccount(` — Opens a fixed-deposit account.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/savings/api/SavingsAccountApi.kt`

```kotlin
interface SavingsAccountApi
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/impl/SavingsCommandRepositoryImpl.kt:62</code></summary>

```kotlin
internal class SavingsCommandRepositoryImpl(
    private val fixedDepositApi: FixedDepositApi,
    private val savingsAccountApi: SavingsAccountApi,
    private val shareAccountApi: ShareAccountApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.SavingsAccounts)
    private val accountStore: Store<SavingsAccountKey, SavingsAccountWithAssociations>,
```

</details>

- `suspend fun getSavingsAccountWithAssociations(` — This Service Retrieve a savings application/account.
- `suspend fun getSavingsAccountTransactionTemplate(` — This Method for Retrieving Savings Account Transaction Template from REST API https://demo.openmf.org/fineract-provider/api/v1/{savingsAccountType}/{savingsAccountId} /transactions/template.
- `suspend fun processTransaction(` — This Service making POST Request to the REST API : https://demo.openmf.org/fineract-provider/api/v1/{savingsAccountType}/ {savingsAccountId}/transactions?command={transactionType}
- `suspend fun activateSavings(` — Activates a savings account.
- `suspend fun approveSavingsApplication(` — Approves a savings application.
- `suspend fun allSavingsAccounts(): List<ProductSavings>` — Reads every savings product.
- `suspend fun createSavingsAccount(@Body savingsPayload: SavingsPayload?): HttpResponse` — Opens a savings account.
- `suspend fun savingsAccountTemplate(): SavingProductsTemplate` — Reads the savings product template.
- `suspend fun getClientSavingsAccountTemplateByProduct(` — Reads the savings-account template of a product for a client.
- `suspend fun getGroupSavingsAccountTemplateByProduct(` — Reads the savings-account template of a product for a group.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/savings/api/ShareAccountApi.kt`

```kotlin
interface ShareAccountApi
```
Fineract endpoints for share account.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/impl/SavingsCommandRepositoryImpl.kt:63</code></summary>

```kotlin
    private val fixedDepositApi: FixedDepositApi,
    private val savingsAccountApi: SavingsAccountApi,
    private val shareAccountApi: ShareAccountApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.SavingsAccounts)
    private val accountStore: Store<SavingsAccountKey, SavingsAccountWithAssociations>,
    @FromStore(AppStoreIds.ClientGetClientAccounts)
```

</details>

- `suspend fun shareProductTemplate(` — Reads the share-account template for a client.
- `suspend fun createShareAccount(` — Opens a share account.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/savings/dto/SavingAccountOption.kt`

```kotlin
data class SavingAccountOption(
```
One saving account choice offered by a template.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/defaultaccount/ClientDefaultAccountScreen.kt:139</code></summary>

```kotlin
internal fun ClientDefaultAccountContent(
    state: ClientDefaultAccountState,
    accounts: ScreenState<List<SavingAccountOption>>,
    onAction: (ClientDefaultAccountAction) -> Unit,
    modifier: Modifier = Modifier,
    /** The client's CURRENT default, pre-selected until the officer picks something else. */
    defaultAccountId: Long? = null,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/savings/dto/ShareAccountResponse.kt`

```kotlin
data class ShareAccountResponse(
```
Server response to a share account request.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/client/dto/GetClientsClientIdAccountsResponse.kt:30</code></summary>

```kotlin

    /** The share accounts. */
    val shareAccounts: Set<ShareAccountResponse>? = null,
)
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/savings/dto/ShareAccountsStatusResponse.kt`

```kotlin
data class ShareAccountsStatusResponse(
```
Server response to a share accounts status request.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/savings/dto/ShareAccountResponse.kt:38</code></summary>

```kotlin
    val productName: String? = null,
    /** The record's Fineract status. */
    val status: ShareAccountsStatusResponse? = null,
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: Timeline? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/search/api/SearchApi.kt`

```kotlin
interface SearchApi
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/search/SearchKeyedReadStores.kt:30</code></summary>

```kotlin
@CacheKey(fn = "forKey", key = "searchSearchResources:{key}", params = ["key:String"])
fun provideSearchResourcesStore(
    searchApi: SearchApi,
    cache: ApiResponseCacheDao,
): Store<SearchResourcesKey, List<SearchedEntity>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.SearchSearchResources.forKey("${key.query}:${key.resource}:${key.exactMatch}") },
```

</details>

- `suspend fun searchResources(` — Searches clients, groups, centres and accounts for a query.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/server/AppServerConfig.kt`

```kotlin
object AppServerConfig
```
The server the app ships pointed at, as the server-config SCREEN describes one — DERIVED from the generated `mifos` access point, never written a second time.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/serverconfig/impl/ServerConfigRepositoryImpl.kt:38</code></summary>

```kotlin
) : ServerConfigRepository {

    override val defaultConfig: ServerConfig = AppServerConfig.DEFAULT

    // Store-backed like every other read: cached instances render at once, the remote list refreshes
    // behind them, and with nothing cached the screen shows its loading state until the fetch lands.
    override fun instancesStream(scope: CoroutineScope): ScreenDataStream<List<ServerInstance>> =
```

</details>

- `const val ACCESS_POINT = "mifos"` — The access point the field officer signs in to.
- `const val TENANT_HEADER = "Fineract-Platform-TenantId"` — The header that carries Fineract's tenant on `ACCESS_POINT`.
- `val DEFAULT: ServerConfig =` — The shipped server as the screen shows it — the declared main access point's config.
- `const val APP_ID = "field-officer"` — This app's id in the shared `app_config` instance list's `supportedApps` (see `InstanceConfigLoader`).

```kotlin
fun NetworkConfig.toServerConfig(): ServerConfig = requireNotNull(
```
A runtime network config as the screen's five fields: its URL split into protocol / host / port / API path, and the tenant from its `AppServerConfig.TENANT_HEADER` value.

```kotlin
fun ServerConfig.networkHeaders(): Map<String, String> = mapOf(AppServerConfig.TENANT_HEADER to tenant.trim())
```
The header values a `ServerConfig` sets on its network config — the tenant.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/server/InstancesConfig.kt`

```kotlin
fun parseInstances(config: String, app: String = AppServerConfig.APP_ID): List<ServerInstance> =
```
`config` parsed and filtered to the instances that list `app` in `supportedApps`.

```kotlin
fun instancesConfigJson(rows: List<RemoteServerInstanceDto>): String = json.encodeToString(
```
`app_server_config` rows as the `{"instances": `...`}` document `parseInstances` reads — so the Supabase list, its device cache and the bundled list all go through ONE parser.

```kotlin
fun interface InstanceConfigLoader
```
Where the instance list comes from when nothing better is available.

```kotlin
object BundledInstanceConfigLoader : InstanceConfigLoader
```
The list that ships in the app — also the fallback a remote loader must use.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/serverconfig/ServerInstancesTest.kt:109</code></summary>

```kotlin
    @Test
    fun noKeyOrNoRowsForThisAppUsesTheListShippedInTheApp() = runTest {
        val bundled = BundledInstanceConfigLoader.load()
        assertEquals(
            bundled,
            provideServerInstancesStore(
                FakeServerInstanceApi(remoteRows, isConfigured = false),
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/staff/api/StaffApi.kt`

```kotlin
interface StaffApi
```
Fineract endpoints for staff.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/staff/StaffKeyedReadStores2.kt:29</code></summary>

```kotlin
@CacheKey(fn = "forKey", key = "staffAllStaff:{key}", params = ["key:String"])
fun provideAllStaffStore(
    staffApi: StaffApi,
    cache: ApiResponseCacheDao,
): Store<Unit, List<Staff>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.StaffAllStaff.forKey("all") },
```

</details>

- `suspend fun retrieveAll16(` — Retrieve Staff Returns the list of staff members. Example Requests: staff Retrieve a Staff by status Returns the details of a Staff based on status. By default it Returns all the ACTIVE Staff.
- `suspend fun getStaffForOffice(@Query("officeId") officeId: Int): List<Staff>` — Reads the staff of an office, active or not.
- `suspend fun allStaff(): List<Staff>` — Reads every staff member.
- `suspend fun fieldStaffForOffice(): List<Staff>` — Reads every loan officer.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/staff/mapper/StaffMapper.kt`

```kotlin
object StaffMapper : AbstractMapper<RetrieveOneResponse, Staff>()
```
Maps staff between its network, stored and domain shapes.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/survey/api/SurveyApi.kt`

```kotlin
interface SurveyApi
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/survey/impl/SurveyCommandRepositoryImpl.kt:23</code></summary>

```kotlin
@RepositoryBinding(binds = SurveyCommandRepository::class)
internal class SurveyCommandRepositoryImpl(
    private val surveyApi: SurveyApi,
    private val gateway: MutationGateway,
) : SurveyCommandRepository {

    /**
```

</details>

- `suspend fun allSurveys(): List<Survey>` — Reads every survey.
- `suspend fun getSurvey(@Path("surveyId") surveyId: Int): Survey` — Reads a survey.
- `suspend fun submitScore(` — Submits a scorecard for a survey.

### `core/network/src/commonMain/kotlin/kpt/core/network/mifos/survey/dto/ScorecardPayload.kt`

```kotlin
data class ScorecardPayload
```
Created by Nasim Banu on 28,January,2016.

---

_129 type(s), 200 function(s)/property(ies); 307 carry KDoc at source; 2 authored example(s); 97 live call site(s)._
<!-- api-docs:end -->
