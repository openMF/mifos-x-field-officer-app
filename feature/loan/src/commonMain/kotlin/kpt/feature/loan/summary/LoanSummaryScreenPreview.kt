/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.summary

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
private val previewSummary = LoanSummaryUi(
    clientName = "Nadia Islam", accountNo = "000000007", loanProductName = "Business Loan",
    loanOfficerName = "Arif Hossain",
    status = LoanSummaryStatus.ACTIVE,
    totalLoan = "29,400.00", amountPaid = "2,450.00", outstanding = "26,950.00", overdue = "0.00",
    disbursedOn = "15 Oct 2025",
    principalDisbursed = "25,000.00", principalPaid = "2,000.00", principalOutstanding = "23,000.00",
    interestCharged = "4,400.00", interestPaid = "450.00", interestOutstanding = "3,950.00",
    feeChargesCharged = "0.00", feeChargesPaid = "0.00", feeChargesOutstanding = "0.00",
    penaltyChargesCharged = "0.00", penaltyChargesPaid = "0.00", penaltyChargesOutstanding = "0.00",
)

@Preview
@Composable
internal fun LoanSummaryContentPreview() {
    KptTheme {
        LoanSummaryContent(
            state = LoanSummaryState(loanId = 7),
            summary = ScreenState.Content(previewSummary),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun LoanSummaryContentPendingPreview() {
    // A pending loan has no repayment figures yet — `showsFigures = false` hides the money table.
    KptTheme {
        LoanSummaryContent(
            state = LoanSummaryState(loanId = 7),
            summary = ScreenState.Content(previewSummary.copy(status = LoanSummaryStatus.PENDING)),
            onAction = {},
        )
    }
}
