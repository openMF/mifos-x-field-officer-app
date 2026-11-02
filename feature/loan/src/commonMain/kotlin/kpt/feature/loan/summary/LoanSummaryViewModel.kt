/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.common.utils.DateHelper
import kpt.core.data.loan.LoanRepository
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanStatus

/**
 * A loan account's SUMMARY — ported from
 * `6b66e8a43:feature/loan/loanAccountSummary/{LoanAccountSummaryViewModel,Screen}.kt`.
 *
 * ## This is the screen the live callers actually open
 *
 * `loanAccountSummary` and `loanAccountProfile` are two different screens over the same loan, and
 * which one a tap opens depends on where the tap is — the §6a caller rule again:
 *
 * | tapped | opens |
 * |---|---|
 * | a loan hit in SEARCH | summary (this screen) |
 * | a loan on GROUP detail | summary |
 * | a loan in `clientLoanAccounts` (the client's loan list) | profile |
 * | a loan on `clientDetails` | summary — but nothing navigates to `clientDetails` |
 *
 * So the profile ([kpt.feature.loan.profile.LoanProfileViewModel]) is reached only through the
 * client's loan LIST, which is not ported. Until this screen landed, the whole `feature/loan`
 * vertical compiled and was registered with **no live caller** — the same dead-code class as a
 * ViewModel without a screen, one level up. Tracked as `loan-vertical-unreachable`.
 *
 * ## The summary figures are BLANKED for a loan that has not disbursed
 *
 * `shouldInflateLoanSummary` — `active || closedObligationsMet || overpaid` — is the original's
 * rule and it is kept: a pending or awaiting-disbursal loan has a `summary` block full of zeroes
 * and projections, and showing "Amount Paid: 0.00" next to "Outstanding: 0.00" on a loan that has
 * never disbursed reads as a paid-off loan. Blank says "not yet", which is true.
 *
 * ## One status derivation, where the original had FOUR
 *
 * The original derived the status FOUR separate times from the same `LoanStatus`:
 * the dot's colour and the dot's accessibility label each had their own `when` (identical, written
 * twice), `getPrimaryAction()` had a third with a different precedence, and `isButtonActive()` a
 * fourth. And the loan PROFILE screen has a fifth with yet another precedence — it tests
 * `pendingApproval` before `active`, where this screen tests `active` first.
 *
 * Here it is derived ONCE into [LoanSummaryStatus], so the dot, its label and the button cannot
 * describe different loans. The precedence kept is THIS screen's (active → pendingApproval →
 * waitingForDisbursal → overpaid → closed), because that is what this screen rendered; the
 * profile's own ordering is unchanged on the profile. That the two disagree is the original's
 * behaviour and a product question, logged rather than unified.
 */
