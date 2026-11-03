/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.transactions

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
private fun previewTransaction(
    n: Int,
    type: TransactionType,
    amount: String,
    balance: String,
    reversed: Boolean = false,
) = TransactionRow(
    number = "$n", id = "${100 + n}", office = "Head Office", externalId = "", transactionDate = "1$n Oct 2025",
    transactionType = type, amount = amount, principal = amount, interest = "0.00", fees = "0.00", penalties = "0.00",
    loanBalance = balance, manuallyReversed = reversed,
)

@Preview
@Composable
internal fun LoanTransactionsContentPreview() {
    KptTheme {
        LoanTransactionsContent(
            transactions = ScreenState.Content(
                listOf(
                    previewTransaction(1, TransactionType.DISBURSEMENT, "25,000.00", "25,000.00"),
                    previewTransaction(2, TransactionType.REPAYMENT, "2,450.00", "22,550.00"),
                    previewTransaction(3, TransactionType.REPAYMENT, "500.00", "22,550.00", reversed = true),
                ),
            ),
            onAction = {},
        )
    }
}
