/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.savings.SavingsAccountRepository
import kpt.core.data.savings.SavingsCommandRepository
import kpt.core.model.objects.account.saving.SavingsTransactionInput
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ApiDateFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Deposit or withdraw on a savings account — ported from
 * `6b66e8a43:feature/savings/savingsAccountTransaction/` (a 532-line screen + a 146-line
 * ViewModel).
 *
 * The money-moving write of the savings vertical, and the one the summary's two buttons open.
 *
 * ## `toFloat()` for money, and a negative amount passed validation
 *
 * ```kotlin
 * if (amount.toFloat() == 0f) { showSnackbar(...); return false }
 * ```
 *
 * Two defects in one predicate. **Float** carries ~7 significant digits, so a ten-million deposit
 * became `1.0E7` and lost its cents. And the only rejected value is exactly zero — `"-500"` parses
 * to `-500f`, is not `0f`, and submits a NEGATIVE deposit. Validation here is "parses as a decimal
 * AND is greater than zero", on `toBigDecimal`-free `toDoubleOrNull`, with the officer's own string
 * sent on the wire so a round-trip cannot change the figure they typed.
 *
 * ## An unchosen payment type was sent as `0`
 *
 * `paymentTypeId` defaulted to the Int `0` and went out as `paymentTypeId.toString()` — an id that
 * names no row. It is omitted when unchosen, like every other optional reference in this port.
 *
 * ## The template read must not block the write
 *
 * The payment-type options come from the transaction template, which is a NETWORK read. If it
 * fails, the form still submits: Fineract accepts a transaction with no payment type, and refusing
 * to let an officer record cash they have already physically taken because a reference list did
 * not load is the worse failure. The original had no such read on this screen at all — its
 * `paymentType` was a plain string the officer typed, while `paymentTypeId` stayed `0`, so the
 * LABEL and the ID were unrelated values.
 *
 * ## The request body was logged
 *
 * `Logger.d("builtTransactionRequestAsJson", Throwable(json))` serialized the whole request —
 * amount included — and logged it wrapped in a `Throwable`. Dropped: an amount in a log is a
 * financial detail in a crash report, and the `Throwable` wrapper was only there to smuggle a
 * string through a signature that wanted one.
 */
@OptIn(ExperimentalTime::class)
internal class SavingsTransactionViewModel(
    private val repository: SavingsAccountRepository,
    private val commands: SavingsCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SavingsTransactionState, SavingsTransactionEvent, SavingsTransactionAction>(
    SavingsTransactionState(
        accountType = savedStateHandle.get<String>(ACCOUNT_TYPE_KEY).orEmpty(),
        accountId = savedStateHandle.get<Int>(ACCOUNT_ID_KEY) ?: -1,
        kind = savedStateHandle.get<String>(KIND_KEY)
            ?.let { name -> TransactionKind.entries.firstOrNull { it.name == name } }
            ?: TransactionKind.DEPOSIT,
    ),
) {

    init {
        loadTemplate()
    }

    override fun handleAction(action: SavingsTransactionAction) {
        when (action) {
            SavingsTransactionAction.NavigateBack ->
                sendEvent(SavingsTransactionEvent.NavigateBack)

            SavingsTransactionAction.Done -> sendEvent(SavingsTransactionEvent.Done)
            SavingsTransactionAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            SavingsTransactionAction.OnRetryTemplate -> loadTemplate()

            is SavingsTransactionAction.OnAmountChanged ->
                mutableStateFlow.value =
                    state.copy(amount = action.value, amountError = null)

            is SavingsTransactionAction.OnNoteChanged ->
                mutableStateFlow.value = state.copy(note = action.value)

            is SavingsTransactionAction.OnPaymentTypeSelected ->
                mutableStateFlow.value = state.copy(paymentType = action.option)

            is SavingsTransactionAction.OnDateChanged ->
                mutableStateFlow.value = state.copy(date = action.millis)

            SavingsTransactionAction.OpenDatePicker ->
                mutableStateFlow.value = state.copy(isDatePickerOpen = true)

            SavingsTransactionAction.CloseDatePicker ->
                mutableStateFlow.value = state.copy(isDatePickerOpen = false)

            SavingsTransactionAction.OnReview -> review()
            SavingsTransactionAction.CloseReview ->
                mutableStateFlow.value = state.copy(isReviewing = false)

            SavingsTransactionAction.OnConfirm -> submit()
        }
    }

    private fun loadTemplate() {
        mutableStateFlow.value = state.copy(isLoadingTemplate = true, templateFailed = false)

        viewModelScope.launch {
            val template = repository.transactionTemplate(
                savingsAccountType = state.accountType,
                savingsAccountId = state.accountId,
                transactionType = state.kind.command,
            )

            mutableStateFlow.value = state.copy(
                isLoadingTemplate = false,
                // A FAILED read is reported but does NOT block the write — see the class KDoc.
                templateFailed = template == null,
                paymentTypes = template?.paymentTypeOptions.orEmpty().map {
                    PaymentTypeOption(id = it.id, name = it.name)
                },
            )
        }
    }

    /** Validate, then show the review sheet. The officer confirms from there. */
    private fun review() {
        val error = state.amount.validateAmount()
        if (error != null) {
            mutableStateFlow.value = state.copy(amountError = error)
            return
        }
        mutableStateFlow.value = state.copy(amountError = null, isReviewing = true)
    }

    private fun submit() {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isReviewing = false, isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            val result = commands.processTransaction(
                savingsAccountType = state.accountType,
                savingsAccountId = state.accountId,
                transactionType = state.kind.command,
                input = SavingsTransactionInput(
                    // The date and the format built together, by the helper that owns both. The
                    // original paired `DateHelper.getDateAsStringFromLong` with
                    // `DateHelper.SHORT_MONTH`, which DID agree — but only because both came from
                    // the same object; pairing them here is what keeps that true.
                    transactionDate = ApiDateFormatter.formatForApi(state.date),
                    // The officer's own string. A Double round-trip would re-render the figure.
                    transactionAmount = state.amount.trim(),
                    locale = ApiDateFormatter.LOCALE,
                    dateFormat = ApiDateFormatter.DATE_FORMAT,
                    // Omitted when unchosen, never `0`.
                    paymentTypeId = state.paymentType?.id,
                    note = state.note.trim().takeIf { it.isNotBlank() },
                ),
            )

            mutableStateFlow.value = when (result) {
                is MutationResult.Applied -> state.copy(
                    isSubmitting = false,
                    dialogState = TransactionDialog.Succeeded(result.value.resourceId),
                )

                is MutationResult.Blocked -> state.copy(
                    isSubmitting = false,
                    dialogState = TransactionDialog.Failed(
                        offline = result.reason == BlockReason.OFFLINE,
                        msg = null,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "${state.kind} failed" }
                    state.copy(
                        isSubmitting = false,
                        dialogState = TransactionDialog.Failed(
                            offline = false,
                            msg = result.cause.serverErrorMessage(),
                        ),
                    )
                }

                is MutationResult.Conflicted -> state.copy(
                    isSubmitting = false,
                    dialogState = TransactionDialog.Failed(offline = false, msg = null),
                )
            }
        }
    }

    private companion object {
        const val TAG = "SavingsTransaction"
        const val ACCOUNT_TYPE_KEY = "accountType"
        const val ACCOUNT_ID_KEY = "accountId"
        const val KIND_KEY = "kind"
    }
}

