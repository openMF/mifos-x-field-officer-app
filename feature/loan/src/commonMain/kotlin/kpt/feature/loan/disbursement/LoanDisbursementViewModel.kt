/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.disbursement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.data.loan.LoanRepository
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseInput
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Disburse an approved loan — ported from
 * `6b66e8a43:feature/loan/loanDisbursement/{LoanAccountDisbursementViewModel,Screen}.kt`.
 *
 * Reached from the loan SUMMARY's primary button when the loan is `waitingForDisbursal`. That
 * status is one of the two the loan PROFILE collapses into "Unknown", so the summary is its only
 * entry point — see [kpt.feature.loan.summary.LoanSummaryViewModel].
 *
 * ## FIXED: the date contradicted its own declared format — the FOURTH occurrence
 *
 * The original sent `actualDisbursementDate = DateHelper.getDateAsStringFromLong(date)` on a
 * payload whose `dateFormat` defaulted to `DateConstants.DATE_FORMAT` ("dd MMMM yyyy"), while that
 * helper emits "dd-MM-yyyy". Fineract was told to parse a string it cannot parse. Same bug as
 * `clientClosure`, `clientTransfer` and `loanApproval`.
 *
 * This one cannot recur: the fork's [LoanDisburseInput] declares `locale` and `dateFormat` as
 * REQUIRED, non-defaulted parameters, so the pairing has to be made at the call site where the
 * date string is produced. A payload that defaults its own format is what let the other three
 * drift — the field was filled in correctly and the date was not.
 *
 * ## The template read and the write are separate, as everywhere else in this feature
 *
 * The original drove both through one six-case `uiState`, so a disbursement in flight replaced the
 * form with a progress bar and a failure replaced it with a full-screen error — losing the amount
 * and note the officer had typed. The read is a [ScreenState]; the write's outcome is a dialog over
 * the still-filled form.
 */
