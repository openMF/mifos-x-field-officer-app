/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.collectionsheet.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.store.screen.dataOrNull
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.format.formatDecimal
import kpt.core.data.collectionsheet.CollectionSheetCommandRepository
import kpt.core.data.collectionsheet.CollectionSheetRepository
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.collectionsheet.IndividualCollectionSheetPayload
import kpt.core.model.collectionsheet.RequestCollectionSheetPayload
import kpt.core.model.objects.account.loan.PaymentTypeOptions
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.shared.BulkRepaymentTransactions
import kpt.core.model.utils.ApiDateFormatter

/**
 * Ported from `6b66e8a43:feature/collectionSheet/.../IndividualCollectionSheetDetailsViewModel.kt`
 * and `PaymentDetailsViewModel.kt` — now ONE ViewModel, because the original's split lost data.
 *
 * ## The defect this fixes
 *
 * The original details screen held the payload in a `rememberSaveable`, and opened each loan's
 * payment screen with the payload SERIALISED INTO THE ROUTE as JSON. The payment screen decoded its
 * own copy and wrote the officer's amount and payment details into that copy — which nothing ever
 * read. Submit sent the details screen's untouched payload: every loan at its default amount, with
 * no payment type, whatever the officer had entered.
 *
 * Here this ViewModel owns one draft per loan ([CollectionSheetDetailsState.drafts]); the payment
 * editor is a sheet over this screen that writes into it, and Submit sends exactly what is drafted.
 *
 * ## Also fixed
 *  - Rows are LOANS, not clients. The original listed clients and read each one's loan as
 *    `client.loans[clientIndex]` — the client's position used to index that client's loans, which
 *    showed the wrong loan and threw for any client with fewer loans than its row number.
 *  - The account label printed the product name twice (`"$product (#$product)"`); it is the account
 *    number now, as the payment screen already had it.
 *  - `transactionDate` is sent. The original payload never set it, and Fineract requires it.
 *  - A failed submit keeps the sheet and says why (the server's own message); the original replaced
 *    the list with an error whose Retry did nothing.
 */
