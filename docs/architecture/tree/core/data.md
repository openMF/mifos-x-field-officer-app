# `core/data`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATA.md`
> **Measured:** 165 Kotlin files, 33 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@RepositoryBinding` | `data-ksp` | di/GeneratedRepositoryBindings |
| `@DataProvider` | `data-ksp` | di/GeneratedRepositoryBindings, config/AppOutboxQualifiers |
| `@FromStore` | `data-ksp` |  |
| `@FromQualifier` | `data-ksp` |  |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

**Defines annotations:** `@ExperimentalDataTestingApi`

## Principal types

`AccountStores`, `AuthCommandRepository`, `AuthCommandRepositoryImpl`, `CenterCommandRepository`, `CenterCommandRepositoryImpl`, `CenterRepository`, `CenterRepositoryImpl`, `ChargeCommandRepository`, `ChargeCommandRepositoryImpl`, `ChargeRepository`, `ChargeRepositoryImpl`, `CheckerInboxCommandRepository`, `CheckerInboxCommandRepositoryImpl`, `CheckerInboxRepository`  …and 86 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/data sha=175b802e07f22abde4ef98a12e2a2bc73a14e834 -->
## API reference

_Generated from `core/data` at tree `175b802e07f2` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/data/src/commonMain/kotlin/kpt/core/data/auth/AuthCommandRepository.kt`

```kotlin
interface AuthCommandRepository
```
Every auth write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileViewModel.kt:55</code></summary>

```kotlin
    staff: StaffRepository,
    network: NetworkMonitor,
    private val auth: AuthCommandRepository,
) : BaseViewModel<ProfileUiState, ProfileEvent, ProfileAction>(ProfileUiState()) {

    private val session = project.fineractUser.value
    private val userStream = dashboard.userStream(session.userId, viewModelScope)
```

</details>

- `suspend fun authenticate(` — Sign in. On a granted sign-in the session is already persisted when this returns `Applied`.
- `suspend fun resumeStoredSession(): MutationResult<Unit>` — Mark the stored session unlocked again (after a biometric check).
- `suspend fun signOut(clearLocalData: Boolean): MutationResult<Unit>` — End the session. With `clearLocalData` false only the session goes — cached data and unsynced work stay for the next sign-in.
- `suspend fun changePassword(newPassword: String, repeatPassword: String): MutationResult<Unit>` — Change the signed-in user's own password, then sign in again with it so the stored session key (derived from the old password) is replaced and the next request is not a 401.

```kotlin
class PasswordChangedSignInFailed(cause: Throwable? = null) : Exception(
```
The password changed, but signing in with it failed; the session was ended (data kept).

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileViewModel.kt:140</code></summary>

```kotlin
                    sendEvent(ProfileEvent.PasswordChanged)
                }
                is MutationResult.Failed -> if (result.cause is PasswordChangedSignInFailed) {
                    // Signed out (data kept) — the root graph is already moving to sign-in.
                    updateState { copy(password = null) }
                } else {
                    updatePassword {
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/center/CenterCommandRepository.kt`

```kotlin
interface CenterCommandRepository
```
Every center write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateViewModel.kt:51</code></summary>

```kotlin
internal class ActivateViewModel(
    private val clients: ClientCommandRepository,
    private val centers: CenterCommandRepository,
    private val groups: GroupCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ActivateState, ActivateEvent, ActivateAction>(
    ActivateState(
```

</details>

- `suspend fun activate2(`
- `suspend fun saveCollectionSheet(`
- `suspend fun saveCollectionSheetAsync(`
- `suspend fun createCenter(centerPayload: CenterPayload?): MutationResult<SaveResponse>`
- `suspend fun activateCenter(centerId: Int, activatePayload: ActivatePayload?): MutationResult<GenericResponse>`

### `core/data/src/commonMain/kotlin/kpt/core/data/center/CenterRepository.kt`

```kotlin
interface CenterRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/detail/CenterDetailViewModel.kt:58</code></summary>

```kotlin
 */
