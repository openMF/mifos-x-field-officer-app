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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.loan.LoanRepository
import kpt.core.data.recent.RecentRecordsRepository
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanStatus
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.recent.RecentKind
import kpt.core.model.recent.RecentRecord
import kpt.feature.loan.profile.components.LoanProfileActionItem

/**
 * A loan account's profile — ported from
 * `6b66e8a43:feature/loan/loanAccountProfile/LoanAccountProfileViewModel.kt`.
 *
 * Reached from the client's loan list and from group detail. (The original ALSO reached it from
 * `clientDetailRoute`, which nothing navigates to.)
 *
 * ## The `networkMonitor` observer here was the worst of the set
 *
 * Every ported screen had one; this one used it as a load TRIGGER with a null-guard:
 *
 * ```kotlin
 * networkMonitor.isOnline.collect { isConnected ->
 *     if (isConnected) { if (state.loanAccount == null) loadLoanAccountDetails(route.loanId) }
 *     else if (state.loanAccount == null) { … Error(network_not_available) }
 * }
 * ```
 *
 * So the loan was fetched once and then NEVER again — `loanAccount != null` suppressed every
 * subsequent load, including after a reconnect and including the explicit Retry, which checked
 * `networkConnection` and showed "details not found" when offline rather than retrying. A stale
 * loan could sit on screen indefinitely with no way to refresh it. The stream re-reads on
 * reconnect for every screen, and `retry()` always reaches the read.
 *
 * ## The status drives two derived things, and they must agree
 *
 * `statusUiModel` (label + colour) and `nextActionButtonRes` (which action the primary button
 * offers) were both written into the state at load time from the same status. They are derived
 * from the stream here, so the chip and the button can never disagree about what state the loan is
 * in — the class of bug that shipped on the group screen, where the status text and the Activate
 * button tested the same field differently.
 */
internal class LoanProfileViewModel(
    repository: LoanRepository,
    savedStateHandle: SavedStateHandle,
    private val recents: RecentRecordsRepository,
) : BaseViewModel<Unit, LoanProfileEvent, LoanProfileAction>(Unit) {

    private val loanId: Int = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1

    private val stream = repository.loanStream(loanId, viewModelScope)

    val loan: StateFlow<ScreenState<LoanWithAssociations>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    init {
        // Home's "Recently opened": record this visit once, when the record first loads.
        loan.filterIsInstance<ScreenState.Content<LoanWithAssociations>>().take(1)
            .onEach { content ->
                val data = content.data
                recents.record(
                    RecentRecord(
                        RecentKind.LOAN,
                        (data.id ?: loanId).toLong(),
                        data.loanProductName.orEmpty(),
                        data.accountNo,
                        detail = data.clientName,
                    ),
                )
            }
            .launchIn(viewModelScope)
    }

    /** The loan's status, derived from the same emission the detail renders. */
    val status: StateFlow<ScreenState<LoanProfileStatus>> = stream.state
        .mapContent { loan, _ -> loan.status.toProfileStatus() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: LoanProfileAction) {
        when (action) {
            LoanProfileAction.NavigateBack -> sendEvent(LoanProfileEvent.NavigateBack)

            // Always reaches the read. The original refused to retry while it believed itself
            // offline, and showed "details not found" instead.
            LoanProfileAction.OnRetry -> stream.retry()

            is LoanProfileAction.OnDetailItemClick ->
                sendEvent(LoanProfileEvent.OpenDetail(action.item, loanId))

            LoanProfileAction.OnAccountClick ->
                sendEvent(LoanProfileEvent.OpenAccountDetails(loanId))

            // The STATUS comes from the screen, which holds the emission it rendered — the
            // original re-read `state.loanAccount` at tap time and silently did nothing if it was
            // null.
            is LoanProfileAction.OnPrimaryActionClick -> sendEvent(
                when (action.status) {
                    LoanProfileStatus.PENDING -> LoanProfileEvent.OpenAction(LoanPrimaryAction.Approve, loanId)
                    LoanProfileStatus.OVERPAID -> LoanProfileEvent.OpenAction(LoanPrimaryAction.Transfer, loanId)
                    LoanProfileStatus.ACTIVE -> LoanProfileEvent.OpenAction(LoanPrimaryAction.Repayment, loanId)
                    LoanProfileStatus.UNKNOWN -> LoanProfileEvent.OpenAccountDetails(loanId)
                },
            )
        }
    }

    private companion object {
        const val LOAN_ID_KEY = "loanId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * The four states this screen distinguishes.
 *
 * NOT the full Fineract status set — `LoanStatus` also carries `waitingForDisbursal`,
 * `closedObligationsMet`, `closedWrittenOff`, `closedRescheduled` and `closed`, all of which fall
 * into [UNKNOWN] here. That is the original's behaviour and it is kept: a closed loan shows
 * "Unknown" with a "View Action" button. Recorded in CAPABILITY_GAPS rather than widened, because
 * deciding what a closed or awaiting-disbursal loan should OFFER is a product question.
 */
internal enum class LoanProfileStatus { ACTIVE, PENDING, OVERPAID, UNKNOWN }

/**
 * The original's precedence, carried over exactly: pending beats overpaid beats active.
 *
 * The order matters — a loan can be both `active` and `overpaid` in Fineract, and the original
 * showed OVERPAID for that combination because it tested `overpaid` first.
 */
internal fun LoanStatus?.toProfileStatus(): LoanProfileStatus = when {
    this == null -> LoanProfileStatus.UNKNOWN
    pendingApproval == true -> LoanProfileStatus.PENDING
    overpaid == true -> LoanProfileStatus.OVERPAID
    active == true -> LoanProfileStatus.ACTIVE
    else -> LoanProfileStatus.UNKNOWN
}

/** Which action the primary button offers for a status. Derived, never stored. */
internal fun LoanProfileStatus.primaryAction(): LoanPrimaryAction? = when (this) {
    LoanProfileStatus.PENDING -> LoanPrimaryAction.Approve
    LoanProfileStatus.OVERPAID -> LoanPrimaryAction.Transfer
    LoanProfileStatus.ACTIVE -> LoanPrimaryAction.Repayment
    // UNKNOWN offers no action of its own — the button falls through to account details.
    LoanProfileStatus.UNKNOWN -> null
}

internal enum class LoanPrimaryAction { Approve, Repayment, Transfer }

internal sealed interface LoanProfileEvent {
    data object NavigateBack : LoanProfileEvent
    data class OpenAction(val action: LoanPrimaryAction, val loanId: Int) : LoanProfileEvent
    data class OpenDetail(val item: LoanProfileActionItem, val loanId: Int) : LoanProfileEvent
    data class OpenAccountDetails(val loanId: Int) : LoanProfileEvent
}

internal sealed interface LoanProfileAction {
    data object NavigateBack : LoanProfileAction
    data object OnRetry : LoanProfileAction
    data object OnAccountClick : LoanProfileAction
    data class OnPrimaryActionClick(val status: LoanProfileStatus) : LoanProfileAction
    data class OnDetailItemClick(val item: LoanProfileActionItem) : LoanProfileAction
}
