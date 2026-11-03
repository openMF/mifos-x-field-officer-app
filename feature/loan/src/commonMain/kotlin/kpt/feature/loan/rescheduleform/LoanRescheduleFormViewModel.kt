/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.rescheduleform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.data.loan.LoanRepository
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleRequest
import kpt.core.model.objects.account.loan.reschedules.RescheduleReasonOption
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ApiDateFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Propose a reschedule — ported from
 * `6b66e8a43:feature/loan/createLoanReschedules/{LoanRescheduleFormViewModel,Screen}.kt`.
 *
 * The reschedules list's only action, and the reason its "+" was not drawn.
 *
 * ## Five optional changes, each gated by its own checkbox
 *
 * Change repayment date · introduce grace periods · extend repayment period · adjust interest rate
 * · waive penalties. Unchecking one CLEARS its fields, which is the original's behaviour and is
 * right: a value left behind by an unchecked option is a value the officer cannot see and did not
 * mean.
 *
 * ## FIXED: unselected options were sent as EMPTY STRINGS, not omitted
 *
 * ```kotlin
 * adjustedDueDate = if (state.changeRepaymentDateSelected) state.adjustedDueDate else "",
 * graceOnPrincipal = if (state.introduceGracePeriodsSelected) state.graceOnPrincipal else "",
 * ```
 *
 * Every one of the five was `""` when unchecked. `LoanRescheduleRequest` declares them nullable
 * with null defaults — `null` means "not part of this proposal", `""` means "here is a date/number,
 * and it is the empty string". Fineract then tries to parse `""` as a date against the declared
 * `dateFormat`. Omitted now.
 *
 * ## FIXED: the four numeric fields were never validated
 *
 * `graceOnPrincipal`, `graceOnInterest`, `extraTerms` and `newInterestRate` were collected as free
 * text and sent as strings — `isSubmitEnabled` only checked they were non-blank. "two" reached the
 * server. Each is checked for a non-negative number before submit, and the error names the field.
 *
 * ## Dates are MILLIS in state, formatted at submit
 *
 * The original stored them pre-formatted (`rescheduleFromDate: String =
 * ApiDateFormatter.formatForApi(now)`), so there was nothing for the picker to re-open at and the
 * only representation was the wire one. Same change as the approval screen's.
 */
