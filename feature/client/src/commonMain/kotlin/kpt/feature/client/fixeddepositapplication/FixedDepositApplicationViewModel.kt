/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.fixeddepositapplication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.FormDraftSlot
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.store.submit.draft.isOffline
import kpt.core.base.store.submit.draft.offlineWhenUnsent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.DateFormatPattern
import kpt.core.data.savings.SavingsAccountRepository
import kpt.core.data.savings.SavingsCommandRepository
import kpt.core.model.draft.CreateForm
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.savings.FixedDepositPayload
import kpt.core.model.savings.FixedDepositTemplate
import kpt.core.model.utils.ApiDateFormatter

/**
 * Open a FIXED DEPOSIT account for a client — ported from
 * `6b66e8a43:feature/client/newFixedDepositAccount/` (a 933-line ViewModel, a 214-line screen and
 * six page composables: Details, Terms, Settings, Interest, Charges, Preview).
 *
 * Reached from the apply-new chooser's "Fixed Deposit" row and from the fixed-deposit account
 * list's "+", BOTH of which were gated off because this destination did not exist. With it, the
 * chooser finally offers every product type the client's eligibility allows.
 *
 * ## FIXED: eleven option-list indexes, two of which crash
 *
 * The original's payload resolves every dropdown by indexing the template's option list with an
 * index held separately in state — and it is inconsistent about how:
 *
 * ```kotlin
 * productId = state.template.productOptions?.get(state.…​.productSelected)?.id          // get
 * depositPeriodFrequencyId = state.template.periodFrequencyTypeOptions?.get(…​)?.id      // get
 * interestCompoundingPeriodType = …​.interestCompoundingPeriodTypeOptions?.getOrNull(…​)  // getOrNull
 * ```
 *
 * Nine use `getOrNull` and silently yield null; **two use `get`**. Both of those index with a
 * field whose default is `-1` (`productSelected: Int = -1`,
 * `depositPeriodTypeIndex: Int = -1`), so reaching the payload without choosing a product or a
 * deposit-period type throws `IndexOutOfBoundsException` out of the submit handler. The same
 * defect the recurring-deposit wizard carried
 * (`recurring-deposit-product-read-through-an-unguarded-index`).
 *
 * A separately-held index is also wrong even when it is in range: the template is re-read when the
 * product changes, and a shorter list leaves every stored index pointing at a DIFFERENT option
 * than the one on screen — the defect the create-client dropdowns shipped
 * (`create-client-dropdowns-stored-a-different-option-than-shown`).
 *
 * Here every choice is a [FixedDepositOption] carrying its own id, so there is no index, nothing
 * to force, and nothing that can drift against a re-read list.
 *
 * ## FIXED: two unguarded numeric conversions
 *
 * `depositAmount.toDouble()` and `depositPeriod.toInt()` ran at payload time with no guard, so a
 * blank or non-numeric entry threw rather than reporting a field error — the loan wizard's
 * `toFloat()` defect again. Both are validated before submit and parsed safely.
 *
 * ## The date and its declared format come from ONE value
 *
 * As on the share wizard: the original paired `DateHelper.getDateAsStringFromLong` with
 * `dateFormat = DateHelper.SHORT_MONTH`, which AGREE — this is not another occurrence of the
 * date/format defect. [SUBMIT_DATE_FORMAT] makes the agreement structural rather than
 * coincidental, and keeps the wire format byte-identical. See the share wizard for why that
 * matters while two different `SHORT_MONTH` constants exist.
 */
