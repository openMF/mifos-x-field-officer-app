/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.profile

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanStatus
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.feature.loan.profile.components.LoanProfileActionItem
import kotlin.test.Test

/**
 * The RENDERED proof of "absent, never inert" on this screen — the rule the whole port turns on,
 * and the one thing a ViewModel test cannot show. A row that is drawn but goes nowhere, or a live
 * button that does nothing when tapped, is visible only here.
 */
@OptIn(ExperimentalTestApi::class)
class LoanProfileScreenUiTest {

    private val loan = ScreenState.Content(
        LoanWithAssociations(id = 7, clientName = "Asha", loanProductName = "Group Loan"),
    )

    @Test
    fun onlyThePortedDetailRowsAreDrawn() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanProfileContent(
                    loan = loan,
                    status = ScreenState.Content(LoanProfileStatus.ACTIVE),
                    onAction = {},
                    availableDetails = listOf(
                        LoanProfileActionItem.RepaymentSchedule,
                        LoanProfileActionItem.Transactions,
                        LoanProfileActionItem.Charges,
                        LoanProfileActionItem.Reschedules,
                        LoanProfileActionItem.Documents,
                    ),
                )
            }
        }

        // The five with destinations: two from S4b, Charges from S4f, Reschedules and Documents
        // from S4i. Documents reuses the GENERIC already-ported document list.
        onNodeWithText("Repayment Schedule").assertIsDisplayed()
        onNodeWithText("Transactions").assertIsDisplayed()
        onNodeWithText("Charges").assertIsDisplayed()
        // And none of the eight that have no destination yet — not greyed, not inert: ABSENT.
        onNodeWithText("Standing Instructions").assertDoesNotExist()
        onNodeWithText("Notes").assertDoesNotExist()
        onNodeWithText("Term Variations").assertDoesNotExist()
    }

    @Test
    fun theSectionHeaderIsDrawnEvenWhenNoRowIs() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanProfileContent(
                    loan = loan,
                    status = ScreenState.Content(LoanProfileStatus.ACTIVE),
                    onAction = {},
                )
            }
        }
        // The original draws the header unconditionally; keeping it means the shape of the screen
        // is the original's even while the rows arrive one slice at a time.
        onNodeWithText("ACTIONS & DETAILS").assertIsDisplayed()
    }

    @Test
    fun thePrimaryButtonIsLiveForAPortedActionAndDisabledOtherwise() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanProfileContent(
                    loan = loan,
                    // PENDING → Approve, which landed in S4b.
                    status = ScreenState.Content(LoanProfileStatus.PENDING),
                    onAction = {},
                    availableActions = setOf(LoanPrimaryAction.Approve),
                )
            }
        }
        onNodeWithTag(LoanProfileTestTags.PRIMARY_ACTION).assertIsEnabled()
        onNodeWithText("Approve Account").assertIsDisplayed()
    }

    @Test
    fun anUnportedActionRendersItsRealLabelDisabledRatherThanDoingNothing() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanProfileContent(
                    loan = loan,
                    // ACTIVE → Make Repayment, which has not landed.
                    status = ScreenState.Content(LoanProfileStatus.ACTIVE),
                    onAction = {},
                    availableActions = setOf(LoanPrimaryAction.Approve),
                )
            }
        }
        // Disabled, not hidden: this is the screen's one call to action and the original always
        // draws it. The label still names the action that belongs to this loan's status, which is
        // true and useful; a live button that silently did nothing would not be.
        onNodeWithTag(LoanProfileTestTags.PRIMARY_ACTION).assertIsNotEnabled()
        onNodeWithText("Make Repayment").assertIsDisplayed()
    }

    @Test
    fun theStatusChipAndTheButtonAgreeBecauseBothComeFromOneStatus() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanProfileContent(
                    loan = loan,
                    status = ScreenState.Content(LoanProfileStatus.OVERPAID),
                    onAction = {},
                )
            }
        }
        // The group screen shipped the opposite: its status text and its action button tested the
        // same field differently and could describe different states at once.
        onNodeWithTag(LoanProfileTestTags.STATUS).assertIsDisplayed()
        onNodeWithText("Overpaid").assertIsDisplayed()
        onNodeWithText("Transfer Funds").assertIsDisplayed()
    }

    @Test
    fun aStatusStillLoadingRendersTheUnknownFallbackRatherThanNothing() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanProfileContent(
                    loan = loan,
                    // The loan has landed but its derived status has not yet — the card must still
                    // draw, because the client name and the balances are already known.
                    status = ScreenState.Loading,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanProfileTestTags.CARD).assertIsDisplayed()
        onNodeWithText("Asha").assertIsDisplayed()
        onNodeWithText("Unknown").assertIsDisplayed()
    }

    @Test
    fun anApprovedLoanNamesTheServersStatusRatherThanUnknown() = runComposeUiTest {
        // Approved / waiting-for-disbursal derives to UNKNOWN; the chip shows what the server calls it.
        val approved = LoanWithAssociations(
            id = 7,
            clientName = "Asha",
            status = LoanStatus(value = "Approved", waitingForDisbursal = true),
        )
        setContent {
            KptTheme {
                LoanProfileContent(
                    loan = ScreenState.Content(approved),
                    status = ScreenState.Content(approved.status.toProfileStatus()),
                    onAction = {},
                )
            }
        }
        onNodeWithText("Approved").assertIsDisplayed()
        onNodeWithText("Unknown").assertDoesNotExist()
    }
}
