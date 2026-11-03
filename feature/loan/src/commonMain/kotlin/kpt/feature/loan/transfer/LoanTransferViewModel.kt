/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.transfer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.data.loan.LoanRepository
import kpt.core.model.objects.account.loan.transfer.AccountOption
import kpt.core.model.objects.account.loan.transfer.AccountTransferRequest
import kpt.core.model.objects.account.loan.transfer.AccountTypeOption
import kpt.core.model.objects.account.loan.transfer.ClientOption
import kpt.core.model.objects.account.loan.transfer.OfficeOption
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.components.ResultStatus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Transfer funds out of a loan account — ported from
 * `6b66e8a43:feature/loan/amountTransfer/{AmountTransferViewModel,Screen}.kt`.
 *
 * Reached from the loan PROFILE's primary button when the loan is OVERPAID. The loan SUMMARY
 * offers the same action and it was DEAD there — `NavigateToLoanTransfer` dispatched an event whose
 * handler was an empty `{ }` on both sides — so the same action worked on one screen and did
 * nothing on the other. Both are wired here.
 *
 * ## A cascading query, not a subscription
 *
 * Office → client → account type → account: each selection narrows the next by asking the server a
 * DIFFERENT question (`accounttransfers/template` with more `to*` parameters filled in). So the
 * template read is a suspend one-shot rather than a `ScreenDataStream` — see
 * [LoanRepository.accountTransferTemplate] for why a stream would leak one collector per
 * selection.
 *
 * ## FIXED: Retry crashed
 *
 * `submitTransfer` resolved four nullable state fields with `!!`:
 *
 * ```kotlin
 * toOfficeId = state.selectedOfficeId!!, toClientId = state.selectedClientId!!,
 * toAccountType = state.accountTypeId!!, toAccountId = state.accountId!!,
 * ```
 *
 * Safe from the happy path, because `validateFields()` checks all four before calling it — but
 * `OnRetryClick` called `submitTransfer()` **directly**, skipping validation. So retrying after a
 * failure threw. `state.amount.toDouble()` was unguarded on the same path.
 *
 * Now the four ids and the amount are resolved ONCE into a [TransferDraft], and `submit` takes
 * that draft — so retry re-sends a draft that was already proven complete, and there is no path on
 * which a null reaches the payload. `AccountTransferRequest`'s fields are required now too, so the
 * compiler enforces it.
 *
 * ## FIXED: two selections resolved by INDEX
 *
 * `OnOfficeChanged(index)` and `OnClientChange(index)` looked their id up as
 * `state.offices.getOrNull(index)?.id` — while `OnAccountTypeChange` and `OnAccountChange` carried
 * the id directly. The lists are re-sorted on every template fetch, so an index captured before a
 * refetch could resolve to a different office. All four carry the option now.
 */
