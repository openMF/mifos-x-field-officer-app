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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.DateHelper
import kpt.core.data.savings.SavingsAccountRepository
import kpt.core.model.savings.SavingAccountDepositType
import kpt.core.model.savings.SavingsAccountStatus
import kpt.core.model.savings.SavingsAccountTransaction
import kpt.core.model.savings.SavingsAccountWithAssociations
import kpt.feature.savings.transaction.TransactionKind

/**
 * The savings account summary — ported from
 * `6b66e8a43:feature/savings/savingsAccountSummary/` (a 681-line screen + a 44-line ViewModel).
 *
 * First screen of the savings vertical, and the one a search hit opens: `SearchNavigation` routed
 * `CLIENT`, `GROUP` and `LOAN` and left `SAVING` with no destination, so its row was not drawn.
 * Wiring it is what makes this slice reachable, per spec §6a.
 *
 * ## The account is addressed by (type, id), never by id alone
 *
 * [SavingsAccountRepository.savingsAccountStream] takes a Fineract path segment —
 * `"savingsaccounts"`, `"recurringdepositaccounts"` — because the same numeric id means different
 * accounts under different types. The route carries both.
 *
 * ## `status.active` is NULLABLE, and the original dereferenced it with `!!` four times
 *
 * `!status?.active!!` appeared in the button's `onClick` selector, in its label, in its visibility
 * predicate and in the deposit/withdraw visibility predicate. `active` is `Boolean?` and Fineract
 * omits it on an account that has never been activated — which is precisely the account an officer
 * opens this screen to approve or activate. All four sites are evaluated during COMPOSITION, so a
 * pending account did not render a broken button; it crashed the screen.
 *
 * Replaced by [SavingsAction], ONE derivation of what the account can do next, computed from the
 * status with `== true` tests throughout. The screen reads the result; it does not re-derive it,
 * which is what let the original's four copies disagree.
 *
 * ## The original's label function could throw a ClassCastException
 *
 * `getSavingsButtonText`'s `else` branch was `"Button not visible" as StringResource` — a String
 * cast to a resource handle. It survived only because a SECOND function,
 * `savingsButtonVisibilityStatus`, re-derived the same `when` chain and happened to return false in
 * that case. Two independent copies of one decision, one of which is a landmine; [SavingsAction]
 * makes the unreachable case unexpressible instead.
 */
