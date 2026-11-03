# `feature/client/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 155 tracked files — 117× `.kt`, 32× `.xml`, 3× `.png`, 2× `.md`, 1× `.kts`

**117 Kotlin files** — 27 screen(s), 27 ViewModel(s).

Screens: `ClientAddAddressScreen`, `ClientAddDocumentScreen`, `ClientAddIdentifierScreen`, `ClientAddressScreen`, `ClientApplyNewScreen`, `ClientClosureScreen`, `ClientCollateralDetailsScreen`, `ClientCollateralScreen`, `ClientDefaultAccountScreen`, `ClientDepositAccountsScreen`, `ClientDetailsScreen`, `ClientDocumentPreviewScreen`, `ClientDocumentsScreen`, `ClientEditDetailsScreen`, `ClientEditProfileScreen`, `ClientGeneralScreen`, `ClientIdentifiersScreen`, `ClientListScreen`, `ClientLoanAccountsScreen`, `ClientProfileScreen`, `ClientShareAccountsScreen`, `ClientSignatureScreen`, `ClientStaffScreen`, `ClientTransferScreen`, `CreateNewClientScreen`, `FixedDepositApplicationScreen`, `ShareApplicationScreen`

ViewModels: `ClientAddAddressViewModel`, `ClientAddDocumentViewModel`, `ClientAddIdentifierViewModel`, `ClientAddressViewModel`, `ClientApplyNewViewModel`, `ClientClosureViewModel`, `ClientCollateralDetailsViewModel`, `ClientCollateralViewModel`, `ClientDefaultAccountViewModel`, `ClientDepositAccountsViewModel`, `ClientDetailsViewModel`, `ClientDocumentPreviewViewModel`, `ClientDocumentsViewModel`, `ClientEditDetailsViewModel`, `ClientEditProfileViewModel`, `ClientGeneralViewModel`, `ClientIdentifiersViewModel`, `ClientListViewModel`, `ClientLoanAccountsViewModel`, `ClientProfileViewModel`, `ClientShareAccountsViewModel`, `ClientSignatureViewModel`, `ClientStaffViewModel`, `ClientTransferViewModel`, `CreateNewClientViewModel`, `FixedDepositApplicationViewModel`, `ShareApplicationViewModel`

**Consumes:** `core/store/client`, `core/store/document`, `core/store/office`, `core/store/recent`, `core/store/savings`, `core/store/staff`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/client/MIGRATION.md` | feature/client — migration ledger |
| `feature/client/README.md` | :feature:client |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/client sha=cb28df6cf8bf98eb3cf87a0ab2c6721f4047fff0 -->
## API reference

_Generated from `feature/client` at tree `cb28df6cf8bf` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/client/src/commonMain/kotlin/kpt/feature/client/depositaccounts/ClientDepositAccountsViewModel.kt`

```kotlin
enum class DepositAccountKind(
```
Which deposit product this screen lists. `serverType` is the discriminator the three originals differed by, and the ONLY thing that differed between them.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/depositaccounts/ClientDepositAccountsScreen.kt:100</code></summary>

```kotlin
    onViewAccount: ((accountId: Int, accountEndpoint: String) -> Unit)? = null,
    onApproveAccount: ((Int) -> Unit)? = null,
    onAddAccount: ((clientId: Int, kind: DepositAccountKind) -> Unit)? = null,
    viewModel: ClientDepositAccountsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
```

</details>

### `feature/client/src/commonMain/kotlin/kpt/feature/client/details/components/ClientDetailsActionItem.kt`

```kotlin
sealed class ClientDetailsActionItem(
```
Ported verbatim from `6b66e8a43:feature/client/clientDetailsProfile/ClientProfileDetailsAction.kt`. Eleven items declared, EIGHT rendered.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/details/ClientDetailsScreen.kt:126</code></summary>

```kotlin
    val client by viewModel.client.collectAsStateWithLifecycle()
    // The wired destination of each action row; an action with none (or not in the rendered list) is absent.
    val actionHandlers = mapOf<ClientDetailsActionItem, ((Int) -> Unit)?>(
        ClientDetailsActionItem.ApplyNewApplication to onApplyNewApplication,
        ClientDetailsActionItem.ClosureApplication to onClosureApplication,
        ClientDetailsActionItem.TransferClient to onTransferClient,
        ClientDetailsActionItem.AssignStaff to onAssignStaff,
```

