# `core/model`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_MODEL.md`
> **Measured:** 425 Kotlin files, 2 test files

## Principal types

`Account`, `AccountChart`, `AccountLinkingOptions`, `AccountOption`, `AccountOptions`, `AccountTransferRequest`, `AccountTransferTemplate`, `AccountTypeOption`, `AccountingRule`, `ActivatePayload`, `ActualDisbursementDate`, `Address`, `AddressConfiguration`, `AddressDraftPayload`  …and 354 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/model sha=ede28f417cbdc185867336ee9ca9c5902468e4a9 -->
## API reference

_Generated from `core/model` at tree `ede28f417cbd` by `scripts/docs/api-docs-gen.sh`._
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/converter/CenterTypeConverters.kt:25</code></summary>

```kotlin
    /** Stores a `CenterDateEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromCenterDate(centerDate: CenterDateEntity?): String? {
        return centerDate?.let { Json.encodeToString(it) }
    }

    /** Reads a `CenterDateEntity?` back from the JSON column it is stored in. */
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

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/center/CenterCommandRepository.kt:41</code></summary>

```kotlin
        postCentersCenterIdRequest: PostCentersCenterIdRequest,
        command: String? = null,
    ): MutationResult<PostCentersCenterIdResponse>

    /** Saves a centre's collection sheet. */
    suspend fun saveCollectionSheet(
        centerId: Int,
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/charge/converter/ChargeTypeConverters.kt:49</code></summary>

```kotlin
    /** Stores a `ClientChargeCurrencyEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromClientChargeCurrency(currency: ClientChargeCurrencyEntity?): String? {
        return currency?.let { Json.encodeToString(it) }
    }

    /** Reads a `ClientChargeCurrencyEntity?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/AddressConfiguration.kt`

```kotlin
data class AddressConfiguration(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `AddressConfiguration`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientRepository.kt:107</code></summary>

```kotlin
     * validation made client creation impossible on such a tenant.
     */
    fun addressConfigurationStream(scope: CoroutineScope): ScreenDataStream<AddressConfiguration>

    /** The address template: address types and the fields each one takes. */
    fun addressTemplateStream(scope: CoroutineScope): ScreenDataStream<AddressTemplate>
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/client/Client.kt:85</code></summary>

```kotlin
    val clientType: ClientType? = null,
    /** The client's classification. */
    val clientClassification: ClientClassification? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientCloseTemplateResponse.kt`

```kotlin
data class ClientCloseTemplateResponse(
```
Server response to a client close template request.

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
A closure reason offered by the close-client template.

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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/converter/ClientTypeConverters.kt:52</code></summary>

```kotlin
    /** Stores a `ClientDateEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromClientDate(date: ClientDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    /** Reads a `ClientDateEntity?` back from the JSON column it is stored in. */
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/client/Client.kt:83</code></summary>

```kotlin
    val gender: ClientGender? = null,
    /** The client's type. */
    val clientType: ClientType? = null,
    /** The client's classification. */
    val clientClassification: ClientClassification? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/ClientsAttendance.kt`

```kotlin
data class ClientsAttendance(
```
One client's attendance on a collection sheet.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/CollectionSheetPayload.kt:35</code></summary>

```kotlin

    /** The attendance recorded for each client. */
    var clientsAttendance: MutableList<ClientsAttendance> = ArrayList(),

    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    var dateFormat: String = ApiDateFormatter.DATE_FORMAT,
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/client/ClientsTemplate.kt:38</code></summary>

```kotlin
    val clientClassificationOptions: List<Options>? = emptyList(),
    /** The legal forms that may be chosen. */
    val clientLegalFormOptions: List<InterestTypeOption>? = emptyList(),
    /** The data tables registered for this entity. */
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

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/navigation/ActivateRoute.kt:37</code></summary>

```kotlin

/** Opens the activation screen for client [clientId]. */
fun NavController.navigateToActivateClient(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ActivateRoute(clientId, Constants.ACTIVATE_CLIENT), navOptions)

/** Opens the activation screen for centre [centerId]. */
fun NavController.navigateToActivateCenter(centerId: Int, navOptions: NavOptions? = null) =
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/PostClientAddressRequest.kt`

```kotlin
data class PostClientAddressRequest(
```
Request body for a post client address.

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
Server response to a post client address request.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt:103</code></summary>

```kotlin
        addressTypeId: Int,
        addressPayload: PostClientAddressRequest,
    ): MutationResult<PostClientAddressResponse>

    /** Assigns a loan officer to a client. */
    suspend fun assignStaff(clientId: Int, payload: AssignStaffRequest): MutationResult<Unit>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/client/PostClientsClientIdRequest.kt`

```kotlin
data class PostClientsClientIdRequest(
```
PostClientsClientIdRequest

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt:60</code></summary>

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

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt:62</code></summary>

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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:146</code></summary>

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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:58</code></summary>

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
One staff choice offered by a template.

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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:145</code></summary>

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

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/CollectionSheetRepository.kt:33</code></summary>

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

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/CollectionSheetRepository.kt:55</code></summary>

```kotlin

    /** Generates a centre's productive collection sheet, or null when there is none. */
    suspend fun productiveSheet(centerId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?

    /** Generates a group's collection sheet, or null when there is none. */
    suspend fun groupCollectionSheet(groupId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/GroupCollectionSheet.kt`

```kotlin
data class GroupCollectionSheet(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupCollectionSheet`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/CollectionSheetResponse.kt:29</code></summary>

```kotlin

    /** The groups. */
    var groups: List<GroupCollectionSheet> = ArrayList(),

    /** The loan products the sheet covers. */
    var loanProducts: List<kpt.core.model.objects.organisations.LoanProducts> = ArrayList(),
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/converter/CollectionSheetTypeConverters.kt:23</code></summary>

```kotlin
    /** Stores a `List<MeetingFallCalendar>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromMeetingFallCalendarList(meetings: List<MeetingFallCalendar>?): String =
        Json.encodeToString(meetings ?: emptyList())

    /** Reads a `List<MeetingFallCalendar>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/ProductiveCollectionSheetPayload.kt`

```kotlin
data class ProductiveCollectionSheetPayload(
```
Created by Tarun on 25-07-2017.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/CollectionSheetCommandRepository.kt:47</code></summary>

```kotlin
    suspend fun submitProductiveSheet(
        centerId: Int,
        payload: ProductiveCollectionSheetPayload?,
    ): MutationResult<GenericResponse>

    /** Submits a group's collection sheet. */
    suspend fun submitCollectionSheet(groupId: Int, payload: CollectionSheetPayload?): MutationResult<GenericResponse>
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/ClientCollectionSheet.kt:32</code></summary>

```kotlin

    /** The savings accounts. */
    var savings: ArrayList<SavingsCollectionSheet> = ArrayList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/payload/CollectionSheetPayload.kt`

```kotlin
class CollectionSheetPayload : Payload()
```
Request body for a collection sheet.

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

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:155</code></summary>

```kotlin
        onAction = viewModel::trySendAction,
        onOpenServerConfig = onOpenServerConfig,
        onOpenPrivacyPolicy = { urlLauncher.launchInApp(AppPrivacyConfig.PRIVACY_POLICY_URL) },
        onOpenTerms = { AppOrgLinks["terms"]?.let(urlLauncher::launchInApp) },
        modifier = modifier,
    )
}
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
- `const val FIRST_RUN_ONBOARDING: Boolean = true` — `security.first_run_onboarding` — whether a fresh install shows onboarding once (false = the app has none).

### `core/model/src/commonMain/kotlin/kpt/core/model/dashboard/Dashboard.kt`

```kotlin
data class DashboardScope(val officeId: Long, val staffId: Long?, val username: String)
```
Whose book the dashboard shows. A user linked to a staff record sees their own portfolio; an unlinked user (the gateway demo user) sees their office, and "own" loans fall back to the ones they submitted.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:135</code></summary>

```kotlin
        ).on(HomeCardId.entries, key = "user")
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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:169</code></summary>