internal class SavingsSummaryViewModel(
    repository: SavingsAccountRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit, SavingsSummaryEvent, SavingsSummaryAction>(Unit) {

    private val accountType: String =
        savedStateHandle.get<String>(ACCOUNT_TYPE_KEY) ?: DEFAULT_ACCOUNT_TYPE
    private val accountId: Int = savedStateHandle.get<Int>(ACCOUNT_ID_KEY) ?: -1

    private val stream = repository.savingsAccountStream(accountType, accountId, viewModelScope)

    val account: StateFlow<ScreenState<SavingsSummary>> = stream.state
        .mapContent { account, _ -> account.toSummary() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: SavingsSummaryAction) {
        when (action) {
            SavingsSummaryAction.NavigateBack -> sendEvent(SavingsSummaryEvent.NavigateBack)
            // ONE member, because `RefreshableScreenContent` routes both the error-state retry and
            // the pull gesture to the same callback — a retry after a failed read IS a refresh.
            // Two members would leave one of them undispatched.
            SavingsSummaryAction.OnRefresh -> stream.refresh(forceFresh = true)

            SavingsSummaryAction.DepositClicked ->
                sendEvent(SavingsSummaryEvent.OpenTransaction(accountType, accountId, TransactionKind.DEPOSIT))

            SavingsSummaryAction.WithdrawClicked ->
                sendEvent(SavingsSummaryEvent.OpenTransaction(accountType, accountId, TransactionKind.WITHDRAWAL))

            SavingsSummaryAction.ApproveClicked ->
                sendEvent(SavingsSummaryEvent.OpenApproval(accountId))

            SavingsSummaryAction.ActivateClicked ->
                sendEvent(SavingsSummaryEvent.OpenActivation(accountId))

            SavingsSummaryAction.DocumentsClicked ->
                sendEvent(SavingsSummaryEvent.OpenDocuments(accountId))

            SavingsSummaryAction.MoreInfoClicked ->
                sendEvent(SavingsSummaryEvent.OpenMoreInfo(accountId))

            SavingsSummaryAction.ChargesClicked ->
                sendEvent(SavingsSummaryEvent.OpenCharges(accountId))

            is SavingsSummaryAction.TransactionClicked ->
                sendEvent(SavingsSummaryEvent.ShowTransaction(action.transaction))
        }
    }

    private companion object {
        const val ACCOUNT_TYPE_KEY = "accountType"
        const val ACCOUNT_ID_KEY = "accountId"

        /**
         * Fineract's path segment for a plain savings account, from the enum that owns the
         * id -> endpoint mapping for all three deposit kinds rather than a second literal.
         */
        val DEFAULT_ACCOUNT_TYPE: String =
            SavingAccountDepositType.ServerTypes.SAVINGS.endpoint
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * What the summary renders — a projection, so the screen never reads the entity's nullable soup.
 *
 * Every figure keeps its currency alongside it: the original formatted the four totals with
 * `summary.currency` but the transaction rows with the ACCOUNT's currency, two fields that can
 * disagree mid-refresh.
 */
internal data class SavingsSummary(
    val accountNo: String,
    val clientName: String?,
    val productName: String?,
    val currencyCode: String,
    val decimalPlaces: Int,
    val accountBalance: Double?,
    val totalDeposits: Double?,
    val totalWithdrawals: Double?,
    val interestEarned: Double?,
    val transactions: List<SavingsTransactionRow>,
    /** The ONE derivation of what this account can do next. */
    val available: SavingsAction,
)

/** One transaction row, already projected, labelled and date-formatted. */
internal data class SavingsTransactionRow(
    val id: Int?,
    val typeLabel: String?,
    /** Display-formatted, or blank when the row carries no usable date. See [transactionDate]. */
    val date: String,
    val amount: Double?,
    val runningBalance: Double?,
    val isDeposit: Boolean,
    val isWithdrawal: Boolean,
    val isReversed: Boolean,
)

/**
 * What the account's status permits — exactly one of these.
 *
 * A sealed enum rather than the original's three independently-derived booleans. The `CLOSED` case
 * is deliberately NOT an action: the original drew an enabled Button labelled "Savings account
 * closed" whose `onClick` was `{ }` — a visible control that does nothing. A closed account's
 * status is TEXT, per this port's absent-never-inert rule.
 */
internal enum class SavingsAction {
    /** Submitted and awaiting approval — approving is the next step. */
    APPROVE,

    /** Approved but not yet active. */
    ACTIVATE,

    /** Active: money can move. */
    TRANSACT,

    /** Closed, or a status this app does not act on (rejected, withdrawn). Nothing to offer. */
    NONE,
}

/**
 * The account's status → the one action it permits.
 *
 * `== true` throughout. The original's `!status?.active!!` threw on the null `active` Fineract sends
 * for a never-activated account, which is the main account this screen exists to act on.
 *
 * Order matters and is the original's: pending-approval wins over everything, then not-yet-active,
 * then active. `closed`/`rejected`/`withdrawnByApplicant` all fall to [SavingsAction.NONE].
 */
internal fun SavingsAccountStatus?.toAvailableAction(): SavingsAction = when {
    this == null -> SavingsAction.NONE
    submittedAndPendingApproval == true -> SavingsAction.APPROVE
    closed == true || rejected == true || withdrawnByApplicant == true -> SavingsAction.NONE
    active == true -> SavingsAction.TRANSACT
    // Approved but not active — the `!active` case, reached only once the three terminal states
    // above are excluded, so a null `active` lands here instead of throwing.
    approved == true -> SavingsAction.ACTIVATE
    else -> SavingsAction.NONE
}

private fun SavingsAccountWithAssociations.toSummary(): SavingsSummary {
    // The ACCOUNT's currency, used for every figure on the screen including the four totals. The
    // original read `summary.currency` for the totals and the account's for the rows.
    val code = currency?.code.orEmpty()
    val digits = currency?.decimalPlaces ?: DEFAULT_DECIMALS

    return SavingsSummary(
        accountNo = accountNo.orEmpty(),
        clientName = clientName,
        productName = savingsProductName,
        currencyCode = code,
        decimalPlaces = digits,
        accountBalance = summary?.accountBalance,
        totalDeposits = summary?.totalDeposits,
        totalWithdrawals = summary?.totalWithdrawals,
        interestEarned = summary?.totalInterestEarned,
        transactions = transactions.map { tx ->
            SavingsTransactionRow(
                id = tx.id,
                typeLabel = tx.transactionType?.value,
                date = transactionDate(tx),
                amount = tx.amount,
                runningBalance = tx.runningBalance,
                isDeposit = tx.transactionType?.deposit == true,
                isWithdrawal = tx.transactionType?.withdrawal == true,
                isReversed = tx.reversed == true,
            )
        },
        available = status.toAvailableAction(),
    )
}

/**
 * A transaction's date, formatted for display — or blank.
 *
 * Two sources, because Fineract populates different ones on different endpoints: the savings
 * endpoint sends a `{transactionId, year, month, day}` object and leaves the generic `date` array
 * EMPTY (which is also this entity's default).
 *
 * The original rendered `DateHelper.getDateAsString(transaction.date as List<Int>)` — an unchecked
 * cast of a `List<Int?>` followed by indexing `[0]`,`[1]`,`[2]`. On the empty list that endpoint
 * actually sends, that is an `IndexOutOfBoundsException` inside the composition, so every row on a
 * savings account crashed the list rather than showing a blank date. The cast additionally hid the
 * nullability: erasure lets it pass, and a null element would have flowed into the date arithmetic.
 *
 * Blank is the right fallback — a transaction with no usable date is still a real transaction whose
 * amount and running balance the officer needs to see.
 */
private fun transactionDate(tx: SavingsAccountTransaction): String {
    tx.savingsTransactionDate?.let { d ->
        val year = d.year
        val month = d.month
        val day = d.day
        if (year != null && month != null && day != null) {
            return DateHelper.getDateAsString(listOf(year, month, day))
        }
    }
    // The generic array, only when it is complete — `filterNotNull` so a partial row is refused
    // rather than silently shifted by one position.
    return tx.date.filterNotNull().takeIf { it.size >= 3 }
        ?.let { DateHelper.getDateAsString(it) }
        .orEmpty()
}

/** Fineract's own default when a currency declares no precision. */
private const val DEFAULT_DECIMALS = 2

internal sealed interface SavingsSummaryEvent {
    data object NavigateBack : SavingsSummaryEvent

    /** Deposit or withdraw on this account — `feature/savings`' transaction screen (S5b). */
    data class OpenTransaction(
        val accountType: String,
        val accountId: Int,
        val kind: TransactionKind,
    ) : SavingsSummaryEvent

    data class OpenApproval(val accountId: Int) : SavingsSummaryEvent
    data class OpenActivation(val accountId: Int) : SavingsSummaryEvent

    /** The generic document list, scoped to this savings account. */
    data class OpenDocuments(val accountId: Int) : SavingsSummaryEvent

    /** The account's datatables — the original's \"More savings account info\". */
    data class OpenMoreInfo(val accountId: Int) : SavingsSummaryEvent

    /** The ONE charge module (`feature/charge`), scoped to this savings account. */
    data class OpenCharges(val accountId: Int) : SavingsSummaryEvent

    /** The transaction detail dialog — the original's `DialogBoxRowItem` sheet. */
    data class ShowTransaction(val transaction: SavingsTransactionRow) : SavingsSummaryEvent
}

internal sealed interface SavingsSummaryAction {
    data object NavigateBack : SavingsSummaryAction
    data object OnRefresh : SavingsSummaryAction
    data object DepositClicked : SavingsSummaryAction
    data object WithdrawClicked : SavingsSummaryAction
    data object ApproveClicked : SavingsSummaryAction
    data object ActivateClicked : SavingsSummaryAction
    data object DocumentsClicked : SavingsSummaryAction
    data object MoreInfoClicked : SavingsSummaryAction
    data object ChargesClicked : SavingsSummaryAction
    data class TransactionClicked(val transaction: SavingsTransactionRow) : SavingsSummaryAction
}
