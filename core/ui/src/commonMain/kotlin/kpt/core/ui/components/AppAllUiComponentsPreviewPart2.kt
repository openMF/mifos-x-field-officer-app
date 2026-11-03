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

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTheme
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_ic_centers_24dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun EmptyDataPreview() {
    AppTheme {
        EmptyDataComponent()
    }
}

@Preview
@Composable
private fun EmptyDataComponentWithModifiedMessageAndIconPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        EmptyDataComponentWithModifiedMessageAndIcon(
            message = "No data found",
            icon = AppIcons.Error,
            modifier = modifier,
            isEmptyData = true,
        )
    }
}

@Preview
@Composable
private fun AppFABPreview() {
    AppTheme {
        AppFAB(
            icon = AppIcons.Info,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun AppMobileIconPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppIcon(
            mobileIcon = Res.drawable.core_ui_ic_centers_24dp,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun AppItemCardPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppItemCard(
            onClick = {},
            modifier = modifier,
        ) {
            Text(text = "Card Content")
        }
    }
}

@Preview
@Composable
private fun AppLinkTextPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppLinkText(
            text = "Link Text",
            onClick = {},
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun AppDefaultListingComponentPreview() {
    val sampleData = mapOf(
        "Name" to "John Doe",
        "Email" to "john.doe@example.com",
        "Role" to "Developer",
        "Location" to "Remote",
    )

    AppTheme {
        AppDefaultListingComponent(data = sampleData)
    }
}

@Preview
@Composable
private fun PreviewAppClientFeeListingComponent() {
    KptTheme {
        AppClientFeeListingComponent(
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
private fun PreviewAppLoanListingComponent() {
    KptTheme {
        AppLoanListingComponent(
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
private fun PreviewAppSavingsListingComponent() {
    KptTheme {
        AppSavingsListingComponent(
            accountNo = "SV9876",
            savingsProduct = "Regular Savings",
            lastActive = "2025-08-15",
            balance = "$1200",
        )
    }
}

@Preview
@Composable
private fun PreviewAppCollateralDataListingComponent() {
    KptTheme {
        AppCollateralDataListingComponent(
            name = "Gold Jewelry",
            quantity = "5",
            totalValue = "$2500",
            totalCollateralValue = "$2500",
        )
    }
}

@Preview
@Composable
private fun PreviewAppIdentifierListingComponent() {
    KptTheme {
        AppIdentifierListingComponent(
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
private fun PreviewAppDelinquencyListingComponent() {
    KptTheme {
        AppDelinquencyListingComponent(
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
private fun PreviewAppChargesListingComponent() {
    KptTheme {
        AppChargesListingComponent(
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
private fun PreviewAppSellLoanListingComponent() {
    KptTheme {
        AppSellLoanListingComponent(
            status = Status.Pending,
            effectiveForm = "2025-08-20",
            ownerExternalId = "OWN123",
            transferExternalId = "TRF456",
            settlementDate = "2025-08-25",
            purchasePriceRatio = "0.85",
        )
    }
}
