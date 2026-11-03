/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.transfer

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.account.loan.transfer.AccountOption
import kpt.core.model.objects.account.loan.transfer.AccountTypeOption
import kpt.core.model.objects.account.loan.transfer.ClientOption
import kpt.core.model.objects.account.loan.transfer.OfficeOption
import kpt.core.ui.components.ResultStatus
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoanTransferScreenUiTest {

    private fun loaded() = LoanTransferState(
        fromLoanId = 7,
        fromClientId = 53,
        fromOfficeId = 2,
        fromAccountNumber = "000000007",
        currency = "INR",
        offices = listOf(OfficeOption(id = 3, name = "Alpha Branch")),
    )

    private fun filled() = loaded().copy(
        office = OfficeOption(id = 3, name = "Alpha Branch"),
        client = ClientOption(id = 12, displayName = "Amit"),
        accountType = AccountTypeOption(id = 2, value = "Savings"),
        account = AccountOption(id = 400, accountNo = "SA0001"),
        clients = listOf(ClientOption(id = 12, displayName = "Amit")),
        accountTypes = listOf(AccountTypeOption(id = 2, value = "Savings")),
        accounts = listOf(AccountOption(id = 400, accountNo = "SA0001")),
        amount = "250",
        description = "Overpayment refund",
    )

    @Test
    fun theFromCardAndAllSixInputsRender() = runComposeUiTest {
        setContent {
            KptTheme { LoanTransferContent(state = filled(), onAction = {}) }
        }
        onNodeWithTag(LoanTransferTestTags.FROM_CARD).assertIsDisplayed()
        onNodeWithText("000000007").assertIsDisplayed()
        onNodeWithTag(LoanTransferTestTags.OFFICE).assertIsDisplayed()
        onNodeWithTag(LoanTransferTestTags.CLIENT).assertIsDisplayed()
        onNodeWithTag(LoanTransferTestTags.ACCOUNT_TYPE).assertIsDisplayed()
        onNodeWithTag(LoanTransferTestTags.ACCOUNT).assertIsDisplayed()
        onNodeWithTag(LoanTransferTestTags.AMOUNT).assertIsDisplayed()
        onNodeWithTag(LoanTransferTestTags.DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun aCascadeLevelIsDISABLEDUntilTheLevelAboveIsChosen() = runComposeUiTest {
        setContent {
            KptTheme { LoanTransferContent(state = loaded(), onAction = {}) }
        }
        // The original expressed this only by leaving the list EMPTY — so an un-chooseable
        // dropdown looked identical to one whose office genuinely had no clients.
        onNodeWithTag(LoanTransferTestTags.OFFICE).assertIsEnabled()
        onNodeWithTag(LoanTransferTestTags.CLIENT).assertIsNotEnabled()
        onNodeWithTag(LoanTransferTestTags.ACCOUNT_TYPE).assertIsNotEnabled()
        onNodeWithTag(LoanTransferTestTags.ACCOUNT).assertIsNotEnabled()
    }

    @Test
    fun eachLevelBecomesEnabledInTurn() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransferContent(
                    state = loaded().copy(
                        office = OfficeOption(id = 3, name = "Alpha Branch"),
                        client = ClientOption(id = 12, displayName = "Amit"),
                    ),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanTransferTestTags.CLIENT).assertIsEnabled()
        onNodeWithTag(LoanTransferTestTags.ACCOUNT_TYPE).assertIsEnabled()
        // Still not the account — its type has not been chosen.
        onNodeWithTag(LoanTransferTestTags.ACCOUNT).assertIsNotEnabled()
    }

    @Test
    fun everyFieldErrorIsShownAtOnce() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransferContent(
                    state = loaded().copy(
                        amountError = TransferFieldError.AMOUNT_INVALID,
                        descriptionError = TransferFieldError.DESCRIPTION_REQUIRED,
                        officeError = TransferFieldError.OFFICE_REQUIRED,
                    ),
                    onAction = {},
                )
            }
        }
        onNodeWithText("Enter a valid amount").assertIsDisplayed()
        onNodeWithText("Description cannot be empty").assertIsDisplayed()
        onNodeWithText("Select an office").assertIsDisplayed()
    }

    @Test
    fun theZeroAmountErrorIsDistinctFromTheInvalidOne() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransferContent(
                    state = filled().copy(
                        amount = "0",
                        amountError = TransferFieldError.AMOUNT_NOT_POSITIVE,
                    ),
                    onAction = {},
                )
            }
        }
        onNodeWithText("Transfer amount cannot be zero").assertIsDisplayed()
    }

    @Test
    fun theCascadeSpinnerIsINLINENotAnOverlay() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransferContent(
                    state = filled().copy(isLoadingTemplate = true),
                    onAction = {},
                )
            }
        }
        // Each cascade step reloads the template, so an overlay would flash over the form four
        // times on the way to a transfer. The form stays visible.
        onNodeWithTag(LoanTransferTestTags.LOADING).assertIsDisplayed()
        onNodeWithTag(LoanTransferTestTags.AMOUNT).assertIsDisplayed()
        // And submit is held while a level is reloading.
        onNodeWithTag(LoanTransferTestTags.SUBMIT).assertIsNotEnabled()
    }

    @Test
    fun aFailedFIRSTTemplateReadReplacesTheForm() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransferContent(
                    state = LoanTransferState(fromLoanId = 7, templateError = true),
                    onAction = {},
                )
            }
        }
        // Nothing to fill in, so there is no form to keep.
        onNodeWithTag(LoanTransferTestTags.TEMPLATE_ERROR).assertIsDisplayed()
        onNodeWithTag(LoanTransferTestTags.AMOUNT).assertDoesNotExist()
    }

    @Test
    fun aLaterTemplateFailureKEEPSTheForm() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransferContent(
                    // Offices already loaded, so a later cascade step failing must not discard
                    // everything the officer has chosen.
                    state = filled().copy(templateError = true),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanTransferTestTags.TEMPLATE_ERROR).assertDoesNotExist()
        onNodeWithTag(LoanTransferTestTags.AMOUNT).assertIsDisplayed()
    }

    @Test
    fun submitIsDisabledWhileTheTransferIsInFlight() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransferContent(state = filled().copy(isSubmitting = true), onAction = {})
            }
        }
        onNodeWithTag(LoanTransferTestTags.SUBMIT).assertIsNotEnabled()
    }

    @Test
    fun theOutcomeIsADialogOverTheStillChosenForm() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransferContent(
                    state = filled().copy(
                        dialogState = LoanTransferState.DialogState.Result(
                            status = ResultStatus.FAILURE,
                            msg = "insufficient balance on the source account",
                            retryable = true,
                        ),
                    ),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanTransferTestTags.STATUS_DIALOG).assertIsDisplayed()
        onNodeWithText("insufficient balance on the source account").assertIsDisplayed()
    }
}
