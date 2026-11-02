/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.shareapplication

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
import kpt.core.model.savings.ShareAccountPayload
import kpt.core.model.savings.ShareTemplate
import kpt.core.model.utils.ApiDateFormatter

/**
 * Open a SHARE account for a client — ported from
 * `6b66e8a43:feature/client/createShareAccount/` (a 656-line ViewModel, a 344-line screen and four
 * page composables: Details, Terms, Charges, Preview).
 *
 * Reached from the apply-new chooser's "Share Account" row, which that chooser GATED OUT because
 * this destination did not exist — so the chooser rendered three product types where the client's
 * eligibility actually allowed four.
 *
 * ## The date and its declared format now come from ONE value
 *
 * The original built the payload with
 *
 * ```kotlin
 * submittedDate = state.submissionDate,            // DateHelper.getDateAsStringFromLong(...)
 * dateFormat = DateHelper.SHORT_MONTH,
 * ```
 *
 * and those two AGREE — `getDateAsStringFromLong` formats with `DateHelper.SHORT_MONTH`. **This
 * original does not have the date/format defect** that eight other screens in this port did; it is
 * recorded as agreeing rather than quietly "fixed".
 *
 * They agree by coincidence of two independent references, though, and the hazard here is sharper
 * than elsewhere: `DateHelper.SHORT_MONTH` is `"dd-MM-yyyy"` while `DateFormatPattern.SHORT_MONTH`
 * is `"dd MMM yyyy"` — two constants, one name, different formats
 * (`two-short-month-constants-disagree`). Anyone "unifying" them would silently change what this
 * payload declares while leaving what it sends untouched.
 *
 * So [SUBMIT_DATE_FORMAT] is a single [DateFormatPattern] that both FORMATS the dates and supplies
 * `dateFormat`, and it is [DateFormatPattern.NUMERIC_DASH] — the same `"dd-MM-yyyy"` the server
 * receives today, under the name that actually describes it.
 *
 * ## FIXED: four `!!` indexes into option lists that may not contain them
 *
 * The original's payload read
 * `state.productOption[state.shareProductIndex!!].id` and
 * `state.savingsAccountOptions[state.savingsAccountIdx!!].id`, with the frequency-type lists
 * indexed the same way. Each is an index stored separately from the list it indexes, so a template
 * re-read that returns a shorter list leaves the index pointing past the end — and `!!` turns "not
 * chosen yet" into a crash rather than a validation message.
 *
 * Here the CHOICE is the option (`ShareOption(id, label)`), so there is no index to go stale and
 * nothing to force-unwrap.
 */
