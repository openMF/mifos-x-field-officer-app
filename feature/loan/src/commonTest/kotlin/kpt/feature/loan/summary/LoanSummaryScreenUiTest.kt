/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.summary

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

/**
 * ## `assertExists` for the primary button's LABEL, `assertIsDisplayed` for everything else
 *
 * The button is the last thing in a scrolling column that holds a header card, three figure cards,
 * a four-row overview and a five-row table — so in a fixed-size test window it is composed but
 * below the fold. `assertIsNotEnabled` / `assertIsEnabled` on its test tag answer the question that
 * matters (is this affordance live?) and need no visibility; the label assertion is about WHICH
 * action it names, so existence is the right check. Using `assertIsDisplayed` there made all three
 * button tests fail for a reason that had nothing to do with the button.
 */
@OptIn(ExperimentalTestApi::class)
class LoanSummaryScreenUiTest {

    private fun ui(status: LoanSummaryStatus, blank: Boolean = !status.showsFigures) = LoanSummaryUi(
        clientName = "Asha",
        accountNo = "000000007",
        loanProductName = "Group Loan",
        loanOfficerName = "R. Kumar",
        status = status,
        totalLoan = if (blank) "" else "₹11,000.00",
        amountPaid = if (blank) "" else "₹4,000.00",
        outstanding = if (blank) "" else "₹7,000.00",
        overdue = if (blank) "" else "₹500.00",
        disbursedOn = if (blank) "" else "14 Mar 2026",
        principalDisbursed = if (blank) "" else "₹10,000.00",
        principalPaid = if (blank) "" else "₹3,500.00",
        principalOutstanding = if (blank) "" else "₹6,500.00",
        interestCharged = if (blank) "" else "₹1,000.00",
        interestPaid = if (blank) "" else "₹500.00",
        interestOutstanding = if (blank) "" else "₹500.00",
        feeChargesCharged = "",
        feeChargesPaid = "",
        feeChargesOutstanding = "",
        penaltyChargesCharged = "",
        penaltyChargesPaid = "",
        penaltyChargesOutstanding = "",
    )

    @Test
    fun theHeaderOverviewAndSummaryTableAllRender() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanSummaryContent(
                    state = LoanSummaryState(loanId = 7),
                    summary = ScreenState.Content(ui(LoanSummaryStatus.ACTIVE)),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanSummaryTestTags.HEADER).assertIsDisplayed()
        onNodeWithTag(LoanSummaryTestTags.STATUS_DOT).assertIsDisplayed()
        onNodeWithTag(LoanSummaryTestTags.COPY).assertIsDisplayed()
        onNodeWithTag(LoanSummaryTestTags.OUTSTANDING).assertIsDisplayed()
        onNodeWithTag(LoanSummaryTestTags.OVERVIEW).assertIsDisplayed()
        onNodeWithTag(LoanSummaryTestTags.TABLE).assertIsDisplayed()
        onNodeWithText("Asha").assertIsDisplayed()
        // The account number renders through a PLACEHOLDER, where the original concatenated
        // "Loan ID: #" with the value and so could not be reordered by any translation.
        onNodeWithText("Loan ID: #000000007").assertIsDisplayed()
    }

    @Test
    fun theButtonIsLiveForAPortedActionAndNamesIt() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanSummaryContent(
                    state = LoanSummaryState(loanId = 7),
                    summary = ScreenState.Content(ui(LoanSummaryStatus.ACTIVE)),
                    onAction = {},
                    availableActions = setOf(LoanSummaryAct.MakeRepayment),
                )
            }
        }
        onNodeWithTag(LoanSummaryTestTags.PRIMARY_ACTION).assertIsEnabled()
        onNodeWithText("Make Repayment").assertExists()
    }

    @Test
    fun anUnportedActionIsDisabledWithItsRealLabel() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanSummaryContent(
                    state = LoanSummaryState(loanId = 7),
                    summary = ScreenState.Content(ui(LoanSummaryStatus.AWAITING_DISBURSAL)),
                    onAction = {},
                    availableActions = setOf(LoanSummaryAct.MakeRepayment),
                )
            }
        }
        onNodeWithTag(LoanSummaryTestTags.PRIMARY_ACTION).assertIsNotEnabled()
        onNodeWithText("Disburse Loan").assertExists()
    }

    @Test
    fun aClosedLoansButtonIsDisabledBecauseItHasNoActionAtAll() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanSummaryContent(
                    state = LoanSummaryState(loanId = 7),
                    summary = ScreenState.Content(ui(LoanSummaryStatus.CLOSED, blank = false)),
                    onAction = {},
                    // Even with EVERY action ported, CLOSED stays disabled — the original drew it
                    // enabled only via a separate predicate and gave it an empty `{ }` onClick.
                    availableActions = LoanSummaryAct.entries.toSet(),
                )
            }
        }
        onNodeWithTag(LoanSummaryTestTags.PRIMARY_ACTION).assertIsNotEnabled()
        onNodeWithText("Loan Closed").assertExists()
    }

    @Test
    fun aPendingLoanShowsItsStatusAndOfficerButNoFigures() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanSummaryContent(
                    state = LoanSummaryState(loanId = 7),
                    summary = ScreenState.Content(ui(LoanSummaryStatus.PENDING)),
                    onAction = {},
                )
            }
        }
        // The officer is still shown — assigned before disbursement, and what someone looking at a
        // pending loan needs. The money is not.
        onNodeWithText("R. Kumar").assertIsDisplayed()
        onNodeWithText("₹7,000.00").assertDoesNotExist()
        onNodeWithText("14 Mar 2026").assertDoesNotExist()
    }

    @Test
    fun theOverflowIconIsNotDrawnWhenNoMenuEntryHasADestination() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanSummaryContent(
                    state = LoanSummaryState(loanId = 7),
                    summary = ScreenState.Content(ui(LoanSummaryStatus.ACTIVE)),
                    onAction = {},
                    availableMenuItems = emptyList(),
                )
            }
        }
        // A menu button that opens an empty menu is the same dead control as a dead row.
        onNodeWithTag(LoanSummaryTestTags.OVERFLOW).assertDoesNotExist()
    }

    @Test
    fun theMenuShowsOnlyThePortedEntries() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanSummaryContent(
                    state = LoanSummaryState(loanId = 7, openDropdown = true),
                    summary = ScreenState.Content(ui(LoanSummaryStatus.ACTIVE)),
                    onAction = {},
                    availableMenuItems = listOf(
                        LoanSummaryMenuItem.Transactions,
                        LoanSummaryMenuItem.RepaymentSchedule,
                        LoanSummaryMenuItem.Charges,
                        LoanSummaryMenuItem.Documents,
                    ),
                )
            }
        }
        onNodeWithTag(LoanSummaryTestTags.OVERFLOW).assertIsDisplayed()
        onNodeWithText("Transactions").assertIsDisplayed()
        onNodeWithText("Loan Charges").assertIsDisplayed()
        // `More Loan Info` needs the loan data-table screen, which is not ported — absent, not
        // greyed.
        onNodeWithText("More Loan Info").assertDoesNotExist()
    }

    @Test
    fun theScreenDrawsItsChromeWhileTheLoanIsStillLoading() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanSummaryContent(
                    state = LoanSummaryState(loanId = 7),
                    summary = ScreenState.Loading,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanSummaryTestTags.SCREEN).assertIsDisplayed()
        onNodeWithText("Loan Account Summary").assertIsDisplayed()
    }
}
