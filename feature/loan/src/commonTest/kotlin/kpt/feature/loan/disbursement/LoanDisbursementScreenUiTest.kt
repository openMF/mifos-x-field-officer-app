/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.disbursement

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
class LoanDisbursementScreenUiTest {

    private val template = ScreenState.Content(
        LoanDisbursementTemplate(
            netDisbursalAmount = 10_000.0,
            currencyCode = "INR",
            decimalPlaces = 2,
            paymentTypes = listOf(DisbursePaymentType(1, "Cash")),
        ),
    )

    private fun filled() = LoanDisbursementState(
        loanId = 7,
        amount = "10000",
        paymentTypeId = 1,
        paymentTypeName = "Cash",
        prefilled = true,
    )

    @Test
    fun allFourFieldsAndSubmitRender() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanDisbursementContent(state = filled(), template = template, onAction = {})
            }
        }
        onNodeWithTag(LoanDisbursementTestTags.DATE).assertIsDisplayed()
        onNodeWithTag(LoanDisbursementTestTags.AMOUNT).assertIsDisplayed()
        onNodeWithTag(LoanDisbursementTestTags.PAYMENT_TYPE).assertIsDisplayed()
        onNodeWithTag(LoanDisbursementTestTags.NOTE).assertIsDisplayed()
        onNodeWithTag(LoanDisbursementTestTags.SUBMIT).assertIsEnabled()
        onNodeWithText("Disburse Loan").assertIsDisplayed()
    }

    @Test
    fun anInvalidAmountShowsItsErrorText() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanDisbursementContent(
                    state = filled().copy(amount = "abc", amountError = true),
                    template = template,
                    onAction = {},
                )
            }
        }
        // The original passed `error = null` always and had both of its Toasts commented out, so
        // an invalid amount refused to submit with nothing on screen to say why.
        onNodeWithText("Enter a valid amount").assertIsDisplayed()
    }

    @Test
    fun submitIsDisabledWhileTheDisbursementIsInFlight() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanDisbursementContent(
                    state = filled().copy(isSubmitting = true),
                    template = template,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanDisbursementTestTags.SUBMIT).assertIsNotEnabled()
    }

    @Test
    fun theOutcomeIsADialogOverTheStillFilledForm() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanDisbursementContent(
                    state = filled().copy(
                        dialogState = LoanDisbursementState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            msg = "disbursement date cannot be before the approval date",
                        ),
                    ),
                    template = template,
                    onAction = {},
                )
            }
        }
        // The original replaced the whole form with a `MifosSweetError` on failure, and fired a
        // snackbar plus a navigation from the composition body on success.
        onNodeWithTag(LoanDisbursementTestTags.STATUS_DIALOG).assertIsDisplayed()
        onNodeWithText("disbursement date cannot be before the approval date").assertIsDisplayed()
    }

    @Test
    fun theScreenDrawsItsChromeWhileTheTemplateIsStillLoading() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanDisbursementContent(
                    state = LoanDisbursementState(loanId = 7),
                    template = ScreenState.Loading,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanDisbursementTestTags.SCREEN).assertIsDisplayed()
        // And not the form — the amount has nothing to prefill from yet.
        onNodeWithTag(LoanDisbursementTestTags.AMOUNT).assertDoesNotExist()
    }
}
