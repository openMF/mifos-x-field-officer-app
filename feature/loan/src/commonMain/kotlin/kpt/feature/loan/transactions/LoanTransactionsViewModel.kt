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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.common.utils.DateHelper
import kpt.core.data.loan.LoanRepository

/**
 * A loan's transaction history — ported from
 * `6b66e8a43:feature/loan/loanTransaction/LoanTransactionsViewModel.kt`.
 *
 * Projects the same `loanStream` the profile reads, for the reason in
 * [kpt.feature.loan.schedule.LoanScheduleViewModel]'s header: the transactions are part of
 * `LoanWithAssociations`, so the original's `getLoanTransactions(loanId)` was a second request for
 * data already in hand.
 *
 * ## The row-action bottom sheet is NOT ported
 *
 * The original made every row tappable, opening a sheet offering Undo / View receipts / View
 * journal entries / View details — but its `onActionSelected` handler only dismissed the sheet, so
 * all four did nothing. Under this port's absent-never-inert rule that sheet is four controls that
 * look functional and are not, so the rows are not clickable and no sheet is drawn. The table is
 * read-only, which is what the original actually was. Tracked in CAPABILITY_GAPS as
 * `loan-transaction-row-actions-unimplemented`.
 *
 * ## Reversed transactions are KEPT, and flagged
 *
 * Fineract marks a reversal `manuallyReversed`; the original struck the row through in the error
 * colour and listed it. An officer reconciling an account needs to see that a payment was undone,
 * so the row stays and the flag travels with it.
 */
internal class LoanTransactionsViewModel(
    repository: LoanRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit, LoanTransactionsEvent, LoanTransactionsAction>(Unit) {

    private val loanId: Int = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1

    private val stream = repository.loanStream(loanId, viewModelScope)

    val transactions: StateFlow<ScreenState<List<TransactionRow>>> = stream.state
        .mapContent { loan, _ ->
            val currencyCode = loan.currency?.code
            val maxDigits = loan.currency?.decimalPlaces
            loan.transactions.orEmpty().mapIndexed { index, transaction ->
                TransactionRow(
                    number = (index + 1).toString(),
                    id = transaction.id?.toString() ?: PLACEHOLDER,
                    office = transaction.officeName ?: PLACEHOLDER,
                    // The original hardcoded "-" here too: `externalId` is not on the transaction
                    // model at all, so the column has never carried a value. Kept rather than
                    // dropped — the column is part of the table the officer knows.
                    externalId = PLACEHOLDER,
                    transactionDate = transaction.date
                        .takeIf { it.size >= DATE_PARTS }
                        ?.let { DateHelper.getDateAsString(it) }
                        ?: PLACEHOLDER,
                    transactionType = transaction.type?.value
                        ?.let { TransactionType.fromValue(it) }
                        ?: TransactionType.UNKNOWN,
                    amount = CurrencyFormatter.format(transaction.amount, currencyCode, maxDigits),
                    principal = CurrencyFormatter.format(transaction.principalPortion, currencyCode, maxDigits),
                    interest = CurrencyFormatter.format(transaction.interestPortion, currencyCode, maxDigits),
                    fees = CurrencyFormatter.format(transaction.feeChargesPortion, currencyCode, maxDigits),
                    penalties = CurrencyFormatter.format(transaction.penaltyChargesPortion, currencyCode, maxDigits),
                    loanBalance = CurrencyFormatter.format(
                        transaction.outstandingLoanBalance,
                        currencyCode,
                        maxDigits,
                    ),
                    manuallyReversed = transaction.manuallyReversed == true,
                )
            }
        }
        // A loan with no transactions is EMPTY, not an empty table with headers. The original
        // checked `transactions.isEmpty()` in the screen and rendered its empty state there; this
        // moves the same decision to where the list is produced, so the screen has one branch.
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: LoanTransactionsAction) {
        when (action) {
            LoanTransactionsAction.NavigateBack -> sendEvent(LoanTransactionsEvent.NavigateBack)
            LoanTransactionsAction.OnRetry -> stream.retry()
        }
    }

    private companion object {
        const val LOAN_ID_KEY = "loanId"
        const val STATE_TIMEOUT_MS = 5_000L
        const val PLACEHOLDER = "-"
        const val DATE_PARTS = 3
    }
}

/** One table row, fully formatted. The screen renders strings and never reaches for the model. */
internal data class TransactionRow(
    val number: String,
    val id: String,
    val office: String,
    val externalId: String,
    val transactionDate: String,
    val transactionType: TransactionType,
    val amount: String,
    val principal: String,
    val interest: String,
    val fees: String,
    val penalties: String,
    val loanBalance: String,
    val manuallyReversed: Boolean = false,
)

/**
 * The three transaction kinds the original's table distinguished, plus the catch-all.
 *
 * Fineract has many more (waiver, write-off, recovery, refund, charge payment …); every one of
 * them lands in [UNKNOWN] and renders as "-". That is the original's behaviour and it is kept —
 * which columns a waiver should fill is a product question, logged rather than guessed.
 */
internal enum class TransactionType(val value: String) {
    ACCRUAL("Accrual"),
    DISBURSEMENT("Disbursement"),
    REPAYMENT("Repayment"),
    UNKNOWN("-"),
    ;

    companion object {
        fun fromValue(value: String): TransactionType? = entries.find { it.value == value }
    }
}

internal sealed interface LoanTransactionsEvent {
    data object NavigateBack : LoanTransactionsEvent
}

internal sealed interface LoanTransactionsAction {
    data object NavigateBack : LoanTransactionsAction
    data object OnRetry : LoanTransactionsAction
}
