/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import kpt.core.designsystem.theme.MifosTypography
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_core_common_working
import kpt.core.ui.generated.resources.core_ui_ic_centers_24dp
import kpt.core.ui.generated.resources.core_ui_ic_group_black_24dp
import kpt.core.ui.generated.resources.core_ui_no_internet
import kpt.core.ui.util.DevicePreview
import kpt.core.ui.util.TextUtil
import org.jetbrains.compose.ui.tooling.preview.Preview

@DevicePreview
@Composable
private fun EmptyDataViewPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        EmptyDataView(
            error = Res.string.core_ui_no_internet,
            modifier = modifier,
            image = null,
        )
    }
}

@DevicePreview
@Composable
private fun EmptyDataViewPreviewForIcon(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        EmptyDataView(
            error = "This is error",
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun PreviewMifosActionsNoteListingComponent() {
    KptTheme {
        MifosActionsNoteListingComponent(
            createdBy = "John Doe",
            date = "02 September 2025",
            notes = "This is a note",
            menuList = listOf(
                Actions.Edit(),
                Actions.Delete(),
            ),
            onExpand = {},
            isExpanded = true,
            onActionClicked = { action ->
                when (action) {
                    is Actions.Edit -> println(Actions.ViewAccount::class.simpleName ?: "")
                    is Actions.Delete -> println(Actions.ApproveAccount::class.simpleName ?: "")
                    else -> println("Action not Handled")
                }
            },
        )
    }
}

@Preview
@Composable
private fun PreviewMifosActionsIdentifierListingComponent() {
    KptTheme {
        MifosActionsIdentifierListingComponent(
            type = "Passport",
            id = "P1234567",
            key = "Government ID",
            status = Status.Active,
            description = "Primary identification document",
            identifyDocuments = "Passport Scan",
            menuList = listOf(
                Actions.ViewDocument(),
                Actions.UploadAgain(),
                Actions.DeleteDocument(),
            ),
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewDocument -> println(Actions.ViewDocument::class.simpleName)
                    is Actions.UploadAgain -> println(Actions.UploadAgain::class.simpleName)
                    is Actions.DeleteDocument -> println(Actions.DeleteDocument::class.simpleName)
                    else -> println("Action not Handled")
                }
            },
            onClick = {},
            isExpanded = true,
        )
    }
}

@Preview
@Composable
private fun PreviewMifosActionsClientFeeListingComponent() {
    KptTheme {
        MifosActionsClientFeeListingComponent(
            name = "John Doe",
            dueAsOf = "2025-08-19",
            due = "$1000",
            paid = "$400",
            waived = "$50",
            outstanding = "$550",
            menuList = listOf(
                Actions.ViewAccount(),
                Actions.ApproveAccount(),
            ),
            isActive = true,
            onClick = {},
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewAccount -> println(Actions.ViewDocument::class.simpleName)
                    is Actions.ApproveAccount -> println(Actions.ApproveAccount::class.simpleName)
                    else -> println("Action not Handled")
                }
            },
        )
    }
}

@Preview
@Composable
private fun PreviewMifosActionsSavingsListingComponent() {
    KptTheme {
        MifosActionsSavingsListingComponent(
            accountNo = "SV9876",
            savingsProduct = "Savings Product",
            savingsProductName = "Wallet",
            lastActive = "2025-08-15",
            balance = "$1200",
            menuList = listOf(
                Actions.ViewAccount(),
                Actions.ApproveAccount(),
            ),
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewAccount -> println(Actions.ViewDocument::class.simpleName)
                    is Actions.ApproveAccount -> println(Actions.ApproveAccount::class.simpleName)
                    else -> println("Action not Handled")
                }
            },
            isExpanded = true,
            onExpandToggle = {},
        )
    }
}

