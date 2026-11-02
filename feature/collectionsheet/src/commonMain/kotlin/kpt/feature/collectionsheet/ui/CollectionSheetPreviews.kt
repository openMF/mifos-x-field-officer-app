/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.collectionsheet.ui

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.collectionsheet.ClientCollectionSheet
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.objects.account.loan.PaymentTypeOptions
import kpt.core.model.objects.collectionsheets.LoanCollectionSheet
import kpt.core.model.office.Office
import kpt.core.model.shared.BulkRepaymentTransactions
import kpt.feature.collectionsheet.ui.details.CollectionSheetDetailsContent
import kpt.feature.collectionsheet.ui.details.CollectionSheetDetailsState
import kpt.feature.collectionsheet.ui.details.PaymentEditor
import kpt.feature.collectionsheet.ui.details.toView
import kpt.feature.collectionsheet.ui.newsheet.Choice
import kpt.feature.collectionsheet.ui.newsheet.NewCollectionSheetContent
import kpt.feature.collectionsheet.ui.newsheet.NewCollectionSheetState
import org.jetbrains.compose.ui.tooling.preview.Preview

// i18n:skip — preview fixtures
private val previewSheet = IndividualCollectionSheet(
    clients = arrayListOf(
        ClientCollectionSheet(
            clientId = 1,
            clientName = "Asha Rahman",
            loans = arrayListOf(
                LoanCollectionSheet(
                    loanId = 11,
                    accountId = "000000011",
                    productShortName = "GL",
                    totalDue = 1200.0,
                    chargesDue = 25.0,
                ),
                LoanCollectionSheet(loanId = 12, accountId = "000000012", productShortName = "EL", totalDue = 450.0),
            ),
        ),
        ClientCollectionSheet(
            clientId = 2,
            clientName = "Bimal Das",
            loans = arrayListOf(
                LoanCollectionSheet(loanId = 21, accountId = "000000021", productShortName = "GL", totalDue = 800.0),
            ),
        ),
    ),
    paymentTypeOptions = arrayListOf(
        PaymentTypeOptions(id = 1, name = "Cash"),
        PaymentTypeOptions(id = 2, name = "Cheque"),
    ),
)

// i18n:skip — preview fixture
private val previewDrafts = mapOf(
    12 to BulkRepaymentTransactions(loanId = 12, transactionAmount = 300.0, paymentTypeId = 1),
)

@Preview
@Composable
internal fun NewCollectionSheetPreview() {
    KptTheme {
        NewCollectionSheetContent(
            // i18n:skip — preview fixture
            state = NewCollectionSheetState(office = Choice(1, "Head Office"), dateMillis = 1_792_756_800_000L),
            // i18n:skip — preview fixture
            offices = ScreenState.Content(listOf(Office(id = 1, name = "Head Office"))),
            staff = emptyList(),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun CollectionSheetDetailsPreview() {
    KptTheme {
        CollectionSheetDetailsContent(
            state = CollectionSheetDetailsState(drafts = previewDrafts),
            // i18n:skip — preview fixture
            sheet = ScreenState.Content(previewSheet.toView("24 October 2026", previewDrafts)),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun CollectionSheetPaymentEditorPreview() {
    // i18n:skip — preview fixture
    val view = previewSheet.toView("24 October 2026", emptyMap())
    KptTheme {
        CollectionSheetDetailsContent(
            state = CollectionSheetDetailsState(editor = PaymentEditor.of(view.rows.first()).copy(paymentTypeId = 1)),
            sheet = ScreenState.Content(view),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun CollectionSheetNothingDuePreview() {
    KptTheme {
        CollectionSheetDetailsContent(state = CollectionSheetDetailsState(), sheet = ScreenState.Empty, onAction = {})
    }
}
