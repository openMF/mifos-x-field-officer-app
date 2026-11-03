/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.summary

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SavingsSummaryScreenUiTest {

    private fun summary(
        available: SavingsAction = SavingsAction.TRANSACT,
        transactions: List<SavingsTransactionRow> = emptyList(),
    ) = ScreenState.Content(
        SavingsSummary(
            accountNo = "000000055",
            clientName = "Ankit Sharma",
            productName = "Passbook Savings",
            currencyCode = "USD",
            decimalPlaces = 2,
            accountBalance = 1_250.0,
            totalDeposits = 2_000.0,
            totalWithdrawals = 750.0,
            interestEarned = 12.5,
            transactions = transactions,
            available = available,
        ),
    )

    private fun row(id: Int, date: String = "21 Oct 2026", reversed: Boolean = false) =
        SavingsTransactionRow(
            id = id,
            typeLabel = "Deposit",
            date = date,
            amount = 500.0,
            runningBalance = 1_250.0,
            isDeposit = true,
            isWithdrawal = false,
            isReversed = reversed,
        )

    @Test
    fun theFourTotalsRender() = runComposeUiTest {
        setContent {
            KptTheme { SavingsSummaryContent(account = summary(), onAction = {}) }
        }
        onNodeWithTag(SavingsSummaryTestTags.TOTALS).assertIsDisplayed()
    }

    @Test
    fun anActiveAccountOffersDepositAndWithdraw() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsSummaryContent(
                    account = summary(SavingsAction.TRANSACT),
                    onAction = {},
                    canTransact = true,
                )
            }
        }
        onNodeWithTag(SavingsSummaryTestTags.DEPOSIT).assertExists()
        onNodeWithTag(SavingsSummaryTestTags.WITHDRAW).assertExists()
        onNodeWithTag(SavingsSummaryTestTags.APPROVE).assertDoesNotExist()
        onNodeWithTag(SavingsSummaryTestTags.ACTIVATE).assertDoesNotExist()
    }

    @Test
    fun aPendingAccountOffersApproveOnly() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsSummaryContent(
                    account = summary(SavingsAction.APPROVE),
                    onAction = {},
                    canApprove = true,
                )
            }
        }
        onNodeWithTag(SavingsSummaryTestTags.APPROVE).assertExists()
        onNodeWithTag(SavingsSummaryTestTags.DEPOSIT).assertDoesNotExist()
    }

    @Test
    fun anApprovedAccountOffersActivateOnly() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsSummaryContent(
                    account = summary(SavingsAction.ACTIVATE),
                    onAction = {},
                    canActivate = true,
                )
            }
        }
        onNodeWithTag(SavingsSummaryTestTags.ACTIVATE).assertExists()
        onNodeWithTag(SavingsSummaryTestTags.APPROVE).assertDoesNotExist()
    }

    @Test
    fun aClosedAccountShowsStatusTextRatherThanADeadButton() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsSummaryContent(
                    account = summary(SavingsAction.NONE),
                    onAction = {},
                    // Every destination wired, so absence here is the STATUS speaking, not a
                    // missing slice.
                    canTransact = true,
                    canApprove = true,
                    canActivate = true,
                )
            }
        }
        // The original drew an enabled Button labelled "Savings account closed" with `onClick = { }`.
        onNodeWithTag(SavingsSummaryTestTags.CLOSED_NOTICE).assertExists()
        onNodeWithTag(SavingsSummaryTestTags.DEPOSIT).assertDoesNotExist()
        onNodeWithTag(SavingsSummaryTestTags.WITHDRAW).assertDoesNotExist()
        onNodeWithTag(SavingsSummaryTestTags.APPROVE).assertDoesNotExist()
        onNodeWithTag(SavingsSummaryTestTags.ACTIVATE).assertDoesNotExist()
    }

    @Test
    fun anActionWithNoDestinationIsAbsentNotInert() = runComposeUiTest {
        setContent {
            KptTheme {
                // Active account, but the transaction screen has not landed (S5c) — so the two
                // buttons are not drawn rather than drawn dead.
                SavingsSummaryContent(
                    account = summary(SavingsAction.TRANSACT),
                    onAction = {},
                    canTransact = false,
                )
            }
        }
        onNodeWithTag(SavingsSummaryTestTags.ACTIONS).assertExists()
        onNodeWithTag(SavingsSummaryTestTags.DEPOSIT).assertDoesNotExist()
        onNodeWithTag(SavingsSummaryTestTags.WITHDRAW).assertDoesNotExist()
    }

    @Test
    fun theOverflowIsAbsentWhenDocumentsHasNoDestination() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsSummaryContent(account = summary(), onAction = {}, canOpenDocuments = false)
            }
        }
        onNodeWithTag(SavingsSummaryTestTags.OVERFLOW).assertDoesNotExist()
    }

    @Test
    fun documentsDispatchesFromTheOverflow() = runComposeUiTest {
        val sent = mutableListOf<SavingsSummaryAction>()
        setContent {
            KptTheme {
                SavingsSummaryContent(
                    account = summary(),
                    onAction = { sent += it },
                    canOpenDocuments = true,
                )
            }
        }
        onNodeWithTag(SavingsSummaryTestTags.OVERFLOW).performClick()
        onNodeWithTag(SavingsSummaryTestTags.DOCUMENTS).performClick()
        assertEquals(listOf<SavingsSummaryAction>(SavingsSummaryAction.DocumentsClicked), sent)
    }

    @Test
    fun anEmptyAccountSaysSoRatherThanShowingNothing() = runComposeUiTest {
        setContent {
            KptTheme { SavingsSummaryContent(account = summary(), onAction = {}) }
        }
        onNodeWithTag(SavingsSummaryTestTags.NO_TRANSACTIONS).assertExists()
        onNodeWithTag(SavingsSummaryTestTags.TRANSACTIONS).assertDoesNotExist()
    }

    @Test
    fun transactionsRenderAndAreTappable() = runComposeUiTest {
        val sent = mutableListOf<SavingsSummaryAction>()
        setContent {
            KptTheme {
                SavingsSummaryContent(
                    account = summary(transactions = listOf(row(9), row(10))),
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(SavingsSummaryTestTags.TRANSACTIONS).assertExists()
        onNodeWithTag(SavingsSummaryTestTags.transaction(9)).performClick()

        val tapped = sent.single() as SavingsSummaryAction.TransactionClicked
        assertEquals(9, tapped.transaction.id)
    }

    @Test
    fun aRowWithNoDateStillRendersItsAmount() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsSummaryContent(
                    // The case the original crashed on: no usable date. The row must still appear.
                    account = summary(transactions = listOf(row(9, date = ""))),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsSummaryTestTags.transaction(9)).assertExists()
    }

    @Test
    fun theActionRowIsNotOverlaidOnTheTransactions() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsSummaryContent(
                    account = summary(transactions = (1..40).map { row(it) }),
                    onAction = {},
                    canTransact = true,
                )
            }
        }
        // The original stacked the buttons in a `Box(align = BottomStart)` over a `fillMaxSize`
        // column, so on a long transaction list they covered the last rows. The list is weighted
        // here, which keeps the action row reachable AND non-overlapping.
        onNodeWithTag(SavingsSummaryTestTags.DEPOSIT).assertIsDisplayed()
    }

    @Test
    fun theTransactionDialogShowsTheRowsFigures() = runComposeUiTest {
        setContent {
            KptTheme { SavingsTransactionDialog(row = row(9), onDismiss = {}) }
        }
        onNodeWithTag(SavingsSummaryTestTags.DETAIL_DIALOG).assertExists()
    }

    @Test
    fun theDialogHasAVisibleDismiss() = runComposeUiTest {
        var dismissed = false
        setContent {
            KptTheme { SavingsTransactionDialog(row = row(9), onDismiss = { dismissed = true }) }
        }
        // The original's detail card relied on tapping outside, which is neither discoverable nor
        // reachable by a screen reader.
        onNodeWithTag(SavingsSummaryTestTags.DETAIL_DIALOG).assertExists()
        assertEquals(false, dismissed)
    }
}
