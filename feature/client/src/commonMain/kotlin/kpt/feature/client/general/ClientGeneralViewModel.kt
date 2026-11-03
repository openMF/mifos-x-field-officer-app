/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.general

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientRepository
import kpt.core.model.client.ClientAccounts
import kpt.core.model.savings.SavingAccountDepositType
import kpt.feature.client.general.components.ClientGeneralActionItem

/**
 * The client's GENERAL view — performance history plus the linked-account actions. Reached from the
 * client profile's "General" row. Ported from
 * `6b66e8a43:feature/client/clientGeneral/ClientProfileGeneralViewmodel.kt`.
 *
 * ## This is where accounts belong
 *
 * An earlier pass of this port put a loan/savings accounts section on the client DETAIL screen.
 * The original has no accounts there: the profile screen's General row opens THIS screen, which
 * summarises them and routes to the per-type list screens. The accounts read moved here with them.
 *
 * ## Substrate changes
 *
 * `GetClientDetailsUseCase` (which fanned client + accounts into one `DataState`) collapses to
 * `clientAccountsStream`; `core/domain` is deleted on this substrate (§2). The `networkMonitor`
 * observer goes, as on every other ported screen — `ScreenState.NoNetwork` is the stream's.
 *
 * The CLIENT itself is not read here. The original kept `state.client` only to pass `client.id`
 * into the seven navigation callbacks, and that id is the route argument this screen already has —
 * so reading the whole client to recover its own id was a wasted request, and one that could
 * resolve to `-1` while in flight.
 *
 * ## State is `Unit`
 *
 * Everything this screen renders is on [performance], and the client id is a route argument — so
 * there is no per-screen state left to hold. `BaseViewModel<Unit, E, A>` is the template's own
 * shape for that case; a one-field state class would just be ceremony.
 */
internal class ClientGeneralViewModel(
    repository: ClientRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit, ClientGeneralEvent, ClientGeneralAction>(Unit) {

    private val clientId: Int = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1

    private val stream = repository.clientAccountsStream(clientId, viewModelScope)

    /**
     * Performance history, DERIVED from the accounts rather than stored.
     *
     * The original computed it once per load and wrote it into the state beside the accounts, with
     * `var` fields in the holder. Deriving it means it cannot fall out of step with the accounts
     * it summarises.
     */
    val performance: StateFlow<ScreenState<PerformanceHistory>> = stream.state
        .mapContent { accounts, _ -> performanceHistory(accounts) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ClientGeneralAction) {
        when (action) {
            ClientGeneralAction.NavigateBack -> sendEvent(ClientGeneralEvent.NavigateBack)
            ClientGeneralAction.OnRetry -> stream.retry()
            is ClientGeneralAction.OnActionClick ->
                sendEvent(ClientGeneralEvent.OnActionClick(action.action, clientId))
        }
    }

    private companion object {
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * The five figures on the performance-history card, and the currency they are shown in.
 *
 * Every rule here is the original's, including the one gap it left.
 */
internal data class PerformanceHistory(
    val loanCyclesCount: Int = 0,
    val activeLoans: Int = 0,
    /**
     * Always `null`. The original's comment: *"No function yet created for calculating this
     * value."* Kept null rather than invented, so the row renders "Not Available" exactly as it
     * does today — a fabricated figure on a loan screen is worse than an honest blank.
     */
    val lastLoanAmount: Double? = null,
    val activeSavingsCount: Int = 0,
    val totalSaving: Double? = null,
    val currency: String = DEFAULT_CURRENCY,
) {
    companion object {
        /** The original's fallback when no savings account carries a currency. */
        const val DEFAULT_CURRENCY = "$"
    }
}

/**
 * The original's arithmetic, carried over exactly.
 *
 * Two of these rules are worth naming because they are not what the labels suggest:
 *
 * - **"No. Of Loan Cycles"** SUMS `loanCycle` across active loans; it is not a count of loans.
 * - **"Total Savings"** excludes RECURRING deposits and closed accounts, so it does not equal the
 *   sum of the savings balances the accounts list shows.
 *
 * Both are kept. They are the figures the app shows today, and changing them would change reported
 * numbers under the officer without anyone asking for it.
 */
internal fun performanceHistory(accounts: ClientAccounts): PerformanceHistory {
    val loans = accounts.loanAccounts
    val savings = accounts.savingsAccounts

    return PerformanceHistory(
        loanCyclesCount = loans.filter { it.status?.active == true }.sumOf { it.loanCycle ?: 0 },
        activeLoans = loans.count { it.status?.active == true },
        lastLoanAmount = null,
        activeSavingsCount = savings.count { it.status?.active == true },
        totalSaving = savings
            .filter {
                it.status?.active == true &&
                    it.depositType?.serverType != SavingAccountDepositType.ServerTypes.RECURRING &&
                    it.status?.closed == false
            }
            .sumOf { it.accountBalance ?: 0.0 },
        // First savings account's symbol, as in the original — not a per-account currency.
        currency = savings.firstOrNull()?.currency?.displaySymbol ?: PerformanceHistory.DEFAULT_CURRENCY,
    )
}

internal sealed interface ClientGeneralEvent {
    data object NavigateBack : ClientGeneralEvent
    data class OnActionClick(val action: ClientGeneralActionItem, val clientId: Int) : ClientGeneralEvent
}

internal sealed interface ClientGeneralAction {
    data object NavigateBack : ClientGeneralAction
    data object OnRetry : ClientGeneralAction
    data class OnActionClick(val action: ClientGeneralActionItem) : ClientGeneralAction
}
