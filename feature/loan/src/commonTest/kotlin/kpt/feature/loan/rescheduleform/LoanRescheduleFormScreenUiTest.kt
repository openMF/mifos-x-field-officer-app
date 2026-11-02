/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.rescheduleform

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.account.loan.reschedules.RescheduleReasonOption
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoanRescheduleFormScreenUiTest {

    private fun loaded() = LoanRescheduleFormState(
        loanId = 7,
        reasons = listOf(RescheduleReasonOption(id = 3, name = "Crop failure")),
    )

    @Test
    fun theRequiredFieldsAndAllFiveCheckboxesRender() = runComposeUiTest {
        setContent {
            KptTheme { LoanRescheduleFormContent(state = loaded(), onAction = {}) }
        }
        onNodeWithTag(LoanRescheduleFormTestTags.FROM_DATE).assertIsDisplayed()
        onNodeWithTag(LoanRescheduleFormTestTags.REASON).assertIsDisplayed()
        onNodeWithTag(LoanRescheduleFormTestTags.SUBMITTED_ON).assertIsDisplayed()
        onNodeWithTag(LoanRescheduleFormTestTags.COMMENTS).assertIsDisplayed()
        RescheduleOption.entries.forEach { option ->
            onNodeWithTag(LoanRescheduleFormTestTags.option(option)).assertExists()
        }
    }

    @Test
    fun anOptionsFieldsAppearONLYWhenItIsChecked() = runComposeUiTest {
        setContent {
            KptTheme { LoanRescheduleFormContent(state = loaded(), onAction = {}) }
        }
        onNodeWithTag(LoanRescheduleFormTestTags.ADJUSTED_DUE_DATE).assertDoesNotExist()
        RescheduleField.entries
            .filter { it != RescheduleField.REASON && it != RescheduleField.ADJUSTED_DUE_DATE }
            .forEach { onNodeWithTag(LoanRescheduleFormTestTags.number(it)).assertDoesNotExist() }
    }

    @Test
    fun checkingChangeRepaymentDateRevealsItsDateField() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRescheduleFormContent(
                    state = loaded().copy(changeRepaymentDate = true),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanRescheduleFormTestTags.ADJUSTED_DUE_DATE).assertIsDisplayed()
    }

    @Test
    fun checkingGracePeriodsRevealsBOTHItsFields() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRescheduleFormContent(
                    state = loaded().copy(introduceGracePeriods = true),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(
            LoanRescheduleFormTestTags.number(RescheduleField.GRACE_ON_PRINCIPAL),
        ).assertExists()
        onNodeWithTag(
            LoanRescheduleFormTestTags.number(RescheduleField.GRACE_ON_INTEREST),
        ).assertExists()
    }

    @Test
    fun waivePenaltiesRevealsNOField() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRescheduleFormContent(
                    state = loaded().copy(waivePenalties = true),
                    onAction = {},
                )
            }
        }
        // The checkbox IS the value — there is nothing to collect.
        onNodeWithTag(LoanRescheduleFormTestTags.option(RescheduleOption.WAIVE_PENALTIES))
            .assertExists()
        onNodeWithTag(LoanRescheduleFormTestTags.ADJUSTED_DUE_DATE).assertDoesNotExist()
    }

    @Test
    fun everyErrorIsShownONItsOwnField() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRescheduleFormContent(
                    state = loaded().copy(
                        changeRepaymentDate = true,
                        extendRepaymentPeriod = true,
                        errors = setOf(
                            RescheduleField.REASON,
                            RescheduleField.ADJUSTED_DUE_DATE,
                            RescheduleField.EXTRA_TERMS,
                        ),
                    ),
                    onAction = {},
                )
            }
        }
        // The original had ONE error field, for the reason only — the other five had nothing to
        // say, and the submit button was simply disabled.
        onNodeWithText("Select a reschedule reason").assertIsDisplayed()
        onNodeWithText("Pick a date").assertIsDisplayed()
        onNodeWithText("Enter a whole, non-negative number").assertIsDisplayed()
    }

    @Test
    fun submitStaysENABLEDSoTappingItReportsWhatIsMissing() = runComposeUiTest {
        setContent {
            KptTheme { LoanRescheduleFormContent(state = loaded(), onAction = {}) }
        }
        // The original's `isSubmitEnabled` disabled the button whenever any required field was
        // blank, leaving an officer with a dead control and no indication which of six fields it
        // was waiting for.
        onNodeWithTag(LoanRescheduleFormTestTags.SUBMIT).assertIsEnabled()
    }

    @Test
    fun submitIsDisabledOnlyWhileSubmitting() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRescheduleFormContent(
                    state = loaded().copy(isSubmitting = true),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanRescheduleFormTestTags.SUBMIT).assertIsNotEnabled()
    }

    @Test
    fun aFailedReasonReadReplacesTheFormWithARetry() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRescheduleFormContent(
                    state = LoanRescheduleFormState(loanId = 7, reasonsError = true),
                    onAction = {},
                )
            }
        }
        // Without the reasons there is no proposal to make — the reason is required.
        onNodeWithTag(LoanRescheduleFormTestTags.REASONS_ERROR).assertIsDisplayed()
        onNodeWithTag(LoanRescheduleFormTestTags.REASON).assertDoesNotExist()
    }

    @Test
    fun theOutcomeIsADialog() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanRescheduleFormContent(
                    state = loaded().copy(
                        dialogState = LoanRescheduleFormState.DialogState.Failed(
                            "reschedule date should be after the last repayment",
                        ),
                    ),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanRescheduleFormTestTags.STATUS_DIALOG).assertIsDisplayed()
        onNodeWithText("reschedule date should be after the last repayment").assertIsDisplayed()
    }
}
