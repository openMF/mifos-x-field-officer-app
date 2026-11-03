# `core/model`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_MODEL.md`
> **Measured:** 465 Kotlin files, 5 test files

## Principal types

`Account`, `AccountChart`, `AccountLinkingOptions`, `AccountOption`, `AccountOptions`, `AccountTransferChanges`, `AccountTransferRequest`, `AccountTransferResponse`, `AccountTransferTemplate`, `AccountTypeOption`, `AccountingRule`, `ActivatePayload`, `ActualDisbursementDate`, `Address`  …and 393 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/model sha=fb87866924dd2ff1cd6e650267ff442d8e7e6c14 -->
## API reference

_Generated from `core/model` at tree `fb87866924dd` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/model/src/commonMain/kotlin/kpt/core/model/auth/ChangePasswordRequest.kt`

```kotlin
data class ChangePasswordRequest(
```
`POST users/{userId}/pwd` — the signed-in user changes their own password. The server checks the policy AND that the two match (measured on the gateway 2026-10-27), so the screen's own match check only saves a round trip.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/auth/impl/AuthCommandRepositoryImpl.kt:93</code></summary>

```kotlin
        val changed = gateway.command(
            CommandSpec(
                payload = ChangePasswordRequest(password = newPassword, repeatPassword = repeatPassword),
                endpoint = { authApi.changePassword(userId = user.userId, request = it) },
            ),
            policy = MutationPolicy.OnlineRequired,
        )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/auth/PostAuthenticationRequest.kt`

```kotlin
data class PostAuthenticationRequest(
```
PostAuthenticationRequest

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInViewModel.kt:135</code></summary>

```kotlin
        mutableStateFlow.value = state.copy(isSubmitting = true, error = null)
        viewModelScope.launch {
            when (val result = auth.authenticate(PostAuthenticationRequest(username = username, password = password))) {
                is MutationResult.Applied -> onAuthenticated(result.value)
                is MutationResult.Blocked ->
                    fail(if (result.reason == BlockReason.OFFLINE) SignInError.Offline else SignInError.Rejected)
                is MutationResult.Failed -> {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/auth/PostAuthenticationResponse.kt`

```kotlin
data class PostAuthenticationResponse(
```
PostAuthenticationResponse

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInViewModel.kt:204</code></summary>

```kotlin
     * a successful CALL is not a successful LOGIN — the flag has to be checked explicitly.
     */
    private fun onAuthenticated(response: PostAuthenticationResponse) {
        if (response.authenticated != true) {
            fail(SignInError.Rejected)
            return
        }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/center/Center.kt`

```kotlin
data class Center(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `CenterEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:179</code></summary>

```kotlin
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(KptTheme.spacing.md))
        // The original's "App Version" row opened the system app-info page; kept, as a chip.
        Surface(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/center/CenterAccounts.kt`

```kotlin
data class CenterAccounts(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `CenterAccounts`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/center/CenterDate.kt`

```kotlin
data class CenterDate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `CenterDateEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/converter/CenterTypeConverters.kt:23</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromCenterDate(centerDate: CenterDateEntity?): String? {
        return centerDate?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/center/CenterPayload.kt`

```kotlin
data class CenterPayload(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `CenterPayloadEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/activate/src/commonTest/kotlin/kpt/feature/activate/ui/ActivateViewModelTest.kt:278</code></summary>

```kotlin
    ) = error("not used by these tests")

    override suspend fun createCenter(centerPayload: CenterPayload?) =
        error("not used by these tests")
}

private class FakeGroupCommands(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/center/CenterWithAssociations.kt`

```kotlin
data class CenterWithAssociations(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `CenterWithAssociations`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/center/PostCentersCenterIdRequest.kt`

```kotlin
data class PostCentersCenterIdRequest(
```
PostCentersCenterIdRequest

<details><summary>Used in the template — <code>feature/activate/src/commonTest/kotlin/kpt/feature/activate/ui/ActivateViewModelTest.kt:264</code></summary>

```kotlin
    override suspend fun activate2(
        centerId: Long,
        postCentersCenterIdRequest: PostCentersCenterIdRequest,
        command: String?,
    ) = error("not used by these tests")

    override suspend fun saveCollectionSheet(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/center/PostCentersCenterIdResponse.kt`

```kotlin
data class PostCentersCenterIdResponse(
```
PostCentersCenterIdResponse

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/center/CenterCommandRepository.kt:40</code></summary>

```kotlin
        postCentersCenterIdRequest: PostCentersCenterIdRequest,
        command: String? = null,
    ): MutationResult<PostCentersCenterIdResponse>

    suspend fun saveCollectionSheet(
        centerId: Int,
        collectionSheetPayload: CollectionSheetPayload?,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/charge/ChargeCalculationType.kt`

```kotlin
data class ChargeCalculationType(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ChargeCalculationTypeEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:61</code></summary>

```kotlin
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
        penalty = false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/charge/ChargeTimeType.kt`

```kotlin
data class ChargeTimeType(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ChargeTimeTypeEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:60</code></summary>

```kotlin
            name = name,
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/charge/Charges.kt`

```kotlin
data class Charges(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ChargesEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/ui/ChargeUi.kt:43</code></summary>

```kotlin
)

internal fun Charges.toUi(): ChargeUi {
    fun money(value: Double?) = value?.let { CurrencyFormatter.format(it, currency?.code, currency?.decimalPlaces) }
        ?: PLACEHOLDER
    return ChargeUi(
        id = id,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/charge/ClientChargeCurrency.kt`

```kotlin
data class ClientChargeCurrency(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientChargeCurrencyEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/charge/converter/ChargeTypeConverters.kt:43</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromClientChargeCurrency(currency: ClientChargeCurrencyEntity?): String? {
        return currency?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/AddressConfiguration.kt`

```kotlin
data class AddressConfiguration(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `AddressConfiguration`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientRepository.kt:106</code></summary>

```kotlin
     * validation made client creation impossible on such a tenant.
     */
    fun addressConfigurationStream(scope: CoroutineScope): ScreenDataStream<AddressConfiguration>

    fun addressTemplateStream(scope: CoroutineScope): ScreenDataStream<AddressTemplate>

    /** Where the officer has recorded this client — the pinpoint map/list. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/AddressTemplate.kt`

```kotlin
data class AddressTemplate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `AddressTemplate`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/client/Client.kt`

```kotlin
data class Client(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:210</code></summary>

```kotlin
        ActivateResult.Succeeded -> stringResource(
            when (state.type) {
                ActivateType.Client -> Res.string.feature_activate_client
                ActivateType.Center -> Res.string.feature_activate_center
                ActivateType.Group -> Res.string.feature_activate_group
            },
        )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientAccounts.kt`

```kotlin
data class ClientAccounts(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientAccounts`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientClassification.kt`

```kotlin
data class ClientClassification(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientClassificationEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/client/Client.kt:53</code></summary>

```kotlin
    val gender: ClientGender? = null,
    val clientType: ClientType? = null,
    val clientClassification: ClientClassification? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientCloseTemplateResponse.kt`

```kotlin
data class ClientCloseTemplateResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/closure/ClientClosureViewModelTest.kt:52</code></summary>

```kotlin

    private val closeTemplate =
        MutableStateFlow<ScreenState<ClientCloseTemplateResponse>>(ScreenState.Loading)

    private val repository = FakeClientRepository(closeTemplate = closeTemplate)

    private fun viewModel(
```

</details>

```kotlin
data class Narration(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/closure/ClientClosureScreen.kt:123</code></summary>

```kotlin
internal fun ClientClosureContent(
    state: ClientClosureState,
    reasons: ScreenState<List<Narration>>,
    onAction: (ClientClosureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContent(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientDate.kt`

```kotlin
data class ClientDate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientDateEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientTypeConverters.kt:46</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromClientDate(date: ClientDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientGender.kt`

```kotlin
data class ClientGender(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientGenderEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/editdetails/ClientEditDetailsFormTest.kt:186</code></summary>

```kotlin
            // A code value Fineract never sent: the entity's own default is id 0, which is not a
            // code value, and the original turned it into `genderId = 0` on the next save.
            gender = ClientGender(id = 0, name = null),
            legalForm = ClientStatus(id = 1, value = "PERSON"),
            dateOfBirth = listOf(1815, 12, 10),
            timeline = Timeline(submittedOnDate = listOf(2026, 1, 9)),
        ).asPrefill()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientPayload.kt`

```kotlin
data class ClientPayload(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientPayloadEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientViewModel.kt:215</code></summary>

```kotlin
        ownerId = null,
        ClientFormSnapshot.serializer(),
        ClientPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = draft.offered
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientStatus.kt`

```kotlin
data class ClientStatus(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientStatusEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/applynew/ClientApplyNewViewModel.kt:103</code></summary>

```kotlin
 */
internal fun Client.canApplyForNewAccount(): Boolean =
    status?.value == ClientStatus.STATUS_ACTIVE

internal sealed interface ClientApplyNewEvent {
    data object NavigateBack : ClientApplyNewEvent
    data class OpenApplication(val item: ApplyNewItem, val clientId: Int) : ClientApplyNewEvent
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientTemplateOptions.kt`

```kotlin
data class ClientTemplateOptions(
```
The option sets Fineract offers for ONE client — the domain projection of `GET {resourceType}/{clientId}/template`.

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/defaultaccount/ClientDefaultAccountViewModelTest.kt:56</code></summary>

```kotlin
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val template = MutableStateFlow<ScreenState<ClientTemplateOptions>>(ScreenState.Loading)
    private val client = MutableStateFlow<ScreenState<Client>>(ScreenState.Loading)

    private val repository = FakeClientRepository(template = template, client = client)
```

</details>

```kotlin
data class SavingAccountOption(
```
A savings account offered as a choice on a client form. Declared here rather than reused from `core/network/.../savings/dto` for the same reason as `StaffOption`: it is read by a screen, so it belongs on the domain side of the seam.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientType.kt`

```kotlin
data class ClientType(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientTypeEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/client/Client.kt:52</code></summary>

```kotlin
    val legalForm: ClientStatus? = null,
    val gender: ClientGender? = null,
    val clientType: ClientType? = null,
    val clientClassification: ClientClassification? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientsAttendance.kt`

```kotlin
data class ClientsAttendance(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/CollectionSheetPayload.kt:30</code></summary>

```kotlin
    var calendarId: Int? = 0,

    var clientsAttendance: MutableList<ClientsAttendance> = ArrayList(),

    var dateFormat: String = ApiDateFormatter.DATE_FORMAT,

    var locale: String = ApiDateFormatter.LOCALE,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientsTemplate.kt`

```kotlin
data class ClientsTemplate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientsTemplateEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/editdetails/ClientEditDetailsViewModel.kt:492</code></summary>

```kotlin

/** The tenant's option sets, with unusable rows dropped rather than offered. */
internal fun ClientsTemplate.asOptions(): ClientEditDetailsOptions = ClientEditDetailsOptions(
    genders = genderOptions.orEmpty().mapNotNull { o ->
        o.id.takeIf { it > 0 }?.let { EditOption(it, o.name) }
    },
    // Sorted by id, which is the original's order for this one list and happens to be the
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/InterestTypeOption.kt`

```kotlin
data class InterestTypeOption(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `InterestTypeEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/client/ClientsTemplate.kt:29</code></summary>

```kotlin
    val clientTypeOptions: List<Options>? = emptyList(),
    val clientClassificationOptions: List<Options>? = emptyList(),
    val clientLegalFormOptions: List<InterestTypeOption>? = emptyList(),
    val dataTables: List<DataTable>? = emptyList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/OfficeOptions.kt`

```kotlin
data class OfficeOptions(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `OfficeOptionsEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/transfer/LoanTransferViewModel.kt:208</code></summary>

```kotlin
                    // Sorted as the original sorted them, and every option with a null id is
                    // dropped: it cannot be sent, so it must not be selectable.
                    offices = template.toOfficeOptions.filter { it.id != null }.sortedBy { it.name },
                    clients = template.toClientOptions.filter { it.id != null }
                        .sortedBy { it.displayName },
                    accountTypes = template.toAccountTypeOptions.filter { it.id != null }
                        .sortedBy { it.value },
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/Options.kt`

```kotlin
data class Options(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `OptionsEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/navigation/ActivateRoute.kt:33</code></summary>

```kotlin
data class ActivateRoute(val id: Int, val type: String)

fun NavController.navigateToActivateClient(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ActivateRoute(clientId, Constants.ACTIVATE_CLIENT), navOptions)

fun NavController.navigateToActivateCenter(centerId: Int, navOptions: NavOptions? = null) =
    navigate(ActivateRoute(centerId, Constants.ACTIVATE_CENTER), navOptions)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/PostClientAddressRequest.kt`

```kotlin
data class PostClientAddressRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addaddress/ClientAddAddressViewModel.kt:255</code></summary>

```kotlin
        postalCode.isNotBlank()

internal fun ClientAddAddressState.toPayload(): PostClientAddressRequest = PostClientAddressRequest(
    addressLine1 = addressLine1.trim(),
    addressLine2 = addressLine2.trim(),
    addressLine3 = addressLine3.trim(),
    city = city.trim(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/PostClientAddressResponse.kt`

```kotlin
data class PostClientAddressResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt:94</code></summary>

```kotlin
        addressTypeId: Int,
        addressPayload: PostClientAddressRequest,
    ): MutationResult<PostClientAddressResponse>

    suspend fun assignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit>

    suspend fun unassignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/PostClientsClientIdRequest.kt`

```kotlin
data class PostClientsClientIdRequest(
```
PostClientsClientIdRequest

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt:59</code></summary>

```kotlin
    suspend fun activate1(
        clientId: Long,
        postClientsClientIdRequest: PostClientsClientIdRequest,
        command: String? = null,
    ): MutationResult<PostClientsClientIdResponse>

    /** The photo as bytes + name + MIME type; the multipart encoding is core/data's job, not the caller's. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/PostClientsClientIdResponse.kt`

```kotlin
data class PostClientsClientIdResponse(
```
PostClientsClientIdResponse

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt:61</code></summary>

```kotlin
        postClientsClientIdRequest: PostClientsClientIdRequest,
        command: String? = null,
    ): MutationResult<PostClientsClientIdResponse>

    /** The photo as bytes + name + MIME type; the multipart encoding is core/data's job, not the caller's. */
    suspend fun uploadClientImage(clientId: Int, image: DocumentUpload): MutationResult<Unit>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/SavingProductOptions.kt`

```kotlin
data class SavingProductOptions(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingProductOptionsEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:131</code></summary>

```kotlin
        clientsTemplate.officeOptions?.let { insertOfficeOptions(it) }
        clientsTemplate.staffOptions?.let { insertStaffOptions(it) }
        clientsTemplate.savingProductOptions?.let { insertSavingProductOptions(it) }
        clientsTemplate.genderOptions?.forEach { insertOption(it.copy(optionType = GENDER_OPTIONS)) }
        clientsTemplate.clientTypeOptions?.forEach { insertOption(it.copy(optionType = CLIENT_TYPE_OPTIONS)) }
        clientsTemplate.clientClassificationOptions?.forEach {
            insertOption(it.copy(optionType = CLIENT_CLASSIFICATION_OPTIONS))
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/Savings.kt`

```kotlin
data class Savings(
```
Created by nellyk on 2/19/2016.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:52</code></summary>

```kotlin
    listOf(
        MeetingToday(centerId = 10, name = "Centro Arin", groups = 3, time = "09:30"),
        MeetingToday(centerId = 18, name = "Arin Savings", groups = 2, time = "14:00"),
    ),
)

private val demoPortfolio = HomeCard.Portfolio(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/StaffOption.kt`

```kotlin
data class StaffOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/staff/ClientStaffScreen.kt:116</code></summary>

```kotlin
internal fun ClientStaffContent(
    state: ClientStaffState,
    options: ScreenState<List<StaffOption>>,
    onAction: (ClientStaffAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContent(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/StaffOptions.kt`

```kotlin
data class StaffOptions(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `StaffOptionsEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:130</code></summary>

```kotlin
        insertClientsTemplate(clientsTemplate)
        clientsTemplate.officeOptions?.let { insertOfficeOptions(it) }
        clientsTemplate.staffOptions?.let { insertStaffOptions(it) }
        clientsTemplate.savingProductOptions?.let { insertSavingProductOptions(it) }
        clientsTemplate.genderOptions?.forEach { insertOption(it.copy(optionType = GENDER_OPTIONS)) }
        clientsTemplate.clientTypeOptions?.forEach { insertOption(it.copy(optionType = CLIENT_TYPE_OPTIONS)) }
        clientsTemplate.clientClassificationOptions?.forEach {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/CenterDetail.kt`

```kotlin
data class CenterDetail(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `CenterDetail`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/ClientCollectionSheet.kt`

```kotlin
data class ClientCollectionSheet(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientCollectionSheet`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/CollectionSheetPayload.kt`

```kotlin
data class CollectionSheetPayload(
```
Created by Tarun on 31-07-17.

<details><summary>Used in the template — <code>feature/activate/src/commonTest/kotlin/kpt/feature/activate/ui/ActivateViewModelTest.kt:270</code></summary>

```kotlin
    override suspend fun saveCollectionSheet(
        centerId: Int,
        collectionSheetPayload: CollectionSheetPayload?,
    ) = error("not used by these tests")

    override suspend fun saveCollectionSheetAsync(
        centerId: Int,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/CollectionSheetResponse.kt`

```kotlin
data class CollectionSheetResponse(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `CollectionSheetResponse`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/CollectionSheetRepository.kt:52</code></summary>

```kotlin
    suspend fun individualCollectionSheet(request: RequestCollectionSheetPayload): IndividualCollectionSheet?

    suspend fun productiveSheet(centerId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?

    suspend fun groupCollectionSheet(groupId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/GroupCollectionSheet.kt`

```kotlin
data class GroupCollectionSheet(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupCollectionSheet`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/CollectionSheetResponse.kt:26</code></summary>

```kotlin
    var dueDate: IntArray? = null,

    var groups: List<GroupCollectionSheet> = ArrayList(),

    var loanProducts: List<kpt.core.model.objects.organisations.LoanProducts> = ArrayList(),

    var paymentTypeOptions: List<PaymentTypeOptionEntity> = ArrayList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/IndividualCollectionSheet.kt`

```kotlin
data class IndividualCollectionSheet(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `IndividualCollectionSheet`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/IndividualCollectionSheetPayload.kt`

```kotlin
data class IndividualCollectionSheetPayload(
```
Created by Tarun on 11-07-2017.

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/details/CollectionSheetDetailsViewModel.kt:137</code></summary>

```kotlin
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true)
        val payload = IndividualCollectionSheetPayload(
            bulkRepaymentTransactions = ArrayList(view.rows.map { it.collect }),
            dateFormat = ApiDateFormatter.DATE_FORMAT,
            locale = ApiDateFormatter.LOCALE,
            transactionDate = request.transactionDate,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/MeetingFallCalendar.kt`

```kotlin
data class MeetingFallCalendar(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `MeetingFallCalendar`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/converter/CollectionSheetTypeConverters.kt:21</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromMeetingFallCalendarList(meetings: List<MeetingFallCalendar>?): String =
        Json.encodeToString(meetings ?: emptyList())

    @ColumnTypeConverter
    fun toMeetingFallCalendarList(json: String): List<MeetingFallCalendar> =
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/ProductiveCollectionSheetPayload.kt`

```kotlin
data class ProductiveCollectionSheetPayload(
```
Created by Tarun on 25-07-2017.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/CollectionSheetCommandRepository.kt:46</code></summary>

```kotlin
    suspend fun submitProductiveSheet(
        centerId: Int,
        payload: ProductiveCollectionSheetPayload?,
    ): MutationResult<GenericResponse>

    suspend fun submitCollectionSheet(groupId: Int, payload: CollectionSheetPayload?): MutationResult<GenericResponse>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/RequestCollectionSheetPayload.kt`

```kotlin
data class RequestCollectionSheetPayload(
```
Created by Tarun on 06-07-2017.

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/details/CollectionSheetDetailsViewModel.kt:71</code></summary>

```kotlin
    // Keys are CollectionSheetDetailsRoute's property names; read directly (not toRoute) so the
    // Android host tests, whose Bundle is a stub, see the same arguments as the device.
    private val request = RequestCollectionSheetPayload(
        dateFormat = ApiDateFormatter.DATE_FORMAT,
        locale = ApiDateFormatter.LOCALE,
        officeId = requireNotNull(savedStateHandle.get<Int>("officeId")),
        staffId = savedStateHandle.get<Int>("staffId"),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/SavingsCollectionSheet.kt`

```kotlin
data class SavingsCollectionSheet(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsCollectionSheet`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/ClientCollectionSheet.kt:27</code></summary>

```kotlin
    var attendanceType: AttendanceTypeOption? = null,

    var savings: ArrayList<SavingsCollectionSheet> = ArrayList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/payload/CollectionSheetPayload.kt`

```kotlin
class CollectionSheetPayload : Payload()
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/activate/src/commonTest/kotlin/kpt/feature/activate/ui/ActivateViewModelTest.kt:270</code></summary>

```kotlin
    override suspend fun saveCollectionSheet(
        centerId: Int,
        collectionSheetPayload: CollectionSheetPayload?,
    ) = error("not used by these tests")

    override suspend fun saveCollectionSheetAsync(
        centerId: Int,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/config/AppOrgLinks.kt`

```kotlin
object AppOrgLinks
```
The fork's own named organisation links — a community chat, a product site, a status page — for whatever surface the fork builds (an About screen, a help sheet). **SoT: `app-profile/app.yaml#org.links`** (`name: url`).

<details><summary>Example</summary>

```kotlin
AppOrgLinks["community"]?.let { url -> LinkRow("Community", url) }
```

</details>

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutViewModel.kt:70</code></summary>

```kotlin
            websiteUrl = BuildKonfig.WEBSITE_URL,
            sourceUrl = BuildKonfig.SOURCE_URL,
            communityUrl = AppOrgLinks["community"].orEmpty(),
            productUrl = AppOrgLinks["product"].orEmpty(),
            licenseUrl = BuildKonfig.LICENSE_URL,
        )
    }
```

</details>

- `val all: Map<String, String> get() = links` — Every declared link, in app-profile order.

### `core/model/src/commonMain/kotlin/kpt/core/model/config/AppPrivacyConfig.kt`

```kotlin
object AppPrivacyConfig
```
Consent defaults a fresh install starts with — analytics and crash reporting. **SoT: `app-profile/app.yaml#privacy`.** The block below is GENERATED by `./gradlew syncForkConfig`; change the YAML, not this file.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt:63</code></summary>

```kotlin
     * declares otherwise — rather than failing to load.
     */
    val analyticsConsent: Boolean = AppPrivacyConfig.ANALYTICS_ENABLED_BY_DEFAULT,
    /** The user's consent to send crash reports. Defaulted for the same reason as [analyticsConsent]. */
    val crashReportingConsent: Boolean = AppPrivacyConfig.CRASH_REPORTING_ENABLED_BY_DEFAULT,
) {
    /** Factory holder. */
```

</details>

- `const val ANALYTICS_ENABLED_BY_DEFAULT: Boolean = false` — `privacy.analytics_enabled_by_default` — whether a fresh install shares usage analytics.
- `const val CRASH_REPORTING_ENABLED_BY_DEFAULT: Boolean = false` — `privacy.crash_reporting_enabled_by_default` — whether a fresh install sends crash reports.
- `const val PRIVACY_POLICY_URL: String =` — `org.privacy_url` — the privacy policy page, opened in the in-app browser; empty when not set.

### `core/model/src/commonMain/kotlin/kpt/core/model/config/AppSecurityConfig.kt`

```kotlin
object AppSecurityConfig
```
Security defaults a fresh install starts with. **SoT: `app-profile/app.yaml#security`.** The block below is GENERATED by `./gradlew syncForkConfig`; change the YAML, not this file.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt:81</code></summary>

```kotlin
            useDynamicColor = false,
            appLanguage = LanguageConfig.DEFAULT,
            isAuthenticated = AppSecurityConfig.SIGNED_IN_BY_DEFAULT,
            isUnlocked = true,
            isPasscodeEnabled = false,
            isBiometricsEnabled = false,
            showOnboarding = false,
```

</details>

- `const val SCREEN_CAPTURE_ALLOWED_BY_DEFAULT: Boolean = false` — `security.screen_capture_allowed_by_default` — whether a fresh install allows screenshots + recording.
- `const val SIGNED_IN_BY_DEFAULT: Boolean = false` — `security.signed_in_by_default` — whether a fresh install starts signed in (false = the app has a login).

### `core/model/src/commonMain/kotlin/kpt/core/model/dashboard/Dashboard.kt`

```kotlin
data class DashboardScope(val officeId: Long, val staffId: Long?, val username: String)
```
Whose book the dashboard shows. A user linked to a staff record sees their own portfolio; an unlinked user (the gateway demo user) sees their office, and "own" loans fall back to the ones they submitted.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:125</code></summary>

```kotlin
        val userStream = dashboardRepository.userStream(user.userId, viewModelScope).on(*HomeCardId.entries.toTypedArray(), key = "user")
        // The staff link scopes the book. Until it answers, wait; if it cannot be read, fall back to the office.
        val scope: Flow<DashboardScope> = userStream.state.mapNotNull { state ->
            when (state) {
                ScreenState.Loading -> null
                is ScreenState.Content -> DashboardScope(user.officeId, state.data.staffId, user.username.orEmpty())
                else -> DashboardScope(user.officeId, null, user.username.orEmpty())
```

</details>

```kotlin
data class DashboardUser(
```
The signed-in user's own Fineract record — the fields the dashboard and Profile read.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:153</code></summary>

```kotlin
    }

    private fun header(user: User, userStream: ScreenDataStream<DashboardUser>, businessDate: ScreenDataStream<String>) {
        combine(
            userStream.state,
            businessDate.state,
            serverConfigRepository.serverConfigStream(viewModelScope).state,
```

</details>

```kotlin
data class DashboardLoan(
```
One loan as the dashboard needs it: ownership, arrears and the summary sums.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:321</code></summary>

```kotlin
    }

    private fun collectionsFrom(rows: List<DueTodayLoan>, owned: List<DashboardLoan>): HomeCard {
        val dueRows = rows.filter { it.due > 0.0 }
        val dueClients = dueRows.mapNotNull { it.clientId }.toSet()
        val inArrears = owned.filter { it.inArrears }.mapNotNull { it.clientId }.toSet()
        fun money(list: List<DueTodayLoan>, amount: (DueTodayLoan) -> Double) =
```

</details>

```kotlin
data class DashboardSavings(
```
One savings account's balance, for the portfolio total.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/dashboard/DashboardRepository.kt:34</code></summary>

```kotlin
    fun clientCountStream(officeId: Long, status: String, scope: CoroutineScope): ScreenDataStream<Int>

    fun savingsStream(scope: CoroutineScope): ScreenDataStream<List<DashboardSavings>>

    /** Installments of [loanIds] due on [date] (`yyyy-MM-dd`) — Today's collections without the collection sheet. */
    fun dueTodayStream(date: String, loanIds: List<Long>, scope: CoroutineScope): ScreenDataStream<List<DueTodayLoan>>
```

</details>

```kotlin
data class CurrencyTotals(
```
Money sums for one currency — never mixed across currencies.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardModels.kt:67</code></summary>

```kotlin
        val activeLoans: Int?,
        val inArrears: Int?,
        val loanTotals: List<CurrencyTotals>?,
        val savings: List<Money>?,
        val groups: Int?,
        val centers: Int?,
    ) : HomeCard
```

</details>

```kotlin
data class LoanPortfolio(val activeLoans: Int, val inArrears: Int, val byCurrency: List<CurrencyTotals>)
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:294</code></summary>

```kotlin
        return combineScreenStates(parts).mapContent { values, _ ->
            val byKey = values.asKeyed()
            val loans = byKey[LOANS] as? LoanPortfolio
            HomeCard.Portfolio(
                activeClients = byKey[CLIENTS] as? Int,
                activeLoans = loans?.activeLoans,
                inArrears = loans?.inArrears,
```

</details>

```kotlin
data class DueTodayLoan(
```
A loan's installment due on the business date, read from its repayment schedule: `due` is what is still outstanding on it, `paid` what has already been paid against it.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:321</code></summary>

```kotlin
    }

    private fun collectionsFrom(rows: List<DueTodayLoan>, owned: List<DashboardLoan>): HomeCard {
        val dueRows = rows.filter { it.due > 0.0 }
        val dueClients = dueRows.mapNotNull { it.clientId }.toSet()
        val inArrears = owned.filter { it.inArrears }.mapNotNull { it.clientId }.toSet()
        fun money(list: List<DueTodayLoan>, amount: (DueTodayLoan) -> Double) =
```

</details>

```kotlin
fun List<DashboardLoan>.ownedBy(scope: DashboardScope): List<DashboardLoan> =
```
Loans that belong to `scope`: by loan officer when staff-linked, else by who submitted them.

```kotlin
fun portfolioOf(activeLoans: List<DashboardLoan>): LoanPortfolio = LoanPortfolio(
```
Computed from the active-loan list, not from a stretchy report: the Portfolio at Risk / Active Loans reports fail on some Fineract databases (BadSqlGrammar on the PostgreSQL sandbox).

```kotlin
fun User.can(code: String): Boolean =
```
_No KDoc at source._

```kotlin
fun User.canAnyChecker(): Boolean =
```
Any maker-checker approval right.

```kotlin
fun User.canRunReports(): Boolean =
```
Running reports needs READ_REPORT plus at least one report read (or the reporting super user).

### `core/model/src/commonMain/kotlin/kpt/core/model/datatable/ColumnHeader.kt`

```kotlin
data class ColumnHeader(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ColumnHeader`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/datatable/ColumnValue.kt`

```kotlin
data class ColumnValue(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ColumnValue`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/datatable/DataTable.kt`

```kotlin
data class DataTable(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `DataTableEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/ClientDataTableFields.kt:53</code></summary>

```kotlin
@Composable
internal fun DataTableSections(
    tables: List<DataTable>,
    state: CreateNewClientState,
    onAction: (CreateNewClientAction) -> Unit,
) {
    for (table in tables) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/datatable/DataTablePayload.kt`

```kotlin
data class DataTablePayload(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `DataTablePayload`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/datatable/DeleteDataTablesDatatableAppTableIdDatatableIdResponse.kt`

```kotlin
data class DeleteDataTablesDatatableAppTableIdDatatableIdResponse(
```
DeleteDataTablesDatatableAppTableIdDatatableIdResponse

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/datatable/DataTableCommandRepository.kt:35</code></summary>

```kotlin
        apptableId: Long,
        datatableId: Long,
    ): MutationResult<DeleteDataTablesDatatableAppTableIdDatatableIdResponse>

    suspend fun createEntryInDataTable(
        dataTableName: String,
        entityId: Int,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/draft/CreateForm.kt`

```kotlin
enum class CreateForm(val key: String)
```
Every create flow that keeps a draft (core-base `FormDraft`). `key` is its `form` / outbox `formKey`; all share `PREFIX` so the create outbox can claim exactly these rows.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/create/CreateCenterViewModel.kt:72</code></summary>

```kotlin
        drafts,
        viewModelScope,
        CreateForm.CENTER.key,
        ownerId = null,
        CreateCenterState.serializer(),
        CenterPayload.serializer(),
    )
```

</details>

```kotlin
data class AddressDraftPayload(
```
The address create's two path arguments travel with its body — the payload alone cannot be replayed.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addaddress/ClientAddAddressViewModel.kt:99</code></summary>

```kotlin
        ownerId = state.clientId.toLong(),
        ClientAddAddressState.serializer(),
        AddressDraftPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = draft.offered
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/group/Group.kt`

```kotlin
data class Group(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:212</code></summary>

```kotlin
                ActivateType.Client -> Res.string.feature_activate_client
                ActivateType.Center -> Res.string.feature_activate_center
                ActivateType.Group -> Res.string.feature_activate_group
            },
        )

        ActivateResult.Failed -> listOfNotNull(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/group/GroupAccounts.kt`

```kotlin
data class GroupAccounts(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupAccounts`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/testing/FakeCenterRepositories.kt:83</code></summary>

```kotlin
    /** Members / accounts — Loading until a test sets them. */
    val membersState = MutableStateFlow<ScreenState<GroupWithAssociations>>(ScreenState.Loading)
    val accountsState = MutableStateFlow<ScreenState<GroupAccounts>>(ScreenState.Loading)

    override fun groupWithMembersStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupWithAssociations> =
        screenDataStreamForTesting(membersState)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/group/GroupDate.kt`

```kotlin
data class GroupDate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupDateEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/group/converter/GroupTypeConverters.kt:27</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromGroupDate(date: GroupDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/group/GroupPayload.kt`

```kotlin
data class GroupPayload(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupPayloadEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/activate/src/commonTest/kotlin/kpt/feature/activate/ui/ActivateViewModelTest.kt:297</code></summary>

```kotlin
    }

    override suspend fun createGroup(groupPayload: GroupPayload?) =
        error("not used by these tests")
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/group/GroupWithAssociations.kt`

```kotlin
data class GroupWithAssociations(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupWithAssociations`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/testing/FakeCenterRepositories.kt:82</code></summary>

```kotlin

    /** Members / accounts — Loading until a test sets them. */
    val membersState = MutableStateFlow<ScreenState<GroupWithAssociations>>(ScreenState.Loading)
    val accountsState = MutableStateFlow<ScreenState<GroupAccounts>>(ScreenState.Loading)

    override fun groupWithMembersStream(groupId: Int, scope: CoroutineScope): ScreenDataStream<GroupWithAssociations> =
        screenDataStreamForTesting(membersState)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/ActualDisbursementDate.kt`

```kotlin
data class ActualDisbursementDate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ActualDisbursementDateEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanScheduleTypeConverters.kt:39</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromActualDisbursementDate(date: ActualDisbursementDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/AmortizationType.kt`

```kotlin
class AmortizationType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:89</code></summary>

```kotlin
        termPeriodFrequencyType = TermPeriodFrequencyType(id = 2, value = "Months"),
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/AssignLoanOfficerRequestDto.kt`

```kotlin
data class AssignLoanOfficerRequestDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:90</code></summary>

```kotlin
    suspend fun assignLoanOfficer(
        loanId: Int,
        request: AssignLoanOfficerRequestDto,
    ): MutationResult<AssignLoanOfficerResponseDto>

    /**
     * Submit a new INDIVIDUAL loan application.
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/AssignLoanOfficerResponseDto.kt`

```kotlin
data class AssignLoanOfficerResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:91</code></summary>

```kotlin
        loanId: Int,
        request: AssignLoanOfficerRequestDto,
    ): MutationResult<AssignLoanOfficerResponseDto>

    /**
     * Submit a new INDIVIDUAL loan application.
     *
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/CreateGuarantorResponseDto.kt`

```kotlin
data class CreateGuarantorResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:112</code></summary>

```kotlin
    suspend fun calculateLoanSchedule(input: LoanApplicationInput): MutationResult<RepaymentSchedule>

    suspend fun createGuarantor(loanId: Int, request: GuarantorRequestDto): MutationResult<CreateGuarantorResponseDto>

    /**
     * Move money between two accounts.
     *
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/GuarantorRequestDto.kt`

```kotlin
data class GuarantorRequestDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:112</code></summary>

```kotlin
    suspend fun calculateLoanSchedule(input: LoanApplicationInput): MutationResult<RepaymentSchedule>

    suspend fun createGuarantor(loanId: Int, request: GuarantorRequestDto): MutationResult<CreateGuarantorResponseDto>

    /**
     * Move money between two accounts.
     *
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/Loan.kt`

```kotlin
data class Loan(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `Loan`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanAccount.kt`

```kotlin
data class LoanAccount(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanAccountEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/general/ClientGeneralScreen.kt:96</code></summary>

```kotlin
    val actionHandlers = mapOf<ClientGeneralActionItem, ((Int) -> Unit)?>(
        ClientGeneralActionItem.UpcomingCharges to onUpcomingCharges,
        ClientGeneralActionItem.LoanAccount to onLoanAccounts,
        ClientGeneralActionItem.SavingAccounts to onSavingAccounts,
        ClientGeneralActionItem.FixedDepositAccounts to onFixedDepositAccounts,
        ClientGeneralActionItem.RecurringDepositAccounts to onRecurringDepositAccounts,
        ClientGeneralActionItem.SharesAccounts to onSharesAccounts,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanAccountSummary.kt`

```kotlin
data class LoanAccountSummary(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanAccountSummaryEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/profile/LoanProfileScreenPreview.kt:37</code></summary>

```kotlin
    principal = 25_000.0,
    currency = SavingAccountCurrency(code = "BDT", name = "Bangladeshi Taka", decimalPlaces = 2, displaySymbol = "৳"),
    summary = LoanAccountSummary(totalOutstanding = 26_950.0, totalOverdue = 1_200.0),
)

@Preview
@Composable
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanAccountType.kt`

```kotlin
data class LoanAccountType(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanTypeEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/loan/Loan.kt:39</code></summary>

```kotlin
    val loanOfficerId: Int? = null,
    val loanOfficerName: String? = null,
    val loanType: LoanAccountType? = null,
    val currency: Currency? = null,
    val principal: Double? = null,
    val approvedPrincipal: Double? = null,
    val termFrequency: Int? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanApprovalData.kt`

```kotlin
data class LoanApprovalData(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanChargeOffRequestDto.kt`

```kotlin
data class LoanChargeOffRequestDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:86</code></summary>

```kotlin
    ): MutationResult<LoanRepaymentResponse>

    suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>

    suspend fun assignLoanOfficer(
        loanId: Int,
        request: AssignLoanOfficerRequestDto,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanChargeOffResponseDto.kt`

```kotlin
data class LoanChargeOffResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:86</code></summary>

```kotlin
    ): MutationResult<LoanRepaymentResponse>

    suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>

    suspend fun assignLoanOfficer(
        loanId: Int,
        request: AssignLoanOfficerRequestDto,
```

</details>

```kotlin
data class LoanChargeOffChanges(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanRepaymentRequest.kt`

```kotlin
data class LoanRepaymentRequest(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanRepaymentRequestEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/repayment/LoanRepaymentViewModel.kt:208</code></summary>

```kotlin
            }

            val request = LoanRepaymentRequest(
                loanId = state.loanId,
                // From the eagerly-shared projection of the loan — see [accountNumber]. Keeping a
                // copy in the editable state would be a second source that could go stale against
                // the loan the header is showing.
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanRepaymentResponse.kt`

```kotlin
data class LoanRepaymentResponse(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanRepaymentResponseEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:84</code></summary>

```kotlin
        loanId: Int,
        request: LoanRepaymentRequest,
    ): MutationResult<LoanRepaymentResponse>

    suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>

    suspend fun assignLoanOfficer(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanRepaymentTemplate.kt`

```kotlin
data class LoanRepaymentTemplate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanRepaymentTemplateEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/repayment/LoanRepaymentViewModelTest.kt:69</code></summary>

```kotlin
    )

    private val templateState = MutableStateFlow<ScreenState<LoanRepaymentTemplate>>(
        ScreenState.Content(
            LoanRepaymentTemplate(
                loanId = 7,
                amount = 1_000.0,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanStatus.kt`

```kotlin
data class LoanStatus(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanStatusEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/loanaccounts/ClientLoanAccountsViewModel.kt:192</code></summary>

```kotlin
    ;

    fun matches(status: LoanStatus?): Boolean {
        status ?: return false
        return when (this) {
            ACTIVE -> status.active == true
            PENDING -> status.pendingApproval == true
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanTemplate.kt`

```kotlin
data class LoanTemplate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanTemplate`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationViewModel.kt:231</code></summary>

```kotlin

        viewModelScope.launch {
            val template = repository.groupLoanTemplate(state.groupId, productId)

            mutableStateFlow.value = if (template == null) {
                state.copy(isLoadingTemplate = false, templateError = true)
            } else {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanTimeline.kt`

```kotlin
data class LoanTimeline(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanTimelineEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/approval/LoanApprovalViewModelTest.kt:233</code></summary>

```kotlin
            LoanWithAssociations(
                id = 7,
                timeline = LoanTimeline(expectedDisbursementDate = listOf(2026)),
            ),
        )

        vm.prefill.test {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanTransactionTemplate.kt`

```kotlin
data class LoanTransactionTemplate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanTransactionTemplate`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanType.kt`

```kotlin
data class LoanType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/converter/ListTypeConverters.kt:96</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromType(type: LoanType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/RejectLoanRequestDto.kt`

```kotlin
data class RejectLoanRequestDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:69</code></summary>

```kotlin
    suspend fun approveLoanApplication(loanId: Int, approval: LoanApproval): MutationResult<GenericResponse>

    suspend fun rejectLoan(loanId: Int, request: RejectLoanRequestDto): MutationResult<RejectLoanResponseDto>

    /**
     * Disburse an approved loan.
     *
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/RejectLoanResponseDto.kt`

```kotlin
data class RejectLoanResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:69</code></summary>

```kotlin
    suspend fun approveLoanApplication(loanId: Int, approval: LoanApproval): MutationResult<GenericResponse>

    suspend fun rejectLoan(loanId: Int, request: RejectLoanRequestDto): MutationResult<RejectLoanResponseDto>

    /**
     * Disburse an approved loan.
     *
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/note/NoteDraft.kt`

```kotlin
data class NoteDraft(
```
A note the officer has written, as a DURABLE payload. ## Why this type exists `NoteCommandRepository` takes four loose parameters (`resourceType`, `resourceId`, `noteId`, `NoteRequestDto`).

<details><summary>Used in the template — <code>feature/note/src/commonMain/kotlin/kpt/feature/note/ui/AddEditNoteViewModel.kt:65</code></summary>

```kotlin
    private val repository: NoteRepository,
    private val commands: NoteCommandRepository,
    outbox: SubmitOutbox<NoteDraft>,
    savedStateHandle: SavedStateHandle,
) : BaseMutationViewModel<NoteDraft, Unit>(
    MutationMode.Draft(
        outbox = outbox,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/notification/AppNotification.kt`

```kotlin
data class AppNotification(
```
One Fineract notification. `objectType` (`loan`, `client`, `group`, `center`, `savingsAccount` …) and `objectId` name the record it is about; `createdAt` is the server's ISO local date-time.

<details><summary>Used in the template — <code>feature/notification/src/commonMain/kotlin/kpt/feature/notification/navigation/NotificationsRoute.kt:44</code></summary>

```kotlin

/** Opens the record a notification is about; the screen only offers the tap for these kinds. */
private fun NavController.openRecordOf(notification: AppNotification) {
    val id = notification.objectId?.toInt() ?: return
    when (notification.target) {
        NotificationTarget.LOAN -> navigateToLoanProfile(id)
        NotificationTarget.CLIENT -> navigateToClientProfile(id)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/Changes.kt`

```kotlin
data class Changes(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/profile/src/commonTest/kotlin/kpt/feature/profile/identity/ProfileViewModelTest.kt:197</code></summary>

```kotlin

        vm.trySendAction(ProfileAction.SubmitPassword)
        assertTrue(auth.passwordChanges.isEmpty(), "a mismatched pair is never sent")

        vm.trySendAction(ProfileAction.RepeatPassword("Field#Office2026"))
        assertTrue(vm.profile.value.password!!.canSave)
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/ErrorSyncServerMessage.kt`

```kotlin
class ErrorSyncServerMessage
```
Created by Rajan Maurya on 23/07/16.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/LanguageConfig.kt`

```kotlin
enum class LanguageConfig(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:62</code></summary>

```kotlin
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onChangeLanguage: (language: LanguageConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/SearchedEntity.kt`

```kotlin
data class SearchedEntity(
```
Created by ishankhanna on 14/02/14.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/search/SearchBody.kt:74</code></summary>

```kotlin
     * types have one. See [SearchResultRow].
     */
    onResultClick: ((SearchedEntity) -> Unit)? = null,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val results by viewModel.results.collectAsStateWithLifecycle()
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/AccountLinkingOptions.kt`

```kotlin
data class AccountLinkingOptions(
```
Created by Tarun on 12/16/2016.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:132</code></summary>

```kotlin

    val maximumGap: Int? = null,
    val accountLinkingOptions: List<AccountLinkingOptions> = emptyList(),

    val loanScheduleType: TermFrequencyTypeOptions? = null,

    val overdueCharges: List<ChargesEntity> = emptyList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/AmortizationType.kt`

```kotlin
class AmortizationType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:89</code></summary>

```kotlin
        termPeriodFrequencyType = TermPeriodFrequencyType(id = 2, value = "Months"),
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/ChargeOffReasonOption.kt`

```kotlin
data class ChargeOffReasonOption(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/Currency.kt`

```kotlin
data class Currency(
```
Created by mayankjindal on 22/10/16.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:299</code></summary>

```kotlin
                activeLoans = loans?.activeLoans,
                inArrears = loans?.inArrears,
                loanTotals = loans?.byCurrency,
                savings = (byKey[SAVINGS] as? List<*>)?.filterIsInstance<Money>(),
                groups = byKey[GROUPS] as? Int,
                centers = byKey[CENTERS] as? Int,
            )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/DaysInMonthType.kt`

```kotlin
data class DaysInMonthType(
```
Created by nellyk on 2/21/2016.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:122</code></summary>

```kotlin
    val canDisburse: Boolean? = null,
    val product: Product? = null,
    val daysInMonthType: DaysInMonthType? = null,
    val daysInYearType: DaysInYearType? = null,

    val isInterestRecalculationEnabled: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/DaysInYearType.kt`

```kotlin
data class DaysInYearType(
```
Created by nellyk on 2/21/2016.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:123</code></summary>

```kotlin
    val product: Product? = null,
    val daysInMonthType: DaysInMonthType? = null,
    val daysInYearType: DaysInYearType? = null,

    val isInterestRecalculationEnabled: Boolean? = null,

    val isvaliableInstallmentsAllowed: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/InterestCalculationPeriodType.kt`

```kotlin
data class InterestCalculationPeriodType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationScreenUiTest.kt:45</code></summary>

```kotlin
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        interestCalculationPeriodTypeOptions = listOf(InterestCalculationPeriodType(id = 1, value = "Daily")),
        transactionProcessingStrategyOptions = listOf(
            TransactionProcessingStrategyOptions(id = 4, code = "mifos-standard-strategy", name = "Standard"),
        ),
        fundOptions = listOf(FundOptions(id = 9, name = "Donor fund")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/InterestRateFrequencyType.kt`

```kotlin
data class InterestRateFrequencyType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanTypeConverters.kt:80</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestRateFrequencyType(type: InterestRateFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/InterestType.kt`

```kotlin
class InterestType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:90</code></summary>

```kotlin
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/LoanApplicationInput.kt`

```kotlin
data class LoanApplicationInput(
```
A new INDIVIDUAL loan application, as the new-application wizard assembles it.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModel.kt:122</code></summary>

```kotlin
        ownerId = state.clientId.toLong(),
        LoanFormSnapshot.serializer(),
        LoanApplicationInput.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = draft.offered
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/LoanApproval.kt`

```kotlin
data class LoanApproval(
```
What the officer is asking Fineract to approve.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/approval/LoanApprovalViewModel.kt:158</code></summary>

```kotlin

        viewModelScope.launch {
            val approval = LoanApproval(
                // Every one of these three comes from `ApiDateFormatter`, which is the point:
                // `locale` and `dateFormat` are REQUIRED parameters now, so the format cannot be
                // left at a default that disagrees with however the date was produced. See
                // `LoanApproval`'s own header for the four times that happened.
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/LoanApprovalRequest.kt`

```kotlin
data class LoanApprovalRequest(
```
Created by ishankhanna on 23/06/14.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/LoanChargeOffInput.kt`

```kotlin
data class LoanChargeOffInput(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/LoanPurposeOptions.kt`

```kotlin
data class LoanPurposeOptions(
```
Created by nellyk on 2/25/2016.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationScreenUiTest.kt:41</code></summary>

```kotlin
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        loanPurposeOptions = listOf(LoanPurposeOptions(id = 5, name = "Working capital")),
    )

    private fun filled() = GroupLoanApplicationState(
        groupId = 31,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/LoanRepaymentResponse.kt`

```kotlin
data class LoanRepaymentResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:84</code></summary>

```kotlin
        loanId: Int,
        request: LoanRepaymentRequest,
    ): MutationResult<LoanRepaymentResponse>

    suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>

    suspend fun assignLoanOfficer(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/PaymentDetailData.kt`

```kotlin
data class PaymentDetailData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/Transaction.kt:22</code></summary>

```kotlin
    var date: List<Int> = ArrayList(),
    var currency: Currency? = null,
    var paymentDetailData: PaymentDetailData? = null,
    var amount: Double? = null,
    var netDisbursalAmount: Double? = null,
    var principalPortion: Double? = null,
    var interestPortion: Double? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/PaymentType.kt`

```kotlin
data class PaymentType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/disbursement/LoanDisbursementViewModelTest.kt:57</code></summary>

```kotlin
                currency = Currency(code = "INR", decimalPlaces = 2),
                paymentTypeOptions = listOf(
                    PaymentType(id = 1, name = "Cash"),
                    PaymentType(id = 4, name = "Mobile Money"),
                ),
            ),
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/PaymentTypeOptions.kt`

```kotlin
data class PaymentTypeOptions(
```
Created by nellyk on 3/3/2016.

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/CollectionSheetPreviews.kt:56</code></summary>

```kotlin
    ),
    paymentTypeOptions = arrayListOf(
        PaymentTypeOptions(id = 1, name = "Cash"),
        PaymentTypeOptions(id = 2, name = "Cheque"),
    ),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/Period.kt`

```kotlin
data class Period(
```
Created by ishankhanna on 19/06/14.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/fixeddepositapplication/FixedDepositApplicationScreen.kt:269</code></summary>

```kotlin

        AppOutlinedTextField(
            value = state.depositPeriod,
            onValueChange = { onAction(FixedDepositApplicationAction.UpdateDepositPeriod(it)) },
            label = stringResource(Res.string.feature_client_fdapp_deposit_period),
            isError = state.depositPeriodError,
            errorText = state.depositPeriodError
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/RejectLoanInput.kt`

```kotlin
data class RejectLoanInput(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/RepaymentFrequencyDayOfWeekType.kt`

```kotlin
data class RepaymentFrequencyDayOfWeekType(
```
Created by nellyk on 2/21/2016.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/RepaymentFrequencyNthDayType.kt`

```kotlin
data class RepaymentFrequencyNthDayType(
```
Created by nellyk on 2/21/2016.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/RepaymentFrequencyType.kt`

```kotlin
data class RepaymentFrequencyType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:88</code></summary>

```kotlin
        interestRatePerPeriod = 2.5,
        termPeriodFrequencyType = TermPeriodFrequencyType(id = 2, value = "Months"),
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/RepaymentFrequencyTypeOptions.kt`

```kotlin
data class RepaymentFrequencyTypeOptions(
```
Created by nellyk on 2/25/2016.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationScreenUiTest.kt:38</code></summary>

```kotlin
    private val template = GroupLoanTemplate(
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        loanPurposeOptions = listOf(LoanPurposeOptions(id = 5, name = "Working capital")),
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/RepaymentSchedule.kt`

```kotlin
data class RepaymentSchedule(
```
Created by ishankhanna on 19/06/14.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/navigation/LoanNavigation.kt:240</code></summary>

```kotlin
private fun NavController.navigateToProfileDetail(item: LoanProfileActionItem, loanId: Int) {
    when (item) {
        LoanProfileActionItem.RepaymentSchedule ->
            navigate(LoanScheduleRoute(loanId))
        LoanProfileActionItem.Transactions ->
            navigate(LoanTransactionsRoute(loanId))
        // The ONE charge module, scoped to this loan — list, add, edit, delete.
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/RepaymentScheduleTableData.kt`

```kotlin
data class RepaymentScheduleTableData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/schedule/LoanScheduleScreen.kt:106</code></summary>

```kotlin
@Composable
internal fun LoanScheduleContent(
    schedule: ScreenState<RepaymentScheduleTableData>,
    onAction: (LoanScheduleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
```

</details>

```kotlin
data class RepaymentScheduleRowData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/schedule/LoanScheduleScreen.kt:275</code></summary>

```kotlin
}

private fun RepaymentScheduleRowData.toDisplayValues() = listOf(
    number, days, date, paidDate, balance,
    principal, interest, fees, penalties,
    due, paid, inAdvance, late, outstanding,
)
```

</details>

```kotlin
data class RepaymentScheduleTotalsData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/schedule/LoanScheduleScreen.kt:281</code></summary>

```kotlin
)

private fun RepaymentScheduleTotalsData.toDisplayValues(totalLabel: String) = listOf(
    "", "", totalLabel, "", "",
    principal, interest, fees, penalties,
    due, paid, inAdvance, late, outstanding,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/SavingsApproval.kt`

```kotlin
data class SavingsApproval(
```
The wire body for `POST savingsaccounts/{id}?command=approve`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/impl/SavingsCommandRepositoryImpl.kt:144</code></summary>

```kotlin
                savingsAccountApi.approveSavingsApplication(
                    savingsAccountId = savingsAccountId,
                    savingsApproval = SavingsApproval(
                        approvedOnDate = it.approvedOnDate,
                        locale = it.locale,
                        dateFormat = it.dateFormat,
                        note = it.note,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/TermFrequencyTypeOptions.kt`

```kotlin
data class TermFrequencyTypeOptions(
```
Created by nellyk on 2/27/2016.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationScreenUiTest.kt:37</code></summary>

```kotlin

    private val template = GroupLoanTemplate(
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        loanPurposeOptions = listOf(LoanPurposeOptions(id = 5, name = "Working capital")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/TermPeriodFrequencyType.kt`

```kotlin
data class TermPeriodFrequencyType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:87</code></summary>

```kotlin
        repaymentEvery = 1,
        interestRatePerPeriod = 2.5,
        termPeriodFrequencyType = TermPeriodFrequencyType(id = 2, value = "Months"),
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/Transaction.kt`

```kotlin
data class Transaction(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/details/CollectionSheetDetailsViewModel.kt:128</code></summary>

```kotlin
        }
        mutableStateFlow.value = state.copy(
            drafts = state.drafts + (editor.loanId to editor.toTransaction(amount)),
            editor = null,
        )
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/TransactionProcessingStrategy.kt`

```kotlin
data class TransactionProcessingStrategy(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/Type.kt`

```kotlin
data class Type(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/detail/ChargeDetailScreen.kt:72</code></summary>

```kotlin
internal fun ChargeDetailScreen(
    onBackPressed: () -> Unit,
    onEditCharge: (resourceType: String, resourceId: Int, chargeId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChargeDetailViewModel = koinViewModel(),
) {
    val charge by viewModel.charge.collectAsStateWithLifecycle()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/assignLoanOfficer/AssignLoanOfficerInput.kt`

```kotlin
data class AssignLoanOfficerInput(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/assignLoanOfficer/AssignLoanOfficerResponse.kt`

```kotlin
data class AssignLoanOfficerResponse(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/guarantor/CreateGuarantor.kt`

```kotlin
data class CreateGuarantor(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/guarantor/CreateGuarantorInput.kt`

```kotlin
data class CreateGuarantorInput(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/guarantor/GuarantorTemplate.kt`

```kotlin
data class GuarantorTemplate(
```
_No KDoc at source._

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
data class GuarantorRelationshipOption(
```
_No KDoc at source._

```kotlin
data class GuarantorAccountTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/loan/LoanCommandRepositoryTest.kt:185</code></summary>

```kotlin
            request: GuarantorRequestDto,
        ): CreateGuarantorResponseDto = unused()
        override suspend fun getGuarantorAccountTemplate(
            loanId: Int,
            clientId: Int,
        ): GuarantorAccountTemplateDto = unused()
        override suspend fun getAllLoans(): List<LoanProducts> = unused()
```

</details>

```kotlin
data class GuarantorType(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanDisburse/LoanDisburseInput.kt`

```kotlin
data class LoanDisburseInput(
```
What the officer is asking Fineract to disburse.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/disbursement/LoanDisbursementViewModel.kt:132</code></summary>

```kotlin

        viewModelScope.launch {
            val request = LoanDisburseInput(
                // Formatted by the helper that owns the two format fields below.
                actualDisbursementDate = ApiDateFormatter.formatForApi(state.date),
                transactionAmount = amount,
                // Null when unchosen, NOT 0. The original initialised `paymentTypeId` to 0 and
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanDisburse/LoanDisburseTemplate.kt`

```kotlin
data class LoanDisburseTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/disbursement/LoanDisbursementViewModelTest.kt:50</code></summary>

```kotlin
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val templateState = MutableStateFlow<ScreenState<LoanDisburseTemplate>>(
        ScreenState.Content(
            LoanDisburseTemplate(
                netDisbursalAmount = 10_000.0,
                date = listOf(2026, 3, 14),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanWithAssociations/ActualDisbursementDate.kt`

```kotlin
data class ActualDisbursementDate(
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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanWithAssociations/LoanAccountSummary.kt`

```kotlin
data class LoanAccountSummary(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/profile/LoanProfileScreenPreview.kt:37</code></summary>

```kotlin
    principal = 25_000.0,
    currency = SavingAccountCurrency(code = "BDT", name = "Bangladeshi Taka", decimalPlaces = 2, displaySymbol = "৳"),
    summary = LoanAccountSummary(totalOutstanding = 26_950.0, totalOverdue = 1_200.0),
)

@Preview
@Composable
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanWithAssociations/LoanStatus.kt`

```kotlin
data class LoanStatus(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/loanaccounts/ClientLoanAccountsViewModel.kt:192</code></summary>

```kotlin
    ;

    fun matches(status: LoanStatus?): Boolean {
        status ?: return false
        return when (this) {
            ACTIVE -> status.active == true
            PENDING -> status.pendingApproval == true
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanWithAssociations/LoanTimeline.kt`

```kotlin
data class LoanTimeline(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/approval/LoanApprovalViewModelTest.kt:233</code></summary>

```kotlin
            LoanWithAssociations(
                id = 7,
                timeline = LoanTimeline(expectedDisbursementDate = listOf(2026)),
            ),
        )

        vm.prefill.test {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanWithAssociations/LoanType.kt`

```kotlin
data class LoanType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/converter/ListTypeConverters.kt:96</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromType(type: LoanType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanWithAssociations/LoanWithAssociations.kt`

```kotlin
data class LoanWithAssociations(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/profile/LoanProfileScreen.kt:131</code></summary>

```kotlin
@Composable
internal fun LoanProfileContent(
    loan: ScreenState<LoanWithAssociations>,
    status: ScreenState<LoanProfileStatus>,
    onAction: (LoanProfileAction) -> Unit,
    modifier: Modifier = Modifier,
    availableDetails: List<LoanProfileActionItem> = emptyList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanWithAssociations/SavingAccountCurrency.kt`

```kotlin
data class SavingAccountCurrency(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/general/ClientGeneralViewModelTest.kt:84</code></summary>

```kotlin
            },
        ),
        currency = symbol?.let { SavingAccountCurrency(displaySymbol = it) },
    )

    // ---- the arithmetic ----
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/reschedules/LoanRescheduleApprovalRequest.kt`

```kotlin
data class LoanRescheduleApprovalRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/reschedules/LoanReschedulesViewModel.kt:162</code></summary>

```kotlin
                    state.loanId,
                    rescheduleId,
                    LoanRescheduleApprovalRequest(
                        approvedOnDate = today,
                        dateFormat = ApiDateFormatter.DATE_FORMAT,
                        locale = ApiDateFormatter.LOCALE,
                    ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/reschedules/LoanRescheduleRejectionRequest.kt`

```kotlin
data class LoanRescheduleRejectionRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/reschedules/LoanReschedulesViewModel.kt:172</code></summary>

```kotlin
                    state.loanId,
                    rescheduleId,
                    LoanRescheduleRejectionRequest(
                        rejectedOnDate = today,
                        dateFormat = ApiDateFormatter.DATE_FORMAT,
                        locale = ApiDateFormatter.LOCALE,
                    ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/reschedules/LoanRescheduleRequest.kt`

```kotlin
data class LoanRescheduleRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/rescheduleform/LoanRescheduleFormViewModel.kt:212</code></summary>

```kotlin

        viewModelScope.launch {
            val request = LoanRescheduleRequest(
                loanId = state.loanId,
                rescheduleFromDate = ApiDateFormatter.formatForApi(draft.rescheduleFrom),
                rescheduleReasonId = draft.reasonId,
                submittedOnDate = ApiDateFormatter.formatForApi(draft.submittedOn),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/reschedules/LoanRescheduleResponse.kt`

```kotlin
data class LoanRescheduleResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/reschedules/LoanReschedulesViewModel.kt:245</code></summary>

```kotlin
}

private fun kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse.toRow() =
    RescheduleRow(
        id = id,
        status = statusEnum.toStatusCode(),
        rescheduleFrom = date(rescheduleFromDate),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/reschedules/LoanRescheduleTemplate.kt`

```kotlin
data class LoanRescheduleTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/rescheduleform/LoanRescheduleFormViewModelTest.kt:57</code></summary>

```kotlin
    private fun viewModel(
        commands: FakeLoanCommandRepository = FakeLoanCommandRepository(),
        rescheduleReasons: () -> LoanRescheduleTemplate? = {
            LoanRescheduleTemplate(rescheduleReasons = reasons)
        },
        loanId: Int = 7,
    ) = LoanRescheduleFormViewModel(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/reschedules/RescheduleReasonOption.kt`

```kotlin
data class RescheduleReasonOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/rescheduleform/LoanRescheduleFormScreenPreview.kt:30</code></summary>

```kotlin
// i18n:skip — preview fixtures below
private val previewReasons = listOf(
    RescheduleReasonOption(id = 1, name = "Crop failure"),
    RescheduleReasonOption(id = 2, name = "Medical emergency"),
)

@Preview
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/reschedules/RescheduleStatus.kt`

```kotlin
data class RescheduleStatus(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/reschedules/LoanReschedulesViewModel.kt:273</code></summary>

```kotlin
 * null status object or all three false.
 */
internal fun RescheduleStatus?.toStatusCode(): RescheduleStatusCode = when {
    this == null -> RescheduleStatusCode.UNKNOWN
    pendingApproval -> RescheduleStatusCode.PENDING
    approved -> RescheduleStatusCode.APPROVED
    rejected -> RescheduleStatusCode.REJECTED
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/reschedules/RescheduleTimeline.kt`

```kotlin
data class RescheduleTimeline(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/reschedules/LoanReschedulesViewModelTest.kt:78</code></summary>

```kotlin
        rescheduleReasonCodeValue = RescheduleReasonOption(id = 3, name = "Crop failure"),
        statusEnum = status,
        timeline = RescheduleTimeline(
            submittedOnDate = listOf(2026, 3, 14),
            submittedByUsername = "r.kumar",
        ),
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/transfer/AccountOption.kt`

```kotlin
data class AccountOption(
```
Data class representing an account option for transfers

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/transfer/LoanTransferScreenPreview.kt:36</code></summary>

```kotlin
)
private val previewAccountType = AccountTypeOption(id = 2, code = "accountType.savings", value = "Savings Account")
private val previewAccount = AccountOption(
    id = 9,
    accountNo = "000000009",
    clientId = 4,
    clientName = "Nusrat Jahan Chowdhury",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/transfer/AccountTransferChanges.kt`

```kotlin
data class AccountTransferChanges(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/transfer/AccountTransferRequest.kt`

```kotlin
data class AccountTransferRequest(
```
Move money from one account to another.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/transfer/LoanTransferViewModel.kt:238</code></summary>

```kotlin

        viewModelScope.launch {
            val request = AccountTransferRequest(
                fromOfficeId = state.fromOfficeId,
                fromClientId = state.fromClientId,
                fromAccountType = LOAN_ACCOUNT_TYPE,
                fromAccountId = state.fromLoanId,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/transfer/AccountTransferResponse.kt`

```kotlin
data class AccountTransferResponse(
```
Response from account transfer submission

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/transfer/AccountTransferTemplate.kt`

```kotlin
data class AccountTransferTemplate(
```
Data class representing the template for account transfers

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/transfer/LoanTransferViewModelTest.kt:94</code></summary>

```kotlin
        toClientId: Int?,
        toAccountType: Int?,
    ) = AccountTransferTemplate(
        toOfficeOptions = offices,
        toClientOptions = if (toOfficeId != null) clients else emptyList(),
        toAccountTypeOptions = if (toClientId != null) accountTypes else emptyList(),
        toAccountOptions = if (toAccountType != null) accounts else emptyList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/transfer/AccountTypeOption.kt`

```kotlin
data class AccountTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/transfer/LoanTransferScreenPreview.kt:35</code></summary>

```kotlin
    officeName = "Head Office",
)
private val previewAccountType = AccountTypeOption(id = 2, code = "accountType.savings", value = "Savings Account")
private val previewAccount = AccountOption(
    id = 9,
    accountNo = "000000009",
    clientId = 4,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/transfer/ClientOption.kt`

```kotlin
data class ClientOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientScreen.kt:216</code></summary>

```kotlin
    state: CreateNewClientState,
    form: ScreenState<CreateClientForm>,
    staffOptions: ScreenState<List<ClientOption>>,
    cropper: ImageCropper,
    onAction: (CreateNewClientAction) -> Unit,
    modifier: Modifier = Modifier,
    address: AddressFormOptions? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/transfer/OfficeOption.kt`

```kotlin
data class OfficeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/groups/src/commonMain/kotlin/kpt/feature/groups/create/CreateGroupScreen.kt:165</code></summary>

```kotlin
                onOptionSelected = { index, label ->
                    officeList.getOrNull(index)?.let {
                        onAction(CreateGroupAction.UpdateOffice(OfficeOption(it.id, label)))
                    }
                },
                options = officeList.mapNotNull { it.name },
                label = stringResource(Res.string.feature_groups_create_office),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/ChargeCalculationType.kt`

```kotlin
data class ChargeCalculationType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:61</code></summary>

```kotlin
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
        penalty = false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/ChargeTimeType.kt`

```kotlin
data class ChargeTimeType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:60</code></summary>

```kotlin
            name = name,
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/FieldOfficerOptions.kt`

```kotlin
class FieldOfficerOptions(
```
Created by nkiboi on 12/15/2015.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingProductsTemplate.kt:45</code></summary>

```kotlin
    val expenseAccountOptions: List<AccountOptions>? = null,
    val incomeAccountOptions: List<AccountOptions>? = null,
    val fieldOfficerOptions: List<FieldOfficerOptions>? = null,

    val chargeOptions: List<ChargeOptions>? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/InterestCalculationDaysInYearType.kt`

```kotlin
class InterestCalculationDaysInYearType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:46</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestCalculationDaysInYearType(
        type: InterestCalculationDaysInYearType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/InterestCalculationType.kt`

```kotlin
data class InterestCalculationType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:58</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestCalculationType(type: InterestCalculationType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/InterestCompoundingPeriodType.kt`

```kotlin
data class InterestCompoundingPeriodType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:68</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestCompoundingPeriodType(
        type: InterestCompoundingPeriodType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/InterestPostingPeriodType.kt`

```kotlin
data class InterestPostingPeriodType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:25</code></summary>

```kotlin
class SavingsPeriodTypeConverters {
    @ColumnTypeConverter
    fun fromInterestPostingPeriodType(
        type: InterestPostingPeriodType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/LockinPeriodFrequencyType.kt`

```kotlin
data class LockinPeriodFrequencyType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:39</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromLockinPeriodFrequencyType(type: LockinPeriodFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/SavingsAccountTransactionResponse.kt`

```kotlin
data class SavingsAccountTransactionResponse(
```
Created by ishankhanna on 12/06/14.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/SavingsCommandRepository.kt:54</code></summary>

```kotlin
        transactionType: String,
        input: SavingsTransactionInput,
    ): MutationResult<SavingsAccountTransactionResponse>

    /** Activate an approved savings account. Typed, not a `HashMap<String, String>`. */
    suspend fun activateSavings(
        savingsAccountId: Int,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/SavingsWriteInputs.kt`

```kotlin
data class SavingsApprovalInput(
```
Typed inputs for the three savings writes an officer performs in the field.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/statuschange/SavingsStatusChangeViewModel.kt:111</code></summary>

```kotlin
                SavingsStatusChange.APPROVE -> commands.approveSavingsApplication(
                    savingsAccountId = state.accountId,
                    input = SavingsApprovalInput(
                        approvedOnDate = date,
                        locale = ApiDateFormatter.LOCALE,
                        dateFormat = ApiDateFormatter.DATE_FORMAT,
                        // Blank omitted. The original sent the empty string, which Fineract stores
```

</details>

```kotlin
data class SavingsActivationInput(
```
Activate an approved savings account. See `SavingsApprovalInput` for the date-pairing rule.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/statuschange/SavingsStatusChangeViewModel.kt:123</code></summary>

```kotlin
                SavingsStatusChange.ACTIVATE -> commands.activateSavings(
                    savingsAccountId = state.accountId,
                    input = SavingsActivationInput(
                        activatedOnDate = date,
                        locale = ApiDateFormatter.LOCALE,
                        dateFormat = ApiDateFormatter.DATE_FORMAT,
                    ),
```

</details>

```kotlin
data class SavingsTransactionInput(
```
A deposit or a withdrawal.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/transaction/SavingsTransactionViewModel.kt:164</code></summary>

```kotlin
                savingsAccountId = state.accountId,
                transactionType = state.kind.command,
                input = SavingsTransactionInput(
                    // The date and the format built together, by the helper that owns both. The
                    // original paired `DateHelper.getDateAsStringFromLong` with
                    // `DateHelper.SHORT_MONTH`, which DID agree — but only because both came from
                    // the same object; pairing them here is what keeps that true.
```

</details>

```kotlin
data class SavingsApplicationInput(
```
A new savings application, for a CLIENT or a GROUP. `ownerId` + `isGroup` replace the original route's `(groupId, clientId, isGroupAccount)` triple — of which the wizard's ViewModel read only `clientId`, dropping the other two.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/application/SavingsApplicationViewModel.kt:119</code></summary>

```kotlin
        ownerId = state.ownerId.toLong(),
        SavingsFormSnapshot.serializer(),
        SavingsApplicationInput.serializer(),
        ownerKind = if (state.isGroup) "group" else "client",
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered
```

</details>

```kotlin
data class SavingsChargeInput(val chargeId: Int, val amount: String)
```
One charge on a savings application: which charge, and how much.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/application/SavingsApplicationViewModel.kt:355</code></summary>

```kotlin
                enforceMinRequiredBalance = state.toggle(SavingsToggle.ENFORCE_MIN_BALANCE),
                charges = state.charges.map {
                    SavingsChargeInput(chargeId = it.chargeId, amount = it.amount)
                },
            )

            lastInput = input
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/Timeline.kt`

```kotlin
data class Timeline(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/editdetails/ClientEditDetailsFormTest.kt:189</code></summary>

```kotlin
            legalForm = ClientStatus(id = 1, value = "PERSON"),
            dateOfBirth = listOf(1815, 12, 10),
            timeline = Timeline(submittedOnDate = listOf(2026, 1, 9)),
        ).asPrefill()

        assertEquals("Ada", prefilled.firstName)
        assertEquals("000000042", prefilled.accountNo)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/share/ShareAccounts.kt`

```kotlin
data class ShareAccounts(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/shareaccounts/ClientShareAccountsScreen.kt:133</code></summary>

```kotlin
internal fun ClientShareAccountsContent(
    state: ClientShareAccountsState,
    accounts: ScreenState<List<ShareAccounts>>,
    onAction: (ClientShareAccountsAction) -> Unit,
    modifier: Modifier = Modifier,
    totalCount: Int = 0,
    canAddAccount: Boolean = false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/share/ShareAccountsStatus.kt`

```kotlin
data class ShareAccountsStatus(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/account/share/ShareAccounts.kt:25</code></summary>

```kotlin
    val productId: Int? = null,
    val productName: String? = null,
    val status: ShareAccountsStatus? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/charge/ChargeResource.kt`

```kotlin
enum class ChargeResource(val path: String)
```
What a charge is levied ON. `path` is the Fineract path segment of `/{resource}/{resourceId}/charges`.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/detail/ChargeDetailViewModel.kt:44</code></summary>

```kotlin
) : BaseViewModel<Unit, ChargeDetailEvent, ChargeDetailAction>(Unit) {

    private val resource: ChargeResource = savedStateHandle.chargeResource()
    private val resourceId: Int = savedStateHandle.chargeResourceId()
    private val chargeId: Int = savedStateHandle.get<Int>(CHARGE_ID_KEY) ?: -1

    val canEdit: Boolean = resource.canUpdate
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/checkerinboxtask/CheckerInboxSearchTemplate.kt`

```kotlin
data class CheckerInboxSearchTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/checkerinbox/src/commonMain/kotlin/kpt/feature/checkerinbox/ui/CheckerInboxPreviews.kt:45</code></summary>

```kotlin
)

private val previewTemplate = CheckerInboxSearchTemplate(
    actionNames = listOf("CREATE", "APPROVE"),
    entityNames = listOf("CLIENT", "LOAN"),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/checkerinboxtask/CheckerTask.kt`

```kotlin
data class CheckerTask(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/checkerinbox/src/commonMain/kotlin/kpt/feature/checkerinbox/ui/CheckerInboxPreviews.kt:27</code></summary>

```kotlin
// i18n:skip — preview fixtures
private val previewTasks = listOf(
    CheckerTask(41, OCT_20_2026, "awaiting.approval", "asha.r", "CREATE", "CLIENT", "1043"),
    CheckerTask(40, OCT_20_2026 - DAY, "awaiting.approval", "bimal.d", "APPROVE", "LOAN", "877"),
    CheckerTask(37, OCT_20_2026 - 3 * DAY, "awaiting.approval", "asha.r", "DISBURSE", "LOAN", "861"),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/checkerinboxtask/RescheduleLoansTask.kt`

```kotlin
data class RescheduleLoansTask(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/checkerinbox/src/commonMain/kotlin/kpt/feature/checkerinbox/ui/CheckerInboxPreviews.kt:34</code></summary>

```kotlin
// i18n:skip — preview fixtures
private val previewReschedules = listOf(
    RescheduleLoansTask(
        4,
        77,
        "Asha Rahman",
        "000000077",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/checkerinboxtask/RescheduleReasonCodeValue.kt`

```kotlin
data class RescheduleReasonCodeValue(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/checkerinbox/src/commonMain/kotlin/kpt/feature/checkerinbox/ui/CheckerInboxPreviews.kt:40</code></summary>

```kotlin
        "000000077",
        listOf(2026, 11, 5),
        rescheduleReasonCodeValue = RescheduleReasonCodeValue(21, "Crop failure"),
    ),
    RescheduleLoansTask(5, 78, "Bimal Das", "000000078", listOf(2026, 12, 1)),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/ActivatePayload.kt`

```kotlin
data class ActivatePayload(
```
Created by Rajan Maurya on 09/02/17.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:173</code></summary>

```kotlin
                            // the formatter that owns the format constant, which closure and
                            // transfer did NOT (see their ViewModels).
                            ActivatePayload(
                                activationDate = ApiDateFormatter.formatForApi(activateDate),
                                dateFormat = ApiDateFormatter.DATE_FORMAT,
                                locale = ApiDateFormatter.LOCALE,
                            ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/Address.kt`

```kotlin
data class Address(
```
Created by Rajan Maurya on 15/12/16.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/address/ClientAddressTestTags.kt:14</code></summary>

```kotlin
/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientAddressTestTags {
    const val SCREEN = "clientAddress:screen"
    const val LIST = "clientAddress:list"
    const val COUNT = "clientAddress:count"
    const val ADD = "clientAddress:add"
    const val EMPTY = "clientAddress:empty"
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/AssignStaffRequest.kt`

```kotlin
data class AssignStaffRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/details/ClientDetailsViewModel.kt:138</code></summary>

```kotlin

        viewModelScope.launch {
            when (val result = commands.unassignStaff(clientId, AssignStaffRequest(staffId = staffId))) {
                is MutationResult.Applied -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = ClientDetailsState.DialogState.ShowStatusDialog(ResultStatus.SUCCESS),
                )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/ChargeCreationResponse.kt`

```kotlin
data class ChargeCreationResponse(
```
Created by Tarun on 13-08-17.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/ChargesRename.kt`

```kotlin
data class ChargesRename(
```
Created by nellyk on 2/15/2016. / /* This project is licensed under the open source MPL V2.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/ClientAddressEntity.kt`

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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/ClientAddressRequest.kt`

```kotlin
data class ClientAddressRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt:74</code></summary>

```kotlin
    suspend fun addClientPinpointLocation(
        clientId: Int,
        clientAddressRequest: ClientAddressRequest?,
    ): MutationResult<PinpointLocationActionResponse>

    suspend fun deleteClientPinpointLocation(
        apptableId: Int,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/ClientAddressResponse.kt`

```kotlin
data class ClientAddressResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientRepository.kt:111</code></summary>

```kotlin

    /** Where the officer has recorded this client — the pinpoint map/list. */
    fun pinpointLocationsStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<List<ClientAddressResponse>>

    /** Collateral the client has already pledged. */
    fun clientCollateralStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<List<CollateralItemResult>>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/ClientCloseRequest.kt`

```kotlin
data class ClientCloseRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/closure/ClientClosureViewModel.kt:105</code></summary>

```kotlin

        viewModelScope.launch {
            val request = ClientCloseRequest(
                // Formatted by the helper that OWNS the format constant below — see the header.
                closureDate = ApiDateFormatter.formatForApi(state.date),
                closureReasonId = closureReasonId,
                dateFormat = ApiDateFormatter.DATE_FORMAT,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/CollateralPayload.kt`

```kotlin
data class CollateralPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/collateral/ClientCollateralViewModel.kt:86</code></summary>

```kotlin
        ownerId = state.id.toLong(),
        ClientCollateralState.serializer(),
        CollateralPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/Page.kt`

```kotlin
data class Page<T>(
```
Created by ishankhanna on 09/02/14.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/center/CenterRepositoryTest.kt:100</code></summary>

```kotlin
        }

        override suspend fun getCenters(b: Boolean, offset: Int, limit: Int): Page<Center> {
            check(online) { "offline" }
            return Page(pageItems = records.values.map { Center(id = it.id, name = it.name) })
        }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/Permission.kt`

```kotlin
data class Permission(
```
Created by ishankhanna on 09/02/14.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsExtensions.kt:117</code></summary>

```kotlin

/** A runtime permission decision. */
fun AnalyticsHelper.trackPermission(permissionName: String, granted: Boolean) {
    logEvent(
        AnalyticsEvent(
            if (granted) KptEventTypes.PERMISSION_GRANTED else KptEventTypes.PERMISSION_DENIED,
            listOf(Param(KptParamKeys.PERMISSION_NAME, permissionName)),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/ProposeTransferRequest.kt`

```kotlin
data class ProposeTransferRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/transfer/ClientTransferViewModel.kt:97</code></summary>

```kotlin

        viewModelScope.launch {
            val request = ProposeTransferRequest(
                destinationOfficeId = destinationOfficeId,
                transferDate = ApiDateFormatter.formatForApi(state.date),
                note = state.note,
                dateFormat = ApiDateFormatter.DATE_FORMAT,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/Role.kt`

```kotlin
data class Role(
```
Created by ishankhanna on 09/02/14.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:443</code></summary>

```kotlin
                    .shadow(CARD_SHADOW, CircleShape)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onProfile)
                    .semantics { contentDescription = profileLabel }
                    .testTag(TestTags.Home.PROFILE)
                    .background(c.primaryContainer, CircleShape)
                    .border(0.5f.tw, c.surfaceContainerLowest, CircleShape),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/UpdateSavingsAccountRequest.kt`

```kotlin
data class UpdateSavingsAccountRequest(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/defaultaccount/ClientDefaultAccountViewModel.kt:133</code></summary>

```kotlin
        ownerId = state.clientId.toLong(),
        ClientDefaultAccountState.serializer(),
        UpdateSavingsAccountRequest.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/AttendanceTypeOption.kt`

```kotlin
data class AttendanceTypeOption(
```
Created by Tarun on 21-07-2017.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/ClientCollectionSheet.kt:25</code></summary>

```kotlin
    var clientName: String? = null,
    var loans: ArrayList<LoanCollectionSheet>? = null,
    var attendanceType: AttendanceTypeOption? = null,

    var savings: ArrayList<SavingsCollectionSheet> = ArrayList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/BulkSavingsDueTransaction.kt`

```kotlin
data class BulkSavingsDueTransaction(
```
Created by Tarun on 31-07-17.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/CollectionSheetPayload.kt:26</code></summary>

```kotlin

    var bulkRepaymentTransactions: MutableList<BulkRepaymentTransactions> = ArrayList(),
    var bulkSavingsDueTransactions: MutableList<BulkSavingsDueTransaction> = ArrayList(),

    var calendarId: Int? = 0,

    var clientsAttendance: MutableList<ClientsAttendance> = ArrayList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/CollectionFrequency.kt`

```kotlin
data class CollectionFrequency(
```
Created by ishankhanna on 16/07/14.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/CollectionMeetingCalendar.kt`

```kotlin
data class CollectionMeetingCalendar(
```
Created by ishankhanna on 16/07/14.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterWithAssociations.kt:50</code></summary>

```kotlin

    var groupMembers: List<GroupEntity> = ArrayList(),
    var collectionMeetingCalendar: CollectionMeetingCalendar = CollectionMeetingCalendar(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/CollectionSheetRequestPayload.kt`

```kotlin
data class CollectionSheetRequestPayload(
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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/EntityType.kt`

```kotlin
data class EntityType(
```
Created by ishankhanna on 16/07/14.

<details><summary>Used in the template — <code>feature/document/src/commonMain/kotlin/kpt/feature/document/list/DocumentListScreenPreview.kt:29</code></summary>

```kotlin
    Document(
        id = 1,
        parentEntityType = "groups",
        parentEntityId = 11,
        name = "Meeting minutes",
        fileName = "minutes-oct.pdf",
        size = 182_000,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/LoanAndClientName.kt`

```kotlin
class LoanAndClientName(
```
Created by Tarun on 17-07-2017.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/LoanCollectionSheet.kt`

```kotlin
data class LoanCollectionSheet(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/CollectionSheetPreviews.kt:37</code></summary>

```kotlin
            clientName = "Asha Rahman",
            loans = arrayListOf(
                LoanCollectionSheet(
                    loanId = 11,
                    accountId = "000000011",
                    productShortName = "GL",
                    totalDue = 1200.0,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/SavingsProduct.kt`

```kotlin
data class SavingsProduct(
```
Created by Tarun on 31-07-17.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/CollectionSheetResponse.kt:32</code></summary>

```kotlin
    var paymentTypeOptions: List<PaymentTypeOptionEntity> = ArrayList(),

    var savingsProducts: List<SavingsProduct> = ArrayList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/commonfiles/InterestType.kt`

```kotlin
data class InterestType(
```
Created by rajan on 13/3/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:90</code></summary>

```kotlin
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Account.kt`

```kotlin
data class Account(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/shareapplication/ShareApplicationScreen.kt:217</code></summary>

```kotlin
        val savings = template.savingsAccountOptions.orEmpty()
        AppTextFieldDropdown(
            value = state.savingsAccount?.label.orEmpty(),
            onOptionSelected = { index, label ->
                savings.getOrNull(index)?.id?.let {
                    onAction(ShareApplicationAction.UpdateSavingsAccount(ShareOption(it, label)))
                }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/AppGroup.kt`

```kotlin
data class AppGroup(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Client.kt:21</code></summary>

```kotlin
    var attendanceType: AttendanceType? = null,

    var mifosGroup: AppGroup? = null,

    val loans: List<Loan> = ArrayList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/AttendanceType.kt`

```kotlin
data class AttendanceType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Client.kt:19</code></summary>

```kotlin
    var clientName: String? = null,

    var attendanceType: AttendanceType? = null,

    var mifosGroup: AppGroup? = null,

    val loans: List<Loan> = ArrayList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Client.kt`

```kotlin
data class Client(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:210</code></summary>

```kotlin
        ActivateResult.Succeeded -> stringResource(
            when (state.type) {
                ActivateType.Client -> Res.string.feature_activate_client
                ActivateType.Center -> Res.string.feature_activate_center
                ActivateType.Group -> Res.string.feature_activate_group
            },
        )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/CollectionMeetingCalendar.kt`

```kotlin
data class CollectionMeetingCalendar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterWithAssociations.kt:50</code></summary>

```kotlin

    var groupMembers: List<GroupEntity> = ArrayList(),
    var collectionMeetingCalendar: CollectionMeetingCalendar = CollectionMeetingCalendar(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/CollectionSheet.kt`

```kotlin
data class CollectionSheet(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/activate/src/commonTest/kotlin/kpt/feature/activate/ui/ActivateViewModelTest.kt:268</code></summary>

```kotlin
    ) = error("not used by these tests")

    override suspend fun saveCollectionSheet(
        centerId: Int,
        collectionSheetPayload: CollectionSheetPayload?,
    ) = error("not used by these tests")
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Currency.kt`

```kotlin
data class Currency(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:299</code></summary>

```kotlin
                activeLoans = loans?.activeLoans,
                inArrears = loans?.inArrears,
                loanTotals = loans?.byCurrency,
                savings = (byKey[SAVINGS] as? List<*>)?.filterIsInstance<Money>(),
                groups = byKey[GROUPS] as? Int,
                centers = byKey[CENTERS] as? Int,
            )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/EntityType.kt`

```kotlin
data class EntityType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/document/src/commonMain/kotlin/kpt/feature/document/list/DocumentListScreenPreview.kt:29</code></summary>

```kotlin
    Document(
        id = 1,
        parentEntityType = "groups",
        parentEntityId = 11,
        name = "Meeting minutes",
        fileName = "minutes-oct.pdf",
        size = 182_000,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Loan.kt`

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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/MeetingCenter.kt`

```kotlin
data class MeetingCenter(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/OfflineCenter.kt:17</code></summary>

```kotlin
    var staffName: String? = null,

    var meetingFallCenters: Array<MeetingCenter>? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/MeetingDate.kt`

```kotlin
data class MeetingDate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/CollectionMeetingCalendar.kt:27</code></summary>

```kotlin
    var location: String? = null,

    var meetingCalendarDate: MeetingDate? = null,

    var isRepeating: Boolean = false,

    var recurrence: String? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/OfflineCenter.kt`

```kotlin
data class OfflineCenter(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/api/CenterApi.kt:161</code></summary>

```kotlin
        @Query("officeId") officeId: Int,
        @Query("staffId") staffId: Int,
    ): List<OfflineCenter>

    /**
     * This is the service to activate the center
     * REST ENT POINT
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/RepaymentTransaction.kt`

```kotlin
data class RepaymentTransaction(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Status.kt`

```kotlin
data class Status(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/identifiers/IdentifierDocumentKey.kt:30</code></summary>

```kotlin
 * including `"null"` for an absent part, so documents uploaded by the original app still match.
 */
internal fun identifierDocumentKey(documentTypeName: String?, documentKey: String?, status: Status?): String =
    "$documentTypeName$documentKey$status"

/** The key without its status — what still names the identifier after its status changes. */
internal fun identifierDocumentKeyPrefix(documentTypeName: String?, documentKey: String?): String =
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/error/AppError.kt`

```kotlin
data class AppError(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/error/ServerErrorParser.kt:29</code></summary>

```kotlin

    /** The [AppError] in [body] — a bare JSON body, or text that contains one after Ktor's prefix. */
    fun parse(body: String): AppError? {
        val json = extractJsonObject(body) ?: return null
        return runCatching { decoder.decodeFromString(AppError.serializer(), json) }.getOrNull()
            ?.takeIf { it.userMessage != null }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/error/Arg.kt`

```kotlin
data class Arg(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/error/Errors.kt`

```kotlin
data class Errors(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:267</code></summary>

```kotlin
        /** The shape the Ktor client error carries, trimmed from the one captured on device. */
        const val FINERACT_403 = "Client request(PUT https://x/loans/1/charges/1) invalid: 403. Text: " +
            "\"{\"defaultUserMessage\":\"Errors contain reason for domain rule violation.\",\"errors\":" +
            "[{\"defaultUserMessage\":\"This charge cannot be updated as the loan is not pending approval\"}]}\""
    }
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/error/ServerErrorParser.kt`

```kotlin
object ServerErrorParser
```
Parses Fineract error responses into `AppError` — the ONE place a server error is decoded.

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:258</code></summary>

```kotlin
        assertEquals(
            "This charge cannot be updated as the loan is not pending approval",
            ServerErrorParser.parse(FINERACT_403)?.userMessage,
        )
        // Not a Fineract body (a dropped connection) -> no message; the screen's generic one shows.
        assertNull(ServerErrorParser.parse("Unable to resolve host")?.userMessage)
    }
```

</details>

- `fun parse(body: String): AppError?` — The `AppError` in `body` — a bare JSON body, or text that contains one after Ktor's prefix.
- `val json = extractJsonObject(body) ?: return null`
- `fun parse(error: Throwable?): AppError? =` — The `AppError` carried by `error` or by one of its causes — a repository that wraps the transport exception does not hide the server's answer.
- `val bodyStart = text.indexOf(KTOR_BODY_MARKER).let { if (it < 0) 0 else it }`
- `val start = text.indexOf('{', bodyStart)`
- `val end = text.lastIndexOf('}')`

```kotlin
val AppError.userMessage: String?
```
The text to show an officer: the per-field `AppError.errors` messages (they say WHAT is wrong — the top-level message is usually just "Validation errors exist."), else the top-level one.

```kotlin
fun Throwable?.serverErrorMessage(): String? = ServerErrorParser.parse(this)?.userMessage
```
The server's user-facing message for this failure, or null when it sent none — in which case show the screen's own generic copy, never this throwable's `message` (a diagnostic, not copy).

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:1339</code></summary>

```kotlin
 */
@Composable
private fun errorKind(error: Throwable?, offline: Boolean): String {
    if (offline || error == null) {
        return stringResource(if (offline) Res.string.feature_home_dashboard_error_kind_offline else Res.string.feature_home_dashboard_error_kind_failed)
    }
    return when (val cat = categorize(error)) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/groups/CenterInfo.kt`

```kotlin
data class CenterInfo(
```
Created by Rajan Maurya on 05/02/17. The `GroupSummaryCounts` report answers in LOWER-case keys (`activeclients`, `overduegrouploans`) — measured on the gateway 2026-10-25.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/detail/CenterDetailScreen.kt:131</code></summary>

```kotlin
    canActivate: Boolean = false,
    meeting: ScreenState<CenterWithAssociations> = ScreenState.Loading,
    summary: ScreenState<CenterInfo> = ScreenState.Loading,
    canAddSavingsAccount: Boolean = false,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(CenterDetailTestTags.SCREEN),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/nav/ClientArgs.kt`

```kotlin
data class ClientArgs(
```
/* Copyright 2024 Mifos Initiative This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/nav/DataTableNavigationArg.kt`

```kotlin
data class DataTableNavigationArg(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/noncoreobjects/Document.kt`

```kotlin
data class Document(
```
Created by ishankhanna on 02/07/14.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/adddocument/ClientAddDocumentViewModel.kt:138</code></summary>

```kotlin
                description = state.description.trim(),
            )
            when (val result = commands.createDocument(ENTITY_TYPE, state.clientId, body)) {
                is MutationResult.Applied -> {
                    mutableStateFlow.value = state.copy(isSubmitting = false)
                    sendEvent(ClientAddDocumentEvent.Uploaded)
                }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/noncoreobjects/DocumentType.kt`

```kotlin
data class DocumentType(
```
Created by ishankhanna on 03/07/14.

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/identifiers/ClientIdentifiersViewModelTest.kt:173</code></summary>

```kotlin
            id = 3,
            documentKey = "A1234",
            documentType = DocumentType(name = "Passport"),
            // Fineract's status code — mapped through getClientIdentifierStatus, as the list does.
            status = "clientIdentifierStatusType.active",
        )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/noncoreobjects/DocumentUpload.kt`

```kotlin
class DocumentUpload(
```
A document to attach to a Fineract entity — what the officer picked, in domain terms.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientViewModel.kt:625</code></summary>

```kotlin
        val upload = commands.uploadClientImage(
            clientId,
            DocumentUpload(
                name = photo.fileName,
                fileName = "${photo.fileName.substringBeforeLast(".")}.${ImageFormat.PNG.name.lowercase()}",
                mimeType = getMimeTypeFromPlatformFile(ImageFormat.PNG.name),
                bytes = photo.bytes,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/noncoreobjects/Identifier.kt`

```kotlin
data class Identifier(
```
Created by ishankhanna on 03/07/14.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addidentifier/ClientAddIdentifierViewModel.kt:118</code></summary>

```kotlin
                mutableStateFlow.value = state.copy(description = action.value)

            ClientAddIdentifierAction.CreateIdentifier -> createIdentifier()

            ClientAddIdentifierAction.OpenFileSource ->
                mutableStateFlow.value = state.copy(isSourceSheetOpen = true)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/noncoreobjects/IdentifierPayload.kt`

```kotlin
data class IdentifierPayload(
```
Created by Rajan Maurya on 01/10/16.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addidentifier/ClientAddIdentifierViewModel.kt:156</code></summary>

```kotlin
        mutableStateFlow.value = state.copy(isSubmitting = true, error = null, serverMessage = null)
        viewModelScope.launch {
            val payload = IdentifierPayload(
                documentTypeId = state.documentTypeId,
                documentKey = state.documentKey.trim(),
                status = state.status,
                description = state.description.trim().ifEmpty { null },
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/noncoreobjects/IdentifierTemplate.kt`

```kotlin
class IdentifierTemplate(
```
Created by Rajan Maurya on 01/10/16.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addidentifier/ClientAddIdentifierScreen.kt:127</code></summary>

```kotlin
internal fun ClientAddIdentifierContent(
    state: ClientAddIdentifierState,
    template: ScreenState<IdentifierTemplate>,
    onAction: (ClientAddIdentifierAction) -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/noncoreobjects/IdentifierType.kt`

```kotlin
data class IdentifierType(
```
Created by Rajan Maurya on 01/10/16.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/note/CreateNoteInput.kt`

```kotlin
data class CreateNoteInput(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/note/CreateNoteResponseDto.kt`

```kotlin
data class CreateNoteResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/note/src/commonTest/kotlin/kpt/feature/note/testing/FakeNoteRepository.kt:110</code></summary>

```kotlin
        noteRequestDto: NoteRequestDto,
        idempotencyKey: String,
    ): MutationResult<CreateNoteResponseDto> {
        added += Written(resourceType, resourceId, text = noteRequestDto.note)
        createKeys += idempotencyKey
        return outcome().map { CreateNoteResponseDto() }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/note/DeleteNoteResponseDto.kt`

```kotlin
data class DeleteNoteResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/note/src/commonTest/kotlin/kpt/feature/note/testing/FakeNoteRepository.kt:130</code></summary>

```kotlin
        resourceId: Long,
        noteId: Long,
    ): MutationResult<DeleteNoteResponseDto> {
        deleted += Written(resourceType, resourceId, noteId)
        return outcome().map { DeleteNoteResponseDto() }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/note/Note.kt`

```kotlin
data class Note(
```
A note on a client — what every layer above `core/store` sees. The Room row is `core/database`'s `NoteEntity`; `core/store` maps between the two, so a feature never depends on the database.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/create/CreateCenterScreen.kt:224</code></summary>

```kotlin
        is CreateCenterState.Dialog.Failed -> dialog.serverMessage?.let { "$failed\n$it" } ?: failed
    }
    val savedNote = stringResource(BaseRes.string.draft_saved)
    val canSaveDraft = dialog is CreateCenterState.Dialog.Failed && !state.draftSaved
    AlertDialog(
        onDismissRequest = { onAction(CreateCenterAction.DismissDialog) },
        confirmButton = {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/note/NoteRequestDto.kt`

```kotlin
data class NoteRequestDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/note/src/commonTest/kotlin/kpt/feature/note/testing/FakeNoteRepository.kt:108</code></summary>

```kotlin
        resourceType: String,
        resourceId: Long,
        noteRequestDto: NoteRequestDto,
        idempotencyKey: String,
    ): MutationResult<CreateNoteResponseDto> {
        added += Written(resourceType, resourceId, text = noteRequestDto.note)
        createKeys += idempotencyKey
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/note/UpdateNoteInput.kt`

```kotlin
data class UpdateNoteInput(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/note/UpdateNoteResponseDto.kt`

```kotlin
data class UpdateNoteResponseDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/note/src/commonTest/kotlin/kpt/feature/note/testing/FakeNoteRepository.kt:121</code></summary>

```kotlin
        noteId: Long,
        noteRequestDto: NoteRequestDto,
    ): MutationResult<UpdateNoteResponseDto> {
        updated += Written(resourceType, resourceId, noteId, noteRequestDto.note)
        return outcome().map { UpdateNoteResponseDto() }
    }
```

</details>

```kotlin
data class ChangesDto(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/AccountingRule.kt`

```kotlin
data class AccountingRule(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/LoanProducts.kt:151</code></summary>

```kotlin

    @SerialName("accountingRule")
    var accountingRule: AccountingRule? = null,

    @SerialName("multiDisburseLoan")
    var multiDisburseLoan: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/AllowAttributeOverrides.kt`

```kotlin
data class AllowAttributeOverrides(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/LoanProducts.kt:169</code></summary>

```kotlin

    @SerialName("allowAttributeOverrides")
    var allowAttributeOverrides: AllowAttributeOverrides? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/AmortizationType.kt`

```kotlin
data class AmortizationType(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:89</code></summary>

```kotlin
        termPeriodFrequencyType = TermPeriodFrequencyType(id = 2, value = "Months"),
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/ClientClassificationOptions.kt`

```kotlin
data class ClientClassificationOptions(
```
Created by nkiboi on 12/15/2015.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/ClientTypeOptions.kt`

```kotlin
data class ClientTypeOptions(
```
Created by nkiboi on 12/15/2015.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/Currency.kt`

```kotlin
data class Currency(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:299</code></summary>

```kotlin
                activeLoans = loans?.activeLoans,
                inArrears = loans?.inArrears,
                loanTotals = loans?.byCurrency,
                savings = (byKey[SAVINGS] as? List<*>)?.filterIsInstance<Money>(),
                groups = byKey[GROUPS] as? Int,
                centers = byKey[CENTERS] as? Int,
            )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/DaysInMonthType.kt`

```kotlin
data class DaysInMonthType(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:122</code></summary>

```kotlin
    val canDisburse: Boolean? = null,
    val product: Product? = null,
    val daysInMonthType: DaysInMonthType? = null,
    val daysInYearType: DaysInYearType? = null,

    val isInterestRecalculationEnabled: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/DaysInYearType.kt`

```kotlin
data class DaysInYearType(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:123</code></summary>

```kotlin
    val product: Product? = null,
    val daysInMonthType: DaysInMonthType? = null,
    val daysInYearType: DaysInYearType? = null,

    val isInterestRecalculationEnabled: Boolean? = null,

    val isvaliableInstallmentsAllowed: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/GenderOptions.kt`

```kotlin
data class GenderOptions(
```
Created by nkiboi on 12/15/2015.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/InterestCalculationDaysInYearType.kt`

```kotlin
data class InterestCalculationDaysInYearType(
```
Created by nkiboi on 12/15/2015.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:46</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestCalculationDaysInYearType(
        type: InterestCalculationDaysInYearType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/InterestCalculationPeriodType.kt`

```kotlin
data class InterestCalculationPeriodType(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationScreenUiTest.kt:45</code></summary>

```kotlin
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        interestCalculationPeriodTypeOptions = listOf(InterestCalculationPeriodType(id = 1, value = "Daily")),
        transactionProcessingStrategyOptions = listOf(
            TransactionProcessingStrategyOptions(id = 4, code = "mifos-standard-strategy", name = "Standard"),
        ),
        fundOptions = listOf(FundOptions(id = 9, name = "Donor fund")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/InterestCalculationTypeOptions.kt`

```kotlin
data class InterestCalculationTypeOptions(
```
Created by nkiboi on 12/15/2015.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/InterestCompoundingPeriod.kt`

```kotlin
data class InterestCompoundingPeriod(
```
Created by nkiboi on 12/15/2015.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/InterestPostingPeriodType.kt`

```kotlin
data class InterestPostingPeriodType(
```
Created by nkiboi on 12/15/2015.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:25</code></summary>

```kotlin
class SavingsPeriodTypeConverters {
    @ColumnTypeConverter
    fun fromInterestPostingPeriodType(
        type: InterestPostingPeriodType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/InterestRateFrequencyType.kt`

```kotlin
data class InterestRateFrequencyType(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanTypeConverters.kt:80</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestRateFrequencyType(type: InterestRateFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/InterestType.kt`

```kotlin
data class InterestType(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:90</code></summary>

```kotlin
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/LoanFund.kt`

```kotlin
data class LoanFund(
```
Created by nellyk on 2/21/2016.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/LoanProducts.kt`

```kotlin
data class LoanProducts(
```
Created by Rajan Maurya on 12/15/2015.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationViewModelTest.kt:58</code></summary>

```kotlin

    private val products = listOf(
        LoanProducts(id = 11, name = "Group Grameen"),
        LoanProducts(id = 22, name = "Group Emergency"),
        // Unsendable: no id. Must not be offered.
        LoanProducts(id = null, name = "Draft product"),
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/LoanPurpose.kt`

```kotlin
data class LoanPurpose(
```
Created by nellyk on 2/21/2016.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/ProductSavings.kt`

```kotlin
data class ProductSavings(
```
Created by nellyk on 12/15/2015.

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/application/SavingsApplicationViewModelTest.kt:57</code></summary>

```kotlin

    private val products = listOf(
        ProductSavings(id = 7, name = "Passbook Savings"),
        ProductSavings(id = 8, name = "Youth Savings"),
        // Unsendable: no id. Must not be offered.
        ProductSavings(id = null, name = "Draft product"),
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/RepaymentFrequencyType.kt`

```kotlin
data class RepaymentFrequencyType(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:88</code></summary>

```kotlin
        interestRatePerPeriod = 2.5,
        termPeriodFrequencyType = TermPeriodFrequencyType(id = 2, value = "Months"),
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/payloads/ChargesPayload.kt`

```kotlin
data class ChargesPayload(
```
Created by nellyk on 2/15/2016. / /* This project is licensed under the open source MPL V2.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormViewModel.kt:167</code></summary>

```kotlin
                    state.resourceId,
                    editingId,
                    ChargesPayload(
                        amount = amount,
                        dueDate = dueDate,
                        locale = LOCALE,
                        dateFormat = DateHelper.SHORT_MONTH,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/payloads/GroupLoanPayload.kt`

```kotlin
data class GroupLoanPayload(
```
Created by nellyk on 2/20/2016.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationViewModel.kt:119</code></summary>

```kotlin
        ownerId = state.groupId.toLong(),
        GroupLoanFormSnapshot.serializer(),
        GroupLoanPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/payloads/NotesPayload.kt`

```kotlin
data class NotesPayload(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/payloads/RecurringDepositAccountPayload.kt`

```kotlin
data class RecurringDepositAccountPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/recurringdeposit/impl/RecurringDepositCommandRepositoryImpl.kt:73</code></summary>

```kotlin
 * than a validation point.
 */
private fun RecurringDepositApplicationInput.toPayload() = RecurringDepositAccountPayload(
    clientId = clientId,
    productId = productId,
    submittedOnDate = submittedOnDate,
    locale = locale,
```

</details>

```kotlin
data class ChargeItem(
```
_No KDoc at source._

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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/payloads/SavingsPayload.kt`

```kotlin
class SavingsPayload
```
Created by nellyk on 2/10/2016.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/impl/SavingsCommandRepositoryImpl.kt:253</code></summary>

```kotlin
 * the ViewModel.
 */
private fun SavingsApplicationInput.toPayload() = SavingsPayload().apply {
    productId = this@toPayload.productId
    clientId = this@toPayload.ownerId.takeUnless { isGroup }
    groupId = this@toPayload.ownerId.takeIf { isGroup }
    locale = this@toPayload.locale
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/responses/SaveResponse.kt`

```kotlin
class SaveResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/create/CreateCenterViewModelTest.kt:64</code></summary>

```kotlin
    }

    private class FakeCommands(private val result: () -> MutationResult<SaveResponse>) : CenterCommandRepository {
        val created = mutableListOf<CenterPayload?>()
        override suspend fun createCenter(centerPayload: CenterPayload?): MutationResult<SaveResponse> {
            created += centerPayload
            return result()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/runreport/ColumnHeader.kt`

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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/runreport/DataRow.kt`

```kotlin
data class DataRow(
```
Created by Tarun on 03-08-17.

<details><summary>Used in the template — <code>feature/report/src/commonMain/kotlin/kpt/feature/report/ui/ReportPreviews.kt:81</code></summary>

```kotlin
        .map { ColumnHeader(columnName = it) },
    data = listOf(
        DataRow(listOf("Head Office", "Asha Rahman", "000000011", "12000.00", "8400.00")),
        DataRow(listOf("Head Office", "Bimal Das", "000000021", "8000.00", null)),
        DataRow(listOf("Head Office", "Chitra Sen", "000000034", "15000.00", "15000.00")),
    ),
)
```

</details>

```kotlin
object RowDeserializer : KSerializer<List<String?>>
```
_No KDoc at source._

- `val jsonElement = (decoder as? JsonDecoder)?.decodeJsonElement()`
- `val jsonArray = JsonArray(`

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/runreport/FullParameterListResponse.kt`

```kotlin
data class FullParameterListResponse(
```
Created by Tarun on 03-08-17.

<details><summary>Used in the template — <code>feature/report/src/commonMain/kotlin/kpt/feature/report/ui/ReportCsv.kt:19</code></summary>

```kotlin
 * Each field is quoted when it needs to be, quotes are doubled, and a missing value is empty.
 */
internal fun FullParameterListResponse.toCsv(): String = buildString {
    appendLine(columnHeaders.joinToString(",") { csvField(it.columnName) })
    for (dataRow in data) {
        // Pad short rows: every line has one field per header, so columns cannot shift.
        val cells = columnHeaders.indices.map { dataRow.row.getOrNull(it) }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/runreport/client/ClientReportTypeItem.kt`

```kotlin
data class ClientReportTypeItem(
```
Created by Tarun on 03-08-17.

<details><summary>Used in the template — <code>feature/report/src/commonMain/kotlin/kpt/feature/report/ui/ReportPreviews.kt:33</code></summary>

```kotlin
// i18n:skip — preview fixtures
private val previewReports = listOf(
    ClientReportTypeItem(
        reportId = 1,
        reportName = "Active Loans - Details",
        reportType = "Table",
        reportCategory = "Loan",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/searchrecord/GenericSearchRecord.kt`

```kotlin
data class GenericSearchRecord(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/searchrecord/src/commonMain/kotlin/kpt/feature/searchrecord/navigation/SearchRecordRoute.kt:33</code></summary>

```kotlin
fun NavGraphBuilder.searchRecordDestination(
    onBackClick: () -> Unit,
    onRecordSelected: (GenericSearchRecord) -> Unit,
) {
    composableWithPushTransitions<SearchRecordRoute> {
        SearchRecordScreen(
            onBackClick = onBackClick,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/searchrecord/RecordType.kt`

```kotlin
enum class RecordType
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigation.kt:412</code></summary>

```kotlin
            // landed.
            onAddNewIdentifier = navController::navigateToClientAddIdentifier,
            onNavigateToSearch = { navController.navigateToSearchRecord(RecordType.IDENTIFIER) },
            // The original form's VIEW_DOCUMENT phase, now the shared in-app preview. The scan
            // is a `client_identifiers` document keyed by the CLIENT's id — the original's
            // storage convention, kept so scans it uploaded are still found.
            onViewDocument = { event ->
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/searchrecord/SearchRecordKey.kt`

```kotlin
data class SearchRecordKey(val type: RecordType, val query: String)
```
One record search: which kind of record, and the text to match. In core/model because the screen builds it — a feature sees core/data + core/model only.

<details><summary>Used in the template — <code>feature/searchrecord/src/commonMain/kotlin/kpt/feature/searchrecord/ui/SearchRecordViewModel.kt:68</code></summary>

```kotlin

    /** A blank box searches nothing: the store is asked only for a real query. */
    private val searchKeys = settledQuery.filter { it.isNotBlank() }.map { SearchRecordKey(state.recordType, it) }

    private val stream = repository.searchStream(searchKeys, viewModelScope)

    /** What the screen draws — derived from the input, the settled query and the stream; never assigned. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/surveys/Scorecard.kt`

```kotlin
data class Scorecard(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/survey/SurveyCommandRepository.kt:28</code></summary>

```kotlin
interface SurveyCommandRepository {

    suspend fun submitScore(surveyId: Int, scorecardPayload: Scorecard?): MutationResult<Scorecard>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/surveys/ScorecardValues.kt`

```kotlin
data class ScorecardValues(
```
Created by Nasim Banu on 28,January,2016.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/surveys/Scorecard.kt:26</code></summary>

```kotlin
//    var createdOn: Date? = null,
    var createdOn: List<Int> = emptyList(),
    var scorecardValues: List<ScorecardValues>? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/systems/Code.kt`

```kotlin
class Code
```
Created by ishankhanna on 16/06/14.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addaddress/ClientAddAddressScreen.kt:243</code></summary>

```kotlin

        AppOutlinedTextField(
            value = state.postalCode,
            onValueChange = { onAction(ClientAddAddressAction.UpdatePostalCode(it)) },
            label = stringResource(Res.string.feature_client_postal_code),
            isError = state.errors.postalCode,
            errorText = state.errors.postalCode
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/systems/CodeValue.kt`

```kotlin
class CodeValue
```
Created by ishankhanna on 16/06/14.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeAppliesTo.kt`

```kotlin
data class ChargeAppliesTo(
```
Created by mayankjindal on 13/12/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt:27</code></summary>

```kotlin
    val amount: Double? = null,
    val chargeTimeType: ChargeTimeType? = null,
    val chargeAppliesTo: ChargeAppliesTo? = null,
    val chargeCalculationType: ChargeCalculationType? = null,
    val chargePaymentMode: ChargePaymentMode? = null,
    val incomeOrLiabilityAccount: IncomeOrLiabilityAccount? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeCalculationType.kt`

```kotlin
data class ChargeCalculationType(
```
Created by mayankjindal on 13/12/16.

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:61</code></summary>

```kotlin
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
        penalty = false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt`

```kotlin
data class ChargeOptions(
```
Created by mayankjindal on 13/12/16.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormViewModel.kt:240</code></summary>

```kotlin
private val NEEDS_UNCOLLECTED_INPUT = setOf(ANNUAL_FEE, MONTHLY_FEE, WEEKLY_FEE, OVERDUE_INSTALLMENT)

internal fun ChargeOptions.toChargeType(resource: ChargeResource): ChargeType? {
    val time = chargeTimeType?.id
    val selectable = when (resource) {
        ChargeResource.LOAN -> time in LOAN_TIMES && chargeCalculationType?.id == FLAT
        ChargeResource.CLIENT, ChargeResource.SAVINGS -> time != null && time !in NEEDS_UNCOLLECTED_INPUT
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargePaymentMode.kt`

```kotlin
data class ChargePaymentMode(
```
Created by mayankjindal on 13/12/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt:29</code></summary>

```kotlin
    val chargeAppliesTo: ChargeAppliesTo? = null,
    val chargeCalculationType: ChargeCalculationType? = null,
    val chargePaymentMode: ChargePaymentMode? = null,
    val incomeOrLiabilityAccount: IncomeOrLiabilityAccount? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeTemplate.kt`

```kotlin
data class ChargeTemplate(
```
Created by mayankjindal on 13/12/16.

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:64</code></summary>

```kotlin
        )

    private val catalogue = ChargeTemplate(
        penalty = false,
        chargeOptions = listOf(
            option(1, time = 2),
            option(2, time = 1),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeTimeType.kt`

```kotlin
data class ChargeTimeType(
```
Created by mayankjindal on 13/12/16.

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:60</code></summary>

```kotlin
            name = name,
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/Currency.kt`

```kotlin
data class Currency(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:299</code></summary>

```kotlin
                activeLoans = loans?.activeLoans,
                inArrears = loans?.inArrears,
                loanTotals = loans?.byCurrency,
                savings = (byKey[SAVINGS] as? List<*>)?.filterIsInstance<Money>(),
                groups = byKey[GROUPS] as? Int,
                centers = byKey[CENTERS] as? Int,
            )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/IncomeOrLiabilityAccount.kt`

```kotlin
data class IncomeOrLiabilityAccount(
```
Created by mayankjindal on 13/12/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt:30</code></summary>

```kotlin
    val chargeCalculationType: ChargeCalculationType? = null,
    val chargePaymentMode: ChargePaymentMode? = null,
    val incomeOrLiabilityAccount: IncomeOrLiabilityAccount? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/AccountingRule.kt`

```kotlin
data class AccountingRule(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/AccountingRule.kt:18</code></summary>

```kotlin
 */
@Serializable
data class AccountingRule(
    var id: Int? = null,

    var code: String? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/AllowAttributeOverrides.kt`

```kotlin
data class AllowAttributeOverrides(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/AllowAttributeOverrides.kt:18</code></summary>

```kotlin
 */
@Serializable
data class AllowAttributeOverrides(
    var amortizationType: Boolean? = null,

    var interestType: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/AmortizationType.kt`

```kotlin
data class AmortizationType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:89</code></summary>

```kotlin
        termPeriodFrequencyType = TermPeriodFrequencyType(id = 2, value = "Months"),
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/AmortizationTypeOptions.kt`

```kotlin
data class AmortizationTypeOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationScreenUiTest.kt:39</code></summary>

```kotlin
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        loanPurposeOptions = listOf(LoanPurposeOptions(id = 5, name = "Working capital")),
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/CalendarOptions.kt`

```kotlin
data class CalendarOptions(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/GroupLoanTemplate.kt:99</code></summary>

```kotlin
    var chargeOptions: List<ChargeOptions>? = null,

    var calendarOptions: List<CalendarOptions>? = null,

    var multiDisburseLoan: Boolean? = null,

    var canDefineInstallmentAmount: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/CalendarOptionsType.kt`

```kotlin
data class CalendarOptionsType(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/CalendarOptions.kt:33</code></summary>

```kotlin
    var duration: Int? = null,

    var type: CalendarOptionsType? = null,

    var repeating: Boolean? = null,

    var recurrence: String? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/ChargeAppliesTo.kt`

```kotlin
data class ChargeAppliesTo(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeAppliesTo.kt:18</code></summary>

```kotlin
 */
@Serializable
data class ChargeAppliesTo(
    val id: Int,
    val code: String,
    val value: String,
) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/ChargeCalculationType.kt`

```kotlin
data class ChargeCalculationType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:61</code></summary>

```kotlin
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
        penalty = false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/ChargeOptions.kt`

```kotlin
data class ChargeOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormViewModel.kt:240</code></summary>

```kotlin
private val NEEDS_UNCOLLECTED_INPUT = setOf(ANNUAL_FEE, MONTHLY_FEE, WEEKLY_FEE, OVERDUE_INSTALLMENT)

internal fun ChargeOptions.toChargeType(resource: ChargeResource): ChargeType? {
    val time = chargeTimeType?.id
    val selectable = when (resource) {
        ChargeResource.LOAN -> time in LOAN_TIMES && chargeCalculationType?.id == FLAT
        ChargeResource.CLIENT, ChargeResource.SAVINGS -> time != null && time !in NEEDS_UNCOLLECTED_INPUT
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/ChargePaymentMode.kt`

```kotlin
data class ChargePaymentMode(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt:29</code></summary>

```kotlin
    val chargeAppliesTo: ChargeAppliesTo? = null,
    val chargeCalculationType: ChargeCalculationType? = null,
    val chargePaymentMode: ChargePaymentMode? = null,
    val incomeOrLiabilityAccount: IncomeOrLiabilityAccount? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/ChargeTimeType.kt`

```kotlin
data class ChargeTimeType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:60</code></summary>

```kotlin
            name = name,
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/Charges.kt`

```kotlin
data class Charges(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/ui/ChargeUi.kt:43</code></summary>

```kotlin
)

internal fun Charges.toUi(): ChargeUi {
    fun money(value: Double?) = value?.let { CurrencyFormatter.format(it, currency?.code, currency?.decimalPlaces) }
        ?: PLACEHOLDER
    return ChargeUi(
        id = id,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/Currency.kt`

```kotlin
data class Currency(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:299</code></summary>

```kotlin
                activeLoans = loans?.activeLoans,
                inArrears = loans?.inArrears,
                loanTotals = loans?.byCurrency,
                savings = (byKey[SAVINGS] as? List<*>)?.filterIsInstance<Money>(),
                groups = byKey[GROUPS] as? Int,
                centers = byKey[CENTERS] as? Int,
            )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/DaysInMonthType.kt`

```kotlin
class DaysInMonthType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:122</code></summary>

```kotlin
    val canDisburse: Boolean? = null,
    val product: Product? = null,
    val daysInMonthType: DaysInMonthType? = null,
    val daysInYearType: DaysInYearType? = null,

    val isInterestRecalculationEnabled: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/DaysInYearType.kt`

```kotlin
data class DaysInYearType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:123</code></summary>

```kotlin
    val product: Product? = null,
    val daysInMonthType: DaysInMonthType? = null,
    val daysInYearType: DaysInYearType? = null,

    val isInterestRecalculationEnabled: Boolean? = null,

    val isvaliableInstallmentsAllowed: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/EntityType.kt`

```kotlin
data class EntityType(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>feature/document/src/commonMain/kotlin/kpt/feature/document/list/DocumentListScreenPreview.kt:29</code></summary>

```kotlin
    Document(
        id = 1,
        parentEntityType = "groups",
        parentEntityId = 11,
        name = "Meeting minutes",
        fileName = "minutes-oct.pdf",
        size = 182_000,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/Frequency.kt`

```kotlin
data class Frequency(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationViewModel.kt:419</code></summary>

```kotlin
    interestRatePerPeriod?.let { put(GroupLoanField.NOMINAL_INTEREST, it.toString()) }
    repaymentEvery?.let { put(GroupLoanField.REPAID_EVERY, it.toString()) }
    termFrequency?.let { put(GroupLoanField.LOAN_TERM, it.toString()) }
}

/** A selectable loan product. */
@Serializable
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/FundOptions.kt`

```kotlin
data class FundOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationScreenUiTest.kt:49</code></summary>

```kotlin
            TransactionProcessingStrategyOptions(id = 4, code = "mifos-standard-strategy", name = "Standard"),
        ),
        fundOptions = listOf(FundOptions(id = 9, name = "Donor fund")),
    )

    private fun onStep(step: LoanStep, schedule: RepaymentSchedule? = null) =
        NewLoanApplicationState(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/Group.kt`

```kotlin
data class Group(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:212</code></summary>

```kotlin
                ActivateType.Client -> Res.string.feature_activate_client
                ActivateType.Center -> Res.string.feature_activate_center
                ActivateType.Group -> Res.string.feature_activate_group
            },
        )

        ActivateResult.Failed -> listOfNotNull(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/GroupLoanTemplate.kt`

```kotlin
data class GroupLoanTemplate(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationScreen.kt:295</code></summary>

```kotlin
private fun ApplicationForm(
    state: GroupLoanApplicationState,
    template: GroupLoanTemplate,
    onAction: (GroupLoanApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/GroupTimeline.kt`

```kotlin
data class GroupTimeline(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/Group.kt:41</code></summary>

```kotlin
    var groupLevel: Int? = null,

    var timeline: GroupTimeline? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestCalculationPeriodType.kt`

```kotlin
data class InterestCalculationPeriodType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationScreenUiTest.kt:45</code></summary>

```kotlin
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        interestCalculationPeriodTypeOptions = listOf(InterestCalculationPeriodType(id = 1, value = "Daily")),
        transactionProcessingStrategyOptions = listOf(
            TransactionProcessingStrategyOptions(id = 4, code = "mifos-standard-strategy", name = "Standard"),
        ),
        fundOptions = listOf(FundOptions(id = 9, name = "Donor fund")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestCalculationPeriodTypeOptions.kt`

```kotlin
data class InterestCalculationPeriodTypeOptions(
```
Created by Rajan Maurya on 16/07/16.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRateFrequencyType.kt`

```kotlin
data class InterestRateFrequencyType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanTypeConverters.kt:80</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestRateFrequencyType(type: InterestRateFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRateFrequencyTypeOptions.kt`

```kotlin
data class InterestRateFrequencyTypeOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:106</code></summary>

```kotlin
    val repaymentFrequencyNthDayTypeOptions: List<RepaymentFrequencyNthDayTypeOptions> = emptyList(),
    val repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions> = emptyList(),
    val interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions> = emptyList(),
    val amortizationTypeOptions: List<AmortizationTypeOptions> = emptyList(),
    val interestTypeOptions: List<InterestTypeOptions> = emptyList(),
    val interestCalculationPeriodTypeOptions: List<InterestCalculationPeriodType> = emptyList(),
    val transactionProcessingStrategyOptions: List<TransactionProcessingStrategyOptions> = emptyList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationCompoundingType.kt`

```kotlin
data class InterestRecalculationCompoundingType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationData.kt:23</code></summary>

```kotlin
    var productId: Int? = null,

    var interestRecalculationCompoundingType: InterestRecalculationCompoundingType? = null,

    var rescheduleStrategyType: RescheduleStrategyType? = null,

    var recalculationRestFrequencyType: RecalculationRestFrequencyType? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationData.kt`

```kotlin
data class InterestRecalculationData(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/GroupLoanTemplate.kt:121</code></summary>

```kotlin
    var isInterestRecalculationEnabled: Boolean? = null,

    var interestRecalculationData: InterestRecalculationData? = null,

    var isVariableInstallmentsAllowed: Boolean? = null,

    var minimumGap: Int? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestType.kt`

```kotlin
data class InterestType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:90</code></summary>

```kotlin
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestTypeOptions.kt`

```kotlin
data class InterestTypeOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationScreenUiTest.kt:40</code></summary>

```kotlin
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        loanPurposeOptions = listOf(LoanPurposeOptions(id = 5, name = "Working capital")),
    )

    private fun filled() = GroupLoanApplicationState(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/LoanCollateralOptions.kt`

```kotlin
data class LoanCollateralOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:112</code></summary>

```kotlin
    val transactionProcessingStrategyOptions: List<TransactionProcessingStrategyOptions> = emptyList(),
    val chargeOptions: List<ChargeOptions> = emptyList(),
    val loanCollateralOptions: List<LoanCollateralOptions> = emptyList(),

    val multiDisburseLoan: Boolean? = null,

    val transactionProcessingStrategyCode: String? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/LoanOfficerOption.kt`

```kotlin
data class LoanOfficerOption(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:99</code></summary>

```kotlin

    val dataTables: ArrayList<DataTableEntity> = ArrayList(),
    val loanOfficerOptions: List<LoanOfficerOption> = emptyList(),
    val loanPurposeOptions: List<LoanPurposeOptions> = emptyList(),
    val fundOptions: List<FundOptions> = emptyList(),
    val termFrequencyTypeOptions: List<TermFrequencyTypeOptions> = emptyList(),
    val repaymentFrequencyTypeOptions: List<RepaymentFrequencyTypeOptions> = emptyList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/LoanPurposeOptions.kt`

```kotlin
data class LoanPurposeOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationScreenUiTest.kt:41</code></summary>

```kotlin
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        loanPurposeOptions = listOf(LoanPurposeOptions(id = 5, name = "Working capital")),
    )

    private fun filled() = GroupLoanApplicationState(
        groupId = 31,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/OverdueCharges.kt`

```kotlin
class OverdueCharges
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/GroupLoanTemplate.kt:113</code></summary>

```kotlin
    var product: Product? = null,

    var overdueCharges: List<OverdueCharges>? = null,

    var daysInMonthType: DaysInMonthType? = null,

    var daysInYearType: DaysInYearType? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/PreClosureInterestCalculationStrategy.kt`

```kotlin
data class PreClosureInterestCalculationStrategy(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationData.kt:35</code></summary>

```kotlin
    var isCompoundingToBePostedAsTransaction: Boolean? = null,

    var preClosureInterestCalculationStrategy: PreClosureInterestCalculationStrategy? = null,

    var allowCompoundingOnEod: Boolean? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/Product.kt`

```kotlin
data class Product(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/depositaccounts/ClientDepositAccountsScreen.kt:262</code></summary>

```kotlin
            AppActionsSavingsListingComponent(
                accountNo = account.accountNo.orEmpty(),
                savingsProduct = productLabel,
                savingsProductName = account.productName.orEmpty(),
                balance = account.accountBalance
                    ?.let { CurrencyFormatter.format(it, account.currency?.code, account.currency?.decimalPlaces) }
                    ?: notAvailable,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/ProductOptions.kt`

```kotlin
data class ProductOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:131</code></summary>

```kotlin
        clientsTemplate.officeOptions?.let { insertOfficeOptions(it) }
        clientsTemplate.staffOptions?.let { insertStaffOptions(it) }
        clientsTemplate.savingProductOptions?.let { insertSavingProductOptions(it) }
        clientsTemplate.genderOptions?.forEach { insertOption(it.copy(optionType = GENDER_OPTIONS)) }
        clientsTemplate.clientTypeOptions?.forEach { insertOption(it.copy(optionType = CLIENT_TYPE_OPTIONS)) }
        clientsTemplate.clientClassificationOptions?.forEach {
            insertOption(it.copy(optionType = CLIENT_CLASSIFICATION_OPTIONS))
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/RecalculationRestFrequencyType.kt`

```kotlin
data class RecalculationRestFrequencyType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationData.kt:27</code></summary>

```kotlin
    var rescheduleStrategyType: RescheduleStrategyType? = null,

    var recalculationRestFrequencyType: RecalculationRestFrequencyType? = null,

    var recalculationRestFrequencyInterval: Int? = null,

    var isArrearsBasedOnOriginalSchedule: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/RepaymentFrequencyDaysOfWeekTypeOptions.kt`

```kotlin
data class RepaymentFrequencyDaysOfWeekTypeOptions(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:105</code></summary>

```kotlin
    val repaymentFrequencyTypeOptions: List<RepaymentFrequencyTypeOptions> = emptyList(),
    val repaymentFrequencyNthDayTypeOptions: List<RepaymentFrequencyNthDayTypeOptions> = emptyList(),
    val repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions> = emptyList(),
    val interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions> = emptyList(),
    val amortizationTypeOptions: List<AmortizationTypeOptions> = emptyList(),
    val interestTypeOptions: List<InterestTypeOptions> = emptyList(),
    val interestCalculationPeriodTypeOptions: List<InterestCalculationPeriodType> = emptyList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/RepaymentFrequencyNthDayTypeOptions.kt`

```kotlin
data class RepaymentFrequencyNthDayTypeOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:104</code></summary>

```kotlin
    val termFrequencyTypeOptions: List<TermFrequencyTypeOptions> = emptyList(),
    val repaymentFrequencyTypeOptions: List<RepaymentFrequencyTypeOptions> = emptyList(),
    val repaymentFrequencyNthDayTypeOptions: List<RepaymentFrequencyNthDayTypeOptions> = emptyList(),
    val repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions> = emptyList(),
    val interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions> = emptyList(),
    val amortizationTypeOptions: List<AmortizationTypeOptions> = emptyList(),
    val interestTypeOptions: List<InterestTypeOptions> = emptyList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/RepaymentFrequencyType.kt`

```kotlin
data class RepaymentFrequencyType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:88</code></summary>

```kotlin
        interestRatePerPeriod = 2.5,
        termPeriodFrequencyType = TermPeriodFrequencyType(id = 2, value = "Months"),
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
        transactionProcessingStrategyCode = "mifos-standard-strategy",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/RepaymentFrequencyTypeOptions.kt`

```kotlin
data class RepaymentFrequencyTypeOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationScreenUiTest.kt:38</code></summary>

```kotlin
    private val template = GroupLoanTemplate(
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        loanPurposeOptions = listOf(LoanPurposeOptions(id = 5, name = "Working capital")),
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/RepeatsOnNthDayOfMonth.kt`

```kotlin
data class RepeatsOnNthDayOfMonth(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/CalendarOptions.kt:43</code></summary>

```kotlin
    var interval: Int? = null,

    var repeatsOnNthDayOfMonth: RepeatsOnNthDayOfMonth? = null,

    var firstReminder: Int? = null,

    var secondReminder: Int? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/RescheduleStrategyType.kt`

```kotlin
data class RescheduleStrategyType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationData.kt:25</code></summary>

```kotlin
    var interestRecalculationCompoundingType: InterestRecalculationCompoundingType? = null,

    var rescheduleStrategyType: RescheduleStrategyType? = null,

    var recalculationRestFrequencyType: RecalculationRestFrequencyType? = null,

    var recalculationRestFrequencyInterval: Int? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/Status.kt`

```kotlin
data class Status(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/identifiers/IdentifierDocumentKey.kt:30</code></summary>

```kotlin
 * including `"null"` for an absent part, so documents uploaded by the original app still match.
 */
internal fun identifierDocumentKey(documentTypeName: String?, documentKey: String?, status: Status?): String =
    "$documentTypeName$documentKey$status"

/** The key without its status — what still names the identifier after its status changes. */
internal fun identifierDocumentKeyPrefix(documentTypeName: String?, documentKey: String?): String =
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/TaxGroup.kt`

```kotlin
data class TaxGroup(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/ChargeOptions.kt:39</code></summary>

```kotlin
    var chargePaymentMode: ChargePaymentMode? = null,

    var taxGroup: TaxGroup? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/TermFrequencyTypeOptions.kt`

```kotlin
data class TermFrequencyTypeOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/grouploan/GroupLoanApplicationScreenUiTest.kt:37</code></summary>

```kotlin

    private val template = GroupLoanTemplate(
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        loanPurposeOptions = listOf(LoanPurposeOptions(id = 5, name = "Working capital")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/TermPeriodFrequencyType.kt`

```kotlin
data class TermPeriodFrequencyType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationViewModelTest.kt:87</code></summary>

```kotlin
        repaymentEvery = 1,
        interestRatePerPeriod = 2.5,
        termPeriodFrequencyType = TermPeriodFrequencyType(id = 2, value = "Months"),
        repaymentFrequencyType = RepaymentFrequencyType(id = 1, value = "Weeks"),
        amortizationType = AmortizationType(id = 1, value = "Equal instalments"),
        interestType = InterestType(id = 0, value = "Declining balance"),
        interestCalculationPeriodType = InterestCalculationPeriodType(id = 1, value = "Daily"),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/Timeline.kt`

```kotlin
data class Timeline(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/editdetails/ClientEditDetailsFormTest.kt:189</code></summary>

```kotlin
            legalForm = ClientStatus(id = 1, value = "PERSON"),
            dateOfBirth = listOf(1815, 12, 10),
            timeline = Timeline(submittedOnDate = listOf(2026, 1, 9)),
        ).asPrefill()

        assertEquals("Ada", prefilled.firstName)
        assertEquals("000000042", prefilled.accountNo)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/TransactionProcessingStrategyOptions.kt`

```kotlin
data class TransactionProcessingStrategyOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationScreenUiTest.kt:47</code></summary>

```kotlin
        interestCalculationPeriodTypeOptions = listOf(InterestCalculationPeriodType(id = 1, value = "Daily")),
        transactionProcessingStrategyOptions = listOf(
            TransactionProcessingStrategyOptions(id = 4, code = "mifos-standard-strategy", name = "Standard"),
        ),
        fundOptions = listOf(FundOptions(id = 9, name = "Donor fund")),
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/Type.kt`

```kotlin
data class Type(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/detail/ChargeDetailScreen.kt:72</code></summary>

```kotlin
internal fun ChargeDetailScreen(
    onBackPressed: () -> Unit,
    onEditCharge: (resourceType: String, resourceId: Int, chargeId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChargeDetailViewModel = koinViewModel(),
) {
    val charge by viewModel.charge.collectAsStateWithLifecycle()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt`

```kotlin
data class AccountChart(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/AccountChart.kt:15</code></summary>

```kotlin

@Serializable
data class AccountChart(
    val accountId: Int? = null,
    val accountNumber: String? = null,
    val chartSlabs: List<ChartSlab>? = null,
    val endDate: List<Int>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AttributeName.kt`

```kotlin
data class AttributeName(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/Incentive.kt:19</code></summary>

```kotlin
data class Incentive(
    val amount: Double? = null,
    val attributeName: AttributeName? = null,
    val attributeValue: String? = null,
    val attributeValueDesc: String? = null,
    val conditionType: ConditionType? = null,
    val entityType: EntityType? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AttributeNameOption.kt`

```kotlin
data class AttributeNameOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:17</code></summary>

```kotlin
@Serializable
data class AccountChart(
    val attributeNameOptions: List<AttributeNameOption>? = null,
    val chartSlabs: List<ChartSlab>? = null,
//    val clientClassificationOptions: List<Any?>? = null,
    val clientTypeOptions: List<ClientTypeOption>? = null,
    val conditionTypeOptions: List<ConditionTypeOption>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ChartSlab.kt`

```kotlin
data class ChartSlab(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:18</code></summary>

```kotlin
data class AccountChart(
    val attributeNameOptions: List<AttributeNameOption>? = null,
    val chartSlabs: List<ChartSlab>? = null,
//    val clientClassificationOptions: List<Any?>? = null,
    val clientTypeOptions: List<ClientTypeOption>? = null,
    val conditionTypeOptions: List<ConditionTypeOption>? = null,
    val endDate: List<Int>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ClientTypeOption.kt`

```kotlin
data class ClientTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:20</code></summary>

```kotlin
    val chartSlabs: List<ChartSlab>? = null,
//    val clientClassificationOptions: List<Any?>? = null,
    val clientTypeOptions: List<ClientTypeOption>? = null,
    val conditionTypeOptions: List<ConditionTypeOption>? = null,
    val endDate: List<Int>? = null,
    val entityTypeOptions: List<EntityTypeOption>? = null,
    val fromDate: List<Int>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ConditionType.kt`

```kotlin
data class ConditionType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/Incentive.kt:22</code></summary>

```kotlin
    val attributeValue: String? = null,
    val attributeValueDesc: String? = null,
    val conditionType: ConditionType? = null,
    val entityType: EntityType? = null,
    val incentiveType: IncentiveType? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ConditionTypeOption.kt`

```kotlin
data class ConditionTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:21</code></summary>

```kotlin
//    val clientClassificationOptions: List<Any?>? = null,
    val clientTypeOptions: List<ClientTypeOption>? = null,
    val conditionTypeOptions: List<ConditionTypeOption>? = null,
    val endDate: List<Int>? = null,
    val entityTypeOptions: List<EntityTypeOption>? = null,
    val fromDate: List<Int>? = null,
//    val genderOptions: List<Any?>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/Currency.kt`

```kotlin
data class Currency(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:299</code></summary>

```kotlin
                activeLoans = loans?.activeLoans,
                inArrears = loans?.inArrears,
                loanTotals = loans?.byCurrency,
                savings = (byKey[SAVINGS] as? List<*>)?.filterIsInstance<Money>(),
                groups = byKey[GROUPS] as? Int,
                centers = byKey[CENTERS] as? Int,
            )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/EntityType.kt`

```kotlin
data class EntityType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/document/src/commonMain/kotlin/kpt/feature/document/list/DocumentListScreenPreview.kt:29</code></summary>

```kotlin
    Document(
        id = 1,
        parentEntityType = "groups",
        parentEntityId = 11,
        name = "Meeting minutes",
        fileName = "minutes-oct.pdf",
        size = 182_000,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/EntityTypeOption.kt`

```kotlin
data class EntityTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:23</code></summary>

```kotlin
    val conditionTypeOptions: List<ConditionTypeOption>? = null,
    val endDate: List<Int>? = null,
    val entityTypeOptions: List<EntityTypeOption>? = null,
    val fromDate: List<Int>? = null,
//    val genderOptions: List<Any?>? = null,
    val incentiveTypeOptions: List<IncentiveTypeOption>? = null,
    val isPrimaryGroupingByAmount: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/FieldOfficerOption.kt`

```kotlin
data class FieldOfficerOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:68</code></summary>

```kotlin
            ProductOption(id = null, name = "Draft RD"),
        ),
        fieldOfficerOptions = listOf(FieldOfficerOption(id = 9, displayName = "Officer A")),
        periodFrequencyTypeOptions = listOf(
            PeriodFrequencyTypeOption(id = 2, value = "Months"),
            PeriodFrequencyTypeOption(id = 1, value = "Weeks"),
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/MaturityInstructionOption.kt`

```kotlin
data class MaturityInstructionOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/savings/FixedDepositTemplate.kt:60</code></summary>

```kotlin

    val lockinPeriodFrequencyTypeOptions: List<LockinPeriodFrequencyTypeOption>? = null,
    val maturityInstructionOptions: List<MaturityInstructionOption>? = null,
    val accountChart: AccountChart? = null,

    val chargeOptions: List<ChargeOption>? = null,
    val savingsAccounts: List<SavingsAccountOption>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/Timeline.kt`

```kotlin
class Timeline(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/editdetails/ClientEditDetailsFormTest.kt:189</code></summary>

```kotlin
            legalForm = ClientStatus(id = 1, value = "PERSON"),
            dateOfBirth = listOf(1815, 12, 10),
            timeline = Timeline(submittedOnDate = listOf(2026, 1, 9)),
        ).asPrefill()

        assertEquals("Ada", prefilled.firstName)
        assertEquals("000000042", prefilled.accountNo)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/WithdrawalFeeTypeOption.kt`

```kotlin
data class WithdrawalFeeTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDepositAccountTemplate.kt:74</code></summary>

```kotlin
    val withHoldTax: Boolean? = null,
    val withdrawalFeeForTransfers: Boolean? = null,
    val withdrawalFeeTypeOptions: List<WithdrawalFeeTypeOption>? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/charge/ChargeAppliesTo.kt`

```kotlin
data class ChargeAppliesTo(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeAppliesTo.kt:18</code></summary>

```kotlin
 */
@Serializable
data class ChargeAppliesTo(
    val id: Int,
    val code: String,
    val value: String,
) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/charge/ChargeCalculationType.kt`

```kotlin
data class ChargeCalculationType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:61</code></summary>

```kotlin
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
        penalty = false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/charge/ChargeOption.kt`

```kotlin
data class ChargeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:83</code></summary>

```kotlin
            PreClosurePenalInterestOnTypeOption(id = 1, value = "Whole term"),
        ),
        chargeOptions = listOf(ChargeOption(id = 11, name = "Account maintenance")),
    )

    private fun viewModel(
        commands: FakeRecurringDepositCommandRepository = FakeRecurringDepositCommandRepository(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/charge/ChargePaymentMode.kt`

```kotlin
data class ChargePaymentMode(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt:29</code></summary>

```kotlin
    val chargeAppliesTo: ChargeAppliesTo? = null,
    val chargeCalculationType: ChargeCalculationType? = null,
    val chargePaymentMode: ChargePaymentMode? = null,
    val incomeOrLiabilityAccount: IncomeOrLiabilityAccount? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/charge/ChargeTimeType.kt`

```kotlin
data class ChargeTimeType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonTest/kotlin/kpt/feature/charge/ChargeFormViewModelTest.kt:60</code></summary>

```kotlin
            name = name,
            amount = amount,
            chargeTimeType = ChargeTimeType(id = time, code = "c", value = "time $time"),
            chargeCalculationType = ChargeCalculationType(id = calculation, code = "c", value = "calc $calculation"),
        )

    private val catalogue = ChargeTemplate(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/DepositType.kt`

```kotlin
data class DepositType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/converter/CenterTypeConverters.kt:33</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromDepositType(type: SavingAccountDepositTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/InMultiplesOfDepositTermType.kt`

```kotlin
data class InMultiplesOfDepositTermType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt:43</code></summary>

```kotlin
    val fieldOfficerName: String? = null,
    val id: Int? = null,
    val inMultiplesOfDepositTermType: InMultiplesOfDepositTermType? = null,
    val interestCalculationDaysInYearType: InterestCalculationDaysInYearType? = null,
    val interestCalculationType: InterestCalculationType? = null,
    val interestCompoundingPeriodType: InterestCompoundingPeriodType? = null,
    val interestPostingPeriodType: InterestPostingPeriodType? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/MaxDepositTermType.kt`

```kotlin
data class MaxDepositTermType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt:55</code></summary>

```kotlin
    val maturityAmount: Double? = null,
    val maturityDate: List<Int>? = null,
    val maxDepositTermType: MaxDepositTermType? = null,
    val minDepositTerm: Int? = null,
    val minDepositTermType: MinDepositTermType? = null,
    val nominalAnnualInterestRate: Double? = null,
    val preClosurePenalApplicable: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/MinDepositTermType.kt`

```kotlin
data class MinDepositTermType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt:57</code></summary>

```kotlin
    val maxDepositTermType: MaxDepositTermType? = null,
    val minDepositTerm: Int? = null,
    val minDepositTermType: MinDepositTermType? = null,
    val nominalAnnualInterestRate: Double? = null,
    val preClosurePenalApplicable: Boolean? = null,
    val recurringFrequency: Int? = null,
    val recurringFrequencyType: RecurringFrequencyType? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/Incentive.kt`

```kotlin
data class Incentive(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ChartSlab.kt:22</code></summary>

```kotlin
    val description: String? = null,
    val fromPeriod: Int? = null,
    val incentives: List<Incentive>? = null,
    val periodType: PeriodType? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/IncentiveType.kt`

```kotlin
data class IncentiveType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/Incentive.kt:24</code></summary>

```kotlin
    val conditionType: ConditionType? = null,
    val entityType: EntityType? = null,
    val incentiveType: IncentiveType? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/IncentiveTypeOption.kt`

```kotlin
data class IncentiveTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:26</code></summary>

```kotlin
    val fromDate: List<Int>? = null,
//    val genderOptions: List<Any?>? = null,
    val incentiveTypeOptions: List<IncentiveTypeOption>? = null,
    val isPrimaryGroupingByAmount: Boolean? = null,
    val name: String? = null,
    val description: String? = null,
    val periodTypes: List<PeriodType>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/InterestCalculationDaysInYearType.kt`

```kotlin
data class InterestCalculationDaysInYearType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:46</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestCalculationDaysInYearType(
        type: InterestCalculationDaysInYearType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/InterestCalculationDaysInYearTypeOption.kt`

```kotlin
data class InterestCalculationDaysInYearTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:78</code></summary>

```kotlin
        interestCalculationTypeOptions = listOf(InterestCalculationTypeOption(id = 1, value = "Daily Balance")),
        interestCalculationDaysInYearTypeOptions = listOf(
            InterestCalculationDaysInYearTypeOption(id = 365, value = "365 Days"),
        ),
        preClosurePenalInterestOnTypeOptions = listOf(
            PreClosurePenalInterestOnTypeOption(id = 1, value = "Whole term"),
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/InterestCalculationType.kt`

```kotlin
data class InterestCalculationType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:58</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestCalculationType(type: InterestCalculationType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/InterestCalculationTypeOption.kt`

```kotlin
data class InterestCalculationTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:76</code></summary>

```kotlin
        interestCompoundingPeriodTypeOptions = listOf(InterestCompoundingPeriodTypeOption(id = 1, value = "Daily")),
        interestPostingPeriodTypeOptions = listOf(InterestPostingPeriodTypeOption(id = 4, value = "Monthly")),
        interestCalculationTypeOptions = listOf(InterestCalculationTypeOption(id = 1, value = "Daily Balance")),
        interestCalculationDaysInYearTypeOptions = listOf(
            InterestCalculationDaysInYearTypeOption(id = 365, value = "365 Days"),
        ),
        preClosurePenalInterestOnTypeOptions = listOf(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/InterestCompoundingPeriodType.kt`

```kotlin
data class InterestCompoundingPeriodType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:68</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestCompoundingPeriodType(
        type: InterestCompoundingPeriodType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/InterestCompoundingPeriodTypeOption.kt`

```kotlin
data class InterestCompoundingPeriodTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:74</code></summary>

```kotlin
        ),
        lockinPeriodFrequencyTypeOptions = listOf(LockinPeriodFrequencyTypeOption(id = 2, value = "Months")),
        interestCompoundingPeriodTypeOptions = listOf(InterestCompoundingPeriodTypeOption(id = 1, value = "Daily")),
        interestPostingPeriodTypeOptions = listOf(InterestPostingPeriodTypeOption(id = 4, value = "Monthly")),
        interestCalculationTypeOptions = listOf(InterestCalculationTypeOption(id = 1, value = "Daily Balance")),
        interestCalculationDaysInYearTypeOptions = listOf(
            InterestCalculationDaysInYearTypeOption(id = 365, value = "365 Days"),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/InterestPostingPeriodType.kt`

```kotlin
data class InterestPostingPeriodType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:25</code></summary>

```kotlin
class SavingsPeriodTypeConverters {
    @ColumnTypeConverter
    fun fromInterestPostingPeriodType(
        type: InterestPostingPeriodType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/InterestPostingPeriodTypeOption.kt`

```kotlin
data class InterestPostingPeriodTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:75</code></summary>

```kotlin
        lockinPeriodFrequencyTypeOptions = listOf(LockinPeriodFrequencyTypeOption(id = 2, value = "Months")),
        interestCompoundingPeriodTypeOptions = listOf(InterestCompoundingPeriodTypeOption(id = 1, value = "Daily")),
        interestPostingPeriodTypeOptions = listOf(InterestPostingPeriodTypeOption(id = 4, value = "Monthly")),
        interestCalculationTypeOptions = listOf(InterestCalculationTypeOption(id = 1, value = "Daily Balance")),
        interestCalculationDaysInYearTypeOptions = listOf(
            InterestCalculationDaysInYearTypeOption(id = 365, value = "365 Days"),
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/PreClosurePenalInterestOnTypeOption.kt`

```kotlin
data class PreClosurePenalInterestOnTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:81</code></summary>

```kotlin
        ),
        preClosurePenalInterestOnTypeOptions = listOf(
            PreClosurePenalInterestOnTypeOption(id = 1, value = "Whole term"),
        ),
        chargeOptions = listOf(ChargeOption(id = 11, name = "Account maintenance")),
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/period/LockinPeriodFrequencyType.kt`

```kotlin
data class LockinPeriodFrequencyType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:39</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromLockinPeriodFrequencyType(type: LockinPeriodFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/period/LockinPeriodFrequencyTypeOption.kt`

```kotlin
data class LockinPeriodFrequencyTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:73</code></summary>

```kotlin
            PeriodFrequencyTypeOption(id = 1, value = "Weeks"),
        ),
        lockinPeriodFrequencyTypeOptions = listOf(LockinPeriodFrequencyTypeOption(id = 2, value = "Months")),
        interestCompoundingPeriodTypeOptions = listOf(InterestCompoundingPeriodTypeOption(id = 1, value = "Daily")),
        interestPostingPeriodTypeOptions = listOf(InterestPostingPeriodTypeOption(id = 4, value = "Monthly")),
        interestCalculationTypeOptions = listOf(InterestCalculationTypeOption(id = 1, value = "Daily Balance")),
        interestCalculationDaysInYearTypeOptions = listOf(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/period/PeriodFrequencyTypeOption.kt`

```kotlin
data class PeriodFrequencyTypeOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:70</code></summary>

```kotlin
        fieldOfficerOptions = listOf(FieldOfficerOption(id = 9, displayName = "Officer A")),
        periodFrequencyTypeOptions = listOf(
            PeriodFrequencyTypeOption(id = 2, value = "Months"),
            PeriodFrequencyTypeOption(id = 1, value = "Weeks"),
        ),
        lockinPeriodFrequencyTypeOptions = listOf(LockinPeriodFrequencyTypeOption(id = 2, value = "Months")),
        interestCompoundingPeriodTypeOptions = listOf(InterestCompoundingPeriodTypeOption(id = 1, value = "Daily")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/period/PeriodType.kt`

```kotlin
data class PeriodType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/fixeddepositapplication/FixedDepositApplicationScreen.kt:281</code></summary>

```kotlin
        val periods = template.periodFrequencyTypeOptions.orEmpty()
        OptionDropdown(
            value = state.depositPeriodType?.label.orEmpty(),
            label = stringResource(Res.string.feature_client_fdapp_deposit_period_type),
            labels = periods.mapNotNull { it.value },
            error = state.depositPeriodTypeError
                .errorOr(Res.string.feature_client_fdapp_error_period_type_required),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/period/ProductOption.kt`

```kotlin
data class ProductOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:64</code></summary>

```kotlin
    private fun template() = RecurringDepositAccountTemplate(
        productOptions = listOf(
            ProductOption(id = 4, name = "Monthly RD"),
            // Unsendable: no id. Must not be offered.
            ProductOption(id = null, name = "Draft RD"),
        ),
        fieldOfficerOptions = listOf(FieldOfficerOption(id = 9, displayName = "Officer A")),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/saving/AccountOptions.kt`

```kotlin
data class AccountOptions(
```
Created by rajan on 13/3/16.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/defaultaccount/ClientDefaultAccountViewModel.kt:155</code></summary>

```kotlin
     */
    val accounts: StateFlow<ScreenState<List<SavingAccountOption>>> = templateStream.state
        .mapContent { template, _ -> template.savingAccountOptions }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /**
     * The account that is the client's default RIGHT NOW, or `null` when it is unknown.
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/saving/TagId.kt`

```kotlin
data class TagId(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/saving/AccountOptions.kt:40</code></summary>

```kotlin
    var nameDecorated: String? = null,

    var tagId: TagId? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/users/User.kt`

```kotlin
data class User(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:95</code></summary>

```kotlin
        """{"developerMessage":"The user associated with credentials passed on this request does not have sufficient privileges to perform this action.",""" +
        """"httpStatusCode":"403","defaultUserMessage":"Insufficient privileges to perform this action.",""" +
        """"userMessageGlobalisationCode":"error.msg.not.authorized","errors":[{"developerMessage":"User has no authority to READ collectionsheets",""" +
        """"defaultUserMessage":"User has no authority to READ collectionsheets","userMessageGlobalisationCode":"error.msg.not.authorized","parameterName":"id","args":[]}]}""" + "\"",
)

private fun content(card: HomeCard): ScreenState<HomeCard> = ScreenState.Content(card)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/users/UserLatLng.kt`

```kotlin
data class UserLatLng(
```
Created by Rajan Maurya on 24/01/17.

<details><summary>Used in the template — <code>feature/pathtracking/src/commonMain/kotlin/kpt/feature/pathtracking/ui/PathTrackingPreviews.kt:24</code></summary>

```kotlin
    TrackedPath(
        points = listOf(
            UserLatLng(19.4326, -99.1332),
            UserLatLng(19.4340, -99.1405),
            UserLatLng(19.4282, -99.1446),
            UserLatLng(19.4231, -99.1580),
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/users/UserLocation.kt`

```kotlin
data class UserLocation(
```
Created by Rajan Maurya on 24/01/17.

<details><summary>Used in the template — <code>feature/datatable/src/commonTest/kotlin/kpt/feature/datatable/rows/DataTableRowsTest.kt:157</code></summary>

```kotlin
        ) = error("not used here")

        override suspend fun addUserPathTracking(userId: Int, userLocation: UserLocation?) =
            error("not used here")
    }

    private fun viewModel(commands: FakeCommands = FakeCommands()) = DataTableRowsViewModel(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/office/Office.kt`

```kotlin
data class Office(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `OfficeEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/create/CreateCenterScreen.kt:106</code></summary>

```kotlin
internal fun CreateCenterContent(
    state: CreateCenterState,
    offices: ScreenState<List<Office>>,
    onAction: (CreateCenterAction) -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    offeredDraft: SavedFormDraft? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/office/OfficeOpeningDate.kt`

```kotlin
data class OfficeOpeningDate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `OfficeOpeningDateEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/office/converter/OfficeTypeConverters.kt:26</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromOfficeOpeningDate(status: OfficeOpeningDateEntity?): String? {
        return status?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/offline/DownloadProgress.kt`

```kotlin
data class DownloadProgress(
```
Progress of an offline download — centers or groups the officer selected, walked down to every client and active account they hold, so those screens open with no connection.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/list/CenterListScreenPreview.kt:84</code></summary>

```kotlin
            ui = CenterListState(
                // i18n:skip — preview fixture
                download = kpt.core.model.offline.DownloadProgress(
                    completed = 2,
                    total = 2,
                    records = 37,
                    finished = true,
```

</details>

```kotlin
data class DownloadFailure(val item: String, val reason: String?)
```
One thing that could not be downloaded: what it was, and the server's reason when it gave one.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/list/CenterListScreenPreview.kt:90</code></summary>

```kotlin
                    finished = true,
                    failures = listOf(
                        kpt.core.model.offline.DownloadFailure(
                            "Riverside Centre · loan #42",
                            "Loan with identifier 42 does not exist",
                        ),
                    ),
```

</details>

```kotlin
data class DownloadTarget(val id: Int, val name: String)
```
A selected center or group: its id, and the name progress and failures are reported under.

### `core/model/src/commonMain/kotlin/kpt/core/model/offline/OfflineSync.kt`

```kotlin
enum class RecordKind { CENTRE, GROUP, CLIENT }
```
The three levels an officer can keep on the device (Settings → Offline sync).

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/list/CenterListViewModel.kt:96</code></summary>

```kotlin
        val rows = (screenState.value as? ScreenState.Content)?.data.orEmpty()
        val targets = rows.mapNotNull { c ->
            c.id?.takeIf { it in state.selected }?.let { SyncTarget(RecordKind.CENTRE, it, c.name ?: "#$it") }
        }
        if (targets.isEmpty() || watch?.isActive == true) return
        mutableStateFlow.value = state.copy(selected = emptySet(), download = DownloadProgress(total = targets.size))
        val requestedAt = nowMs()
```

</details>

```kotlin
data class SyncTarget(val kind: RecordKind, val id: Int, val name: String)
```
One record the officer chose to keep on the device.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/list/CenterListViewModel.kt:96</code></summary>

```kotlin
        val rows = (screenState.value as? ScreenState.Content)?.data.orEmpty()
        val targets = rows.mapNotNull { c ->
            c.id?.takeIf { it in state.selected }?.let { SyncTarget(RecordKind.CENTRE, it, c.name ?: "#$it") }
        }
        if (targets.isEmpty() || watch?.isActive == true) return
        mutableStateFlow.value = state.copy(selected = emptySet(), download = DownloadProgress(total = targets.size))
        val requestedAt = nowMs()
```

</details>

```kotlin
enum class RecordSyncStatus { WAITING, SYNCING, SYNCED, FAILED }
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/offlinesync/src/commonMain/kotlin/kpt/feature/offlinesync/ui/OfflineSyncPreviews.kt:41</code></summary>

```kotlin
    loans = 52,
    savings = 22,
    status = RecordSyncStatus.SYNCED,
    refreshedAtMs = NOW - 2 * HOUR,
)
private val arinSavings = OfflineRecord(
    RecordKind.CENTRE,
```

</details>

```kotlin
data class OfflineRecord(
```
A record held on the device — chosen directly (`chosen`) or brought in through a parent (`parentKind`/`parentId`, "via Centro Arin"). Counts describe what came down under it.

<details><summary>Used in the template — <code>feature/offlinesync/src/commonMain/kotlin/kpt/feature/offlinesync/ui/OfflineSyncPicker.kt:251</code></summary>

```kotlin

@Composable
internal fun RemoveDialog(record: OfflineRecord, onAction: (OfflineSyncAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(OfflineSyncAction.DismissRemove) },
        text = { Text(stringResource(Res.string.feature_offlinesync_remove_confirm, record.name)) },
        confirmButton = {
```

</details>

```kotlin
enum class SyncJobStatus { RUNNING, FINISHED, CANCELLED }
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/list/CenterListViewModelTest.kt:168</code></summary>

```kotlin
            downloads.latestJob.value = SyncJob(
                1,
                SyncJobStatus.FINISHED,
                listOf(SyncTarget(RecordKind.CENTRE, 2, "x")),
                done = 1,
                startedAtMs = 1,
            )
```

</details>

```kotlin
data class SyncJob(
```
One background download. `done`/`total` count the chosen records; `reads` every server read so far (the job discovers a record's tree as it walks it, so a read total is not known up front).

<details><summary>Used in the template — <code>feature/center/src/commonTest/kotlin/kpt/feature/center/list/CenterListViewModelTest.kt:166</code></summary>

```kotlin
            testScheduler.advanceUntilIdle()
            // The background job finishes; the dialog follows its durable row.
            downloads.latestJob.value = SyncJob(
                1,
                SyncJobStatus.FINISHED,
                listOf(SyncTarget(RecordKind.CENTRE, 2, "x")),
                done = 1,
```

</details>

```kotlin
fun SyncJob.toDownloadProgress(): DownloadProgress = DownloadProgress(
```
The job as the centre / group lists' progress dialog draws it (`core/ui` OfflineDownload).

```kotlin
enum class RefreshMode { MANUAL, AUTOMATIC }
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/offlinesync/src/commonMain/kotlin/kpt/feature/offlinesync/ui/OfflineSyncPreviews.kt:166</code></summary>

```kotlin
            startedAtMs = NOW,
        ),
        refresh = RefreshSettings(mode = RefreshMode.AUTOMATIC, intervalHours = 6, wifiOnly = true),
    ),
)

@Preview @Composable
```

</details>

```kotlin
data class RefreshSettings(
```
How the chosen records are kept current. Only the CHOSEN records ever sync, in either mode.

<details><summary>Used in the template — <code>feature/offlinesync/src/commonMain/kotlin/kpt/feature/offlinesync/ui/OfflineSyncPreviews.kt:166</code></summary>

```kotlin
            startedAtMs = NOW,
        ),
        refresh = RefreshSettings(mode = RefreshMode.AUTOMATIC, intervalHours = 6, wifiOnly = true),
    ),
)

@Preview @Composable
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/payment/PaymentTypeOption.kt`

```kotlin
data class PaymentTypeOption(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `PaymentTypeOptionEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/repayment/LoanRepaymentViewModelTest.kt:76</code></summary>

```kotlin
                currency = SavingAccountCurrency(code = "INR", decimalPlaces = 2),
                paymentTypeOptions = listOf(
                    PaymentTypeOption(id = 1, name = "Cash"),
                    PaymentTypeOption(id = 4, name = "Mobile Money"),
                ),
            ),
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recent/RecentRecord.kt`

```kotlin
enum class RecentKind { CLIENT, GROUP, CENTER, LOAN }
```
What a recently-opened record is — decides its icon and which profile reopens it.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/detail/CenterDetailViewModel.kt:123</code></summary>

```kotlin
                val c = content.data
                val id = (c.id ?: centerId).toLong()
                recents.record(RecentRecord(RecentKind.CENTER, id, c.name.orEmpty(), c.accountNo))
            }
            .launchIn(viewModelScope)
    }
```

</details>

```kotlin
data class RecentRecord(
```
A record the officer opened, for Home's "Recently opened". `detail` is the secondary line the record carries itself (a loan's client name); the kind label and relative time are rendered.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/detail/CenterDetailViewModel.kt:123</code></summary>

```kotlin
                val c = content.data
                val id = (c.id ?: centerId).toLong()
                recents.record(RecentRecord(RecentKind.CENTER, id, c.name.orEmpty(), c.accountNo))
            }
            .launchIn(viewModelScope)
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/AccountChart.kt`

```kotlin
data class AccountChart(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:16</code></summary>

```kotlin
import kpt.core.model.objects.template.recurring.period.PeriodType
@Serializable
data class AccountChart(
    val attributeNameOptions: List<AttributeNameOption>? = null,
    val chartSlabs: List<ChartSlab>? = null,
//    val clientClassificationOptions: List<Any?>? = null,
    val clientTypeOptions: List<ClientTypeOption>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/AttributeName.kt`

```kotlin
data class AttributeName(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AttributeName.kt:14</code></summary>

```kotlin
import kotlinx.serialization.Serializable
@Serializable
data class AttributeName(
    val code: String? = null,
    val id: Int? = null,
    val value: String? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/ChartSlab.kt`

```kotlin
data class ChartSlab(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:18</code></summary>

```kotlin
data class AccountChart(
    val attributeNameOptions: List<AttributeNameOption>? = null,
    val chartSlabs: List<ChartSlab>? = null,
//    val clientClassificationOptions: List<Any?>? = null,
    val clientTypeOptions: List<ClientTypeOption>? = null,
    val conditionTypeOptions: List<ConditionTypeOption>? = null,
    val endDate: List<Int>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/ConditionType.kt`

```kotlin
data class ConditionType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ConditionType.kt:14</code></summary>

```kotlin
import kotlinx.serialization.Serializable
@Serializable
data class ConditionType(
    val code: String? = null,
    val id: Int? = null,
    val value: String? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/Currency.kt`

```kotlin
data class Currency(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:299</code></summary>

```kotlin
                activeLoans = loans?.activeLoans,
                inArrears = loans?.inArrears,
                loanTotals = loans?.byCurrency,
                savings = (byKey[SAVINGS] as? List<*>)?.filterIsInstance<Money>(),
                groups = byKey[GROUPS] as? Int,
                centers = byKey[CENTERS] as? Int,
            )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/EntityType.kt`

```kotlin
data class EntityType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/document/src/commonMain/kotlin/kpt/feature/document/list/DocumentListScreenPreview.kt:29</code></summary>

```kotlin
    Document(
        id = 1,
        parentEntityType = "groups",
        parentEntityId = 11,
        name = "Meeting minutes",
        fileName = "minutes-oct.pdf",
        size = 182_000,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/Incentive.kt`

```kotlin
data class Incentive(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ChartSlab.kt:22</code></summary>

```kotlin
    val description: String? = null,
    val fromPeriod: Int? = null,
    val incentives: List<Incentive>? = null,
    val periodType: PeriodType? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/IncentiveType.kt`

```kotlin
data class IncentiveType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/Incentive.kt:24</code></summary>

```kotlin
    val conditionType: ConditionType? = null,
    val entityType: EntityType? = null,
    val incentiveType: IncentiveType? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/LockinPeriodFrequencyType.kt`

```kotlin
data class LockinPeriodFrequencyType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:39</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromLockinPeriodFrequencyType(type: LockinPeriodFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/PeriodType.kt`

```kotlin
data class PeriodType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/fixeddepositapplication/FixedDepositApplicationScreen.kt:281</code></summary>

```kotlin
        val periods = template.periodFrequencyTypeOptions.orEmpty()
        OptionDropdown(
            value = state.depositPeriodType?.label.orEmpty(),
            label = stringResource(Res.string.feature_client_fdapp_deposit_period_type),
            labels = periods.mapNotNull { it.value },
            error = state.depositPeriodTypeError
                .errorOr(Res.string.feature_client_fdapp_error_period_type_required),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt`

```kotlin
data class RecurringDeposit(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModel.kt:324</code></summary>

```kotlin

    private companion object {
        const val TAG = "RecurringDeposit"
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDepositAccountTemplate.kt`

```kotlin
data class RecurringDepositAccountTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModel.kt:345</code></summary>

```kotlin
 * so each needs its own projection — there is no common supertype to write one against.
 */
private fun RecurringDepositAccountTemplate.toForm() = RecurringDepositForm(
    products = productOptions.orEmpty().mapNotNull { o ->
        RecurringOption(id = o.id ?: return@mapNotNull null, label = o.name.orEmpty())
    },
    fieldOfficers = fieldOfficerOptions.orEmpty().mapNotNull { o ->
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDepositApplicationInput.kt`

```kotlin
data class RecurringDepositApplicationInput(
```
A new recurring-deposit application.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModel.kt:104</code></summary>

```kotlin
        ownerId = state.clientId.toLong(),
        RecurringDepositState.serializer(),
        RecurringDepositApplicationInput.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered
```

</details>

```kotlin
data class RecurringDepositChargeInput(val chargeId: Int, val amount: String)
```
One charge on a recurring-deposit application.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModel.kt:285</code></summary>

```kotlin
                    .takeIf { state.toggle(RecurringToggle.PRE_CLOSURE_PENALTY) },
                charges = state.charges.map {
                    RecurringDepositChargeInput(chargeId = it.chargeId, amount = it.amount)
                },
            )

            lastInput = input
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringFrequencyType.kt`

```kotlin
data class RecurringFrequencyType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt:61</code></summary>

```kotlin
    val preClosurePenalApplicable: Boolean? = null,
    val recurringFrequency: Int? = null,
    val recurringFrequencyType: RecurringFrequencyType? = null,
    val status: Status? = null,
    val summary: Summary? = null,
    val timeline: Timeline? = null,
    val withHoldTax: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/Status.kt`

```kotlin
data class Status(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/identifiers/IdentifierDocumentKey.kt:30</code></summary>

```kotlin
 * including `"null"` for an absent part, so documents uploaded by the original app still match.
 */
internal fun identifierDocumentKey(documentTypeName: String?, documentKey: String?, status: Status?): String =
    "$documentTypeName$documentKey$status"

/** The key without its status — what still names the identifier after its status changes. */
internal fun identifierDocumentKeyPrefix(documentTypeName: String?, documentKey: String?): String =
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/Summary.kt`

```kotlin
data class Summary(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/details/CollectionSheetDetailsScreen.kt:132</code></summary>

```kotlin
        ) { view, _ ->
            Column(Modifier.fillMaxSize()) {
                Summary(view)
                LazyColumn(
                    modifier = Modifier.weight(1f).testTag(CollectionSheetDetailsTestTags.LIST),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/Timeline.kt`

```kotlin
data class Timeline(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/editdetails/ClientEditDetailsFormTest.kt:189</code></summary>

```kotlin
            legalForm = ClientStatus(id = 1, value = "PERSON"),
            dateOfBirth = listOf(1815, 12, 10),
            timeline = Timeline(submittedOnDate = listOf(2026, 1, 9)),
        ).asPrefill()

        assertEquals("Ada", prefilled.firstName)
        assertEquals("000000042", prefilled.accountNo)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/deposit/DepositPeriodFrequency.kt`

```kotlin
data class DepositPeriodFrequency(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt:34</code></summary>

```kotlin
    val depositAmount: Double? = null,
    val depositPeriod: Int? = null,
    val depositPeriodFrequency: DepositPeriodFrequency? = null,
    val depositProductId: Int? = null,
    val depositProductName: String? = null,
    val depositType: DepositType? = null,
    val expectedFirstDepositOnDate: List<Int>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/deposit/DepositType.kt`

```kotlin
data class DepositType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/converter/CenterTypeConverters.kt:33</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromDepositType(type: SavingAccountDepositTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/deposit/InMultiplesOfDepositTermType.kt`

```kotlin
data class InMultiplesOfDepositTermType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/InMultiplesOfDepositTermType.kt:14</code></summary>

```kotlin
import kotlinx.serialization.Serializable
@Serializable
data class InMultiplesOfDepositTermType(
    val code: String? = null,
    val id: Int? = null,
    val value: String? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/deposit/MaxDepositTermType.kt`

```kotlin
data class MaxDepositTermType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/MaxDepositTermType.kt:14</code></summary>

```kotlin
import kotlinx.serialization.Serializable
@Serializable
data class MaxDepositTermType(
    val code: String? = null,
    val id: Int? = null,
    val value: String? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/deposit/MinDepositTermType.kt`

```kotlin
data class MinDepositTermType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/MinDepositTermType.kt:14</code></summary>

```kotlin
import kotlinx.serialization.Serializable
@Serializable
data class MinDepositTermType(
    val code: String? = null,
    val id: Int? = null,
    val value: String? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/interest/InterestCalculationDaysInYearType.kt`

```kotlin
data class InterestCalculationDaysInYearType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:46</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestCalculationDaysInYearType(
        type: InterestCalculationDaysInYearType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/interest/InterestCalculationType.kt`

```kotlin
data class InterestCalculationType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:58</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestCalculationType(type: InterestCalculationType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/interest/InterestCompoundingPeriodType.kt`

```kotlin
data class InterestCompoundingPeriodType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:68</code></summary>

```kotlin

    @ColumnTypeConverter
    fun fromInterestCompoundingPeriodType(
        type: InterestCompoundingPeriodType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/interest/InterestPostingPeriodType.kt`

```kotlin
data class InterestPostingPeriodType(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:25</code></summary>

```kotlin
class SavingsPeriodTypeConverters {
    @ColumnTypeConverter
    fun fromInterestPostingPeriodType(
        type: InterestPostingPeriodType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/report/ReportParameter.kt`

```kotlin
data class ReportParameter(
```
One input a report asks for, from Fineract's `FullParameterList` (`stretchy_parameter`). `name` is the parameter's own report (`runreports/{name}` lists its options); `variable` is what the report SQL substitutes — sent as `R_{variable}`.

<details><summary>Used in the template — <code>feature/report/src/commonMain/kotlin/kpt/feature/report/ui/ReportPreviews.kt:50</code></summary>

```kotlin
// i18n:skip — preview fixtures
private val previewParameters = listOf(
    ReportParameter("OfficeIdSelectOne", "officeId", "Office", ReportParameterType.SELECT),
    ReportParameter(
        "loanOfficerIdSelectAll",
        "loanOfficerId",
        "Loan Officer",
```

</details>

```kotlin
enum class ReportParameterType { SELECT, DATE, TEXT, HIDDEN }
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/report/src/commonMain/kotlin/kpt/feature/report/ui/ReportPreviews.kt:50</code></summary>

```kotlin
// i18n:skip — preview fixtures
private val previewParameters = listOf(
    ReportParameter("OfficeIdSelectOne", "officeId", "Office", ReportParameterType.SELECT),
    ReportParameter(
        "loanOfficerIdSelectAll",
        "loanOfficerId",
        "Loan Officer",
```

</details>

```kotlin
data class ReportOption(val id: String, val label: String)
```
A choosable value of a SELECT parameter: what is sent, and what the officer sees.

<details><summary>Used in the template — <code>feature/report/src/commonMain/kotlin/kpt/feature/report/ui/ReportPreviews.kt:65</code></summary>

```kotlin
// i18n:skip — preview fixtures
private val previewOptions = mapOf(
    "OfficeIdSelectOne" to ParameterOptions.Loaded(listOf(ReportOption("1", "Head Office"))),
    "loanOfficerIdSelectAll" to ParameterOptions.Loaded(listOf(ReportOption("7", "Asha Rahman"))),
)

// i18n:skip — preview fixtures
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/Charge.kt`

```kotlin
data class Charge(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `Charge`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/FixedDepositPayload.kt`

```kotlin
data class FixedDepositPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/fixeddepositapplication/FixedDepositApplicationViewModel.kt:108</code></summary>

```kotlin
        ownerId = state.clientId.toLong(),
        FixedDepositApplicationState.serializer(),
        FixedDepositPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/FixedDepositProductOption.kt`

```kotlin
data class FixedDepositProductOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/savings/FixedDepositTemplate.kt:39</code></summary>

```kotlin

    @SerialName("productOptions")
    val productOptions: List<FixedDepositProductOption>? = null,

    @SerialName("fieldOfficerOptions")
    val fieldOfficerOptions: List<FieldOfficerOptions>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/FixedDepositTemplate.kt`

```kotlin
data class FixedDepositTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/fixeddepositapplication/FixedDepositApplicationScreen.kt:133</code></summary>

```kotlin
internal fun FixedDepositApplicationContent(
    state: FixedDepositApplicationState,
    template: ScreenState<FixedDepositTemplate>,
    onAction: (FixedDepositApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
    offeredDraft: SavedFormDraft? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingAccountCurrency.kt`

```kotlin
data class SavingAccountCurrency(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingAccountCurrencyEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/general/ClientGeneralViewModelTest.kt:84</code></summary>

```kotlin
            },
        ),
        currency = symbol?.let { SavingAccountCurrency(displaySymbol = it) },
    )

    // ---- the arithmetic ----
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingAccountDepositType.kt`

```kotlin
data class SavingAccountDepositType(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingAccountDepositTypeEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/depositaccounts/ClientDepositAccountsViewModel.kt:174</code></summary>

```kotlin
 */
enum class DepositAccountKind(
    val serverType: SavingAccountDepositType.ServerTypes,
) {
    SAVINGS(SavingAccountDepositType.ServerTypes.SAVINGS),
    FIXED(SavingAccountDepositType.ServerTypes.FIXED),
    RECURRING(SavingAccountDepositType.ServerTypes.RECURRING),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingProductsTemplate.kt`

```kotlin
data class SavingProductsTemplate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingProductsTemplate`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingsAccount.kt`

```kotlin
data class SavingsAccount(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsAccountEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/defaultaccount/ClientDefaultAccountViewModel.kt:215</code></summary>

```kotlin
            val payload = UpdateSavingsAccountRequest(savingsAccountId = accountId)
            lastPayload = payload
            val result = commands.updateSavingsAccount(clientId = state.clientId, payload = payload).offlineWhenUnsent()
            when (result) {
                is MutationResult.Applied -> {
                    formDraft.clear()
                    mutableStateFlow.value = state.copy(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingsAccountStatus.kt`

```kotlin
data class SavingsAccountStatus(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsAccountStatusEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/depositaccounts/ClientDepositAccountsViewModelTest.kt:58</code></summary>

```kotlin
        productName = "Product $id",
        depositType = SavingAccountDepositType(id = serverType.id, code = serverType.code),
        status = SavingsAccountStatus(
            closed = closed,
            submittedAndPendingApproval = pending,
        ),
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingsAccountSummary.kt`

```kotlin
data class SavingsAccountSummary(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsAccountSummaryEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/summary/SavingsSummaryViewModelTest.kt:62</code></summary>

```kotlin
        status = status,
        currency = SavingAccountCurrency(code = "USD", decimalPlaces = 2),
        summary = SavingsAccountSummary(
            accountBalance = 1_250.0,
            totalDeposits = 2_000.0,
            totalWithdrawals = 750.0,
            totalInterestEarned = 12.5,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingsAccountTransaction.kt`

```kotlin
data class SavingsAccountTransaction(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsAccountTransactionEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/summary/SavingsSummaryViewModel.kt:250</code></summary>

```kotlin
 * amount and running balance the officer needs to see.
 */
private fun transactionDate(tx: SavingsAccountTransaction): String {
    tx.savingsTransactionDate?.let { d ->
        val year = d.year
        val month = d.month
        val day = d.day
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingsAccountTransactionRequest.kt`

```kotlin
data class SavingsAccountTransactionRequest(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsAccountTransactionRequestEntity`. Same field names as the row; the `core/store/mapping` mappers convert between them.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/impl/SavingsCommandRepositoryImpl.kt:231</code></summary>

```kotlin
 * `errorMessage` is likewise storage — a request body has no business carrying one.
 */
private fun SavingsTransactionInput.toRequest() = SavingsAccountTransactionRequest(
    transactionDate = transactionDate,
    transactionAmount = transactionAmount,
    locale = locale,
    dateFormat = dateFormat,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingsAccountTransactionTemplate.kt`

```kotlin
data class SavingsAccountTransactionTemplate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsAccountTransactionTemplateEntity`. Same field names as the row; the `core/store/mapping` mappers convert between them.

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/transaction/SavingsTransactionViewModelTest.kt:51</code></summary>

```kotlin
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun template() = SavingsAccountTransactionTemplate(
        paymentTypeOptions = listOf(
            PaymentTypeOption(id = 3, name = "Cash"),
            PaymentTypeOption(id = 4, name = "Mobile Money"),
        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingsAccountWithAssociations.kt`

```kotlin
data class SavingsAccountWithAssociations(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsAccountWithAssociationsEntity`. Same field names as the row; the `core/store/mapping` mappers convert between them.

<details><summary>Used in the template — <code>feature/savings/src/commonMain/kotlin/kpt/feature/savings/summary/SavingsSummaryViewModel.kt:202</code></summary>

```kotlin
}

private fun SavingsAccountWithAssociations.toSummary(): SavingsSummary {
    // The ACCOUNT's currency, used for every figure on the screen including the four totals. The
    // original read `summary.currency` for the totals and the account's for the rows.
    val code = currency?.code.orEmpty()
    val digits = currency?.decimalPlaces ?: DEFAULT_DECIMALS
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingsTransactionDate.kt`

```kotlin
data class SavingsTransactionDate(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsTransactionDateEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/summary/SavingsSummaryViewModelTest.kt:200</code></summary>

```kotlin
                        // which is this entity's default and what the original cast to
                        // `List<Int>` before indexing `[2]`.
                        savingsTransactionDate = SavingsTransactionDate(
                            year = 2026,
                            month = 10,
                            day = 21,
                        ),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/SavingsTransactionType.kt`

```kotlin
data class SavingsTransactionType(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsTransactionTypeEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/summary/SavingsSummaryViewModelTest.kt:206</code></summary>

```kotlin
                        ),
                        amount = 500.0,
                        transactionType = SavingsTransactionType(deposit = true, value = "Deposit"),
                    ),
                ),
            ),
        )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/ShareAccountPayload.kt`

```kotlin
data class ShareAccountPayload(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/shareapplication/ShareApplicationViewModel.kt:99</code></summary>

```kotlin
        ownerId = state.clientId.toLong(),
        ShareApplicationState.serializer(),
        ShareAccountPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/ShareTemplate.kt`

```kotlin
data class ShareTemplate(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/shareapplication/ShareApplicationScreen.kt:125</code></summary>

```kotlin
internal fun ShareApplicationContent(
    state: ShareApplicationState,
    template: ScreenState<ShareTemplate>,
    onAction: (ShareApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
    offeredDraft: SavedFormDraft? = null,
```

</details>

```kotlin
data class SavingsAccountOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/savings/FixedDepositTemplate.kt:64</code></summary>

```kotlin

    val chargeOptions: List<ChargeOption>? = null,
    val savingsAccounts: List<SavingsAccountOption>? = null,
)
```

</details>

```kotlin
data class FrequencyTypeOption(
```
_No KDoc at source._

```kotlin
data class ChargeOptions(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormViewModel.kt:240</code></summary>

```kotlin
private val NEEDS_UNCOLLECTED_INPUT = setOf(ANNUAL_FEE, MONTHLY_FEE, WEEKLY_FEE, OVERDUE_INSTALLMENT)

internal fun ChargeOptions.toChargeType(resource: ChargeResource): ChargeType? {
    val time = chargeTimeType?.id
    val selectable = when (resource) {
        ChargeResource.LOAN -> time in LOAN_TIMES && chargeCalculationType?.id == FLAT
        ChargeResource.CLIENT, ChargeResource.SAVINGS -> time != null && time !in NEEDS_UNCOLLECTED_INPUT
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/search/SearchResourcesKey.kt`

```kotlin
data class SearchResourcesKey(
```
Addresses one `searchResources` read: what a search screen asks `SearchRepository` for. In core/model, not beside the store that uses it as a key: a feature builds it, and a feature depends on core/data + core/model only.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/search/SearchViewModel.kt:48</code></summary>

```kotlin
     * so typing must not fire requests. `null` means nothing has been submitted yet.
     */
    private val submitted = MutableStateFlow<SearchResourcesKey?>(null)

    /**
     * Results for the submitted query.
     *
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/BulkRepaymentTransactions.kt`

```kotlin
data class BulkRepaymentTransactions(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/ui/CollectionSheetPreviews.kt:63</code></summary>

```kotlin
// i18n:skip — preview fixture
private val previewDrafts = mapOf(
    12 to BulkRepaymentTransactions(loanId = 12, transactionAmount = 300.0, paymentTypeId = 1),
)

@Preview
@Composable
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/Changes.kt`

```kotlin
data class Changes(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/profile/src/commonTest/kotlin/kpt/feature/profile/identity/ProfileViewModelTest.kt:197</code></summary>

```kotlin

        vm.trySendAction(ProfileAction.SubmitPassword)
        assertTrue(auth.passwordChanges.isEmpty(), "a mismatched pair is never sent")

        vm.trySendAction(ProfileAction.RepeatPassword("Field#Office2026"))
        assertTrue(vm.profile.value.password!!.canSave)
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/CollateralItem.kt`

```kotlin
data class CollateralItem(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/collateral/ClientCollateralScreen.kt:127</code></summary>

```kotlin
internal fun ClientCollateralContent(
    state: ClientCollateralState,
    collaterals: ScreenState<List<CollateralItem>>,
    onAction: (ClientCollateralAction) -> Unit,
    modifier: Modifier = Modifier,
    offeredDraft: SavedFormDraft? = null,
) {
```

</details>

```kotlin
data class CollateralItemResult(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/collateraldetails/ClientCollateralDetailsScreen.kt:77</code></summary>

```kotlin
@Composable
internal fun ClientCollateralDetailsContent(
    collaterals: ScreenState<List<CollateralItemResult>>,
    onAction: (ClientCollateralDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContent(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/CurrencyDto.kt`

```kotlin
data class CurrencyDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoanDisburseTemplateDto.kt:20</code></summary>

```kotlin
    val netDisbursalAmount: Double,
    val date: List<Int>,
    val currency: CurrencyDto? = null,
    val paymentTypeOptions: List<PaymentTypeOptionDto> = emptyList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/DefaultPayload.kt`

```kotlin
data class DefaultPayload(
```
Created by ADMIN on 16-Jun-15.

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/EnumOptionData.kt`

```kotlin
data class EnumOptionData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/auth/PostAuthenticationResponse.kt:43</code></summary>

```kotlin
    val officeName: String? = null,

    val organisationalRole: EnumOptionData? = null,

    val permissions: List<String>? = null,

    val roles: List<RoleData>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/GenericRequest.kt`

```kotlin
class GenericRequest
```
Created by ishankhanna on 24/06/14.

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/GenericResponse.kt`

```kotlin
data class GenericResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/activate/src/commonTest/kotlin/kpt/feature/activate/ui/ActivateViewModelTest.kt:248</code></summary>

```kotlin
private class FakeCenterCommands(
    private val gate: CompletableDeferred<Unit>? = null,
    private val result: () -> MutationResult<GenericResponse> =
        { MutationResult.Applied(GenericResponse(), synced = true) },
) : CenterCommandRepository {
    val activations = mutableListOf<Pair<Int, ActivatePayload?>>()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/Payload.kt`

```kotlin
open class Payload
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/activate/src/commonTest/kotlin/kpt/feature/activate/ui/ActivateViewModelTest.kt:255</code></summary>

```kotlin
    override suspend fun activateCenter(
        centerId: Int,
        activatePayload: ActivatePayload?,
    ): MutationResult<GenericResponse> {
        activations += centerId to activatePayload
        gate?.await()
        return result()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/PaymentTypeOptionDto.kt`

```kotlin
data class PaymentTypeOptionDto(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoanDisburseTemplateDto.kt:21</code></summary>

```kotlin
    val date: List<Int>,
    val currency: CurrencyDto? = null,
    val paymentTypeOptions: List<PaymentTypeOptionDto> = emptyList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/PinpointLocationActionResponse.kt`

```kotlin
data class PinpointLocationActionResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt:75</code></summary>

```kotlin
        clientId: Int,
        clientAddressRequest: ClientAddressRequest?,
    ): MutationResult<PinpointLocationActionResponse>

    suspend fun deleteClientPinpointLocation(
        apptableId: Int,
        datatableId: Int,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/ProductOption.kt`

```kotlin
data class ProductOption(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savings/src/commonTest/kotlin/kpt/feature/savings/recurring/RecurringDepositViewModelTest.kt:64</code></summary>

```kotlin
    private fun template() = RecurringDepositAccountTemplate(
        productOptions = listOf(
            ProductOption(id = 4, name = "Monthly RD"),
            // Unsendable: no id. Must not be offered.
            ProductOption(id = null, name = "Draft RD"),
        ),
        fieldOfficerOptions = listOf(FieldOfficerOption(id = 9, displayName = "Officer A")),
```

</details>

```kotlin
data class ProductCurrency(
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/ResultsetColumnHeaderData.kt`

```kotlin
data class ResultsetColumnHeaderData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/datatable/dto/GetDataTablesResponse.kt:28</code></summary>

```kotlin
    val applicationTableName: String? = null,

    val columnHeaderData: List<ResultsetColumnHeaderData>? = null,

    val registeredTableName: String? = null,

)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/RetrieveOneResponse.kt`

```kotlin
data class RetrieveOneResponse(
```
GetStaffResponse

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/staff/api/StaffApi.kt:42</code></summary>

```kotlin
        @Query("loanOfficersOnly") loanOfficersOnly: Boolean? = false,
        @Query("status") status: String? = "active",
    ): List<RetrieveOneResponse>

    @GET("staff?status=all")
    suspend fun getStaffForOffice(@Query("officeId") officeId: Int): List<Staff>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/RoleData.kt`

```kotlin
data class RoleData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/auth/PostAuthenticationResponse.kt:47</code></summary>

```kotlin
    val permissions: List<String>? = null,

    val roles: List<RoleData>? = null,

    val staffDisplayName: String? = null,

    val staffId: Long? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/Timeline.kt`

```kotlin
data class Timeline(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonTest/kotlin/kpt/feature/client/editdetails/ClientEditDetailsFormTest.kt:189</code></summary>

```kotlin
            legalForm = ClientStatus(id = 1, value = "PERSON"),
            dateOfBirth = listOf(1815, 12, 10),
            timeline = Timeline(submittedOnDate = listOf(2026, 1, 9)),
        ).asPrefill()

        assertEquals("Ada", prefilled.firstName)
        assertEquals("000000042", prefilled.accountNo)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/staff/Staff.kt`

```kotlin
data class Staff(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `StaffEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientScreen.kt:915</code></summary>

```kotlin
    const val CLIENT_ACTIVE = "createNewClient:clientActive"
    const val ADDRESS_ACTIVE = "createNewClient:addressActive"
    const val NO_STAFF = "createNewClient:noStaff"
    const val UPLOADING_PHOTO = "createNewClient:uploadingPhoto"
    const val SUBMIT = "createNewClient:submit"
    const val CREATED_DIALOG = "createNewClient:created"
    const val FAILED_DIALOG = "createNewClient:failed"
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/survey/ComponentDatas.kt`

```kotlin
data class ComponentDatas(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ComponentDatasEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/survey/entity/ComponentDatasEntity.kt:24</code></summary>

```kotlin
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "ComponentDatas",
)
data class ComponentDatasEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/survey/QuestionDatas.kt`

```kotlin
data class QuestionDatas(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `QuestionDatasEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/survey/dao/SurveyDao.kt:55</code></summary>

```kotlin
    }

    @Query("SELECT * FROM QuestionDatas WHERE surveyId = :surveyId ORDER BY sequenceNo ASC")
    fun getQuestionDatas(surveyId: Int): Flow<List<QuestionDatasEntity>>

    @Query("SELECT * FROM ResponseDatas WHERE id = :questionId ORDER BY sequenceNo ASC")
    fun getResponseDatas(questionId: Int): Flow<List<ResponseDatasEntity>>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/survey/ResponseDatas.kt`

```kotlin
data class ResponseDatas(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ResponseDatasEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/survey/dao/SurveyDao.kt:58</code></summary>

```kotlin
    fun getQuestionDatas(surveyId: Int): Flow<List<QuestionDatasEntity>>

    @Query("SELECT * FROM ResponseDatas WHERE id = :questionId ORDER BY sequenceNo ASC")
    fun getResponseDatas(questionId: Int): Flow<List<ResponseDatasEntity>>

    /** Stamp the owning survey onto question data before persisting. Was `SurveyDaoHelper`. */
    suspend fun saveQuestionDataFor(surveyId: Int, questionData: QuestionDatasEntity): QuestionDatasEntity {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/survey/Survey.kt`

```kotlin
data class Survey(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SurveyEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/kpt/core/common/utils/Constants.kt:44</code></summary>

```kotlin
    const val CLIENT_NAME = "clientName"
    const val CLIENT_ID = "clientId"
    const val CLIENT_SURVEY = "clientSurvey"
    const val ID = "id"
    const val CLIENT = "Client"
    const val CLIENTS = "clients"
    const val LOAN_ACCOUNT_NUMBER = "loanAccountNumber"
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/AuthState.kt`

```kotlin
sealed class AuthState
```
Models high level auth state for the application.

### `core/model/src/commonMain/kotlin/kpt/core/model/user/DarkThemeConfig.kt`

```kotlin
enum class DarkThemeConfig(val configName: String, val osValue: Int)
```
The user's dark-mode preference: follow the system, or force light/dark. `osValue` maps to the platform's own night-mode constant, so the choice can be handed straight to the OS rather than re-interpreted per platform.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsDialog.kt:68</code></summary>

```kotlin
        onChangeThemeBrand = viewModel::updateThemeBrand,
        onChangeDynamicColorPreference = viewModel::updateDynamicColorPreference,
        onChangeDarkThemeConfig = viewModel::updateDarkThemeConfig,
    )
}

@Composable
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/LanguageConfig.kt`

```kotlin
enum class LanguageConfig(
```
Every language the app can be switched to, in the user's OWN language. GENERATED from core/registries/LOCALE_REGISTRY.yaml by `core/scripts/language-picker-sync.sh --write` — DO NOT HAND-EDIT.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:62</code></summary>

```kotlin
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onChangeLanguage: (language: LanguageConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/ThemeBrand.kt`

```kotlin
enum class ThemeBrand(val brandName: String)
```
The selected colour brand. A fork extends this to offer its own palettes.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsDialog.kt:66</code></summary>

```kotlin
        settingsState = settingsState,
        onRetry = viewModel::onRetry,
        onChangeThemeBrand = viewModel::updateThemeBrand,
        onChangeDynamicColorPreference = viewModel::updateDynamicColorPreference,
        onChangeDarkThemeConfig = viewModel::updateDarkThemeConfig,
    )
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UnlockType.kt`

```kotlin
enum class UnlockType
```
How the user unlocks the app — passcode or biometric.

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt`

```kotlin
data class UserData(
```
Everything the app persists about the current user — theme, language, onboarding progress and the lock state.

<details><summary>Used in the template — <code>feature/settings/src/commonTest/kotlin/kpt/feature/settings/SettingsViewModelTest.kt:78</code></summary>

```kotlin
     */
    private class FakeUserDataRepository(
        private val initial: UserData,
        private val stateFlowOverride: Flow<ScreenState<UserData>>? = null,
        private val refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1),
    ) : UserDataRepository {
        val current = MutableStateFlow(initial)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/ApiDateFormatter.kt`

```kotlin
object ApiDateFormatter
```
Centralized date formatter for Fineract API requests.

<details><summary>Example</summary>

```kotlin
val payload = ClientPayload(
    activationDate = ApiDateFormatter.formatForApi(dateMillis),
    dateFormat = ApiDateFormatter.DATE_FORMAT,
    locale = ApiDateFormatter.LOCALE
)
```

</details>

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:174</code></summary>

```kotlin
                            // transfer did NOT (see their ViewModels).
                            ActivatePayload(
                                activationDate = ApiDateFormatter.formatForApi(activateDate),
                                dateFormat = ApiDateFormatter.DATE_FORMAT,
                                locale = ApiDateFormatter.LOCALE,
                            ),
                        ),
```

</details>

- `const val DATE_FORMAT = DateConstants.DATE_FORMAT` — Standard date format for Fineract API: "dd MMMM yyyy" Example: "18 February 2026"
- `const val LOCALE = DateConstants.LOCALE` — Standard locale for Fineract API
- `fun formatForApi(millis: Long): String` — Format epoch milliseconds to API date string using standard format.
- `fun formatForApi(millis: Long, pattern: DateFormatPattern): String` — Format epoch milliseconds to date string using specified pattern.
- `val dateTime = Instant`
- `fun formatForApi(date: LocalDate): String` — Format LocalDate to API date string using standard format.
- `fun formatForApi(date: LocalDate, pattern: DateFormatPattern): String` — Format LocalDate to date string using specified pattern.
- `val day = date.dayOfMonth.toString().padStart(2, '0')`
- `val month = date.month`
- `val monthNumber = date.monthNumber.toString().padStart(2, '0')`
- `val year = date.year`
- `fun formatForApi(day: Int, month: Int, year: Int): String` — Format date components (day, month, year) to API date string.
- `val dayStr = day.toString().padStart(2, '0')`
- `val monthName = getMonthName(month)`
  _…more members; read the file._

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/DateConstants.kt`

```kotlin
object DateConstants
```
Standard date format constants for Fineract API. These constants ensure consistency between the date strings sent to the API and the dateFormat parameter that specifies how they should be parsed.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/impl/CollectionSheetCommandRepositoryImpl.kt:146</code></summary>

```kotlin
    CollectionSheetRequestPayload(
        calendarId = calendarId,
        dateFormat = dateFormat ?: DateConstants.DATE_FORMAT,
        locale = locale ?: DateConstants.LOCALE,
        transactionDate = transactionDate,
    )
```

</details>

- `const val DATE_FORMAT = "dd MMMM yyyy"` — Standard date format for Fineract API: "dd MMMM yyyy" Example: "18 February 2026"
- `const val LOCALE = "en"` — Standard locale for Fineract API
- `const val DATE_FORMAT_WITH_TIME = "dd MMMM yyyy HH:mm"` — Date format with time: "dd MMMM yyyy HH:mm" Example: "18 February 2026 14:30"

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/FormatDate.kt`

```kotlin
fun formatDate(millis: Long): String
```
Format epoch milliseconds to API-compatible date string. Returns date in "dd MMMM yyyy" format (e.g., "18 February 2026") which matches the standard Fineract API dateFormat parameter.

```kotlin
fun formatDate(millis: Long, pattern: DateFormatPattern): String
```
Format epoch milliseconds to date string using specified pattern.

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/JsonExtension.kt`

```kotlin
fun String.asServerConfig(): ServerConfig
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/ServerConfig.kt`

```kotlin
data class ServerConfig(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileViewModel.kt:77</code></summary>

```kotlin
        serverConfigs.instancesStream(viewModelScope).state,
    ) { config, instances ->
        val cfg = (config as? ScreenState.Content<ServerConfig>)?.data
        val instance = cfg?.let { c -> (instances as? ScreenState.Content)?.data?.matching(c) }
        ServerSession(
            instanceName = instance?.name ?: cfg?.endPoint.orEmpty(),
            known = instance != null,
```

</details>

```kotlin
fun ServerConfig.getInstanceUrl(): String
```
_No KDoc at source._

```kotlin
fun ServerConfig.baseUrl(): String
```
The full URL this config names — the one requests resolve against. The port is written only when it is not the scheme's default, so a config split from a declared URL rebuilds EXACTLY that URL.

```kotlin
fun serverConfigOf(url: String, tenant: String): ServerConfig?
```
Splits a full server URL (`https://host[:port][/path]`) and a tenant into a `ServerConfig` — the original's Instance URL dialog took exactly these two. Null when `url` is not an http(s) URL.

```kotlin
data class ServerInstance(
```
A server the officer picks by name instead of typing five fields. `demoAccounts` is non-empty for a PUBLIC demo instance — the Mifos community publishes those sign-ins, so showing them is the point.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigScreen.kt:464</code></summary>

```kotlin
/** "Signed in to …": the session's server, its address and tenant, and who is signed in. */
@Composable
private fun LiveServerCard(live: ServerConfig, instance: ServerInstance?, username: String?) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth().testTag(ServerConfigTestTags.LIVE),
```

</details>

```kotlin
data class DemoCredentials(val role: String, val username: String, val password: String)
```
One published sign-in of a demo `ServerInstance` — `role` says what it can do. Never a real account.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigScreen.kt:359</code></summary>

```kotlin
/** The selected demo instance's PUBLISHED sign-ins, one line per role — so a tester knows what to type. */
@Composable
private fun DemoCredentialsHint(accounts: List<DemoCredentials>) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth().testTag(ServerConfigTestTags.DEMO_HINT),
```

</details>

```kotlin
fun List<ServerInstance>.matching(config: ServerConfig): ServerInstance? = firstOrNull { it.config == config }
```
The instance whose config is exactly `config`, or null for a custom server.

---

_500 type(s), 46 function(s)/property(ies); 308 carry KDoc at source; 2 authored example(s); 453 live call site(s)._
<!-- api-docs:end -->
