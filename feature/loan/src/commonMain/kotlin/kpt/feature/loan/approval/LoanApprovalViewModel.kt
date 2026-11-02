/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.approval

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.data.loan.LoanRepository
import kpt.core.model.objects.account.loan.LoanApproval
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Approve a pending loan — ported from
 * `6b66e8a43:feature/loan/loanApproval/LoanAccountApprovalViewModel.kt` and its screen.
 *
 * Reached from the loan profile's primary button when the loan is `pendingApproval`.
 *
 * ## The nav argument was a serialized loan; now it is a loan id
 *
 * The original route carried `LoanApprovalData(loanID, loanWithAssociations)` — the ENTIRE loan
 * with all its associations — JSON-encoded into a `SavedStateHandle` string, decoded in the
 * ViewModel's constructor:
 *
 * ```kotlin
 * private val arg = savedStateHandle.getStateFlow(key = "arg", initialValue = "")
 * private val loanAccountData: LoanApprovalData = Json.decodeFromString(arg.value)
 * ```
 *
 * Three problems, all fixed by carrying the id: the decode runs in the CONSTRUCTOR and throws on
 * the empty default (process death restores the handle before the argument, so the screen died on
 * restore); the blob is pushed through the back stack, which Android caps; and the prefill values
 * were a SNAPSHOT taken when the profile loaded, so an amount changed server-side in between was
 * silently approved at the stale figure. Reading `loanStream` here is also free — the profile just
 * populated that cache.
 *
 * ## A FIXED defect: the approval date contradicted its own declared format
 *
 * The original sent `DateHelper.getDateAsStringFromLong(approveDate)` while [LoanApproval]
 * DEFAULTED `dateFormat` to "dd MMMM yyyy". That helper emits **`dd-MM-yyyy`** ("21-10-2026"), so
 * Fineract was told to parse a string it cannot parse. Identical to the bug already fixed in
 * `clientClosure` and `clientTransfer` — the third of four occurrences.
 *
 * Fixed twice over: the payload is built ONLY from [ApiDateFormatter], and [LoanApproval] no longer
 * DEFAULTS `locale`/`dateFormat` at all, so a future edit cannot reintroduce the drift by leaving
 * them untouched.
 *
 * Mind the constant names when reading the original: `DateHelper.SHORT_MONTH` is `"dd-MM-yyyy"`
 * while `DateFormatPattern.SHORT_MONTH` is `"dd MMM yyyy"` — same name, different formats, and
 * `getDateAsStringFromLong` uses the first. Logged as `two-short-month-constants-disagree`.
 *
 * The expected-disbursement date had the same problem one layer up: it arrived from the loan as a
 * `[year, month, day]` triple formatted for DISPLAY, and that display string was sent verbatim.
 * The millis are held in state and formatted for the API at submit, so the field the officer reads
 * and the field the server parses are produced from one value by two formatters that know which
 * job they are doing.
 */
