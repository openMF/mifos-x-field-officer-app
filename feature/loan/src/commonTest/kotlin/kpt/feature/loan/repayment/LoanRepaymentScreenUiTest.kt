/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.repayment

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoanRepaymentScreenUiTest {

    private val form = ScreenState.Content(
        LoanRepaymentForm(
            clientName = "Asha",
            loanAccountNumber = "000000007",
            loanProductName = "Group Loan",
            amountInArrears = 250.0,
            amountDue = 1_000.0,
            currencyCode = "INR",
            decimalPlaces = 2,
            paymentTypes = listOf(LoanPaymentType(1, "Cash"), LoanPaymentType(4, "Mobile Money")),
        ),
    )

    private fun filled() = LoanRepaymentState(
        loanId = 7,
        amount = "1000",
        fees = "50",
        paymentTypeId = 1,
        paymentTypeName = "Cash",
    )

    @Test
    fun theHeaderFiguresAndEveryFieldRender() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRepaymentContent(state = filled(), form = form, onAction = {})
            }
        }
        onNodeWithText("Asha").assertIsDisplayed()
        onNodeWithText("Loan in Arrears").assertIsDisplayed()
        onNodeWithText("Loan Amount Due").assertIsDisplayed()
        onNodeWithTag(LoanRepaymentTestTags.DATE).assertIsDisplayed()
        onNodeWithTag(LoanRepaymentTestTags.PAYMENT_TYPE).assertIsDisplayed()
        onNodeWithTag(LoanRepaymentTestTags.AMOUNT).assertIsDisplayed()
        onNodeWithTag(LoanRepaymentTestTags.ADDITIONAL).assertIsDisplayed()
        onNodeWithTag(LoanRepaymentTestTags.FEES).assertIsDisplayed()
        onNodeWithTag(LoanRepaymentTestTags.TOTAL).assertIsDisplayed()
    }

    @Test
    fun reviewIsEnabledOnlyForASubmittableForm() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRepaymentContent(state = filled(), form = form, onAction = {})
            }
        }
        onNodeWithTag(LoanRepaymentTestTags.REVIEW).assertIsEnabled()
    }

    @Test
    fun reviewIsDisabledWithNoPaymentTypeChosen() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRepaymentContent(
                    state = LoanRepaymentState(loanId = 7, amount = "1000"),
                    form = form,
                    onAction = {},
                )
            }
        }
        // The original enabled it on the display STRING being non-blank; this checks the id the
        // request actually needs.
        onNodeWithTag(LoanRepaymentTestTags.REVIEW).assertIsNotEnabled()
    }

    @Test
    fun reviewIsDisabledForAZeroTotal() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRepaymentContent(
                    state = LoanRepaymentState(loanId = 7, paymentTypeId = 1, paymentTypeName = "Cash"),
                    form = form,
                    onAction = {},
                )
            }
        }
        // The original let an all-blank form through — each field read as 0.0 and each was `>= 0`
        // — so a zero-amount repayment could be posted as a real transaction.
        onNodeWithTag(LoanRepaymentTestTags.REVIEW).assertIsNotEnabled()
    }

    @Test
    fun theFormIsNOTDrawnWhenARepaymentIsAlreadyQueued() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRepaymentContent(
                    state = filled(),
                    form = form,
                    onAction = {},
                    alreadyQueued = true,
                )
            }
        }
        // The refusal replaces the form rather than sitting over it — a second capture is not an
        // option the officer should be able to fall back into.
        onNodeWithTag(LoanRepaymentTestTags.ALREADY_QUEUED_DIALOG).assertIsDisplayed()
        onNodeWithTag(LoanRepaymentTestTags.AMOUNT).assertDoesNotExist()
    }

    @Test
    fun theReviewSheetShowsTheSameTotalTheRequestWillSend() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRepaymentContent(
                    state = filled().copy(showReview = true, additionalPayment = "25.5"),
                    form = form,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanRepaymentTestTags.REVIEW_SHEET).assertIsDisplayed()
        onNodeWithText("000000007").assertIsDisplayed()
        // "Cash" appears twice — in the dropdown behind the sheet and in the sheet's own row — so
        // the assertion is on the sheet's total and its pay button rather than on that label.
        // 1000 + 50 + 25.5 — the same `totalOf` the payload sends, not a re-derivation.
        onNodeWithTag(LoanRepaymentTestTags.REVIEW_TOTAL).assertIsDisplayed()
        onNodeWithTag(LoanRepaymentTestTags.PAY_NOW).assertIsDisplayed()
    }

    @Test
    fun theQueuedOutcomeSaysPlainlyThatTheServerHasNotSeenIt() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRepaymentContent(
                    state = filled().copy(dialogState = LoanRepaymentState.DialogState.Queued),
                    form = form,
                    onAction = {},
                )
            }
        }
        // The distinction between "sent" and "saved on this device" is the whole point: the
        // original reported the offline case through its generic failure string.
        onNodeWithTag(LoanRepaymentTestTags.QUEUED_DIALOG).assertIsDisplayed()
        onNodeWithText("Saved to Sync").assertIsDisplayed()
    }

    @Test
    fun aSuccessfulPaymentShowsFineractsTransactionId() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRepaymentContent(
                    state = filled().copy(
                        dialogState = LoanRepaymentState.DialogState.Submitted(transactionId = 9001),
                    ),
                    form = form,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanRepaymentTestTags.SUCCESS_SHEET).assertIsDisplayed()
        onNodeWithText("Transaction ID: 9001").assertIsDisplayed()
    }

    @Test
    fun aMissingTransactionIdRendersADashNotTheWordNull() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRepaymentContent(
                    state = filled().copy(
                        dialogState = LoanRepaymentState.DialogState.Submitted(transactionId = null),
                    ),
                    form = form,
                    onAction = {},
                )
            }
        }
        // The original printed `resourceId.toString()` on a nullable Int.
        onNodeWithText("Transaction ID: null").assertDoesNotExist()
        onNodeWithText("Transaction ID: —").assertIsDisplayed()
    }

    @Test
    fun aFailureIsADialogOverTheStillFilledForm() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRepaymentContent(
                    state = filled().copy(
                        dialogState = LoanRepaymentState.DialogState.Failed("date is before the last transaction"),
                    ),
                    form = form,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanRepaymentTestTags.FAILED_DIALOG).assertIsDisplayed()
        onNodeWithText("date is before the last transaction").assertIsDisplayed()
        // The form is still there behind it, so dismissing returns the officer to their figures.
        onNodeWithTag(LoanRepaymentTestTags.AMOUNT).assertIsDisplayed()
    }
}
