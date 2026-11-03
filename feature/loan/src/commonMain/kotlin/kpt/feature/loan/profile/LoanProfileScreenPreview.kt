/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.profile

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanAccountSummary
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.loanWithAssociations.SavingAccountCurrency
import kpt.feature.loan.profile.components.LoanProfileActionItem
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screen is not
 * previewed; its stateless `*Content` half is. Values are server content, not app copy —
 * `// i18n:skip`.
 */

// i18n:skip — preview fixtures below
private val previewLoan = LoanWithAssociations(
    id = 7,
    accountNo = "000000007",
    clientName = "Nadia Islam",
    loanProductName = "Business Loan",
    loanOfficerName = "Arif Hossain",
    principal = 25_000.0,
    currency = SavingAccountCurrency(code = "BDT", name = "Bangladeshi Taka", decimalPlaces = 2, displaySymbol = "৳"),
    summary = LoanAccountSummary(totalOutstanding = 26_950.0, totalOverdue = 1_200.0),
)

@Preview
@Composable
internal fun LoanProfileContentPreview() {
    KptTheme {
        LoanProfileContent(
            loan = ScreenState.Content(previewLoan),
            status = ScreenState.Content(LoanProfileStatus.ACTIVE),
            onAction = {},
            availableDetails = listOf(
                LoanProfileActionItem.RepaymentSchedule,
                LoanProfileActionItem.Transactions,
                LoanProfileActionItem.Charges,
                LoanProfileActionItem.Documents,
            ),
            availableActions = setOf(LoanPrimaryAction.Repayment, LoanPrimaryAction.Transfer),
            accountDetailsAvailable = true,
        )
    }
}

@Preview
@Composable
internal fun LoanProfileContentPendingApprovalPreview() {
    KptTheme {
        LoanProfileContent(
            loan = ScreenState.Content(previewLoan),
            status = ScreenState.Content(LoanProfileStatus.PENDING),
            onAction = {},
            availableActions = setOf(LoanPrimaryAction.Approve),
        )
    }
}