internal class LoanRescheduleFormViewModel(
    private val repository: LoanRepository,
    private val commands: LoanCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanRescheduleFormState, LoanRescheduleFormEvent, LoanRescheduleFormAction>(
    LoanRescheduleFormState(loanId = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1),
) {

    init {
        loadReasons()
    }

    override fun handleAction(action: LoanRescheduleFormAction) {
        when (action) {
            LoanRescheduleFormAction.NavigateBack -> sendEvent(LoanRescheduleFormEvent.NavigateBack)
            LoanRescheduleFormAction.OnRetryFetching -> loadReasons()
            LoanRescheduleFormAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            is LoanRescheduleFormAction.OnReasonSelected -> mutableStateFlow.value = state.copy(
                reason = action.option,
                errors = state.errors - RescheduleField.REASON,
            )

            is LoanRescheduleFormAction.OnDateChanged ->
                mutableStateFlow.value = state.withDate(action.field, action.millis)

            is LoanRescheduleFormAction.OpenDatePicker ->
                mutableStateFlow.value = state.copy(activeDatePicker = action.field)

            LoanRescheduleFormAction.CloseDatePicker ->
                mutableStateFlow.value = state.copy(activeDatePicker = null)

            is LoanRescheduleFormAction.OnCommentsChanged ->
                mutableStateFlow.value = state.copy(comments = action.value)

            // Unchecking CLEARS the option's fields — a value left behind by an unchecked option
            // is one the officer cannot see and did not mean. The original's behaviour.
            is LoanRescheduleFormAction.OnToggle -> mutableStateFlow.value = state.toggle(action)

            is LoanRescheduleFormAction.OnNumberChanged -> mutableStateFlow.value = state.copy(
                numbers = state.numbers + (action.field to action.value),
                errors = state.errors - action.field,
            )

            LoanRescheduleFormAction.OnSubmit -> validateThenSubmit()
            LoanRescheduleFormAction.OnRetrySubmit -> state.draft?.let(::submit)
        }
    }

    private fun loadReasons() {
        mutableStateFlow.value = state.copy(isLoadingReasons = true, reasonsError = false)

        viewModelScope.launch {
            val template = repository.loanRescheduleReasons()

            mutableStateFlow.value = if (template == null) {
                state.copy(isLoadingReasons = false, reasonsError = true)
            } else {
                state.copy(
                    isLoadingReasons = false,
                    reasonsError = false,
                    reasons = template.rescheduleReasons.sortedBy { it.name },
                )
            }
        }
    }

    private fun validateThenSubmit() {
        if (state.isSubmitting) return

        val errors = validationErrors()
        mutableStateFlow.value = state.copy(errors = errors)
        if (errors.isEmpty()) buildDraft()?.let(::submit)
    }

    private fun validationErrors(): Set<RescheduleField> = buildSet {
        if (state.reason == null) add(RescheduleField.REASON)
        if (state.changeRepaymentDate && state.adjustedDueDate == null) {
            add(RescheduleField.ADJUSTED_DUE_DATE)
        }
        // Each ENABLED numeric option must carry a usable number. The original only checked
        // non-blankness, so "two" was submitted as-is.
        if (state.introduceGracePeriods) addAll(graceErrors())
        if (state.extendRepaymentPeriod) {
            val terms = state.numbers[RescheduleField.EXTRA_TERMS].orEmpty()
            if (!terms.isNonNegativeNumber()) add(RescheduleField.EXTRA_TERMS)
        }
        if (state.adjustInterestRate) {
            val rate = state.numbers[RescheduleField.NEW_INTEREST_RATE].orEmpty()
            if (!rate.isNonNegativeNumber()) add(RescheduleField.NEW_INTEREST_RATE)
        }
    }

    private fun graceErrors(): Set<RescheduleField> = buildSet {
        val principal = state.numbers[RescheduleField.GRACE_ON_PRINCIPAL].orEmpty()
        val interest = state.numbers[RescheduleField.GRACE_ON_INTEREST].orEmpty()
        // At least ONE of the two, as the original required — a grace period on principal
        // alone is a legitimate proposal.
        if (principal.isBlank() && interest.isBlank()) {
            add(RescheduleField.GRACE_ON_PRINCIPAL)
        }
        if (principal.isNotBlank() && !principal.isNonNegativeNumber()) {
            add(RescheduleField.GRACE_ON_PRINCIPAL)
        }
        if (interest.isNotBlank() && !interest.isNonNegativeNumber()) {
            add(RescheduleField.GRACE_ON_INTEREST)
        }
    }

    /**
     * Resolved once, after the errors are known clear — the same shape as the transfer's
     * draft, and for the same reason: retry must not re-read nullable state.
     */
    private fun buildDraft(): RescheduleDraft? {
        val reasonId = state.reason?.id ?: return null
        return RescheduleDraft(
            reasonId = reasonId,
            rescheduleFrom = state.rescheduleFrom,
            submittedOn = state.submittedOn,
            comments = state.comments.trim().takeIf { it.isNotBlank() },
            // Each option contributes its value ONLY when checked, and null otherwise — never the
            // empty string the original sent.
            adjustedDueDate = state.adjustedDueDate.takeIf { state.changeRepaymentDate },
            graceOnPrincipal = state.numberIf(
                RescheduleField.GRACE_ON_PRINCIPAL,
                state.introduceGracePeriods,
            ),
            graceOnInterest = state.numberIf(
                RescheduleField.GRACE_ON_INTEREST,
                state.introduceGracePeriods,
            ),
            extraTerms = state.numberIf(RescheduleField.EXTRA_TERMS, state.extendRepaymentPeriod),
            newInterestRate = state.numberIf(
                RescheduleField.NEW_INTEREST_RATE,
                state.adjustInterestRate,
            ),
            // `true` or omitted, never `false` — Fineract treats the field's presence as the
            // instruction. The original's `if (selected) true else null`, kept.
            waivePenalties = true.takeIf { state.waivePenalties },
        )
    }

    private fun submit(draft: RescheduleDraft) {
        mutableStateFlow.value = state.copy(isSubmitting = true, draft = draft, dialogState = null)

        viewModelScope.launch {
            val request = LoanRescheduleRequest(
                loanId = state.loanId,
                rescheduleFromDate = ApiDateFormatter.formatForApi(draft.rescheduleFrom),
                rescheduleReasonId = draft.reasonId,
                submittedOnDate = ApiDateFormatter.formatForApi(draft.submittedOn),
                dateFormat = ApiDateFormatter.DATE_FORMAT,
                locale = ApiDateFormatter.LOCALE,
                graceOnPrincipal = draft.graceOnPrincipal,
                graceOnInterest = draft.graceOnInterest,
                extraTerms = draft.extraTerms,
                adjustedDueDate = draft.adjustedDueDate?.let { ApiDateFormatter.formatForApi(it) },
                newInterestRate = draft.newInterestRate,
                rescheduleReasonComment = draft.comments,
                waivePenalties = draft.waivePenalties,
            )

            when (val result = commands.submitLoanReschedule(request)) {
                is MutationResult.Applied -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanRescheduleFormState.DialogState.Submitted,
                )

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanRescheduleFormState.DialogState.Failed(
                        msg = if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                        retryable = true,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "propose reschedule failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = LoanRescheduleFormState.DialogState.Failed(
                            // "loan reschedule request date should be after the last repayment" is
                            // the common one.
                            msg = result.cause.serverErrorMessage() ?: FAILED_MSG,
                            retryable = true,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanRescheduleFormState.DialogState.Failed(
                        msg = FAILED_MSG,
                        retryable = true,
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "LoanRescheduleForm"
        const val LOAN_ID_KEY = "loanId"

        const val OFFLINE_MSG = "No connection — the reschedule was not proposed."
        const val FAILED_MSG = "Could not propose this reschedule."
    }
}

/** Non-negative, and parseable. A negative grace period or term count is not a proposal. */
private fun String.isNonNegativeNumber(): Boolean =
    trim().toDoubleOrNull()?.let { it >= 0 } == true

/** The five optional changes, toggled by checkbox. Unchecking clears the option's own fields. */
private fun LoanRescheduleFormState.toggle(
    action: LoanRescheduleFormAction.OnToggle,
): LoanRescheduleFormState = when (action.option) {
    RescheduleOption.CHANGE_REPAYMENT_DATE -> copy(
        changeRepaymentDate = action.selected,
        adjustedDueDate = adjustedDueDate.takeIf { action.selected },
        errors = errors - RescheduleField.ADJUSTED_DUE_DATE,
    )

    RescheduleOption.INTRODUCE_GRACE_PERIODS -> copy(
        introduceGracePeriods = action.selected,
        numbers = if (action.selected) {
            numbers
        } else {
            numbers - RescheduleField.GRACE_ON_PRINCIPAL - RescheduleField.GRACE_ON_INTEREST
        },
        errors = errors - RescheduleField.GRACE_ON_PRINCIPAL - RescheduleField.GRACE_ON_INTEREST,
    )

    RescheduleOption.EXTEND_REPAYMENT_PERIOD -> copy(
        extendRepaymentPeriod = action.selected,
        numbers = if (action.selected) numbers else numbers - RescheduleField.EXTRA_TERMS,
        errors = errors - RescheduleField.EXTRA_TERMS,
    )

    RescheduleOption.ADJUST_INTEREST_RATE -> copy(
        adjustInterestRate = action.selected,
        numbers = if (action.selected) numbers else numbers - RescheduleField.NEW_INTEREST_RATE,
        errors = errors - RescheduleField.NEW_INTEREST_RATE,
    )

    // No field of its own — the checkbox IS the value.
    RescheduleOption.WAIVE_PENALTIES -> copy(waivePenalties = action.selected)
}

/** A proposal proven complete, so the submit path and RETRY never re-read nullable state. */
internal data class RescheduleDraft(
    val reasonId: Int,
    val rescheduleFrom: Long,
    val submittedOn: Long,
    val comments: String?,
    val adjustedDueDate: Long?,
    val graceOnPrincipal: String?,
    val graceOnInterest: String?,
    val extraTerms: String?,
    val newInterestRate: String?,
    val waivePenalties: Boolean?,
)

/** The five optional changes. */
internal enum class RescheduleOption {
    CHANGE_REPAYMENT_DATE,
    INTRODUCE_GRACE_PERIODS,
    EXTEND_REPAYMENT_PERIOD,
    ADJUST_INTEREST_RATE,
    WAIVE_PENALTIES,
}

/** The three dates the form collects. */
internal enum class RescheduleDate { FROM, SUBMITTED_ON, ADJUSTED_DUE }

/** Every field that can carry a validation error, named so the error can sit on it. */
internal enum class RescheduleField {
    REASON,
    ADJUSTED_DUE_DATE,
    GRACE_ON_PRINCIPAL,
    GRACE_ON_INTEREST,
    EXTRA_TERMS,
    NEW_INTEREST_RATE,
}

@OptIn(ExperimentalTime::class)
internal data class LoanRescheduleFormState(
    val loanId: Int = -1,
    val reasons: List<RescheduleReasonOption> = emptyList(),
    val reason: RescheduleReasonOption? = null,
    /** Millis, not a pre-formatted string — see the ViewModel's header. */
    val rescheduleFrom: Long = Clock.System.now().toEpochMilliseconds(),
    val submittedOn: Long = Clock.System.now().toEpochMilliseconds(),
    val adjustedDueDate: Long? = null,
    val comments: String = "",

    val changeRepaymentDate: Boolean = false,
    val introduceGracePeriods: Boolean = false,
    val extendRepaymentPeriod: Boolean = false,
    val adjustInterestRate: Boolean = false,
    val waivePenalties: Boolean = false,

    /** The four numeric fields, keyed so the screen and the validation agree on the name. */
    val numbers: Map<RescheduleField, String> = emptyMap(),
    val errors: Set<RescheduleField> = emptySet(),

    val activeDatePicker: RescheduleDate? = null,
    val isLoadingReasons: Boolean = false,
    val reasonsError: Boolean = false,
    val isSubmitting: Boolean = false,
    val draft: RescheduleDraft? = null,
    val dialogState: DialogState? = null,
) {
    fun number(field: RescheduleField): String = numbers[field].orEmpty()

    /** The field's value when its option is checked, trimmed; null otherwise. */
    fun numberIf(field: RescheduleField, enabled: Boolean): String? =
        number(field).trim().takeIf { enabled && it.isNotBlank() }

    sealed interface DialogState {
        data object Submitted : DialogState

        /**
         * [retryable] drives whether the dialog's action is Retry.
         *
         * True for every failed SUBMIT, because [LoanRescheduleFormAction.OnRetrySubmit] re-sends
         * the already-validated [LoanRescheduleFormState.draft] rather than re-reading the form —
         * so a retry cannot send something different from what the officer confirmed.
         */
        data class Failed(val msg: String, val retryable: Boolean = false) : DialogState
    }
}

internal sealed interface LoanRescheduleFormEvent {
    data object NavigateBack : LoanRescheduleFormEvent
}

internal sealed interface LoanRescheduleFormAction {
    data object NavigateBack : LoanRescheduleFormAction
    data object OnRetryFetching : LoanRescheduleFormAction
    data object OnSubmit : LoanRescheduleFormAction
    data object OnRetrySubmit : LoanRescheduleFormAction
    data object CloseDialog : LoanRescheduleFormAction
    data object CloseDatePicker : LoanRescheduleFormAction
    data class OnReasonSelected(val option: RescheduleReasonOption) : LoanRescheduleFormAction
    data class OnDateChanged(val field: RescheduleDate, val millis: Long) : LoanRescheduleFormAction
    data class OpenDatePicker(val field: RescheduleDate) : LoanRescheduleFormAction
    data class OnCommentsChanged(val value: String) : LoanRescheduleFormAction
    data class OnToggle(val option: RescheduleOption, val selected: Boolean) : LoanRescheduleFormAction
    data class OnNumberChanged(val field: RescheduleField, val value: String) : LoanRescheduleFormAction
}

private fun LoanRescheduleFormState.withDate(field: RescheduleDate, millis: Long): LoanRescheduleFormState =
    when (field) {
        RescheduleDate.FROM -> copy(rescheduleFrom = millis)
        RescheduleDate.SUBMITTED_ON -> copy(submittedOn = millis)
        RescheduleDate.ADJUSTED_DUE -> copy(
            adjustedDueDate = millis,
            errors = errors - RescheduleField.ADJUSTED_DUE_DATE,
        )
    }
