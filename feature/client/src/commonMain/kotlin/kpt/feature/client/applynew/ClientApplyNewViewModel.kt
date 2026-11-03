/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.applynew

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientRepository
import kpt.core.model.client.Client
import kpt.core.model.client.ClientStatus

/**
 * "Apply for a new account" — the client-side chooser, ported from
 * `6b66e8a43:feature/client/clientApplyNewApplications/` (201-line screen + 57-line ViewModel).
 *
 * Five account types; each row opens that type's application. This is the original's real entry
 * point for all five, which is why it is ported now rather than as its own slice: it is what gives
 * the savings application its CLIENT caller (S5c wired only the group path, from group detail) and
 * the recurring-deposit wizard its only caller at all.
 *
 * ## Only the rows whose destinations exist are drawn
 *
 * The original drew all five unconditionally, so in this fork three of them would open nothing.
 * Each is gated on its own callback being non-null — absent, never inert — so a row appears the
 * slice its application lands and nothing here changes to reveal it.
 *
 * ## The status is READ, not passed as a nav argument
 *
 * The original's route carried it: `ClientApplyNewApplicationRoute(clientId, status, accountNo)`,
 * compared as `state.status == ClientStatus.STATUS_ACTIVE`. A status in a nav argument is a
 * SNAPSHOT — activate the client on another screen, come back, and the argument still says
 * pending, so the gate refuses an application the server would now accept. The client is read from
 * [ClientRepository] instead, so this gate and the client screen cannot disagree. Same reasoning
 * as the savings summary reading its account rather than trusting a nav-arg blob.
 *
 * Fineract will not accept an application for a client that is not active, so the gate itself is
 * right and is kept. `== STATUS_ACTIVE`, so an unreadable or unrecognised status falls to "not
 * active" — the safe direction.
 */
internal class ClientApplyNewViewModel(
    repository: ClientRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientApplyNewState, ClientApplyNewEvent, ClientApplyNewAction>(
    ClientApplyNewState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    private val stream = repository.clientStream(state.clientId, viewModelScope)

    /**
     * Whether an application can be submitted at all, derived from the client the server has.
     *
     * `Eagerly`, NOT `WhileSubscribed`: this is read by the ROW-GATING path as well as the
     * composition, and a `WhileSubscribed` flow hands back its seed when nothing collects — the
     * defect that bit this migration four times (see
     * `while-subscribed-read-off-the-composition-path`).
     */
    val client: StateFlow<ScreenState<Client>> = stream.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, ScreenState.Loading)

    override fun handleAction(action: ClientApplyNewAction) {
        when (action) {
            ClientApplyNewAction.NavigateBack -> sendEvent(ClientApplyNewEvent.NavigateBack)
            ClientApplyNewAction.OnRetry -> stream.retry()
            is ClientApplyNewAction.OnItemClick ->
                sendEvent(ClientApplyNewEvent.OpenApplication(action.item, state.clientId))
        }
    }

    private companion object {
        const val CLIENT_ID_KEY = "clientId"
    }
}

/** The five account types a client can apply for. */
internal enum class ApplyNewItem {
    LOAN,
    SAVINGS,
    SHARE,
    RECURRING_DEPOSIT,
    FIXED_DEPOSIT,
}

internal data class ClientApplyNewState(val clientId: Int = -1)

/**
 * Fineract refuses an application for a client that is not active.
 *
 * `== STATUS_ACTIVE` on the client's OWN status, so an unreadable or unrecognised one falls to
 * "not active" — the safe direction.
 */
internal fun Client.canApplyForNewAccount(): Boolean =
    status?.value == ClientStatus.STATUS_ACTIVE

internal sealed interface ClientApplyNewEvent {
    data object NavigateBack : ClientApplyNewEvent
    data class OpenApplication(val item: ApplyNewItem, val clientId: Int) : ClientApplyNewEvent
}

internal sealed interface ClientApplyNewAction {
    data object NavigateBack : ClientApplyNewAction
    data object OnRetry : ClientApplyNewAction
    data class OnItemClick(val item: ApplyNewItem) : ClientApplyNewAction
}