internal class LoanTransferViewModel(
    private val repository: LoanRepository,
    private val commands: LoanCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanTransferState, LoanTransferEvent, LoanTransferAction>(
    LoanTransferState(fromLoanId = savedStateHandle.get<Int>(LOAN_ID_KEY) ?: -1),
) {

    private val loanStream = repository.loanStream(state.fromLoanId, viewModelScope)

    init {
        // The FROM side is READ, not passed in. The original's route carried five fields — client
        // id, office id, account number, currency, account id — snapshotted by the caller from the
        // loan it had just rendered. Reading the loan here instead means the route is one id (the
        // same simplification the approval screen got), and it costs nothing: whichever detail
        // screen sent the officer here has just populated that cache.
        //
        // The template read DEPENDS on those values, so it cannot run until the loan lands —
        // which is why this is a collector rather than a straight call.
        viewModelScope.launch {
            loanStream.state
                .mapNotNull { (it as? ScreenState.Content)?.data }
                .distinctUntilChangedBy { it.id }
                .collect { loan ->
                    mutableStateFlow.value = state.copy(
                        fromClientId = loan.clientId ?: -1,
                        fromOfficeId = loan.clientOfficeId ?: -1,
                        fromAccountNumber = loan.accountNo.orEmpty(),
                        currency = loan.currency?.code.orEmpty(),
                    )
                    loadTemplate()
                }
        }
    }

    override fun handleAction(action: LoanTransferAction) {
        when (action) {
            LoanTransferAction.NavigateBack -> sendEvent(LoanTransferEvent.NavigateBack)
            LoanTransferAction.OnRetryFetching -> {
                // BOTH reads: the loan may be what failed, and without it the template read has no
                // from-side to ask about.
                loanStream.retry()
                loadTemplate()
            }
            LoanTransferAction.CloseDialog -> mutableStateFlow.value = state.copy(dialogState = null)

            is LoanTransferAction.OnAmountChange ->
                mutableStateFlow.value = state.copy(amount = action.amount, amountError = null)

            is LoanTransferAction.OnDescriptionChange ->
                mutableStateFlow.value = state.copy(
                    description = action.description,
                    descriptionError = null,
                )

            // Each selection CLEARS the levels below it, because the server's answer for those is
            // about to change. The original did this for office and client but NOT for account
            // type — so changing the account type left the previously-chosen ACCOUNT selected,
            // and that account may not exist under the new type.
            is LoanTransferAction.OnOfficeSelected -> {
                mutableStateFlow.value = state.copy(
                    office = action.option,
                    officeError = null,
                    client = null,
                    accountType = null,
                    account = null,
                    clients = emptyList(),
                    accountTypes = emptyList(),
                    accounts = emptyList(),
                )
                loadTemplate()
            }

            is LoanTransferAction.OnClientSelected -> {
                mutableStateFlow.value = state.copy(
                    client = action.option,
                    clientError = null,
                    accountType = null,
                    account = null,
                    accountTypes = emptyList(),
                    accounts = emptyList(),
                )
                loadTemplate()
            }

            is LoanTransferAction.OnAccountTypeSelected -> {
                mutableStateFlow.value = state.copy(
                    accountType = action.option,
                    accountTypeError = null,
                    account = null,
                    accounts = emptyList(),
                )
                loadTemplate()
            }

            is LoanTransferAction.OnAccountSelected ->
                mutableStateFlow.value = state.copy(account = action.option, accountError = null)

            LoanTransferAction.OnSubmit -> validateThenSubmit()

            // Retry re-sends the draft that was already proven complete, rather than re-reading
            // nullable state — see the header for the crash that was.
            LoanTransferAction.OnRetrySubmit -> state.draft?.let(::submit)
        }
    }

    private fun loadTemplate() {
        // Nothing to ask until the loan has told us who is transferring FROM.
        if (state.fromClientId <= 0) return
        mutableStateFlow.value = state.copy(isLoadingTemplate = true, templateError = false)

        viewModelScope.launch {
            val template = repository.accountTransferTemplate(
                fromClientId = state.fromClientId,
                fromAccountId = state.fromLoanId,
                fromAccountType = LOAN_ACCOUNT_TYPE,
                fromOfficeId = state.fromOfficeId,
                // Only what has been chosen so far — the cascade's whole shape.
                toOfficeId = state.office?.id,
                toClientId = state.client?.id,
                toAccountType = state.accountType?.id,
            )

            mutableStateFlow.value = if (template == null) {
                state.copy(isLoadingTemplate = false, templateError = true)
            } else {
                state.copy(
                    isLoadingTemplate = false,
                    templateError = false,
                    // Sorted as the original sorted them, and every option with a null id is
                    // dropped: it cannot be sent, so it must not be selectable.
                    offices = template.toOfficeOptions.filter { it.id != null }.sortedBy { it.name },
                    clients = template.toClientOptions.filter { it.id != null }
                        .sortedBy { it.displayName },
                    accountTypes = template.toAccountTypeOptions.filter { it.id != null }
                        .sortedBy { it.value },
                    accounts = template.toAccountOptions.filter { it.id != null }
                        .sortedBy { it.accountNo },
                )
            }
        }
    }

    /**
     * Validate every field, reporting ALL failures at once, then submit.
     *
     * The original's `validateFields` also collected every error — kept, because a form with six
     * required fields that reports one error per tap is six taps to discover what it wants.
     */
    private fun validateThenSubmit() {
        if (state.isSubmitting) return

        val next = state.withFieldErrors()
        mutableStateFlow.value = next
        if (!next.hasFieldError) next.toDraft()?.let(::submit)
    }

    private fun submit(draft: TransferDraft) {
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null, draft = draft)

        viewModelScope.launch {
            val request = AccountTransferRequest(
                fromOfficeId = state.fromOfficeId,
                fromClientId = state.fromClientId,
                fromAccountType = LOAN_ACCOUNT_TYPE,
                fromAccountId = state.fromLoanId,
                toOfficeId = draft.toOfficeId,
                toClientId = draft.toClientId,
                toAccountType = draft.toAccountType,
                toAccountId = draft.toAccountId,
                // The original paired `DateHelper.formattedFullDate` with
                // `Constants.DATE_FORMAT_LONG` — both "dd MMMM yyyy", so this screen's date was
                // one of only two in the feature that worked. Built from `ApiDateFormatter` now,
                // so the pairing is held in one place rather than in two constants that happened
                // to agree.
                transferDate = ApiDateFormatter.formatForApi(draft.transferDate),
                transferAmount = draft.amount,
                transferDescription = draft.description,
                dateFormat = ApiDateFormatter.DATE_FORMAT,
                locale = ApiDateFormatter.LOCALE,
            )

            when (val result = commands.submitAccountTransfer(request)) {
                is MutationResult.Applied -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanTransferState.DialogState.Result(ResultStatus.SUCCESS),
                )

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanTransferState.DialogState.Result(
                        status = ResultStatus.FAILURE,
                        // OnlineRequired with no offline path: a transfer moves money between two
                        // accounts and nothing has been captured that refusing would lose.
                        msg = if (result.reason == BlockReason.OFFLINE) OFFLINE_MSG else FAILED_MSG,
                        retryable = true,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "transfer failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        dialogState = LoanTransferState.DialogState.Result(
                            status = ResultStatus.FAILURE,
                            msg = result.cause.serverErrorMessage() ?: FAILED_MSG,
                            retryable = true,
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialogState = LoanTransferState.DialogState.Result(
                        status = ResultStatus.FAILURE,
                        msg = FAILED_MSG,
                        retryable = true,
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "LoanTransfer"
        const val LOAN_ID_KEY = "loanId"

        /** Fineract's `portfolioAccountType` for a loan. The original's route defaulted it to 1. */
        const val LOAN_ACCOUNT_TYPE = 1

        const val OFFLINE_MSG = "No connection — the transfer was not sent."
        const val FAILED_MSG = "Could not complete this transfer."
    }
}

/**
 * A transfer proven complete, resolved once at validation.
 *
 * Exists so the submit path — and RETRY especially — never re-reads nullable state. The original
 * read six nullable fields at submit time with `!!`, and its retry skipped the validation that
 * made them safe.
 */
internal data class TransferDraft(
    val toOfficeId: Int,
    val toClientId: Int,
    val toAccountType: Int,
    val toAccountId: Int,
    val amount: Double,
    val description: String,
    val transferDate: Long,
)

/** The six field-level failures, as a closed set rather than six `StringResource`s in state. */
internal enum class TransferFieldError {
    AMOUNT_INVALID,
    AMOUNT_NOT_POSITIVE,
    DESCRIPTION_REQUIRED,
    OFFICE_REQUIRED,
    CLIENT_REQUIRED,
    ACCOUNT_TYPE_REQUIRED,
    ACCOUNT_REQUIRED,
}

internal data class LoanTransferState(
    // The FROM side, from the route.
    val fromLoanId: Int = -1,
    val fromClientId: Int = -1,
    val fromOfficeId: Int = -1,
    val fromAccountNumber: String = "",
    val currency: String = "",

    // The TO side, chosen by the officer.
    val office: OfficeOption? = null,
    val client: ClientOption? = null,
    val accountType: AccountTypeOption? = null,
    val account: AccountOption? = null,
    val amount: String = "",
    val description: String = "",

    // What the cascade currently offers.
    val offices: List<OfficeOption> = emptyList(),
    val clients: List<ClientOption> = emptyList(),
    val accountTypes: List<AccountTypeOption> = emptyList(),
    val accounts: List<AccountOption> = emptyList(),

    val isLoadingTemplate: Boolean = false,
    val templateError: Boolean = false,
    val isSubmitting: Boolean = false,
    val draft: TransferDraft? = null,
    val dialogState: DialogState? = null,

    val amountError: TransferFieldError? = null,
    val descriptionError: TransferFieldError? = null,
    val officeError: TransferFieldError? = null,
    val clientError: TransferFieldError? = null,
    val accountTypeError: TransferFieldError? = null,
    val accountError: TransferFieldError? = null,
) {
    val hasFieldError: Boolean
        get() = listOf(
            amountError,
            descriptionError,
            officeError,
            clientError,
            accountTypeError,
            accountError,
        ).any { it != null }

    sealed interface DialogState {
        /** [retryable] drives whether the dialog offers Retry — only a failed SUBMIT does. */
        data class Result(
            val status: ResultStatus,
            val msg: String = "",
            val retryable: Boolean = false,
        ) : DialogState
    }
}

internal sealed interface LoanTransferEvent {
    data object NavigateBack : LoanTransferEvent
}

internal sealed interface LoanTransferAction {
    data object NavigateBack : LoanTransferAction
    data object OnSubmit : LoanTransferAction
    data object OnRetrySubmit : LoanTransferAction
    data object OnRetryFetching : LoanTransferAction
    data object CloseDialog : LoanTransferAction
    data class OnAmountChange(val amount: String) : LoanTransferAction
    data class OnDescriptionChange(val description: String) : LoanTransferAction
    data class OnOfficeSelected(val option: OfficeOption) : LoanTransferAction
    data class OnClientSelected(val option: ClientOption) : LoanTransferAction
    data class OnAccountTypeSelected(val option: AccountTypeOption) : LoanTransferAction
    data class OnAccountSelected(val option: AccountOption) : LoanTransferAction
}

private fun LoanTransferState.withFieldErrors(): LoanTransferState {
    val parsed = amount.trim().toDoubleOrNull()
    return copy(
        amountError = when {
            parsed == null -> TransferFieldError.AMOUNT_INVALID
            parsed <= 0.0 -> TransferFieldError.AMOUNT_NOT_POSITIVE
            else -> null
        },
        descriptionError = TransferFieldError.DESCRIPTION_REQUIRED.takeIf { description.isBlank() },
        officeError = TransferFieldError.OFFICE_REQUIRED.takeIf { office == null },
        clientError = TransferFieldError.CLIENT_REQUIRED.takeIf { client == null },
        accountTypeError = TransferFieldError.ACCOUNT_TYPE_REQUIRED.takeIf { accountType == null },
        accountError = TransferFieldError.ACCOUNT_REQUIRED.takeIf { account == null },
    )
}

/**
 * Resolved ONCE, AFTER the errors are known to be clear. Null-safe rather than `!!`: the
 * validation makes the null unreachable, and an unreachable `!!` is still the thing that crashed
 * the original's retry path.
 */
@OptIn(ExperimentalTime::class)
private fun LoanTransferState.toDraft(): TransferDraft? {
    val ids = listOfNotNull(office?.id, client?.id, accountType?.id, account?.id)
        .takeIf { it.size == TRANSFER_TARGET_ID_COUNT }
    val parsed = amount.trim().toDoubleOrNull()
    return if (ids == null || parsed == null) {
        null
    } else {
        TransferDraft(
            toOfficeId = ids[0],
            toClientId = ids[1],
            toAccountType = ids[2],
            toAccountId = ids[3],
            amount = parsed,
            description = description.trim(),
            transferDate = Clock.System.now().toEpochMilliseconds(),
        )
    }
}

private const val TRANSFER_TARGET_ID_COUNT = 4
