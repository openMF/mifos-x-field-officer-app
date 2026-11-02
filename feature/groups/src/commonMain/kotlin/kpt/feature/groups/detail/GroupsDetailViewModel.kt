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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.group.GroupRepository
import kpt.core.data.recent.RecentRecordsRepository
import kpt.core.model.client.Client
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.recent.RecentKind
import kpt.core.model.recent.RecentRecord

/**
 * Group detail — ported from `6b66e8a43:feature/groups/groupDetails/GroupDetailsViewModel.kt`.
 *
 * ## Why this is NOT the Centers detail with a rename
 *
 * The two screens look alike and are not. A center's detail exists to reach its GROUPS, so the port
 * folded the original's separate `centerGroupList` into it. A group's detail is a leaf — the
 * original's six overflow entries (add loan, add savings, documents, group clients, more info,
 * notes) lead OUT of `feature/groups`. All six are live (2026-10-25): the overflow carries add loan,
 * add savings, documents, more info and notes; the group's clients and its loan / savings accounts
 * are expandable sections on the screen, each row opening its own detail — as the original drew
 * the accounts.
 *
 * ## Activate
 *
 * The original's "Activate group" button is ported because `GroupCommandRepository.activateGroup`
 * really exists — it is the one action on this screen with a path all the way to Fineract. It is
 * offered ONLY for an inactive group, which is also the original's condition.
 *
 * It NAVIGATES to `feature/activate` rather than writing inline. An earlier pass of this port did
 * the write here with an all-null `ActivatePayload`; see [handleAction]'s Activate branch.
 */