internal class ShareApplicationViewModel(
    private val repository: SavingsAccountRepository,
    private val commands: SavingsCommandRepository,
    savedStateHandle: SavedStateHandle,
    drafts: FormDraftRepository,
) : BaseViewModel<ShareApplicationState, ShareApplicationEvent, ShareApplicationAction>(
    ShareApplicationState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    /** This client's application that did not land — resumable here, listed in Sync & Drafts. */
    private val formDraft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.SHARE.key,
        ownerId = state.clientId.toLong(),
        ShareApplicationState.serializer(),
        ShareAccountPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = formDraft.offered

    private fun keepDraft(failure: String?) = formDraft.keep(
        title = state.product?.label.orEmpty(),
        state = state.copy(isSubmitting = false, step = ShareStep.DETAILS),
        payload = state.toPayload(),
        failure = failure,
    )

    /**
     * The template, re-read whenever the chosen PRODUCT changes.
     *
     * `productId = null` is the first read and supplies the product options; choosing one re-reads
     * with that product's price, currency and charge options filled in. The original did the same
     * with two separate methods (`loadShareTemplate` / `loadShareTemplateFromProduct`) that
     * duplicated their success handling.
     */
    val template: StateFlow<ScreenState<ShareTemplate>> =
        repository.shareTemplateStream(state.clientId, null, viewModelScope).state
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ShareApplicationAction) {
        when (action) {
            ShareApplicationAction.ResumeDraft -> formDraft.resume()?.let { mutableStateFlow.value = it }
            ShareApplicationAction.DiscardDraft -> formDraft.discard()
            ShareApplicationAction.SaveDraft -> {
                keepDraft(failure = state.serverMessage ?: "Not accepted by the server.")
                mutableStateFlow.value = state.copy(draftSaved = true)
            }
            ShareApplicationAction.NavigateBack -> sendEvent(ShareApplicationEvent.NavigateBack)

            ShareApplicationAction.NextStep -> moveTo(state.step.next())
            ShareApplicationAction.PreviousStep -> moveTo(state.step.previous())

            ShareApplicationAction.Submit -> submit()

            ShareApplicationAction.DismissError ->
                mutableStateFlow.value = state.copy(error = null, serverMessage = null)

            else -> updateField(action)
        }
    }

    private fun updateField(action: ShareApplicationAction) {
        when (action) {
            is ShareApplicationAction.UpdateProduct ->
                mutableStateFlow.value =
                    state.copy(product = action.option, productError = false)

            is ShareApplicationAction.UpdateSavingsAccount ->
                mutableStateFlow.value =
                    state.copy(savingsAccount = action.option, savingsAccountError = false)

            is ShareApplicationAction.UpdateTotalShares ->
                mutableStateFlow.value =
                    state.copy(totalShares = action.value, totalSharesError = false)

            is ShareApplicationAction.UpdateExternalId ->
                mutableStateFlow.value = state.copy(externalId = action.value)

            is ShareApplicationAction.UpdateSubmissionDate ->
                mutableStateFlow.value = state.copy(submissionDateMillis = action.millis)

            is ShareApplicationAction.UpdateApplicationDate ->
                mutableStateFlow.value = state.copy(applicationDateMillis = action.millis)

            is ShareApplicationAction.UpdateMinimumActivePeriod ->
                mutableStateFlow.value = state.copy(minimumActivePeriod = action.value)

            is ShareApplicationAction.UpdateMinimumActivePeriodType ->
                mutableStateFlow.value = state.copy(minimumActivePeriodType = action.option)

            is ShareApplicationAction.UpdateLockInPeriod ->
                mutableStateFlow.value = state.copy(lockInPeriod = action.value)

            is ShareApplicationAction.UpdateLockInPeriodType ->
                mutableStateFlow.value = state.copy(lockInPeriodType = action.option)

            is ShareApplicationAction.UpdateDividendAllowed ->
                mutableStateFlow.value = state.copy(isDividendAllowed = action.value)

            else -> Unit
        }
    }

    /**
     * Move between steps, validating the step being LEFT when moving forward.
     *
     * Backwards never validates — the original's `moveToPreviousStep` did not either, and blocking
     * a retreat because the current page is incomplete would trap an officer who stepped forward
     * by mistake.
     */
    private fun moveTo(next: ShareStep) {
        if (next.ordinal <= state.step.ordinal) {
            mutableStateFlow.value = state.copy(step = next)
            return
        }
        val errors = state.validate(state.step)
        mutableStateFlow.value = state.copy(
            productError = errors.product,
            savingsAccountError = errors.savingsAccount,
            totalSharesError = errors.totalShares,
            step = if (errors.isValid) next else state.step,
        )
    }

    private fun submit() {
        val errors = state.validate(ShareStep.DETAILS)
        if (!errors.isValid || state.isSubmitting) {
            mutableStateFlow.value = state.copy(
                productError = errors.product,
                savingsAccountError = errors.savingsAccount,
                totalSharesError = errors.totalShares,
            )
            return
        }

        mutableStateFlow.value = state.copy(isSubmitting = true, error = null, serverMessage = null)
        viewModelScope.launch {
            when (val result = commands.createShareAccount(state.toPayload()).offlineWhenUnsent()) {
                is MutationResult.Applied -> {
                    formDraft.clear()
                    mutableStateFlow.value = state.copy(isSubmitting = false)
                    sendEvent(ShareApplicationEvent.Created)
                }

                // Offline: kept automatically and sent on reconnect.
                is MutationResult.Blocked -> {
                    if (result.isOffline) keepDraft(failure = null)
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        error = if (result.reason == BlockReason.OFFLINE) {
                            ShareApplicationError.Offline
                        } else {
                            ShareApplicationError.Failed
                        },
                        draftSaved = result.isOffline,
                    )
                }

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create share account failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        error = ShareApplicationError.Failed,
                        serverMessage = result.cause.serverErrorMessage(),
                    )
                }

                is MutationResult.Conflicted ->
                    mutableStateFlow.value =
                        state.copy(isSubmitting = false, error = ShareApplicationError.Failed)
            }
        }
    }

    internal companion object {
        const val CLIENT_ID_KEY = "clientId"

        /**
         * The ONE pattern that both formats the dates and declares `dateFormat` on the wire.
         *
         * `NUMERIC_DASH` is `"dd-MM-yyyy"`, byte-identical to what the original sent via
         * `DateHelper.SHORT_MONTH` — the wire format is unchanged, only its single source is.
         */
        val SUBMIT_DATE_FORMAT = DateFormatPattern.NUMERIC_DASH

        /** Fineract's locale for this payload, as the original sent it. */
        const val LOCALE = "en"

        private const val STATE_TIMEOUT_MS = 5_000L
        private const val TAG = "ShareApplication"
    }
}

/** The wizard's pages, in the original's order. */
internal enum class ShareStep {
    DETAILS,
    TERMS,
    CHARGES,
    PREVIEW,
    ;

    fun next(): ShareStep = entries.getOrElse(ordinal + 1) { this }
    fun previous(): ShareStep = entries.getOrElse(ordinal - 1) { this }
}

/** One selectable option. The CHOICE is the option, so no index can go stale against its list. */
@Serializable
internal data class ShareOption(val id: Int, val label: String)

internal data class ShareFieldErrors(
    val product: Boolean = false,
    val savingsAccount: Boolean = false,
    val totalShares: Boolean = false,
) {
    val isValid: Boolean get() = !product && !savingsAccount && !totalShares
}

