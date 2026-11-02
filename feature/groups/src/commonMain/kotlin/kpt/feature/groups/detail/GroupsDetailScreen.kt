/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kpt.core.designsystem.fieldofficer.component.AppMenuDropDownItem
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.client.Client
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.feature.groups.generated.resources.Res
import kpt.feature.groups.generated.resources.feature_groups_account_no
import kpt.feature.groups.generated.resources.feature_groups_activate
import kpt.feature.groups.generated.resources.feature_groups_add_loan_account
import kpt.feature.groups.generated.resources.feature_groups_add_savings_account
import kpt.feature.groups.generated.resources.feature_groups_back
import kpt.feature.groups.generated.resources.feature_groups_center
import kpt.feature.groups.generated.resources.feature_groups_detail_title
import kpt.feature.groups.generated.resources.feature_groups_documents
import kpt.feature.groups.generated.resources.feature_groups_external_id
import kpt.feature.groups.generated.resources.feature_groups_group_clients
import kpt.feature.groups.generated.resources.feature_groups_loan_accounts
import kpt.feature.groups.generated.resources.feature_groups_more_group_info
import kpt.feature.groups.generated.resources.feature_groups_more_options
import kpt.feature.groups.generated.resources.feature_groups_notes
import kpt.feature.groups.generated.resources.feature_groups_office
import kpt.feature.groups.generated.resources.feature_groups_savings_accounts
import kpt.feature.groups.generated.resources.feature_groups_staff
import kpt.feature.groups.generated.resources.feature_groups_status_active
import kpt.feature.groups.generated.resources.feature_groups_status_inactive
import kpt.feature.groups.generated.resources.feature_groups_unnamed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun GroupsDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    // Activation is a screen in `feature/activate` (shared with client and centre), so the group
    // graph supplies the destination. `null` would hide the Activate button rather than draw it
    // inert, but it is always wired — the shared screen is ported.
    onActivate: ((Int) -> Unit)? = null,
    onDocuments: ((Int) -> Unit)? = null,
    onDataTables: ((Int) -> Unit)? = null,
    onAddLoanAccount: ((Int) -> Unit)? = null,
    onAddSavingsAccount: ((Int) -> Unit)? = null,
    onNotes: ((Int) -> Unit)? = null,
    onOpenLoan: ((Int) -> Unit)? = null,
    onOpenSavings: ((accountId: Int, depositTypeEndpoint: String) -> Unit)? = null,
    onOpenClient: ((Int) -> Unit)? = null,
    viewModel: GroupsDetailViewModel = koinViewModel(),
) {
    val group by viewModel.group.collectAsStateWithLifecycle()
    val canActivate by viewModel.canActivate.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                GroupsDetailEvent.NavigateBack -> onBackClick()
                is GroupsDetailEvent.Activate -> onActivate?.invoke(event.groupId)
                is GroupsDetailEvent.OpenDocuments -> onDocuments?.invoke(event.groupId)
                is GroupsDetailEvent.OpenDataTables -> onDataTables?.invoke(event.groupId)
                is GroupsDetailEvent.OpenAddLoanAccount ->
                    onAddLoanAccount?.invoke(event.groupId)
                is GroupsDetailEvent.OpenAddSavingsAccount ->
                    onAddSavingsAccount?.invoke(event.groupId)
                is GroupsDetailEvent.OpenNotes -> onNotes?.invoke(event.groupId)
                is GroupsDetailEvent.OpenLoan -> onOpenLoan?.invoke(event.loanId)
                is GroupsDetailEvent.OpenSavings -> onOpenSavings?.invoke(event.accountId, event.depositTypeEndpoint)
                is GroupsDetailEvent.OpenClient -> onOpenClient?.invoke(event.clientId)
            }
        }
    }

    GroupsDetailContent(
        group = group,
        // The button needs BOTH an inactive group and somewhere to send it.
        canActivate = canActivate && onActivate != null,
        canOpenDocuments = onDocuments != null,
        canOpenDataTables = onDataTables != null,
        canAddLoanAccount = onAddLoanAccount != null,
        canAddSavingsAccount = onAddSavingsAccount != null,
        canOpenNotes = onNotes != null,
        members = members,
        accounts = accounts,
        canOpenLoan = onOpenLoan != null,
        canOpenSavings = onOpenSavings != null,
        canOpenClient = onOpenClient != null,
        onAction = { viewModel.actionChannel.trySend(it) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroupsDetailContent(
    group: ScreenState<Group>,
    canActivate: Boolean,
    onAction: (GroupsDetailAction) -> Unit,
    modifier: Modifier = Modifier,
    canOpenDocuments: Boolean = false,
    canOpenDataTables: Boolean = false,
    canAddLoanAccount: Boolean = false,
    canAddSavingsAccount: Boolean = false,
    canOpenNotes: Boolean = false,
    members: ScreenState<List<Client>> = ScreenState.Loading,
    accounts: ScreenState<GroupAccounts> = ScreenState.Loading,
    canOpenLoan: Boolean = false,
    canOpenSavings: Boolean = false,
    canOpenClient: Boolean = false,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(GroupsDetailTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feature_groups_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(GroupsDetailAction.BackClicked) }) {
                        Icon(
                            AppIcons.ArrowBack,
                            contentDescription = stringResource(Res.string.feature_groups_back),
                        )
                    }
                },
                actions = {
                    OverflowMenu(
                        canAddLoanAccount = canAddLoanAccount,
                        canAddSavingsAccount = canAddSavingsAccount,
                        canOpenDocuments = canOpenDocuments,
                        canOpenDataTables = canOpenDataTables,
                        canOpenNotes = canOpenNotes,
                        onAction = onAction,
                    )
                },
            )
        },
    ) { padding ->
        // Scrollable: seven fields plus the Activate button overflow a short screen, and this
        // Column had no scroll — which put the one WRITE the screen offers out of reach on exactly
        // the groups that need it. Found by the render test asserting both affordances are visible.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ScreenContent(
                state = group,
                onRetry = { onAction(GroupsDetailAction.Retry) },
                modifier = Modifier.fillMaxWidth(),
            ) { loaded, _ ->
                GroupFields(loaded)
            }

            // The original's Accounts section (loan + savings cards), and the group's clients.
            // Each loads on its own, so a slow accounts read never hides the group's fields.
            ScreenContent(
                state = accounts,
                onRetry = { onAction(GroupsDetailAction.RetryAccounts) },
                modifier = Modifier.fillMaxWidth().testTag(GroupsDetailTestTags.ACCOUNTS),
                refreshingIndicator = null,
            ) { loaded, _ ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (loaded.loanAccounts.isNotEmpty()) {
                        ExpandableSection(
                            title = stringResource(Res.string.feature_groups_loan_accounts),
                            count = loaded.loanAccounts.size,
                            tag = GroupsDetailTestTags.LOAN_ACCOUNTS,
                        ) {
                            loaded.loanAccounts.forEach { account ->
                                LoanAccountRow(
                                    account,
                                    if (canOpenLoan) { id -> onAction(GroupsDetailAction.LoanClicked(id)) } else null,
                                )
                            }
                        }
                    }
                    if (loaded.savingsAccounts.isNotEmpty()) {
                        ExpandableSection(
                            title = stringResource(Res.string.feature_groups_savings_accounts),
                            count = loaded.savingsAccounts.size,
                            tag = GroupsDetailTestTags.SAVINGS_ACCOUNTS,
                        ) {
                            loaded.savingsAccounts.forEach { account ->
                                SavingsAccountRow(
                                    account,
                                    if (canOpenSavings) {
                                        { id, endpoint -> onAction(GroupsDetailAction.SavingsClicked(id, endpoint)) }
                                    } else {
                                        null
                                    },
                                )
                            }
                        }
                    }
                }
            }
            ScreenContent(
                state = members,
                onRetry = { onAction(GroupsDetailAction.RetryMembers) },
                modifier = Modifier.fillMaxWidth(),
                refreshingIndicator = null,
            ) { clients, _ ->
                if (clients.isNotEmpty()) {
                    ExpandableSection(
                        title = stringResource(Res.string.feature_groups_group_clients),
                        count = clients.size,
                        tag = GroupsDetailTestTags.CLIENTS,
                    ) {
                        clients.forEach { client ->
                            ClientRow(
                                client,
                                if (canOpenClient) {
                                    { id -> onAction(GroupsDetailAction.ClientClicked(id)) }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }

            // The write error sits BESIDE the fields, not instead of them: a failed activation does
            // not make the group's details untrue, and replacing them would lose the context the
            // user needs to decide whether to retry.
            if (canActivate) {
                Button(
                    onClick = { onAction(GroupsDetailAction.ActivateClicked) },
                    modifier = Modifier.fillMaxWidth().testTag(GroupsDetailTestTags.ACTIVATE),
                ) {
                    Text(stringResource(Res.string.feature_groups_activate))
                }
            }
        }
    }
}

/**
 * The detail screen's overflow menu — the original's six-item `DropdownMenu`
 * (`6b66e8a43:feature/groups/.../GroupDetailsScreen.kt:177`).
 *
 * ## Why this had to be written rather than carried across
 *
 * `GroupsDetailAction.DocumentsClicked` / `DataTablesClicked`, their events, the ViewModel's
 * handlers, the nav edges in `GroupsNavigation.kt` and even the [GroupsDetailTestTags] constants
 * all existed before this function did — the port landed the whole chain except the control that
 * dispatches it, so Documents and Datatables were reachable from nowhere. That is the inverse of
 * the dead-control defect "absent, never inert" guards against: not a button that does nothing,
 * but a working destination with no button. Both are caught by the same question — name the
 * affordance in the RUNNING fork that opens this screen (spec §6a).
 *
 * ## Three of the original's six
 *
 * Add Loan Account · Add Savings Account · Documents · More Group Info. Add Savings Account joined
 * in S5c — exactly as designed: a row appears the slice its destination lands, and nothing in this
 * function needed editing to reveal it beyond its own flag.
 *
 * Notes joined on 2026-10-25 with `feature/note` generalised to any owner. Group Clients is not a
 * menu row here: the members are on the screen as an expandable section, like the accounts.
 *
 * The icon itself is absent when NO item is available, rather than opening an empty menu.
 */
@Composable
private fun OverflowMenu(
    canAddLoanAccount: Boolean,
    canAddSavingsAccount: Boolean,
    canOpenDocuments: Boolean,
    canOpenDataTables: Boolean,
    canOpenNotes: Boolean,
    onAction: (GroupsDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val anyItem = listOf(canAddLoanAccount, canAddSavingsAccount, canOpenDocuments, canOpenDataTables, canOpenNotes)
    if (anyItem.none { it }) {
        return
    }

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.testTag(GroupsDetailTestTags.OVERFLOW),
        ) {
            Icon(
                AppIcons.MoreVert,
                contentDescription = stringResource(Res.string.feature_groups_more_options),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (canAddLoanAccount) {
                AppMenuDropDownItem(
                    option = stringResource(Res.string.feature_groups_add_loan_account),
                    onClick = {
                        expanded = false
                        onAction(GroupsDetailAction.AddLoanAccountClicked)
                    },
                    modifier = Modifier.testTag(GroupsDetailTestTags.ADD_LOAN_ACCOUNT),
                )
            }
            if (canAddSavingsAccount) {
                AppMenuDropDownItem(
                    option = stringResource(Res.string.feature_groups_add_savings_account),
                    onClick = {
                        expanded = false
                        onAction(GroupsDetailAction.AddSavingsAccountClicked)
                    },
                    modifier = Modifier.testTag(GroupsDetailTestTags.ADD_SAVINGS_ACCOUNT),
                )
            }
            if (canOpenDocuments) {
                AppMenuDropDownItem(
                    option = stringResource(Res.string.feature_groups_documents),
                    onClick = {
                        expanded = false
                        onAction(GroupsDetailAction.DocumentsClicked)
                    },
                    modifier = Modifier.testTag(GroupsDetailTestTags.DOCUMENTS),
                )
            }
            if (canOpenDataTables) {
                AppMenuDropDownItem(
                    option = stringResource(Res.string.feature_groups_more_group_info),
                    onClick = {
                        expanded = false
                        onAction(GroupsDetailAction.DataTablesClicked)
                    },
                    modifier = Modifier.testTag(GroupsDetailTestTags.DATA_TABLES),
                )
            }
            if (canOpenNotes) {
                AppMenuDropDownItem(
                    option = stringResource(Res.string.feature_groups_notes),
                    onClick = {
                        expanded = false
                        onAction(GroupsDetailAction.NotesClicked)
                    },
                    modifier = Modifier.testTag(GroupsDetailTestTags.NOTES),
                )
            }
        }
    }
}

@Composable
private fun GroupFields(group: Group) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = group.name ?: stringResource(Res.string.feature_groups_unnamed),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            // `active != true` — the SAME test `canActivate` uses, so the word on
            // screen and the presence of the Activate button can never disagree about a null.
            text = stringResource(
                if (group.active == true) {
                    Res.string.feature_groups_status_active
                } else {
                    Res.string.feature_groups_status_inactive
                },
            ),
            style = MaterialTheme.typography.labelLarge,
        )
        group.accountNo?.let { Field(stringResource(Res.string.feature_groups_account_no), it) }
        group.officeName?.let { Field(stringResource(Res.string.feature_groups_office), it) }
        // A group's center is the one relationship worth surfacing here — it is how an officer
        // orients a group within the caseload, and it is already on the entity.
        group.centerName?.let { Field(stringResource(Res.string.feature_groups_center), it) }
        group.staffName?.let { Field(stringResource(Res.string.feature_groups_staff), it) }
        group.externalId?.let { Field(stringResource(Res.string.feature_groups_external_id), it) }
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
