/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_no_internet
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun EmptyDataViewPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        EmptyDataView(
            error = Res.string.core_ui_no_internet,
            modifier = modifier,
            image = null,
        )
    }
}

@Preview
@Composable
private fun EmptyDataViewPreviewForIcon(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        EmptyDataView(
            error = "This is error",
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun PreviewAppActionsNoteListingComponent() {
    KptTheme {
        AppActionsNoteListingComponent(
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
private fun PreviewAppActionsIdentifierListingComponent() {
    KptTheme {
        AppActionsIdentifierListingComponent(
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
private fun PreviewAppActionsClientFeeListingComponent() {
    KptTheme {
        AppActionsClientFeeListingComponent(
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
private fun PreviewAppActionsSavingsListingComponent() {
    KptTheme {
        AppActionsSavingsListingComponent(
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
private fun PreviewAppActionsCollateralDataListingComponent() {
    KptTheme {
        AppActionsCollateralDataListingComponent(
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
private fun PreviewAppActionsLoanListingComponent() {
    KptTheme {
        AppActionsLoanListingComponent(
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
private fun AppAddressCardPreview() {
    val sampleAddresses = mapOf(
        "Address 1" to "123, MG Road",
        "Address 2" to "2nd Floor, Lotus Building",
        "City" to "Bengaluru",
        "State" to "Karnataka",
        "Pincode" to "560001",
        "Country" to "India",
    )

    AppTheme {
        AppAddressCard(
            title = "Client Address",
            addressList = sampleAddresses,
        )
    }
}

@Preview
@Composable
private fun AppAlertDialogPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppAlertDialog(
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
    AppTheme {
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
private fun AppCheckBoxPreview() {
    AppTheme {
        AppCheckBox(
            checked = false,
            onCheckChanged = {},
            text = "",
        )
    }
}

@Preview
@Composable
private fun AppEmptyCardPreview() {
    AppTheme {
        AppEmptyCard(msg = "Add any to show")
    }
}

@Preview
@Composable
private fun AppEmptyUiPreview() {
    AppTheme {
        AppEmptyUi(
            text = "No data available",
            icon = AppIcons.Info,
        )
    }
}

@Preview
@Composable
private fun NoInternetPreview() {
    AppTheme {
        NoInternetComponent()
    }
}
