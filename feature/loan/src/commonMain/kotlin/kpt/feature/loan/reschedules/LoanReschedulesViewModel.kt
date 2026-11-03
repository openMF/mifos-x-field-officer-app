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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.DateHelper
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.data.loan.LoanRepository
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleApprovalRequest
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRejectionRequest
import kpt.core.model.objects.account.loan.reschedules.RescheduleStatus
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ApiDateFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Every reschedule proposed on a loan, with approve and reject — ported from
 * `6b66e8a43:feature/loan/loanReschedules/{LoanReschedulesViewModel,Screen}.kt`.
 *
 * Reached from the loan PROFILE's "Loan Reschedules" row, which was not drawn until now.
 *
 * ## One of the better-written screens in the original, and it shows
 *
 * This ViewModel already used `BaseViewModel`, already paired its dates through `ApiDateFormatter`,
 * and already checked the PENDING precondition twice — once when the icon is tapped and again when
 * the confirmation is accepted, so a row that changed status underneath cannot be acted on. All
 * three are kept; the second check in particular is the kind of guard most of this feature's other
 * screens lacked.
 *
 * ## The one thing it got wrong: one dialog state for two different waits
 *
 * `DialogState.Loading` covered both the initial fetch AND an approve/reject in flight, so
 * approving a row replaced the whole list with a loading dialog and then rebuilt it. The read is a
 * [ScreenState] here and the action's in-flight state is its own flag, so the list stays on screen
 * while a row is being approved.
 *
 * ## "Delete" is really REJECT
 *
 * The original's icon, action and repository method are all named delete — but the wire command is
 * `?command=reject` with a `LoanRescheduleRejectionRequest`, so the server records a rejection with
 * a date and a user rather than removing anything. Named reject throughout here; the gap entry
 * `loan-reschedule-delete-is-really-reject` records that the LABEL is a product decision.
 */