internal class FixedDepositApplicationViewModel(
    repository: SavingsAccountRepository,
    private val commands: SavingsCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<
    FixedDepositApplicationState,
    FixedDepositApplicationEvent,
    FixedDepositApplicationAction,
    >(
    FixedDepositApplicationState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    /** This client's application that did not land — resumable here, listed in Sync & Drafts. */
    private val formDraft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.FIXED_DEPOSIT.key,
        ownerId = state.clientId.toLong(),
        FixedDepositApplicationState.serializer(),
        FixedDepositPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered

    private fun keepDraft(failure: String?) = formDraft.keep(
        title = state.product?.label.orEmpty(),
        state = state.copy(isSubmitting = false, step = FixedDepositStep.DETAILS),
        payload = state.toPayload(),
        failure = failure,
    )

    val template: StateFlow<ScreenState<FixedDepositTemplate>> =
        repository.fixedDepositTemplateStream(state.clientId, null, viewModelScope).state
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: FixedDepositApplicationAction) {
        when (action) {
            FixedDepositApplicationAction.ResumeDraft -> formDraft.resume()?.let { mutableStateFlow.value = it }
            FixedDepositApplicationAction.DiscardDraft -> formDraft.discard()
            FixedDepositApplicationAction.SaveDraft -> {
                keepDraft(failure = state.serverMessage ?: "Not accepted by the server.")
                mutableStateFlow.value = state.copy(draftSaved = true)
            }
            FixedDepositApplicationAction.NavigateBack ->
                sendEvent(FixedDepositApplicationEvent.NavigateBack)

            FixedDepositApplicationAction.NextStep -> moveTo(state.step.next())
            FixedDepositApplicationAction.PreviousStep -> moveTo(state.step.previous())

            FixedDepositApplicationAction.Submit -> submit()

            FixedDepositApplicationAction.DismissError ->
                mutableStateFlow.value = state.copy(error = null, serverMessage = null)

            else -> updateField(action)
        }
    }

    private fun updateField(action: FixedDepositApplicationAction) {
        when (action) {
            is FixedDepositApplicationAction.UpdateProduct ->
                mutableStateFlow.value = state.copy(product = action.option, productError = false)

            is FixedDepositApplicationAction.UpdateFieldOfficer ->
                mutableStateFlow.value = state.copy(fieldOfficer = action.option)

            is FixedDepositApplicationAction.UpdateExternalId ->
                mutableStateFlow.value = state.copy(externalId = action.value)

            is FixedDepositApplicationAction.UpdateSubmissionDate ->
                mutableStateFlow.value = state.copy(submissionDateMillis = action.millis)

            is FixedDepositApplicationAction.UpdateDepositAmount ->
                mutableStateFlow.value =
                    state.copy(depositAmount = action.value, depositAmountError = false)

            is FixedDepositApplicationAction.UpdateDepositPeriod ->
                mutableStateFlow.value =
                    state.copy(depositPeriod = action.value, depositPeriodError = false)

            is FixedDepositApplicationAction.UpdateDepositPeriodType ->
                mutableStateFlow.value =
                    state.copy(depositPeriodType = action.option, depositPeriodTypeError = false)

            else -> updateTermsField(action)
        }
    }

    // The interest, lock-in and penal terms — split from [updateField] to keep each `when` small.
    private fun updateTermsField(action: FixedDepositApplicationAction) {
        when (action) {
            is FixedDepositApplicationAction.UpdateInterestCompoundingPeriod ->
                mutableStateFlow.value = state.copy(interestCompoundingPeriod = action.option)

            is FixedDepositApplicationAction.UpdateInterestPostingPeriod ->
                mutableStateFlow.value = state.copy(interestPostingPeriod = action.option)

            is FixedDepositApplicationAction.UpdateInterestCalculationType ->
                mutableStateFlow.value = state.copy(interestCalculationType = action.option)

            is FixedDepositApplicationAction.UpdateInterestCalculationDaysInYear ->
                mutableStateFlow.value = state.copy(interestCalculationDaysInYear = action.option)

            is FixedDepositApplicationAction.UpdateLockInPeriod ->
                mutableStateFlow.value = state.copy(lockInPeriod = action.value)

            is FixedDepositApplicationAction.UpdateLockInPeriodType ->
                mutableStateFlow.value = state.copy(lockInPeriodType = action.option)

            is FixedDepositApplicationAction.UpdateTransferInterestToSavings ->
                mutableStateFlow.value = state.copy(transferInterestToSavings = action.value)

            is FixedDepositApplicationAction.UpdatePenalInterestApplicable ->
                mutableStateFlow.value = state.copy(penalInterestApplicable = action.value)

            is FixedDepositApplicationAction.UpdatePenalInterest ->
                mutableStateFlow.value = state.copy(penalInterest = action.value)

            else -> Unit
        }
    }

    /** Validate the page being LEFT when moving forward; never block a retreat. */
    private fun moveTo(next: FixedDepositStep) {
        if (next.ordinal <= state.step.ordinal) {
            mutableStateFlow.value = state.copy(step = next)
            return
        }
        val errors = state.validate(state.step)
        mutableStateFlow.value = state.withErrors(errors).copy(
            step = if (errors.isValid) next else state.step,
        )
    }

    private fun submit() {
        // Validate EVERY gated page, not just the current one — the officer can reach Preview by
        // stepping forward, and the two pages that carry required fields are behind it.
        val errors = state.validateAll()
        if (!errors.isValid || state.isSubmitting) {
            mutableStateFlow.value = state.withErrors(errors)
            return
        }

        mutableStateFlow.value = state.copy(isSubmitting = true, error = null, serverMessage = null)
        viewModelScope.launch {
            when (val result = commands.createFixedDepositAccount(state.toPayload()).offlineWhenUnsent()) {
                is MutationResult.Applied -> {
                    formDraft.clear()
                    mutableStateFlow.value = state.copy(isSubmitting = false)
                    sendEvent(FixedDepositApplicationEvent.Created)
                }

                // Offline: kept automatically and sent on reconnect.
                is MutationResult.Blocked -> {
                    if (result.isOffline) keepDraft(failure = null)
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        error = if (result.reason == BlockReason.OFFLINE) {
                            FixedDepositError.Offline
                        } else {
                            FixedDepositError.Failed
                        },
                        draftSaved = result.isOffline,
                    )
                }

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create fixed deposit failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        error = FixedDepositError.Failed,
                        serverMessage = result.cause.serverErrorMessage(),
                    )
                }

                is MutationResult.Conflicted ->
                    mutableStateFlow.value =
                        state.copy(isSubmitting = false, error = FixedDepositError.Failed)
            }
        }
    }

    internal companion object {
        const val CLIENT_ID_KEY = "clientId"

        /** One pattern formats the date AND declares `dateFormat`. `"dd-MM-yyyy"`, as before. */
        val SUBMIT_DATE_FORMAT = DateFormatPattern.NUMERIC_DASH

        const val LOCALE = "en"
        private const val STATE_TIMEOUT_MS = 5_000L
        private const val TAG = "FixedDepositApplication"
    }
}

