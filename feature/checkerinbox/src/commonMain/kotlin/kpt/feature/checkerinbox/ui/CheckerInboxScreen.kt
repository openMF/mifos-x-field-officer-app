/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.checkerinbox.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.dataOrNull
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.objects.checkerinboxtask.CheckerInboxSearchTemplate
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.model.objects.checkerinboxtask.RescheduleLoansTask
import kpt.core.ui.components.AppEmptyCard
import kpt.feature.checkerinbox.generated.resources.Res
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_action
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_all
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_apply_filter
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_approve
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_cancel
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_clear_filter
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_clear_selection
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_confirm_approve
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_confirm_delete
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_confirm_reject
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_create_by
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_delete
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_done
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_entity
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_failures
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_filter
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_from_date
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_invalid_date_range
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_no
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_no_match
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_no_reschedules
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_nothing_pending
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_offline
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_ok
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_reject
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_reschedule_from
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_resource
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_resource_id
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_search_by_user
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_select
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_selected
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_tab_approvals
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_tab_reschedules
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_title
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_to_date
import kpt.feature.checkerinbox.generated.resources.feature_checkerinbox_yes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** See [CheckerInboxViewModel]. */
@Composable
internal fun CheckerInboxScreen(
    onNavigateBack: () -> Unit,
    onOpenLoanReschedules: (loanId: Int) -> Unit,
    viewModel: CheckerInboxViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val inbox by viewModel.inbox.collectAsStateWithLifecycle()
    val reschedules by viewModel.reschedules.collectAsStateWithLifecycle()
    val template by viewModel.template.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            CheckerInboxEvent.NavigateBack -> onNavigateBack()
            is CheckerInboxEvent.OpenLoanReschedules -> onOpenLoanReschedules(event.loanId)
        }
    }

    CheckerInboxContent(
        state = state,
        inbox = inbox,
        reschedules = reschedules,
        template = template,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        onRefresh = viewModel::onRefresh,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun CheckerInboxContent(
    state: CheckerInboxState,
    inbox: ScreenState<InboxView>,
    reschedules: ScreenState<List<RescheduleLoansTask>>,
    template: CheckerInboxSearchTemplate,
    onAction: (CheckerInboxAction) -> Unit,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {},
) {
    AppScaffold(
        title = stringResource(Res.string.feature_checkerinbox_title),
        onBackPressed = { onAction(CheckerInboxAction.NavigateBack) },
        modifier = modifier,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).testTag(CheckerInboxTestTags.SCREEN)) {
            PrimaryTabRow(selectedTabIndex = state.tab.ordinal) {
                Tab(
                    selected = state.tab == InboxTab.APPROVALS,
                    onClick = { onAction(CheckerInboxAction.TabSelected(InboxTab.APPROVALS)) },
                    text = {
                        Text(
                            stringResource(Res.string.feature_checkerinbox_tab_approvals, inbox.dataOrNull?.total ?: 0),
                        )
                    },
                    modifier = Modifier.testTag(CheckerInboxTestTags.TAB_APPROVALS),
                )
                Tab(
                    selected = state.tab == InboxTab.RESCHEDULES,
                    onClick = { onAction(CheckerInboxAction.TabSelected(InboxTab.RESCHEDULES)) },
                    text = {
                        Text(
                            stringResource(
                                Res.string.feature_checkerinbox_tab_reschedules,
                                reschedules.dataOrNull?.size ?: 0,
                            ),
                        )
                    },
                    modifier = Modifier.testTag(CheckerInboxTestTags.TAB_RESCHEDULES),
                )
            }
            if (state.isDeciding) LinearProgressIndicator(Modifier.fillMaxWidth())
            when (state.tab) {
                InboxTab.APPROVALS -> Approvals(state, inbox, onAction, onRefresh)
                InboxTab.RESCHEDULES -> Reschedules(reschedules, onAction, onRefresh)
            }
        }
    }
    state.draft?.let { FilterSheet(it, template, onAction) }
    state.datePickerFor?.let { field -> DateDialog(field, state.draft, onAction) }
    state.confirm?.let { ConfirmDialog(it, onAction) }
    state.outcome?.let { OutcomeDialog(it, onAction) }
}