```kotlin
    private fun header(
        user: User,
        userStream: ScreenDataStream<DashboardUser>,
        businessDate: ScreenDataStream<String>,
    ) {
        combine(
            userStream.state,
```

</details>

```kotlin
data class DashboardLoan(
```
One loan as the dashboard needs it: ownership, arrears and the summary sums.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:429</code></summary>

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

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/dashboard/DashboardRepository.kt:35</code></summary>

```kotlin

    /** The officer's savings accounts. */
    fun savingsStream(scope: CoroutineScope): ScreenDataStream<List<DashboardSavings>>

    /** Installments of [loanIds] due on [date] (`yyyy-MM-dd`) — Today's collections without the collection sheet. */
    fun dueTodayStream(date: String, loanIds: List<Long>, scope: CoroutineScope): ScreenDataStream<List<DueTodayLoan>>
```

</details>

```kotlin
data class CurrencyTotals(
```
Money sums for one currency — never mixed across currencies.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardCards.kt:722</code></summary>

```kotlin
/** col-span-2 bg-primary-container/40 — total outstanding per currency under a bank glyph (03 #portfolio_card). */
@Composable
private fun OutstandingTile(t: List<CurrencyTotals>) {
    val c = KptTheme.colorScheme
    Box(
        Modifier.fillMaxWidth().clip(
            KptTheme.shapes.medium,
```

</details>

```kotlin
data class LoanPortfolio(val activeLoans: Int, val inArrears: Int, val byCurrency: List<CurrencyTotals>)
```
The officer's loan portfolio totals.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:388</code></summary>

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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:429</code></summary>

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
True when the user holds permission `code`, directly or through a covering super-permission.

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

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/datatable/DataTableCommandRepository.kt:36</code></summary>

```kotlin
        apptableId: Long,
        datatableId: Long,
    ): MutationResult<DeleteDataTablesDatatableAppTableIdDatatableIdResponse>

    /** Adds a row for an entity to a data table. */
    suspend fun createEntryInDataTable(
        dataTableName: String,
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/group/converter/GroupTypeConverters.kt:28</code></summary>

```kotlin
    /** Stores a `GroupDateEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromGroupDate(date: GroupDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    /** Reads a `GroupDateEntity?` back from the JSON column it is stored in. */
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanScheduleTypeConverters.kt:44</code></summary>