/** The wizard's pages, in the original's order. */
internal enum class FixedDepositStep {
    DETAILS,
    TERMS,
    SETTINGS,
    INTEREST,
    CHARGES,
    PREVIEW,
    ;

    fun next(): FixedDepositStep = entries.getOrElse(ordinal + 1) { this }
    fun previous(): FixedDepositStep = entries.getOrElse(ordinal - 1) { this }
}

/** One selectable option. The CHOICE carries its id, so no index can drift against a re-read. */
@Serializable
internal data class FixedDepositOption(val id: Int, val label: String)

internal data class FixedDepositFieldErrors(
    val product: Boolean = false,
    val depositAmount: Boolean = false,
    val depositPeriod: Boolean = false,
    val depositPeriodType: Boolean = false,
) {
    val isValid: Boolean get() = !product && !depositAmount && !depositPeriod && !depositPeriodType
}

/** The original's required-field rules, per page. */
internal fun FixedDepositApplicationState.validate(step: FixedDepositStep): FixedDepositFieldErrors =
    when (step) {
        FixedDepositStep.DETAILS -> FixedDepositFieldErrors(product = product == null)
        FixedDepositStep.TERMS -> FixedDepositFieldErrors(
            // A deposit of zero is not a deposit; the original only checked it was parseable.
            depositAmount = (depositAmount.trim().toDoubleOrNull() ?: 0.0) <= 0.0,
            depositPeriod = (depositPeriod.trim().toIntOrNull() ?: 0) <= 0,
            depositPeriodType = depositPeriodType == null,
        )
        else -> FixedDepositFieldErrors()
    }

/**
 * Every gated page at once.
 *
 * Submit lives on PREVIEW, two pages past the last required field, so validating only the current
 * page would let an incomplete application through — which is exactly how the original reached
 * `productOptions?.get(-1)`.
 */
internal fun FixedDepositApplicationState.validateAll(): FixedDepositFieldErrors {
    val details = validate(FixedDepositStep.DETAILS)
    val terms = validate(FixedDepositStep.TERMS)
    return FixedDepositFieldErrors(
        product = details.product,
        depositAmount = terms.depositAmount,
        depositPeriod = terms.depositPeriod,
        depositPeriodType = terms.depositPeriodType,
    )
}

internal fun FixedDepositApplicationState.withErrors(
    errors: FixedDepositFieldErrors,
): FixedDepositApplicationState = copy(
    productError = errors.product,
    depositAmountError = errors.depositAmount,
    depositPeriodError = errors.depositPeriod,
    depositPeriodTypeError = errors.depositPeriodType,
)

internal fun FixedDepositApplicationState.toPayload(): FixedDepositPayload = FixedDepositPayload(
    clientId = clientId,
    productId = product?.id,
    fieldOfficerId = fieldOfficer?.id,
    externalId = externalId.trim().ifEmpty { null },
    submittedOnDate = ApiDateFormatter.formatForApi(
        submissionDateMillis,
        FixedDepositApplicationViewModel.SUBMIT_DATE_FORMAT,
    ),
    depositAmount = depositAmount.trim().toDoubleOrNull(),
    depositPeriod = depositPeriod.trim().toIntOrNull(),
    depositPeriodFrequencyId = depositPeriodType?.id,
    interestCompoundingPeriodType = interestCompoundingPeriod?.id,
    interestPostingPeriodType = interestPostingPeriod?.id,
    interestCalculationType = interestCalculationType?.id,
    interestCalculationDaysInYearType = interestCalculationDaysInYear?.id,
    lockinPeriodFrequency = lockInPeriod.trim().toIntOrNull(),
    lockinPeriodFrequencyType = lockInPeriodType?.id,
    transferInterestToSavings = transferInterestToSavings,
    preClosurePenalApplicable = penalInterestApplicable,
    // Only meaningful when the penalty applies; sending a rate with the flag off would be a
    // number the server has no rule for.
    preClosurePenalInterest = if (penalInterestApplicable) {
        penalInterest.trim().toDoubleOrNull()
    } else {
        null
    },
    // Declared from the SAME value that formatted the date above.
    dateFormat = FixedDepositApplicationViewModel.SUBMIT_DATE_FORMAT.pattern,
    locale = FixedDepositApplicationViewModel.LOCALE,
)

