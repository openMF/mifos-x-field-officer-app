/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.checkerinbox.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.store.screen.dataOrNull
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.checkerinbox.CheckerInboxCommandRepository
import kpt.core.data.checkerinbox.CheckerInboxRepository
import kpt.core.model.objects.checkerinboxtask.CheckerInboxSearchTemplate
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.model.objects.checkerinboxtask.RescheduleLoansTask
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.shared.GenericResponse
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Ported from `6b66e8a43:feature/checker-inbox-task` — `CheckerInboxTasksViewModel` (the hub),
 * `CheckerInboxViewModel` (the inbox) and `CheckerInboxDialogViewmodel` (the filter's names).
 *
 * The hub's five tiles become two tabs: of the original's five, three (client approval, loan approval,
 * loan disbursal) showed a hardcoded "0" and did nothing, and the reschedule tile showed a real count
 * and did nothing. Approvals is the maker-checker inbox; Reschedules lists pending loan reschedules,
 * each opening the loan's own reschedules screen, which already approves and rejects them.
 *
 * Search, filter and selection are state; the list shown is DERIVED from them and the inbox stream.
 * A decided task leaves the list because the command clears it through the store — never a reload.
 */
internal class CheckerInboxViewModel(
    repository: CheckerInboxRepository,
    private val commands: CheckerInboxCommandRepository,
) : BaseViewModel<CheckerInboxState, CheckerInboxEvent, CheckerInboxAction>(CheckerInboxState()) {

    private val taskStream = repository.checkerTasksStream(viewModelScope)
    private val rescheduleStream = repository.rescheduleRequestsStream(viewModelScope)

    val inbox: StateFlow<ScreenState<InboxView>> = taskStream.state
        .combineContent(stateFlow) { all, ui, _ ->
            InboxView(tasks = all.matching(ui.query, ui.filter).sortedByDescending { it.madeOnDate }, total = all.size)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    val reschedules: StateFlow<ScreenState<List<RescheduleLoansTask>>> = rescheduleStream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /** The filter's action and entity choices; empty until read, and the filter still works by text. */
    val template: StateFlow<CheckerInboxSearchTemplate> = flow { repository.searchTemplate()?.let { emit(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), CheckerInboxSearchTemplate())

    fun onRetry() = when (state.tab) {
        InboxTab.APPROVALS -> taskStream.retry()
        InboxTab.RESCHEDULES -> rescheduleStream.retry()
    }

    fun onRefresh() = when (state.tab) {
        InboxTab.APPROVALS -> taskStream.refresh(forceFresh = true)
        InboxTab.RESCHEDULES -> rescheduleStream.refresh(forceFresh = true)
    }

    override fun handleAction(action: CheckerInboxAction) {
        when (action) {
            is CheckerInboxAction.TabSelected,
            is CheckerInboxAction.QueryChanged,
            is CheckerInboxAction.TaskClicked,
            is CheckerInboxAction.TaskLongPressed,
            CheckerInboxAction.SelectionCleared,
            -> handleBrowse(action)
            is CheckerInboxAction.DecideRequested,
            CheckerInboxAction.DecideCancelled,
            CheckerInboxAction.DecideConfirmed,
            CheckerInboxAction.OutcomeDismissed,
            -> handleDecision(action)
            is CheckerInboxAction.DraftChanged,
            is CheckerInboxAction.OpenDatePicker,
            CheckerInboxAction.FilterOpened,
            CheckerInboxAction.FilterClosed,
            CheckerInboxAction.CloseDatePicker,
            CheckerInboxAction.FilterApplied,
            CheckerInboxAction.FilterCleared,
            -> handleFilter(action)
            is CheckerInboxAction.RescheduleClicked ->
                action.request.loanId?.let { sendEvent(CheckerInboxEvent.OpenLoanReschedules(it)) }
            CheckerInboxAction.NavigateBack -> sendEvent(CheckerInboxEvent.NavigateBack)
        }
    }

    private fun handleBrowse(action: CheckerInboxAction) {
        when (action) {
            is CheckerInboxAction.TabSelected -> update { copy(tab = action.tab, selected = emptySet()) }
            is CheckerInboxAction.QueryChanged -> update { copy(query = action.query) }
            is CheckerInboxAction.TaskClicked -> update {
                when {
                    selected.isNotEmpty() -> copy(selected = selected.toggle(action.id))
                    else -> copy(expanded = if (expanded == action.id) null else action.id)
                }
            }
            is CheckerInboxAction.TaskLongPressed ->
                update { copy(selected = selected.toggle(action.id), expanded = null) }
            CheckerInboxAction.SelectionCleared -> update { copy(selected = emptySet()) }
            else -> Unit
        }
    }

    private fun handleDecision(action: CheckerInboxAction) {
        when (action) {
            is CheckerInboxAction.DecideRequested -> update {
                val ids = action.id?.let { setOf(it) } ?: selected
                if (ids.isEmpty()) this else copy(confirm = PendingDecision(action.decision, ids))
            }
            CheckerInboxAction.DecideCancelled -> update { copy(confirm = null) }
            CheckerInboxAction.DecideConfirmed -> decide()
            CheckerInboxAction.OutcomeDismissed -> update { copy(outcome = null) }
            else -> Unit
        }
    }

    private fun handleFilter(action: CheckerInboxAction) {
        when (action) {
            CheckerInboxAction.FilterOpened -> update { copy(draft = filter) }
            CheckerInboxAction.FilterClosed -> update { copy(draft = null, datePickerFor = null) }
            is CheckerInboxAction.DraftChanged -> update { copy(draft = draft?.let(action.change)) }
            is CheckerInboxAction.OpenDatePicker -> update { copy(datePickerFor = action.field) }
            CheckerInboxAction.CloseDatePicker -> update { copy(datePickerFor = null) }
            CheckerInboxAction.FilterApplied -> update {
                val next = draft ?: return@update this
                if (!next.isValid) this else copy(filter = next, draft = null, selected = emptySet())
            }
            CheckerInboxAction.FilterCleared ->
                update { copy(filter = InboxFilter(), draft = null, selected = emptySet()) }
            else -> Unit
        }
    }

    private suspend fun decideOne(decision: Decision, id: Int): MutationResult<GenericResponse> = when (decision) {
        Decision.APPROVE -> commands.approveCheckerEntry(id)
        Decision.REJECT -> commands.rejectCheckerEntry(id)
        Decision.DELETE -> commands.deleteCheckerEntry(id)
    }

    /**
     * Decides each task in turn. One failure does not stop the rest — each is reported with the
     * server's reason; going offline does, since every later call would be refused the same way.
     */
    private fun decide() {
        val pending = state.confirm ?: return
        if (state.isDeciding) return
        update { copy(confirm = null, isDeciding = true) }
        viewModelScope.launch {
            val labels = inbox.value.dataOrNull?.tasks.orEmpty().associate { it.id to it.label }
            val decided = mutableSetOf<Int>()
            var offline = false
            val failures = mutableListOf<DecisionFailure>()
            for (id in pending.ids.sorted()) {
                val label = labels[id] ?: "#$id"
                when (val result = decideOne(pending.decision, id)) {
                    is MutationResult.Applied -> decided += id
                    is MutationResult.Blocked ->
                        if (result.reason == BlockReason.OFFLINE) {
                            offline = true
                        } else {
                            failures += DecisionFailure(label, null)
                        }
                    is MutationResult.Failed ->
                        failures += DecisionFailure(label, result.cause.serverErrorMessage())
                    is MutationResult.Conflicted -> failures += DecisionFailure(label, null)
                }
                if (offline) break
            }
            update {
                copy(
                    isDeciding = false,
                    // What was not decided stays selected, so a failure can be retried as is.
                    selected = selected - decided,
                    expanded = null,
                    outcome = DecisionOutcome(pending.decision, decided.size, failures, offline),
                )
            }
        }
    }

    private inline fun update(change: CheckerInboxState.() -> CheckerInboxState) {
        mutableStateFlow.value = state.change()
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

private fun Set<Int>.toggle(id: Int) = if (id in this) this - id else this + id

/** How a task is named in an outcome — the original's own heading. */
internal val CheckerTask.label get() = "#$id $actionName $entityName"

/**
 * The original's filter, fixed: it ignored action and entity unless a date was set, returned NOTHING
 * when only a From date was set, and excluded both end dates. Every criterion applies on its own here,
 * and the range is inclusive by calendar day.
 */
@OptIn(ExperimentalTime::class)
internal fun List<CheckerTask>.matching(query: String, filter: InboxFilter): List<CheckerTask> = filter { task ->
    val day = Instant.fromEpochMilliseconds(task.madeOnDate).toLocalDateTime(TimeZone.currentSystemDefault()).date
    (query.isBlank() || task.maker.contains(query.trim(), ignoreCase = true)) &&
        (filter.action == null || task.actionName.equals(filter.action, ignoreCase = true)) &&
        (filter.entity == null || task.entityName.equals(filter.entity, ignoreCase = true)) &&
        (filter.resourceId.isBlank() || task.resourceId == filter.resourceId.trim()) &&
        (filter.from == null || day >= filter.from) &&
        (filter.to == null || day <= filter.to)
}

internal enum class InboxTab { APPROVALS, RESCHEDULES }

internal enum class Decision { APPROVE, REJECT, DELETE }

internal enum class DateField { FROM, TO }

internal data class InboxFilter(
    /** Null = all. */
    val action: String? = null,
    val entity: String? = null,
    val resourceId: String = "",
    val from: LocalDate? = null,
    val to: LocalDate? = null,
) {
    val isActive get() = this != InboxFilter()
    val isValid get() = from == null || to == null || from <= to
}

/** The tasks shown, and how many are pending in all — so an empty RESULT is told apart from an empty inbox. */
internal data class InboxView(val tasks: List<CheckerTask>, val total: Int)

internal data class PendingDecision(val decision: Decision, val ids: Set<Int>)

internal data class DecisionFailure(val task: String, val serverMessage: String?)

internal data class DecisionOutcome(
    val decision: Decision,
    val done: Int,
    val failures: List<DecisionFailure>,
    val offline: Boolean,
)

internal data class CheckerInboxState(
    val tab: InboxTab = InboxTab.APPROVALS,
    val query: String = "",
    val filter: InboxFilter = InboxFilter(),
    /** The filter sheet's working copy; null while the sheet is closed. */
    val draft: InboxFilter? = null,
    val datePickerFor: DateField? = null,
    val selected: Set<Int> = emptySet(),
    val expanded: Int? = null,
    val confirm: PendingDecision? = null,
    val isDeciding: Boolean = false,
    val outcome: DecisionOutcome? = null,
)

internal sealed interface CheckerInboxEvent {
    data object NavigateBack : CheckerInboxEvent
    data class OpenLoanReschedules(val loanId: Int) : CheckerInboxEvent
}

internal sealed interface CheckerInboxAction {
    data class TabSelected(val tab: InboxTab) : CheckerInboxAction
    data class QueryChanged(val query: String) : CheckerInboxAction
    data class TaskClicked(val id: Int) : CheckerInboxAction
    data class TaskLongPressed(val id: Int) : CheckerInboxAction
    data object SelectionCleared : CheckerInboxAction

    /** [id] for one task's own buttons; null for the selection. */
    data class DecideRequested(val decision: Decision, val id: Int? = null) : CheckerInboxAction
    data object DecideCancelled : CheckerInboxAction
    data object DecideConfirmed : CheckerInboxAction
    data object OutcomeDismissed : CheckerInboxAction
    data object FilterOpened : CheckerInboxAction
    data object FilterClosed : CheckerInboxAction
    data class DraftChanged(val change: (InboxFilter) -> InboxFilter) : CheckerInboxAction
    data class OpenDatePicker(val field: DateField) : CheckerInboxAction
    data object CloseDatePicker : CheckerInboxAction
    data object FilterApplied : CheckerInboxAction
    data object FilterCleared : CheckerInboxAction
    data class RescheduleClicked(val request: RescheduleLoansTask) : CheckerInboxAction
    data object NavigateBack : CheckerInboxAction
}
