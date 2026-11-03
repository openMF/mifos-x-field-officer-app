/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.approval

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.ui.components.ResultStatus
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoanApprovalScreenUiTest {

    private val prefill = ScreenState.Content(
        LoanApprovalPrefill(approvedAmount = 5_000.0, expectedDisbursementOn = listOf(2026, 3, 14)),
    )

    @Test
    fun theFourFieldsAndTheSubmitButtonRender() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanApprovalContent(
                    state = LoanApprovalState(loanId = 7, approvedAmount = "5000", prefilled = true),
                    prefill = prefill,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanApprovalTestTags.APPROVED_ON).assertIsDisplayed()
        onNodeWithTag(LoanApprovalTestTags.DISBURSEMENT_ON).assertIsDisplayed()
        onNodeWithTag(LoanApprovalTestTags.AMOUNT).assertIsDisplayed()
        onNodeWithTag(LoanApprovalTestTags.NOTE).assertIsDisplayed()
        onNodeWithTag(LoanApprovalTestTags.SUBMIT).assertIsEnabled()
    }

    @Test
    fun theTransactionAmountFieldIsNotDrawn() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanApprovalContent(
                    state = LoanApprovalState(loanId = 7, prefilled = true),
                    prefill = prefill,
                    onAction = {},
                )
            }
        }
        // The original rendered it, required it to be valid, and then discarded it — the request
        // has no such field. Absent rather than drawn; see the screen's header.
        onNodeWithText("Transaction Amount").assertDoesNotExist()
    }

    @Test
    fun anUnsetDisbursementDateReadsNotSetRatherThanTheWordNull() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanApprovalContent(
                    state = LoanApprovalState(loanId = 7, disbursementOn = null, prefilled = true),
                    prefill = ScreenState.Content(
                        LoanApprovalPrefill(approvedAmount = null, expectedDisbursementOn = null),
                    ),
                    onAction = {},
                )
            }
        }
        // The original's field was `disbursementDate ?: "null"` — the literal four characters.
        onNodeWithText("null").assertDoesNotExist()
        onNodeWithText("Not set").assertIsDisplayed()
    }

    @Test
    fun anInvalidAmountShowsItsErrorText() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanApprovalContent(
                    state = LoanApprovalState(
                        loanId = 7,
                        approvedAmount = "abc",
                        amountError = true,
                        prefilled = true,
                    ),
                    prefill = prefill,
                    onAction = {},
                )
            }
        }
        // The original passed `error = null` always and its submit branch did nothing, so a bad
        // amount made the button look broken with no explanation anywhere on screen.
        onNodeWithText("Enter a valid amount").assertIsDisplayed()
    }

    @Test
    fun submitIsDisabledWhileTheApprovalIsInFlight() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanApprovalContent(
                    state = LoanApprovalState(
                        loanId = 7,
                        approvedAmount = "5000",
                        isSubmitting = true,
                        prefilled = true,
                    ),
                    prefill = prefill,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanApprovalTestTags.SUBMIT).assertIsNotEnabled()
    }

    @Test
    fun theOutcomeIsADialogRatherThanASnackbarFiredFromComposition() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanApprovalContent(
                    state = LoanApprovalState(
                        loanId = 7,
                        approvedAmount = "5000",
                        prefilled = true,
                        dialogState = LoanApprovalState.DialogState.ShowStatusDialog(
                            ResultStatus.SUCCESS,
                        ),
                    ),
                    prefill = prefill,
                    onAction = {},
                )
            }
        }
        // The original launched its snackbar AND navigated away from inside the composition body,
        // so the message was never readable and both re-fired on every recomposition.
        onNodeWithTag(LoanApprovalTestTags.STATUS_DIALOG).assertIsDisplayed()
        onNodeWithText("Loan Approved").assertIsDisplayed()
    }
}