/**
 * The original's required-field rules for the DETAILS page.
 *
 * Shares must be a positive whole number: the original checked `totalShares.toInt()` at payload
 * time with no guard, so a blank or non-numeric value threw `NumberFormatException` out of the
 * submit handler rather than reporting a field error — the same shape as the loan wizard's
 * `toFloat()` defect.
 */
internal fun ShareApplicationState.validate(step: ShareStep): ShareFieldErrors =
    if (step != ShareStep.DETAILS) {
        ShareFieldErrors()
    } else {
        ShareFieldErrors(
            product = product == null,
            savingsAccount = savingsAccount == null,
            totalShares = (totalShares.trim().toIntOrNull() ?: 0) <= 0,
        )
    }

internal fun ShareApplicationState.toPayload(): ShareAccountPayload = ShareAccountPayload(
    clientId = clientId,
    productId = product?.id,
    requestedShares = totalShares.trim().toIntOrNull() ?: 0,
    externalId = externalId.trim().ifEmpty { null },
    submittedDate = ApiDateFormatter.formatForApi(
        submissionDateMillis,
        ShareApplicationViewModel.SUBMIT_DATE_FORMAT,
    ),
    applicationDate = ApiDateFormatter.formatForApi(
        applicationDateMillis,
        ShareApplicationViewModel.SUBMIT_DATE_FORMAT,
    ),
    minimumActivePeriod = minimumActivePeriod.trim().toIntOrNull(),
    minimumActivePeriodFrequencyType = minimumActivePeriodType?.id,
    lockinPeriodFrequency = lockInPeriod.trim().toIntOrNull(),
    lockinPeriodFrequencyType = lockInPeriodType?.id,
    allowDividendCalculationForInactiveClients = isDividendAllowed,
    savingsAccountId = savingsAccount?.id ?: 0,
    // Declared from the SAME value that formatted both dates above.
    dateFormat = ShareApplicationViewModel.SUBMIT_DATE_FORMAT.pattern,
    locale = ShareApplicationViewModel.LOCALE,
)

/** What went wrong on submit, as a value the screen renders. */
internal enum class ShareApplicationError {
    Offline,
    Failed,
}

/** Serializable so a draft restores the wizard as left; the transient fields are per-visit. */
@Serializable
internal data class ShareApplicationState(
    val clientId: Int = -1,
    val step: ShareStep = ShareStep.DETAILS,
    val product: ShareOption? = null,
    val savingsAccount: ShareOption? = null,
    val totalShares: String = "",
    val externalId: String = "",
    val submissionDateMillis: Long = 0L,
    val applicationDateMillis: Long = 0L,
    val minimumActivePeriod: String = "",
    val minimumActivePeriodType: ShareOption? = null,
    val lockInPeriod: String = "",
    val lockInPeriodType: ShareOption? = null,
    val isDividendAllowed: Boolean = false,
    @Transient val productError: Boolean = false,
    @Transient val savingsAccountError: Boolean = false,
    @Transient val totalSharesError: Boolean = false,
    @Transient val isSubmitting: Boolean = false,
    @Transient val error: ShareApplicationError? = null,
    /** Fineract's own words, when it gave any. Never rewritten. */
    @Transient val serverMessage: String? = null,
    /** This create was kept as a draft (offline automatically, or "Save as draft"). */
    @Transient val draftSaved: Boolean = false,
) {
    val canSubmit: Boolean get() = !isSubmitting
}

internal sealed interface ShareApplicationEvent {
    data object NavigateBack : ShareApplicationEvent
    data object Created : ShareApplicationEvent
}

internal sealed interface ShareApplicationAction {
    data object ResumeDraft : ShareApplicationAction
    data object DiscardDraft : ShareApplicationAction
    data object SaveDraft : ShareApplicationAction
    data object NavigateBack : ShareApplicationAction
    data object NextStep : ShareApplicationAction
    data object PreviousStep : ShareApplicationAction
    data object Submit : ShareApplicationAction
    data object DismissError : ShareApplicationAction
    data class UpdateProduct(val option: ShareOption) : ShareApplicationAction
    data class UpdateSavingsAccount(val option: ShareOption) : ShareApplicationAction
    data class UpdateTotalShares(val value: String) : ShareApplicationAction
    data class UpdateExternalId(val value: String) : ShareApplicationAction
    data class UpdateSubmissionDate(val millis: Long) : ShareApplicationAction
    data class UpdateApplicationDate(val millis: Long) : ShareApplicationAction
    data class UpdateMinimumActivePeriod(val value: String) : ShareApplicationAction
    data class UpdateMinimumActivePeriodType(val option: ShareOption?) : ShareApplicationAction
    data class UpdateLockInPeriod(val value: String) : ShareApplicationAction
    data class UpdateLockInPeriodType(val option: ShareOption?) : ShareApplicationAction
    data class UpdateDividendAllowed(val value: Boolean) : ShareApplicationAction
}
