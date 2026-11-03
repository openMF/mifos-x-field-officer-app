/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.repayment

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
import kpt.core.base.store.screen.combineScreenStates
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.DateHelper
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.data.loan.LoanRepository
import kpt.core.data.sync.OfflineQueueRepository
import kpt.core.model.loan.LoanRepaymentRequest
import kpt.core.model.objects.error.serverErrorMessage
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Record a repayment against a loan — ported from
 * `6b66e8a43:feature/loan/loanRepayment/LoanRepaymentViewModel.kt` and its screen.
 *
 * Reached from the loan profile's primary button when the loan is ACTIVE, which is the common case
 * — so this is the button most field officers press.
 *
 * ## Three reads become one combined state
 *
 * The original ran three sequential loads with six `_uiState.value =` assignments between them:
 * `getLoanById` (for the header) → `getDatabaseLoanRepaymentByLoanId` (the already-queued check) →
 * `getLoanRepayTemplate` (the form's payment types and amount due). Each overwrote the single
 * `uiState`, and the third was triggered FROM THE COMPOSITION BODY:
 *
 * ```kotlin
 * LoanRepaymentUiState.ShowLoanRepaymentDoesNotExistInDatabase -> {
 *     onLoanRepaymentDoesNotExistInDatabase.invoke()   // loads the template, during composition
 * }
 * ```
 *
 * So the template load re-fired on every recomposition for as long as that state was showing.
 * Here the loan and the template are two streams combined into one [ScreenState], and the queued
 * check is a third flow that gates the form — no ordering to get wrong and no load triggered by
 * drawing.
 *
 * ## The already-queued check is a REFUSAL, and it is kept
 *
 * If a repayment for this loan is already sitting in the offline queue, the original refused to
 * take another and told the officer to sync first. That is right: two repayments for one visit is
 * money counted twice, and the queued one has not reached Fineract yet so the server cannot
 * dedupe it. Kept, reading the same queue the sync screen drains.
 *
 * ## The date pairing here is CORRECT, unlike the approval's
 *
 * The original sends `transactionDate = DateHelper.getDateAsStringFromLong(date)` with
 * `dateFormat = DateHelper.SHORT_MONTH`, and those two genuinely agree — that constant is
 * `"dd-MM-yyyy"` and the helper formats with it. Carried over unchanged, pinned by a test, and
 * flagged here because the same-looking line in `LoanApprovalViewModel` was broken: the two
 * constants named `SHORT_MONTH` in this codebase hold different formats.
 */
internal class LoanRepaymentViewModel(
    private val repository: LoanRepository,
    private val commands: LoanCommandRepository,
    private val queue: OfflineQueueRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanRepaymentState, LoanRepaymentEvent, LoanRepaymentAction>(
    LoanRepaymentState(loanId = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1),
) {

    private val loanStream = repository.loanStream(state.loanId, viewModelScope)
    private val templateStream = repository.repaymentTemplateStream(state.loanId, viewModelScope)
    private val queueStream = queue.pendingLoanRepaymentsStream(viewModelScope)

    /**
     * The header figures and the form's options, from the two reads at once.
     *
     * Combined rather than nested: the original rendered the header from one request and the form
     * from another, and showed whichever had landed. A single state means the screen never draws
     * an amount-due from one loan beside a payment-type list fetched for another.
     */
    val form: StateFlow<ScreenState<LoanRepaymentForm>> =
        combineScreenStates(loanStream.state, templateStream.state) { loan, template ->
            LoanRepaymentForm(
                clientName = loan.clientName.orEmpty(),
                loanAccountNumber = loan.accountNo.orEmpty(),
                loanProductName = loan.loanProductName.orEmpty(),
                amountInArrears = loan.summary?.totalOverdue,
                amountDue = template.amount,
                currencyCode = template.currency?.code,
                decimalPlaces = template.currency?.decimalPlaces,
                // Projected to the pair the form needs. The entity carries five more fields the
                // screen never reads, and keeping it out means `feature/loan` does not name a
                // `core/database` type in its own form model.
                paymentTypes = template.paymentTypeOptions.orEmpty().map {
                    LoanPaymentType(id = it.id, name = it.name)
                },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /**
     * Whether a repayment for THIS loan is already queued offline.
     *
     * A `StateFlow<Boolean>` rather than a `ScreenState`, because the screen does not render the
     * queue's loading or error — it only needs the answer, and a read that has not landed is
     * treated as "not queued" so the form draws.
     *
     * ## `Eagerly`, NOT `WhileSubscribed` — and that is load-bearing
     *
     * [submit] reads this flow's CURRENT VALUE as its last line of defence against paying twice.
     * Under `WhileSubscribed` the upstream only runs while something collects, so `.value` sits at
     * the initial `false` whenever it does not — and a guard that reads `false` because nobody was
     * looking is not a guard. Caught by `submitRefusesWhenOneWasQueuedWhileTheFormSatOpen`, which
     * failed for exactly this reason: on device the screen happens to be collecting, so the bug
     * would only have appeared as a double payment in the field.
     *
     * The cost is one queue subscription for the ViewModel's life, which is the right trade for a
     * check that exists to stop money being recorded twice.
     */
    val alreadyQueued: StateFlow<Boolean> = queueStream.state
        .map { state -> (state as? ScreenState.Content)?.data.orEmpty().any { it.loanId == this.state.loanId } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * The loan's account number, for the request to name the account.
     *
     * `Eagerly` for the same reason as [alreadyQueued]: it is read by the WRITE, and a
     * `WhileSubscribed` share hands back its initial `null` when no collector is active. That is
     * how `accountNumber` arrived null in the first version of this ViewModel.
     *
     * Separate from [form] rather than read out of it, because [form] is a render-only state that
     * legitimately stays `Loading` until BOTH reads land — the write needs only this one field.
     */
    private val accountNumber: StateFlow<String?> = loanStream.state
        .map { (it as? ScreenState.Content)?.data?.accountNo }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override fun handleAction(action: LoanRepaymentAction) {
        when (action) {
            LoanRepaymentAction.NavigateBack -> sendEvent(LoanRepaymentEvent.NavigateBack)

            LoanRepaymentAction.OnRetry -> {
                // BOTH reads. The original's retry re-ran whichever load it believed had failed,
                // chosen from a flag — so a failure in the other one was unreachable.
                loanStream.retry()
                templateStream.retry()
            }

            is LoanRepaymentAction.UpdateAmount ->
                mutableStateFlow.value = state.copy(amount = action.value)

            is LoanRepaymentAction.UpdateAdditionalPayment ->
                mutableStateFlow.value = state.copy(additionalPayment = action.value)

            is LoanRepaymentAction.UpdateFees ->
                mutableStateFlow.value = state.copy(fees = action.value)

            is LoanRepaymentAction.UpdateDate ->
                mutableStateFlow.value = state.copy(date = action.millis)

            is LoanRepaymentAction.UpdateDatePicker ->
                mutableStateFlow.value = state.copy(showDatePicker = action.status)

            // The chosen payment type carries its ID, not its index. The original looked the ID up
            // as `paymentTypeOptions?.get(index)?.id ?: 0` at selection time, and `?: 0` is not a
            // valid payment type — a mis-indexed selection posted type 0 rather than failing.
            is LoanRepaymentAction.SelectPaymentType -> mutableStateFlow.value = state.copy(
                paymentTypeId = action.option.id,
                paymentTypeName = action.option.name,
            )

            LoanRepaymentAction.OnReview -> mutableStateFlow.value = state.copy(showReview = true)
            LoanRepaymentAction.OnDismissReview -> mutableStateFlow.value = state.copy(showReview = false)
            LoanRepaymentAction.CloseDialog -> mutableStateFlow.value = state.copy(dialogState = null)

            LoanRepaymentAction.OnSubmit -> submit()
        }
    }

    private fun submit() {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, showReview = false)

        viewModelScope.launch {
            // Re-checked at submit, not only at render: the form may have sat open while a sync
            // elsewhere queued one, and a render-time flag is a read of a flow that can lag.
            if (alreadyQueued.value) {
                mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanRepaymentState.DialogState.AlreadyQueued,
                )
                return@launch
            }

            val request = LoanRepaymentRequest(
                loanId = state.loanId,
                // From the eagerly-shared projection of the loan — see [accountNumber]. Keeping a
                // copy in the editable state would be a second source that could go stale against
                // the loan the header is showing.
                accountNumber = accountNumber.value,
                paymentTypeId = state.paymentTypeId?.toString(),
                // The ONE place in this feature where `DateHelper` is correct in a payload — the
                // declared format is the same constant the helper formats with. See the header.
                transactionDate = DateHelper.getDateAsStringFromLong(state.date),
                dateFormat = DateHelper.SHORT_MONTH,
                locale = LOCALE,
                transactionAmount = total().toString(),
            )

            when (val result = commands.submitPayment(state.loanId, request)) {
                is MutationResult.Applied -> {
                    // No re-read: the command ingests the repaid loan — new balance, new
                    // transaction — through the `loans` store the profile and details observe.
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = LoanRepaymentState.DialogState.Submitted(
                            transactionId = result.value.resourceId,
                        ),
                    )
                }

                is MutationResult.Blocked -> {
                    // OFFLINE is NOT a failure for a repayment — it is the one write in this
                    // feature with a designed offline path, because a field officer collects cash
                    // in places with no signal and the money has already changed hands. The
                    // request is queued and the officer is told plainly that it has not been sent.
                    if (result.reason == BlockReason.OFFLINE) {
                        enqueue(request)
                    } else {
                        mutableStateFlow.value = state.copy(
                            isSubmitting = false,
                            dialogState = LoanRepaymentState.DialogState.Failed(FAILED_MSG),
                        )
                    }
                }

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "repayment failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = LoanRepaymentState.DialogState.Failed(
                            result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanRepaymentState.DialogState.Failed(FAILED_MSG),
                )
            }
        }
    }

    private suspend fun enqueue(request: LoanRepaymentRequest) {
        val queued = queue.enqueueLoanRepayment(state.loanId, request)
        mutableStateFlow.value = state.copy(
            isSubmitting = false,
            dialogState = if (queued is MutationResult.Applied) {
                LoanRepaymentState.DialogState.Queued
            } else {
                // The queue write itself failed, so NOTHING holds this payment — the officer has
                // to be told, or cash is collected against no record anywhere.
                LoanRepaymentState.DialogState.Failed(QUEUE_FAILED_MSG)
            },
        )
    }

    private fun total(): Double = totalOf(state.fees, state.amount, state.additionalPayment)

    private companion object {
        const val TAG = "LoanRepayment"
        const val LOAN_ID_KEY = "loanId"
        const val STATE_TIMEOUT_MS = 5_000L
        const val LOCALE = "en"

        const val FAILED_MSG = "Could not record this repayment."
        const val QUEUE_FAILED_MSG =
            "Could not send OR save this repayment — nothing has been recorded. Try again."
    }
}

/**
 * Fees + amount + additional payment, rounded to two decimals, with unparseable fields read as
 * zero. The original's arithmetic exactly, lifted out of the ViewModel so it can be tested without
 * one — the screen sends the sum as a SINGLE `transactionAmount`, which is Fineract's shape.
 */
internal fun totalOf(fees: String, amount: String, additionalPayment: String): Double {
    fun value(raw: String) = raw.trim().toDoubleOrNull() ?: 0.0
    val total = value(fees) + value(amount) + value(additionalPayment)
    return round(total * ROUNDING) / ROUNDING
}

private const val ROUNDING = 100.0

/**
 * Every amount field must parse and be non-negative, and a payment type must be chosen.
 *
 * The original's rule, with one addition: it checked `paymentType.isNotBlank()` on the DISPLAY
 * STRING, so a type whose name was blank passed as unselected and a selection carrying id 0 passed
 * as selected. The id is what the request needs, so the id is what is checked.
 */
internal fun LoanRepaymentState.isSubmittable(): Boolean {
    // Blank is allowed and reads as zero (the original's `calculateTotal` did the same); a
    // non-blank field must parse, and no field may be negative.
    val fields = listOf(amount, additionalPayment, fees)
    val allParse = fields.all { it.isBlank() || (it.trim().toDoubleOrNull()?.let { n -> n >= 0 } == true) }
    // A total of zero is NOT submittable. The original let it through — all three fields blank
    // passed `isAllFieldsValid` (each read as 0.0, each `>= 0`), so an officer could post a
    // zero-amount repayment, which Fineract accepts as a real transaction on the account.
    return allParse && paymentTypeId != null && totalOf(fees, amount, additionalPayment) > 0
}

/** The header figures and the form's options, from both reads at once. */
internal data class LoanRepaymentForm(
    val clientName: String,
    val loanAccountNumber: String,
    val loanProductName: String,
    val amountInArrears: Double?,
    val amountDue: Double?,
    val currencyCode: String?,
    val decimalPlaces: Int?,
    val paymentTypes: List<LoanPaymentType>,
)

/** A selectable payment type: the id the request needs, and the name the dropdown shows. */
internal data class LoanPaymentType(val id: Int, val name: String)

@OptIn(ExperimentalTime::class)
internal data class LoanRepaymentState(
    val loanId: Int = -1,
    val amount: String = "",
    val additionalPayment: String = "",
    val fees: String = "",
    val paymentTypeId: Int? = null,
    val paymentTypeName: String = "",
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val showDatePicker: Boolean = false,
    val showReview: Boolean = false,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
) {
    sealed interface DialogState {
        /** Sent and accepted. [transactionId] is Fineract's `resourceId`, shown to the officer. */
        data class Submitted(val transactionId: Int?) : DialogState

        /** Offline — held in the queue, not sent. The officer must be told the difference. */
        data object Queued : DialogState

        /** A repayment for this loan is already queued; a second one is refused. */
        data object AlreadyQueued : DialogState

        data class Failed(val msg: String) : DialogState
    }
}

internal sealed interface LoanRepaymentEvent {
    data object NavigateBack : LoanRepaymentEvent
}

internal sealed interface LoanRepaymentAction {
    data object NavigateBack : LoanRepaymentAction
    data object OnRetry : LoanRepaymentAction
    data object OnReview : LoanRepaymentAction
    data object OnDismissReview : LoanRepaymentAction
    data object OnSubmit : LoanRepaymentAction
    data object CloseDialog : LoanRepaymentAction
    data class UpdateAmount(val value: String) : LoanRepaymentAction
    data class UpdateAdditionalPayment(val value: String) : LoanRepaymentAction
    data class UpdateFees(val value: String) : LoanRepaymentAction
    data class UpdateDate(val millis: Long) : LoanRepaymentAction
    data class UpdateDatePicker(val status: Boolean) : LoanRepaymentAction
    data class SelectPaymentType(val option: LoanPaymentType) : LoanRepaymentAction
}