</details>

### `feature/client/src/commonMain/kotlin/kpt/feature/client/di/ClientModule.kt`

```kotlin
val ClientModule = module
```
Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`, which scans each feature module's `di` package — no registry edit (spec §4).

### `feature/client/src/commonMain/kotlin/kpt/feature/client/general/components/ClientGeneralActionItem.kt`

```kotlin
sealed class ClientGeneralActionItem(
```
Ported verbatim from `6b66e8a43:feature/client/clientGeneral/ClientProfileGeneralActionItem.kt` — same seven items, same titles and subtitles, same drawables.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/general/ClientGeneralScreen.kt:94</code></summary>

```kotlin
    val performance by viewModel.performance.collectAsStateWithLifecycle()
    // The wired destination of each action row; an action with none is absent rather than inert.
    val actionHandlers = mapOf<ClientGeneralActionItem, ((Int) -> Unit)?>(
        ClientGeneralActionItem.UpcomingCharges to onUpcomingCharges,
        ClientGeneralActionItem.LoanAccount to onLoanAccounts,
        ClientGeneralActionItem.SavingAccounts to onSavingAccounts,
        ClientGeneralActionItem.FixedDepositAccounts to onFixedDepositAccounts,
```

</details>

### `feature/client/src/commonMain/kotlin/kpt/feature/client/list/ClientListTestTags.kt`

```kotlin
object ClientListTestTags
```
Stable handles for the client list — asserted by UI tests, never shown to a user.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/list/ClientListScreen.kt:106</code></summary>

```kotlin
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(ClientListTestTags.SCREEN),
        floatingActionButton = {
            if (canCreate) {
                FloatingActionButton(
                    onClick = { onAction(ClientListAction.CreateClientClicked) },
```

</details>

- `const val SCREEN = "client-list:screen"` — The client list screen's root.
- `const val SEARCH = "client-list:search"` — The search field.
- `const val LIST = "client-list:list"` — The list of clients.
- `const val REFRESH = "client-list:refresh"` — The pull-to-refresh container.
- `const val CREATE = "client-list:create"` — The "new client" button.
- `const val FILTER_TOGGLE = "client-list:filter-toggle"` — The button that shows or hides the filters.
- `const val FILTERS = "client-list:filters"` — The filter panel.
- `const val CLEAR_FILTERS = "client-list:clear-filters"` — The "clear filters" button.

### `feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt`

```kotlin
fun NavController.navigateToClientEditDetails(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the edit form for client `clientId`'s details.

```kotlin
fun NavController.navigateToClientEditProfile(
```
Opens the profile-photo screen for a client.

```kotlin
fun NavController.navigateToClientDefaultAccount(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the default-savings-account picker for client `clientId`.

```kotlin
fun NavController.navigateToClientAddDocument(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the attach-document form for client `clientId`.

```kotlin
fun NavController.navigateToClientAddAddress(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the add-address form for client `clientId`.

```kotlin
fun NavController.navigateToClientAddIdentifier(clientId: Long, navOptions: NavOptions? = null) =
```
Opens the add-identifier form for client `clientId`.

```kotlin
fun NavController.navigateToClientShareAccounts(clientId: Int, navOptions: NavOptions? = null) =
```
Opens client `clientId`'s share accounts.

```kotlin
fun NavController.navigateToShareApplication(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the share-account application for client `clientId`.

```kotlin
fun NavController.navigateToFixedDepositApplication(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the fixed-deposit application for client `clientId`.

```kotlin
fun NavController.navigateToClientDepositAccounts(
```
Opens a client's deposit accounts of one kind.

```kotlin
fun NavController.navigateToClientSignature(
```
Opens the signature screen for a client.

```kotlin
fun NavController.navigateToClientDocuments(clientId: Int, navOptions: NavOptions? = null) =
```
Opens client `clientId`'s attached documents.

```kotlin
fun NavController.navigateToClientDocumentPreview(route: ClientDocumentPreviewRoute) = navigate(route)
```
Opens one saved document in the in-app viewer.

### `feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigation.kt`

```kotlin
data object ClientDestination
```
The tab's graph. Mirrors `HomeDestination` — the tab points at the graph, not at a screen.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:21</code></summary>

```kotlin
/** Opens the clients graph (the tab's destination). */
fun NavController.navigateToClients(navOptions: NavOptions? = null) =
    navigate(ClientDestination, navOptions)

/** Opens client [clientId]'s profile. */
fun NavController.navigateToClientProfile(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientProfileRoute(clientId), navOptions)
```

</details>

```kotlin
data object ClientListRoute
```
The client list inside the client graph (its start destination).

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:17</code></summary>

```kotlin
/** Opens the client list. */
fun NavController.navigateToClientList(navOptions: NavOptions? = null) =
    navigate(ClientListRoute, navOptions)

/** Opens the clients graph (the tab's destination). */
fun NavController.navigateToClients(navOptions: NavOptions? = null) =
    navigate(ClientDestination, navOptions)
```

</details>

```kotlin
data class ClientProfileRoute(val clientId: Int)
```
The client PROFILE — what tapping a client in the list opens. Named for the original's `ClientProfileRoute`, which is what `clientListScreenRoute(onClientSelect = navController::navigateToClientProfileRoute)` pointed at.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:25</code></summary>

```kotlin
/** Opens client [clientId]'s profile. */
fun NavController.navigateToClientProfile(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientProfileRoute(clientId), navOptions)

/** Opens the assign-loan-officer screen for client [clientId]. */
fun NavController.navigateToClientStaff(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientStaffRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientStaffRoute(val clientId: Int)
```
Assign a loan officer to a client. `clientId`, not the original's bare `id: Int = -1`.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:29</code></summary>

```kotlin
/** Opens the assign-loan-officer screen for client [clientId]. */
fun NavController.navigateToClientStaff(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientStaffRoute(clientId), navOptions)

/** Opens the closure form for client [clientId]. */
fun NavController.navigateToClientClosure(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientClosureRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientClosureRoute(val clientId: Int)
```
Close a client. Same `clientId`-without-a-default reasoning as `ClientStaffRoute`.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:33</code></summary>

```kotlin
/** Opens the closure form for client [clientId]. */
fun NavController.navigateToClientClosure(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientClosureRoute(clientId), navOptions)

/** Opens the office-transfer form for client [clientId]. */
fun NavController.navigateToClientTransfer(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientTransferRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientTransferRoute(val clientId: Int)
```
Propose transferring a client to another office.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:37</code></summary>

```kotlin
/** Opens the office-transfer form for client [clientId]. */
fun NavController.navigateToClientTransfer(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientTransferRoute(clientId), navOptions)

/** Opens the pledge-collateral form for client [clientId]. */
fun NavController.navigateToClientCollateral(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientCollateralRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientCollateralRoute(val clientId: Int)
```
Pledge collateral against a client.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:41</code></summary>

```kotlin
/** Opens the pledge-collateral form for client [clientId]. */
fun NavController.navigateToClientCollateral(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientCollateralRoute(clientId), navOptions)

/** Opens client [clientId]'s general view. */
fun NavController.navigateToClientGeneral(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientGeneralRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientGeneralRoute(val clientId: Int)
```
The client's GENERAL view — performance history + linked-account actions. Reached from the profile's "General" row, and where the client's accounts actually live.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:45</code></summary>

```kotlin
/** Opens client [clientId]'s general view. */
fun NavController.navigateToClientGeneral(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientGeneralRoute(clientId), navOptions)

/** Opens client [clientId]'s identity documents. */
fun NavController.navigateToClientIdentifiers(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientIdentifiersRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientIdentifiersRoute(val clientId: Int)
```
The client's identity documents. Reached from the profile's "Identifiers" row.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:49</code></summary>

```kotlin
/** Opens client [clientId]'s identity documents. */
fun NavController.navigateToClientIdentifiers(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientIdentifiersRoute(clientId), navOptions)

/** Opens the apply-for-a-new-account chooser for client [clientId]. */
fun NavController.navigateToClientApplyNew(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientApplyNewRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientApplyNewRoute(val clientId: Int)
```
"Apply for a new account" — the client-side chooser. `clientId` only.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:53</code></summary>

```kotlin
/** Opens the apply-for-a-new-account chooser for client [clientId]. */
fun NavController.navigateToClientApplyNew(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientApplyNewRoute(clientId), navOptions)

/** Opens client [clientId]'s detail fields and actions. */
fun NavController.navigateToClientDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDetailsRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientDetailsRoute(val clientId: Int)
```
The client's DETAIL fields + its eight actions — what the profile's ProfileCard taps through to. Named for the original's `clientDetailsProfile`.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:57</code></summary>

```kotlin
/** Opens client [clientId]'s detail fields and actions. */
fun NavController.navigateToClientDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDetailsRoute(clientId), navOptions)

/** Opens client [clientId]'s addresses. */
fun NavController.navigateToClientAddress(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddressRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientAddressRoute(val clientId: Int)
```
The client's postal addresses. Reached from the profile's "Address" row.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:61</code></summary>

```kotlin
/** Opens client [clientId]'s addresses. */
fun NavController.navigateToClientAddress(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddressRoute(clientId), navOptions)

/** Opens the collateral client [clientId] has pledged. */
fun NavController.navigateToClientCollateralDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientCollateralDetailsRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientCollateralDetailsRoute(val clientId: Int)
```
The collateral a client has already PLEDGED — read-only. Reached from client-general's "Collateral Data" row. Distinct from `ClientCollateralRoute`, which is the ADD form reached from client details' "Create Collateral".

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:65</code></summary>

```kotlin
/** Opens the collateral client [clientId] has pledged. */
fun NavController.navigateToClientCollateralDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientCollateralDetailsRoute(clientId), navOptions)

/** Opens client [clientId]'s loan accounts. */
fun NavController.navigateToClientLoanAccounts(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientLoanAccountsRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientLoanAccountsRoute(val clientId: Int)
```
The client's LOAN ACCOUNTS list.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:69</code></summary>

```kotlin
/** Opens client [clientId]'s loan accounts. */
fun NavController.navigateToClientLoanAccounts(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientLoanAccountsRoute(clientId), navOptions)

/** Opens the new-client form. */
fun NavController.navigateToCreateNewClient(navOptions: NavOptions? = null) =
    navigate(CreateNewClientRoute, navOptions)
```

</details>

```kotlin
data class ClientDepositAccountsRoute(val clientId: Int, val kind: String)
```
A client's deposit accounts of ONE kind — savings, fixed deposit or recurring deposit. `kind` is a `DepositAccountKind` name.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:61</code></summary>

```kotlin
    kind: DepositAccountKind,
    navOptions: NavOptions? = null,
) = navigate(ClientDepositAccountsRoute(clientId, kind.name), navOptions)

/** Opens the signature screen for a client. */
fun NavController.navigateToClientSignature(
    clientId: Int,
```

</details>

```kotlin
data object CreateNewClientRoute
```
Create a new client. Takes NO arguments — the form reads the tenant template itself.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt:73</code></summary>

```kotlin
/** Opens the new-client form. */
fun NavController.navigateToCreateNewClient(navOptions: NavOptions? = null) =
    navigate(CreateNewClientRoute, navOptions)
```

</details>

```kotlin
data class ClientEditDetailsRoute(val clientId: Int)
```
Edit a client's details. Same `clientId`-without-a-default reasoning as `ClientStaffRoute`.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:18</code></summary>

```kotlin
/** Opens the edit form for client [clientId]'s details. */
fun NavController.navigateToClientEditDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientEditDetailsRoute(clientId), navOptions)

/** Opens the profile-photo screen for a client. */
fun NavController.navigateToClientEditProfile(
    clientId: Int,
```

</details>

```kotlin
data class ClientEditProfileRoute(
```
The client's profile PHOTO. Reached from client details' "Update Photo". Carries `clientName` + `accountNo` for the same reason `ClientSignatureRoute` does: the screen shows them in its header and has no client read of its own.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:26</code></summary>

```kotlin
    accountNo: String,
    navOptions: NavOptions? = null,
) = navigate(ClientEditProfileRoute(clientId, clientName, accountNo), navOptions)

/** Opens the default-savings-account picker for client [clientId]. */
fun NavController.navigateToClientDefaultAccount(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDefaultAccountRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientDefaultAccountRoute(val clientId: Int)
```
Nominate a client's default savings account. Same no-default reasoning as `ClientStaffRoute`.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:30</code></summary>

```kotlin
/** Opens the default-savings-account picker for client [clientId]. */
fun NavController.navigateToClientDefaultAccount(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDefaultAccountRoute(clientId), navOptions)

/** Opens the attach-document form for client [clientId]. */
fun NavController.navigateToClientAddDocument(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddDocumentRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientAddDocumentRoute(val clientId: Int)
```
Attach a document to a client. Reached from the client documents screen's "+".

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:34</code></summary>

```kotlin
/** Opens the attach-document form for client [clientId]. */
fun NavController.navigateToClientAddDocument(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddDocumentRoute(clientId), navOptions)

/** Opens the add-address form for client [clientId]. */
fun NavController.navigateToClientAddAddress(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddAddressRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientAddAddressRoute(val clientId: Int)
```
Add a postal address to a client. Reached from the client address screen's "+".

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:38</code></summary>

```kotlin
/** Opens the add-address form for client [clientId]. */
fun NavController.navigateToClientAddAddress(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddAddressRoute(clientId), navOptions)

/** Opens the add-identifier form for client [clientId]. */
fun NavController.navigateToClientAddIdentifier(clientId: Long, navOptions: NavOptions? = null) =
    navigate(ClientAddIdentifierRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientAddIdentifierRoute(val clientId: Long)
```
Add an identity document to a client. Reached from the client identifiers screen's "+". `clientId` is a Long here because the identifier commands and the template store are keyed by Long, unlike every other client route.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:42</code></summary>

```kotlin
/** Opens the add-identifier form for client [clientId]. */
fun NavController.navigateToClientAddIdentifier(clientId: Long, navOptions: NavOptions? = null) =
    navigate(ClientAddIdentifierRoute(clientId), navOptions)

/** Opens client [clientId]'s share accounts. */
fun NavController.navigateToClientShareAccounts(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientShareAccountsRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientShareAccountsRoute(val clientId: Int)
```
A client's SHARE accounts. Reached from client-general's "Share Accounts" row.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:46</code></summary>

```kotlin
/** Opens client [clientId]'s share accounts. */
fun NavController.navigateToClientShareAccounts(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientShareAccountsRoute(clientId), navOptions)

/** Opens the share-account application for client [clientId]. */
fun NavController.navigateToShareApplication(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ShareApplicationRoute(clientId), navOptions)
```

</details>

```kotlin
data class ShareApplicationRoute(val clientId: Int)
```
Open a SHARE account. Reached from the apply-new chooser and from the share-accounts list.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:50</code></summary>

```kotlin
/** Opens the share-account application for client [clientId]. */
fun NavController.navigateToShareApplication(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ShareApplicationRoute(clientId), navOptions)

/** Opens the fixed-deposit application for client [clientId]. */
fun NavController.navigateToFixedDepositApplication(clientId: Int, navOptions: NavOptions? = null) =
    navigate(FixedDepositApplicationRoute(clientId), navOptions)
```

</details>

```kotlin
data class FixedDepositApplicationRoute(val clientId: Int)
```
Open a FIXED DEPOSIT account. Reached from the apply-new chooser and from the deposit-accounts list when it is showing the FIXED kind.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:54</code></summary>

```kotlin
/** Opens the fixed-deposit application for client [clientId]. */
fun NavController.navigateToFixedDepositApplication(clientId: Int, navOptions: NavOptions? = null) =
    navigate(FixedDepositApplicationRoute(clientId), navOptions)

/** Opens a client's deposit accounts of one kind. */
fun NavController.navigateToClientDepositAccounts(
    clientId: Int,
```

</details>

```kotlin
data class ClientSignatureRoute(
```
The client's signature. Reached from client details' "Update Signature". Carries `clientName` + `accountNo` because the screen shows them in its header and has no client read of its own — it reads the DOCUMENT list.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:69</code></summary>

```kotlin
    accountNo: String,
    navOptions: NavOptions? = null,
) = navigate(ClientSignatureRoute(clientId, clientName, accountNo), navOptions)

/** Opens client [clientId]'s attached documents. */
fun NavController.navigateToClientDocuments(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDocumentsRoute(clientId), navOptions)
```

</details>

```kotlin
data class ClientDocumentsRoute(val clientId: Int)
```
The client's attached documents. Reached from the profile's "Documents" row.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:73</code></summary>

```kotlin
/** Opens client [clientId]'s attached documents. */
fun NavController.navigateToClientDocuments(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDocumentsRoute(clientId), navOptions)

/** Opens one saved document in the in-app viewer. */
fun NavController.navigateToClientDocumentPreview(route: ClientDocumentPreviewRoute) = navigate(route)
```

</details>

```kotlin
data class ClientDocumentPreviewRoute(
```
One saved document, shown in the app.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientFormNavigators.kt:76</code></summary>

```kotlin

/** Opens one saved document in the in-app viewer. */
fun NavController.navigateToClientDocumentPreview(route: ClientDocumentPreviewRoute) = navigate(route)
```

</details>

```kotlin
fun NavGraphBuilder.clientListDestination(navController: NavController)
```
The client list — a TOP-LEVEL destination on the authenticated graph, so it is annotated and collected into `GeneratedFeatureDestinations` by `:cmp-navigation:generateFeatureDestinations`.

```kotlin
fun NavGraphBuilder.clientTabGraph(outerNav: NavController)
```
The client graph for the bottom-nav tab's INNER NavHost. Two NavHosts are in play and they want different things. `clientListDestination` registers the list on the OUTER authenticated graph, where another feature can push it full-screen.

### `feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientNavigators.kt`

```kotlin
fun NavController.navigateToClientList(navOptions: NavOptions? = null) =
```
Opens the client list.

```kotlin
fun NavController.navigateToClients(navOptions: NavOptions? = null) =
```
Opens the clients graph (the tab's destination).

```kotlin
fun NavController.navigateToClientProfile(clientId: Int, navOptions: NavOptions? = null) =
```
Opens client `clientId`'s profile.

```kotlin
fun NavController.navigateToClientStaff(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the assign-loan-officer screen for client `clientId`.

```kotlin
fun NavController.navigateToClientClosure(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the closure form for client `clientId`.

```kotlin
fun NavController.navigateToClientTransfer(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the office-transfer form for client `clientId`.

```kotlin
fun NavController.navigateToClientCollateral(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the pledge-collateral form for client `clientId`.

```kotlin
fun NavController.navigateToClientGeneral(clientId: Int, navOptions: NavOptions? = null) =
```
Opens client `clientId`'s general view.

```kotlin
fun NavController.navigateToClientIdentifiers(clientId: Int, navOptions: NavOptions? = null) =
```
Opens client `clientId`'s identity documents.

```kotlin
fun NavController.navigateToClientApplyNew(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the apply-for-a-new-account chooser for client `clientId`.

```kotlin
fun NavController.navigateToClientDetails(clientId: Int, navOptions: NavOptions? = null) =
```
Opens client `clientId`'s detail fields and actions.

```kotlin
fun NavController.navigateToClientAddress(clientId: Int, navOptions: NavOptions? = null) =
```
Opens client `clientId`'s addresses.

```kotlin
fun NavController.navigateToClientCollateralDetails(clientId: Int, navOptions: NavOptions? = null) =
```
Opens the collateral client `clientId` has pledged.

```kotlin
fun NavController.navigateToClientLoanAccounts(clientId: Int, navOptions: NavOptions? = null) =
```
Opens client `clientId`'s loan accounts.

```kotlin
fun NavController.navigateToCreateNewClient(navOptions: NavOptions? = null) =
```
Opens the new-client form.

### `feature/client/src/commonMain/kotlin/kpt/feature/client/navigation/ClientTab.kt`

```kotlin
object ClientTab : NavigationItem
```
The Clients bottom-nav tab.

### `feature/client/src/commonMain/kotlin/kpt/feature/client/profile/components/ClientProfileActionItem.kt`

```kotlin
sealed class ClientProfileActionItem(
```
Ported verbatim from `6b66e8a43:feature/client/clientProfile/ClientProfileActions.kt` — same seven items, same titles, same subtitles, same drawables (copied across with it).

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/profile/ClientProfileScreen.kt:101</code></summary>

```kotlin
    // The wired destination of each action row. FamilyMembers has none: the original's branch is an
    // empty `{}` and the row is not in the list.
    val actionHandlers = mapOf<ClientProfileActionItem, ((Int) -> Unit)?>(
        ClientProfileActionItem.General to onViewAssociatedAccounts,
        ClientProfileActionItem.Address to onViewAddress,
        ClientProfileActionItem.Identifiers to onIdentifiers,
        ClientProfileActionItem.Documents to onDocuments,
```

</details>

---

_34 type(s), 39 function(s)/property(ies); 73 carry KDoc at source; 0 authored example(s); 33 live call site(s)._
<!-- api-docs:end -->
