/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.schedule

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.account.loan.RepaymentScheduleRowData
import kpt.core.model.objects.account.loan.RepaymentScheduleTableData
import kpt.core.model.objects.account.loan.RepaymentScheduleTotalsData
import kotlin.test.Test

/**
 * The schedule is a two-tier grouped table — the one screen in this slice where a correct
 * projection can still render wrong, because the group-header spans are computed from the column
 * widths and nothing but a render catches them drifting apart.
 */
@OptIn(ExperimentalTestApi::class)
class LoanScheduleScreenUiTest {

    private fun row(number: String) = RepaymentScheduleRowData(
        number = number, days = "30", date = "1 Jun 2026", paidDate = "",
        balance = "9,000.00", principal = "1,000.00", interest = "50.00",
        fees = "", penalties = "", due = "1,050.00",
        paid = "0.00", inAdvance = "0.00", late = "0.00", outstanding = "1,050.00",
    )

    private val table = RepaymentScheduleTableData(
        disbursementRow = row("").copy(date = "1 May 2026"),
        rows = listOf(row("1"), row("2")),
        totals = RepaymentScheduleTotalsData(
            principal = "2,000.00", interest = "100.00", fees = "", penalties = "",
            due = "2,100.00", paid = "0.00", inAdvance = "0.00", late = "0.00",
            outstanding = "2,100.00",
        ),
        completeCount = 0,
        overdueCount = 1,
        pendingCount = 2,
    )

    @Test
    fun bothHeaderTiersAndTheTotalsRowRender() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanScheduleContent(
                    schedule = ScreenState.Content(table),
                    onAction = {},
                )
            }
        }

        // Tier 1 — the three group headers that span several columns each.
        onNodeWithText("Loan Amount and Balance").assertIsDisplayed()
        onNodeWithText("Total Cost of Loan").assertIsDisplayed()
        onNodeWithText("Installment Totals").assertIsDisplayed()
        // Tier 2 — a per-column header under them.
        onNodeWithText("Principal Due").assertIsDisplayed()
        // And the totals row, which the original drew below the periods.
        onNodeWithTag(LoanScheduleTestTags.TOTALS).assertIsDisplayed()
        onNodeWithText("Total").assertIsDisplayed()
    }

    @Test
    fun theThreeCountersRenderInTheBottomBar() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanScheduleContent(
                    schedule = ScreenState.Content(table),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanScheduleTestTags.COUNTERS).assertIsDisplayed()
        onNodeWithText("Complete: 0").assertIsDisplayed()
        onNodeWithText("Pending: 2").assertIsDisplayed()
        onNodeWithText("Overdue: 1").assertIsDisplayed()
    }

    @Test
    fun theScreenDrawsItsChromeWhileTheScheduleIsStillLoading() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanScheduleContent(
                    schedule = ScreenState.Loading,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanScheduleTestTags.SCREEN).assertIsDisplayed()
        onNodeWithText("Loan Repayment Schedule").assertIsDisplayed()
    }
}