@Composable
private fun Approvals(
    state: CheckerInboxState,
    inbox: ScreenState<InboxView>,
    onAction: (CheckerInboxAction) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        if (state.selected.isNotEmpty()) {
            SelectionBar(state.selected.size, onAction)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppOutlinedTextField(
                    value = state.query,
                    onValueChange = { onAction(CheckerInboxAction.QueryChanged(it)) },
                    label = stringResource(Res.string.feature_checkerinbox_search_by_user),
                    modifier = Modifier.weight(1f).testTag(CheckerInboxTestTags.SEARCH),
                )
                IconButton(
                    onClick = { onAction(CheckerInboxAction.FilterOpened) },
                    modifier = Modifier.testTag(CheckerInboxTestTags.FILTER),
                ) {
                    BadgedBox(badge = { if (state.filter.isActive) Badge() }) {
                        Icon(
                            AppIcons.FilterList,
                            contentDescription = stringResource(Res.string.feature_checkerinbox_filter),
                        )
                    }
                }
            }
        }
        RefreshableScreenContent(state = inbox, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) { view, _ ->
            when {
                view.total == 0 -> Empty(Res.string.feature_checkerinbox_nothing_pending)
                view.tasks.isEmpty() -> Empty(Res.string.feature_checkerinbox_no_match)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag(CheckerInboxTestTags.LIST),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    items(view.tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            selected = task.id in state.selected,
                            expanded = state.expanded == task.id && state.selected.isEmpty(),
                            onAction = onAction,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionBar(count: Int, onAction: (CheckerInboxAction) -> Unit) {
    Surface(
        color = KptTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.sm).testTag(CheckerInboxTestTags.SELECTION_BAR),
        shape = KptTheme.shapes.medium,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onAction(CheckerInboxAction.SelectionCleared) }) {
                Icon(
                    AppIcons.Close,
                    contentDescription = stringResource(Res.string.feature_checkerinbox_clear_selection),
                )
            }
            Text(
                text = stringResource(Res.string.feature_checkerinbox_selected, count),
                style = KptTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            DecisionIcons(onAction, id = null)
        }
    }
}

/** Approve · reject · delete — for one task ([id]) or for the selection (null). */
@Composable
private fun DecisionIcons(
    onAction: (CheckerInboxAction) -> Unit,
    id: Int?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        IconButton(
            onClick = { onAction(CheckerInboxAction.DecideRequested(Decision.APPROVE, id)) },
            modifier = Modifier.testTag(CheckerInboxTestTags.APPROVE),
        ) {
            Icon(
                AppIcons.Check,
                contentDescription = stringResource(Res.string.feature_checkerinbox_approve),
                tint = KptTheme.colorScheme.primary,
            )
        }
        IconButton(
            onClick = { onAction(CheckerInboxAction.DecideRequested(Decision.REJECT, id)) },
            modifier = Modifier.testTag(CheckerInboxTestTags.REJECT),
        ) {
            Icon(
                AppIcons.Cancel,
                contentDescription = stringResource(Res.string.feature_checkerinbox_reject),
                tint = KptTheme.colorScheme.tertiary,
            )
        }
        IconButton(
            onClick = { onAction(CheckerInboxAction.DecideRequested(Decision.DELETE, id)) },
            modifier = Modifier.testTag(CheckerInboxTestTags.DELETE),
        ) {
            Icon(
                AppIcons.Delete,
                contentDescription = stringResource(Res.string.feature_checkerinbox_delete),
                tint = KptTheme.colorScheme.error,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskCard(task: CheckerTask, selected: Boolean, expanded: Boolean, onAction: (CheckerInboxAction) -> Unit) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) KptTheme.colorScheme.primaryContainer else KptTheme.colorScheme.surface,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md)
            .combinedClickable(
                onClick = { onAction(CheckerInboxAction.TaskClicked(task.id)) },
                onLongClick = { onAction(CheckerInboxAction.TaskLongPressed(task.id)) },
            )
            .testTag(CheckerInboxTestTags.task(task.id)),
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = { Text(task.label) },
            supportingContent = {
                Column {
                    Text(
                        stringResource(Res.string.feature_checkerinbox_resource, task.resourceId),
                        style = KptTheme.typography.bodySmall,
                    )
                    val createBy = stringResource(Res.string.feature_checkerinbox_create_by)
                    Text(
                        "$createBy ${task.maker} · ${task.getDate()}",
                        style = KptTheme.typography.bodySmall,
                    )
                }
            },
        )
        if (expanded) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = KptTheme.spacing.sm),
                horizontalArrangement = Arrangement.End,
            ) { DecisionIcons(onAction, id = task.id) }
        }
    }
}

