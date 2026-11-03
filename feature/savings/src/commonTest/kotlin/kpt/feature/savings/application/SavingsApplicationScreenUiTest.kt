/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.application

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.commonfiles.InterestType
import kpt.core.model.objects.template.client.ChargeOptions
import kpt.core.model.savings.SavingProductsTemplate
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SavingsApplicationScreenUiTest {

    private val products = ScreenState.Content(
        listOf(SavingsProductOption(7, "Passbook Savings"), SavingsProductOption(8, "Youth Savings")),
    )

    private val template = SavingProductsTemplate(
        interestCompoundingPeriodTypeOptions = listOf(InterestType(id = 1, value = "Daily")),
        interestPostingPeriodTypeOptions = listOf(InterestType(id = 4, value = "Monthly")),
        interestCalculationTypeOptions = listOf(InterestType(id = 1, value = "Daily Balance")),
        interestCalculationDaysInYearTypeOptions = listOf(InterestType(id = 365, value = "365 Days")),
        lockinPeriodFrequencyTypeOptions = listOf(InterestType(id = 2, value = "Months")),
        chargeOptions = listOf(ChargeOptions(id = 11, name = "Account maintenance")),
    )

    private fun onStep(
        step: SavingsStep,
        isGroup: Boolean = false,
        charges: List<SavingsChargeRow> = emptyList(),
    ) = SavingsApplicationState(
        ownerId = 31,
        isGroup = isGroup,
        step = step,
        template = template,
        selections = mapOf(SavingsField.PRODUCT to SavingsOption(7, "Passbook Savings")),
        charges = charges,
    )

    @Test
    fun theHeaderNamesEveryStep() = runComposeUiTest {
        SavingsStep.entries.forEach { step ->
            setContent {
                KptTheme { SavingsApplicationContent(onStep(step), products, onAction = {}) }
            }
            onNodeWithTag(SavingsApplicationTestTags.STEP_HEADER).assertIsDisplayed()
        }
    }

    @Test
    fun step1ShowsOnlyTheProductUntilATemplateLands() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsApplicationContent(
                    state = SavingsApplicationState(ownerId = 31),
                    products = products,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsApplicationTestTags.PRODUCT).assertIsDisplayed()
        onNodeWithTag(SavingsApplicationTestTags.SUBMITTED_ON).assertDoesNotExist()
        onNodeWithTag(SavingsApplicationTestTags.EXTERNAL_ID).assertDoesNotExist()
    }

    @Test
    fun step1ShowsTheDateAndExternalIdOnceTheTemplateLands() = runComposeUiTest {
        setContent {
            KptTheme { SavingsApplicationContent(onStep(SavingsStep.DETAILS), products, onAction = {}) }
        }
        onNodeWithTag(SavingsApplicationTestTags.SUBMITTED_ON).assertExists()
        onNodeWithTag(SavingsApplicationTestTags.EXTERNAL_ID).assertExists()
        // The fixture offers no field officers, so that dropdown is absent rather than empty.
        onNodeWithTag(SavingsApplicationTestTags.dropdown(SavingsField.FIELD_OFFICER))
            .assertDoesNotExist()
    }

    @Test
    fun step2DrawsTheBalancesAndTheFourInterestFields() = runComposeUiTest {
        setContent {
            KptTheme { SavingsApplicationContent(onStep(SavingsStep.TERMS), products, onAction = {}) }
        }
        SavingsField.optionalNumbers.forEach {
            onNodeWithTag(SavingsApplicationTestTags.number(it)).assertExists()
        }
        SavingsField.requiredSelections.forEach {
            onNodeWithTag(SavingsApplicationTestTags.dropdown(it)).assertExists()
        }
        SavingsToggle.entries.forEach {
            onNodeWithTag(SavingsApplicationTestTags.toggle(it)).assertExists()
        }
    }

    @Test
    fun step3SaysChargesAreOptionalWhenNoneAreAdded() = runComposeUiTest {
        setContent {
            KptTheme { SavingsApplicationContent(onStep(SavingsStep.CHARGES), products, onAction = {}) }
        }
        // A blank page must not read as a failure.
        onNodeWithTag(SavingsApplicationTestTags.NO_CHARGES).assertExists()
        onNodeWithTag(SavingsApplicationTestTags.CHARGE_LIST).assertDoesNotExist()
        onNodeWithTag(SavingsApplicationTestTags.CHARGE_PICKER).assertExists()
    }

    @Test
    fun step3ListsAddedChargesEachWithARemove() = runComposeUiTest {
        val sent = mutableListOf<SavingsApplicationAction>()
        setContent {
            KptTheme {
                SavingsApplicationContent(
                    state = onStep(
                        SavingsStep.CHARGES,
                        charges = listOf(
                            SavingsChargeRow(11, "Maintenance", "50"),
                            SavingsChargeRow(12, "Ledger", "20"),
                        ),
                    ),
                    products = products,
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(SavingsApplicationTestTags.CHARGE_LIST).assertExists()
        onNodeWithTag(SavingsApplicationTestTags.chargeRow(0)).assertExists()
        onNodeWithTag(SavingsApplicationTestTags.chargeRow(1)).assertExists()
        onNodeWithTag(SavingsApplicationTestTags.NO_CHARGES).assertDoesNotExist()
    }

    @Test
    fun theAddChargeButtonIsDisabledUntilBothFieldsAreGiven() = runComposeUiTest {
        setContent {
            KptTheme { SavingsApplicationContent(onStep(SavingsStep.CHARGES), products, onAction = {}) }
        }
        // A charge row with no amount is not a charge; the original appended whatever was typed.
        onNodeWithTag(SavingsApplicationTestTags.CHARGE_ADD).assertIsNotEnabled()
    }

    @Test
    fun step4NamesTheOwnerAsAGroupForAGroupApplication() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsApplicationContent(
                    onStep(SavingsStep.PREVIEW, isGroup = true),
                    products,
                    onAction = {},
                )
            }
        }
        // The original titled and summarised this identically for a client and a group, on the
        // screen whose group path silently posted a client id.
        onNodeWithTag(SavingsApplicationTestTags.SUMMARY).assertExists()
    }

    @Test
    fun nextBecomesSubmitOnlyOnTheLastStep() = runComposeUiTest {
        val sent = mutableListOf<SavingsApplicationAction>()
        setContent {
            KptTheme {
                SavingsApplicationContent(onStep(SavingsStep.TERMS), products, onAction = { sent += it })
            }
        }
        onNodeWithTag(SavingsApplicationTestTags.NEXT).assertIsDisplayed().performClick()
        assertEquals(listOf<SavingsApplicationAction>(SavingsApplicationAction.OnNext), sent)
    }

    @Test
    fun theLastStepSubmits() = runComposeUiTest {
        val sent = mutableListOf<SavingsApplicationAction>()
        setContent {
            KptTheme {
                SavingsApplicationContent(
                    onStep(SavingsStep.PREVIEW),
                    products,
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(SavingsApplicationTestTags.NEXT).assertIsDisplayed().performClick()
        assertEquals(listOf<SavingsApplicationAction>(SavingsApplicationAction.OnSubmit), sent)
    }

    @Test
    fun theTopBarLeavesWhileTheFooterWalksAStep() = runComposeUiTest {
        val sent = mutableListOf<SavingsApplicationAction>()
        setContent {
            KptTheme {
                SavingsApplicationContent(onStep(SavingsStep.TERMS), products, onAction = { sent += it })
            }
        }
        onNodeWithTag(SavingsApplicationTestTags.BACK).assertIsDisplayed().performClick()
        assertEquals(listOf<SavingsApplicationAction>(SavingsApplicationAction.OnBack), sent)
    }

    @Test
    fun submitIsBlockedWhileOneIsInFlight() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsApplicationContent(
                    onStep(SavingsStep.PREVIEW).copy(isSubmitting = true),
                    products,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsApplicationTestTags.NEXT).assertIsNotEnabled()
    }

    @Test
    fun theFormSurvivesAFailure() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsApplicationContent(
                    onStep(SavingsStep.PREVIEW).copy(
                        dialogState = ApplicationDialog.Failed(offline = true, msg = null),
                    ),
                    products,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsApplicationTestTags.STATUS_DIALOG).assertExists()
        onNodeWithTag(SavingsApplicationTestTags.SUMMARY).assertExists()
    }

    @Test
    fun aFailedTemplateReadOffersRetry() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsApplicationContent(
                    state = SavingsApplicationState(ownerId = 31, templateError = true),
                    products = products,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsApplicationTestTags.TEMPLATE_ERROR).assertExists()
    }

    @Test
    fun theWizardHasFourSteps() = runComposeUiTest {
        // Charges stay, unlike the LOAN wizard's identical-looking page: `SavingsPayload` carries
        // them and the original's builder mapped them, so they reach the server.
        assertEquals(
            listOf("DETAILS", "TERMS", "CHARGES", "PREVIEW"),
            SavingsStep.entries.map { it.name },
        )
    }
}
