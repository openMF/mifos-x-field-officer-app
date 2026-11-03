/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.loan.LoanRepository
import kpt.core.model.objects.account.loan.RepaymentSchedule
import kpt.core.model.objects.account.loan.RepaymentScheduleTableData

/**
 * A loan's repayment schedule — ported from
 * `6b66e8a43:feature/loan/loanRepaymentSchedule/LoanRepaymentScheduleViewModel.kt`.
 *
 * ## It shares the profile's read rather than making its own
 *
 * The original called `getLoanRepaySchedule(loanId)` — a second request to the same
 * `loans/{id}?associations=all` family the profile had already fetched. The schedule is part of
 * `LoanWithAssociations`, so this projects the SAME cached `loanStream`: opening the profile and
 * then the schedule costs one request, not two, and the two screens cannot disagree about the
 * loan. [kpt.feature.loan.transactions.LoanTransactionsViewModel] does the same with the
 * transactions, for the same reason.
 *
 * The row/totals projection is the original's, unchanged — including the detail that
 * [RepaymentSchedule.getListOfActualPeriods] drops period 0. Fineract returns the DISBURSEMENT as
 * period 0; it is not a repayment, has no due amounts, and is rendered as its own highlighted row
 * above the schedule rather than counted in it. The totals and the complete/pending/overdue
 * counters therefore all run over the actual periods only.
 */
internal class LoanScheduleViewModel(
    repository: LoanRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit, LoanScheduleEvent, LoanScheduleAction>(Unit) {

    private val loanId: Int = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1

    private val stream = repository.loanStream(loanId, viewModelScope)

    val schedule: StateFlow<ScreenState<RepaymentScheduleTableData>> = stream.state
        .mapContent { loan, _ ->
            // The currency travels WITH the periods. In the original the schedule and the loan
            // came from different requests, so a mid-flight refresh could format one request's
            // amounts with another's currency symbol.
            val currencyCode = loan.currency?.code.orEmpty()
            val maxDigits = loan.currency?.decimalPlaces ?: DEFAULT_DECIMALS
            val all = loan.repaymentSchedule?.periods.orEmpty()
            val periods = loan.repaymentSchedule?.getListOfActualPeriods().orEmpty()

            toScheduleTable(all, periods, currencyCode, maxDigits)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: LoanScheduleAction) {
        when (action) {
            LoanScheduleAction.NavigateBack -> sendEvent(LoanScheduleEvent.NavigateBack)
            LoanScheduleAction.OnRetry -> stream.retry()
        }
    }

    private companion object {
        const val LOAN_ID_KEY = "loanId"
        const val STATE_TIMEOUT_MS = 5_000L
        const val DEFAULT_DECIMALS = 2
    }
}

internal sealed interface LoanScheduleEvent {
    data object NavigateBack : LoanScheduleEvent
}

internal sealed interface LoanScheduleAction {
    data object NavigateBack : LoanScheduleAction
    data object OnRetry : LoanScheduleAction
}