internal class LoanSummaryViewModel(
    repository: LoanRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanSummaryState, LoanSummaryEvent, LoanSummaryAction>(
    LoanSummaryState(loanId = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1),
) {

    private val stream = repository.loanStream(state.loanId, viewModelScope)

    val summary: StateFlow<ScreenState<LoanSummaryUi>> = stream.state
        .mapContent { loan, _ ->
            val status = loan.status.toSummaryStatus()
            // The original's `shouldInflateLoanSummary`, applied by NULLING the summary rather
            // than by hiding each row — so every figure below is blank together or populated
            // together, and no row can be left showing a stale value.
            val figures = loan.summary?.takeIf { status.showsFigures }
            val code = loan.currency?.code
            val places = loan.currency?.decimalPlaces

            // Before disbursal a figure is "—", not blank: the server sends no `summary` then, and a
            // screen of empty cards read as broken (loan 38, device 2026-10-26).
            val absent = if (status.showsFigures) "" else NOT_YET
            fun money(value: Double?) = value?.let { CurrencyFormatter.format(it, code, places) } ?: absent

            LoanSummaryUi(
                clientName = loan.clientName.orEmpty(),
                accountNo = loan.accountNo.orEmpty(),
                loanProductName = loan.loanProductName.orEmpty(),
                loanOfficerName = loan.loanOfficerName?.takeIf { it.isNotBlank() } ?: absent,
                status = status,
                // A pending loan has no repayment total yet, so the card shows what was asked for —
                // and once approved, what was approved. Its label says which (`totalLoanBasis`).
                totalLoan = when (status) {
                    LoanSummaryStatus.PENDING -> money(loan.principal)
                    LoanSummaryStatus.AWAITING_DISBURSAL -> money(loan.approvedPrincipal ?: loan.principal)
                    else -> money(figures?.totalExpectedRepayment)
                },
                amountPaid = money(figures?.totalRepayment),
                outstanding = money(figures?.totalOutstanding),
                overdue = money(figures?.totalOverdue),
                principalDisbursed = money(figures?.principalDisbursed),
                principalPaid = money(figures?.principalPaid),
                principalOutstanding = money(figures?.principalOutstanding),
                interestCharged = money(figures?.interestCharged),
                interestPaid = money(figures?.interestPaid),
                interestOutstanding = money(figures?.interestOutstanding),
                feeChargesCharged = money(figures?.feeChargesCharged),
                feeChargesPaid = money(figures?.feeChargesPaid),
                feeChargesOutstanding = money(figures?.feeChargesOutstanding),
                penaltyChargesCharged = money(figures?.penaltyChargesCharged),
                penaltyChargesPaid = money(figures?.penaltyChargesPaid),
                penaltyChargesOutstanding = money(figures?.penaltyChargesOutstanding),
                // The disbursement date is only shown when the figures are, for the same reason —
                // an expected date on an undisbursed loan reads as an actual one.
                disbursedOn = loan.timeline?.actualDisbursementDate
                    ?.takeIf { status.showsFigures }
                    ?.let { parts -> parts.filterNotNull().takeIf { it.size >= DATE_PARTS } }
                    ?.let { DateHelper.getDateAsString(it) }
                    ?: absent,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: LoanSummaryAction) {
        when (action) {
            LoanSummaryAction.NavigateBack -> sendEvent(LoanSummaryEvent.NavigateBack)
            LoanSummaryAction.OnRetry -> stream.retry()

            LoanSummaryAction.ToggleDropdown ->
                mutableStateFlow.value = state.copy(openDropdown = !state.openDropdown)

            LoanSummaryAction.OnAccountNoCopied ->
                mutableStateFlow.value = state.copy(showCopiedMessage = true)

            LoanSummaryAction.OnMessageShown ->
                mutableStateFlow.value = state.copy(showCopiedMessage = false)

            is LoanSummaryAction.OnMenuItemClick -> {
                mutableStateFlow.value = state.copy(openDropdown = false)
                sendEvent(LoanSummaryEvent.OpenMenuItem(action.item, state.loanId))
            }

            // Carries the status the screen RENDERED, as the profile does — the original read
            // `state.loanWithAssociations` back at tap time and did nothing when it was null, so
            // a tap during a reload silently failed.
            is LoanSummaryAction.OnPrimaryActionClick ->
                action.status.primaryAction()?.let { sendEvent(LoanSummaryEvent.OpenAction(it, state.loanId)) }
        }
    }

    private companion object {
        const val LOAN_ID_KEY = "loanId"
        const val STATE_TIMEOUT_MS = 5_000L
        const val DATE_PARTS = 3
        const val NOT_YET = "—"
    }
}

/**
 * The five states this screen distinguishes, in the original's own precedence.
 *
 * [showsFigures] is `shouldInflateLoanSummary`: ACTIVE, OVERPAID and CLOSED have real money
 * behind them; PENDING and AWAITING_DISBURSAL do not.
 */
internal enum class LoanSummaryStatus(val showsFigures: Boolean) {
    ACTIVE(showsFigures = true),
    PENDING(showsFigures = false),
    AWAITING_DISBURSAL(showsFigures = false),
    OVERPAID(showsFigures = true),
    CLOSED(showsFigures = true),
}

/**
 * THIS screen's precedence — active first.
 *
 * Deliberately NOT the loan profile's, which tests `pendingApproval` first and so reports PENDING
 * for a loan Fineract marks both pending and active. The two screens disagreed in the original and
 * that is carried over rather than silently unified; see the ViewModel header.
 */
internal fun LoanStatus?.toSummaryStatus(): LoanSummaryStatus = when {
    this == null -> LoanSummaryStatus.CLOSED
    active == true -> LoanSummaryStatus.ACTIVE
    pendingApproval == true -> LoanSummaryStatus.PENDING
    waitingForDisbursal == true -> LoanSummaryStatus.AWAITING_DISBURSAL
    overpaid == true -> LoanSummaryStatus.OVERPAID
    // Every closed variant, and anything unrecognised. The original's `else`.
    else -> LoanSummaryStatus.CLOSED
}

/**
 * Which action the primary button offers, or null when it offers none.
 *
 * CLOSED returns null, which is what makes the button inert in the original too — there
 * `isButtonActive()` excluded CLOSED, so the button was drawn DISABLED and its `onClick` branch
 * was an empty `{ }`. Keeping the null means the disabled state is derived from the same place as
 * the label rather than from a second predicate that could disagree.
 */
internal fun LoanSummaryStatus.primaryAction(): LoanSummaryAct? = when (this) {
    LoanSummaryStatus.ACTIVE -> LoanSummaryAct.MakeRepayment
    LoanSummaryStatus.PENDING -> LoanSummaryAct.Approve
    LoanSummaryStatus.AWAITING_DISBURSAL -> LoanSummaryAct.Disburse
    LoanSummaryStatus.OVERPAID -> LoanSummaryAct.Transfer
    LoanSummaryStatus.CLOSED -> null
}

internal enum class LoanSummaryAct { MakeRepayment, Approve, Disburse, Transfer }

/** The overflow menu's five entries, in the original's order. */
internal enum class LoanSummaryMenuItem { MoreInfo, Transactions, RepaymentSchedule, Documents, Charges }

/** Everything the screen renders, formatted once. */
internal data class LoanSummaryUi(
    val clientName: String,
    val accountNo: String,
    val loanProductName: String,
    val loanOfficerName: String,
    val status: LoanSummaryStatus,
    val totalLoan: String,
    val amountPaid: String,
    val outstanding: String,
    val overdue: String,
    val disbursedOn: String,
    val principalDisbursed: String,
    val principalPaid: String,
    val principalOutstanding: String,
    val interestCharged: String,
    val interestPaid: String,
    val interestOutstanding: String,
    val feeChargesCharged: String,
    val feeChargesPaid: String,
    val feeChargesOutstanding: String,
    val penaltyChargesCharged: String,
    val penaltyChargesPaid: String,
    val penaltyChargesOutstanding: String,
)

internal data class LoanSummaryState(
    val loanId: Int = -1,
    val openDropdown: Boolean = false,
    val showCopiedMessage: Boolean = false,
)

internal sealed interface LoanSummaryEvent {
    data object NavigateBack : LoanSummaryEvent
    data class OpenMenuItem(val item: LoanSummaryMenuItem, val loanId: Int) : LoanSummaryEvent
    data class OpenAction(val action: LoanSummaryAct, val loanId: Int) : LoanSummaryEvent
}

internal sealed interface LoanSummaryAction {
    data object NavigateBack : LoanSummaryAction
    data object OnRetry : LoanSummaryAction
    data object ToggleDropdown : LoanSummaryAction
    data object OnAccountNoCopied : LoanSummaryAction
    data object OnMessageShown : LoanSummaryAction
    data class OnMenuItemClick(val item: LoanSummaryMenuItem) : LoanSummaryAction
    data class OnPrimaryActionClick(val status: LoanSummaryStatus) : LoanSummaryAction
}