/**
 * Why an amount is not acceptable, or null.
 *
 * Deliberately NOT a boolean plus a snackbar: the original showed one of three snackbar messages
 * and left the field unmarked, so the officer had to remember which field the message was about.
 */
internal enum class AmountError { MISSING, NOT_A_NUMBER, NOT_POSITIVE }

internal fun String.validateAmount(): AmountError? {
    val trimmed = trim()
    // `toDoubleOrNull`, not `toFloat()` inside a try/catch: Float holds ~7 significant digits, so
    // a ten-million deposit lost its cents.
    val value = trimmed.toDoubleOrNull()
    return when {
        trimmed.isEmpty() -> AmountError.MISSING
        value == null -> AmountError.NOT_A_NUMBER
        // The original rejected ONLY exactly zero, so "-500" submitted a negative deposit.
        value > 0 -> null
        else -> AmountError.NOT_POSITIVE
    }
}

/**
 * Which way money moves. [command] is Fineract's `command` query parameter, verbatim.
 *
 * ONE enum, shared with the summary screen that opens this one. It briefly existed twice — once
 * here with the command value and once in `summary` without it — which is the same
 * two-same-named-constants trap as `DateHelper.SHORT_MONTH` vs `DateFormatPattern.SHORT_MONTH`
 * (logged as `two-short-month-constants-disagree`): identical names, different content, and
 * whichever one a call site imported decided whether the request had a command at all.
 */
internal enum class TransactionKind(val command: String) {
    DEPOSIT("deposit"),
    WITHDRAWAL("withdrawal"),
}

/** A selectable payment type. */
internal data class PaymentTypeOption(val id: Int, val name: String)

internal sealed interface TransactionDialog {
    /** [transactionId] is Fineract's new transaction id, shown as the officer's receipt number. */
    data class Succeeded(val transactionId: Int?) : TransactionDialog
    data class Failed(val offline: Boolean, val msg: String?) : TransactionDialog
}

@OptIn(ExperimentalTime::class)
internal data class SavingsTransactionState(
    val accountType: String = "",
    val accountId: Int = -1,
    val kind: TransactionKind = TransactionKind.DEPOSIT,

    val amount: String = "",
    val amountError: AmountError? = null,
    val note: String = "",
    val paymentType: PaymentTypeOption? = null,
    val paymentTypes: List<PaymentTypeOption> = emptyList(),
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val isDatePickerOpen: Boolean = false,

    val isLoadingTemplate: Boolean = false,
    /** Reported, but never a blocker — see [SavingsTransactionViewModel]. */
    val templateFailed: Boolean = false,
    val isReviewing: Boolean = false,
    val isSubmitting: Boolean = false,
    val dialogState: TransactionDialog? = null,
)

internal sealed interface SavingsTransactionEvent {
    data object NavigateBack : SavingsTransactionEvent

    /** The transaction landed — leave, so the summary behind reloads its balance. */
    data object Done : SavingsTransactionEvent
}

internal sealed interface SavingsTransactionAction {
    data object NavigateBack : SavingsTransactionAction
    data object Done : SavingsTransactionAction
    data object OnRetryTemplate : SavingsTransactionAction
    data object OnReview : SavingsTransactionAction
    data object CloseReview : SavingsTransactionAction
    data object OnConfirm : SavingsTransactionAction
    data object CloseDialog : SavingsTransactionAction
    data object OpenDatePicker : SavingsTransactionAction
    data object CloseDatePicker : SavingsTransactionAction
    data class OnAmountChanged(val value: String) : SavingsTransactionAction
    data class OnNoteChanged(val value: String) : SavingsTransactionAction
    data class OnPaymentTypeSelected(val option: PaymentTypeOption) : SavingsTransactionAction
    data class OnDateChanged(val millis: Long) : SavingsTransactionAction
}
