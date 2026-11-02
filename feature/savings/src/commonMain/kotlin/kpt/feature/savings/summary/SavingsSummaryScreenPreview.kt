/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.summary

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screen is not
 * previewed; its stateless `*Content` half is. Values are server content, not app copy —
 * `// i18n:skip`.
 */

// i18n:skip — preview fixtures below
private val previewAccount = SavingsSummary(
    accountNo = "000000009",
    clientName = "Nusrat Jahan Chowdhury",
    productName = "Voluntary Savings",
    currencyCode = "BDT",
    decimalPlaces = 2,
    accountBalance = 12_500.0,
    totalDeposits = 15_000.0,
    totalWithdrawals = 2_500.0,
    interestEarned = 210.0,
    transactions = listOf(
        SavingsTransactionRow(
            id = 3,
            typeLabel = "Withdrawal",
            date = "05 Oct 2025",
            amount = 2_500.0,
            runningBalance = 12_500.0,
            isDeposit = false,
            isWithdrawal = true,
            isReversed = false,
        ),
        SavingsTransactionRow(
            id = 2,
            typeLabel = "Deposit",
            date = "01 Oct 2025",
            amount = 5_000.0,
            runningBalance = 15_000.0,
            isDeposit = true,
            isWithdrawal = false,
            isReversed = false,
        ),
        SavingsTransactionRow(
            id = 1,
            typeLabel = "Deposit",
            date = "15 Sep 2025",
            amount = 10_000.0,
            runningBalance = 10_000.0,
            isDeposit = true,
            isWithdrawal = false,
            isReversed = true,
        ),
    ),
    available = SavingsAction.TRANSACT,
)

@Preview
@Composable
internal fun SavingsSummaryContentPreview() {
    KptTheme {
        SavingsSummaryContent(
            account = ScreenState.Content(previewAccount),
            onAction = {},
            canTransact = true,
            canOpenDocuments = true,
            canOpenCharges = true,
        )
    }
}

@Preview
@Composable
internal fun SavingsSummaryContentAwaitingApprovalPreview() {
    KptTheme {
        SavingsSummaryContent(
            account = ScreenState.Content(
                previewAccount.copy(transactions = emptyList(), available = SavingsAction.APPROVE),
            ),
            onAction = {},
            canApprove = true,
        )
    }
}
