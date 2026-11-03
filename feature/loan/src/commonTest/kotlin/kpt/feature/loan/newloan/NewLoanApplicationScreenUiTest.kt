/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.newloan

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.loan.LoanTemplate
import kpt.core.model.objects.account.loan.Currency
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.template.loan.AmortizationTypeOptions
import kpt.core.model.objects.template.loan.FundOptions
import kpt.core.model.objects.template.loan.InterestCalculationPeriodType
import kpt.core.model.objects.template.loan.InterestTypeOptions
import kpt.core.model.objects.template.loan.RepaymentFrequencyTypeOptions
import kpt.core.model.objects.template.loan.TermFrequencyTypeOptions
import kpt.core.model.objects.template.loan.TransactionProcessingStrategyOptions
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class NewLoanApplicationScreenUiTest {

    private val products = ScreenState.Content(
        listOf(LoanProductOption(11, "Individual Grameen"), LoanProductOption(22, "Individual Emergency")),
    )

    private val template = LoanTemplate(
        clientId = 77,
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        interestCalculationPeriodTypeOptions = listOf(InterestCalculationPeriodType(id = 1, value = "Daily")),
        transactionProcessingStrategyOptions = listOf(
            TransactionProcessingStrategyOptions(id = 4, code = "mifos-standard-strategy", name = "Standard"),
        ),
        fundOptions = listOf(FundOptions(id = 9, name = "Donor fund")),
    )

    private fun onStep(step: LoanStep, schedule: RepaymentSchedule? = null) =
        NewLoanApplicationState(
            clientId = 77,
            step = step,
            template = template,
            selections = mapOf(LoanField.PRODUCT to LoanOption(11, "Individual Grameen")),
            numbers = LoanField.requiredPositive.associateWith { "12" },
            schedule = schedule,
        )

    private fun schedule() = RepaymentSchedule(
        currency = Currency(code = "USD", decimalPlaces = 2),
        totalPrincipalExpected = 36_000.0,
        totalInterestCharged = 4_000.0,
        totalRepaymentExpected = 40_000.0,
    )

    @Test
    fun theHeaderNamesTheStepAndIsPresentOnEveryOne() = runComposeUiTest {
        LoanStep.entries.forEach { step ->
            setContent {
                KptTheme {
                    NewLoanApplicationContent(onStep(step, schedule()), products, onAction = {})
                }
            }
            // A bare progress bar does not say where you are; the header names the step.
            onNodeWithTag(NewLoanApplicationTestTags.STEP_HEADER).assertIsDisplayed()
        }
    }

    @Test
    fun step1ShowsOnlyTheProductUntilATemplateLands() = runComposeUiTest {
        setContent {
            KptTheme {
                NewLoanApplicationContent(
                    state = NewLoanApplicationState(clientId = 77),
                    products = products,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(NewLoanApplicationTestTags.PRODUCT).assertIsDisplayed()
        // Every other field's options and defaults come from the product's template.
        onNodeWithTag(NewLoanApplicationTestTags.EXTERNAL_ID).assertDoesNotExist()
        onNodeWithTag(NewLoanApplicationTestTags.SUBMITTED_ON).assertDoesNotExist()
    }

    @Test
    fun step1ShowsTheReferenceFieldsTheTemplateOffers() = runComposeUiTest {
        setContent {
            KptTheme { NewLoanApplicationContent(onStep(LoanStep.DETAILS), products, onAction = {}) }
        }
        onNodeWithTag(NewLoanApplicationTestTags.SUBMITTED_ON).assertExists()
        onNodeWithTag(NewLoanApplicationTestTags.DISBURSEMENT_DATE).assertExists()
        onNodeWithTag(NewLoanApplicationTestTags.EXTERNAL_ID).assertExists()
        // The fixture offers a FUND but no officer, purpose or linked savings, so only Fund draws.
        onNodeWithTag(NewLoanApplicationTestTags.dropdown(LoanField.FUND)).assertExists()
        onNodeWithTag(NewLoanApplicationTestTags.dropdown(LoanField.LOAN_OFFICER)).assertDoesNotExist()
        onNodeWithTag(NewLoanApplicationTestTags.dropdown(LoanField.LOAN_PURPOSE)).assertDoesNotExist()
        onNodeWithTag(NewLoanApplicationTestTags.dropdown(LoanField.LINK_SAVINGS)).assertDoesNotExist()
    }

    @Test
    fun step2DrawsTheTermFieldsAndBothFrequencyDropdownsSeparately() = runComposeUiTest {
        setContent {
            KptTheme { NewLoanApplicationContent(onStep(LoanStep.TERMS), products, onAction = {}) }
        }
        LoanField.requiredPositive.forEach {
            onNodeWithTag(NewLoanApplicationTestTags.number(it)).assertExists()
        }
        onNodeWithTag(NewLoanApplicationTestTags.number(LoanField.NOMINAL_INTEREST)).assertExists()

        // TWO dropdowns, not one. The original had a single term-frequency control feeding both
        // the term and the repayment payload fields.
        onNodeWithTag(NewLoanApplicationTestTags.dropdown(LoanField.TERM_FREQUENCY_TYPE)).assertExists()
        onNodeWithTag(NewLoanApplicationTestTags.dropdown(LoanField.REPAYMENT_FREQUENCY_TYPE)).assertExists()

        // The fixture offers no day-of-week or nth-day options, so neither is drawn — and neither
        // payload field is filled from a neighbour's control.
        onNodeWithTag(NewLoanApplicationTestTags.dropdown(LoanField.REPAYMENT_DAY_OF_WEEK)).assertDoesNotExist()
        onNodeWithTag(NewLoanApplicationTestTags.dropdown(LoanField.REPAYMENT_NTH_DAY)).assertDoesNotExist()

        LoanToggle.entries.forEach {
            onNodeWithTag(NewLoanApplicationTestTags.toggle(it)).assertExists()
        }
    }

    @Test
    fun step3RendersTheScheduleTheServerCalculated() = runComposeUiTest {
        setContent {
            KptTheme {
                NewLoanApplicationContent(onStep(LoanStep.SCHEDULE, schedule()), products, onAction = {})
            }
        }
        onNodeWithTag(NewLoanApplicationTestTags.SCHEDULE_LOADING).assertDoesNotExist()
        onNodeWithTag(NewLoanApplicationTestTags.SCHEDULE_ERROR).assertDoesNotExist()
    }

    @Test
    fun step3ShowsTheServersOwnReasonWhenItRefusesTheTerms() = runComposeUiTest {
        setContent {
            KptTheme {
                NewLoanApplicationContent(
                    state = onStep(LoanStep.SCHEDULE).copy(
                        scheduleError = ScheduleError.Rejected("repayment every must be > 0"),
                    ),
                    products = products,
                    onAction = {},
                )
            }
        }
        // Beside the wizard, not instead of it: the officer needs the reason AND the form.
        onNodeWithTag(NewLoanApplicationTestTags.SCHEDULE_ERROR).assertExists()
        onNodeWithTag(NewLoanApplicationTestTags.STEP_HEADER).assertIsDisplayed()
        onNodeWithTag(NewLoanApplicationTestTags.NEXT).assertExists()
    }

    @Test
    fun step4SummarisesTheSchedulesOwnFigures() = runComposeUiTest {
        setContent {
            KptTheme {
                NewLoanApplicationContent(onStep(LoanStep.PREVIEW, schedule()), products, onAction = {})
            }
        }
        onNodeWithTag(NewLoanApplicationTestTags.SUMMARY).assertExists()
    }

    @Test
    fun step4WithoutAScheduleOffersRecalculationRatherThanBlankFigures() = runComposeUiTest {
        setContent {
            KptTheme {
                NewLoanApplicationContent(onStep(LoanStep.PREVIEW, schedule = null), products, onAction = {})
            }
        }
        // Reachable by walking back, editing a term and forward again — the edit clears the
        // preview, so there is nothing to summarise until it is recalculated.
        onNodeWithTag(NewLoanApplicationTestTags.SUMMARY).assertDoesNotExist()
        onNodeWithTag(NewLoanApplicationTestTags.SCHEDULE_ERROR).assertExists()
    }

    @Test
    fun nextBecomesSubmitOnlyOnTheLastStep() = runComposeUiTest {
        val sent = mutableListOf<NewLoanApplicationAction>()
        setContent {
            KptTheme {
                NewLoanApplicationContent(onStep(LoanStep.TERMS), products, onAction = { sent += it })
            }
        }
        onNodeWithTag(NewLoanApplicationTestTags.NEXT).assertIsDisplayed().performClick()
        assertEquals(listOf<NewLoanApplicationAction>(NewLoanApplicationAction.OnNext), sent)
    }

    @Test
    fun theLastStepSubmits() = runComposeUiTest {
        val sent = mutableListOf<NewLoanApplicationAction>()
        setContent {
            KptTheme {
                NewLoanApplicationContent(
                    onStep(LoanStep.PREVIEW, schedule()),
                    products,
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(NewLoanApplicationTestTags.NEXT).assertIsDisplayed().performClick()
        assertEquals(listOf<NewLoanApplicationAction>(NewLoanApplicationAction.OnSubmit), sent)
    }

    @Test
    fun theTopBarLeavesTheWizardWhileTheFooterWalksAStep() = runComposeUiTest {
        val sent = mutableListOf<NewLoanApplicationAction>()
        setContent {
            KptTheme {
                NewLoanApplicationContent(onStep(LoanStep.TERMS), products, onAction = { sent += it })
            }
        }
        onNodeWithTag(NewLoanApplicationTestTags.BACK).assertIsDisplayed().performClick()

        // Two DIFFERENT actions. The original wired both to one callback, so the top bar abandoned
        // a four-page form on what looked like a step-back.
        assertEquals(listOf<NewLoanApplicationAction>(NewLoanApplicationAction.OnBack), sent)
    }

    @Test
    fun nextStaysLiveSoItCanReportWhatIsMissing() = runComposeUiTest {
        val sent = mutableListOf<NewLoanApplicationAction>()
        setContent {
            KptTheme {
                NewLoanApplicationContent(
                    state = onStep(LoanStep.TERMS).copy(numbers = emptyMap()),
                    products = products,
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(NewLoanApplicationTestTags.NEXT).assertIsDisplayed().performClick()
        assertEquals(listOf<NewLoanApplicationAction>(NewLoanApplicationAction.OnNext), sent)
    }

    @Test
    fun nextIsBlockedWhileASubmitIsInFlight() = runComposeUiTest {
        val sent = mutableListOf<NewLoanApplicationAction>()
        setContent {
            KptTheme {
                NewLoanApplicationContent(
                    state = onStep(LoanStep.PREVIEW, schedule()).copy(isSubmitting = true),
                    products = products,
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(NewLoanApplicationTestTags.NEXT).assertIsDisplayed().performClick()
        assertEquals(emptyList<NewLoanApplicationAction>(), sent)
    }

    @Test
    fun theDeadChargesPageIsNotInTheWizard() = runComposeUiTest {
        // `LoansPayload` carries no charges and no collateral, so the original's Charges page and
        // collateral dialogs discarded everything the officer entered. Four steps, not five.
        assertEquals(
            listOf("DETAILS", "TERMS", "SCHEDULE", "PREVIEW"),
            LoanStep.entries.map { it.name },
        )
    }
}