internal class GroupsDetailViewModel(
    repository: GroupRepository,
    savedStateHandle: SavedStateHandle,
    private val recents: RecentRecordsRepository,
) : BaseViewModel<Unit, GroupsDetailEvent, GroupsDetailAction>(Unit) {

    /** Must match `GroupsDetailRoute.groupId` — type-safe nav keys the handle by property name. */
    private val groupId: Int = savedStateHandle.get<Int>(GROUP_ID_KEY) ?: 0

    private val stream = repository.groupsStream(viewModelScope)

    /**
     * The one group this screen is about.
     *
     * `stateIn(… WhileSubscribed …)`, not an `init{}` collector — see `CenterListViewModel` for why
     * the hand-lift kept a Store subscription alive for screens nobody was looking at.
     *
     * filter -> emptyIfContent -> first, so a group outside the officer's caseload renders as Empty
     * rather than as a detail page of blank fields. `mapContent` carries `fetchedAt` and
     * `freshnessSignal` through, which a hand-rolled `when` over the states would drop.
     */
    val group: StateFlow<ScreenState<Group>> = stream.state
        .mapContent { groups, _ -> groups.filter { it.id == groupId } }
        .emptyIfContent { it.isEmpty() }
        .mapContent { matches, _ -> matches.first() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    init {
        // Home's "Recently opened": record this visit once, when the record first loads.
        group.filterIsInstance<ScreenState.Content<Group>>().take(1)
            .onEach { content ->
                val g = content.data
                val id = (g.id ?: groupId).toLong()
                recents.record(RecentRecord(RecentKind.GROUP, id, g.name.orEmpty(), g.accountNo))
            }
            .launchIn(viewModelScope)
    }

    /**
     * Whether Activate can be offered, derived from the SAME flow the status text reads.
     *
     * `!= true`, NOT `== false`: Fineract leaves `active` NULL for a pending group — exactly the
     * group an officer opens this screen to activate. Measured on device 2026-10-20: group "Ankit"
     * rendered "Inactive" and offered no button, because two predicates disagreed on null.
     */
    private val membersStream = repository.groupWithMembersStream(groupId, viewModelScope)
    private val accountsStream = repository.groupAccountsStream(groupId, viewModelScope)

    /** The group's member clients — its own loading / error states, beside the fields. */
    val members: StateFlow<ScreenState<List<Client>>> = membersStream.state
        .mapContent { group, _ -> group.clientMembers }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /** The group's loan and savings accounts. */
    val accounts: StateFlow<ScreenState<GroupAccounts>> = accountsStream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    val canActivate: StateFlow<Boolean> = group.map { g ->
        (g as? ScreenState.Content)?.data?.let { it.active != true } == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), false)

    override fun handleAction(action: GroupsDetailAction) {
        when (action) {
            GroupsDetailAction.BackClicked -> sendEvent(GroupsDetailEvent.NavigateBack)
            GroupsDetailAction.ActivateClicked -> sendEvent(GroupsDetailEvent.Activate(groupId))

            GroupsDetailAction.DocumentsClicked ->
                sendEvent(GroupsDetailEvent.OpenDocuments(groupId))

            GroupsDetailAction.DataTablesClicked ->
                sendEvent(GroupsDetailEvent.OpenDataTables(groupId))

            GroupsDetailAction.AddLoanAccountClicked ->
                sendEvent(GroupsDetailEvent.OpenAddLoanAccount(groupId))

            GroupsDetailAction.AddSavingsAccountClicked ->
                sendEvent(GroupsDetailEvent.OpenAddSavingsAccount(groupId))

            GroupsDetailAction.NotesClicked -> sendEvent(GroupsDetailEvent.OpenNotes(groupId))
            is GroupsDetailAction.LoanClicked -> sendEvent(GroupsDetailEvent.OpenLoan(action.loanId))
            is GroupsDetailAction.SavingsClicked ->
                sendEvent(GroupsDetailEvent.OpenSavings(action.accountId, action.depositTypeEndpoint))
            is GroupsDetailAction.ClientClicked -> sendEvent(GroupsDetailEvent.OpenClient(action.clientId))

            GroupsDetailAction.Retry -> stream.retry()
            GroupsDetailAction.RetryMembers -> membersStream.retry()
            GroupsDetailAction.RetryAccounts -> accountsStream.retry()
        }
    }

    /**
     * Activation is a SCREEN, not an inline write.
     *
     * This used to post `ActivatePayload()` with all three fields null, on the assumption that the
     * server would default the activation date to today. That was a guess: Fineract's activate
     * command takes `activationDate` + `dateFormat` + `locale`, and the original has always
     * collected the date from a date picker on a dedicated screen (`feature/activate`, shared with
     * client and centre). Navigating there removes the guess and the §6a divergence at once.
     */

    private companion object {
        const val TAG = "GroupsDetail"
        const val GROUP_ID_KEY = "groupId"

        /** Template default — long enough to survive a rotation or a brief tab switch. */
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

internal sealed interface GroupsDetailAction {
    data object BackClicked : GroupsDetailAction
    data object ActivateClicked : GroupsDetailAction
    data object DocumentsClicked : GroupsDetailAction
    data object DataTablesClicked : GroupsDetailAction
    data object AddLoanAccountClicked : GroupsDetailAction
    data object AddSavingsAccountClicked : GroupsDetailAction
    data object NotesClicked : GroupsDetailAction
    data class LoanClicked(val loanId: Int) : GroupsDetailAction
    data class SavingsClicked(val accountId: Int, val depositTypeEndpoint: String) : GroupsDetailAction
    data class ClientClicked(val clientId: Int) : GroupsDetailAction
    data object Retry : GroupsDetailAction
    data object RetryMembers : GroupsDetailAction
    data object RetryAccounts : GroupsDetailAction
}

internal sealed interface GroupsDetailEvent {
    data object NavigateBack : GroupsDetailEvent

    /** Open the shared activate screen for this group. */
    data class Activate(val groupId: Int) : GroupsDetailEvent

    /** Open the generic document list for this group — the original's `loadDocumentList` edge. */
    data class OpenDocuments(val groupId: Int) : GroupsDetailEvent

    /** Open the group's datatables — the original's `loadGroupDataTables` edge. */
    data class OpenDataTables(val groupId: Int) : GroupsDetailEvent

    /** Open the group loan application form — the original's `addGroupLoanAccount` edge. */
    data class OpenAddLoanAccount(val groupId: Int) : GroupsDetailEvent

    /**
     * Open the savings application for this GROUP — the original's `addSavingsAccount` edge.
     *
     * The original passed `(groupId, 0, true)` into a wizard that read only the second argument,
     * so this path created an application against client 0. See `SavingsApplicationViewModel`.
     */
    data class OpenAddSavingsAccount(val groupId: Int) : GroupsDetailEvent

    /** The group's notes — the original's `loadNotes(groupId, "groups")` edge. */
    data class OpenNotes(val groupId: Int) : GroupsDetailEvent

    /** One of the group's loans — the original's `loadLoanAccountSummary`. */
    data class OpenLoan(val loanId: Int) : GroupsDetailEvent

    /** One of the group's savings accounts, at its deposit type's endpoint — `loadSavingsAccountSummary`. */
    data class OpenSavings(val accountId: Int, val depositTypeEndpoint: String) : GroupsDetailEvent

    /** A member client — the original's "Group clients" entry. */
    data class OpenClient(val clientId: Int) : GroupsDetailEvent
}