internal class LoanReschedulesViewModel(
    repository: LoanRepository,
    private val commands: LoanCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanReschedulesState, LoanReschedulesEvent, LoanReschedulesAction>(
    LoanReschedulesState(loanId = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1),
) {

    private val stream = repository.loanReschedulesStream(state.loanId, viewModelScope)

    val history: StateFlow<ScreenState<List<RescheduleRow>>> = stream.state
        .mapContent { reschedules, _ -> reschedules.map { it.toRow() } }
        // The empty decision lives HERE, not on the repository's stream as an `isEmpty` predicate.
        // One place, and provable without the real store — the same shape the transactions, charges
        // and client-loan-accounts lists use.
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /**
     * Which reschedules are PENDING right now — shared EAGERLY, and that is load-bearing.
     *
     * [act] reads this as its second precondition check. Under `WhileSubscribed` a `.value` read
     * returns the INITIAL value whenever nothing is collecting, so the guard would see an empty
     * set and refuse every approve and reject — which is exactly what the first version of this
     * ViewModel did, caught by eight failing tests at once.
     *
     * Third time this shape has bitten in this port: the repayment screen's double-payment guard
     * and account number (both `Eagerly` now), the client loan-accounts count (render-only, so
     * `WhileSubscribed` was right there and the TEST had to collect), and this. The rule that
     * falls out, written down in `feature/client/MIGRATION.md`: **a flow read by anything other
     * than the composition must not be `WhileSubscribed`.** [history] itself stays
     * `WhileSubscribed` because only the screen reads it.
     */
    private val pendingIds: StateFlow<Set<Int>> = stream.state
        .map { state ->
            (state as? ScreenState.Content)
                ?.data
                ?.filter { it.statusEnum.toStatusCode() == RescheduleStatusCode.PENDING }
                ?.map { it.id }
                ?.toSet()
                .orEmpty()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    override fun handleAction(action: LoanReschedulesAction) {
        when (action) {
            LoanReschedulesAction.NavigateBack -> sendEvent(LoanReschedulesEvent.NavigateBack)

            LoanReschedulesAction.OnRefresh -> stream.refresh(forceFresh = true)
            LoanReschedulesAction.OnPropose -> sendEvent(LoanReschedulesEvent.Propose(state.loanId))
            LoanReschedulesAction.DismissDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            // Only a PENDING reschedule can be acted on. Checked here, and AGAIN at confirm —
            // the original's double guard, kept, because the list re-reads between the two taps
            // and a row can change status in between.
            is LoanReschedulesAction.OnApproveClick -> mutableStateFlow.value = when {
                action.row.status != RescheduleStatusCode.PENDING -> state
                else -> state.copy(
                    dialogState = LoanReschedulesState.DialogState.ConfirmApprove(action.row.id),
                )
            }

            is LoanReschedulesAction.OnRejectClick -> mutableStateFlow.value = when {
                action.row.status != RescheduleStatusCode.PENDING -> state
                else -> state.copy(
                    dialogState = LoanReschedulesState.DialogState.ConfirmReject(action.row.id),
                )
            }

            is LoanReschedulesAction.ConfirmApprove -> act(action.rescheduleId, Act.APPROVE)
            is LoanReschedulesAction.ConfirmReject -> act(action.rescheduleId, Act.REJECT)
        }
    }

    /** Approve and reject differ only in their payload and their endpoint. */
    private enum class Act { APPROVE, REJECT }

    @OptIn(ExperimentalTime::class)
    private fun act(rescheduleId: Int, what: Act) {
        if (state.actingOn != null) return
        // The SECOND precondition check, against the list as it stands NOW.
        if (!isPending(rescheduleId)) {
            mutableStateFlow.value = state.copy(
                dialogState = LoanReschedulesState.DialogState.Failed(NO_LONGER_PENDING_MSG),
            )
            return
        }

        mutableStateFlow.value = state.copy(actingOn = rescheduleId, dialogState = null)

        viewModelScope.launch {
            val today = ApiDateFormatter.formatForApi(Clock.System.now().toEpochMilliseconds())
            val result = when (what) {
                Act.APPROVE -> commands.approveLoanReschedule(
                    state.loanId,
                    rescheduleId,
                    LoanRescheduleApprovalRequest(
                        approvedOnDate = today,
                        dateFormat = ApiDateFormatter.DATE_FORMAT,
                        locale = ApiDateFormatter.LOCALE,
                    ),
                )

                Act.REJECT -> commands.rejectLoanReschedule(
                    state.loanId,
                    rescheduleId,
                    LoanRescheduleRejectionRequest(
                        rejectedOnDate = today,
                        dateFormat = ApiDateFormatter.DATE_FORMAT,
                        locale = ApiDateFormatter.LOCALE,
                    ),
                )
            }

            when (result) {
                // No re-read here: the command records the row's new status through the
                // reschedules store, so the list this screen observes has already re-emitted.
                is MutationResult.Applied -> mutableStateFlow.value = state.copy(actingOn = null, dialogState = null)

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    actingOn = null,
                    dialogState = LoanReschedulesState.DialogState.Failed(
                        if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else failedMsg(what),
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "reschedule $what failed" }
                    mutableStateFlow.value = state.copy(
                        actingOn = null,
                        dialogState = LoanReschedulesState.DialogState.Failed(
                            // Fineract's reason is the useful part — "loan is not active" is the
                            // common one. The original showed one fixed string per action.
                            result.cause.serverErrorMessage() ?: failedMsg(what),
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    actingOn = null,
                    dialogState = LoanReschedulesState.DialogState.Failed(failedMsg(what)),
                )
            }
        }
    }

    private fun isPending(rescheduleId: Int): Boolean = rescheduleId in pendingIds.value

    private fun failedMsg(what: Act) = when (what) {
        Act.APPROVE -> APPROVE_FAILED_MSG
        Act.REJECT -> REJECT_FAILED_MSG
    }

    private companion object {
        const val TAG = "LoanReschedules"
        const val LOAN_ID_KEY = "loanId"
        const val STATE_TIMEOUT_MS = 5_000L

        const val OFFLINE_MSG = "No connection — nothing was sent."
        const val APPROVE_FAILED_MSG = "Could not approve this reschedule."
        const val REJECT_FAILED_MSG = "Could not reject this reschedule."
        const val NO_LONGER_PENDING_MSG =
            "This reschedule is no longer pending — reload to see its current status."
    }
}

/** One row, formatted. The screen renders strings and never reaches for the model. */
internal data class RescheduleRow(
    val id: Int,
    val status: RescheduleStatusCode,
    val rescheduleFrom: String,
    val reason: String,
    val submittedOn: String,
    val submittedBy: String,
) {
    /** Approve and reject are offered on a PENDING row only — the original's rule. */
    val isActionable: Boolean get() = status == RescheduleStatusCode.PENDING
}

private fun kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse.toRow() =
    RescheduleRow(
        id = id,
        status = statusEnum.toStatusCode(),
        rescheduleFrom = date(rescheduleFromDate),
        reason = rescheduleReasonCodeValue?.name.orEmpty(),
        submittedOn = date(timeline?.submittedOnDate),
        submittedBy = timeline?.submittedByUsername.orEmpty(),
    )

/**
 * `[year, month, day]` → "14 Mar 2026". Guarded on size because `getDateAsString` indexes `[2]`
 * directly and a reschedule's dates are all optional on the wire.
 */
private fun date(parts: List<Int>?): String =
    parts?.takeIf { it.size >= DATE_PARTS }?.let { DateHelper.getDateAsString(it) } ?: "—"

private const val DATE_PARTS = 3

/** The four states a reschedule can be in. */
internal enum class RescheduleStatusCode { PENDING, APPROVED, REJECTED, UNKNOWN }

/**
 * The original's mapping, kept — including that `UNKNOWN` is the catch-all for a null status.
 *
 * `RescheduleStatus`'s three booleans are non-null in the model, so the only route to UNKNOWN is a
 * null status object or all three false.
 */
internal fun RescheduleStatus?.toStatusCode(): RescheduleStatusCode = when {
    this == null -> RescheduleStatusCode.UNKNOWN
    pendingApproval -> RescheduleStatusCode.PENDING
    approved -> RescheduleStatusCode.APPROVED
    rejected -> RescheduleStatusCode.REJECTED
    else -> RescheduleStatusCode.UNKNOWN
}

internal data class LoanReschedulesState(
    val loanId: Int = -1,
    /** The reschedule currently being approved or rejected, if any. */
    val actingOn: Int? = null,
    val dialogState: DialogState? = null,
) {
    sealed interface DialogState {
        data class ConfirmApprove(val rescheduleId: Int) : DialogState
        data class ConfirmReject(val rescheduleId: Int) : DialogState
        data class Failed(val msg: String) : DialogState
    }
}

internal sealed interface LoanReschedulesEvent {
    data object NavigateBack : LoanReschedulesEvent
    data class Propose(val loanId: Int) : LoanReschedulesEvent
}

internal sealed interface LoanReschedulesAction {
    data object NavigateBack : LoanReschedulesAction

    /**
     * Re-read, from EITHER the pull gesture or the error state's retry.
     *
     * ONE member, because `RefreshableScreenContent` routes both to the same callback — and
     * because they are the same call anyway: `ScreenDataStream.retry()` is declared
     * `= refresh(forceFresh = true)`. A separate `OnRetry` would be a second name for one
     * operation, and whichever the wrapper did not route would sit undispatched.
     */
    data object OnRefresh : LoanReschedulesAction
    data object OnPropose : LoanReschedulesAction
    data object DismissDialog : LoanReschedulesAction
    data class OnApproveClick(val row: RescheduleRow) : LoanReschedulesAction
    data class OnRejectClick(val row: RescheduleRow) : LoanReschedulesAction
    data class ConfirmApprove(val rescheduleId: Int) : LoanReschedulesAction
    data class ConfirmReject(val rescheduleId: Int) : LoanReschedulesAction
}