@Preview
@Composable
private fun PreviewMifosActionsCollateralDataListingComponent() {
    KptTheme {
        MifosActionsCollateralDataListingComponent(
            name = "Gold Jewelry",
            quantity = "5",
            totalValue = "$2500",
            totalCollateralValue = "$2500",
            menuList = listOf(
                Actions.ViewAccount(),
                Actions.ApproveAccount(),
            ),
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewAccount -> println(Actions.ViewDocument::class.simpleName)
                    is Actions.ApproveAccount -> println(Actions.ApproveAccount::class.simpleName)
                    else -> println("Action not Handled")
                }
            },
        )
    }
}

@Preview
@Composable
private fun PreviewMifosActionsLoanListingComponent() {
    KptTheme {
        MifosActionsLoanListingComponent(
            accountNo = "LN12345",
            loanProduct = "Personal Loan",
            originalLoan = "$5000",
            amountPaid = "$2000",
            loanBalance = "$3000",
            type = "Active",
            status = "Active",
            menuList = listOf(
                Actions.ViewAccount(),
                Actions.ApproveAccount(),
            ),
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewAccount -> println(Actions.ViewAccount::class.simpleName)
                    is Actions.ApproveAccount -> println(Actions.ApproveAccount::class.simpleName)
                    else -> println("Action not Handled")
                }
            },
        )
    }
}

@Preview
@Composable
private fun MifosAddressCardPreview() {
    val sampleAddresses = mapOf(
        "Address 1" to "123, MG Road",
        "Address 2" to "2nd Floor, Lotus Building",
        "City" to "Bengaluru",
        "State" to "Karnataka",
        "Pincode" to "560001",
        "Country" to "India",
    )

    MifosTheme {
        MifosAddressCard(
            title = "Client Address",
            addressList = sampleAddresses,
        )
    }
}

