/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.transaction

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SavingsTransactionScreenUiTest {

    private fun state(
        kind: TransactionKind = TransactionKind.DEPOSIT,
        paymentTypes: List<PaymentTypeOption> = listOf(
            PaymentTypeOption(3, "Cash"),
            PaymentTypeOption(4, "Mobile Money"),
        ),
    ) = SavingsTransactionState(
        accountType = "savingsaccounts",
        accountId = 55,
        kind = kind,
        paymentTypes = paymentTypes,
    )

    @Test
    fun theFormRenders() = runComposeUiTest {
        setContent { KptTheme { SavingsTransactionContent(state(), onAction = {}) } }
        onNodeWithTag(SavingsTransactionTestTags.AMOUNT).assertIsDisplayed()
        onNodeWithTag(SavingsTransactionTestTags.DATE).assertExists()
        onNodeWithTag(SavingsTransactionTestTags.PAYMENT_TYPE).assertExists()
        onNodeWithTag(SavingsTransactionTestTags.NOTE).assertExists()
        onNodeWithTag(SavingsTransactionTestTags.REVIEW).assertExists()
    }

    @Test
    fun thePaymentTypeDropdownIsAbsentWhenTheTenantConfiguresNone() = runComposeUiTest {
        setContent {
            KptTheme { SavingsTransactionContent(state(paymentTypes = emptyList()), onAction = {}) }
        }
        onNodeWithTag(SavingsTransactionTestTags.PAYMENT_TYPE).assertDoesNotExist()
        // And the amount is still submittable — a payment type is optional.
        onNodeWithTag(SavingsTransactionTestTags.REVIEW).assertExists()
    }

    @Test
    fun aFailedTemplateReadIsStatedButNotABlocker() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsTransactionContent(
                    state = state(paymentTypes = emptyList()).copy(templateFailed = true),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsTransactionTestTags.TEMPLATE_NOTICE).assertExists()
        // The write stays available: the cash may already be in the officer's hand.
        onNodeWithTag(SavingsTransactionTestTags.REVIEW).assertExists()
    }

    @Test
    fun theTemplateNoticeIsTheRetry() = runComposeUiTest {
        val sent = mutableListOf<SavingsTransactionAction>()
        setContent {
            KptTheme {
                SavingsTransactionContent(
                    state = state(paymentTypes = emptyList()).copy(templateFailed = true),
                    onAction = { sent += it },
                )
            }
        }
        // A notice with no way to act on it would leave `OnRetryTemplate` undispatched, and a
        // separate retry button would compete with Review on a screen whose primary action moves
        // money.
        onNodeWithTag(SavingsTransactionTestTags.TEMPLATE_NOTICE).performScrollTo().performClick()
        assertEquals(
            listOf<SavingsTransactionAction>(SavingsTransactionAction.OnRetryTemplate),
            sent,
        )
    }

    @Test
    fun anInvalidAmountIsMarkedOnTheFieldItself() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsTransactionContent(
                    state = state().copy(amount = "-5", amountError = AmountError.NOT_POSITIVE),
                    onAction = {},
                )
            }
        }
        // The original showed one of three SNACKBARS and left the field unmarked.
        onNodeWithTag(SavingsTransactionTestTags.AMOUNT).assertIsDisplayed()
    }

    @Test
    fun reviewDispatchesRatherThanSubmittingDirectly() = runComposeUiTest {
        val sent = mutableListOf<SavingsTransactionAction>()
        setContent {
            KptTheme { SavingsTransactionContent(state(), onAction = { sent += it }) }
        }
        onNodeWithTag(SavingsTransactionTestTags.REVIEW).performScrollTo().performClick()
        // Review, not submit: money moves on the NEXT tap.
        assertEquals(listOf<SavingsTransactionAction>(SavingsTransactionAction.OnReview), sent)
    }

    @Test
    fun theReviewSheetConfirmButtonSaysConfirm() = runComposeUiTest {
        val sent = mutableListOf<SavingsTransactionAction>()
        setContent {
            KptTheme {
                SavingsTransactionContent(
                    state = state().copy(amount = "500", isReviewing = true),
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(SavingsTransactionTestTags.REVIEW_SHEET).assertExists()
        // The original labelled this button "Review Transaction" — on the sheet that WAS the
        // review, so it asked the officer to review in order to submit.
        onNodeWithTag(SavingsTransactionTestTags.CONFIRM).performClick()
        assertEquals(listOf<SavingsTransactionAction>(SavingsTransactionAction.OnConfirm), sent)
    }

    @Test
    fun reviewIsBlockedWhileASubmitIsInFlight() = runComposeUiTest {
        val sent = mutableListOf<SavingsTransactionAction>()
        setContent {
            KptTheme {
                SavingsTransactionContent(
                    state = state().copy(isSubmitting = true),
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(SavingsTransactionTestTags.REVIEW).performScrollTo().assertIsNotEnabled()
        assertEquals(emptyList<SavingsTransactionAction>(), sent)
    }

    @Test
    fun theFormSurvivesAFailure() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsTransactionContent(
                    state = state().copy(
                        amount = "500",
                        dialogState = TransactionDialog.Failed(offline = true, msg = null),
                    ),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsTransactionTestTags.STATUS_DIALOG).assertExists()
        // The amount survives — the officer cannot re-derive cash already taken.
        onNodeWithTag(SavingsTransactionTestTags.AMOUNT).assertExists()
    }

    @Test
    fun successShowsTheReference() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsTransactionContent(
                    state = state().copy(dialogState = TransactionDialog.Succeeded(4_200)),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsTransactionTestTags.STATUS_DIALOG).assertExists()
    }

    @Test
    fun bothKindsRenderTheSameForm() = runComposeUiTest {
        TransactionKind.entries.forEach { kind ->
            setContent { KptTheme { SavingsTransactionContent(state(kind), onAction = {}) } }
            onNodeWithTag(SavingsTransactionTestTags.AMOUNT).assertIsDisplayed()
        }
    }
}
