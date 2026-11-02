/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.grouploan

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.template.loan.AmortizationTypeOptions
import kpt.core.model.objects.template.loan.GroupLoanTemplate
import kpt.core.model.objects.template.loan.InterestTypeOptions
import kpt.core.model.objects.template.loan.LoanPurposeOptions
import kpt.core.model.objects.template.loan.RepaymentFrequencyTypeOptions
import kpt.core.model.objects.template.loan.TermFrequencyTypeOptions
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class GroupLoanApplicationScreenUiTest {

    private val products = ScreenState.Content(
        listOf(LoanProductOption(11, "Group Grameen"), LoanProductOption(22, "Group Emergency")),
    )

    private val template = GroupLoanTemplate(
        termFrequencyTypeOptions = listOf(TermFrequencyTypeOptions(id = 2, value = "Months")),
        repaymentFrequencyTypeOptions = listOf(RepaymentFrequencyTypeOptions(id = 1, value = "Weeks")),
        amortizationTypeOptions = listOf(AmortizationTypeOptions(id = 1, value = "Equal instalments")),
        interestTypeOptions = listOf(InterestTypeOptions(id = 0, value = "Declining balance")),
        loanPurposeOptions = listOf(LoanPurposeOptions(id = 5, name = "Working capital")),
    )

    private fun filled() = GroupLoanApplicationState(
        groupId = 31,
        product = LoanProductOption(11, "Group Grameen"),
        template = template,
        numbers = GroupLoanField.numeric.associateWith { "12" },
    )

    @Test
    fun noProductMeansNoForm() = runComposeUiTest {
        setContent {
            KptTheme {
                GroupLoanApplicationContent(
                    state = GroupLoanApplicationState(groupId = 31),
                    products = products,
                    onAction = {},
                )
            }
        }
        // The product dropdown is the only control until a product is chosen — every other field's
        // options and defaults come from that product's template.
        onNodeWithTag(GroupLoanApplicationTestTags.PRODUCT).assertIsDisplayed()
        onNodeWithTag(GroupLoanApplicationTestTags.SUBMIT).assertDoesNotExist()
        onNodeWithTag(GroupLoanApplicationTestTags.number(GroupLoanField.PRINCIPAL))
            .assertDoesNotExist()
    }

    @Test
    fun theTemplateDrivesTheWholeForm() = runComposeUiTest {
        setContent { KptTheme { GroupLoanApplicationContent(filled(), products, onAction = {}) } }

        GroupLoanField.numeric.forEach {
            onNodeWithTag(GroupLoanApplicationTestTags.number(it)).assertExists()
        }
        GroupLoanField.requiredSelections.forEach {
            onNodeWithTag(GroupLoanApplicationTestTags.dropdown(it)).assertExists()
        }
        onNodeWithTag(GroupLoanApplicationTestTags.SUBMITTED_ON).assertExists()
        onNodeWithTag(GroupLoanApplicationTestTags.DISBURSEMENT_DATE).assertExists()
        onNodeWithTag(GroupLoanApplicationTestTags.EXACT_DAYS).assertExists()
        onNodeWithTag(GroupLoanApplicationTestTags.SUBMIT).assertExists()
    }

    @Test
    fun aDropdownTheTenantDoesNotConfigureIsAbsent() = runComposeUiTest {
        setContent { KptTheme { GroupLoanApplicationContent(filled(), products, onAction = {}) } }

        // The fixture template offers a loan PURPOSE but no officer and no fund, so those two
        // dropdowns are not drawn. The original rendered each only when its options existed too —
        // that part was already right and is kept.
        onNodeWithTag(GroupLoanApplicationTestTags.dropdown(GroupLoanField.LOAN_PURPOSE)).assertExists()
        onNodeWithTag(GroupLoanApplicationTestTags.dropdown(GroupLoanField.LOAN_OFFICER))
            .assertDoesNotExist()
        onNodeWithTag(GroupLoanApplicationTestTags.dropdown(GroupLoanField.FUND)).assertDoesNotExist()
    }

    @Test
    fun submitStaysLiveSoItCanReportWhatIsMissing() = runComposeUiTest {
        val sent = mutableListOf<GroupLoanApplicationAction>()
        setContent {
            KptTheme {
                GroupLoanApplicationContent(
                    state = filled().copy(numbers = emptyMap()),
                    products = products,
                    onAction = { sent += it },
                )
            }
        }
        // Nothing filled in, and the button still submits: a form this long needs to be TOLD what
        // is missing, not left with a disabled button and no explanation.
        // `performScrollTo` first: this form is fifteen fields long, so Submit is composed but
        // outside the fixed test window, and a click on an off-window node is dropped.
        onNodeWithTag(GroupLoanApplicationTestTags.SUBMIT).performScrollTo().performClick()
        assertEquals(listOf<GroupLoanApplicationAction>(GroupLoanApplicationAction.OnSubmit), sent)
    }

    @Test
    fun submitIsBlockedWhileOneIsInFlight() = runComposeUiTest {
        val sent = mutableListOf<GroupLoanApplicationAction>()
        setContent {
            KptTheme {
                GroupLoanApplicationContent(
                    state = filled().copy(isSubmitting = true),
                    products = products,
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(GroupLoanApplicationTestTags.SUBMIT).performScrollTo().performClick()
        assertEquals(emptyList<GroupLoanApplicationAction>(), sent)
    }

    @Test
    fun aFailedTemplateReadOffersRetryInsteadOfAnEmptyForm() = runComposeUiTest {
        setContent {
            KptTheme {
                GroupLoanApplicationContent(
                    state = GroupLoanApplicationState(groupId = 31, templateError = true),
                    products = products,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(GroupLoanApplicationTestTags.TEMPLATE_ERROR).assertExists()
        onNodeWithTag(GroupLoanApplicationTestTags.SUBMIT).assertDoesNotExist()
    }

    @Test
    fun theDiscardedExternalIdFieldIsGone() = runComposeUiTest {
        setContent { KptTheme { GroupLoanApplicationContent(filled(), products, onAction = {}) } }
        // The original's External Id wrote to a value `GroupLoanPayload` has no field for, so the
        // officer's input was dropped on submit. Absent, not inert — see the screen's comment.
        onNodeWithTag("groupLoan:externalId").assertDoesNotExist()
    }
}