internal class CollectionSheetDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: CollectionSheetRepository,
    private val commands: CollectionSheetCommandRepository,
) : BaseViewModel<CollectionSheetDetailsState, CollectionSheetDetailsEvent, CollectionSheetDetailsAction>(
    CollectionSheetDetailsState(),
) {

    // Keys are CollectionSheetDetailsRoute's property names; read directly (not toRoute) so the
    // Android host tests, whose Bundle is a stub, see the same arguments as the device.
    private val request = RequestCollectionSheetPayload(
        dateFormat = ApiDateFormatter.DATE_FORMAT,
        locale = ApiDateFormatter.LOCALE,
        officeId = requireNotNull(savedStateHandle.get<Int>("officeId")),
        staffId = savedStateHandle.get<Int>("staffId"),
        transactionDate = requireNotNull(savedStateHandle.get<String>("transactionDate")),
    )

    private val stream = repository.individualCollectionSheetStream(request, viewModelScope)

    /** The sheet as rows, each with what will be collected for it — the server sheet merged with the drafts. */
    val sheet: StateFlow<ScreenState<CollectionSheetView>> = stream.state
        // Nothing due is EMPTY, not an empty list — and it is decided here, on content, never with
        // `asScreenStream(isEmpty = …)`, which means "not arrived yet" and would spin forever.
        .emptyIfContent { sheet -> sheet.clients.orEmpty().all { it.loans.isNullOrEmpty() } }
        .combineContent(stateFlow) { sheet, ui, _ -> sheet.toView(request.transactionDate, ui.drafts) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    fun onRetry() = stream.retry()

    override fun handleAction(action: CollectionSheetDetailsAction) {
        when (action) {
            is CollectionSheetDetailsAction.EditPayment -> openEditor(action.loanId)
            is CollectionSheetDetailsAction.EditorChanged ->
                mutableStateFlow.value = state.copy(
                    editor = state.editor?.let(action.change)?.copy(amountError = false),
                )
            CollectionSheetDetailsAction.SaveEditor -> saveEditor()
            CollectionSheetDetailsAction.CloseEditor -> mutableStateFlow.value = state.copy(editor = null)
            CollectionSheetDetailsAction.Submit -> submit()
            CollectionSheetDetailsAction.DismissDialog -> {
                val submitted = state.dialog == CollectionSheetDetailsState.Dialog.Submitted
                mutableStateFlow.value = state.copy(dialog = null)
                // A saved sheet is done: leave, so it cannot be submitted a second time.
                if (submitted) sendEvent(CollectionSheetDetailsEvent.NavigateBack)
            }
            CollectionSheetDetailsAction.NavigateBack -> sendEvent(CollectionSheetDetailsEvent.NavigateBack)
        }
    }

    private fun rowFor(loanId: Int) = sheet.value.dataOrNull?.rows?.firstOrNull { it.loanId == loanId }

    private fun openEditor(loanId: Int) {
        val row = rowFor(loanId) ?: return
        mutableStateFlow.value = state.copy(editor = PaymentEditor.of(row))
    }

    private fun saveEditor() {
        val editor = state.editor ?: return
        val amount = editor.amount.trim().toDoubleOrNull()
        // The original parsed with `toDoubleOrNull() ?: 0.0`, so a typo submitted a ZERO repayment
        // and said nothing. A negative amount is not a collection either.
        if (amount == null || amount < 0) {
            mutableStateFlow.value = state.copy(editor = editor.copy(amountError = true))
            return
        }
        mutableStateFlow.value = state.copy(
            drafts = state.drafts + (editor.loanId to editor.toTransaction(amount)),
            editor = null,
        )
    }

    private fun submit() {
        val view = sheet.value.dataOrNull ?: return
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true)
        val payload = IndividualCollectionSheetPayload(
            bulkRepaymentTransactions = ArrayList(view.rows.map { it.collect }),
            dateFormat = ApiDateFormatter.DATE_FORMAT,
            locale = ApiDateFormatter.LOCALE,
            transactionDate = request.transactionDate,
        )
        viewModelScope.launch {
            val dialog = when (val result = commands.saveIndividualCollectionSheet(request, payload)) {
                is MutationResult.Applied -> CollectionSheetDetailsState.Dialog.Submitted
                is MutationResult.Blocked -> if (result.reason == BlockReason.OFFLINE) {
                    CollectionSheetDetailsState.Dialog.Offline
                } else {
                    CollectionSheetDetailsState.Dialog.Failed(null)
                }
                is MutationResult.Failed -> CollectionSheetDetailsState.Dialog.Failed(result.cause.serverErrorMessage())
                is MutationResult.Conflicted -> CollectionSheetDetailsState.Dialog.Failed(null)
            }
            mutableStateFlow.value = state.copy(isSubmitting = false, dialog = dialog)
        }
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/** What the screen draws: the sheet's header facts and one row per loan due. */
internal data class CollectionSheetView(
    val transactionDate: String,
    val memberCount: Int,
    val rows: List<CollectionRow>,
    val paymentTypes: List<PaymentTypeOptions>,
) {
    val total: Double get() = rows.sumOf { it.collect.transactionAmount }
}

internal data class CollectionRow(
    val loanId: Int,
    val clientId: Int,
    val clientName: String,
    val productName: String,
    val accountNo: String,
    val totalDue: Double,
    val chargesDue: Double,
    /** What will be sent for this loan: the officer's draft, else the due amount plus charges. */
    val collect: BulkRepaymentTransactions,
    val edited: Boolean,
) {
    val hasPaymentDetail: Boolean get() = collect.paymentTypeId != null
}

/** One row per LOAN (a client with two loans is two rows), each defaulting to due + charges. */
internal fun IndividualCollectionSheet.toView(
    transactionDate: String,
    drafts: Map<Int, BulkRepaymentTransactions>,
): CollectionSheetView {
    val clients = clients.orEmpty()
    val rows = clients.flatMap { client ->
        client.loans.orEmpty().map { loan ->
            val draft = drafts[loan.loanId]
            CollectionRow(
                loanId = loan.loanId,
                clientId = client.clientId,
                clientName = client.clientName.orEmpty(),
                productName = loan.productShortName.orEmpty(),
                accountNo = loan.accountId.orEmpty(),
                totalDue = loan.totalDue,
                chargesDue = loan.chargesDue,
                collect = draft ?: BulkRepaymentTransactions(
                    loanId = loan.loanId,
                    transactionAmount = loan.totalDue + loan.chargesDue,
                ),
                edited = draft != null,
            )
        }
    }
    return CollectionSheetView(
        transactionDate = transactionDate,
        memberCount = clients.count { !it.loans.isNullOrEmpty() },
        rows = rows,
        paymentTypes = paymentTypeOptions.orEmpty(),
    )
}

/** The payment sheet's fields for one loan — strings while being typed, parsed on save. */
internal data class PaymentEditor(
    val loanId: Int,
    val clientName: String,
    val productName: String,
    val accountNo: String,
    val chargesDue: Double,
    val amount: String,
    val amountError: Boolean = false,
    val paymentTypeId: Int? = null,
    val accountNumber: String = "",
    val checkNumber: String = "",
    val routingCode: String = "",
    val receiptNumber: String = "",
    val bankNumber: String = "",
) {
    /** Payment details are sent only with a payment type; a type-less detail would be rejected. */
    fun toTransaction(amount: Double) = BulkRepaymentTransactions(
        loanId = loanId,
        transactionAmount = amount,
        paymentTypeId = paymentTypeId,
        accountNumber = accountNumber.trim().ifEmpty { null }.takeIf { paymentTypeId != null },
        checkNumber = checkNumber.trim().ifEmpty { null }.takeIf { paymentTypeId != null },
        routingCode = routingCode.trim().ifEmpty { null }.takeIf { paymentTypeId != null },
        receiptNumber = receiptNumber.trim().ifEmpty { null }.takeIf { paymentTypeId != null },
        bankNumber = bankNumber.trim().ifEmpty { null }.takeIf { paymentTypeId != null },
    )

    companion object {
        fun of(row: CollectionRow) = PaymentEditor(
            loanId = row.loanId,
            clientName = row.clientName,
            productName = row.productName,
            accountNo = row.accountNo,
            chargesDue = row.chargesDue,
            // Two decimals, ungrouped: "1225.00" reads as money and parses back; "1225.0" did neither well.
            amount = row.collect.transactionAmount.formatDecimal(2),
            paymentTypeId = row.collect.paymentTypeId,
            accountNumber = row.collect.accountNumber.orEmpty(),
            checkNumber = row.collect.checkNumber.orEmpty(),
            routingCode = row.collect.routingCode.orEmpty(),
            receiptNumber = row.collect.receiptNumber.orEmpty(),
            bankNumber = row.collect.bankNumber.orEmpty(),
        )
    }
}

internal data class CollectionSheetDetailsState(
    /** The officer's edits, by loan id — the single source of what is sent. */
    val drafts: Map<Int, BulkRepaymentTransactions> = emptyMap(),
    val editor: PaymentEditor? = null,
    val isSubmitting: Boolean = false,
    val dialog: Dialog? = null,
) {
    sealed interface Dialog {
        data object Submitted : Dialog
        data object Offline : Dialog

        /** [serverMessage] is Fineract's own sentence when it sent one; else the generic copy shows. */
        data class Failed(val serverMessage: String?) : Dialog
    }
}

internal sealed interface CollectionSheetDetailsEvent {
    data object NavigateBack : CollectionSheetDetailsEvent
}

internal sealed interface CollectionSheetDetailsAction {
    data class EditPayment(val loanId: Int) : CollectionSheetDetailsAction

    /** One field edit, as a pure function of the editor — keeps the action list short. */
    data class EditorChanged(val change: (PaymentEditor) -> PaymentEditor) : CollectionSheetDetailsAction
    data object SaveEditor : CollectionSheetDetailsAction
    data object CloseEditor : CollectionSheetDetailsAction
    data object Submit : CollectionSheetDetailsAction
    data object DismissDialog : CollectionSheetDetailsAction
    data object NavigateBack : CollectionSheetDetailsAction
}