@Preview
@Composable
private fun MifosAlertDialogPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosAlertDialog(
            dialogTitle = "Dialog Title",
            dialogText = "Dialog Text",
            dismissText = "Dismiss",
            confirmationText = "Confirm",
            onDismissRequest = {},
            onConfirmation = {},
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun BreadcrumbItemPreview() {
    MifosTheme {
        Row(
            modifier = Modifier.padding(DesignToken.padding.medium),
        ) {
            BreadcrumbItem(
                text = "Home",
                isActive = false,
                onClick = {},
            )
            Text(" > ")
            BreadcrumbItem(
                text = "Loan Account",
                isActive = true,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun MifosCheckBoxPreview() {
    MifosTheme {
        MifosCheckBox(
            checked = false,
            onCheckChanged = {},
            text = "",
        )
    }
}

@Preview
@Composable
private fun MifosEmptyCardPreview() {
    MifosTheme {
        MifosEmptyCard("Add any to show")
    }
}

@Preview
@Composable
private fun MifosEmptyUiPreview() {
    MifosTheme {
        MifosEmptyUi(
            text = "No data available",
            icon = MifosIcons.Info,
        )
    }
}

@Preview
@Composable
private fun NoInternetPreview() {
    MifosTheme {
        NoInternetComponent()
    }
}

@Preview
@Composable
private fun EmptyDataPreview() {
    MifosTheme {
        EmptyDataComponent()
    }
}

@Preview
@Composable
private fun EmptyDataComponentWithModifiedMessageAndIconPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        EmptyDataComponentWithModifiedMessageAndIcon(
            message = "No data found",
            icon = MifosIcons.Error,
            modifier = modifier,
            isEmptyData = true,
        )
    }
}

@Preview
@Composable
private fun MifosFABPreview() {
    MifosTheme {
        MifosFAB(
            icon = MifosIcons.Info,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun MifosMobileIconPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosIcon(
            mobileIcon = Res.drawable.core_ui_ic_centers_24dp,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun MifosItemCardPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosItemCard(
            onClick = {},
            modifier = modifier,
        ) {
            Text(text = "Card Content")
        }
    }
}

@Preview
@Composable
private fun MifosLinkTextPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosLinkText(
            text = "Link Text",
            onClick = {},
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun MifosDefaultListingComponentPreview() {
    val sampleData = mapOf(
        "Name" to "John Doe",
        "Email" to "john.doe@example.com",
        "Role" to "Developer",
        "Location" to "Remote",
    )

    MifosTheme {
        MifosDefaultListingComponent(data = sampleData)
    }
}

@Preview
@Composable
private fun PreviewMifosClientFeeListingComponent() {
    KptTheme {
        MifosClientFeeListingComponent(
            name = "John Doe",
            dueAsOf = "2025-08-19",
            due = "$1000",
            paid = "$400",
            waived = "$50",
            outstanding = "$550",
        )
    }
}

@Preview
@Composable
private fun PreviewMifosLoanListingComponent() {
    KptTheme {
        MifosLoanListingComponent(
            accountNo = "LN12345",
            loanProduct = "Personal Loan",
            originalLoan = "$5000",
            amountPaid = "$2000",
            loanBalance = "$3000",
            type = "Active",
        )
    }
}

@Preview
@Composable
private fun PreviewMifosSavingsListingComponent() {
    KptTheme {
        MifosSavingsListingComponent(
            accountNo = "SV9876",
            savingsProduct = "Regular Savings",
            lastActive = "2025-08-15",
            balance = "$1200",
        )
    }
}

@Preview
@Composable
private fun PreviewMifosCollateralDataListingComponent() {
    KptTheme {
        MifosCollateralDataListingComponent(
            name = "Gold Jewelry",
            quantity = "5",
            totalValue = "$2500",
            totalCollateralValue = "$2500",
        )
    }
}

@Preview
@Composable
private fun PreviewMifosIdentifierListingComponent() {
    KptTheme {
        MifosIdentifierListingComponent(
            type = "Passport",
            id = "P1234567",
            key = "Government ID",
            status = Status.Active,
            description = "Primary identification document",
            identifyDocuments = "Passport Scan",
        )
    }
}

@Preview
@Composable
private fun PreviewMifosDelinquencyListingComponent() {
    KptTheme {
        MifosDelinquencyListingComponent(
            date = "2025-08-01",
            time = "10:00 AM",
            isPause = false,
            startDate = "2025-07-01",
            endDate = "2025-07-31",
        )
    }
}

@Preview
@Composable
private fun PreviewMifosChargesListingComponent() {
    KptTheme {
        MifosChargesListingComponent(
            title = "Processing Fee",
            type = "Flat",
            dueAsOf = "2025-08-19",
            calculationType = "Fixed",
            due = "$100",
            paid = "$50",
            waived = "$10",
            outstanding = "$40",
        )
    }
}

@Preview
@Composable
private fun PreviewMifosSellLoanListingComponent() {
    KptTheme {
        MifosSellLoanListingComponent(
            status = Status.Pending,
            effectiveForm = "2025-08-20",
            ownerExternalId = "OWN123",
            transferExternalId = "TRF456",
            settlementDate = "2025-08-25",
            purchasePriceRatio = "0.85",
        )
    }
}

@Preview
@Composable
private fun PreviewMifosHeader() {
    KptTheme {
        MifosListingRowItemHeader(
            text = "Hello",
        )
    }
}

@Preview
@Composable
private fun MifosProgressIndicatorPreview() {
    MifosTheme {
        MifosProgressIndicator()
    }
}

@Preview
@Composable
private fun MifosProgressIndicatorOverlayPreview() {
    MifosTheme {
        MifosProgressIndicatorOverlay()
    }
}

@Preview
@Composable
private fun PreviewRadioButtonDialog() {
    MifosTheme {
        MifosRadioButtonDialog(
            titleResId = Res.string.core_ui_core_common_working,
            items = arrayOf("1", "2", "3"),
            selectedItem = "1",
            onDismissRequest = { },
            selectItem = { _, _ -> },
        )
    }
}

@DevicePreview
@Composable
private fun MifosRoundIconPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosRoundIcon(
            iconId = Res.drawable.core_ui_ic_group_black_24dp,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun MifosRowTextWithButton() {
    MifosTheme {
        MifosRowTextWithButton(
            title = "Title",
            description = "Description",
            size = "Size",
            btnText = "Button",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewMifosRowCard() {
    MifosTheme {
        MifosRowCard(
            title = "Customer Info",
            leftValues = listOf(
                TextUtil(text = "Name", style = MifosTypography.labelMediumEmphasized),
                TextUtil(text = "Age", style = MifosTypography.labelMedium),
                TextUtil(text = "City", style = MifosTypography.labelMedium),
            ),
            rightValues = listOf(
                TextUtil(text = "453", style = MifosTypography.labelMedium, color = Color.Green),
                TextUtil(text = "455", style = MifosTypography.labelMedium),
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewMifosRowCardWithImage() {
    MifosTheme {
        MifosRowCard(
            title = "Customer Info",
            leftValues = listOf(
                TextUtil(text = "Name", style = MifosTypography.labelMediumEmphasized),
                TextUtil(text = "Age", style = MifosTypography.labelMedium),
                TextUtil(text = "City", style = MifosTypography.labelMedium),
            ),
            rightValues = listOf(
                TextUtil(text = "Closed", style = MifosTypography.labelMedium, color = Color.Red),
                TextUtil(text = "455", style = MifosTypography.labelMedium),
            ),
            byteArray = null,
        )
    }
}

@Preview
@Composable
private fun MifosRowWithLabelsPreview() {
    MifosTheme {
        MifosRowWithLabels(
            title = "Title",
            subTitle = "Sub Title",
            label1 = TextUtil("label1", color = Color.Green),
            label2 = TextUtil("label2"),
            onClick = {},
            byteArray = null,
        )
    }
}

@Preview
@Composable
private fun MifosRowDropDownPreview() {
    MifosTheme {
        MifosDropDownRow(
            isActive = true,
            title = "John Wault",
            accountNo = "Acc No. 838272",
            office = "Head Office",
            date = "20-04-2020",
            onClick = {},
        )
    }
}

@Composable
@Preview
private fun MifosRowWithTextAndButtonPreview() {
    MifosTheme {
        MifosRowWithTextAndButton(
            onBtnClick = {},
            btnText = "View",
            text = "2 Collaterals",
        )
    }
}

@Preview
@Composable
private fun MifosSearchBarPreview() {
    var text by remember { mutableStateOf("") }

    MifosSearchBar(
        query = text,
        onQueryChange = { text = it },
        onBackClick = { },
        onSearchClick = { println("Searching for $it") },
    )
}

@Composable
@Preview
private fun MifosSuccessStatusDialogPreview() {
    MifosTheme {
        MifosStatusDialog(
            status = ResultStatus.SUCCESS,
            onConfirm = {},
            btnText = "OK",
            successTitle = "Success",
            successMessage = "Operation Successful",
            failureTitle = "Failure",
            failureMessage = "Operation Failed",
        )
    }
}

@Composable
@Preview
private fun MifosFailureStatusDialogPreview() {
    MifosTheme {
        MifosStatusDialog(
            status = ResultStatus.FAILURE,
            onConfirm = {},
            btnText = "OK",
            successTitle = "Success",
            successMessage = "Operation Successful",
            failureTitle = "Failure",
            failureMessage = "Operation Failed",
        )
    }
}

@Preview
@Composable
private fun MifosStepperDemo() {
    val steps = listOf(
        Step("Details") { Text("Step 1: Details Content") },
        Step("Terms") { Text("Step 2: Terms Content") },
        Step("Charges") { Text("Step 3: Charges Content") },
        Step("Schedule") { Text("Step 4: Schedule Content") },
        Step("Preview") { Text("Step 5: Preview Content") },
    )

    MifosStepper(
        steps = steps,
        currentIndex = 2,
        onStepChange = { },
        modifier = Modifier
            .fillMaxWidth(),
    )
}

@DevicePreview
@Composable
private fun MifosTextTitleDescSingleLinePreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosTextTitleDescSingleLine(
            title = "MifosTextTitleDescSingleLine Title",
            description = "MifosTextTitleDescSingleLine Description",
            modifier = modifier,
        )
    }
}

@DevicePreview
@Composable
private fun MifosTextTitleDescDoubleLinePreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosTextTitleDescDoubleLine(
            title = "MifosTextTitleDescDoubleLine Title",
            description = "MifosTextTitleDescDoubleLine Description",
            descriptionStyle = KptTheme.typography.bodyMedium,
            modifier = modifier,
        )
    }
}

@DevicePreview
@Composable
private fun MifosTextTitleDescDrawableSingleLinePreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosTextTitleDescDrawableSingleLine(
            title = "MifosTextTitleDescDrawableSingleLine Title",
            description = "MifosTextTitleDescDrawableSingleLine Description",
            imageResId = Res.drawable.core_ui_ic_centers_24dp,
            modifier = modifier,
        )
    }
}

@DevicePreview
@Composable
private fun MifosTitleDescSingleLineEqualPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosTitleDescSingleLineEqual(
            title = "MifosTitleDescSingleLineEqual Title",
            description = "MifosTitleDescSingleLineEqual Description",
            modifier = modifier,
        )
    }
}

@DevicePreview
@Composable
private fun MifosTextUserImagePreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosTextUserImage(
            text = "A",
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun SelectionModeTopAppBarPreview() {
    MifosTheme {
        SelectionModeTopAppBar(
            itemCount = 3,
            resetSelectionMode = {},
        )
    }
}

@Composable
@Preview
private fun MifosTwoButtonRowPreview() {
    MifosTheme {
        MifosTwoButtonRow(
            firstBtnText = "First Button",
            secondBtnText = "Second Button",
            onFirstBtnClick = {},
            onSecondBtnClick = {},
        )
    }
}

@DevicePreview
@Composable
private fun MifosUserImagePreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosUserImage(
            bitmap = null,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun SelectionModeTopAppBar() {
    MifosTheme {
        SelectionModeTopAppBar(
            itemCount = 3,
            resetSelectionMode = {},
        )
    }
}

@Preview
@Composable
private fun SelectionModeTopAppBarWithActionsPreview() {
    MifosTheme {
        SelectionModeTopAppBar(
            itemCount = 5,
            resetSelectionMode = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = MifosIcons.Delete,
                        contentDescription = "delete",
                    )
                }
            },
        )
    }
}

@DevicePreview
@Composable
private fun NoInternetPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        NoInternet(
            error = Res.string.core_ui_no_internet,
            modifier = modifier,
            isRetryEnabled = true,
            retry = {},
        )
    }
}

@Preview
@Composable
private fun ChargeDialogPreview() {
    val sampleChargeOptions = listOf("Bank Fee", "Overdue Fee", "Processing Fee")

    MifosTheme {
        AddChargeBottomSheet(
            title = "Add Charge",
            confirmText = "Confirm",
            dismissText = "Cancel",
            showDatePicker = false,
            selectedChargeName = sampleChargeOptions[0],
            selectedDate = "2025-09-06",
            chargeAmount = "1500",
            chargeType = "Flat",
            chargeCollectedOn = "2025-09-06",
            chargeOptions = sampleChargeOptions,
            onConfirm = { },
            onDismiss = { },
            onChargeSelected = { _, _ -> },
            onDatePick = { },
            onDateChange = { },
            onAmountChange = {},
        )
    }
}

@Preview
@Composable
private fun MifosActionsChargeListingComponentPreview() {
    MifosTheme {
        MifosActionsChargeListingComponent(
            chargeTitle = "Bank Fee",
            type = "Flat",
            date = "06-09-2025",
            collectedOn = "06-09-2025",
            amount = "1500 UGX",
            onActionClicked = { },
            isExpanded = true,
            onExpandToggle = {},
        )
    }
}

@Preview
@Composable
private fun PreviewMifosActionsShareListingComponent() {
    val sampleMenu = listOf(
        Actions.Edit(),
        Actions.Delete(),
    )

    MifosTheme {
        MifosActionsShareListingComponent(
            accountNo = "AC123456",
            shareProductName = "Gold Savings Plan",
            pendingForApprovalShares = 5,
            approvedShares = 10,
            isExpanded = true,
            onClick = {},
            menuList = sampleMenu,
            onActionClicked = { },
        )
    }
}
