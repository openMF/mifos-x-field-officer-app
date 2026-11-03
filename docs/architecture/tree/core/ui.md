# `core/ui`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_UI.md`
> **Measured:** 92 Kotlin files, 1 test files

**Defines annotations:** `@DevicePreview`, `@FeatureTab`

## Principal types

`Actions`, `AppStatusDialogTestTags`, `BackgroundEvent`, `BaseViewModel`, `DevicePreview`, `DocumentKind`, `FabButton`, `FabButtonState`, `FabType`, `FeatureTab`, `FloatingActionButtonContent`, `ImageUtil`, `KptPullToRefreshState`, `LottieConstants`  …and 17 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/ui sha=55104488c5642498e25835fe9b3292ca49650764 -->
## API reference

_Generated from `core/ui` at tree `55104488c564` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/Transition.kt`

```kotlin
typealias EnterTransitionProvider =
```
Function type for providing nullable enter transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination enters the screen.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:30</code></summary>

```kotlin
 * Used with [AnimatedContentTransitionScope] to define how a destination enters the screen.
 */
typealias EnterTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)

/**
 * Function type for providing nullable exit transitions in navigation.
```

</details>

```kotlin
typealias ExitTransitionProvider =
```
Function type for providing nullable exit transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination exits the screen.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:37</code></summary>

```kotlin
 * Used with [AnimatedContentTransitionScope] to define how a destination exits the screen.
 */
typealias ExitTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)

/**
 * Function type for providing non-null enter transitions in navigation.
```

</details>

```kotlin
typealias NonNullEnterTransitionProvider =
```
Function type for providing non-null enter transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination enters the screen.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:44</code></summary>

```kotlin
 * Used with [AnimatedContentTransitionScope] to define how a destination enters the screen.
 */
typealias NonNullEnterTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition)

/**
 * Function type for providing non-null exit transitions in navigation.
```

</details>

```kotlin
typealias NonNullExitTransitionProvider =
```
Function type for providing non-null exit transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination exits the screen.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:51</code></summary>

```kotlin
 * Used with [AnimatedContentTransitionScope] to define how a destination exits the screen.
 */
typealias NonNullExitTransitionProvider =
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition)

/**
 * The default transition time (in milliseconds) for all fade transitions in the
```

</details>

```kotlin
val DEFAULT_STAY_TRANSITION_TIME_MS: Int =
```
The default transition time (in milliseconds) for all "stay"/no-op transitions in the `TransitionProviders`. This should be at least as large as any other transition that might also be happening during a navigation.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:79</code></summary>

```kotlin
 * navigation.
 */