```kotlin
    /** Stores a `ActualDisbursementDateEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromActualDisbursementDate(date: ActualDisbursementDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    /** Reads a `ActualDisbursementDateEntity?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/AmortizationType.kt`

```kotlin
class AmortizationType(
```
Kind of an amortization.

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
Request body for an assign loan officer.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:95</code></summary>

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
Server response to an assign loan officer request.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:96</code></summary>

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
Server response to a create guarantor request.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:119</code></summary>

```kotlin

    /** Adds a guarantor to a loan. */
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
Request body for a guarantor.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:119</code></summary>

```kotlin

    /** Adds a guarantor to a loan. */
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/loan/Loan.kt:53</code></summary>

```kotlin
    val loanOfficerName: String? = null,
    /** The loan type (individual, group or JLG). */
    val loanType: LoanAccountType? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** The loan principal. */
    val principal: Double? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanApprovalData.kt`

```kotlin
data class LoanApprovalData(
```
A loan approval data.

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanChargeOffRequestDto.kt`

```kotlin
data class LoanChargeOffRequestDto(
```
Request body for a loan charge off.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:90</code></summary>

```kotlin

    /** Charges off a loan. */
    suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>

    /** Assigns or changes a loan's officer. */
    suspend fun assignLoanOfficer(
        loanId: Int,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/LoanChargeOffResponseDto.kt`

```kotlin
data class LoanChargeOffResponseDto(
```
Server response to a loan charge off request.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:90</code></summary>

```kotlin

    /** Charges off a loan. */
    suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>

    /** Assigns or changes a loan's officer. */
    suspend fun assignLoanOfficer(
        loanId: Int,
```

</details>

```kotlin
data class LoanChargeOffChanges(
```
A loan charge off changes.

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

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:87</code></summary>

```kotlin
        loanId: Int,
        request: LoanRepaymentRequest,
    ): MutationResult<LoanRepaymentResponse>

    /** Charges off a loan. */
    suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>
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
Kind of a loan.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/converter/ListTypeConverters.kt:111</code></summary>

```kotlin
    /** Stores a `LoanType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromType(type: LoanType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `LoanType?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/loan/RejectLoanRequestDto.kt`

```kotlin
data class RejectLoanRequestDto(
```
Request body for a reject loan.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:71</code></summary>

```kotlin

    /** Rejects a loan application. */
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
Server response to a reject loan request.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:71</code></summary>

```kotlin

    /** Rejects a loan application. */
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

<details><summary>Used in the template — <code>feature/notification/src/commonMain/kotlin/kpt/feature/notification/navigation/NotificationsRoute.kt:46</code></summary>

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
The fields a command changed, as the server echoes them.

<details><summary>Used in the template — <code>feature/profile/src/commonTest/kotlin/kpt/feature/profile/identity/ProfileViewModelTest.kt:197</code></summary>

```kotlin

        vm.trySendAction(ProfileAction.SubmitPassword)
        assertTrue(auth.passwordChanges.isEmpty(), "a mismatched pair is never sent")

        vm.trySendAction(ProfileAction.RepeatPassword("Field#Office2026"))
        assertTrue(vm.profile.value.password!!.canSave)
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/LanguageConfig.kt`

```kotlin
enum class LanguageConfig(
```
A language the app can be shown in.

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:71</code></summary>

```kotlin
        override val authToken: String? get() = unused()
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
        override val observeDarkThemeConfig: Flow<DarkThemeConfig> get() = unused()
        override val observeDynamicColorPreference: Flow<Boolean> get() = unused()
        override val observeScreenCapturePreference: Flow<Boolean> get() = unused()
        override val observeAnalyticsConsent: Flow<Boolean> get() = unused()
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:189</code></summary>

```kotlin
    val maximumGap: Int? = null,
    /** The savings accounts that may be linked for repayments. */
    val accountLinkingOptions: List<AccountLinkingOptions> = emptyList(),

    /** The loan's schedule type. */
    val loanScheduleType: TermFrequencyTypeOptions? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/AmortizationType.kt`

```kotlin
class AmortizationType(
```
Kind of an amortization.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/Currency.kt`

```kotlin
data class Currency(
```
Created by mayankjindal on 22/10/16.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:393</code></summary>

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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:173</code></summary>

```kotlin
    val product: Product? = null,
    /** How many days a month has for interest calculation (actual or 30). */
    val daysInMonthType: DaysInMonthType? = null,
    /** How many days a year has for interest calculation (actual, 360, 364 or 365). */
    val daysInYearType: DaysInYearType? = null,

    /** True when interest is recalculated as repayments arrive. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/DaysInYearType.kt`

```kotlin
data class DaysInYearType(
```
Created by nellyk on 2/21/2016.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:175</code></summary>

```kotlin
    val daysInMonthType: DaysInMonthType? = null,
    /** How many days a year has for interest calculation (actual, 360, 364 or 365). */
    val daysInYearType: DaysInYearType? = null,

    /** True when interest is recalculated as repayments arrive. */
    val isInterestRecalculationEnabled: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/InterestCalculationPeriodType.kt`

```kotlin
data class InterestCalculationPeriodType(
```
Kind of an interest calculation period.

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
Kind of an interest rate frequency.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanTypeConverters.kt:91</code></summary>

```kotlin
    /** Stores a `InterestRateFrequencyType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromInterestRateFrequencyType(type: InterestRateFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `InterestRateFrequencyType?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/InterestType.kt`

```kotlin
class InterestType(
```
Kind of an interest.

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
Server response to a loan repayment request.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/loan/LoanCommandRepository.kt:87</code></summary>

```kotlin
        loanId: Int,
        request: LoanRepaymentRequest,
    ): MutationResult<LoanRepaymentResponse>

    /** Charges off a loan. */
    suspend fun chargeOff(loanId: Int, request: LoanChargeOffRequestDto): MutationResult<LoanChargeOffResponseDto>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/PaymentDetailData.kt`

```kotlin
data class PaymentDetailData(
```
A payment detail data.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/Transaction.kt:30</code></summary>

```kotlin
    var currency: Currency? = null,
    /** The payment details of the transaction. */
    var paymentDetailData: PaymentDetailData? = null,
    /** The amount, in the record's currency. */
    var amount: Double? = null,
    /** The amount disbursed after the charges deducted at disbursement. */
    var netDisbursalAmount: Double? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/PaymentType.kt`

```kotlin
data class PaymentType(
```
Kind of a payment.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/RepaymentFrequencyType.kt`

```kotlin
data class RepaymentFrequencyType(
```
Kind of a repayment frequency.

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

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/navigation/LoanNavigation.kt:275</code></summary>

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
A repayment schedule table data.

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
A repayment schedule row data.

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
A repayment schedule totals data.

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
Kind of a term period frequency.

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
A transaction.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/Type.kt`

```kotlin
data class Type(
```
A Fineract code value: an id, a code and its display text.

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
Server template for a loan disburse: the options its form offers.

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
An actual disbursement date.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanScheduleTypeConverters.kt:44</code></summary>

```kotlin
    /** Stores a `ActualDisbursementDateEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromActualDisbursementDate(date: ActualDisbursementDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    /** Reads a `ActualDisbursementDateEntity?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanWithAssociations/LoanAccountSummary.kt`

```kotlin
data class LoanAccountSummary(
```
A loan account summary.

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
Lifecycle status of a loan.

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
A loan timeline.

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
Kind of a loan.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/converter/ListTypeConverters.kt:111</code></summary>

```kotlin
    /** Stores a `LoanType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromType(type: LoanType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `LoanType?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/loan/loanWithAssociations/LoanWithAssociations.kt`

```kotlin
data class LoanWithAssociations(
```
A loan with associations.

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
A saving account currency.

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
Request body for a loan reschedule approval.

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
Request body for a loan reschedule rejection.

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
Request body for a loan reschedule.

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
Server response to a loan reschedule request.

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
Server template for a loan reschedule: the options its form offers.

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
One reschedule reason choice offered by a template.

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
Lifecycle status of a reschedule.

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
A reschedule timeline.

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
One account type choice offered by a template.

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
One client choice offered by a template.

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
One office choice offered by a template.

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
Kind of a charge calculation.

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
Kind of a charge time.

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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/entity/SavingProductsTemplate.kt:65</code></summary>

```kotlin
    val incomeAccountOptions: List<AccountOptions>? = null,
    /** The field officers that may be assigned. */
    val fieldOfficerOptions: List<FieldOfficerOptions>? = null,

    /** The charges that may be added. */
    val chargeOptions: List<ChargeOptions>? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/InterestCalculationDaysInYearType.kt`

```kotlin
class InterestCalculationDaysInYearType(
```
Kind of an interest calculation days in year.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:52</code></summary>

```kotlin
    /** Stores an `InterestCalculationDaysInYearType?` in a JSON column. */
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
Kind of an interest calculation.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:66</code></summary>

```kotlin
    /** Stores a `InterestCalculationType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromInterestCalculationType(type: InterestCalculationType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `InterestCalculationType?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/InterestCompoundingPeriodType.kt`

```kotlin
data class InterestCompoundingPeriodType(
```
Kind of an interest compounding period.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:78</code></summary>

```kotlin
    /** Stores an `InterestCompoundingPeriodType?` in a JSON column. */
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
Kind of an interest posting period.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:27</code></summary>

```kotlin
    /** Stores an `InterestPostingPeriodType?` in a JSON column. */
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
Kind of a lockin period frequency.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:43</code></summary>

```kotlin
    /** Stores a `LockinPeriodFrequencyType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromLockinPeriodFrequencyType(type: LockinPeriodFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `LockinPeriodFrequencyType?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/account/saving/SavingsAccountTransactionResponse.kt`

```kotlin
data class SavingsAccountTransactionResponse(
```
Created by ishankhanna on 12/06/14.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/savings/SavingsCommandRepository.kt:55</code></summary>

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
A timeline.

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
A share account as listed on a client.

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
Lifecycle status of a share account.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/account/share/ShareAccounts.kt:36</code></summary>

```kotlin
    val productName: String? = null,
    /** The record's Fineract status. */
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
Server template for a checker inbox search: the options its form offers.

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
A checker task.

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
A reschedule loans task.

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
A reschedule reason code value.

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
Request body for an assign staff.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/ClientAddressEntity.kt`

```kotlin
data class ClientAddressEntity(
```
Room row for a client address.

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
Request body for a client address.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt:79</code></summary>

```kotlin
    suspend fun addClientPinpointLocation(
        clientId: Int,
        clientAddressRequest: ClientAddressRequest?,
    ): MutationResult<PinpointLocationActionResponse>

    /** Deletes a client's pinpoint location. */
    suspend fun deleteClientPinpointLocation(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/ClientAddressResponse.kt`

```kotlin
data class ClientAddressResponse(
```
Server response to a client address request.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientRepository.kt:113</code></summary>

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
Request body for a client close.

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
Request body for a collateral.

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
Request body for a propose transfer.

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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardErrors.kt:210</code></summary>

```kotlin
                        .clip(KptTheme.shapes.small)
                        .background(f.moneyNegative)
                        .clickable(role = Role.Button, onClick = onRetry)
                        .padding(horizontal = 3.tw, vertical = 1.5f.tw),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.5f.tw),
                ) {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/clients/UpdateSavingsAccountRequest.kt`

```kotlin
data class UpdateSavingsAccountRequest(
```
Request body for an update savings account.

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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/ClientCollectionSheet.kt:29</code></summary>

```kotlin
    var loans: ArrayList<LoanCollectionSheet>? = null,
    /** Whether the client attended the meeting. */
    var attendanceType: AttendanceTypeOption? = null,

    /** The savings accounts. */
    var savings: ArrayList<SavingsCollectionSheet> = ArrayList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/BulkSavingsDueTransaction.kt`

```kotlin
data class BulkSavingsDueTransaction(
```
Created by Tarun on 31-07-17.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/collectionsheet/CollectionSheetPayload.kt:29</code></summary>

```kotlin
    var bulkRepaymentTransactions: MutableList<BulkRepaymentTransactions> = ArrayList(),
    /** The savings deposits collected at the meeting. */
    var bulkSavingsDueTransactions: MutableList<BulkSavingsDueTransaction> = ArrayList(),

    /** The server id of the meeting calendar. */
    var calendarId: Int? = 0,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/CollectionMeetingCalendar.kt`

```kotlin
data class CollectionMeetingCalendar(
```
Created by ishankhanna on 16/07/14.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterWithAssociations.kt:65</code></summary>

```kotlin
    var groupMembers: List<GroupEntity> = ArrayList(),
    /** The calendar of the collection meetings. */
    var collectionMeetingCalendar: CollectionMeetingCalendar = CollectionMeetingCalendar(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/CollectionSheetRequestPayload.kt`

```kotlin
data class CollectionSheetRequestPayload(
```
Created by Tarun on 25-07-2017.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/collectionsheet/CollectionSheetRepository.kt:55</code></summary>

```kotlin

    /** Generates a centre's productive collection sheet, or null when there is none. */
    suspend fun productiveSheet(centerId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?

    /** Generates a group's collection sheet, or null when there is none. */
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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/collectionsheets/LoanCollectionSheet.kt`

```kotlin
data class LoanCollectionSheet(
```
A loan collection sheet.

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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/collectionsheet/entity/CollectionSheetResponse.kt:38</code></summary>

```kotlin

    /** The savings products the sheet covers. */
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
An account.

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
An app group.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Client.kt:26</code></summary>

```kotlin

    /** The group the client belongs to. */
    var mifosGroup: AppGroup? = null,

    /** The loans. */
    val loans: List<Loan> = ArrayList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/AttendanceType.kt`

```kotlin
data class AttendanceType(
```
Kind of an attendance.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Client.kt:23</code></summary>

```kotlin

    /** Whether the client attended the meeting. */
    var attendanceType: AttendanceType? = null,

    /** The group the client belongs to. */
    var mifosGroup: AppGroup? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Client.kt`

```kotlin
data class Client(
```
A client.

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
A collection meeting calendar.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/entity/CenterWithAssociations.kt:65</code></summary>

```kotlin
    var groupMembers: List<GroupEntity> = ArrayList(),
    /** The calendar of the collection meetings. */
    var collectionMeetingCalendar: CollectionMeetingCalendar = CollectionMeetingCalendar(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/CollectionSheet.kt`

```kotlin
data class CollectionSheet(
```
A collection sheet.

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
A currency.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:393</code></summary>

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
Kind of an entity.

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
A loan.

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
A meeting center.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/OfflineCenter.kt:21</code></summary>

```kotlin

    /** The centres whose meetings fall on the date. */
    var meetingFallCenters: Array<MeetingCenter>? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/MeetingDate.kt`

```kotlin
data class MeetingDate(
```
A meeting date.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/CollectionMeetingCalendar.kt:36</code></summary>

```kotlin

    /** The date of the meeting. */
    var meetingCalendarDate: MeetingDate? = null,

    /** True when the meeting repeats. */
    var isRepeating: Boolean = false,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/OfflineCenter.kt`

```kotlin
data class OfflineCenter(
```
An offline center.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/center/api/CenterApi.kt:172</code></summary>

```kotlin
        @Query("officeId") officeId: Int,
        @Query("staffId") staffId: Int,
    ): List<OfflineCenter>

    /**
     * This is the service to activate the center
     * REST ENT POINT
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/databaseobjects/Status.kt`

```kotlin
data class Status(
```
Lifecycle status flags of an account.

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
An error the server returned for a request.

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
An argument of a validation error.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/error/Errors.kt`

```kotlin
data class Errors(
```
The validation errors of a failed request.

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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardErrors.kt:90</code></summary>

```kotlin
 */
@Composable
internal fun errorKind(error: Throwable?, offline: Boolean): String {
    if (offline || error == null) {
        return stringResource(
            if (offline) {
                Res.string.feature_home_dashboard_error_kind_offline
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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/note/CreateNoteResponseDto.kt`

```kotlin
data class CreateNoteResponseDto(
```
Server response to a create note request.

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
Server response to a delete note request.

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
Request body for a note.

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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/note/UpdateNoteResponseDto.kt`

```kotlin
data class UpdateNoteResponseDto(
```
Server response to an update note request.

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
Transfer shape of a changes.

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/AccountingRule.kt`

```kotlin
data class AccountingRule(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/LoanProducts.kt:195</code></summary>

```kotlin
    /** The accounting rule (none, cash or accrual). */
    @SerialName("accountingRule")
    var accountingRule: AccountingRule? = null,

    /** True when the loan can be disbursed in several tranches. */
    @SerialName("multiDisburseLoan")
    var multiDisburseLoan: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/AllowAttributeOverrides.kt`

```kotlin
data class AllowAttributeOverrides(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/LoanProducts.kt:219</code></summary>

```kotlin
    /** Which product attributes an account may override. */
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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/Currency.kt`

```kotlin
data class Currency(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:393</code></summary>

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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:173</code></summary>

```kotlin
    val product: Product? = null,
    /** How many days a month has for interest calculation (actual or 30). */
    val daysInMonthType: DaysInMonthType? = null,
    /** How many days a year has for interest calculation (actual, 360, 364 or 365). */
    val daysInYearType: DaysInYearType? = null,

    /** True when interest is recalculated as repayments arrive. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/DaysInYearType.kt`

```kotlin
data class DaysInYearType(
```
Created by Rajan Maurya on 15/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:175</code></summary>

```kotlin
    val daysInMonthType: DaysInMonthType? = null,
    /** How many days a year has for interest calculation (actual, 360, 364 or 365). */
    val daysInYearType: DaysInYearType? = null,

    /** True when interest is recalculated as repayments arrive. */
    val isInterestRecalculationEnabled: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/InterestCalculationDaysInYearType.kt`

```kotlin
data class InterestCalculationDaysInYearType(
```
Created by nkiboi on 12/15/2015.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:52</code></summary>

```kotlin
    /** Stores an `InterestCalculationDaysInYearType?` in a JSON column. */
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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/organisations/InterestPostingPeriodType.kt`

```kotlin
data class InterestPostingPeriodType(
```
Created by nkiboi on 12/15/2015.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:27</code></summary>

```kotlin
    /** Stores an `InterestPostingPeriodType?` in a JSON column. */
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanTypeConverters.kt:91</code></summary>

```kotlin
    /** Stores a `InterestRateFrequencyType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromInterestRateFrequencyType(type: InterestRateFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `InterestRateFrequencyType?` back from the JSON column it is stored in. */
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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/payloads/RecurringDepositAccountPayload.kt`

```kotlin
data class RecurringDepositAccountPayload(
```
Request body for a recurring deposit account.

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
A charge item.

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
Server response to a save request.

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
Row deserializer.

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
One search hit: a client, group, centre or account.

<details><summary>Used in the template — <code>feature/searchrecord/src/commonMain/kotlin/kpt/feature/searchrecord/navigation/SearchRecordRoute.kt:37</code></summary>

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
Kind of a record.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigation.kt:494</code></summary>

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

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/survey/SurveyCommandRepository.kt:29</code></summary>

```kotlin

    /** Submits a scorecard for a survey. */
    suspend fun submitScore(surveyId: Int, scorecardPayload: Scorecard?): MutationResult<Scorecard>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/surveys/ScorecardValues.kt`

```kotlin
data class ScorecardValues(
```
Created by Nasim Banu on 28,January,2016.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/surveys/Scorecard.kt:30</code></summary>

```kotlin
    var createdOn: List<Int> = emptyList(),
    /** The answers in the scorecard. */
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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeAppliesTo.kt`

```kotlin
data class ChargeAppliesTo(
```
Created by mayankjindal on 13/12/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt:35</code></summary>

```kotlin
    val chargeTimeType: ChargeTimeType? = null,
    /** What the charge applies to (loan, savings, client, …). */
    val chargeAppliesTo: ChargeAppliesTo? = null,
    /** How the charge amount is calculated (flat, percent of amount, …). */
    val chargeCalculationType: ChargeCalculationType? = null,
    /** How the charge is paid (regular or from a linked savings account). */
    val chargePaymentMode: ChargePaymentMode? = null,
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt:39</code></summary>

```kotlin
    val chargeCalculationType: ChargeCalculationType? = null,
    /** How the charge is paid (regular or from a linked savings account). */
    val chargePaymentMode: ChargePaymentMode? = null,
    /** The GL account the charge is booked to. */
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
A currency.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:393</code></summary>

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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt:41</code></summary>

```kotlin
    val chargePaymentMode: ChargePaymentMode? = null,
    /** The GL account the charge is booked to. */
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
    /** The server id of this record. */
    var id: Int? = null,

    /**
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
    /** How the principal is amortised (equal installments or equal principal). */
    var amortizationType: Boolean? = null,

    /** The interest method (declining balance or flat). */
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/GroupLoanTemplate.kt:140</code></summary>

```kotlin

    /** The meeting calendars the loan may follow. */
    var calendarOptions: List<CalendarOptions>? = null,

    /** True when the loan can be disbursed in several tranches. */
    var multiDisburseLoan: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/CalendarOptionsType.kt`

```kotlin
data class CalendarOptionsType(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/CalendarOptions.kt:41</code></summary>

```kotlin

    /** The record's type. */
    var type: CalendarOptionsType? = null,

    /** True when the meeting repeats. */
    var repeating: Boolean? = null,
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
    /** The server id of this record. */
    val id: Int,
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt:39</code></summary>

```kotlin
    val chargeCalculationType: ChargeCalculationType? = null,
    /** How the charge is paid (regular or from a linked savings account). */
    val chargePaymentMode: ChargePaymentMode? = null,
    /** The GL account the charge is booked to. */
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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:393</code></summary>

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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:173</code></summary>

```kotlin
    val product: Product? = null,
    /** How many days a month has for interest calculation (actual or 30). */
    val daysInMonthType: DaysInMonthType? = null,
    /** How many days a year has for interest calculation (actual, 360, 364 or 365). */
    val daysInYearType: DaysInYearType? = null,

    /** True when interest is recalculated as repayments arrive. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/DaysInYearType.kt`

```kotlin
data class DaysInYearType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:175</code></summary>

```kotlin
    val daysInMonthType: DaysInMonthType? = null,
    /** How many days a year has for interest calculation (actual, 360, 364 or 365). */
    val daysInYearType: DaysInYearType? = null,

    /** True when interest is recalculated as repayments arrive. */
    val isInterestRecalculationEnabled: Boolean? = null,
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/Group.kt:53</code></summary>

```kotlin

    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
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

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRateFrequencyType.kt`

```kotlin
data class InterestRateFrequencyType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/converter/LoanTypeConverters.kt:91</code></summary>

```kotlin
    /** Stores a `InterestRateFrequencyType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromInterestRateFrequencyType(type: InterestRateFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `InterestRateFrequencyType?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRateFrequencyTypeOptions.kt`

```kotlin
data class InterestRateFrequencyTypeOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:145</code></summary>

```kotlin
    val repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions> = emptyList(),
    /** The interest rate frequency choices. */
    val interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions> = emptyList(),
    /** The amortisation choices. */
    val amortizationTypeOptions: List<AmortizationTypeOptions> = emptyList(),
    /** The interest method choices. */
    val interestTypeOptions: List<InterestTypeOptions> = emptyList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationCompoundingType.kt`

```kotlin
data class InterestRecalculationCompoundingType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationData.kt:26</code></summary>

```kotlin

    /** What interest recalculation compounds (interest, fees or both). */
    var interestRecalculationCompoundingType: InterestRecalculationCompoundingType? = null,

    /** How the schedule is adjusted when repayments arrive early. */
    var rescheduleStrategyType: RescheduleStrategyType? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationData.kt`

```kotlin
data class InterestRecalculationData(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/GroupLoanTemplate.kt:173</code></summary>

```kotlin

    /** The product's interest-recalculation settings. */
    var interestRecalculationData: InterestRecalculationData? = null,

    /** True when installment amounts may vary. */
    var isVariableInstallmentsAllowed: Boolean? = null,
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:157</code></summary>

```kotlin
    val chargeOptions: List<ChargeOptions> = emptyList(),
    /** The collateral types that may be pledged. */
    val loanCollateralOptions: List<LoanCollateralOptions> = emptyList(),

    /** True when the loan can be disbursed in several tranches. */
    val multiDisburseLoan: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/LoanOfficerOption.kt`

```kotlin
data class LoanOfficerOption(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:131</code></summary>

```kotlin
    val dataTables: ArrayList<DataTableEntity> = ArrayList(),
    /** The loan officers that may be assigned. */
    val loanOfficerOptions: List<LoanOfficerOption> = emptyList(),
    /** The loan purposes that may be chosen. */
    val loanPurposeOptions: List<LoanPurposeOptions> = emptyList(),
    /** The funds that may be chosen. */
    val fundOptions: List<FundOptions> = emptyList(),
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/GroupLoanTemplate.kt:161</code></summary>

```kotlin

    /** The charges applied when an installment is overdue. */
    var overdueCharges: List<OverdueCharges>? = null,

    /** How many days a month has for interest calculation (actual or 30). */
    var daysInMonthType: DaysInMonthType? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/PreClosureInterestCalculationStrategy.kt`

```kotlin
data class PreClosureInterestCalculationStrategy(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationData.kt:44</code></summary>

```kotlin

    /** How interest is calculated when the loan is closed early. */
    var preClosureInterestCalculationStrategy: PreClosureInterestCalculationStrategy? = null,

    /** True when interest compounds at end of day. */
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/client/dao/ClientDao.kt:146</code></summary>

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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationData.kt:32</code></summary>

```kotlin

    /** The unit of the recalculation rest frequency. */
    var recalculationRestFrequencyType: RecalculationRestFrequencyType? = null,

    /** How many rest-frequency units between recalculations. */
    var recalculationRestFrequencyInterval: Int? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/RepaymentFrequencyDaysOfWeekTypeOptions.kt`

```kotlin
data class RepaymentFrequencyDaysOfWeekTypeOptions(
```
Created by mayankjindal on 02/10/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:143</code></summary>

```kotlin
    val repaymentFrequencyNthDayTypeOptions: List<RepaymentFrequencyNthDayTypeOptions> = emptyList(),
    /** The weekday choices for the repayment day. */
    val repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions> = emptyList(),
    /** The interest rate frequency choices. */
    val interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions> = emptyList(),
    /** The amortisation choices. */
    val amortizationTypeOptions: List<AmortizationTypeOptions> = emptyList(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/RepaymentFrequencyNthDayTypeOptions.kt`

```kotlin
data class RepaymentFrequencyNthDayTypeOptions(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/loan/entity/LoanTemplate.kt:141</code></summary>

```kotlin
    val repaymentFrequencyTypeOptions: List<RepaymentFrequencyTypeOptions> = emptyList(),
    /** The nth-day choices for the repayment day. */
    val repaymentFrequencyNthDayTypeOptions: List<RepaymentFrequencyNthDayTypeOptions> = emptyList(),
    /** The weekday choices for the repayment day. */
    val repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions> = emptyList(),
    /** The interest rate frequency choices. */
    val interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions> = emptyList(),
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/CalendarOptions.kt:56</code></summary>

```kotlin

    /** Which occurrence in the month the meeting falls on. */
    var repeatsOnNthDayOfMonth: RepeatsOnNthDayOfMonth? = null,

    /** Minutes before the meeting the first reminder fires. */
    var firstReminder: Int? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/RescheduleStrategyType.kt`

```kotlin
data class RescheduleStrategyType(
```
Created by Rajan Maurya on 16/07/16.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/InterestRecalculationData.kt:29</code></summary>

```kotlin

    /** How the schedule is adjusted when repayments arrive early. */
    var rescheduleStrategyType: RescheduleStrategyType? = null,

    /** The unit of the recalculation rest frequency. */
    var recalculationRestFrequencyType: RecalculationRestFrequencyType? = null,
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

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/loan/ChargeOptions.kt:50</code></summary>

```kotlin

    /** The tax group applied to the charge. */
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
A Fineract code value: an id, a code and its display text.

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
An account chart.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/AccountChart.kt:16</code></summary>

```kotlin
/** An account chart. */
@Serializable
data class AccountChart(
    /** The server id of the account. */
    val accountId: Int? = null,
    /** The account number the payment was made from or to. */
    val accountNumber: String? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AttributeName.kt`

```kotlin
data class AttributeName(
```
A client attribute an incentive is conditioned on.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/Incentive.kt:22</code></summary>

```kotlin
    val amount: Double? = null,
    /** The client attribute the incentive depends on. */
    val attributeName: AttributeName? = null,
    /** The attribute value the incentive applies to. */
    val attributeValue: String? = null,
    /** The description of that attribute value. */
    val attributeValueDesc: String? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AttributeNameOption.kt`

```kotlin
data class AttributeNameOption(
```
One attribute name choice offered by a template.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:19</code></summary>

```kotlin
data class AccountChart(
    /** The client attributes incentives may depend on. */
    val attributeNameOptions: List<AttributeNameOption>? = null,
    /** The rate slabs of the chart. */
    val chartSlabs: List<ChartSlab>? = null,
//    val clientClassificationOptions: List<Any?>? = null,
    /** The client types that may be chosen. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ChartSlab.kt`

```kotlin
data class ChartSlab(
```
A chart slab.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:21</code></summary>

```kotlin
    val attributeNameOptions: List<AttributeNameOption>? = null,
    /** The rate slabs of the chart. */
    val chartSlabs: List<ChartSlab>? = null,
//    val clientClassificationOptions: List<Any?>? = null,
    /** The client types that may be chosen. */
    val clientTypeOptions: List<ClientTypeOption>? = null,
    /** The comparisons incentives may use. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ClientTypeOption.kt`

```kotlin
data class ClientTypeOption(
```
One client type choice offered by a template.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:24</code></summary>

```kotlin
//    val clientClassificationOptions: List<Any?>? = null,
    /** The client types that may be chosen. */
    val clientTypeOptions: List<ClientTypeOption>? = null,
    /** The comparisons incentives may use. */
    val conditionTypeOptions: List<ConditionTypeOption>? = null,
    /** The date this ends. */
    val endDate: List<Int>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ConditionType.kt`

```kotlin
data class ConditionType(
```
Kind of a condition.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/Incentive.kt:28</code></summary>

```kotlin
    val attributeValueDesc: String? = null,
    /** The comparison the incentive uses (equal, greater than, …). */
    val conditionType: ConditionType? = null,
    /** The kind of entity this record belongs to (client, group, loan, …). */
    val entityType: EntityType? = null,
    /** How the incentive changes the rate (fixed, increase, decrease). */
    val incentiveType: IncentiveType? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ConditionTypeOption.kt`

```kotlin
data class ConditionTypeOption(
```
One condition type choice offered by a template.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:26</code></summary>

```kotlin
    val clientTypeOptions: List<ClientTypeOption>? = null,
    /** The comparisons incentives may use. */
    val conditionTypeOptions: List<ConditionTypeOption>? = null,
    /** The date this ends. */
    val endDate: List<Int>? = null,
    /** The entity types the chart may be attached to. */
    val entityTypeOptions: List<EntityTypeOption>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/Currency.kt`

```kotlin
data class Currency(
```
A currency.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:393</code></summary>

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
Kind of an entity.

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
One entity type choice offered by a template.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:30</code></summary>

```kotlin
    val endDate: List<Int>? = null,
    /** The entity types the chart may be attached to. */
    val entityTypeOptions: List<EntityTypeOption>? = null,
    /** The date this starts from. */
    val fromDate: List<Int>? = null,
//    val genderOptions: List<Any?>? = null,
    /** The incentive types that may be chosen. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/FieldOfficerOption.kt`

```kotlin
data class FieldOfficerOption(
```
One field officer choice offered by a template.

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
One maturity instruction choice offered by a template.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/savings/FixedDepositTemplate.kt:73</code></summary>

```kotlin
    val lockinPeriodFrequencyTypeOptions: List<LockinPeriodFrequencyTypeOption>? = null,
    /** The maturity instructions that may be chosen. */
    val maturityInstructionOptions: List<MaturityInstructionOption>? = null,
    /** The interest-rate chart of the deposit. */
    val accountChart: AccountChart? = null,

    /** The charges that may be added. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/Timeline.kt`

```kotlin
class Timeline(
```
A timeline.

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
One withdrawal fee type choice offered by a template.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDepositAccountTemplate.kt:112</code></summary>

```kotlin
    val withdrawalFeeForTransfers: Boolean? = null,
    /** The withdrawal fee choices. */
    val withdrawalFeeTypeOptions: List<WithdrawalFeeTypeOption>? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/charge/ChargeAppliesTo.kt`

```kotlin
data class ChargeAppliesTo(
```
What a charge applies to: loans, savings or clients.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeAppliesTo.kt:18</code></summary>

```kotlin
 */
@Serializable
data class ChargeAppliesTo(
    /** The server id of this record. */
    val id: Int,
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/charge/ChargeCalculationType.kt`

```kotlin
data class ChargeCalculationType(
```
Kind of a charge calculation.

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
One charge choice offered by a template.

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
A charge payment mode.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/client/ChargeOptions.kt:39</code></summary>

```kotlin
    val chargeCalculationType: ChargeCalculationType? = null,
    /** How the charge is paid (regular or from a linked savings account). */
    val chargePaymentMode: ChargePaymentMode? = null,
    /** The GL account the charge is booked to. */
    val incomeOrLiabilityAccount: IncomeOrLiabilityAccount? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/charge/ChargeTimeType.kt`

```kotlin
data class ChargeTimeType(
```
Kind of a charge time.

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
Kind of a deposit.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/converter/CenterTypeConverters.kt:37</code></summary>

```kotlin
    /** Stores a `SavingAccountDepositTypeEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromDepositType(type: SavingAccountDepositTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `SavingAccountDepositTypeEntity?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/InMultiplesOfDepositTermType.kt`

```kotlin
data class InMultiplesOfDepositTermType(
```
Kind of an in multiples of deposit term.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt:63</code></summary>

```kotlin
    val id: Int? = null,
    /** The unit the deposit term is counted in multiples of. */
    val inMultiplesOfDepositTermType: InMultiplesOfDepositTermType? = null,
    /** How many days a year has for interest calculation (360, 365, actual). */
    val interestCalculationDaysInYearType: InterestCalculationDaysInYearType? = null,
    /** How interest is calculated on the balance (daily or average daily balance). */
    val interestCalculationType: InterestCalculationType? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/MaxDepositTermType.kt`

```kotlin
data class MaxDepositTermType(
```
Kind of a max deposit term.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt:87</code></summary>

```kotlin
    val maturityDate: List<Int>? = null,
    /** The unit of the maximum deposit term. */
    val maxDepositTermType: MaxDepositTermType? = null,
    /** The shortest deposit term. */
    val minDepositTerm: Int? = null,
    /** The unit of the minimum deposit term. */
    val minDepositTermType: MinDepositTermType? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/MinDepositTermType.kt`

```kotlin
data class MinDepositTermType(
```
Kind of a min deposit term.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt:91</code></summary>

```kotlin
    val minDepositTerm: Int? = null,
    /** The unit of the minimum deposit term. */
    val minDepositTermType: MinDepositTermType? = null,
    /** The nominal annual interest rate, in percent. */
    val nominalAnnualInterestRate: Double? = null,
    /** True when a penalty applies if the deposit is closed early. */
    val preClosurePenalApplicable: Boolean? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/Incentive.kt`

```kotlin
data class Incentive(
```
An incentive.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ChartSlab.kt:29</code></summary>

```kotlin
    val fromPeriod: Int? = null,
    /** The incentives of this rate slab. */
    val incentives: List<Incentive>? = null,
    /** The unit of the period range. */
    val periodType: PeriodType? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/IncentiveType.kt`

```kotlin
data class IncentiveType(
```
Kind of an incentive.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/Incentive.kt:32</code></summary>

```kotlin
    val entityType: EntityType? = null,
    /** How the incentive changes the rate (fixed, increase, decrease). */
    val incentiveType: IncentiveType? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/IncentiveTypeOption.kt`

```kotlin
data class IncentiveTypeOption(
```
One incentive type choice offered by a template.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:35</code></summary>

```kotlin
//    val genderOptions: List<Any?>? = null,
    /** The incentive types that may be chosen. */
    val incentiveTypeOptions: List<IncentiveTypeOption>? = null,
    /** True when the chart groups its slabs by amount before period. */
    val isPrimaryGroupingByAmount: Boolean? = null,
    /** The display name. */
    val name: String? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/InterestCalculationDaysInYearType.kt`

```kotlin
data class InterestCalculationDaysInYearType(
```
Kind of an interest calculation days in year.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:52</code></summary>

```kotlin
    /** Stores an `InterestCalculationDaysInYearType?` in a JSON column. */
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
One interest calculation days in year type choice offered by a template.

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
Kind of an interest calculation.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:66</code></summary>

```kotlin
    /** Stores a `InterestCalculationType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromInterestCalculationType(type: InterestCalculationType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `InterestCalculationType?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/interest/InterestCalculationTypeOption.kt`

```kotlin
data class InterestCalculationTypeOption(
```
One interest calculation type choice offered by a template.

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
Kind of an interest compounding period.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:78</code></summary>

```kotlin
    /** Stores an `InterestCompoundingPeriodType?` in a JSON column. */
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
One interest compounding period type choice offered by a template.

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
Kind of an interest posting period.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:27</code></summary>

```kotlin
    /** Stores an `InterestPostingPeriodType?` in a JSON column. */
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
One interest posting period type choice offered by a template.

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
One pre closure penal interest on type choice offered by a template.

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
Kind of a lockin period frequency.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:43</code></summary>

```kotlin
    /** Stores a `LockinPeriodFrequencyType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromLockinPeriodFrequencyType(type: LockinPeriodFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `LockinPeriodFrequencyType?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/period/LockinPeriodFrequencyTypeOption.kt`

```kotlin
data class LockinPeriodFrequencyTypeOption(
```
One lockin period frequency type choice offered by a template.

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
One period frequency type choice offered by a template.

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
Kind of a period.

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
One product choice offered by a template.

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
A savings transaction tag.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/saving/AccountOptions.kt:49</code></summary>

```kotlin

    /** The tag of the GL account. */
    var tagId: TagId? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/objects/users/User.kt`

```kotlin
data class User(
```
A signed-in user.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:131</code></summary>

```kotlin
        """"httpStatusCode":"403","defaultUserMessage":"Insufficient privileges to perform this action.",""" +
        """"userMessageGlobalisationCode":"error.msg.not.authorized",""" +
        """"errors":[{"developerMessage":"User has no authority to READ collectionsheets",""" +
        """"defaultUserMessage":"User has no authority to READ collectionsheets",""" +
        """"userMessageGlobalisationCode":"error.msg.not.authorized","parameterName":"id","args":[]}]}""" + "\"",
)
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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/office/converter/OfficeTypeConverters.kt:29</code></summary>

```kotlin
    /** Stores a `OfficeOpeningDateEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromOfficeOpeningDate(status: OfficeOpeningDateEntity?): String? {
        return status?.let { Json.encodeToString(it) }
    }

    /** Reads a `OfficeOpeningDateEntity?` back from the JSON column it is stored in. */
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
enum class RecordKind
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
enum class RecordSyncStatus
```
Lifecycle status of a record sync.

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
enum class SyncJobStatus
```
Lifecycle status of a sync job.

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
enum class RefreshMode
```
Refresh mode.

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
enum class RecentKind
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
An account chart.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:17</code></summary>

```kotlin
/** An account chart. */
@Serializable
data class AccountChart(
    /** The client attributes incentives may depend on. */
    val attributeNameOptions: List<AttributeNameOption>? = null,
    /** The rate slabs of the chart. */
    val chartSlabs: List<ChartSlab>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/AttributeName.kt`

```kotlin
data class AttributeName(
```
A client attribute an incentive is conditioned on.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AttributeName.kt:15</code></summary>

```kotlin
/** A client attribute an incentive is conditioned on. */
@Serializable
data class AttributeName(
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/ChartSlab.kt`

```kotlin
data class ChartSlab(
```
A chart slab.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/AccountChart.kt:21</code></summary>

```kotlin
    val attributeNameOptions: List<AttributeNameOption>? = null,
    /** The rate slabs of the chart. */
    val chartSlabs: List<ChartSlab>? = null,
//    val clientClassificationOptions: List<Any?>? = null,
    /** The client types that may be chosen. */
    val clientTypeOptions: List<ClientTypeOption>? = null,
    /** The comparisons incentives may use. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/ConditionType.kt`

```kotlin
data class ConditionType(
```
Kind of a condition.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ConditionType.kt:15</code></summary>

```kotlin
/** Kind of a condition. */
@Serializable
data class ConditionType(
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/Currency.kt`

```kotlin
data class Currency(
```
A currency.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:393</code></summary>

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
Kind of an entity.

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
An incentive.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/ChartSlab.kt:29</code></summary>

```kotlin
    val fromPeriod: Int? = null,
    /** The incentives of this rate slab. */
    val incentives: List<Incentive>? = null,
    /** The unit of the period range. */
    val periodType: PeriodType? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/IncentiveType.kt`

```kotlin
data class IncentiveType(
```
Kind of an incentive.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/incentive/Incentive.kt:32</code></summary>

```kotlin
    val entityType: EntityType? = null,
    /** How the incentive changes the rate (fixed, increase, decrease). */
    val incentiveType: IncentiveType? = null,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/LockinPeriodFrequencyType.kt`

```kotlin
data class LockinPeriodFrequencyType(
```
Kind of a lockin period frequency.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:43</code></summary>

```kotlin
    /** Stores a `LockinPeriodFrequencyType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromLockinPeriodFrequencyType(type: LockinPeriodFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `LockinPeriodFrequencyType?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/PeriodType.kt`

```kotlin
data class PeriodType(
```
Kind of a period.

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
A recurring deposit.

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
Server template for a recurring deposit account: the options its form offers.

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
Kind of a recurring frequency.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt:99</code></summary>

```kotlin
    val recurringFrequency: Int? = null,
    /** The unit of the deposit frequency. */
    val recurringFrequencyType: RecurringFrequencyType? = null,
    /** The record's Fineract status. */
    val status: Status? = null,
    /** The account's running totals. */
    val summary: Summary? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/Status.kt`

```kotlin
data class Status(
```
Lifecycle status flags of an account.

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
A summary.

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
A timeline.

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
A deposit period frequency.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/RecurringDeposit.kt:45</code></summary>

```kotlin
    val depositPeriod: Int? = null,
    /** The unit of the deposit term. */
    val depositPeriodFrequency: DepositPeriodFrequency? = null,
    /** The server id of the deposit product. */
    val depositProductId: Int? = null,
    /** The name of the deposit product. */
    val depositProductName: String? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/deposit/DepositType.kt`

```kotlin
data class DepositType(
```
Kind of a deposit.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/center/converter/CenterTypeConverters.kt:37</code></summary>

```kotlin
    /** Stores a `SavingAccountDepositTypeEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromDepositType(type: SavingAccountDepositTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `SavingAccountDepositTypeEntity?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/deposit/InMultiplesOfDepositTermType.kt`

```kotlin
data class InMultiplesOfDepositTermType(
```
Kind of an in multiples of deposit term.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/InMultiplesOfDepositTermType.kt:15</code></summary>

```kotlin
/** Kind of an in multiples of deposit term. */
@Serializable
data class InMultiplesOfDepositTermType(
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/deposit/MaxDepositTermType.kt`

```kotlin
data class MaxDepositTermType(
```
Kind of a max deposit term.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/MaxDepositTermType.kt:15</code></summary>

```kotlin
/** Kind of a max deposit term. */
@Serializable
data class MaxDepositTermType(
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/deposit/MinDepositTermType.kt`

```kotlin
data class MinDepositTermType(
```
Kind of a min deposit term.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/objects/template/recurring/deposit/MinDepositTermType.kt:15</code></summary>

```kotlin
/** Kind of a min deposit term. */
@Serializable
data class MinDepositTermType(
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/interest/InterestCalculationDaysInYearType.kt`

```kotlin
data class InterestCalculationDaysInYearType(
```
Kind of an interest calculation days in year.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:52</code></summary>

```kotlin
    /** Stores an `InterestCalculationDaysInYearType?` in a JSON column. */
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
Kind of an interest calculation.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:66</code></summary>

```kotlin
    /** Stores a `InterestCalculationType?` in a JSON column. */
    @ColumnTypeConverter
    fun fromInterestCalculationType(type: InterestCalculationType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `InterestCalculationType?` back from the JSON column it is stored in. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/recurringdeposit/interest/InterestCompoundingPeriodType.kt`

```kotlin
data class InterestCompoundingPeriodType(
```
Kind of an interest compounding period.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsTypeConverters.kt:78</code></summary>

```kotlin
    /** Stores an `InterestCompoundingPeriodType?` in a JSON column. */
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
Kind of an interest posting period.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/savings/converter/SavingsPeriodTypeConverters.kt:27</code></summary>

```kotlin
    /** Stores an `InterestPostingPeriodType?` in a JSON column. */
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
enum class ReportParameterType
```
Kind of a report parameter.

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
Request body for a fixed deposit.

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
One fixed deposit product choice offered by a template.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/savings/FixedDepositTemplate.kt:44</code></summary>

```kotlin
    /** The products that may be chosen. */
    @SerialName("productOptions")
    val productOptions: List<FixedDepositProductOption>? = null,

    /** The field officers that may be assigned. */
    @SerialName("fieldOfficerOptions")
    val fieldOfficerOptions: List<FieldOfficerOptions>? = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/savings/FixedDepositTemplate.kt`

```kotlin
data class FixedDepositTemplate(
```
Server template for a fixed deposit: the options its form offers.

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

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/depositaccounts/ClientDepositAccountsViewModel.kt:175</code></summary>

```kotlin
enum class DepositAccountKind(
    /** The deposit type the accounts are filtered on. */
    val serverType: SavingAccountDepositType.ServerTypes,
) {
    /** Plain savings accounts. */
    SAVINGS(SavingAccountDepositType.ServerTypes.SAVINGS),
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
Request body for a share account.

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
Server template for a share account: the options its form offers.

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
One savings account choice offered by a template.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/savings/FixedDepositTemplate.kt:80</code></summary>

```kotlin
    val chargeOptions: List<ChargeOption>? = null,
    /** The savings accounts. */
    val savingsAccounts: List<SavingsAccountOption>? = null,
)
```

</details>

```kotlin
data class FrequencyTypeOption(
```
One frequency type choice offered by a template.

```kotlin
data class ChargeOptions(
```
One charge choice offered by a template.

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
One repayment row of a collection sheet.

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
The fields a command changed, as the server echoes them.

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
A collateral item a client may pledge.

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
Collateral pledged by a client.

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
Transfer shape of a currency.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoanDisburseTemplateDto.kt:24</code></summary>

```kotlin
    val date: List<Int>,
    /** The currency the amounts are in. */
    val currency: CurrencyDto? = null,
    /** The payment types the user may choose from. */
    val paymentTypeOptions: List<PaymentTypeOptionDto> = emptyList(),
)
```

</details>

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

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/GenericResponse.kt`

```kotlin
data class GenericResponse(
```
Server response to a generic request.

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
Request body for creating an entity.

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
One payment type choice offered by a template.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/loan/dto/LoanDisburseTemplateDto.kt:26</code></summary>

```kotlin
    val currency: CurrencyDto? = null,
    /** The payment types the user may choose from. */
    val paymentTypeOptions: List<PaymentTypeOptionDto> = emptyList(),
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/PinpointLocationActionResponse.kt`

```kotlin
data class PinpointLocationActionResponse(
```
Server response to a pinpoint location action request.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/client/ClientCommandRepository.kt:80</code></summary>

```kotlin
        clientId: Int,
        clientAddressRequest: ClientAddressRequest?,
    ): MutationResult<PinpointLocationActionResponse>

    /** Deletes a client's pinpoint location. */
    suspend fun deleteClientPinpointLocation(
        apptableId: Int,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/shared/ProductOption.kt`

```kotlin
data class ProductOption(
```
One product choice offered by a template.

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
A product currency.

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

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/mifos/staff/api/StaffApi.kt:43</code></summary>

```kotlin
        @Query("loanOfficersOnly") loanOfficersOnly: Boolean? = false,
        @Query("status") status: String? = "active",
    ): List<RetrieveOneResponse>

    /** Reads the staff of an office, active or not. */
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
A timeline.

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

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/survey/entity/ComponentDatasEntity.kt:25</code></summary>

```kotlin
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "ComponentDatas",
)
data class ComponentDatasEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/survey/QuestionDatas.kt`

```kotlin
data class QuestionDatas(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `QuestionDatasEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/survey/dao/SurveyDao.kt:61</code></summary>

```kotlin

    /** Observes the `QuestionDatas` rows whose `surveyId` is `surveyId`, ordered by `sequenceNo`. */
    @Query("SELECT * FROM QuestionDatas WHERE surveyId = :surveyId ORDER BY sequenceNo ASC")
    fun getQuestionDatas(surveyId: Int): Flow<List<QuestionDatasEntity>>

    /** Observes the `ResponseDatas` rows whose `id` is `questionId`, ordered by `sequenceNo`. */
    @Query("SELECT * FROM ResponseDatas WHERE id = :questionId ORDER BY sequenceNo ASC")
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/survey/ResponseDatas.kt`

```kotlin
data class ResponseDatas(
```
GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ResponseDatasEntity`. Same field names as the row; the `core/store/mapping/` mappers convert between them.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/survey/dao/SurveyDao.kt:65</code></summary>

```kotlin

    /** Observes the `ResponseDatas` rows whose `id` is `questionId`, ordered by `sequenceNo`. */
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

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/survey/SurveyRepository.kt:19</code></summary>

```kotlin
interface SurveyRepository {
    /** Surveys the officer can administer in the field. */
    fun surveysStream(scope: CoroutineScope): ScreenDataStream<List<Survey>>
}
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

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:72</code></summary>

```kotlin
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
        override val observeDarkThemeConfig: Flow<DarkThemeConfig> get() = unused()
        override val observeDynamicColorPreference: Flow<Boolean> get() = unused()
        override val observeScreenCapturePreference: Flow<Boolean> get() = unused()
        override val observeAnalyticsConsent: Flow<Boolean> get() = unused()
        override val observeCrashReportingConsent: Flow<Boolean> get() = unused()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/LanguageConfig.kt`

```kotlin
enum class LanguageConfig(
```
Every language the app can be switched to, in the user's OWN language. GENERATED from core/registries/LOCALE_REGISTRY.yaml by `core/scripts/language-picker-sync.sh --write` — DO NOT HAND-EDIT.

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:71</code></summary>

```kotlin
        override val authToken: String? get() = unused()
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
        override val observeDarkThemeConfig: Flow<DarkThemeConfig> get() = unused()
        override val observeDynamicColorPreference: Flow<Boolean> get() = unused()
        override val observeScreenCapturePreference: Flow<Boolean> get() = unused()
        override val observeAnalyticsConsent: Flow<Boolean> get() = unused()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/ThemeBrand.kt`

```kotlin
enum class ThemeBrand(val brandName: String)
```
The selected colour brand. A fork extends this to offer its own palettes.

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:78</code></summary>

```kotlin
        override val observeCrashReportingConsent: Flow<Boolean> get() = unused()
        override suspend fun setLanguage(language: LanguageConfig) = unused()
        override suspend fun setThemeBrand(themeBrand: ThemeBrand) = unused()
        override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) = unused()
        override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) = unused()
        override suspend fun setIsAuthenticated(isAuthenticated: Boolean) = unused()
        override suspend fun setIsUnlocked(isUnlocked: Boolean) = unused()
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

<details><summary>Used in the template — <code>feature/onboarding/src/commonTest/kotlin/kpt/feature/onboarding/ui/OnboardingViewModelTest.kt:67</code></summary>

```kotlin
        }

        override val userData: StateFlow<UserData> get() = unused()
        override fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData> = unused()
        override val authToken: String? get() = unused()
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
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

### `core/model/src/commonMain/kotlin/kpt/core/model/utils/ServerConfig.kt`

```kotlin
data class ServerConfig(
```
A server config.

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
The API base URL: protocol, endpoint and API path, without the port.

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

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigScreen.kt:491</code></summary>

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

_461 type(s), 46 function(s)/property(ies); 489 carry KDoc at source; 2 authored example(s); 451 live call site(s)._
<!-- api-docs:end -->