internal class LoanApprovalViewModel(
    repository: LoanRepository,
    private val commands: LoanCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanApprovalState, LoanApprovalEvent, LoanApprovalAction>(
    LoanApprovalState(loanId = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1),
) {

    private val stream = repository.loanStream(state.loanId, viewModelScope)

    /**
     * What the form prefills from: the approved principal and the expected disbursement date.
     *
     * The screen seeds its editable fields from this on first Content — it does NOT re-seed on
     * every emission, or a refresh landing mid-edit would overwrite what the officer typed.
     */
    val prefill: StateFlow<ScreenState<LoanApprovalPrefill>> = stream.state
        .mapContent { loan, _ ->
            LoanApprovalPrefill(
                approvedAmount = loan.approvedPrincipal,
                // `?.` all the way down. The original wrote `loanWithAssociations?.timeline!!`,
                // which crashed the screen on a loan whose timeline Fineract omitted.
                expectedDisbursementOn = loan.timeline?.expectedDisbursementDate
                    ?.takeIf { it.size >= DATE_PARTS },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: LoanApprovalAction) {
        when (action) {
            LoanApprovalAction.NavigateBack -> sendEvent(LoanApprovalEvent.NavigateBack)
            LoanApprovalAction.OnRetry -> stream.retry()

            is LoanApprovalAction.UpdateApprovedAmount ->
                mutableStateFlow.value = state.copy(approvedAmount = action.value)

            is LoanApprovalAction.UpdateNote ->
                mutableStateFlow.value = state.copy(note = action.value)

            is LoanApprovalAction.UpdateApprovedOn ->
                mutableStateFlow.value = state.copy(approvedOn = action.millis)

            is LoanApprovalAction.UpdateDisbursementOn ->
                mutableStateFlow.value = state.copy(disbursementOn = action.millis)

            is LoanApprovalAction.OpenDatePicker ->
                mutableStateFlow.value = state.copy(datePicker = action.which)

            LoanApprovalAction.CloseDatePicker ->
                mutableStateFlow.value = state.copy(datePicker = null)

            LoanApprovalAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            is LoanApprovalAction.Prefilled -> mutableStateFlow.value = state.copy(
                prefilled = true,
                approvedAmount = action.amount,
                disbursementOn = action.disbursementOn,
            )

            LoanApprovalAction.OnSubmit -> approve()
        }
    }

    private fun approve() {
        if (state.isSubmitting) return
        val amount = state.approvedAmount.toDoubleOrNull()
        // The original's `isFieldValid` returned false and the submit branch did NOTHING — no
        // message, no highlight. A blank or non-numeric amount looked like a dead button.
        if (amount == null) {
            mutableStateFlow.value = state.copy(amountError = true)
            return
        }

        mutableStateFlow.value = state.copy(isSubmitting = true, amountError = false, dialogState = null)

        viewModelScope.launch {
            val approval = LoanApproval(
                // Every one of these three comes from `ApiDateFormatter`, which is the point:
                // `locale` and `dateFormat` are REQUIRED parameters now, so the format cannot be
                // left at a default that disagrees with however the date was produced. See
                // `LoanApproval`'s own header for the four times that happened.
                approvedOnDate = ApiDateFormatter.formatForApi(state.approvedOn),
                locale = ApiDateFormatter.LOCALE,
                dateFormat = ApiDateFormatter.DATE_FORMAT,
                approvedLoanAmount = state.approvedAmount,
                expectedDisbursementDate = state.disbursementOn?.let { ApiDateFormatter.formatForApi(it) },
                note = state.note.takeIf { it.isNotBlank() },
            )

            when (val result = commands.approveLoanApplication(state.loanId, approval)) {
                is MutationResult.Applied -> {
                    // No re-read: the command ingests the approved loan through the `loans` store,
                    // so the profile behind this screen already shows it approved.
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = LoanApprovalState.DialogState.ShowStatusDialog(ResultStatus.SUCCESS),
                    )
                }

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanApprovalState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "approve loan failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = LoanApprovalState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            // Fineract's own reason is the useful part — "approved amount exceeds
                            // proposed principal" tells the officer exactly what to change.
                            msg = result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanApprovalState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = FAILED_MSG,
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "LoanApproval"
        const val LOAN_ID_KEY = "loanId"
        const val STATE_TIMEOUT_MS = 5_000L
        const val DATE_PARTS = 3

        const val OFFLINE_MSG = "No connection — approval not sent."
        const val FAILED_MSG = "Could not approve this loan."
    }
}

/** The loan's own figures, used to seed the form once. */
internal data class LoanApprovalPrefill(
    val approvedAmount: Double?,
    /** `[year, month, day]`, as Fineract sends it. */
    val expectedDisbursementOn: List<Int>?,
)

@OptIn(ExperimentalTime::class)
internal data class LoanApprovalState(
    val loanId: Int = -1,
    val approvedAmount: String = "",
    val note: String = "",
    val approvedOn: Long = Clock.System.now().toEpochMilliseconds(),
    /** Null until the loan supplies one or the officer picks one. */
    val disbursementOn: Long? = null,
    val prefilled: Boolean = false,
    val amountError: Boolean = false,
    val datePicker: DateField? = null,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
) {
    /** Which of the two date fields the picker is open for — the original used two booleans. */
    enum class DateField { APPROVED_ON, DISBURSEMENT_ON }

    sealed interface DialogState {
        data class ShowStatusDialog(val status: ResultStatus, val msg: String = "") : DialogState
    }
}

internal sealed interface LoanApprovalEvent {
    data object NavigateBack : LoanApprovalEvent
}

internal sealed interface LoanApprovalAction {
    data object NavigateBack : LoanApprovalAction
    data object OnRetry : LoanApprovalAction
    data object OnSubmit : LoanApprovalAction
    data object CloseDialog : LoanApprovalAction
    data object CloseDatePicker : LoanApprovalAction
    data class UpdateApprovedAmount(val value: String) : LoanApprovalAction
    data class UpdateNote(val value: String) : LoanApprovalAction
    data class UpdateApprovedOn(val millis: Long) : LoanApprovalAction
    data class UpdateDisbursementOn(val millis: Long) : LoanApprovalAction
    data class OpenDatePicker(val which: LoanApprovalState.DateField) : LoanApprovalAction

    /** Seeds the editable fields from the loan, exactly once — see [LoanApprovalViewModel.prefill]. */
    data class Prefilled(val amount: String, val disbursementOn: Long?) : LoanApprovalAction
}

/**
 * `[year, month, day]` → epoch millis at the start of that day in the device's zone.
 *
 * The prefill needs MILLIS because that is what the date picker and the API formatter both take.
 * The original kept the disbursement date as a formatted display STRING and sent that string, so
 * there was nothing to re-format and nothing for the picker to start from — picking a new date
 * replaced the string with one in a third format.
 */
@OptIn(ExperimentalTime::class)
internal fun List<Int>.toStartOfDayMillis(): Long? = runCatching {
    LocalDate(this[0], this[1], this[2])
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
}.getOrNull()