@Composable
private fun Reschedules(
    reschedules: ScreenState<List<RescheduleLoansTask>>,
    onAction: (CheckerInboxAction) -> Unit,
    onRefresh: () -> Unit,
) {
    RefreshableScreenContent(state = reschedules, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) { list, _ ->
        if (list.isEmpty()) {
            Empty(Res.string.feature_checkerinbox_no_reschedules)
            return@RefreshableScreenContent
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(top = KptTheme.spacing.sm)
                .testTag(CheckerInboxTestTags.RESCHEDULES),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            items(list, key = { it.id }) { request ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = KptTheme.spacing.md)
                        .clickable(enabled = request.loanId != null) {
                            onAction(CheckerInboxAction.RescheduleClicked(request))
                        }
                        .testTag(CheckerInboxTestTags.RESCHEDULE),
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(request.clientName) },
                        supportingContent = {
                            Text(
                                listOfNotNull(
                                    request.loanAccountNumber.ifBlank { null },
                                    request.rescheduleFromDate.toIsoDate()?.let {
                                        stringResource(Res.string.feature_checkerinbox_reschedule_from, it)
                                    },
                                    request.rescheduleReasonCodeValue?.name?.ifBlank { null },
                                ).joinToString(" · "),
                            )
                        },
                        trailingContent = {
                            if (request.loanId != null) Icon(AppIcons.ChevronRight, contentDescription = null)
                        },
                    )
                }
            }
        }
    }
}

/** Fineract's `[yyyy, M, d]` date. */
private fun List<Int>.toIsoDate(): String? =
    if (size == 3) runCatching { LocalDate(this[0], this[1], this[2]).toString() }.getOrNull() else null

@Composable
private fun Empty(message: org.jetbrains.compose.resources.StringResource) {
    AppEmptyCard(msg = stringResource(message), modifier = Modifier.padding(KptTheme.spacing.md))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    draft: InboxFilter,
    template: CheckerInboxSearchTemplate,
    onAction: (CheckerInboxAction) -> Unit,
) {
    val all = stringResource(Res.string.feature_checkerinbox_all)
    ModalBottomSheet(onDismissRequest = { onAction(CheckerInboxAction.FilterClosed) }) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(KptTheme.spacing.md)
                .testTag(CheckerInboxTestTags.FILTER_SHEET),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            NameDropdown(
                stringResource(Res.string.feature_checkerinbox_action),
                all,
                draft.action,
                template.actionNames,
            ) { value ->
                onAction(CheckerInboxAction.DraftChanged { it.copy(action = value) })
            }
            NameDropdown(
                stringResource(Res.string.feature_checkerinbox_entity),
                all,
                draft.entity,
                template.entityNames,
            ) { value ->
                onAction(CheckerInboxAction.DraftChanged { it.copy(entity = value) })
            }
            AppOutlinedTextField(
                value = draft.resourceId,
                onValueChange = { value -> onAction(CheckerInboxAction.DraftChanged { it.copy(resourceId = value) }) },
                label = stringResource(Res.string.feature_checkerinbox_resource_id),
                modifier = Modifier.fillMaxWidth(),
            )
            val rangeError = if (draft.isValid) {
                null
            } else {
                stringResource(Res.string.feature_checkerinbox_invalid_date_range)
            }
            AppDatePickerTextField(
                value = draft.from?.toString().orEmpty(),
                label = stringResource(Res.string.feature_checkerinbox_from_date),
                errorMessage = rangeError,
                openDatePicker = { onAction(CheckerInboxAction.OpenDatePicker(DateField.FROM)) },
                modifier = Modifier.fillMaxWidth(),
            )
            AppDatePickerTextField(
                value = draft.to?.toString().orEmpty(),
                label = stringResource(Res.string.feature_checkerinbox_to_date),
                openDatePicker = { onAction(CheckerInboxAction.OpenDatePicker(DateField.TO)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm)) {
                TextButton(onClick = { onAction(CheckerInboxAction.FilterCleared) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.feature_checkerinbox_clear_filter))
                }
                AppButton(
                    onClick = { onAction(CheckerInboxAction.FilterApplied) },
                    enabled = draft.isValid,
                    modifier = Modifier.weight(1f).testTag(CheckerInboxTestTags.APPLY_FILTER),
                ) { Text(stringResource(Res.string.feature_checkerinbox_apply_filter)) }
            }
        }
    }
}

