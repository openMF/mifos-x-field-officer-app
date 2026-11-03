/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.loanaccounts

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.loan.LoanAccount
import kpt.core.model.loan.LoanStatus
import kpt.feature.client.awaitResourceText
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ClientLoanAccountsScreenUiTest {

    private val active = LoanAccount(
        id = 1,
        accountNo = "LN0001",
        productName = "Group Loan",
        status = LoanStatus(active = true, value = "Active"),
        originalLoan = 10_000.0,
        amountPaid = 4_000.0,
        loanBalance = 6_000.0,
    )

    private val pending = LoanAccount(
        id = 2,
        accountNo = "LN0002",
        productName = "Group Loan",
        status = LoanStatus(pendingApproval = true, value = "Pending Approval"),
        originalLoan = 5_000.0,
    )

    @Test
    fun theHeaderCountAndTheListRender() = runComposeUiTest {
        setContent {
            KptTheme {
                ClientLoanAccountsContent(
                    state = ClientLoanAccountsState(clientId = 53),
                    accounts = ScreenState.Content(listOf(active, pending)),
                    onAction = {},
                    totalCount = 2,
                )
            }
        }
        onNodeWithTag(ClientLoanAccountsTestTags.LIST).assertIsDisplayed()
        awaitResourceText("Loan Accounts")
        onNodeWithText("Loan Accounts").assertIsDisplayed()
        // A placeholder, where the original concatenated the count with the SAVINGS screen's
        // "item" string.
        onNodeWithTag(ClientLoanAccountsTestTags.COUNT).assertIsDisplayed()
        onNodeWithText("2 item(s)").assertIsDisplayed()
    }

    @Test
    fun theHeaderControlsAreDrawnWhenTheClientHasAccountsEvenIfTheFILTERHidesThemAll() =
        runComposeUiTest {
            setContent {
                KptTheme {
                    ClientLoanAccountsContent(
                        state = ClientLoanAccountsState(
                            clientId = 53,
                            selectedStatus = setOf(LoanStatusFilter.CLOSED),
                        ),
                        accounts = ScreenState.Empty,
                        onAction = {},
                        // The UNFILTERED total. The original hid these controls when the FILTERED
                        // list was empty — so filtering everything out also removed the only way
                        // to clear the filter.
                        totalCount = 2,
                    )
                }
            }
            onNodeWithTag(ClientLoanAccountsTestTags.FILTER_TOGGLE).assertIsDisplayed()
            onNodeWithTag(ClientLoanAccountsTestTags.SEARCH_TOGGLE).assertIsDisplayed()
            // And the "a filter is on" dot, so it is visible WHY the list is empty.
            onNodeWithTag(ClientLoanAccountsTestTags.FILTER_ACTIVE_DOT).assertIsDisplayed()
        }

    @Test
    fun aClientWithNoAccountsAtAllDrawsNoHeaderControls() = runComposeUiTest {
        setContent {
            KptTheme {
                ClientLoanAccountsContent(
                    state = ClientLoanAccountsState(clientId = 53),
                    accounts = ScreenState.Empty,
                    onAction = {},
                    totalCount = 0,
                )
            }
        }
        // Nothing to search and nothing to filter.
        onNodeWithTag(ClientLoanAccountsTestTags.SEARCH_TOGGLE).assertDoesNotExist()
        onNodeWithTag(ClientLoanAccountsTestTags.FILTER_TOGGLE).assertDoesNotExist()
    }

    @Test
    fun theTwoEmptyStatesSayDifferentThings() = runComposeUiTest {
        setContent {
            KptTheme {
                ClientLoanAccountsContent(
                    state = ClientLoanAccountsState(clientId = 53, searchText = "zzz"),
                    accounts = ScreenState.Empty,
                    onAction = {},
                    totalCount = 2,
                    canAddAccount = true,
                )
            }
        }
        // Filtering: "nothing matches", and NO create button — creating an account would not
        // reveal the ones the filter is hiding. The original showed one message for both cases and
        // offered the button in each.
        onNodeWithTag(ClientLoanAccountsTestTags.EMPTY).assertIsDisplayed()
        onNodeWithText("No Loan Account Found").assertDoesNotExist()
    }

    @Test
    fun theUnfilteredEmptyStateOffersToCreateAnAccount() = runComposeUiTest {
        setContent {
            KptTheme {
                ClientLoanAccountsContent(
                    state = ClientLoanAccountsState(clientId = 53),
                    accounts = ScreenState.Empty,
                    onAction = {},
                    totalCount = 0,
                    canAddAccount = true,
                )
            }
        }
        awaitResourceText("No Loan Account Found")
        onNodeWithText("No Loan Account Found").assertIsDisplayed()
    }

    @Test
    fun theAddButtonIsABSENTWhenTheCreateScreenIsNotPorted() = runComposeUiTest {
        setContent {
            KptTheme {
                ClientLoanAccountsContent(
                    state = ClientLoanAccountsState(clientId = 53),
                    accounts = ScreenState.Content(listOf(active)),
                    onAction = {},
                    totalCount = 1,
                    canAddAccount = false,
                )
            }
        }
        // `newLoanAccount` is a ~2,900-line wizard and is not ported, so the "+" is not drawn
        // rather than drawn inert.
        onNodeWithTag(ClientLoanAccountsTestTags.ADD).assertDoesNotExist()
    }

    @Test
    fun theSearchBarAppearsOnlyWhenToggledOn() = runComposeUiTest {
        var action: ClientLoanAccountsAction? = null
        setContent {
            KptTheme {
                ClientLoanAccountsContent(
                    state = ClientLoanAccountsState(clientId = 53),
                    accounts = ScreenState.Content(listOf(active)),
                    onAction = { action = it },
                    totalCount = 1,
                )
            }
        }
        onNodeWithTag(ClientLoanAccountsTestTags.SEARCH_BAR).assertDoesNotExist()
        onNodeWithTag(ClientLoanAccountsTestTags.SEARCH_TOGGLE).performClick()
        assertEquals(ClientLoanAccountsAction.ToggleSearch, action)
    }

    @Test
    fun theFilterSheetRendersAllFourStatusBoxes() = runComposeUiTest {
        setContent {
            KptTheme {
                ClientLoanAccountsContent(
                    state = ClientLoanAccountsState(clientId = 53, isFilterDialogOpen = true),
                    accounts = ScreenState.Content(listOf(active)),
                    onAction = {},
                    totalCount = 1,
                )
            }
        }
        onNodeWithTag(ClientLoanAccountsTestTags.FILTER_SHEET).assertIsDisplayed()
        LoanStatusFilter.entries.forEach { status ->
            onNodeWithTag(ClientLoanAccountsTestTags.filterBox(status)).assertExists()
        }
        onNodeWithTag(ClientLoanAccountsTestTags.FILTER_CLEAR).assertIsDisplayed()
        onNodeWithTag(ClientLoanAccountsTestTags.FILTER_APPLY).assertIsDisplayed()
    }

    @Test
    fun aPendingLoansAmountsReadNotAvailableWithoutACurrencySymbolGluedOn() = runComposeUiTest {
        setContent {
            KptTheme {
                ClientLoanAccountsContent(
                    state = ClientLoanAccountsState(clientId = 53),
                    accounts = ScreenState.Content(listOf(pending)),
                    onAction = {},
                    totalCount = 1,
                )
            }
        }
        // The original built this as `symbol + (if (pending) "N/A" else amount)` — producing
        // "₹Not Available", a currency symbol glued to the words. The symbol belongs to an amount.
        awaitResourceText("Not Available")
        onNodeWithText("Not Available").assertIsDisplayed()
    }
}