internal enum class FixedDepositError {
    Offline,
    Failed,
}

/** Serializable so a draft restores the wizard as left; the transient fields are per-visit. */
@Serializable
internal data class FixedDepositApplicationState(
    val clientId: Int = -1,
    val step: FixedDepositStep = FixedDepositStep.DETAILS,
    val product: FixedDepositOption? = null,
    val fieldOfficer: FixedDepositOption? = null,
    val externalId: String = "",
    val submissionDateMillis: Long = 0L,
    val depositAmount: String = "",
    val depositPeriod: String = "",
    val depositPeriodType: FixedDepositOption? = null,
    val interestCompoundingPeriod: FixedDepositOption? = null,
    val interestPostingPeriod: FixedDepositOption? = null,
    val interestCalculationType: FixedDepositOption? = null,
    val interestCalculationDaysInYear: FixedDepositOption? = null,
    val lockInPeriod: String = "",
    val lockInPeriodType: FixedDepositOption? = null,
    val transferInterestToSavings: Boolean = false,
    val penalInterestApplicable: Boolean = false,
    val penalInterest: String = "",
    @Transient val productError: Boolean = false,
    @Transient val depositAmountError: Boolean = false,
    @Transient val depositPeriodError: Boolean = false,
    @Transient val depositPeriodTypeError: Boolean = false,
    @Transient val isSubmitting: Boolean = false,
    @Transient val error: FixedDepositError? = null,
    @Transient val serverMessage: String? = null,
    /** This create was kept as a draft (offline automatically, or "Save as draft"). */
    @Transient val draftSaved: Boolean = false,
)

internal sealed interface FixedDepositApplicationEvent {
    data object NavigateBack : FixedDepositApplicationEvent
    data object Created : FixedDepositApplicationEvent
}

internal sealed interface FixedDepositApplicationAction {
    data object ResumeDraft : FixedDepositApplicationAction
    data object DiscardDraft : FixedDepositApplicationAction
    data object SaveDraft : FixedDepositApplicationAction
    data object NavigateBack : FixedDepositApplicationAction
    data object NextStep : FixedDepositApplicationAction
    data object PreviousStep : FixedDepositApplicationAction
    data object Submit : FixedDepositApplicationAction
    data object DismissError : FixedDepositApplicationAction
    data class UpdateProduct(val option: FixedDepositOption) : FixedDepositApplicationAction
    data class UpdateFieldOfficer(val option: FixedDepositOption?) : FixedDepositApplicationAction
    data class UpdateExternalId(val value: String) : FixedDepositApplicationAction
    data class UpdateSubmissionDate(val millis: Long) : FixedDepositApplicationAction
    data class UpdateDepositAmount(val value: String) : FixedDepositApplicationAction
    data class UpdateDepositPeriod(val value: String) : FixedDepositApplicationAction
    data class UpdateDepositPeriodType(val option: FixedDepositOption) : FixedDepositApplicationAction
    data class UpdateInterestCompoundingPeriod(val option: FixedDepositOption?) :
        FixedDepositApplicationAction
    data class UpdateInterestPostingPeriod(val option: FixedDepositOption?) :
        FixedDepositApplicationAction
    data class UpdateInterestCalculationType(val option: FixedDepositOption?) :
        FixedDepositApplicationAction
    data class UpdateInterestCalculationDaysInYear(val option: FixedDepositOption?) :
        FixedDepositApplicationAction
    data class UpdateLockInPeriod(val value: String) : FixedDepositApplicationAction
    data class UpdateLockInPeriodType(val option: FixedDepositOption?) : FixedDepositApplicationAction
    data class UpdateTransferInterestToSavings(val value: Boolean) : FixedDepositApplicationAction
    data class UpdatePenalInterestApplicable(val value: Boolean) : FixedDepositApplicationAction
    data class UpdatePenalInterest(val value: String) : FixedDepositApplicationAction
}
