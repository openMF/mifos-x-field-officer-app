/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.repayment

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screen is not
 * previewed; its stateless `*Content` half is. Values are server content, not app copy —
 * `// i18n:skip`. Every date is the fixed [PREVIEW_DATE_MILLIS]: several states default theirs to
 * `Clock.System.now()`, which would make the golden change on every run.
 */

/** 2025-10-09 — fixed so a golden never changes because the clock moved. */
private const val PREVIEW_DATE_MILLIS = 1_760_000_000_000L

// i18n:skip — preview fixtures below
private val previewForm = LoanRepaymentForm(
    clientName = "Nadia Islam",
    loanAccountNumber = "000000007",
    loanProductName = "Business Loan",
    amountInArrears = 1_200.0,
    amountDue = 2_450.0,
    currencyCode = "BDT",
    decimalPlaces = 2,
    paymentTypes = listOf(LoanPaymentType(id = 1, name = "Cash")),
)

@Preview
@Composable
internal fun LoanRepaymentContentPreview() {
    KptTheme {
        LoanRepaymentContent(
            state = LoanRepaymentState(
                loanId = 7,
                amount = "2450",
                paymentTypeId = 1,
                paymentTypeName = "Cash",
                date = PREVIEW_DATE_MILLIS,
            ),
            form = ScreenState.Content(previewForm),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun LoanRepaymentContentAlreadyQueuedPreview() {
    // A repayment for this loan is waiting to sync: the form blocks a second one.
    KptTheme {
        LoanRepaymentContent(
            state = LoanRepaymentState(loanId = 7, date = PREVIEW_DATE_MILLIS),
            form = ScreenState.Content(previewForm),
            onAction = {},
            alreadyQueued = true,
        )
    }
}
