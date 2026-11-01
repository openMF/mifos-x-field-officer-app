/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.center.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppMenuDropDownItem
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.center.Center
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.group.Group
import kpt.core.model.objects.groups.CenterInfo
import kpt.feature.center.generated.resources.Res
import kpt.feature.center.generated.resources.feature_center_account_no
import kpt.feature.center.generated.resources.feature_center_activate
import kpt.feature.center.generated.resources.feature_center_activation_date
import kpt.feature.center.generated.resources.feature_center_active_client
import kpt.feature.center.generated.resources.feature_center_active_client_borrowers
import kpt.feature.center.generated.resources.feature_center_active_client_loans
import kpt.feature.center.generated.resources.feature_center_active_group_borrowers
import kpt.feature.center.generated.resources.feature_center_active_group_loan
import kpt.feature.center.generated.resources.feature_center_add_savings_account
import kpt.feature.center.generated.resources.feature_center_back
import kpt.feature.center.generated.resources.feature_center_detail_title
import kpt.feature.center.generated.resources.feature_center_external_id
import kpt.feature.center.generated.resources.feature_center_groups
import kpt.feature.center.generated.resources.feature_center_meeting_frequency
import kpt.feature.center.generated.resources.feature_center_more_options
import kpt.feature.center.generated.resources.feature_center_next_meeting_on
import kpt.feature.center.generated.resources.feature_center_office
import kpt.feature.center.generated.resources.feature_center_overdue_client_loans
import kpt.feature.center.generated.resources.feature_center_overdue_group_loans
import kpt.feature.center.generated.resources.feature_center_staff
import kpt.feature.center.generated.resources.feature_center_status_active
import kpt.feature.center.generated.resources.feature_center_status_inactive
import kpt.feature.center.generated.resources.feature_center_summary_info
import kpt.feature.center.generated.resources.feature_center_unassigned
import kpt.feature.center.generated.resources.feature_center_unnamed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CenterDetailScreen(
    onBackClick: () -> Unit,
    onGroupClick: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    // Activation is the shared `feature/activate` screen, supplied by the centre graph.
    onActivate: ((Int) -> Unit)? = null,
    onAddSavingsAccount: ((Int) -> Unit)? = null,
    viewModel: CenterDetailViewModel = koinViewModel(),
) {
    // Two independent flows — a failure in one must not blank the other.
    val center by viewModel.center.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val canActivate by viewModel.canActivate.collectAsStateWithLifecycle()
    val meeting by viewModel.meeting.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CenterDetailEvent.OpenGroup -> onGroupClick?.invoke(event.groupId)
                CenterDetailEvent.NavigateBack -> onBackClick()
                is CenterDetailEvent.Activate -> onActivate?.invoke(event.centerId)
                is CenterDetailEvent.OpenAddSavingsAccount -> onAddSavingsAccount?.invoke(event.centerId)
            }
        }
    }

    CenterDetailContent(
        center = center,
        groups = groups,
        onAction = { viewModel.actionChannel.trySend(it) },
        // The button needs BOTH an inactive centre and somewhere to send it.
        canActivate = canActivate && onActivate != null,
        groupsClickable = onGroupClick != null,
        meeting = meeting,
        summary = summary,
        canAddSavingsAccount = onAddSavingsAccount != null,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CenterDetailContent(
    center: ScreenState<Center>,
    groups: ScreenState<List<Group>>,
    onAction: (CenterDetailAction) -> Unit,
    modifier: Modifier = Modifier,
    groupsClickable: Boolean = true,
    canActivate: Boolean = false,
    meeting: ScreenState<CenterWithAssociations> = ScreenState.Loading,
    summary: ScreenState<CenterInfo> = ScreenState.Loading,
    canAddSavingsAccount: Boolean = false,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(CenterDetailTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feature_center_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(CenterDetailAction.BackClicked) }) {
                        Icon(
                            AppIcons.ArrowBack,
                            contentDescription = stringResource(Res.string.feature_center_back),
                        )
                    }
                },
                actions = {
                    // The original's overflow: "Add savings account" (its "Group list" entry is the
                    // Groups section below). Absent, not inert, when there is nowhere to send it.
                    if (canAddSavingsAccount) {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.testTag(CenterDetailTestTags.OVERFLOW),
                        ) {
                            Icon(
                                AppIcons.MoreVert,
                                contentDescription = stringResource(Res.string.feature_center_more_options),
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            AppMenuDropDownItem(
                                option = stringResource(Res.string.feature_center_add_savings_account),
                                onClick = {
                                    expanded = false
                                    onAction(CenterDetailAction.AddSavingsAccountClicked)
                                },
                                modifier = Modifier.testTag(CenterDetailTestTags.ADD_SAVINGS_ACCOUNT),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        // ONE scrolling list. The sections carry SEPARATE ScreenStates and render independently: a
        // groups / meeting / summary read that fails must not blank the center's details, which are
        // already on screen and still true — the reason the original's two screens could be merged.
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                ScreenContent(
                    state = center,
                    onRetry = { onAction(CenterDetailAction.RetryCenter) },
                    modifier = Modifier.fillMaxWidth(),
                ) { loaded, _ ->
                    CenterFields(loaded)
                }
            }

            // Drawn only where it can succeed — an already-active centre has no Activate button,
            // and neither does one with nowhere to send the tap.
            if (canActivate) {
                item {
                    AppButton(
                        onClick = { onAction(CenterDetailAction.ActivateClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(CenterDetailTestTags.ACTIVATE),
                    ) {
                        Text(stringResource(Res.string.feature_center_activate))
                    }
                }
            }

            item {
                ScreenContent(
                    state = meeting,
                    onRetry = { onAction(CenterDetailAction.RetryMeeting) },
                    modifier = Modifier.fillMaxWidth().testTag(CenterDetailTestTags.MEETING),
                    refreshingIndicator = null,
                ) { loaded, _ ->
                    MeetingFields(loaded)
                }
            }

            item {
                SectionTitle(stringResource(Res.string.feature_center_summary_info))
                ScreenContent(
                    state = summary,
                    onRetry = { onAction(CenterDetailAction.RetrySummary) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag(CenterDetailTestTags.SUMMARY),
                    refreshingIndicator = null,
                ) { info, _ ->
                    SummaryFields(info)
                }
            }

            item { SectionTitle(stringResource(Res.string.feature_center_groups)) }
            item {
                ScreenContent(
                    state = groups,
                    onRetry = { onAction(CenterDetailAction.RetryGroups) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp).testTag(CenterDetailTestTags.GROUPS),
                ) { rows, _ ->
                    Column {
                        rows.forEach { group ->
                            // See CenterListScreen — nullable cross-module property, no smart cast.
                            val groupId = group.id
                            GroupRow(
                                group = group,
                                onClick = if (groupsClickable && groupId != null) {
                                    { onAction(CenterDetailAction.GroupClicked(groupId)) }
                                } else {
                                    null
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

/** The original's activation-date / next-meeting / frequency rows. */
@Composable
private fun MeetingFields(center: CenterWithAssociations) {
    val calendar = center.collectionMeetingCalendar
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (center.activationDate.isNotEmpty()) {
            Field(
                stringResource(Res.string.feature_center_activation_date),
                DateHelper.getDateAsString(center.activationDate),
            )
        }
        Field(
            stringResource(Res.string.feature_center_next_meeting_on),
            // No calendar, or one with no upcoming date, is "Unassigned" — the original's wording.
            calendar.nextTenRecurringDates.firstOrNull()?.takeIf { calendar.calendarInstanceId != null }
                ?.let { DateHelper.getDateAsString(it) }
                ?: stringResource(Res.string.feature_center_unassigned),
        )
        calendar.humanReadable?.let { Field(stringResource(Res.string.feature_center_meeting_frequency), it) }
    }
}

/**
 * The original's "Summary info". Its last row was labelled "Active group loans" while showing
 * OVERDUE CLIENT loans — a copy-paste slip; here each count carries its own label.
 */
@Composable
private fun SummaryFields(info: CenterInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Field(stringResource(Res.string.feature_center_active_client), info.activeClients.orZero())
        Field(stringResource(Res.string.feature_center_active_group_loan), info.activeGroupLoans.orZero())
        Field(stringResource(Res.string.feature_center_active_client_loans), info.activeClientLoans.orZero())
        Field(stringResource(Res.string.feature_center_active_group_borrowers), info.activeGroupBorrowers.orZero())
        Field(stringResource(Res.string.feature_center_active_client_borrowers), info.activeClientBorrowers.orZero())
        Field(stringResource(Res.string.feature_center_overdue_group_loans), info.overdueGroupLoans.orZero())
        Field(stringResource(Res.string.feature_center_overdue_client_loans), info.overdueClientLoans.orZero())
    }
}

private fun Int?.orZero(): String = (this ?: 0).toString()

@Composable
private fun CenterFields(center: Center) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = center.name ?: stringResource(Res.string.feature_center_unnamed),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(
                if (center.active == true) {
                    Res.string.feature_center_status_active
                } else {
                    Res.string.feature_center_status_inactive
                },
            ),
            style = MaterialTheme.typography.labelLarge,
        )
        center.accountNo?.let { Field(stringResource(Res.string.feature_center_account_no), it) }
        center.officeName?.let { Field(stringResource(Res.string.feature_center_office), it) }
        center.staffName?.let { Field(stringResource(Res.string.feature_center_staff), it) }
        center.externalId?.let { Field(stringResource(Res.string.feature_center_external_id), it) }
    }
}

@Composable
private fun Field(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun GroupRow(group: Group, onClick: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp)
            .testTag(CenterDetailTestTags.GROUP_ROW),
    ) {
        Text(text = group.name.orEmpty(), style = MaterialTheme.typography.bodyLarge)
        group.accountNo?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}