val DEFAULT_STAY_TRANSITION_TIME_MS: Int =
    maxOf(
        DEFAULT_FADE_TRANSITION_TIME_MS,
        DEFAULT_SLIDE_TRANSITION_TIME_MS,
        DEFAULT_PUSH_TRANSITION_TIME_MS,
```

</details>

```kotlin
val AnimatedContentTransitionScope<NavBackStackEntry>.isSameGraphNavigation: Boolean
```
Checks if the parent of the destination before and after the navigation is the same. This is useful to ignore certain enter/exit transitions when navigating between distinct, nested flows.

```kotlin
object TransitionProviders
```
Contains standard "transition providers" that may be used to specify the `EnterTransition` and `ExitTransition` used when building a typical composable destination.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt:99</code></summary>

```kotlin
 * components higher up in the graph.
 */
object TransitionProviders {
    /**
     * The standard set of "enter" transition providers.
     */
    object Enter {
```

</details>

- `val fadeIn: EnterTransitionProvider =` — Fades the new screen in. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val pushLeft: EnterTransitionProvider =` — Slides the new screen in from the left of the screen.
- `val pushRight: EnterTransitionProvider =` — Slides the new screen in from the right of the screen.
- `val slideUp: EnterTransitionProvider =` — Slides the new screen in from the bottom of the screen. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val stay: EnterTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately appear while the other screen transitions away.
- `val fadeOut: ExitTransitionProvider =` — Fades the current screen out. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val pushLeft: ExitTransitionProvider =` — Slides the current screen out to the left of the screen.
- `val pushRight: ExitTransitionProvider =` — Slides the current screen out to the right of the screen.
- `val slideDown: ExitTransitionProvider =` — Slides the current screen down to the bottom of the screen. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val stay: ExitTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately disappear while the other screen transitions into place.

```kotlin
object RootTransitionProviders
```
Contains standard "transition providers" that may be used to specify the `EnterTransition` and `ExitTransition` used when building a root `NavHost`, which requires a non-null value.

<details><summary>Used in the template — <code>core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/nav/NavGraphBuilderExtensions.kt:53</code></summary>

```kotlin
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = RootTransitionProviders.Kpt.Enter.slideUp(motion),
        exitTransition = RootTransitionProviders.Kpt.Exit.stay(motion),
        popEnterTransition = RootTransitionProviders.Kpt.Enter.stay(motion),
        popExitTransition = RootTransitionProviders.Kpt.Exit.slideDown(motion),
        sizeTransform = null,
```

</details>

- `val fadeIn: NonNullEnterTransitionProvider =` — Fades the new screen in.
- `val none: NonNullEnterTransitionProvider =` — There is no transition for the entering screen.
- `val pushLeft: NonNullEnterTransitionProvider =` — Slides the new screen in from the left of the screen.
- `val totalTransitionDurationMs = DEFAULT_PUSH_TRANSITION_TIME_MS`
- `val pushRight: NonNullEnterTransitionProvider =` — Slides the new screen in from the right of the screen.
- `val totalTransitionDurationMs = DEFAULT_PUSH_TRANSITION_TIME_MS`
- `val slideUp: NonNullEnterTransitionProvider =` — Slides the new screen in from the bottom of the screen.
- `val stay: NonNullEnterTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately appear while the other screen transitions away.
- `val fadeOut: NonNullExitTransitionProvider =` — Fades the current screen out.
- `val none: NonNullExitTransitionProvider =` — There is no transition for the exiting screen. Unlike the `stay` transition, this will immediately remove the outgoing screen even if there is an ongoing enter transition happening for the new screen.
- `val pushLeft: NonNullExitTransitionProvider =` — Slides the current screen out to the left of the screen.
- `val totalTransitionDurationMs = DEFAULT_PUSH_TRANSITION_TIME_MS`
- `val delayMs = totalTransitionDurationMs / 7`
- `val slideWithoutDelayMs = totalTransitionDurationMs - delayMs`
  _…more members; read the file._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt`

```kotlin
fun KptBottomBar(
```
App-level bottom navigation bar, driven by the generated tab registry. Tabs register by `@FeatureTab` annotation — never by editing a list here, which would make adding a feature a two-place change and silently drop it if one were missed.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationBarItem.kt`

```kotlin
fun RowScope.KptNavigationBarItem(
```
One item in `KptBottomBar`: icon, label and selected state.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt:44</code></summary>

```kotlin
    ) {
        navigationItems.forEach { navigationItem ->
            KptNavigationBarItem(
                contentDescriptionRes = navigationItem.contentDescriptionRes,
                selectedIcon = navigationItem.selectedIcon,
                unselectedIcon = navigationItem.icon,
                isSelected = selectedItem == navigationItem,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRail.kt`

```kotlin
fun KptNavigationRail(
```
The side rail used instead of a bottom bar on wide windows — same tab source, different surface.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRailItem.kt`

```kotlin
fun ColumnScope.KptNavigationRailItem(
```
One item in `KptNavigationRail`.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRail.kt:69</code></summary>

```kotlin
        ) {
            navigationItems.forEach { navigationItem ->
                KptNavigationRailItem(
                    contentDescriptionRes = navigationItem.contentDescriptionRes,
                    selectedIconRes = navigationItem.selectedIcon,
                    unselectedIconRes = navigationItem.icon,
                    isSelected = navigationItem == selectedItem,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AddChargeDialog.kt`

```kotlin
fun AddChargeBottomSheet(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart4.kt:184</code></summary>

```kotlin

    AppTheme {
        AddChargeBottomSheet(
            title = "Add Charge",
            confirmText = "Confirm",
            dismissText = "Cancel",
            showDatePicker = false,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppActionsListingCardComponent.kt`

```kotlin
fun AppActionsListingComponentOutline(
```
_No KDoc at source._

```kotlin
fun AppActionsIdentifierListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/identifiers/ClientIdentifiersScreen.kt:209</code></summary>

```kotlin
                        val status = getClientIdentifierStatus(identifier.status)

                        AppActionsIdentifierListingComponent(
                            type = identifier.documentType?.name ?: notAvailable,
                            id = identifier.id?.toString() ?: notAvailable,
                            key = identifier.documentKey ?: notAvailable,
                            status = status,
```

</details>

```kotlin
fun AppActionsCollateralDataListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/collateraldetails/ClientCollateralDetailsScreen.kt:99</code></summary>

```kotlin
                items(items = items, key = { it.collateralId }) { item ->
                    val (total, totalCollateral) = collateralRowTotals(item)
                    AppActionsCollateralDataListingComponent(
                        name = item.name,
                        quantity = item.quantity.toString(),
                        totalValue = total.toString(),
                        totalCollateralValue = totalCollateral.toString(),
```

</details>

```kotlin
fun AppActionsClientDocumentListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/documents/ClientDocumentsScreen.kt:195</code></summary>

```kotlin
                LazyColumn {
                    items(list, key = { it.id }) { document ->
                        AppActionsClientDocumentListingComponent(
                            documentName = document.name ?: "",
                            documentDescription = document.description ?: "",
                            fileName = document.fileName ?: "",
                            isExpanded = expandedId == document.id,
```

</details>

```kotlin
fun AppActionsLoanListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/loanaccounts/ClientLoanAccountsScreen.kt:295</code></summary>

```kotlin
    val menu = loanMenu(loan, canViewAccount, canMakeRepayment)

    AppActionsLoanListingComponent(
        accountNo = loan.accountNo ?: notAvailable,
        loanProduct = loan.productName ?: notAvailable,
        originalLoan = loan.originalLoan?.let(money) ?: notAvailable,
        // A pending loan has no amounts yet, so the original showed "N/A" rather than a formatted
```

</details>

```kotlin
fun AppActionsShareListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/shareaccounts/ClientShareAccountsScreen.kt:200</code></summary>

```kotlin
        items(accounts, key = { it.id ?: it.accountNo.hashCode() }) { account ->
            val accountId = account.id ?: 0
            AppActionsShareListingComponent(
                accountNo = account.accountNo.orEmpty(),
                shareProductName = account.productName ?: account.shortProductName.orEmpty(),
                approvedShares = account.totalApprovedShares,
                pendingForApprovalShares = account.totalPendingForApprovalShares,
```

</details>

```kotlin
fun AppActionsChargeListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/list/ChargeListScreen.kt:212</code></summary>

```kotlin
) {
    Box(Modifier.testTag(TestTags.ChargeList.row(charge.id))) {
        AppActionsChargeListingComponent(
            chargeTitle = charge.name,
            type = charge.calculation,
            date = charge.dueDate ?: stringResource(Res.string.feature_charge_no_due_date),
            collectedOn = stringResource(Res.string.feature_charge_outstanding, charge.outstanding),
```

</details>

```kotlin
fun AppActionsSavingsListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/depositaccounts/ClientDepositAccountsScreen.kt:260</code></summary>

```kotlin
        items(accounts, key = { it.id ?: it.accountNo.hashCode() }) { account ->
            val accountId = account.id ?: 0
            AppActionsSavingsListingComponent(
                accountNo = account.accountNo.orEmpty(),
                savingsProduct = productLabel,
                savingsProductName = account.productName.orEmpty(),
                balance = account.accountBalance
```

</details>

```kotlin
fun AppActionsClientFeeListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreview.kt:112</code></summary>

```kotlin
private fun PreviewAppActionsClientFeeListingComponent() {
    KptTheme {
        AppActionsClientFeeListingComponent(
            name = "John Doe",
            dueAsOf = "2025-08-19",
            due = "$1000",
            paid = "$400",
```

</details>

```kotlin
sealed class Actions(open val icon: ImageVector, open val text: StringResource)
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/list/ChargeListScreen.kt:219</code></summary>

```kotlin
            amount = charge.amount,
            menuList = buildList {
                add(Actions.ViewDetails())
                if (canEdit) add(Actions.Edit())
                add(Actions.Delete())
            },
            onActionClicked = { action ->
```

</details>

```kotlin
fun AppActionsNoteListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreview.kt:57</code></summary>

```kotlin
private fun PreviewAppActionsNoteListingComponent() {
    KptTheme {
        AppActionsNoteListingComponent(
            createdBy = "John Doe",
            date = "02 September 2025",
            notes = "This is a note",
            menuList = listOf(
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAddressCard.kt`

```kotlin
fun AppAddressCard(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/address/ClientAddressScreen.kt:179</code></summary>

```kotlin
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(list, key = { it.addressId }) { address ->
                    AppAddressCard(
                        title = address.addressType,
                        addressList = mapOf(
                            stringResource(Res.string.feature_client_address_line_1) to address.addressLine1,
                            stringResource(Res.string.feature_client_address_line_2) to address.addressLine2,
```

</details>

```kotlin
fun AppAddressItem(
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAlertDialog.kt`

```kotlin
fun AppAlertDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:232</code></summary>

```kotlin
    }

    AppAlertDialog(
        dialogText = text,
        // Success leaves the screen — the entity is active, so there is nothing left to do here,
        // and the caller re-reads it. A failure only dismisses.
        onConfirmation = {
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppBreadCrumb.kt`

```kotlin
fun AppBreadcrumbNavBar(
```
_No KDoc at source._

```kotlin
fun BreadcrumbItem(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreview.kt:259</code></summary>

```kotlin
            modifier = Modifier.padding(DesignToken.padding.medium),
        ) {
            BreadcrumbItem(
                text = "Home",
                isActive = false,
                onClick = {},
            )
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppCheckBox.kt`

```kotlin
fun AppCheckBox(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/create/CreateCenterScreen.kt:155</code></summary>

```kotlin
                    modifier = Modifier.fillMaxWidth().testTag(CreateCenterTestTags.OFFICE),
                )
                AppCheckBox(
                    text = stringResource(Res.string.feature_center_activate),
                    checked = state.active,
                    onCheckChanged = { onAction(CreateCenterAction.ActiveToggled(it)) },
                    modifier = Modifier.fillMaxWidth().testTag(CreateCenterTestTags.ACTIVE),
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppDocumentViewer.kt`

```kotlin
fun AppDocumentViewer(
```
Shows a downloaded document in place — a PDF through PDFium (`pdf-viewer-kmp`), an image through Coil, anything else as an explanation pointing at Share.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/documentpreview/ClientDocumentPreviewScreen.kt:93</code></summary>

```kotlin
            )

            is PreviewContent.Loaded -> AppDocumentViewer(
                bytes = content.bytes,
                fileName = content.fileName,
                mimeType = content.mimeType,
                modifier = Modifier.fillMaxSize().testTag(ClientDocumentPreviewTestTags.VIEWER),
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppEmptyCard.kt`

```kotlin
fun AppEmptyCard(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/checkerinbox/src/commonMain/kotlin/kpt/feature/checkerinbox/ui/CheckerInboxScreen.kt:421</code></summary>

```kotlin
@Composable
private fun Empty(message: org.jetbrains.compose.resources.StringResource) {
    AppEmptyCard(msg = stringResource(message), modifier = Modifier.padding(KptTheme.spacing.md))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppEmptyUi.kt`

```kotlin
fun AppEmptyUi(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/detail/ChargeDetailScreen.kt:123</code></summary>

```kotlin
            // Deleted elsewhere, or a link to a charge the account no longer carries.
            empty = {
                AppEmptyUi(
                    text = stringResource(Res.string.feature_charge_not_found),
                    icon = AppIcons.FileTask,
                    modifier = Modifier.testTag(TestTags.ChargeDetail.NOT_FOUND),
                )
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppErrorComponent.kt`

```kotlin
fun AppErrorComponent(
```
_No KDoc at source._

```kotlin
fun NoInternetComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreview.kt:309</code></summary>

```kotlin
private fun NoInternetPreview() {
    AppTheme {
        NoInternetComponent()
    }
}
```

</details>

```kotlin
fun EmptyDataComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:27</code></summary>

```kotlin
private fun EmptyDataPreview() {
    AppTheme {
        EmptyDataComponent()
    }
}

@Preview
```

</details>

```kotlin
fun EmptyDataComponentWithModifiedMessageAndIcon(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:37</code></summary>

```kotlin
) {
    AppTheme {
        EmptyDataComponentWithModifiedMessageAndIcon(
            message = "No data found",
            icon = AppIcons.Error,
            modifier = modifier,
            isEmptyData = true,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppFAB.kt`

```kotlin
fun AppFAB(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:50</code></summary>

```kotlin
private fun AppFABPreview() {
    AppTheme {
        AppFAB(
            icon = AppIcons.Info,
            onClick = {},
        )
    }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppFilePickerBottomSheet.kt`

```kotlin
fun AppFilePickerBottomSheet(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/adddocument/ClientAddDocumentScreen.kt:205</code></summary>

```kotlin

        if (state.isSourceSheetOpen) {
            AppFilePickerBottomSheet(
                onDismiss = { onAction(ClientAddDocumentAction.DismissFileSource) },
                onGalleryClick = { onAction(ClientAddDocumentAction.PickFromGallery) },
                onFilesClick = { onAction(ClientAddDocumentAction.PickFromFiles) },
                // Dead in the original (`UseMoreOptions -> {}`). Kept — see the KDoc.
```

</details>

```kotlin
fun PreviewAppFilePickerBottomSheet()
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppGeneralCard.kt`

```kotlin
fun AppGeneralCardComponentOutline(
```
_No KDoc at source._

```kotlin
fun AppGeneralRowItem(
```
_No KDoc at source._

```kotlin
fun AppGeneralCard(
```
_No KDoc at source._

```kotlin
fun AppPreviewGeneralCard()
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppIcon.kt`

```kotlin
fun AppIcon(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:63</code></summary>

```kotlin
) {
    AppTheme {
        AppIcon(
            mobileIcon = Res.drawable.core_ui_ic_centers_24dp,
            modifier = modifier,
        )
    }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppImageCropperDialog.kt`

```kotlin
fun AppImageCropperDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientScreen.kt:715</code></summary>

```kotlin
            // uploaded whatever aspect the camera produced.
            cropper.cropState?.let {
                AppImageCropperDialog(state = it, aspectRatio = AspectRatio(1, 1))
            }

        CreateClientDialog.CropFailed -> Notice(
            message = stringResource(Res.string.feature_client_createnew_crop_failed),
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppItemCard.kt`

```kotlin
fun AppItemCard(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:76</code></summary>

```kotlin
) {
    AppTheme {
        AppItemCard(
            onClick = {},
            modifier = modifier,
        ) {
            Text(text = "Card Content")
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppLinkText.kt`

```kotlin
fun AppLinkText(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:91</code></summary>

```kotlin
) {
    AppTheme {
        AppLinkText(
            text = "Link Text",
            onClick = {},
            modifier = modifier,
        )
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppListingComponent.kt`

```kotlin
fun AppListingComponentOutline(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAddressCard.kt:32</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline {
        Column(
            modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraSmall),
        ) {
```

</details>

```kotlin
fun AppListingRowItem(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppActionsListingCardComponent.kt:187</code></summary>

```kotlin
                    verticalArrangement = Arrangement.spacedBy(DesignToken.padding.medium),
                ) {
                    AppListingRowItem(
                        key = stringResource(Res.string.core_ui_document_id),
                        value = id,
                    )
                    AppListingRowItem(
```

</details>

```kotlin
fun AppListingColumnItem(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppActionsListingCardComponent.kt:872</code></summary>

```kotlin
                Spacer(Modifier.height(DesignToken.padding.large))

                AppListingColumnItem(
                    key = stringResource(Res.string.core_ui_note_date),
                    value = date,
                )
```

</details>

```kotlin
fun AppListingColumnItem(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppActionsListingCardComponent.kt:872</code></summary>

```kotlin
                Spacer(Modifier.height(DesignToken.padding.large))

                AppListingColumnItem(
                    key = stringResource(Res.string.core_ui_note_date),
                    value = date,
                )
```

</details>

```kotlin
fun AppListingRowItem(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppActionsListingCardComponent.kt:187</code></summary>

```kotlin
                    verticalArrangement = Arrangement.spacedBy(DesignToken.padding.medium),
                ) {
                    AppListingRowItem(
                        key = stringResource(Res.string.core_ui_document_id),
                        value = id,
                    )
                    AppListingRowItem(
```

</details>

```kotlin
fun AppListingRowItemHeader(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppActionsListingCardComponent.kt:179</code></summary>

```kotlin
        ) {
            Column(modifier = Modifier.padding(DesignToken.padding.large).onClick { onClick() }) {
                AppListingRowItemHeader(
                    text = type,
                    keyStyle = AppTypography.titleSmallEmphasized,
                )
                Spacer(Modifier.height(DesignToken.padding.large))
```

</details>

```kotlin
fun AppDefaultListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:110</code></summary>

```kotlin

    AppTheme {
        AppDefaultListingComponent(data = sampleData)
    }
}

@Preview
```

</details>

```kotlin
fun AppDefaultListingComponentFromStringResources(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/details/ClientDetailsScreen.kt:296</code></summary>

```kotlin
            // Derived from the client rather than written into the state on load.
            detailCards(entity, notAvailable).forEach { card ->
                AppDefaultListingComponentFromStringResources(data = card)
                Spacer(Modifier.height(KptTheme.spacing.md))
            }

            Row(
```

</details>

```kotlin
fun AppClientFeeListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:118</code></summary>

```kotlin
private fun PreviewAppClientFeeListingComponent() {
    KptTheme {
        AppClientFeeListingComponent(
            name = "John Doe",
            dueAsOf = "2025-08-19",
            due = "$1000",
            paid = "$400",
```

</details>

```kotlin
fun AppLoanListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:133</code></summary>

```kotlin
private fun PreviewAppLoanListingComponent() {
    KptTheme {
        AppLoanListingComponent(
            accountNo = "LN12345",
            loanProduct = "Personal Loan",
            originalLoan = "$5000",
            amountPaid = "$2000",
```

</details>

```kotlin
fun AppSavingsListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:148</code></summary>

```kotlin
private fun PreviewAppSavingsListingComponent() {
    KptTheme {
        AppSavingsListingComponent(
            accountNo = "SV9876",
            savingsProduct = "Regular Savings",
            lastActive = "2025-08-15",
            balance = "$1200",
```

</details>

```kotlin
fun AppCollateralDataListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:161</code></summary>

```kotlin
private fun PreviewAppCollateralDataListingComponent() {
    KptTheme {
        AppCollateralDataListingComponent(
            name = "Gold Jewelry",
            quantity = "5",
            totalValue = "$2500",
            totalCollateralValue = "$2500",
```

</details>

```kotlin
fun AppIdentifierListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:174</code></summary>

```kotlin
private fun PreviewAppIdentifierListingComponent() {
    KptTheme {
        AppIdentifierListingComponent(
            type = "Passport",
            id = "P1234567",
            key = "Government ID",
            status = Status.Active,
```

</details>

```kotlin
enum class Status(val color: Color)
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

```kotlin
fun AppDelinquencyListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:189</code></summary>

```kotlin
private fun PreviewAppDelinquencyListingComponent() {
    KptTheme {
        AppDelinquencyListingComponent(
            date = "2025-08-01",
            time = "10:00 AM",
            isPause = false,
            startDate = "2025-07-01",
```

</details>

```kotlin
fun AppChargesListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:203</code></summary>

```kotlin
private fun PreviewAppChargesListingComponent() {
    KptTheme {
        AppChargesListingComponent(
            title = "Processing Fee",
            type = "Flat",
            dueAsOf = "2025-08-19",
            calculationType = "Fixed",
```

</details>

```kotlin
fun AppSellLoanListingComponent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart2.kt:220</code></summary>

```kotlin
private fun PreviewAppSellLoanListingComponent() {
    KptTheme {
        AppSellLoanListingComponent(
            status = Status.Pending,
            effectiveForm = "2025-08-20",
            ownerExternalId = "OWN123",
            transferExternalId = "TRF456",
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppProgressIndicator.kt`

```kotlin
fun AppProgressIndicator(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormScreen.kt:124</code></summary>

```kotlin
    ) { padding ->
        when {
            state.isLoadingTypes -> AppProgressIndicator(
                modifier = Modifier.fillMaxSize().padding(padding).testTag(TestTags.ChargeForm.LOADING),
            )

            state.typesError -> AppSweetError(
```

</details>

```kotlin
fun AppProgressIndicatorOverlay(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormScreen.kt:149</code></summary>

```kotlin
    // The original's `MifosProgressIndicatorOverlay` while the write is in flight.
    if (state.isSubmitting) {
        AppProgressIndicatorOverlay(Modifier.fillMaxSize().testTag(TestTags.ChargeForm.SUBMITTING))
    }

    state.dialogState?.let { dialog -> StatusDialog(dialog, state.isEdit, onAction) }
}
```

</details>

```kotlin
fun AppPagingAppendProgress(modifier: Modifier = Modifier)
```
_No KDoc at source._

```kotlin
fun AppProgressIndicatorMini(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientScreen.kt:278</code></summary>

```kotlin
                // resources and is rendered here.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppProgressIndicatorMini()
                    Spacer(Modifier.size(KptTheme.spacing.sm))
                    Text(
                        text = stringResource(Res.string.feature_client_createnew_uploading_photo),
                        style = AppTypography.bodySmall,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppRadioButtonAlertDialog.kt`

```kotlin
fun AppRadioButtonDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppAlertDialog.kt:114</code></summary>

```kotlin

@Composable
fun AppRadioButtonDialog(
    title: String,
    selectedItem: String,
    items: Array<String>,
    selectItem: (item: String, index: Int) -> Unit,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppRoundIcon.kt`

```kotlin
fun AppRoundIcon(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart3.kt:77</code></summary>

```kotlin
) {
    AppTheme {
        AppRoundIcon(
            iconId = Res.drawable.core_ui_ic_group_black_24dp,
            modifier = modifier,
        )
    }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppRowCard.kt`

```kotlin
fun AppRowCard(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/details/ClientDetailsScreen.kt:351</code></summary>

```kotlin

            availableActions.forEach { item ->
                AppRowCard(
                    title = stringResource(item.title),
                    imageVector = item.icon,
                    leftValues = listOf(
                        TextUtil(
```

</details>

```kotlin
fun AppRowCard(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/details/ClientDetailsScreen.kt:351</code></summary>

```kotlin

            availableActions.forEach { item ->
                AppRowCard(
                    title = stringResource(item.title),
                    imageVector = item.icon,
                    leftValues = listOf(
                        TextUtil(
```

</details>

```kotlin
fun AppRowTextWithButton(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart3.kt:86</code></summary>

```kotlin
@Preview
@Composable
private fun AppRowTextWithButton() {
    AppTheme {
        AppRowTextWithButton(
            title = "Title",
            description = "Description",
```

</details>

```kotlin
fun AppRowWithLabels(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart3.kt:142</code></summary>

```kotlin
private fun AppRowWithLabelsPreview() {
    AppTheme {
        AppRowWithLabels(
            title = "Title",
            subTitle = "Sub Title",
            label1 = TextUtil("label1", color = Color.Green),
            label2 = TextUtil("label2"),
```

</details>

```kotlin
fun AppDropDownRow(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart3.kt:157</code></summary>

```kotlin
private fun AppRowDropDownPreview() {
    AppTheme {
        AppDropDownRow(
            isActive = true,
            title = "John Wault",
            accountNo = "Acc No. 838272",
            office = "Head Office",
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppRowWithTextAndButton.kt`

```kotlin
fun AppRowWithTextAndButton(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/adddocument/ClientAddDocumentScreen.kt:159</code></summary>

```kotlin
        )

        AppRowWithTextAndButton(
            // The picked file's name, or the original's "No file selected".
            text = state.fileName ?: stringResource(Res.string.feature_client_adddocument_no_file),
            btnText = stringResource(Res.string.feature_client_adddocument_pick),
            onBtnClick = { onAction(ClientAddDocumentAction.OpenFileSource) },
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppSearchBar.kt`

```kotlin
fun AppSearchBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/depositaccounts/ClientDepositAccountsScreen.kt:196</code></summary>

```kotlin

        if (state.isSearchBarActive) {
            AppSearchBar(
                query = state.searchText,
                onQueryChange = { onAction(ClientDepositAccountsAction.UpdateSearchValue(it)) },
                // Filtering is local — the list narrows as the officer types, so there is nothing
                // to submit. The originals re-fetched the whole account list per search tap.
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppSignatureDrawDialog.kt`

```kotlin
fun AppSignatureDrawDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/signature/ClientSignatureScreen.kt:283</code></summary>

```kotlin
        ClientSignatureState.DialogState.ShowUploadOptions -> UploadOptions(onAction)

        ClientSignatureState.DialogState.ShowSignatureDraw -> AppSignatureDrawDialog(
            onSaveFile = { onAction(ClientSignatureAction.CropImage(it)) },
            onDismiss = { onAction(ClientSignatureAction.OnDismissDialog) },
        )
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppStatusDialog.kt`

```kotlin
fun AppStatusDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormScreen.kt:163</code></summary>

```kotlin
    val failed = dialog as? ChargeFormState.DialogState.Failed
    val closeAction = if (failed == null) ChargeFormAction.NavigateBack else ChargeFormAction.CloseDialog
    AppStatusDialog(
        status = if (failed == null) ResultStatus.SUCCESS else ResultStatus.FAILURE,
        btnText = stringResource(Res.string.feature_charge_dialog_continue),
        // Success goes back to the list, which already shows the change; failure keeps the form.
        onConfirm = { onAction(closeAction) },
```

</details>

```kotlin
enum class ResultStatus
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormScreen.kt:164</code></summary>

```kotlin
    val closeAction = if (failed == null) ChargeFormAction.NavigateBack else ChargeFormAction.CloseDialog
    AppStatusDialog(
        status = if (failed == null) ResultStatus.SUCCESS else ResultStatus.FAILURE,
        btnText = stringResource(Res.string.feature_charge_dialog_continue),
        // Success goes back to the list, which already shows the change; failure keeps the form.
        onConfirm = { onAction(closeAction) },
        onDismissRequest = { onAction(closeAction) },
```

</details>

```kotlin
object AppStatusDialogTestTags
```
_No KDoc at source._

- `const val SECONDARY = "status-dialog:secondary"`
- `const val NOTE = "status-dialog:note"`

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppStepper.kt`

```kotlin
fun AppStepper(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart3.kt:236</code></summary>

```kotlin
    )

    AppStepper(
        steps = steps,
        currentIndex = 2,
        onStepChange = { },
        modifier = Modifier
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppTextUserImage.kt`

```kotlin
fun AppTextUserImage(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart4.kt:89</code></summary>

```kotlin
) {
    AppTheme {
        AppTextUserImage(
            text = "A",
            modifier = modifier,
        )
    }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppTexts.kt`

```kotlin
fun AppTextTitleDescSingleLine(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart4.kt:31</code></summary>

```kotlin
) {
    AppTheme {
        AppTextTitleDescSingleLine(
            title = "AppTextTitleDescSingleLine Title",
            description = "AppTextTitleDescSingleLine Description",
            modifier = modifier,
        )
```

</details>

```kotlin
fun AppTextTitleDescDoubleLine(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart4.kt:45</code></summary>

```kotlin
) {
    AppTheme {
        AppTextTitleDescDoubleLine(
            title = "AppTextTitleDescDoubleLine Title",
            description = "AppTextTitleDescDoubleLine Description",
            descriptionStyle = KptTheme.typography.bodyMedium,
            modifier = modifier,
```

</details>

```kotlin
fun AppTextTitleDescDrawableSingleLine(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart4.kt:60</code></summary>

```kotlin
) {
    AppTheme {
        AppTextTitleDescDrawableSingleLine(
            title = "AppTextTitleDescDrawableSingleLine Title",
            description = "AppTextTitleDescDrawableSingleLine Description",
            imageResId = Res.drawable.core_ui_ic_centers_24dp,
            modifier = modifier,
```

</details>

```kotlin
fun AppTitleDescSingleLineEqual(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart4.kt:75</code></summary>

```kotlin
) {
    AppTheme {
        AppTitleDescSingleLineEqual(
            title = "AppTitleDescSingleLineEqual Title",
            description = "AppTitleDescSingleLineEqual Description",
            modifier = modifier,
        )
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppToolbar.kt`

```kotlin
fun SelectionModeTopAppBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart4.kt:100</code></summary>

```kotlin
private fun SelectionModeTopAppBarPreview() {
    AppTheme {
        SelectionModeTopAppBar(
            itemCount = 3,
            resetSelectionMode = {},
        )
    }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppTwoButtonRow.kt`

```kotlin
fun AppTwoButtonRow(
```
The cancel / confirm button pair used by every form in this app.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/collateral/ClientCollateralScreen.kt:221</code></summary>

```kotlin
        if (!items.isNullOrEmpty()) {
            val selected = items[state.currentSelectedIndex.coerceIn(items.indices)]
            AppTwoButtonRow(
                firstBtnText = stringResource(Res.string.feature_client_btn_back),
                secondBtnText = stringResource(Res.string.feature_client_btn_submit),
                onFirstBtnClick = { onAction(ClientCollateralAction.NavigateBack) },
                onSecondBtnClick = { onAction(ClientCollateralAction.OnSave(selected.id)) },
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppUserImage.kt`

```kotlin
fun AppUserImage(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientScreen.kt:331</code></summary>

```kotlin
) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = KptTheme.spacing.md)) {
        AppUserImage(
            bitmap = state.photo?.bytes,
            hasBorder = true,
            modifier = Modifier
                .align(Alignment.Center)
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppUserSignature.kt`

```kotlin
fun AppUserSignatureImage(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/signature/ClientSignatureScreen.kt:183</code></summary>

```kotlin
            Spacer(Modifier.height(DesignToken.padding.largeIncreased))

            AppUserSignatureImage(
                bitmap = state.clientSignatureImage,
                emptyMessage = stringResource(Res.string.feature_client_signature_not_found),
                modifier = Modifier.testTag(ClientSignatureTestTags.IMAGE),
            )
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/DocumentKind.kt`

```kotlin
enum class DocumentKind { Pdf, Image, Unsupported }
```
What a document's bytes can be shown as.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppDocumentViewer.kt:65</code></summary>

```kotlin
    val kind = remember(bytes, fileName, mimeType) { documentKindOf(bytes, fileName, mimeType) }
    when (kind) {
        DocumentKind.Pdf -> PdfContent(bytes = bytes, modifier = modifier)

        DocumentKind.Image -> AsyncImage(
            model = bytes,
            contentDescription = stringResource(Res.string.core_ui_document_viewer_image_description),
```

</details>

```kotlin
fun documentKindOf(bytes: ByteArray, fileName: String? = null, mimeType: String? = null): DocumentKind
```
Decides how to show a document. The CONTENT is asked first — a file's leading bytes cannot be mislabelled, while a Fineract `fileName` or `type` is whatever the uploading client sent.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/EmptyDataView.kt`

```kotlin
fun EmptyDataView(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreview.kt:32</code></summary>

```kotlin
) {
    AppTheme {
        EmptyDataView(
            error = Res.string.core_ui_no_internet,
            modifier = modifier,
            image = null,
        )
```

</details>

```kotlin
fun EmptyDataView(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreview.kt:32</code></summary>

```kotlin
) {
    AppTheme {
        EmptyDataView(
            error = Res.string.core_ui_no_internet,
            modifier = modifier,
            image = null,
        )
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/MultiFloatingActionButton.kt`

```kotlin
enum class FabType
```
_No KDoc at source._

```kotlin
sealed class FabButtonState
```
_No KDoc at source._

```kotlin
sealed class FabButton(val fabType: FabType)
```
_No KDoc at source._

```kotlin
fun FabItem(
```
_No KDoc at source._

```kotlin
fun MultiFloatingActionButton(
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/NoInternet.kt`

```kotlin
fun NoInternet(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppAllUiComponentsPreviewPart4.kt:169</code></summary>

```kotlin
) {
    AppTheme {
        NoInternet(
            error = Res.string.core_ui_no_internet,
            modifier = modifier,
            isRetryEnabled = true,
            retry = {},
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/OfflineDownload.kt`

```kotlin
fun OfflineSelectionBar(
```
Shown while list rows are selected: how many, a download action, and a way out of selection.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/list/CenterListScreen.kt:129</code></summary>

```kotlin
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (ui.selected.isNotEmpty()) {
                OfflineSelectionBar(
                    count = ui.selected.size,
                    onDownload = { onAction(CenterListAction.DownloadSelected) },
                    onClear = { onAction(CenterListAction.SelectionCleared) },
                    modifier = Modifier.padding(vertical = KptTheme.spacing.sm),
```

</details>

```kotlin
fun OfflineDownloadDialog(
```
Progress of a download. While running it can be cancelled (what is already downloaded stays); once finished it says what could not be downloaded, by name and with the server's reason when it gave one.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/list/CenterListScreen.kt:235</code></summary>

```kotlin

    ui.download?.let { progress ->
        OfflineDownloadDialog(
            progress = progress,
            onCancel = { onAction(CenterListAction.DownloadCancelled) },
            onDone = { onAction(CenterListAction.DownloadDismissed) },
        )
```

</details>

```kotlin
object OfflineDownloadTestTags
```
_No KDoc at source._

- `const val SELECTION_BAR = "offline:selectionBar"`
- `const val CLEAR = "offline:clear"`
- `const val DOWNLOAD = "offline:download"`
- `const val DIALOG = "offline:dialog"`
- `const val PROGRESS = "offline:progress"`
- `const val FAILURES = "offline:failures"`
- `const val CANCEL = "offline:cancel"`
- `const val DONE = "offline:done"`

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/PrintTextUtil.kt`

```kotlin
fun PrintTextUtil(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppActionsListingCardComponent.kt:583</code></summary>

```kotlin
                    verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
                ) {
                    PrintTextUtil(
                        TextUtil(
                            text = chargeTitle,
                            style = AppTypography.titleSmallEmphasized,
                        ),
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/components/Step.kt`

```kotlin
data class Step(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/loan/src/commonTest/kotlin/kpt/feature/loan/newloan/NewLoanApplicationScreenUiTest.kt:52</code></summary>

```kotlin
    )

    private fun onStep(step: LoanStep, schedule: RepaymentSchedule? = null) =
        NewLoanApplicationState(
            clientId = 77,
            step = step,
            template = template,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/fieldofficer/utils/IdentifierStatus.kt`

```kotlin
fun getClientIdentifierStatus(status: String?): Status?
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/input/PasswordStrengthIndicator.kt`

```kotlin
fun PasswordStrengthIndicator(
```
Live strength meter for a password field, from `PasswordChecker`'s verdict. Advisory only: it reflects the rules, it does not enforce them — validation belongs at submit.

```kotlin
enum class PasswordStrengthState
```
Rendered strength bands, from weakest to strongest.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/input/RevealSwipe.kt`

```kotlin
fun RevealSwipe(
```
Swipe-to-reveal row: drag aside to expose actions (delete, archive) behind the content. The revealed actions must ALSO be reachable another way — a swipe is invisible to a screen reader and unavailable to keyboard or pointer users.

```kotlin
fun BaseRevealSwipe(
```
Unstyled `RevealSwipe` core — gesture and offset handling with no chrome, for a caller that wants its own presentation.

```kotlin
enum class RevealDirection
```
Which side(s) a row may be dragged toward to reveal its actions.

```kotlin
enum class RevealValue
```
Possible values of `RevealState`.

```kotlin
fun rememberRevealState(
```
Create and `remember` a `RevealState` with the default animation clock.

```kotlin
data class RevealState(
```
Current offset and reveal state of one `RevealSwipe` row; hoist it to reset a row programmatically.

```kotlin
suspend fun RevealState.reset()
```
Reset the component to the default position, with an animation.

```kotlin
suspend fun RevealState.resetFast()
```
Reset the component to the default position, with an animation.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/navigation/FeatureTab.kt`

```kotlin
public annotation class FeatureTab
```
Marks a `NavigationItem` object as a bottom-navigation tab contributed by a feature.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/navigation/CenterTab.kt:35</code></summary>

```kotlin
 * tabs in the bar, one of them dead.
 */
@FeatureTab
object CenterTab : NavigationItem {
    override val selectedIcon: ImageVector get() = Icons.Filled.Business
    override val icon: ImageVector get() = Icons.Outlined.Business
    override val labelRes: StringResource get() = Res.string.feature_center_tab
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/navigation/NavigationItem.kt`

```kotlin
interface NavigationItem
```
Represents a user-interactable item to navigate a user via the bottom app bar or navigation rail.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/navigation/CenterTab.kt:36</code></summary>

```kotlin
 */
@FeatureTab
object CenterTab : NavigationItem {
    override val selectedIcon: ImageVector get() = Icons.Filled.Business
    override val icon: ImageVector get() = Icons.Outlined.Business
    override val labelRes: StringResource get() = Res.string.feature_center_tab
    override val contentDescriptionRes: StringResource get() = Res.string.feature_center_tab
```

</details>

- `val selectedIcon: ImageVector` — The resource ID for the icon representing the tab when it is selected.
- `val icon: ImageVector` — Resource id for the icon representing the tab.
- `val labelRes: StringResource` — Resource id for the label describing the tab.
- `val contentDescriptionRes: StringResource` — Resource id for the content description describing the tab.
- `val graphRoute: String` — Route of the tab's graph.
- `val startDestinationRoute: String` — Route of the tab's start destination.
- `val testTag: String` — The test tag of the tab.
- `val inlineTab: Boolean get() = true` — Whether this tab renders INLINE inside the navbar scaffold — its content swaps within the inner NavHost so the bottom bar stays visible and the tab keeps its own back stack (exactly like the backbone Home/Profile tabs) — versus a FULL-SCREEN destination pushed on the outer authenticated graph (the bottom bar is hidden; e.g. an immersive focus-timer that declares `bottom_navigation_visible: false`). Default `true` (inline). A full-screen tab overrides to `false`. An inline extra tab (beyond Home/Profile) MUST also register its start destination via `ForkTabRegistry.extraInlineTabDestinations` so the inner NavHost can host it; otherwise the tab has nothing to render inline.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/navigation/NavigationRoutes.kt`

```kotlin
fun <T : Any> T.toObjectNavigationRoute(): String = this::class.toObjectKClassNavigationRoute()
```
The navigation route string for a `@Serializable` route object.

```kotlin
fun <T : Any> KClass<T>.toObjectKClassNavigationRoute(): String = this.serializer().descriptor.serialName
```
The route string for a route object's `KClass` — its serial name.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt`

```kotlin
fun KptScaffold(
```
App screen scaffold for a screen with a BACK affordance — the nav icon is always shown and `onNavigationIconClick` is required.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/ProfileScreen.kt:30</code></summary>

```kotlin
    profileBody: @Composable () -> Unit = {},
) {
    KptScaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.Profile.SCREEN),
    ) {
```

</details>

```kotlin
fun KptScaffold(
```
App screen scaffold whose back affordance is CONDITIONAL — pass `showNavigationIcon` when the same screen is reachable both as a tab root and as a pushed destination.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/ProfileScreen.kt:30</code></summary>

```kotlin
    profileBody: @Composable () -> Unit = {},
) {
    KptScaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.Profile.SCREEN),
    ) {
```

</details>

```kotlin
fun KptScaffold(
```
Fully slot-based scaffold: the caller supplies `topBar`, `bottomBar` and the FAB composables.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/ProfileScreen.kt:30</code></summary>

```kotlin
    profileBody: @Composable () -> Unit = {},
) {
    KptScaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.Profile.SCREEN),
    ) {
```

</details>

```kotlin
data class FloatingActionButtonContent(
```
Declarative FAB spec — icon, description and click — so a caller configures the FAB without passing a composable slot.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/address/ClientAddressScreen.kt:104</code></summary>

```kotlin
        // a real device, and it passed `contentDescription = null` so it was unlabelled too.
        floatingActionButtonContent = onAddAddress?.let {
            FloatingActionButtonContent(
                onClick = { viewModel.trySendAction(ClientAddressAction.ShowAddressForm) },
                contentColor = KptTheme.colorScheme.onPrimaryContainer,
                content = {
                    Icon(
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptPullToRefreshState.kt`

```kotlin
data class KptPullToRefreshState(
```
Data class representing the pull-to-refresh state and behavior.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptForkScaffold.kt:57</code></summary>

```kotlin
    containerColor: Color = KptTheme.colorScheme.background,
    floatingActionButtonContent: FloatingActionButtonContent? = null,
    pullToRefreshState: KptPullToRefreshState = rememberKptPullToRefreshState(),
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
//        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal),
```

</details>

```kotlin
fun rememberKptPullToRefreshState(
```
Remembers and returns a `KptPullToRefreshState` instance.

```kotlin
fun rememberKptPullToRefreshState(
```
Bridge: derive a `KptPullToRefreshState` from a `PagingScreenStream` so `KptScaffold` / `KptRootScaffold` can drive pull-to-refresh on paginated screens without the consumer wiring isRefreshing/onRefresh manually.

<details><summary>Example</summary>

```kotlin
KptScaffold(
    pullToRefreshState = rememberKptPullToRefreshState(viewModel.pagingStream),
    ...
) { ... }
// and disable PagingScreenContent's built-in pull-to-refresh:
PagingScreenContent(
    pagingStream = viewModel.pagingStream,
    onRetry = viewModel::onRetry,
    enablePullToRefresh = false,   // scaffold owns the gesture
) { items(coins) { ... } }
```

</details>

```kotlin
fun <T> rememberKptPullToRefreshState(
```
Bridge for non-paginated single-key screens driven by `ScreenDataStream`. Use with `KptScaffold` for detail pages that benefit from pull-to-refresh.

<details><summary>Example</summary>

```kotlin
KptScaffold(
    pullToRefreshState = rememberKptPullToRefreshState(
        stream = viewModel.stream,
        currentState = uiState,  // your collectAsStateWithLifecycle value
    ),
    ...
) { ... }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/AppPasswordChecker.kt`

```kotlin
object PasswordChecker
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordChecker.kt:20</code></summary>

```kotlin
 * Pure and platform-free, so the same verdict drives the indicator and any validation.
 */
object PasswordChecker {
    private const val MIN_PASSWORD_LENGTH = 8
    private const val STRONG_PASSWORD_LENGTH = 12
    private const val MIN_ENTROPY_BITS = 60.0
    private const val MAX_PASSWORD_LENGTH = 128
```

</details>

- `fun getPasswordStrengthResult(password: String): PasswordStrengthResult`
- `val result = getPasswordStrength(password)`
- `val length = password.length`
- `val hasUpperCase = password.any { it.isUpperCase() }`
- `val hasLowerCase = password.any { it.isLowerCase() }`
- `val hasNumbers = password.any { it.isDigit() }`
- `val hasSymbols = password.any { !it.isLetterOrDigit() }`
- `val numTypesPresent =`
- `val entropyBits = calculateEntropy(password)`
- `val charPool = 26 + 26 + 10 + 33 // lowercase + uppercase + digits + symbols`
- `fun getPasswordFeedback(password: String): List<String>`
- `val feedback = mutableListOf<String>()`

```kotlin
sealed class PasswordStrengthResult
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordChecker.kt:34</code></summary>

```kotlin
     */
    @Suppress("ReturnCount")
    fun getPasswordStrengthResult(password: String): PasswordStrengthResult {
        when {
            password.isEmpty() -> return PasswordStrengthResult.Error("Password cannot be empty.")
            password.length > MAX_PASSWORD_LENGTH -> {
                return PasswordStrengthResult.Error(
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/BackgroundEvent.kt`

```kotlin
interface BackgroundEvent
```
Almost all the events in the app involve navigation or toasts. To prevent accidentally navigating to the same view twice, by default, events are ignored if the view is not currently resumed.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/util/EventsEffect.kt:37</code></summary>

```kotlin
        eventFlow
            .filter {
                it is BackgroundEvent ||
                    lifecycleOwner.currentState.isAtLeast(Lifecycle.State.RESUMED)
            }
            .onEach { handler.invoke(it) }
            .launchIn(this)
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/BaseViewModel.kt`

```kotlin
abstract class BaseViewModel<S, E, A>(
```
A base `ViewModel` that helps enforce the unidirectional data flow pattern and associated responsibilities of a typical ViewModel: - Maintaining and emitting a current state (of type `S`) with the given `initialState`.

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutViewModel.kt:36</code></summary>

```kotlin
internal class AboutViewModel(
    config: AboutConfig = AboutConfig.fromBuild(),
) : BaseViewModel<AboutState, AboutEvent, AboutAction>(AboutState.of(config)) {

    override fun handleAction(action: AboutAction) {
        when (action) {
            AboutAction.NavigateBack -> sendEvent(AboutEvent.NavigateBack)
```

</details>

- `val stateFlow: StateFlow<S> = mutableStateFlow.asStateFlow()` — A `StateFlow` representing state updates.
- `val eventFlow: Flow<E> = eventChannel.receiveAsFlow()` — A `Flow` of one-shot events. These may be received and consumed by only a single consumer. Any additional consumers will receive no events.
- `val actionChannel: SendChannel<A> = internalActionChannel` — A `SendChannel` for sending actions to the ViewModel for processing.
- `fun trySendAction(action: A)` — Convenience method for sending an action to the `actionChannel`.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/DevicePreview.kt`

```kotlin
annotation class DevicePreview
```
Multipreview annotation that represents various device sizes. Add this annotation to a composable to render various devices.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/EventsEffect.kt`

```kotlin
fun <E> EventsEffect(
```
Convenience method for observing event flow from `BaseViewModel`. By default, events will only be consumed when the associated screen is resumed, to avoid bugs like duplicate navigation calls.

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:94</code></summary>

```kotlin
    // core-BASE's ShareUtils — the one MainActivity wires with `setActivityProvider`. core/ui has a
    // look-alike copy that nothing wires; calling it threw on the first tap (device, 2026-10-24).
    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            AboutEvent.NavigateBack -> onNavigateBack()
            AboutEvent.OpenAppInfo -> ShareUtils.openAppInfo()
            is AboutEvent.OpenUrl -> ShareUtils.openUrl(event.url)
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/ImageToByteArray.kt`

```kotlin
fun imageToByteArray(image: String?): ByteArray?
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/ImageUtil.kt`

```kotlin
expect object ImageUtil
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/androidMain/kotlin/kpt/core/ui/util/ImageUtil.android.kt:20</code></summary>

```kotlin
import java.io.ByteArrayOutputStream

actual object ImageUtil {
    actual val DEFAULT_MAX_WIDTH: Float = 816f
    actual val DEFAULT_MAX_HEIGHT: Float = 612f

    actual fun compressImage(
```

</details>

- `val DEFAULT_MAX_WIDTH: Float`
- `val DEFAULT_MAX_HEIGHT: Float`
- `fun compressImage(`

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/LottieConstants.kt`

```kotlin
object LottieConstants
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AppProgressIndicator.kt:47</code></summary>

```kotlin
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes(LottieConstants.LOADING_ANIMATION).decodeToString(),
        )
    }
    val progress by animateLottieCompositionAsState(
        composition,
```

</details>

- `const val LOADING_ANIMATION = "files/loading_animation.json"`

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/MultipartRequestBody.kt`

```kotlin
fun multipartRequestBody(
```
_No KDoc at source._

```kotlin
fun documentUpload(
```
What the officer picked, as a `DocumentUpload` — the document write path's input. The multipart encoding lives in `core/network` now; the UI only names the file it chose.

```kotlin
fun getMimeTypeFromPlatformFile(extension: String): String
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/ShareUtils.kt`

```kotlin
expect object ShareUtils
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:97</code></summary>

```kotlin
        when (event) {
            AboutEvent.NavigateBack -> onNavigateBack()
            AboutEvent.OpenAppInfo -> ShareUtils.openAppInfo()
            is AboutEvent.OpenUrl -> ShareUtils.openUrl(event.url)
        }
    }
```

</details>

- `fun shareText(text: String)`
- `suspend fun shareImage(title: String, image: ImageBitmap)`
- `suspend fun shareImage(title: String, byte: ByteArray)`
- `fun openAppInfo()`
- `fun shareApp()`
- `fun callHelpline()`
- `fun mailHelpline()`
- `fun openUrl(url: String)`
- `fun ossLicensesMenuActivity()`
- `fun restartApplication()`

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/TestTagsAsResourceIds.kt`

```kotlin
expect fun Modifier.exposeTestTagsAsResourceIds(): Modifier
```
Expose every `Modifier.testTag` below this node as an Android resource id, so device-level tools (Maestro `id:` selectors, UI Automator) can find them.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/TextFieldsValidator.kt`

```kotlin
object TextFieldsValidator
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/collateral/ClientCollateralViewModel.kt:131</code></summary>

```kotlin

    private fun save(collateralId: Int) {
        val quantityError = TextFieldsValidator.numberValidator(state.quantity)
        if (quantityError != null) {
            mutableStateFlow.value = state.copy(quantityError = quantityError)
            return
        }
```

</details>

- `fun stringValidator(input: String): StringResource?`
- `fun optionalStringValidator(input: String): StringResource?`
- `fun numberValidator(input: String): StringResource?`
- `fun doubleNumberValidator(input: String): StringResource?`
- `fun dropDownEmptyValidator(input: Boolean): StringResource?`

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/TextUtil.kt`

```kotlin
data class TextUtil(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/details/ClientDetailsScreen.kt:355</code></summary>

```kotlin
                    imageVector = item.icon,
                    leftValues = listOf(
                        TextUtil(
                            text = stringResource(item.subTitle),
                            style = AppTypography.bodySmall,
                            color = KptTheme.colorScheme.secondary,
                        ),
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/util/ToDateFromList.kt`

```kotlin
fun List<Int?>?.toDateString(): String
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:217</code></summary>

```kotlin

@Composable
private fun LinkSection(title: String, links: List<AboutLink>, onAction: (AboutAction) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = KptTheme.spacing.md)) {
        Text(
            text = title,
            style = KptTheme.typography.titleSmall,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordChecker.kt`

```kotlin
object PasswordChecker
```
Scores a password against the app's rules and explains what is missing. Pure and platform-free, so the same verdict drives the indicator and any validation.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/util/AppPasswordChecker.kt:16</code></summary>

```kotlin
import kotlin.math.pow

object PasswordChecker {
    private const val MIN_PASSWORD_LENGTH = 8
    private const val STRONG_PASSWORD_LENGTH = 12
    private const val MIN_ENTROPY_BITS = 60.0
    private const val MAX_PASSWORD_LENGTH = 128
```

</details>

- `fun getPasswordStrengthResult(password: String): PasswordStrengthResult` — Validates then scores a password. Returns an error for empty or over-length input rather than a band, because neither has a meaningful strength.
- `val result = getPasswordStrength(password)`
- `fun getPasswordStrength(password: String): PasswordStrength` — Scores a password into a band from character-class variety, length and entropy.
- `val length = password.length`
- `val hasUpperCase = password.any { it.isUpperCase() }`
- `val hasLowerCase = password.any { it.isLowerCase() }`
- `val hasNumbers = password.any { it.isDigit() }`
- `val hasSymbols = password.any { !it.isLetterOrDigit() }`
- `val numTypesPresent =`
- `val entropyBits = calculateEntropy(password)`
- `val charPool = 26 + 26 + 10 + 33 // lowercase + uppercase + digits + symbols`
- `fun getPasswordFeedback(password: String): List<String>` — The unmet requirements, as user-facing sentences — what to fix, not just that it is weak. Empty when the password satisfies every rule.
- `val feedback = mutableListOf<String>()`

```kotlin
sealed class PasswordStrengthResult
```
The verdict: a strength band plus the unmet requirements, so the UI can say WHAT to fix rather than only that it is weak.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/util/AppPasswordChecker.kt:22</code></summary>

```kotlin
    private const val MAX_PASSWORD_LENGTH = 128

    fun getPasswordStrengthResult(password: String): PasswordStrengthResult {
        return when {
            password.isEmpty() -> PasswordStrengthResult.Error("Password cannot be empty.")
            password.length > MAX_PASSWORD_LENGTH -> {
                PasswordStrengthResult.Error(
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordStrength.kt`

```kotlin
enum class PasswordStrength
```
Password strength bands used by `PasswordChecker`.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/util/AppPasswordChecker.kt:32</code></summary>

```kotlin

            else -> {
                val result = getPasswordStrength(password)
                PasswordStrengthResult.Success(result)
            }
        }
    }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordStrengthExtensions.kt`

```kotlin
fun Int.toPasswordStrengthOrNull(): PasswordStrength? = when (this)
```
Converts the given `Int` to a `PasswordStrength`. A `null` value is returned if this value is not in the [0, 4] range.

```kotlin
fun PasswordStrength.toInt(): Int = when (this)
```
Converts the given `PasswordStrength` to an `Int`.

---

_37 type(s), 205 function(s)/property(ies); 85 carry KDoc at source; 2 authored example(s); 107 live call site(s)._
<!-- api-docs:end -->
