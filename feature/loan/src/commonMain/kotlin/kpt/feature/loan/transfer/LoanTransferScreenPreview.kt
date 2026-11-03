/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.transfer

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.account.loan.transfer.AccountOption
import kpt.core.model.objects.account.loan.transfer.AccountTypeOption
import kpt.core.model.objects.account.loan.transfer.ClientOption
import kpt.core.model.objects.account.loan.transfer.OfficeOption
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screen is not
 * previewed; its stateless `*Content` half is. Values are server content, not app copy —
 * `// i18n:skip`.
 */

// i18n:skip — preview fixtures below
private val previewOffice = OfficeOption(id = 1, name = "Head Office")
private val previewClient = ClientOption(
    id = 4,
    displayName = "Nusrat Jahan Chowdhury",
    officeId = 1,
    officeName = "Head Office",
)
private val previewAccountType = AccountTypeOption(id = 2, code = "accountType.savings", value = "Savings Account")
private val previewAccount = AccountOption(
    id = 9,
    accountNo = "000000009",
    clientId = 4,
    clientName = "Nusrat Jahan Chowdhury",
    productName = "Voluntary Savings",
)

@Preview
@Composable
internal fun LoanTransferContentPreview() {
    KptTheme {
        LoanTransferContent(
            state = LoanTransferState(
                fromLoanId = 7, fromClientId = 1, fromOfficeId = 1, fromAccountNumber = "000000007", currency = "BDT",
                office = previewOffice, client = previewClient, accountType = previewAccountType,
                account = previewAccount,
                amount = "1000", description = "Overpayment refund",
                offices = listOf(previewOffice), clients = listOf(previewClient),
                accountTypes = listOf(previewAccountType), accounts = listOf(previewAccount),
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun LoanTransferContentTemplateLoadingPreview() {
    KptTheme {
        LoanTransferContent(
            state = LoanTransferState(fromLoanId = 7, fromAccountNumber = "000000007", isLoadingTemplate = true),
            onAction = {},
        )
    }
}
