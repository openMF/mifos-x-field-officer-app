/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.data.testing.ExperimentalDataTestingApi::class)

package kpt.feature.savings.summary

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeSavingsAccountRepository
import kpt.core.model.savings.SavingAccountCurrency
import kpt.core.model.savings.SavingsAccountStatus
import kpt.core.model.savings.SavingsAccountSummary
import kpt.core.model.savings.SavingsAccountTransaction
import kpt.core.model.savings.SavingsAccountWithAssociations
import kpt.core.model.savings.SavingsTransactionDate
import kpt.core.model.savings.SavingsTransactionType
import kpt.feature.savings.transaction.TransactionKind
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The status predicates are this screen's logic, and the original dereferenced the nullable
 * `active` with `!!` at four sites — all evaluated during composition, so the account an officer
 * opens the screen to activate crashed it.
 */
class SavingsSummaryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun account(
        status: SavingsAccountStatus? = SavingsAccountStatus(active = true),
        transactions: List<SavingsAccountTransaction> = emptyList(),
    ) = SavingsAccountWithAssociations(
        id = 55,
        accountNo = "000000055",
        clientName = "Ankit Sharma",
        savingsProductName = "Passbook Savings",
        status = status,
        currency = SavingAccountCurrency(code = "USD", decimalPlaces = 2),
        summary = SavingsAccountSummary(
            accountBalance = 1_250.0,
            totalDeposits = 2_000.0,
            totalWithdrawals = 750.0,
            totalInterestEarned = 12.5,
        ),
        transactions = transactions,
    )

    private fun viewModel(
        repo: FakeSavingsAccountRepository,
        accountType: String = "savingsaccounts",
    ) = SavingsSummaryViewModel(
        repository = repo,
        savedStateHandle = SavedStateHandle(
            mapOf("accountType" to accountType, "accountId" to 55),
        ),
    )

    private fun repo(account: SavingsAccountWithAssociations) =
        FakeSavingsAccountRepository(account = MutableStateFlow(ScreenState.Content(account)))

    @Test
    fun `the account is addressed by type AND id`() = runTest(dispatcher) {
        val repo = repo(account())
        viewModel(repo, accountType = "recurringdepositaccounts").account.test {
            awaitItem()
            // Both route arguments reach the stream. The same numeric id under a different type is
            // a different account, so dropping the type would quietly fetch the wrong one.
            assertEquals(listOf("recurringdepositaccounts" to 55), repo.requestedKeys)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a never-activated account offers ACTIVATE instead of crashing`() = runTest(dispatcher) {
        // `active` omitted entirely — what Fineract sends for an approved-but-not-active account,
        // and what the original's `!status?.active!!` threw on at four sites in the composition.
        val repo = repo(account(status = SavingsAccountStatus(approved = true)))

        viewModel(repo).account.test {
            awaitItem()
            val summary = assertIs<ScreenState.Content<SavingsSummary>>(awaitItem()).data
            assertEquals(SavingsAction.ACTIVATE, summary.available)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a status with every flag null offers nothing rather than crashing`() = runTest(dispatcher) {
        val repo = repo(account(status = SavingsAccountStatus()))

        viewModel(repo).account.test {
            awaitItem()
            val summary = assertIs<ScreenState.Content<SavingsSummary>>(awaitItem()).data
            assertEquals(SavingsAction.NONE, summary.available)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pending-approval wins over every other flag`() = runTest(dispatcher) {
        // Fineract can send `approved` alongside `submittedAndPendingApproval` during a transition;
        // the original's order is kept, because approving is the step the officer must take.
        val repo = repo(
            account(
                status = SavingsAccountStatus(
                    submittedAndPendingApproval = true,
                    approved = true,
                ),
            ),
        )

        viewModel(repo).account.test {
            awaitItem()
            assertEquals(
                SavingsAction.APPROVE,
                assertIs<ScreenState.Content<SavingsSummary>>(awaitItem()).data.available,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a closed account offers nothing, not a button that does nothing`() = runTest(dispatcher) {
        // The original drew an enabled Button labelled "Savings account closed" with `onClick = { }`.
        val repo = repo(account(status = SavingsAccountStatus(closed = true, active = true)))

        viewModel(repo).account.test {
            awaitItem()
            assertEquals(
                SavingsAction.NONE,
                assertIs<ScreenState.Content<SavingsSummary>>(awaitItem()).data.available,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a rejected or withdrawn account offers nothing`() = runTest(dispatcher) {
        listOf(
            SavingsAccountStatus(rejected = true),
            SavingsAccountStatus(withdrawnByApplicant = true),
        ).forEach { status ->
            val repo = repo(account(status = status))
            viewModel(repo).account.test {
                awaitItem()
                assertEquals(
                    SavingsAction.NONE,
                    assertIs<ScreenState.Content<SavingsSummary>>(awaitItem()).data.available,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `an active account can transact`() = runTest(dispatcher) {
        viewModel(repo(account())).account.test {
            awaitItem()
            assertEquals(
                SavingsAction.TRANSACT,
                assertIs<ScreenState.Content<SavingsSummary>>(awaitItem()).data.available,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a transaction date comes from the savings-specific field`() = runTest(dispatcher) {
        val repo = repo(
            account(
                transactions = listOf(
                    SavingsAccountTransaction(
                        id = 9,
                        // What the savings endpoint actually sends — and it leaves `date` EMPTY,
                        // which is this entity's default and what the original cast to
                        // `List<Int>` before indexing `[2]`.
                        savingsTransactionDate = SavingsTransactionDate(
                            year = 2026,
                            month = 10,
                            day = 21,
                        ),
                        amount = 500.0,
                        transactionType = SavingsTransactionType(deposit = true, value = "Deposit"),
                    ),
                ),
            ),
        )

        viewModel(repo).account.test {
            awaitItem()
            val row = assertIs<ScreenState.Content<SavingsSummary>>(awaitItem())
                .data.transactions.single()
            assertTrue(row.date.isNotBlank(), "expected a formatted date, got '${row.date}'")
            assertTrue(row.isDeposit)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a transaction with no usable date renders blank, not a crash`() = runTest(dispatcher) {
        val repo = repo(
            account(
                transactions = listOf(
                    // Neither source populated — the row still has an amount the officer needs.
                    SavingsAccountTransaction(id = 9, amount = 500.0),
                    // A PARTIAL array: the original's cast passed and the row shifted by a position.
                    SavingsAccountTransaction(id = 10, amount = 20.0, date = listOf(2026, null)),
                ),
            ),
        )

        viewModel(repo).account.test {
            awaitItem()
            val rows = assertIs<ScreenState.Content<SavingsSummary>>(awaitItem()).data.transactions
            assertEquals(listOf("", ""), rows.map { it.date })
            assertEquals(listOf(500.0, 20.0), rows.map { it.amount })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `every figure is formatted with the ACCOUNT currency`() = runTest(dispatcher) {
        viewModel(repo(account())).account.test {
            awaitItem()
            val summary = assertIs<ScreenState.Content<SavingsSummary>>(awaitItem()).data
            // One currency for the four totals AND the transaction rows. The original read
            // `summary.currency` for the totals and the account's for the rows.
            assertEquals("USD", summary.currencyCode)
            assertEquals(2, summary.decimalPlaces)
            assertEquals(1_250.0, summary.accountBalance)
            assertEquals(12.5, summary.interestEarned)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a withdrawal and a reversal are each flagged`() = runTest(dispatcher) {
        val repo = repo(
            account(
                transactions = listOf(
                    SavingsAccountTransaction(
                        id = 1,
                        amount = 100.0,
                        transactionType = SavingsTransactionType(withdrawal = true),
                    ),
                    SavingsAccountTransaction(
                        id = 2,
                        amount = 100.0,
                        reversed = true,
                        transactionType = SavingsTransactionType(deposit = true),
                    ),
                ),
            ),
        )

        viewModel(repo).account.test {
            awaitItem()
            val rows = assertIs<ScreenState.Content<SavingsSummary>>(awaitItem()).data.transactions
            assertEquals(listOf(true, false), rows.map { it.isWithdrawal })
            assertEquals(listOf(false, true), rows.map { it.isReversed })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deposit and withdraw carry the account type the route arrived with`() = runTest(dispatcher) {
        val vm = viewModel(repo(account()), accountType = "recurringdepositaccounts")

        vm.eventFlow.test {
            vm.trySendAction(SavingsSummaryAction.DepositClicked)
            val deposit = assertIs<SavingsSummaryEvent.OpenTransaction>(awaitItem())
            assertEquals("recurringdepositaccounts", deposit.accountType)
            assertEquals(TransactionKind.DEPOSIT, deposit.kind)

            vm.trySendAction(SavingsSummaryAction.WithdrawClicked)
            val withdraw = assertIs<SavingsSummaryEvent.OpenTransaction>(awaitItem())
            assertEquals(TransactionKind.WITHDRAWAL, withdraw.kind)
        }
    }

    @Test
    fun `charges opens the charge module for THIS account`() = runTest(dispatcher) {
        val vm = viewModel(repo(account()))

        vm.eventFlow.test {
            vm.trySendAction(SavingsSummaryAction.ChargesClicked)
            assertEquals(SavingsSummaryEvent.OpenCharges(55), awaitItem())
        }
    }

    @Test
    fun `refresh asks the stream for fresh data`() = runTest(dispatcher) {
        val repo = repo(account())
        val vm = viewModel(repo)

        vm.account.test {
            awaitItem()
            awaitItem()
            vm.trySendAction(SavingsSummaryAction.OnRefresh)
            // Dispatched by `RefreshableScreenContent` from BOTH the pull gesture and the
            // error-state retry, which is why there is one member rather than two.
            cancelAndIgnoreRemainingEvents()
        }
    }
}