internal class LoanDisbursementViewModel(
    repository: LoanRepository,
    private val commands: LoanCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanDisbursementState, LoanDisbursementEvent, LoanDisbursementAction>(
    LoanDisbursementState(loanId = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1),
) {

    private val stream = repository.disburseTemplateStream(state.loanId, viewModelScope)

    /** Fineract's expected net disbursal amount and the tenant's payment types. */
    val template: StateFlow<ScreenState<LoanDisbursementTemplate>> = stream.state
        .mapContent { template, _ ->
            LoanDisbursementTemplate(
                netDisbursalAmount = template.netDisbursalAmount,
                currencyCode = template.currency.code,
                decimalPlaces = template.currency.decimalPlaces,
                paymentTypes = template.paymentTypeOptions.mapNotNull { option ->
                    // A payment type with no id cannot be sent, so it is not offered. The
                    // original mapped straight to names and resolved the id back by INDEX, which
                    // meant a nameless or idless option silently shifted every later selection.
                    option.id?.let { DisbursePaymentType(id = it, name = option.name.orEmpty()) }
                },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: LoanDisbursementAction) {
        when (action) {
            LoanDisbursementAction.NavigateBack -> sendEvent(LoanDisbursementEvent.NavigateBack)
            LoanDisbursementAction.OnRetry -> stream.retry()

            is LoanDisbursementAction.UpdateAmount ->
                mutableStateFlow.value = state.copy(amount = action.value, amountError = false)

            is LoanDisbursementAction.UpdateNote ->
                mutableStateFlow.value = state.copy(note = action.value)

            is LoanDisbursementAction.UpdateDate ->
                mutableStateFlow.value = state.copy(date = action.millis)

            is LoanDisbursementAction.UpdateDatePicker ->
                mutableStateFlow.value = state.copy(showDatePicker = action.status)

            is LoanDisbursementAction.SelectPaymentType -> mutableStateFlow.value = state.copy(
                paymentTypeId = action.option.id,
                paymentTypeName = action.option.name,
            )

            is LoanDisbursementAction.Prefilled ->
                mutableStateFlow.value = state.copy(prefilled = true, amount = action.amount)

            LoanDisbursementAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            LoanDisbursementAction.OnSubmit -> disburse()
        }
    }

    private fun disburse() {
        if (state.isSubmitting) return
        val amount = state.amount.trim().toDoubleOrNull()
        // The original's `isFieldValid` returned false with both of its Toasts commented out, so a
        // blank or non-numeric amount made the button look broken.
        if (amount == null || amount <= 0) {
            mutableStateFlow.value = state.copy(amountError = true)
            return
        }

        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            val request = LoanDisburseInput(
                // Formatted by the helper that owns the two format fields below.
                actualDisbursementDate = ApiDateFormatter.formatForApi(state.date),
                transactionAmount = amount,
                // Null when unchosen, NOT 0. The original initialised `paymentTypeId` to 0 and
                // sent it, and payment type 0 does not exist — Fineract rejects it, or worse,
                // accepts it against whatever row happens to have that id.
                paymentTypeId = state.paymentTypeId,
                note = state.note.takeIf { it.isNotBlank() },
                locale = ApiDateFormatter.LOCALE,
                dateFormat = ApiDateFormatter.DATE_FORMAT,
            )

            when (val result = commands.disburse(state.loanId, request)) {
                is MutationResult.Applied -> {
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = LoanDisbursementState.DialogState.ShowStatusDialog(
                            ResultStatus.SUCCESS,
                        ),
                    )
                }

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanDisbursementState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        // A disbursement is OnlineRequired and has NO offline path: unlike a
                        // repayment, the money has not moved yet, so there is nothing captured
                        // that would be lost by refusing. Telling the officer to find signal is
                        // the correct outcome.
                        msg = if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "disburse failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = LoanDisbursementState.DialogState.ShowStatusDialog(
                            status = ResultStatus.FAILURE,
                            // "Disbursement date cannot be before the approval date" is the common
                            // one and names exactly what to change.
                            msg = result.cause.serverErrorMessage() ?: FAILED_MSG,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanDisbursementState.DialogState.ShowStatusDialog(
                        status = ResultStatus.FAILURE,
                        msg = FAILED_MSG,
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "LoanDisbursement"
        const val LOAN_ID_KEY = "loanId"
        const val STATE_TIMEOUT_MS = 5_000L

        const val OFFLINE_MSG = "No connection — the disbursement was not sent."
        const val FAILED_MSG = "Could not disburse this loan."
    }
}

/** What Fineract expects of the disbursement, used to seed the form once. */
internal data class LoanDisbursementTemplate(
    val netDisbursalAmount: Double,
    val currencyCode: String?,
    val decimalPlaces: Int?,
    val paymentTypes: List<DisbursePaymentType>,
)

/** A selectable payment type: the id the request needs, and the name the dropdown shows. */
internal data class DisbursePaymentType(val id: Int, val name: String)

@OptIn(ExperimentalTime::class)
internal data class LoanDisbursementState(
    val loanId: Int = -1,
    val amount: String = "",
    val note: String = "",
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val paymentTypeId: Int? = null,
    val paymentTypeName: String = "",
    val prefilled: Boolean = false,
    val amountError: Boolean = false,
    val showDatePicker: Boolean = false,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
) {
    sealed interface DialogState {
        data class ShowStatusDialog(val status: ResultStatus, val msg: String = "") : DialogState
    }
}

internal sealed interface LoanDisbursementEvent {
    data object NavigateBack : LoanDisbursementEvent
}

internal sealed interface LoanDisbursementAction {
    data object NavigateBack : LoanDisbursementAction
    data object OnRetry : LoanDisbursementAction
    data object OnSubmit : LoanDisbursementAction
    data object CloseDialog : LoanDisbursementAction
    data class UpdateAmount(val value: String) : LoanDisbursementAction
    data class UpdateNote(val value: String) : LoanDisbursementAction
    data class UpdateDate(val millis: Long) : LoanDisbursementAction
    data class UpdateDatePicker(val status: Boolean) : LoanDisbursementAction
    data class SelectPaymentType(val option: DisbursePaymentType) : LoanDisbursementAction

    /** Seeds the amount from the template, exactly once. */
    data class Prefilled(val amount: String) : LoanDisbursementAction
}
