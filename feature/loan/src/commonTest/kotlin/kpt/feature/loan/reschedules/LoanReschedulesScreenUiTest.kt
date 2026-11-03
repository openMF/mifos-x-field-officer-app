/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.reschedules

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

@OptIn(ExperimentalTestApi::class)
class LoanReschedulesScreenUiTest {

    private fun row(id: Int, status: RescheduleStatusCode) = RescheduleRow(
        id = id,
        status = status,
        rescheduleFrom = "1 Apr 2026",
        reason = "Crop failure",
        submittedOn = "14 Mar 2026",
        submittedBy = "r.kumar",
    )

    @Test
    fun aRowRendersItsStatusDatesAndReason() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanReschedulesContent(
                    state = LoanReschedulesState(loanId = 7),
                    history = ScreenState.Content(listOf(row(1, RescheduleStatusCode.PENDING))),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanReschedulesTestTags.LIST).assertIsDisplayed()
        onNodeWithTag(LoanReschedulesTestTags.row(1)).assertIsDisplayed()
        onNodeWithTag(LoanReschedulesTestTags.status(1)).assertIsDisplayed()
        onNodeWithText("Pending Approval").assertIsDisplayed()
        onNodeWithText("Crop failure").assertIsDisplayed()
        onNodeWithText("r.kumar").assertIsDisplayed()
    }

    @Test
    fun approveAndRejectAreDrawnOnAPENDINGRow() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanReschedulesContent(
                    state = LoanReschedulesState(loanId = 7),
                    history = ScreenState.Content(listOf(row(1, RescheduleStatusCode.PENDING))),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanReschedulesTestTags.approve(1)).assertIsEnabled()
        onNodeWithTag(LoanReschedulesTestTags.reject(1)).assertIsEnabled()
    }

    @Test
    fun theyAreABSENTOnADecidedRow() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanReschedulesContent(
                    state = LoanReschedulesState(loanId = 7),
                    history = ScreenState.Content(
                        listOf(
                            row(2, RescheduleStatusCode.APPROVED),
                            row(3, RescheduleStatusCode.REJECTED),
                            row(4, RescheduleStatusCode.UNKNOWN),
                        ),
                    ),
                    onAction = {},
                )
            }
        }
        // Not greyed — there is nothing to do to a reschedule the server has already decided. The
        // original drew both icons on EVERY row and its handlers returned silently for these.
        listOf(2, 3, 4).forEach { id ->
            onNodeWithTag(LoanReschedulesTestTags.approve(id)).assertDoesNotExist()
            onNodeWithTag(LoanReschedulesTestTags.reject(id)).assertDoesNotExist()
        }
    }

    @Test
    fun onlyTheROWBeingActedOnHasItsActionsHeld() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanReschedulesContent(
                    state = LoanReschedulesState(loanId = 7, actingOn = 1),
                    history = ScreenState.Content(
                        listOf(
                            row(1, RescheduleStatusCode.PENDING),
                            row(5, RescheduleStatusCode.PENDING),
                        ),
                    ),
                    onAction = {},
                )
            }
        }
        // And the LIST stays on screen throughout. The original's single `DialogState.Loading`
        // covered both the initial fetch and an action in flight, so approving a row replaced the
        // whole list with a loading dialog and then rebuilt it.
        onNodeWithTag(LoanReschedulesTestTags.approve(1)).assertIsNotEnabled()
        onNodeWithTag(LoanReschedulesTestTags.approve(5)).assertIsEnabled()
        onNodeWithTag(LoanReschedulesTestTags.LIST).assertIsDisplayed()
    }

    @Test
    fun aLoanWithNoReschedulesRendersTheEmptyState() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanReschedulesContent(
                    state = LoanReschedulesState(loanId = 7),
                    history = ScreenState.Empty,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanReschedulesTestTags.EMPTY).assertIsDisplayed()
        onNodeWithTag(LoanReschedulesTestTags.LIST).assertDoesNotExist()
    }

    @Test
    fun theProposeActionIsABSENTWhenNoCallerSuppliesIt() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanReschedulesContent(
                    state = LoanReschedulesState(loanId = 7),
                    history = ScreenState.Content(listOf(row(1, RescheduleStatusCode.PENDING))),
                    onAction = {},
                    canPropose = false,
                )
            }
        }
        // The gating mechanism, independent of whether the propose form is ported — it WAS the
        // reason this was hidden until S4j, and `LoanNavigation` passes a non-null callback now.
        onNodeWithTag(LoanReschedulesTestTags.PROPOSE).assertDoesNotExist()
    }

    @Test
    fun bothConfirmationsRender() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanReschedulesContent(
                    state = LoanReschedulesState(
                        loanId = 7,
                        dialogState = LoanReschedulesState.DialogState.ConfirmApprove(1),
                    ),
                    history = ScreenState.Content(listOf(row(1, RescheduleStatusCode.PENDING))),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanReschedulesTestTags.CONFIRM_APPROVE).assertIsDisplayed()
        onNodeWithText("Approve Reschedule?").assertIsDisplayed()
    }

    @Test
    fun theRejectConfirmationSaysItRecordsAREJECTION() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanReschedulesContent(
                    state = LoanReschedulesState(
                        loanId = 7,
                        dialogState = LoanReschedulesState.DialogState.ConfirmReject(1),
                    ),
                    history = ScreenState.Content(listOf(row(1, RescheduleStatusCode.PENDING))),
                    onAction = {},
                )
            }
        }
        // The copy matters: the original called this "delete", but the server records a rejection
        // with a date and a user. Nothing is removed.
        onNodeWithTag(LoanReschedulesTestTags.CONFIRM_REJECT).assertIsDisplayed()
        onNodeWithText("Reject Reschedule?").assertIsDisplayed()
    }

    @Test
    fun aFailedActionIsADialogOverTheStillVisibleList() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanReschedulesContent(
                    state = LoanReschedulesState(
                        loanId = 7,
                        dialogState = LoanReschedulesState.DialogState.Failed("loan is not active"),
                    ),
                    history = ScreenState.Content(listOf(row(1, RescheduleStatusCode.PENDING))),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanReschedulesTestTags.FAILED_DIALOG).assertIsDisplayed()
        // Fineract's own reason, where the original showed one fixed string per action.
        onNodeWithText("loan is not active").assertIsDisplayed()
        onNodeWithTag(LoanReschedulesTestTags.LIST).assertIsDisplayed()
    }

    @Test
    fun theScreenDrawsItsChromeWhileLoading() = runComposeUiTest {
        setContent {
            KptTheme {
                LoanReschedulesContent(
                    state = LoanReschedulesState(loanId = 7),
                    history = ScreenState.Loading,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(LoanReschedulesTestTags.SCREEN).assertIsDisplayed()
        onNodeWithText("Loan Reschedules").assertIsDisplayed()
    }
}