/** "All" first, then the server's names. Picking "All" clears the criterion. */
@Composable
private fun NameDropdown(label: String, all: String, value: String?, names: List<String>, onPick: (String?) -> Unit) {
    val choices = listOf(all) + names
    AppTextFieldDropdown(
        value = value ?: all,
        onOptionSelected = { index, _ -> onPick(if (index == 0) null else names[index - 1]) },
        options = choices,
        label = label,
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun DateDialog(field: DateField, draft: InboxFilter?, onAction: (CheckerInboxAction) -> Unit) {
    // The picker speaks UTC-midnight millis; convert both ways in UTC so no time zone shifts the day.
    val current = if (field == DateField.FROM) draft?.from else draft?.to
    val picker = rememberDatePickerState(
        initialSelectedDateMillis = current?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
    )
    DatePickerDialog(
        onDismissRequest = { onAction(CheckerInboxAction.CloseDatePicker) },
        confirmButton = {
            TextButton(onClick = {
                val date = picker.selectedDateMillis?.let {
                    Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                }
                onAction(
                    CheckerInboxAction.DraftChanged {
                        if (field == DateField.FROM) it.copy(from = date) else it.copy(to = date)
                    },
                )
                onAction(CheckerInboxAction.CloseDatePicker)
            }) { Text(stringResource(Res.string.feature_checkerinbox_select)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(CheckerInboxAction.CloseDatePicker) }) {
                Text(stringResource(Res.string.feature_checkerinbox_cancel))
            }
        },
    ) { DatePicker(state = picker) }
}

@Composable
private fun ConfirmDialog(pending: PendingDecision, onAction: (CheckerInboxAction) -> Unit) {
    val question = when (pending.decision) {
        Decision.APPROVE -> Res.string.feature_checkerinbox_confirm_approve
        Decision.REJECT -> Res.string.feature_checkerinbox_confirm_reject
        Decision.DELETE -> Res.string.feature_checkerinbox_confirm_delete
    }
    AlertDialog(
        onDismissRequest = { onAction(CheckerInboxAction.DecideCancelled) },
        text = { Text(stringResource(question, pending.ids.size)) },
        confirmButton = {
            TextButton(
                onClick = { onAction(CheckerInboxAction.DecideConfirmed) },
                modifier = Modifier.testTag(CheckerInboxTestTags.CONFIRM),
            ) {
                Text(stringResource(Res.string.feature_checkerinbox_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(CheckerInboxAction.DecideCancelled) }) {
                Text(stringResource(Res.string.feature_checkerinbox_no))
            }
        },
    )
}

@Composable
private fun OutcomeDialog(outcome: DecisionOutcome, onAction: (CheckerInboxAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(CheckerInboxAction.OutcomeDismissed) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm)) {
                if (outcome.done > 0) Text(stringResource(Res.string.feature_checkerinbox_done, outcome.done))
                if (outcome.offline) {
                    Text(stringResource(Res.string.feature_checkerinbox_offline), color = KptTheme.colorScheme.error)
                }
                if (outcome.failures.isNotEmpty()) {
                    Text(
                        stringResource(Res.string.feature_checkerinbox_failures, outcome.failures.size),
                        color = KptTheme.colorScheme.error,
                    )
                    Column(Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                        outcome.failures.forEach { failure ->
                            Text(
                                text = failure.serverMessage?.let { "${failure.task} — $it" } ?: failure.task,
                                style = KptTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAction(CheckerInboxAction.OutcomeDismissed) }) {
                Text(stringResource(Res.string.feature_checkerinbox_ok))
            }
        },
        modifier = Modifier.testTag(CheckerInboxTestTags.OUTCOME),
    )
}

internal object CheckerInboxTestTags {
    const val SCREEN = "checkerInbox:screen"
    const val TAB_APPROVALS = "checkerInbox:tab:approvals"
    const val TAB_RESCHEDULES = "checkerInbox:tab:reschedules"
    const val SEARCH = "checkerInbox:search"
    const val FILTER = "checkerInbox:filter"
    const val FILTER_SHEET = "checkerInbox:filterSheet"
    const val APPLY_FILTER = "checkerInbox:applyFilter"
    const val LIST = "checkerInbox:list"
    const val SELECTION_BAR = "checkerInbox:selectionBar"
    const val APPROVE = "checkerInbox:approve"
    const val REJECT = "checkerInbox:reject"
    const val DELETE = "checkerInbox:delete"
    const val CONFIRM = "checkerInbox:confirm"
    const val OUTCOME = "checkerInbox:outcome"
    const val RESCHEDULES = "checkerInbox:reschedules"
    const val RESCHEDULE = "checkerInbox:reschedule"

    fun task(id: Int) = "checkerInbox:task:$id"
}