internal class CenterDetailViewModel(
    centerRepository: CenterRepository,
    savedStateHandle: SavedStateHandle,
    private val recents: RecentRecordsRepository,
) : BaseViewModel<Unit, CenterDetailEvent, CenterDetailAction>(Unit) {
```

</details>

- `fun centersStream(scope: CoroutineScope): ScreenDataStream<List<Center>>` — Centers the officer services. Replaces the legacy CenterList + CenterDetails pair.
- `fun centerWithMeetingStream(centerId: Int, scope: CoroutineScope): ScreenDataStream<CenterWithAssociations>` — One center with its group members and collection-meeting calendar — the detail's meeting rows (next meeting, frequency) and activation date.
- `fun centerSummaryStream(centerId: Int, scope: CoroutineScope): ScreenDataStream<List<CenterInfo>>` — The center's summary counts (`runreports/GroupSummaryCounts`) — active clients, loans and borrowers, overdue loans. Its own stream: a role without report access (Fineract 403) fails this section only, never the center's details.
- `suspend fun collectionSheet(centerId: Long, payload: Payload): CollectionSheet?` — The center's collection sheet for the meeting `payload` selects. Fineract's `?command=generateCollectionSheet` is POST-shaped but a read; it was misfiled as a command. Network first; offline, the last sheet generated for that meeting.

### `core/data/src/commonMain/kotlin/kpt/core/data/charge/ChargeCommandRepository.kt`

```kotlin
interface ChargeCommandRepository
```
Every charge write, for any `ChargeResource`, as an explicit outcome.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormViewModel.kt:63</code></summary>

```kotlin
internal class ChargeFormViewModel(
    private val repository: ChargeRepository,
    private val commands: ChargeCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ChargeFormState, ChargeFormEvent, ChargeFormAction>(
    ChargeFormState(
        resource = savedStateHandle.chargeResource(),
```

</details>

- `suspend fun createCharge(` — Levy a charge on a resource. `Unit`, not `HttpResponse`: the endpoint's Ktorfit signature returns a raw response, and a FEATURE cannot name a Ktor type.
- `suspend fun deleteCharge(resource: ChargeResource, resourceId: Int, chargeId: Int): MutationResult<Unit>`
- `suspend fun updateCharge(` — Edit a levied charge. Only where `ChargeResource.canUpdate` — Fineract has no PUT for a client charge, so for `ChargeResource.CLIENT` the server refuses it and this reports `Failed`.

### `core/data/src/commonMain/kotlin/kpt/core/data/charge/ChargeRepository.kt`

```kotlin
interface ChargeRepository
```
Every charge READ, for any `ChargeResource` — the one surface `feature/charge` reads through.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/detail/ChargeDetailViewModel.kt:40</code></summary>

```kotlin
 */
internal class ChargeDetailViewModel(
    repository: ChargeRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit, ChargeDetailEvent, ChargeDetailAction>(Unit) {

    private val resource: ChargeResource = savedStateHandle.chargeResource()
```

</details>

- `fun chargesStream(` — Charges levied on one resource. An owner with no charges is an empty LIST (Content), so a caller maps it to Empty with `emptyIfContent` — and a deleted last charge leaves the screen.
- `suspend fun chargeTemplate(resource: ChargeResource, resourceId: Int): ChargeTemplate?` — Which charges the tenant allows on this resource, and their default amounts. A suspend one-shot: the add-charge form asks once when it opens. `null` on failure.
- `suspend fun charge(resource: ChargeResource, resourceId: Int, chargeId: Int): Charges?` — One charge as the server has it now — fetched through the store, so the table is updated first — falling back to the cached row offline. The edit form prefills from it; `null` when neither is available.

### `core/data/src/commonMain/kotlin/kpt/core/data/checkerinbox/CheckerInboxCommandRepository.kt`

```kotlin
interface CheckerInboxCommandRepository
```
Every checkerinbox write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/checkerinbox/src/commonMain/kotlin/kpt/feature/checkerinbox/ui/CheckerInboxViewModel.kt:51</code></summary>

```kotlin
internal class CheckerInboxViewModel(
    repository: CheckerInboxRepository,
    private val commands: CheckerInboxCommandRepository,
) : BaseViewModel<CheckerInboxState, CheckerInboxEvent, CheckerInboxAction>(CheckerInboxState()) {

    private val taskStream = repository.checkerTasksStream(viewModelScope)
    private val rescheduleStream = repository.rescheduleRequestsStream(viewModelScope)
```

</details>

- `suspend fun approveCheckerEntry(auditId: Int): MutationResult<GenericResponse>`
- `suspend fun rejectCheckerEntry(auditId: Int): MutationResult<GenericResponse>`
- `suspend fun deleteCheckerEntry(auditId: Int): MutationResult<GenericResponse>`

### `core/data/src/commonMain/kotlin/kpt/core/data/checkerinbox/CheckerInboxRepository.kt`

```kotlin
interface CheckerInboxRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/checkerinbox/src/commonMain/kotlin/kpt/feature/checkerinbox/ui/CheckerInboxViewModel.kt:50</code></summary>

```kotlin
 */
internal class CheckerInboxViewModel(
    repository: CheckerInboxRepository,
    private val commands: CheckerInboxCommandRepository,
) : BaseViewModel<CheckerInboxState, CheckerInboxEvent, CheckerInboxAction>(CheckerInboxState()) {

    private val taskStream = repository.checkerTasksStream(viewModelScope)
```

</details>

- `fun checkerTasksStream(scope: CoroutineScope): ScreenDataStream<List<CheckerTask>>` — Maker-checker tasks awaiting this user's approval.
- `fun rescheduleRequestsStream(scope: CoroutineScope): ScreenDataStream<List<RescheduleLoansTask>>` — Loan reschedule requests awaiting approval. Deciding one is the loan's own reschedule screen.
- `suspend fun searchTemplate(): CheckerInboxSearchTemplate?` — The action and entity names the inbox can be filtered by; null when they could not be read.

### `core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt`

```kotlin
interface ClientCommandRepository
```
Every client write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateViewModel.kt:50</code></summary>

```kotlin
 */
internal class ActivateViewModel(
    private val clients: ClientCommandRepository,
    private val centers: CenterCommandRepository,
    private val groups: GroupCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ActivateState, ActivateEvent, ActivateAction>(
```

</details>

- `suspend fun activate1(` — Every client write, as an explicit outcome.
- `suspend fun uploadClientImage(clientId: Int, image: DocumentUpload): MutationResult<Unit>` — The photo as bytes + name + MIME type; the multipart encoding is core/data's job, not the caller's.
- `suspend fun deleteClientImage(clientId: Int): MutationResult<Unit>`
- `suspend fun createClient(clientPayload: ClientPayload?): MutationResult<Client>`
- `suspend fun updateClient(clientId: Int, clientPayload: ClientPayload?): MutationResult<Client>`
- `suspend fun addClientPinpointLocation(`
- `suspend fun deleteClientPinpointLocation(`
- `suspend fun updateClientPinpointLocation(`
- `suspend fun activateClient(clientId: Int, clientActivate: ActivatePayload?): MutationResult<GenericResponse>`
- `suspend fun createClientAddress(`
- `suspend fun assignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit>`
- `suspend fun unassignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit>`
- `suspend fun proposeTransfer(clientId: Int, payload: ProposeTransferRequest): MutationResult<Unit>`
- `suspend fun updateSavingsAccount(clientId: Int, payload: UpdateSavingsAccountRequest): MutationResult<Unit>`
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/client/ClientRepository.kt`

```kotlin
interface ClientRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addaddress/ClientAddAddressViewModel.kt:84</code></summary>

```kotlin
 */
internal class ClientAddAddressViewModel(
    repository: ClientRepository,
    private val commands: ClientCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<ClientAddAddressState, ClientAddAddressEvent, ClientAddAddressAction>(
```

</details>

- `fun clientsStream(scope: CoroutineScope): ScreenDataStream<List<Client>>` — The officer's client roster.
- `fun clientStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<Client>` — One client, by id — backs the detail screen.
- `fun clientAccountsStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<ClientAccounts>` — A client's loan and savings accounts.
- `fun clientIdentifiersStream(clientId: Long, scope: CoroutineScope): ScreenDataStream<List<Identifier>>` — The client's identity documents (passport, national id, …). The STORE for this read already existed (`clientGetClientListIdentifiers`) and nothing exposed it — the identifiers screen could not be ported without this line.
- `fun clientIdentifierTemplateStream(clientId: Long, scope: CoroutineScope): ScreenDataStream<IdentifierTemplate>` — The document TYPES this client may be identified by. The store behind it (`@StoreProvider(id = "clientGetClientIdentifierTemplate")`) already existed and was unexposed — the add-identifier form is its first reader.
- `suspend fun clientImage(clientId: Int): String?` — The client's profile photo as the `data:image/...;base64,...` URL Fineract returns, or `null` when there isn't one.
- `fun clientAddressesStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<List<ClientAddressEntity>>` — The client's postal addresses.
- `fun clientsTemplateStream(scope: CoroutineScope): ScreenDataStream<ClientsTemplate>` — The TENANT's client template — gender / client-type / classification / legal-form code values, and the tenant's configured data tables. NOT the same read as `clientTemplateStream`, which is per-client.
- `fun addressConfigurationStream(scope: CoroutineScope): ScreenDataStream<AddressConfiguration>` — Whether this tenant collects client ADDRESSES at all, and how. The store behind this (`@StoreProvider(id = "addressConfiguration")`) already existed and was unexposed — the create-client form is its first reader.
- `fun addressTemplateStream(scope: CoroutineScope): ScreenDataStream<AddressTemplate>`
- `fun pinpointLocationsStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<List<ClientAddressResponse>>` — Where the officer has recorded this client — the pinpoint map/list.
- `fun clientCollateralStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<List<CollateralItemResult>>` — Collateral the client has already pledged.
- `fun collateralItemsStream(scope: CoroutineScope): ScreenDataStream<List<CollateralItem>>` — The collateral TYPES the tenant offers — a catalog, not client data, which is why it is keyed `Unit` and shared across clients. Backs the type picker on the add-collateral form.
- `fun clientCloseTemplateStream(scope: CoroutineScope): ScreenDataStream<ClientCloseTemplateResponse>` — Closure reasons for the close-client form. Also a tenant-level catalog.
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/CollectionSheetCommandRepository.kt`

```kotlin
interface CollectionSheetCommandRepository
```
Every collectionsheet write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/details/CollectionSheetDetailsViewModel.kt:64</code></summary>

```kotlin
    savedStateHandle: SavedStateHandle,
    repository: CollectionSheetRepository,
    private val commands: CollectionSheetCommandRepository,
) : BaseViewModel<CollectionSheetDetailsState, CollectionSheetDetailsEvent, CollectionSheetDetailsAction>(
    CollectionSheetDetailsState(),
) {
```

</details>

- `suspend fun saveIndividualCollectionSheet(` — `request` is the generate request the sheet came from: the save body carries no office or staff, so without it the saved sheet could not be found again to record the outcome against.
- `suspend fun submitProductiveSheet(`
- `suspend fun submitCollectionSheet(groupId: Int, payload: CollectionSheetPayload?): MutationResult<GenericResponse>`

### `core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/CollectionSheetRepository.kt`

```kotlin
interface CollectionSheetRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/details/CollectionSheetDetailsViewModel.kt:63</code></summary>

```kotlin
internal class CollectionSheetDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: CollectionSheetRepository,
    private val commands: CollectionSheetCommandRepository,
) : BaseViewModel<CollectionSheetDetailsState, CollectionSheetDetailsEvent, CollectionSheetDetailsAction>(
    CollectionSheetDetailsState(),
) {
```

</details>

- `fun centerDetailsStream(` — The centre sheet for one (office, staff, meeting date). A sheet is only meaningful for that triple, so all three are part of the address — a date change must re-fetch rather than serve the previous meeting's sheet.
- `fun individualCollectionSheetStream(` — The individual collection sheet for one (office, staff, date) request — network first, and offline the last sheet generated for it.
- `suspend fun individualCollectionSheet(request: RequestCollectionSheetPayload): IndividualCollectionSheet?` — The individual collection sheet for one (office, staff, date) request — network first, and offline the last sheet generated for it.
- `suspend fun productiveSheet(centerId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?`
- `suspend fun groupCollectionSheet(groupId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?`

### `core/data/src/commonMain/kotlin/kpt/core/data/dashboard/DashboardRepository.kt`

```kotlin
interface DashboardRepository
```
Home dashboard reads — each its own stream, so one failing card never blanks the rest.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:779</code></summary>

```kotlin
        }
        data.awaitingApproval?.let { n ->
            add { QueueTile(Res.string.feature_home_dashboard_queue_approval, n, Icons.Outlined.PendingActions, c.tertiaryContainer, c.tertiary, TestTags.Home.QUEUE_APPROVAL) { onNavigate(HomeNavigation.LoanQueue(DashboardRepository.STATUS_SUBMITTED)) } }
        }
        data.awaitingDisbursal?.let { n ->
            add { QueueTile(Res.string.feature_home_dashboard_queue_disbursal, n, Icons.Outlined.Verified, c.secondaryContainer, c.secondary, TestTags.Home.QUEUE_DISBURSAL) { onNavigate(HomeNavigation.LoanQueue(DashboardRepository.STATUS_APPROVED)) } }
        }
```

</details>

- `fun userStream(userId: Long, scope: CoroutineScope): ScreenDataStream<DashboardUser>` — The signed-in user's own record: first name and the staff link that scopes the book.
- `fun businessDateStream(scope: CoroutineScope): ScreenDataStream<String>` — Fineract's business date as `yyyy-MM-dd` — the "today" every card means.
- `fun loansStream(status: Int, scope: CoroutineScope): ScreenDataStream<List<DashboardLoan>>` — Loans in one status (100 submitted, 200 approved, 300 active), unfiltered by officer.
- `fun clientCountStream(officeId: Long, status: String, scope: CoroutineScope): ScreenDataStream<Int>` — How many clients of `officeId` are in `status` (`pending`, `active`).
- `fun savingsStream(scope: CoroutineScope): ScreenDataStream<List<DashboardSavings>>`
- `fun dueTodayStream(date: String, loanIds: List<Long>, scope: CoroutineScope): ScreenDataStream<List<DueTodayLoan>>` — Installments of `loanIds` due on `date` (`yyyy-MM-dd`) — Today's collections without the collection sheet.
- `const val STATUS_SUBMITTED = 100`
- `const val STATUS_APPROVED = 200`
- `const val STATUS_ACTIVE = 300`

### `core/data/src/commonMain/kotlin/kpt/core/data/datatable/DataTableCommandRepository.kt`

```kotlin
interface DataTableCommandRepository
```
Every datatable write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/datatable/src/commonMain/kotlin/kpt/feature/datatable/rows/DataTableRowsViewModel.kt:57</code></summary>

```kotlin
internal class DataTableRowsViewModel(
    repository: DataTableRepository,
    private val commands: DataTableCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<DataTableRowsState, DataTableRowsEvent, DataTableRowsAction>(
    DataTableRowsState(
        tableName = savedStateHandle.get<String>(TABLE_NAME_KEY).orEmpty(),
```

</details>

- `suspend fun deleteDatatableEntry(`
- `suspend fun createEntryInDataTable(`
- `suspend fun deleteEntryOfDataTableManyToMany(`
- `suspend fun addUserPathTracking(userId: Int, userLocation: UserLocation?): MutationResult<GenericResponse>`

### `core/data/src/commonMain/kotlin/kpt/core/data/datatable/DataTableRepository.kt`

```kotlin
interface DataTableRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/datatable/src/commonMain/kotlin/kpt/feature/datatable/list/DataTableListViewModel.kt:44</code></summary>

```kotlin
 */
internal class DataTableListViewModel(
    repository: DataTableRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit, DataTableListEvent, DataTableListAction>(Unit) {

    /**
```

</details>

- `fun dataTablesStream(appTable: String, scope: CoroutineScope): ScreenDataStream<List<DataTable>>` — Registered datatables for one application table (m_client, m_loan, ...).
- `fun dataTableDataStream(` — One datatable's ROWS for one entity. A raw `JsonArray`: a datatable's columns are configured per DEPLOYMENT, so there is no DTO to deserialize into and the screen renders whatever keys the tenant defined.

### `core/data/src/commonMain/kotlin/kpt/core/data/di/ForkRepositoryModule.kt`

```kotlin
val ForkRepositoryModule = module
```
THE FORK'S repository DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
single<MyRepository> { MyRepositoryImpl(get(), get()) }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/di/OutboxQualifiers.kt`

```kotlin
object OutboxQualifiers
```
Named qualifiers for the app's `SubmitOutbox<*>` bindings. Why qualifiers? Koin indexes `single<T>` definitions by `T::class` (the raw type), not by the full `KType`.

<details><summary>Example</summary>

```kotlin
val MyThing = named("outbox.myThing")
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt`

```kotlin
val DataModule = module
```
DataModule — the INFRA-ONLY (framework) data aggregator, `owner: template` (E1 / C1).

```kotlin
expect val platformModule: Module
```
Per-target repository bindings supplied by each `actual` — the pieces that cannot be expressed in common code.

### `core/data/src/commonMain/kotlin/kpt/core/data/document/DocumentCommandRepository.kt`

```kotlin
interface DocumentCommandRepository
```
Every document write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/adddocument/ClientAddDocumentViewModel.kt:67</code></summary>

```kotlin
 */
internal class ClientAddDocumentViewModel(
    private val commands: DocumentCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientAddDocumentState, ClientAddDocumentEvent, ClientAddDocumentAction>(
    ClientAddDocumentState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {
```

</details>

- `suspend fun createDocument(entityType: String, entityId: Int, upload: DocumentUpload): MutationResult<SaveResponse>` — `SaveResponse.resourceId` is the new document's id.
- `suspend fun removeDocument(entityType: String, entityId: Int, documentId: Int): MutationResult<GenericResponse>`
- `suspend fun updateDocument(`

### `core/data/src/commonMain/kotlin/kpt/core/data/document/DocumentRepository.kt`

```kotlin
interface DocumentRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/documentpreview/ClientDocumentPreviewViewModel.kt:48</code></summary>

```kotlin
 */
internal class ClientDocumentPreviewViewModel(
    private val repository: DocumentRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientDocumentPreviewState, ClientDocumentPreviewEvent, ClientDocumentPreviewAction>(
    ClientDocumentPreviewState(
        title = savedStateHandle.get<String>(TITLE_KEY).orEmpty(),
```

</details>

- `fun documentsStream(` — Documents attached to any Fineract entity — clients, loans, groups all share this surface.
- `suspend fun downloadDocument(entityType: String, entityId: Int, documentId: Int): ByteArray?` — One document's bytes, or `null` when it cannot be fetched.
- `suspend fun fetchDocuments(entityType: String, entityId: Int): List<Document>?` — The entity's documents as the SERVER has them now — fetched through the store, so the database is updated first — falling back to the cached list offline; `null` only when neither is available.

### `core/data/src/commonMain/kotlin/kpt/core/data/draft/CreateDraftDataProviders.kt`

```kotlin
fun provideCreateDraftOutbox(dao: DraftDao): SubmitOutbox<FormDraft> =
```
_No KDoc at source._

```kotlin
fun provideCreateDraftRepository(dao: DraftDao): FormDraftRepository =
```
The create forms' drafts — what each create form's `FormDraftSlot` and Sync & Drafts read.

```kotlin
fun provideCreateDraftSubmitter(
```
_No KDoc at source._

```kotlin
fun provideCreateDraftSyncer(
```
Eager, and gated on auth, for the note syncer's reasons (see `provideNoteSubmitSyncer`): a create kept offline must send when the officer is next online and signed in, with no screen open — and never before the auth header exists, or the first launch burns every draft to FAILED.

### `core/data/src/commonMain/kotlin/kpt/core/data/draft/CreateDraftSubmitter.kt`

```kotlin
class CreateDraftSubmitter(
```
Sends a kept create the way its form would have: decodes `FormDraft.payload` with the form's own payload type and calls the same command repository the form calls.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/draft/CreateDraftDataProviders.kt:55</code></summary>

```kotlin

@DataProvider
fun provideCreateDraftSubmitter(
    clients: ClientCommandRepository,
    groups: GroupCommandRepository,
    centers: CenterCommandRepository,
    loans: LoanCommandRepository,
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/group/GroupCommandRepository.kt`

```kotlin
interface GroupCommandRepository
```
Every group write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateViewModel.kt:52</code></summary>

```kotlin
    private val clients: ClientCommandRepository,
    private val centers: CenterCommandRepository,
    private val groups: GroupCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ActivateState, ActivateEvent, ActivateAction>(
    ActivateState(
        id = savedStateHandle.get<Int>(ID_KEY) ?: -1,
```

</details>

- `suspend fun createGroup(groupPayload: GroupPayload?): MutationResult<SaveResponse>`
- `suspend fun activateGroup(groupId: Int, activatePayload: ActivatePayload): MutationResult<Unit>`

### `core/data/src/commonMain/kotlin/kpt/core/data/group/GroupRepository.kt`

```kotlin
interface GroupRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/testing/FakeCenterRepositories.kt:69</code></summary>

```kotlin
internal class FakeGroupRepository(
    initial: ScreenState<List<Group>> = ScreenState.Loading,
) : GroupRepository {
    val state = MutableStateFlow(initial)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    fun emit(vararg groups: Group) {
```

</details>

- `fun groupsStream(scope: CoroutineScope): ScreenDataStream<List<Group>>` — Groups the officer services.
- `fun groupWithMembersStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupWithAssociations>` — One group with its member clients (`groups/{id}?associations=all`) — the detail's Clients section.
- `fun groupAccountsStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupAccounts>` — The group's loan and savings accounts (`groups/{id}/accounts`) — the detail's Accounts section.

### `core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt`

```kotlin
interface LoanCommandRepository
```
Every loan write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/approval/LoanApprovalViewModel.kt:83</code></summary>

```kotlin
internal class LoanApprovalViewModel(
    repository: LoanRepository,
    private val commands: LoanCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanApprovalState, LoanApprovalEvent, LoanApprovalAction>(
    LoanApprovalState(loanId = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1),
) {
```

</details>

- `suspend fun approveLoanApplication(loanId: Int, approval: LoanApproval): MutationResult<GenericResponse>`
- `suspend fun rejectLoan(loanId: Int, request: RejectLoanRequestDto): MutationResult<RejectLoanResponseDto>`
- `suspend fun disburse(loanId: Int, request: LoanDisburseInput): MutationResult<Unit>` — Disburse an approved loan. Takes `LoanDisburseInput` — a `core/model` type — rather than the network DTO, because `feature/loan` must not depend on `core:network`.
- `suspend fun submitPayment(`
- `suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>`
- `suspend fun assignLoanOfficer(`
- `suspend fun createLoanAccount(input: LoanApplicationInput): MutationResult<Unit>` — Submit a new INDIVIDUAL loan application.
- `suspend fun createGroupLoanAccount(payload: GroupLoanPayload): MutationResult<Loan>`
- `suspend fun calculateLoanSchedule(input: LoanApplicationInput): MutationResult<RepaymentSchedule>` — Preview the repayment schedule for an application that has NOT been submitted. Returns the schedule itself — the preview page renders it, so handing back an `HttpResponse` would make the one caller parse a body from a feature module.
- `suspend fun createGuarantor(loanId: Int, request: GuarantorRequestDto): MutationResult<CreateGuarantorResponseDto>`
- `suspend fun submitAccountTransfer(request: AccountTransferRequest): MutationResult<Unit>` — Move money between two accounts.
- `suspend fun submitLoanReschedule(request: LoanRescheduleRequest): MutationResult<Unit>` — Propose a reschedule. `Unit` for the same reason as `submitAccountTransfer` — the endpoint returns a raw Ktor response and a feature cannot name one.
- `suspend fun approveLoanReschedule(` — Approve a proposed reschedule. `loanId` is the loan it belongs to: the wire command is keyed by the reschedule alone, but its outcome — the reschedule's status and the loan's new schedule — is recorded under the loan.
- `suspend fun rejectLoanReschedule(` — REJECT a proposed reschedule.

### `core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanRepository.kt`

```kotlin
interface LoanRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/approval/LoanApprovalViewModel.kt:82</code></summary>

```kotlin
 */
internal class LoanApprovalViewModel(
    repository: LoanRepository,
    private val commands: LoanCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanApprovalState, LoanApprovalEvent, LoanApprovalAction>(
    LoanApprovalState(loanId = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1),
```

</details>

- `fun loanStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<LoanWithAssociations>` — One loan with its full associations — summary, timeline, schedule, transactions.
- `fun loanTemplateStream(clientId: Int, productId: Int?, scope: CoroutineScope): ScreenDataStream<LoanTemplate>` — The loan-application template for a (client, product). Cached: the officer fills this form in the field, so it has to render with no signal — otherwise the offline capture queue has nothing to capture.
- `fun repaymentTemplateStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<LoanRepaymentTemplate>` — Payment-type options a repayment form needs, per loan.
- `fun disburseTemplateStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<LoanDisburseTemplate>` — Expected amounts, payment types and dates for a disbursement, per loan.
- `suspend fun accountTransferTemplate(` — The account-transfer template: which offices, clients, account types and accounts the officer may transfer TO, given what they have chosen so far. ## A suspend one-shot, not a `ScreenDataStream` This is a cascading QUERY, not a subscription.
- `fun loanProductsStream(scope: CoroutineScope): ScreenDataStream<List<LoanProducts>>` — The tenant's loan PRODUCTS — a catalog, shared across every loan, hence no key.
- `suspend fun groupLoanTemplate(groupId: Int, productId: Int): GroupLoanTemplate?` — The group-loan template for one (group, product) pair. A suspend one-shot: the create screen asks again each time the officer picks a different product, which is the same cascading shape as `accountTransferTemplate`.
- `suspend fun loanTemplate(clientId: Int, productId: Int): LoanTemplate?` — The INDIVIDUAL loan-application template for one (client, product) pair.
- `fun loanReschedulesStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<List<LoanRescheduleResponse>>` — Every reschedule proposed on one loan, with its status and timeline. A STREAM, unlike the two template reads: the list is the screen's subject and changes when the officer approves or rejects a row, so it has to re-read.
- `suspend fun loanRescheduleReasons(): LoanRescheduleTemplate?` — The tenant's reschedule REASONS — a catalog, shared across loans, hence no key. A suspend one-shot like the other two templates: the propose form asks once when it opens.

### `core/data/src/commonMain/kotlin/kpt/core/data/note/NoteCommandRepository.kt`

```kotlin
interface NoteCommandRepository
```
Every note write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/note/src/commonMain/kotlin/kpt/feature/note/ui/AddEditNoteViewModel.kt:64</code></summary>

```kotlin
internal class AddEditNoteViewModel(
    private val repository: NoteRepository,
    private val commands: NoteCommandRepository,
    outbox: SubmitOutbox<NoteDraft>,
    savedStateHandle: SavedStateHandle,
) : BaseMutationViewModel<NoteDraft, Unit>(
    MutationMode.Draft(
```

</details>

- `suspend fun addNewNote(`
- `suspend fun deleteNote(resourceType: String, resourceId: Long, noteId: Long): MutationResult<DeleteNoteResponseDto>`
- `suspend fun updateNote(`
- `suspend fun submitDraft(draft: NoteDraft)` — Send one `NoteDraft` — create or update, decided by `noteId`. The single entry point the OUTBOX replays against.

### `core/data/src/commonMain/kotlin/kpt/core/data/note/NoteDataProviders.kt`

```kotlin
fun provideNoteOutbox(dao: DraftDao): SubmitOutbox<NoteDraft> =
```
Durable outbox for notes — `RoomSubmitOutbox` writes to `framework_submit_drafts`. The qualifier is not optional.

```kotlin
fun provideNoteSubmitSyncer(
```
Eager: watches connectivity from Koin start and replays pending notes once the app is BOTH online and authenticated. `createdAtStart` because the point is to drain drafts the user cannot see.

### `core/data/src/commonMain/kotlin/kpt/core/data/note/NoteRepository.kt`

```kotlin
interface NoteRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/note/src/commonMain/kotlin/kpt/feature/note/ui/AddEditNoteViewModel.kt:63</code></summary>

```kotlin
 */
internal class AddEditNoteViewModel(
    private val repository: NoteRepository,
    private val commands: NoteCommandRepository,
    outbox: SubmitOutbox<NoteDraft>,
    savedStateHandle: SavedStateHandle,
) : BaseMutationViewModel<NoteDraft, Unit>(
```

</details>

- `fun notesStream(resourceType: String, resourceId: Long, scope: CoroutineScope): ScreenDataStream<List<Note>>` — One owner's notes — `resourceType` is Fineract's path (`clients`, `groups`).

### `core/data/src/commonMain/kotlin/kpt/core/data/note/NoteSubmitException.kt`

```kotlin
sealed class NoteSubmitException(message: String, cause: Throwable? = null) :
```
Why a note submission failed, as a THROWN signal. `SubmitHandler` and `OfflineSubmitSyncer` are built on throw-to-fail: a block that returns is a success and its draft is marked SUBMITTED.

<details><summary>Used in the template — <code>feature/note/src/commonTest/kotlin/kpt/feature/note/ui/AddEditNoteViewModelTest.kt:128</code></summary>

```kotlin
        // THE regression this whole change exists for. Before it, this path showed "you are
        // offline", kept the text on screen, and lost it the moment the user navigated away.
        commands.failWith = NoteSubmitException.Offline("OFFLINE")
        val vm = viewModel(clientId = 7)
        vm.onTextChange("client was not at the shop; will revisit Thursday")
        vm.submitForm()
        runCurrent()
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/notification/NotificationRepository.kt`

```kotlin
interface NotificationRepository
```
The bell's notifications (unread first, then recent history) and the one write it offers.

<details><summary>Used in the template — <code>feature/notification/src/commonMain/kotlin/kpt/feature/notification/ui/NotificationsViewModel.kt:28</code></summary>

```kotlin
 */
internal class NotificationsViewModel(
    private val repository: NotificationRepository,
) : BaseViewModel<NotificationsState, NotificationsEvent, NotificationsAction>(NotificationsState()) {

    private val stream = repository.notificationsStream(viewModelScope)
```

</details>

- `fun notificationsStream(scope: CoroutineScope): ScreenDataStream<List<AppNotification>>`
- `suspend fun markAllRead(): MutationResult<Unit>` — Marks every notification read on the server. Online only — the list it changes is the server's.

### `core/data/src/commonMain/kotlin/kpt/core/data/office/OfficeRepository.kt`

```kotlin
interface OfficeRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/create/CreateCenterViewModel.kt:54</code></summary>

```kotlin
@OptIn(ExperimentalTime::class)
internal class CreateCenterViewModel(
    offices: OfficeRepository,
    private val commands: CenterCommandRepository,
    drafts: FormDraftRepository,
) : BaseViewModel<CreateCenterState, CreateCenterEvent, CreateCenterAction>(
    CreateCenterState(activationDateMillis = Clock.System.now().toEpochMilliseconds()),
```

</details>

- `fun officesStream(scope: CoroutineScope): ScreenDataStream<List<Office>>` — Every office in the tenant. Reference data — the officer's whole hierarchy picker depends on it.

### `core/data/src/commonMain/kotlin/kpt/core/data/offline/OfflineSyncRepository.kt`

```kotlin
interface OfflineSyncRepository
```
Settings → Offline sync: the officer chooses centres, groups or clients and a durable background job downloads each one's whole tree, so every screen of those records opens with no network.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/list/CenterListViewModel.kt:55</code></summary>

```kotlin
internal class CenterListViewModel(
    repository: CenterRepository,
    private val downloads: OfflineSyncRepository,
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : BaseViewModel<CenterListState, CenterListEvent, CenterListAction>(CenterListState()) {

    private val stream = repository.centersStream(viewModelScope)
```

</details>

- `val records: Flow<List<OfflineRecord>>` — Everything held on the device, at all three levels, for the signed-in officer.
- `val latestJob: Flow<SyncJob?>` — The running job, else the last one (its result card), else null.
- `val refresh: StateFlow<RefreshSettings>`
- `suspend fun download(targets: List<SyncTarget>)` — Keep `targets` on the device and download them now (picker, list long-press).
- `suspend fun syncNow()` — Re-download every chosen record.
- `suspend fun refreshRecord(target: SyncTarget)` — Re-download one chosen record.
- `suspend fun retryFailed()` — Re-download the records whose last download had refused reads.
- `suspend fun cancel()` — Stop the running job; what it already finished stays on the device.
- `suspend fun remove(target: SyncTarget)` — Stop keeping `target` (and what came in only through it) on the device.
- `suspend fun setRefresh(settings: RefreshSettings)` — Save Manual / Automatic and (re)schedule — or cancel — the one periodic job.
- `suspend fun runPending(refreshAll: Boolean)` — The background job's body: download every chosen record waiting for it, until none is left. `refreshAll` (the periodic run) first marks every chosen record for re-download.

```kotlin
interface OfflineSyncScheduler
```
Where the work runs — the background scheduler, so a download survives the screen and the process.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/ForkRepositoryModule.kt:72</code></summary>

```kotlin
    // is the SAME qualified store the record's screen reads, which is what makes "synced" mean
    // "opens offline".
    single<OfflineSyncScheduler> { WorkManagerOfflineSyncScheduler(workManager = get()) }
    single<OfflineSyncRepository> {
        val nowMs = { Clock.System.now().toEpochMilliseconds() }
        val engine = OfflineSyncEngine(
            entities = EntityStores(
```

</details>

- `suspend fun runNow()`
- `suspend fun cancelRun()`
- `suspend fun schedule(settings: RefreshSettings)`

### `core/data/src/commonMain/kotlin/kpt/core/data/offline/work/OfflineSyncWork.kt`

```kotlin
public class OfflineSyncWorker(
```
The background body of Settings → Offline sync.

### `core/data/src/commonMain/kotlin/kpt/core/data/pathtracking/PathTrackingRepository.kt`

```kotlin
interface PathTrackingRepository
```
Ported from `6b66e8a43:core/data/…/PathTrackingRepository.kt` — one read, now a stream.

<details><summary>Used in the template — <code>feature/pathtracking/src/commonMain/kotlin/kpt/feature/pathtracking/ui/PathTrackingViewModel.kt:40</code></summary>

```kotlin
internal class PathTrackingViewModel(
    session: SessionRepository,
    private val repository: PathTrackingRepository,
) : ViewModel() {

    private var stream: ScreenDataStream<List<UserLocation>>? = null
```

</details>

- `fun userPathsStream(userId: Int, scope: CoroutineScope): ScreenDataStream<List<UserLocation>>` — Every path recorded for `userId`, as Fineract's datatable returns them.

### `core/data/src/commonMain/kotlin/kpt/core/data/recent/RecentRecordsRepository.kt`

```kotlin
interface RecentRecordsRepository
```
The signed-in officer's recently-opened records — written by the profile screens, read by Home.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/detail/CenterDetailViewModel.kt:60</code></summary>

```kotlin
    centerRepository: CenterRepository,
    savedStateHandle: SavedStateHandle,
    private val recents: RecentRecordsRepository,
) : BaseViewModel<Unit, CenterDetailEvent, CenterDetailAction>(Unit) {

    /**
     * The route argument, read by the SAME key the `@Serializable` route property is named with —
```

</details>

- `val recent: Flow<List<RecentRecord>>`
- `suspend fun record(record: RecentRecord)`

```kotlin
class RecentRecordsRepositoryImpl(
```
Room-backed and keyed by the signed-in user, so a shared device never mixes officers' histories.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/ForkRepositoryModule.kt:62</code></summary>

```kotlin
    single<SessionSource> { SessionSource { get<ForkPreferencesRepository>().fineractUser } }
    single<kpt.core.data.recent.RecentRecordsRepository> {
        kpt.core.data.recent.RecentRecordsRepositoryImpl(
            get(),
            get(),
        )
    }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/recurringdeposit/RecurringDepositCommandRepository.kt`

```kotlin
interface RecurringDepositCommandRepository
```
Every recurringdeposit write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModel.kt:90</code></summary>

```kotlin
internal class RecurringDepositViewModel(
    repository: RecurringDepositRepository,
    private val commands: RecurringDepositCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<RecurringDepositState, RecurringDepositEvent, RecurringDepositAction>(
    RecurringDepositState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
```

</details>

- `suspend fun createRecurringDepositAccount(` — Submit a new recurring-deposit application. Takes a `core/model` input and returns `MutationResult<Unit>`: a feature module must not depend on `core:network`, and no caller needs the raw response.

### `core/data/src/commonMain/kotlin/kpt/core/data/recurringdeposit/RecurringDepositRepository.kt`

```kotlin
interface RecurringDepositRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModel.kt:89</code></summary>

```kotlin
@OptIn(ExperimentalTime::class)
internal class RecurringDepositViewModel(
    repository: RecurringDepositRepository,
    private val commands: RecurringDepositCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<RecurringDepositState, RecurringDepositEvent, RecurringDepositAction>(
```

</details>

- `fun templateStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<RecurringDepositAccountTemplate>` — The account-creation template for one client.

### `core/data/src/commonMain/kotlin/kpt/core/data/report/ReportRepository.kt`

```kotlin
interface ReportRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/report/src/commonMain/kotlin/kpt/feature/report/ui/list/ReportListViewModel.kt:31</code></summary>

```kotlin
 */
internal class ReportListViewModel(
    repository: ReportRepository,
) : BaseViewModel<ReportListState, ReportListEvent, ReportListAction>(ReportListState()) {

    private val stream = repository.reportTypesStream(viewModelScope)
```

</details>

- `fun reportTypesStream(scope: CoroutineScope): ScreenDataStream<List<ClientReportTypeItem>>` — The runnable report catalogue.
- `fun reportParametersStream(reportName: String, scope: CoroutineScope): ScreenDataStream<List<ReportParameter>>` — What `reportName` asks for — its parameters, in the server's order.
- `suspend fun parameterOptions(parameterName: String, parentValues: Map<String, String>): List<ReportOption>?` — A SELECT parameter's options. `parentValues` narrow them (`R_officeId` for a loan-officer list); null when they could not be read.
- `suspend fun runReport(reportName: String, values: Map<String, String>): Result<FullParameterListResponse>` — Runs `reportName` with `values` (`R_*` keys). Always from the server — a report is not cached data.

### `core/data/src/commonMain/kotlin/kpt/core/data/savings/SavingsAccountRepository.kt`

```kotlin
interface SavingsAccountRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/fixeddepositapplication/FixedDepositApplicationViewModel.kt:89</code></summary>

```kotlin
 */
internal class FixedDepositApplicationViewModel(
    repository: SavingsAccountRepository,
    private val commands: SavingsCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<
```

</details>

- `fun savingsAccountStream(` — One savings account with its transactions.
- `suspend fun transactionTemplate(` — The transaction template for one (account, command) pair — its payment-type options.
- `fun savingsProductsStream(scope: CoroutineScope): ScreenDataStream<List<ProductSavings>>` — The tenant's savings products — what the create wizard opens on.
- `fun shareTemplateStream(` — The share-account application template for one client, optionally narrowed to one product. `productId = null` is the FIRST read — it returns the product options to choose from.
- `fun fixedDepositTemplateStream(` — The fixed-deposit application template for one client, optionally narrowed to one product.
- `suspend fun savingsApplicationTemplate(` — The savings-application template for one (owner, product) pair. `isGroup` selects Fineract's GROUP endpoint over the client one.

### `core/data/src/commonMain/kotlin/kpt/core/data/savings/SavingsCommandRepository.kt`

```kotlin
interface SavingsCommandRepository
```
Every savings write, as an explicit outcome.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/fixeddepositapplication/FixedDepositApplicationViewModel.kt:90</code></summary>

```kotlin
internal class FixedDepositApplicationViewModel(
    repository: SavingsAccountRepository,
    private val commands: SavingsCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<
    FixedDepositApplicationState,
```

</details>

- `suspend fun createFixedDepositAccount(fixedDepositPayload: FixedDepositPayload): MutationResult<HttpResponse>`
- `suspend fun processTransaction(` — Deposit or withdraw on one savings account. `savingsAccountType` is Fineract's path segment for the deposit kind, so the same numeric id under a different type is a different account.
- `suspend fun activateSavings(` — Activate an approved savings account. Typed, not a `HashMap<String, String>`.
- `suspend fun approveSavingsApplication(` — Approve a submitted savings application.
- `suspend fun createSavingsAccount(input: SavingsApplicationInput): MutationResult<Unit>` — Submit a new savings application for a client or a group.
- `suspend fun createShareAccount(shareAccountPayload: ShareAccountPayload): MutationResult<HttpResponse>`

### `core/data/src/commonMain/kotlin/kpt/core/data/search/SearchRepository.kt`

```kotlin
interface SearchRepository
```
Fineract resource search — the read behind the Home tab. `SearchApi`, `SearchKeyedReadStores` and `SearchedEntity` all existed; only this repository was missing, which is why S2 could not start (`feature/home/MIGRATION.md` §3).

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/search/SearchViewModel.kt:39</code></summary>

```kotlin
 */
class SearchViewModel(
    private val repository: SearchRepository,
) : BaseViewModel<SearchState, SearchEvent, SearchAction>(SearchState()) {

    /**
     * The query actually SUBMITTED, as opposed to the one being typed.
```

</details>

- `fun searchStream(key: SearchResourcesKey, scope: CoroutineScope): ScreenDataStream<List<SearchedEntity>>` — Results for `key`, served through the store so the last answer still renders with no signal. A BLANK query returns empty without reaching the server — see `SearchRepositoryImpl`.

### `core/data/src/commonMain/kotlin/kpt/core/data/searchrecord/SearchRecordRepository.kt`

```kotlin
interface SearchRecordRepository
```
Search over the client addresses / identifiers already cached on this device — no network.

<details><summary>Used in the template — <code>feature/searchrecord/src/commonMain/kotlin/kpt/feature/searchrecord/ui/SearchRecordViewModel.kt:49</code></summary>

```kotlin
internal class SearchRecordViewModel(
    savedStateHandle: SavedStateHandle,
    repository: SearchRecordRepository,
) : BaseViewModel<SearchRecordInput, SearchRecordEvent, SearchRecordAction>(
    initialState = SearchRecordInput(
        recordType = RecordType.valueOf(requireNotNull(savedStateHandle.get<String>("type"))),
    ),
```

</details>

- `fun searchStream(keys: Flow<SearchRecordKey>, scope: CoroutineScope): ScreenDataStream<List<GenericSearchRecord>>`

### `core/data/src/commonMain/kotlin/kpt/core/data/serverconfig/ServerConfigRepository.kt`

```kotlin
interface ServerConfigRepository
```
The Fineract server configuration, read and written through `core/data` like every other value a screen shows — never by a ViewModel reaching into `core/datastore` itself.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:79</code></summary>

```kotlin
    private val groupRepository: GroupRepository,
    private val formDraftRepository: FormDraftRepository,
    private val serverConfigRepository: ServerConfigRepository,
    private val recentRecordsRepository: RecentRecordsRepository,
) : BaseViewModel<HomeUiState, Nothing, HomeAction>(HomeUiState()) {

    /** The live streams behind each card, by source — what that card's retry re-requests. */
```

</details>

- `val defaultConfig: ServerConfig` — The server the app ships pointed at — what "Use defaults" restores. Derived from app-profile.
- `fun instancesStream(scope: CoroutineScope): ScreenDataStream<List<ServerInstance>>` — The named servers to pick from — Supabase `app_server_config` (this app's rows) through the `serverInstances` store; demo instances carry their published sign-ins.
- `fun serverConfigStream(scope: CoroutineScope): ScreenDataStream<ServerConfig>` — Local-only (`CACHE_ONLY`): the configuration is a device preference with no network source.
- `suspend fun updateServerConfig(config: ServerConfig): MutationResult<Unit>` — Persists `config`; the stream re-emits it.

### `core/data/src/commonMain/kotlin/kpt/core/data/session/SessionRepository.kt`

```kotlin
interface SessionRepository
```
The stored Fineract session, read through core/data — never by a feature reaching into core/datastore.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:72</code></summary>

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val sessionRepository: SessionRepository,
    private val dashboardRepository: DashboardRepository,
    private val checkerInboxRepository: CheckerInboxRepository,
    private val collectionSheetRepository: CollectionSheetRepository,
    private val centerRepository: CenterRepository,
```

</details>

- `fun sessionStream(scope: CoroutineScope): ScreenDataStream<User>` — Local-only (`CACHE_ONLY`). An empty `User` means no stored session.

### `core/data/src/commonMain/kotlin/kpt/core/data/staff/StaffRepository.kt`

```kotlin
interface StaffRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientViewModel.kt:201</code></summary>

```kotlin
    clients: ClientRepository,
    offices: OfficeRepository,
    private val staff: StaffRepository,
    private val commands: ClientCommandRepository,
    drafts: FormDraftRepository,
) : BaseViewModel<CreateNewClientState, CreateNewClientEvent, CreateNewClientAction>(
    CreateNewClientState(),
```

</details>

- `fun staffStream(officeId: Int, scope: CoroutineScope): ScreenDataStream<List<Staff>>` — Loan officers attached to one office.

### `core/data/src/commonMain/kotlin/kpt/core/data/survey/SurveyCommandRepository.kt`

```kotlin
interface SurveyCommandRepository
```
Every survey write, as an explicit outcome.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/survey/impl/SurveyCommandRepositoryImpl.kt:21</code></summary>

```kotlin
import kpt.core.network.mifos.survey.api.SurveyApi

@RepositoryBinding(binds = SurveyCommandRepository::class)
internal class SurveyCommandRepositoryImpl(
    private val surveyApi: SurveyApi,
    private val gateway: MutationGateway,
) : SurveyCommandRepository {
```

</details>

- `suspend fun submitScore(surveyId: Int, scorecardPayload: Scorecard?): MutationResult<Scorecard>`

### `core/data/src/commonMain/kotlin/kpt/core/data/survey/SurveyRepository.kt`

```kotlin
interface SurveyRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/survey/impl/SurveyRepositoryImpl.kt:25</code></summary>

```kotlin
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = SurveyRepository::class)
internal class SurveyRepositoryImpl(
    @FromStore(AppStoreIds.Surveys) private val store: Store<Unit, List<Survey>>,
) : SurveyRepository {
```

</details>

- `fun surveysStream(scope: CoroutineScope): ScreenDataStream<List<Survey>>` — Surveys the officer can administer in the field.

### `core/data/src/commonMain/kotlin/kpt/core/data/sync/OfflineQueueRepository.kt`

```kotlin
interface OfflineQueueRepository
```
Work captured with no connectivity, and the queue the sync screen drains. Enqueue is `MutationPolicy.Optimistic` by construction — capture happens against Room and MUST succeed offline, which is the entire point of the app.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/repayment/LoanRepaymentViewModel.kt:78</code></summary>

```kotlin
    private val repository: LoanRepository,
    private val commands: LoanCommandRepository,
    private val queue: OfflineQueueRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanRepaymentState, LoanRepaymentEvent, LoanRepaymentAction>(
    LoanRepaymentState(loanId = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1),
) {
```

</details>

- `fun pendingClientsStream(scope: CoroutineScope): ScreenDataStream<List<ClientPayload>>`
- `fun pendingCentersStream(scope: CoroutineScope): ScreenDataStream<List<CenterPayload>>`
- `fun pendingGroupsStream(scope: CoroutineScope): ScreenDataStream<List<GroupPayload>>`
- `fun pendingLoanRepaymentsStream(scope: CoroutineScope): ScreenDataStream<List<LoanRepaymentRequest>>`
- `fun pendingSavingsTransactionsStream(`
- `suspend fun enqueueClient(id: Int, payload: ClientPayload): MutationResult<ClientPayload>`
- `suspend fun enqueueCenter(id: Int, payload: CenterPayload): MutationResult<CenterPayload>`
- `suspend fun enqueueGroup(id: Int, payload: GroupPayload): MutationResult<GroupPayload>`
- `suspend fun enqueueLoanRepayment(loanId: Int, request: LoanRepaymentRequest): MutationResult<LoanRepaymentRequest>`
- `suspend fun enqueueSavingsTransaction(`
- `suspend fun discardClient(id: Int)` — Drop a queued row — the sync accepted it, or the officer discarded it.
- `suspend fun discardCenter(id: Int)`
- `suspend fun discardGroup(id: Int)`
- `suspend fun discardLoanRepayment(loanId: Int)`
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/ExperimentalDataTestingApi.kt`

```kotlin
annotation class ExperimentalDataTestingApi
```
Opt-in marker for `core/data` test doubles that ship in commonMain so consuming modules can use them.

<details><summary>Used in the template — <code>feature/activate/src/commonTest/kotlin/kpt/feature/activate/ui/ActivateViewModelTest.kt:10</code></summary>

```kotlin
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.data.testing.ExperimentalDataTestingApi::class)

package kpt.feature.activate.ui

import androidx.lifecycle.SavedStateHandle
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeAuthCommandRepository.kt`

```kotlin
class FakeAuthCommandRepository : AuthCommandRepository
```
Records sign-outs (the clearLocalData flag of each) and password changes; sign-in is not scripted.

<details><summary>Used in the template — <code>feature/profile/src/commonTest/kotlin/kpt/feature/profile/identity/ProfileViewModelTest.kt:97</code></summary>

```kotlin
    }
    private val network = FakeNetworkMonitor()
    private val auth = FakeAuthCommandRepository()
    private val servers = FakeServerConfigRepository()

    private suspend fun preferences() = run {
        val plain = MapSettings()
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeCenterRepository.kt`

```kotlin
class FakeCenterRepository(
```
Scripted centres: `centers` is the list; `withMeeting` holds each centre's calendar by id.

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/detail/CenterDetailViewModelTest.kt:50</code></summary>

```kotlin

    private fun viewModel(
        centers: FakeCenterRepository,
        centerId: Int = 7,
    ) = CenterDetailViewModel(centers, SavedStateHandle(mapOf("centerId" to centerId)), FakeRecentRecordsRepository())

    /** The centre's own read, carrying [groups] as its `groupMembers`. */
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeChargeCommandRepository.kt`

```kotlin
class FakeChargeCommandRepository(
```
Test double for `ChargeCommandRepository` — beside the interface it fakes, same reasoning as `FakeClientCommandRepository`. RECORDS every write and replays one scripted outcome.

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:81</code></summary>

```kotlin
        resource: ChargeResource,
        repository: FakeChargeRepository = FakeChargeRepository(template = { catalogue }),
        commands: FakeChargeCommandRepository = FakeChargeCommandRepository(),
        chargeId: Int = -1,
    ) = ChargeFormViewModel(
        repository,
        commands,
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeChargeRepository.kt`

```kotlin
class FakeChargeRepository(
```
Test double for `ChargeRepository` — beside the interface it fakes, so a new read breaks ONE fake.

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeDetailViewModelTest.kt:44</code></summary>

```kotlin
    private val rows = listOf(Charges(id = 4, name = "Other"), Charges(id = 5, name = "Fee", penalty = true))

    private fun viewModel(repository: FakeChargeRepository, resource: ChargeResource = ChargeResource.LOAN) =
        ChargeDetailViewModel(
            repository,
            SavedStateHandle(mapOf("resourceType" to resource.path, "resourceId" to 7, "chargeId" to 5)),
        )
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeCheckerInboxCommandRepository.kt`

```kotlin
class FakeCheckerInboxCommandRepository(
```
Records every decision; `result` answers per task. An APPLIED decision is removed from `inbox`, as the real command clears the row through the store.

<details><summary>Used in the template — <code>feature/checkerinbox/src/commonTest/kotlin/kpt/feature/checkerinbox/ui/CheckerInboxViewModelTest.kt:63</code></summary>

```kotlin
    private fun TestScope.started(
        inbox: FakeCheckerInboxRepository = FakeCheckerInboxRepository(tasks),
        commands: FakeCheckerInboxCommandRepository = FakeCheckerInboxCommandRepository(inbox),
    ) = CheckerInboxViewModel(inbox, commands).also { vm ->
        backgroundScope.launch { vm.inbox.collect {} }
        backgroundScope.launch { vm.reschedules.collect {} }
        runCurrent()
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeCheckerInboxRepository.kt`

```kotlin
class FakeCheckerInboxRepository(
```
Scripted inbox. `tasks` and `reschedules` stream; `FakeCheckerInboxCommandRepository` removes decided tasks from `tasks`.

<details><summary>Used in the template — <code>feature/checkerinbox/src/commonTest/kotlin/kpt/feature/checkerinbox/ui/CheckerInboxViewModelTest.kt:62</code></summary>

```kotlin

    private fun TestScope.started(
        inbox: FakeCheckerInboxRepository = FakeCheckerInboxRepository(tasks),
        commands: FakeCheckerInboxCommandRepository = FakeCheckerInboxCommandRepository(inbox),
    ) = CheckerInboxViewModel(inbox, commands).also { vm ->
        backgroundScope.launch { vm.inbox.collect {} }
        backgroundScope.launch { vm.reschedules.collect {} }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeClientCommandRepository.kt`

```kotlin
class FakeClientCommandRepository(
```
Test double for `ClientCommandRepository`, owned by the module that owns the CONTRACT.

<details><summary>Used in the template — <code>feature/activate/src/commonTest/kotlin/kpt/feature/activate/ui/ActivateViewModelTest.kt:55</code></summary>

```kotlin
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val clients = FakeClientCommandRepository()
    private val centers = FakeCenterCommands()
    private val groups = FakeGroupCommands()

    private fun viewModel(id: Int = 7, type: String = Constants.ACTIVATE_GROUP) =
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeClientRepository.kt`

```kotlin
class FakeClientRepository(
```
Test double for `ClientRepository`, owned by the module that owns the CONTRACT — same reasoning as `FakeClientCommandRepository` (see its KDoc for why `core/data` and why `commonMain`).

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/address/ClientAddressViewModelTest.kt:43</code></summary>

```kotlin

    private val addresses = MutableStateFlow<ScreenState<List<ClientAddressEntity>>>(ScreenState.Loading)
    private val repository = FakeClientRepository(addresses = addresses)

    private fun viewModel(clientId: Int = 42) =
        ClientAddressViewModel(repository, SavedStateHandle(mapOf("clientId" to clientId)))
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeCollectionSheetCommandRepository.kt`

```kotlin
class FakeCollectionSheetCommandRepository(
```
Records each individual-sheet save — what was sent, for which request — and answers `result`.

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonTest/kotlin/kpt/feature/collectionsheet/ui/CollectionSheetDetailsViewModelTest.kt:84</code></summary>

```kotlin

    private val repository = FakeCollectionSheetRepository(ScreenState.Content(sheet))
    private val commands = FakeCollectionSheetCommandRepository()

    private fun TestScope.started(
        commands: FakeCollectionSheetCommandRepository = this@CollectionSheetDetailsViewModelTest.commands,
    ) =
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeCollectionSheetRepository.kt`

```kotlin
class FakeCollectionSheetRepository(
```
The individual sheet a test drives through `sheet`; `requested` records every request streamed. The center/group reads are not exercised by any feature yet and answer "nothing".

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonTest/kotlin/kpt/feature/collectionsheet/ui/CollectionSheetDetailsViewModelTest.kt:83</code></summary>

```kotlin
    )

    private val repository = FakeCollectionSheetRepository(ScreenState.Content(sheet))
    private val commands = FakeCollectionSheetCommandRepository()

    private fun TestScope.started(
        commands: FakeCollectionSheetCommandRepository = this@CollectionSheetDetailsViewModelTest.commands,
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeDashboardRepository.kt`

```kotlin
class FakeDashboardRepository(
```
Scripted dashboard sources; every stream is a `MutableStateFlow` a test can move. `requested` records each call.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/dashboard/HomeViewModelTest.kt:67</code></summary>

```kotlin
        permissions: List<String>,
        staffId: Long? = 1,
        val dashboard: FakeDashboardRepository = FakeDashboardRepository(
            user = DashboardUser(id = 32, firstname = "Irving", staffId = staffId),
            loansByStatus = mapOf(100 to emptyList(), 200 to emptyList(), 300 to emptyList()),
        ),
        val sheet: FakeCollectionSheetRepository = FakeCollectionSheetRepository(ScreenState.Content(IndividualCollectionSheet())),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeFormDraftRepository.kt`

```kotlin
class FakeFormDraftRepository : FormDraftRepository
```
In-memory form drafts: `saved` is what a test asserts on.

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/create/CreateCenterViewModelTest.kt:86</code></summary>

```kotlin
    }

    private val drafts = FakeFormDraftRepository()

    private fun vm(
        result: () -> MutationResult<SaveResponse> = { MutationResult.Applied(SaveResponse(), synced = true) },
    ) =
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeGroupRepository.kt`

```kotlin
class FakeGroupRepository(groups: List<Group> = emptyList()) : GroupRepository
```
Scripted group list; the per-group reads return an empty record for the id asked.

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/testing/FakeCenterRepositories.kt:67</code></summary>

```kotlin
}

internal class FakeGroupRepository(
    initial: ScreenState<List<Group>> = ScreenState.Loading,
) : GroupRepository {
    val state = MutableStateFlow(initial)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeLoanCommandRepository.kt`

```kotlin
class FakeLoanCommandRepository(
```
Test double for `LoanCommandRepository` — the loan-side twin of `FakeClientCommandRepository`, for the same reason (the interface names `core/network` DTOs, so the fake belongs beside it).

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/approval/LoanApprovalViewModelTest.kt:53</code></summary>

```kotlin

    private fun viewModel(
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        repository: FakeLoanRepository = FakeLoanRepository(loan = loanState),
        loanId: Int = 7,
    ) = LoanApprovalViewModel(repository, commands, SavedStateHandle(mapOf("loanId" to loanId)))
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeLoanRepository.kt`

```kotlin
class FakeLoanRepository(
```
Test double for `LoanRepository` — same contract-owner reasoning as `FakeClientRepository`: the module that declares the interface owns its fake, so adding a stream breaks it in ONE place instead of in every anonymous `object : LoanRepository` scattered across feature tests. Three screens now share `loanStream` (profile, schedule, transactions) because the schedule and the transactions ARE part of `LoanWithAssociations` — so a fake that hands all three the same supplied flow is not a convenience here, it is the production shape. Unimplemented streams THROW. See `FakeClientRepository`'s KDoc for why that is deliberate.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/approval/LoanApprovalViewModelTest.kt:54</code></summary>

```kotlin
    private fun viewModel(
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        repository: FakeLoanRepository = FakeLoanRepository(loan = loanState),
        loanId: Int = 7,
    ) = LoanApprovalViewModel(repository, commands, SavedStateHandle(mapOf("loanId" to loanId)))

    @Test
```

</details>

```kotlin
data class TransferTemplateQuery(
```
One `accountTransferTemplate` call, recorded so a test can assert the cascade's questions.

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeNetworkMonitor.kt`

```kotlin
class FakeNetworkMonitor(online: Boolean = true) : NetworkMonitor
```
A `NetworkMonitor` the test switches: set `online` to drive a screen into / out of its offline state.

<details><summary>Used in the template — <code>feature/offlinesync/src/commonTest/kotlin/kpt/feature/offlinesync/ui/OfflineSyncViewModelTest.kt:102</code></summary>

```kotlin
    private fun vm(
        repo: FakeOfflineSyncRepository = FakeOfflineSyncRepository(listOf(centre, group, asha, irving, waiting)),
        network: FakeNetworkMonitor = FakeNetworkMonitor(),
        centers: FakeCenterRepository = FakeCenterRepository(
            listOf(Center(id = 10, name = "Centro Arin"), Center(id = 27, name = "Centro Las Flores")),
        ),
    ) = OfflineSyncViewModel(repo, centers, FakeGroupRepository(), FakeClientRepository(), network, nowMs = { NOW })
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeNotificationRepository.kt`

```kotlin
class FakeNotificationRepository(
```
Scripted notifications; `markAllReadResult` is what the write answers, `markedRead` counts the calls.

<details><summary>Used in the template — <code>feature/notification/src/commonTest/kotlin/kpt/feature/notification/ui/NotificationsViewModelTest.kt:45</code></summary>

```kotlin
    @Test
    fun markAllReadClearsEveryUnreadRow() = runTest(dispatcher) {
        val repo = FakeNotificationRepository(listOf(n(1, "loan", 9, read = false), n(2, "client", 3, read = true)))
        val vm = NotificationsViewModel(repo)
        vm.trySendAction(NotificationsAction.MarkAllRead)
        advanceUntilIdle()
        assertEquals(1, repo.markedRead)
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeOfflineQueueRepository.kt`

```kotlin
class FakeOfflineQueueRepository(
```
Test double for `OfflineQueueRepository`, beside the interface it fakes — same reasoning as `FakeClientRepository`.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/repayment/LoanRepaymentViewModelTest.kt:88</code></summary>

```kotlin
    private fun viewModel(
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        queue: FakeOfflineQueueRepository = FakeOfflineQueueRepository(pendingLoanRepayments = pending),
        repository: FakeLoanRepository = FakeLoanRepository(
            loan = loanState,
            repaymentTemplate = templateState,
        ),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeOfflineSyncRepository.kt`

```kotlin
class FakeOfflineSyncRepository(
```
In-memory Offline sync. Every call is recorded in `calls` (`"download:CENTRE:10"`, `"syncNow"`, …) so a test asserts what the screen asked for; `records` / `latestJob` are set directly to drive the screen through its states.

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/list/CenterListViewModelTest.kt:45</code></summary>

```kotlin
class CenterListViewModelTest {

    private val downloads = FakeOfflineSyncRepository()

    private fun repoWith() = FakeCenterRepository().also { it.emit(center(1), center(2)) }

    private val dispatcher = StandardTestDispatcher()
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakePathTrackingRepository.kt`

```kotlin
class FakePathTrackingRepository(paths: List<UserLocation> = emptyList()) : PathTrackingRepository
```
Scripted paths; `requested` records the user id each stream was opened for.

<details><summary>Used in the template — <code>feature/pathtracking/src/commonTest/kotlin/kpt/feature/pathtracking/ui/PathTrackingViewModelTest.kt:43</code></summary>

```kotlin
    @Test
    fun readsTheSignedInUsersOwnPathsNotTheOffice() = runTest(dispatcher) {
        val repo = FakePathTrackingRepository(
            listOf(
                UserLocation(
                    staffId = 1,
                    latLng = """[{"lat":19.4,"lng":-99.1},{"lat":19.5,"lng":-99.2}]""",
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeRecentRecordsRepository.kt`

```kotlin
class FakeRecentRecordsRepository(initial: List<RecentRecord> = emptyList()) : RecentRecordsRepository
```
In-memory recents: `recorded` is everything written, most recent first.

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/detail/CenterDetailViewModelTest.kt:52</code></summary>

```kotlin
        centers: FakeCenterRepository,
        centerId: Int = 7,
    ) = CenterDetailViewModel(centers, SavedStateHandle(mapOf("centerId" to centerId)), FakeRecentRecordsRepository())

    /** The centre's own read, carrying [groups] as its `groupMembers`. */
    private fun FakeCenterRepository.emitMembers(vararg groups: Group) {
        meetingState.value = ScreenState.Content(CenterWithAssociations(id = 7, groupMembers = groups.toList()))
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeRecurringDepositRepository.kt`

```kotlin
class FakeRecurringDepositRepository(
```
Test double for `RecurringDepositRepository`.

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:88</code></summary>

```kotlin
    private fun viewModel(
        commands: FakeRecurringDepositCommandRepository = FakeRecurringDepositCommandRepository(),
        repo: FakeRecurringDepositRepository = FakeRecurringDepositRepository(
            template = MutableStateFlow(ScreenState.Content(template())),
        ),
        clientId: Int = 55,
        drafts: kpt.core.data.testing.FakeFormDraftRepository = kpt.core.data.testing.FakeFormDraftRepository(),
```

</details>

```kotlin
class FakeRecurringDepositCommandRepository(
```
Test double for `RecurringDepositCommandRepository`.

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:87</code></summary>

```kotlin

    private fun viewModel(
        commands: FakeRecurringDepositCommandRepository = FakeRecurringDepositCommandRepository(),
        repo: FakeRecurringDepositRepository = FakeRecurringDepositRepository(
            template = MutableStateFlow(ScreenState.Content(template())),
        ),
        clientId: Int = 55,
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeReportRepository.kt`

```kotlin
class FakeReportRepository(
```
Scripted reports: `reports` and `parameters` stream; `options` answer by parameter name; `result` answers a run.

<details><summary>Used in the template — <code>feature/report/src/commonTest/kotlin/kpt/feature/report/ui/ReportViewModelsTest.kt:80</code></summary>

```kotlin
    )

    private fun TestScope.list() = ReportListViewModel(FakeReportRepository(reports = catalogue)).also { vm ->
        backgroundScope.launch { vm.reports.collect {} }
        runCurrent()
    }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeSavingsAccountRepository.kt`

```kotlin
class FakeSavingsAccountRepository(
```
Test double for `SavingsAccountRepository` — the savings-side twin of `FakeLoanRepository`.

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/application/SavingsApplicationViewModelTest.kt:81</code></summary>

```kotlin
    private fun viewModel(
        commands: FakeSavingsCommandRepository = FakeSavingsCommandRepository(),
        repo: FakeSavingsAccountRepository = FakeSavingsAccountRepository(
            savingsProducts = MutableStateFlow(ScreenState.Content(products)),
            applicationTemplate = { template() },
        ),
        ownerId: Int = 31,
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeSavingsCommandRepository.kt`

```kotlin
class FakeSavingsCommandRepository(
```
Test double for `SavingsCommandRepository` — the savings-side twin of `FakeLoanCommandRepository`.

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/application/SavingsApplicationViewModelTest.kt:80</code></summary>

```kotlin

    private fun viewModel(
        commands: FakeSavingsCommandRepository = FakeSavingsCommandRepository(),
        repo: FakeSavingsAccountRepository = FakeSavingsAccountRepository(
            savingsProducts = MutableStateFlow(ScreenState.Content(products)),
            applicationTemplate = { template() },
        ),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeSearchRecordRepository.kt`

```kotlin
class FakeSearchRecordRepository(
```
Test double for `SearchRecordRepository`: `records` is the cached data, `matches` decides which of it a key finds (default: name or description contains the query). `requested` records every key.

<details><summary>Used in the template — <code>feature/searchrecord/src/commonTest/kotlin/kpt/feature/searchrecord/ui/SearchRecordViewModelTest.kt:64</code></summary>

```kotlin
    )

    private val repository = FakeSearchRecordRepository(listOf(home, office, passport))

    private fun viewModel(type: RecordType = RecordType.ADDRESS) =
        SearchRecordViewModel(SavedStateHandle(mapOf("type" to type.name)), repository)
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeSearchRepository.kt`

```kotlin
class FakeSearchRepository : SearchRepository
```
Test double for `SearchRepository`, owned by the module that owns the CONTRACT.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/search/SearchViewModelTest.kt:48</code></summary>

```kotlin
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val repository = FakeSearchRepository()

    private fun hit(id: Int, name: String) = SearchedEntity(entityId = id, entityName = name)

    private fun key(query: String, resource: String? = null, exact: Boolean = false) =
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeServerConfigRepository.kt`

```kotlin
class FakeServerConfigRepository(
```
Test double for `ServerConfigRepository`: `saved` is the persisted preference, so a test reads what a submit actually wrote. `failSaves` models the write failing.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/dashboard/HomeViewModelTest.kt:81</code></summary>

```kotlin
            groupRepository = FakeGroupRepository(),
            formDraftRepository = FakeFormDraftRepository(),
            serverConfigRepository = FakeServerConfigRepository(),
            recentRecordsRepository = FakeRecentRecordsRepository(),
        )
    }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FakeSessionRepository.kt`

```kotlin
class FakeSessionRepository(initial: User = User()) : SessionRepository
```
Test double for `SessionRepository`: `session` is the stored user; an empty `User` is "signed out".

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/dashboard/HomeViewModelTest.kt:74</code></summary>

```kotlin
    ) {
        val vm = HomeViewModel(
            sessionRepository = FakeSessionRepository(User(username = "fieldofficer", userId = 32, officeId = 1, officeName = "CENTRAL", permissions = permissions)),
            dashboardRepository = dashboard,
            checkerInboxRepository = FakeCheckerInboxRepository(),
            collectionSheetRepository = sheet,
            centerRepository = FakeCenterRepository(),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/testing/FineractRejection.kt`

```kotlin
fun fineractRejection(userMessage: String, status: Int = 403): Throwable = RuntimeException(
```
A failed write as it really arrives: Ktor's exception, whose `message` is the request line followed by Fineract's JSON body carrying `userMessage` as the field error.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/AuthTokenBridge.kt`

```kotlin
fun provideAuthTokenSource(preferences: UserPreferencesRepository): AuthTokenSource =
```
Binds the network's `AuthTokenSource` port to where the credential actually lives.

```kotlin
fun provideAuthHeaderBridge(
```
Eager: starts collecting the credential at graph construction. `createdAtStart` is load-bearing. Built lazily, this would construct on first injection — and nothing injects it, because its whole job is a side effect.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/LogoutEvent.kt`

```kotlin
data class LogoutEvent(
```
Result class to share the `loggedOutUserId` of a user that was successfully logged out.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/UserLogoutManager.kt:24</code></summary>

```kotlin
     * Observable flow of [LogoutEvent]s
     */
    val logoutEventFlow: SharedFlow<LogoutEvent>

    /**
     * Completely logs out the given [userId], removing all data. The [reason] indicates why the
     * user is being logged out.
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/user/LogoutReason.kt`

```kotlin
sealed class LogoutReason
```
Indicates the reason that the user is being logged out.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/UserLogoutManager.kt:30</code></summary>

```kotlin
     * user is being logged out.
     */
    fun logout(userId: Long, reason: LogoutReason)

    /**
     * Partially logs out the given [userId]. All data for the given [userId] will be removed with
     * the exception of basic account data. The [reason] indicates why the user is being logged out.
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/user/UserDataRepository.kt`

```kotlin
interface UserDataRepository
```
Repository interface for managing user preferences with reactive capabilities. This interface provides reactive access to user preferences including theme settings, dark mode configuration, and dynamic color preferences.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsViewModel.kt:44</code></summary>

```kotlin
 */
class SettingsViewModel(
    private val settingsRepository: UserDataRepository,
    private val analyticsHelper: AnalyticsHelper,
) : BaseViewModel<Unit, Nothing, SettingsAction>(Unit) {

    /** The store-backed preferences read — retained so [onRetry] re-runs it. */
```

</details>

- `val userData: StateFlow<UserData>` — The whole preference aggregate as hot state, so a screen has a value at first composition.
- `fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData>` — Store5-backed read of the same preferences, as a `ScreenDataStream`. `userData` stays for the many call sites that just want the current value (auth, theme bootstrap).
- `val authToken: String?` — The stored credential, or null when signed out. Synchronous read; prefer a stream when the value can change under you.
- `val passcode: String` — The app-lock passcode.
- `val observeLanguage: Flow<LanguageConfig>` — Selected language, re-emitting on change.
- `val observeDarkThemeConfig: Flow<DarkThemeConfig>` — Dark-mode preference as a stream.
- `val observeDynamicColorPreference: Flow<Boolean>` — Whether to derive the palette from platform dynamic colour.
- `val observeScreenCapturePreference: Flow<Boolean>` — Whether screenshots and screen recording are permitted.
- `val observeAnalyticsConsent: Flow<Boolean>` — The user's consent to share usage analytics.
- `val observeCrashReportingConsent: Flow<Boolean>` — The user's consent to send crash reports.
- `suspend fun setAnalyticsConsent(granted: Boolean)` — Records the user's analytics consent — given or withdrawn.
- `suspend fun setCrashReportingConsent(granted: Boolean)` — Records the user's crash-reporting consent — given or withdrawn.
- `suspend fun setLanguage(language: LanguageConfig)` — Persists the app language.
- `suspend fun setThemeBrand(themeBrand: ThemeBrand)` — Persists the colour brand.
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/user/UserLogoutManager.kt`

```kotlin
interface UserLogoutManager
```
Coordinates logout across the app: wipes local state and notifies observers. Centralised so "what must be cleared on logout" is answered once. A feature clearing its own slice is how one gets missed.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:60</code></summary>

```kotlin
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(), get(), get()) }
}

/**
 * Per-target repository bindings supplied by each `actual` — the pieces that cannot be expressed in
```

</details>

- `val logoutEventFlow: SharedFlow<LogoutEvent>` — Observable flow of `LogoutEvent`s
- `fun logout(userId: Long, reason: LogoutReason)` — Completely logs out the given `userId`, removing all data. The `reason` indicates why the user is being logged out.
- `fun softLogout(userId: Long, reason: LogoutReason)` — Partially logs out the given `userId`. All data for the given `userId` will be removed with the exception of basic account data. The `reason` indicates why the user is being logged out.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/impl/UserDataRepositoryImpl.kt`

```kotlin
class UserDataRepositoryImpl(
```
Default `UserDataRepository`.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/impl/UserLogoutManagerImpl.kt`

```kotlin
class UserLogoutManagerImpl(
```
Default `UserLogoutManager`: clears every Store cache and draft row, then emits the logout event. Cache-clearing is the load-bearing half — a Store that survives logout would serve the previous user's data to the next one.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:60</code></summary>

```kotlin
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(), get(), get()) }
}

/**
 * Per-target repository bindings supplied by each `actual` — the pieces that cannot be expressed in
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/util/SharedFlowExtensions.kt`

```kotlin
fun <T> bufferedMutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = MutableSharedFlow(
```
Creates a `MutableSharedFlow` with a buffer of `Int.MAX_VALUE` and the given `replay` count.

---

_85 type(s), 208 function(s)/property(ies); 204 carry KDoc at source; 2 authored example(s); 81 live call site(s)._
<!-- api-docs:end -->
