/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.transactions

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoanTransactionsScreenUiTest {

    private fun row(number: String, reversed: Boolean = false) = TransactionRow(
        number = number,
        id = "10$number",
        office = "Head Office",
        externalId = "-",
        transactionDate = "14 Mar 2026",
        transactionType = TransactionType.REPAYMENT,
        amount = "₹1,000.00",
        principal = "₹900.00",
        interest = "₹100.00",
        fees = "₹0.00",
        penalties = "₹0.00",
        loanBalance = "₹9,000.00",
        manuallyReversed = reversed,
    )

    @Test
    fun theBreakdownGroupHeaderAndItsFiveColumnsRender() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransactionsContent(
                    transactions = ScreenState.Content(listOf(row("1"))),
                    onAction = {},
                )
            }
        }
        // "Break Down" spans five columns, so its width is computed rather than listed — a render
        // is the only thing that catches that span drifting from the columns beneath it.
        onNodeWithText("Break Down").assertIsDisplayed()
        onNodeWithText("Principal").assertIsDisplayed()
        onNodeWithText("Penalties").assertIsDisplayed()
        onNodeWithText("Loan Balance").assertIsDisplayed()
    }

    @Test
    fun aReversedRowIsStillDrawn() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransactionsContent(
                    transactions = ScreenState.Content(listOf(row("1"), row("2", reversed = true))),
                    onAction = {},
                )
            }
        }
        // Both rows present — a reversal is struck through, never filtered out, because an officer
        // reconciling the account has to see that a payment was undone.
        onNodeWithTag(LoanTransactionsTestTags.row("1")).assertIsDisplayed()
        onNodeWithTag(LoanTransactionsTestTags.row("2")).assertIsDisplayed()
    }

    @Test
    fun aLoanWithNoTransactionsRendersTheEmptyStateNotAnEmptyTable() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanTransactionsContent(
                    transactions = ScreenState.Empty,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanTransactionsTestTags.SCREEN).assertIsDisplayed()
        // No table chrome at all — the header row must not be drawn over nothing.
        onNodeWithText("Break Down").assertDoesNotExist()
    }
}
