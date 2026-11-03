/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.statuschange

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.savings.SavingsCommandRepository
import kpt.core.model.objects.account.saving.SavingsActivationInput
import kpt.core.model.objects.account.saving.SavingsApprovalInput
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ApiDateFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Approve or activate a savings account — ported from `6b66e8a43`'s
 * `savingsAccountApproval/` (266-line screen + 44-line ViewModel) and
 * `savingsAccountActivate/` (270 + 44).
 *
 * ## One screen, because the two were the same screen
 *
 * The originals are near-duplicates: a date picker, a submit button, and a three-case UiState. The
 * only differences are the title, the command, and a note field that approval has and activation
 * does not. They are [SavingsStatusChange] here, so the two cannot drift — which they already had:
 * approval wrapped its date in a `SavingsApproval` object, activation hand-built a
 * `HashMap<String, String>` with three string keys, and only one of them bothered to name the
 * locale constant rather than hardcoding `"en"`.
 *
 * ## Both sent EPOCH MILLISECONDS as the date
 *
 * ```kotlin
 * // approval
 * SavingsApproval(approvedOnDate = approvalDate.toString(), note = reasonForApproval)
 * // activation
 * hashMap["activatedOnDate"] = approvalDate.toString()
 * hashMap["dateFormat"] = ApiDateFormatter.DATE_FORMAT
 * ```
 *
 * `approvalDate` is a `Long` of epoch millis (it feeds `initialSelectedDateMillis`), so the request
 * carried `"1761000000000"` while declaring `dateFormat = "dd MMMM yyyy"`. The screen DISPLAYED the
 * date correctly via `DateHelper.getDateAsStringFromLong`, so nothing on screen looked wrong — only
 * the request was. That is the 6th and 7th occurrence of this defect class in the migration and its
 * worst variant: not a mismatched format, but not a date at all.
 *
 * Fixed structurally rather than at the call site: `SavingsApprovalInput` /
 * `SavingsActivationInput` take `locale` and `dateFormat` as REQUIRED parameters, and both dates go
 * through [ApiDateFormatter.formatForApi] — the helper that owns the format it declares.
 */
@OptIn(ExperimentalTime::class)
internal class SavingsStatusChangeViewModel(
    private val commands: SavingsCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SavingsStatusChangeState, SavingsStatusChangeEvent, SavingsStatusChangeAction>(
    SavingsStatusChangeState(
        accountId = savedStateHandle.get<Int>(ACCOUNT_ID_KEY) ?: -1,
        change = savedStateHandle.get<String>(CHANGE_KEY)
            ?.let { name -> SavingsStatusChange.entries.firstOrNull { it.name == name } }
            ?: SavingsStatusChange.APPROVE,
    ),
) {

    override fun handleAction(action: SavingsStatusChangeAction) {
        when (action) {
            SavingsStatusChangeAction.NavigateBack ->
                sendEvent(SavingsStatusChangeEvent.NavigateBack)

            SavingsStatusChangeAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            SavingsStatusChangeAction.Done -> sendEvent(SavingsStatusChangeEvent.Done)

            is SavingsStatusChangeAction.OnDateChanged ->
                mutableStateFlow.value = state.copy(date = action.millis)

            is SavingsStatusChangeAction.OnNoteChanged ->
                mutableStateFlow.value = state.copy(note = action.value)

            SavingsStatusChangeAction.OpenDatePicker ->
                mutableStateFlow.value = state.copy(isDatePickerOpen = true)

            SavingsStatusChangeAction.CloseDatePicker ->
                mutableStateFlow.value = state.copy(isDatePickerOpen = false)

            SavingsStatusChangeAction.OnSubmit -> submit()
        }
    }

    private fun submit() {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null)

        viewModelScope.launch {
            // The date and the format built together, by the helper that owns both.
            val date = ApiDateFormatter.formatForApi(state.date)

            val result = when (state.change) {
                SavingsStatusChange.APPROVE -> commands.approveSavingsApplication(
                    savingsAccountId = state.accountId,
                    input = SavingsApprovalInput(
                        approvedOnDate = date,
                        locale = ApiDateFormatter.LOCALE,
                        dateFormat = ApiDateFormatter.DATE_FORMAT,
                        // Blank omitted. The original sent the empty string, which Fineract stores
                        // as a note of "" rather than as no note.
                        note = state.note.trim().takeIf { it.isNotBlank() },
                    ),
                )

                SavingsStatusChange.ACTIVATE -> commands.activateSavings(
                    savingsAccountId = state.accountId,
                    input = SavingsActivationInput(
                        activatedOnDate = date,
                        locale = ApiDateFormatter.LOCALE,
                        dateFormat = ApiDateFormatter.DATE_FORMAT,
                    ),
                )
            }

            mutableStateFlow.value = when (result) {
                is MutationResult.Applied ->
                    state.copy(isSubmitting = false, dialogState = DialogState.Succeeded)

                is MutationResult.Blocked -> state.copy(
                    isSubmitting = false,
                    // A status change needs the server: there is nothing useful to record locally,
                    // because the account's status IS the server's answer.
                    dialogState = DialogState.Failed(
                        offline = result.reason == BlockReason.OFFLINE,
                        msg = null,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "${state.change} failed" }
                    state.copy(
                        isSubmitting = false,
                        dialogState = DialogState.Failed(offline = false, msg = result.cause.serverErrorMessage()),
                    )
                }

                is MutationResult.Conflicted -> state.copy(
                    isSubmitting = false,
                    dialogState = DialogState.Failed(offline = false, msg = null),
                )
            }
        }
    }

    private companion object {
        const val TAG = "SavingsStatusChange"
        const val ACCOUNT_ID_KEY = "accountId"
        const val CHANGE_KEY = "change"
    }
}

/** Which status change this screen performs. */
internal enum class SavingsStatusChange {
    APPROVE,
    ACTIVATE,
    ;

    /** Only approval carries a note — Fineract's activate command takes none. */
    val hasNote: Boolean get() = this == APPROVE
}

internal sealed interface DialogState {
    data object Succeeded : DialogState

    /** [offline] distinguishes "could not reach the server" from "the server refused". */
    data class Failed(val offline: Boolean, val msg: String?) : DialogState
}

@OptIn(ExperimentalTime::class)
internal data class SavingsStatusChangeState(
    val accountId: Int = -1,
    val change: SavingsStatusChange = SavingsStatusChange.APPROVE,
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val note: String = "",
    val isDatePickerOpen: Boolean = false,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
)

internal sealed interface SavingsStatusChangeEvent {
    data object NavigateBack : SavingsStatusChangeEvent

    /** The change landed — leave, so the summary behind reloads with the new status. */
    data object Done : SavingsStatusChangeEvent
}

internal sealed interface SavingsStatusChangeAction {
    data object NavigateBack : SavingsStatusChangeAction
    data object OnSubmit : SavingsStatusChangeAction
    data object CloseDialog : SavingsStatusChangeAction
    data object Done : SavingsStatusChangeAction
    data object OpenDatePicker : SavingsStatusChangeAction
    data object CloseDatePicker : SavingsStatusChangeAction
    data class OnDateChanged(val millis: Long) : SavingsStatusChangeAction
    data class OnNoteChanged(val value: String) : SavingsStatusChangeAction
}
