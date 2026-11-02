/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientRepository
import kpt.core.data.recent.RecentRecordsRepository
import kpt.core.model.client.Client
import kpt.core.model.recent.RecentKind
import kpt.core.model.recent.RecentRecord
import kpt.core.ui.util.imageToByteArray
import kpt.feature.client.profile.components.ClientProfileActionItem

/**
 * The client PROFILE — what the client list opens. Ported from
 * `6b66e8a43:feature/client/clientProfile/ClientProfileViewModel.kt`.
 *
 * ## This replaces an invented screen
 *
 * An earlier pass of this port shipped a `ClientDetailScreen` that was a rebuild, not a port: a
 * Scaffold with a TopAppBar, raw `Text` identity fields, hand-rolled "Charges"/"Notes" rows and an
 * accounts section. The original has none of that. It is a "Profile" label, the primary-coloured
 * [kpt.feature.client.profile.components.ProfileCard], an "Actions" label, and six `AppRowCard`
 * rows. Accounts are NOT on this screen — they live behind the General action, in `clientGeneral`.
 *
 * ## What the substrate changes
 *
 * **The read is a stream.** `GetClientDetailsUseCase` + the manual `DataState` fan-in collapse to
 * `clientStream`. `core/domain` is deleted on this substrate (§2 of the migration spec).
 *
 * **`networkMonitor` is gone.** It existed only to tell `AppErrorComponent` whether to blame the
 * network; `ScreenState.NoNetwork` is decided by the stream for every screen.
 *
 * **`observeGlobalUpdates` is gone.** The original subscribed to a `clientUpdateEvents`
 * SharedFlow that other screens emitted on, to re-run the load after an edit elsewhere. That is
 * what store invalidation does here — a write through the gateway invalidates the cache and the
 * stream re-emits, with no app-wide event bus to keep in sync.
 *
 * ## The photo is fetched once, not streamed
 *
 * `clientImage` is a one-shot blob (see `ClientRepository.clientImage`). Failure and absence are
 * the same thing to this screen — the avatar placeholder — so it never reaches [ScreenState].
 */
internal class ClientProfileViewModel(
    private val repository: ClientRepository,
    savedStateHandle: SavedStateHandle,
    private val recents: RecentRecordsRepository,
) : BaseViewModel<ClientProfileState, ClientProfileEvent, ClientProfileAction>(
    ClientProfileState(),
) {

    private val clientId: Int = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1

    private val stream = repository.clientStream(clientId, viewModelScope)

    /** The client record. `stateIn(… WhileSubscribed …)`, not an `init{}` hand-lift. */
    val client: StateFlow<ScreenState<Client>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    init {
        // Home's "Recently opened": record this visit once, when the record first loads.
        client.filterIsInstance<ScreenState.Content<Client>>().take(1)
            .onEach { content ->
                content.data.let {
                    recents.record(
                        RecentRecord(RecentKind.CLIENT, it.id.toLong(), it.displayName.orEmpty(), it.accountNo),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    init {
        loadImage()
    }

    private fun loadImage() {
        viewModelScope.launch {
            val dataUrl = repository.clientImage(clientId)
            mutableStateFlow.value = state.copy(profileImage = imageToByteArray(dataUrl))
        }
    }

    override fun handleAction(action: ClientProfileAction) {
        when (action) {
            ClientProfileAction.NavigateBack -> sendEvent(ClientProfileEvent.NavigateBack)

            is ClientProfileAction.OnActionClick ->
                sendEvent(ClientProfileEvent.OnActionClick(action.action, clientId))

            ClientProfileAction.NavigateToClientDetailsScreen ->
                sendEvent(ClientProfileEvent.NavigateToClientDetailsScreen(clientId))

            is ClientProfileAction.OnGroupClick ->
                sendEvent(ClientProfileEvent.NavigateToGroupDetails(action.groupId))

            ClientProfileAction.OnRetry -> {
                stream.retry()
                // The photo is re-fetched too. The original's retry re-ran BOTH loads, and a photo
                // that failed on a dead connection would otherwise stay missing until the screen
                // was left and re-entered.
                loadImage()
            }
        }
    }

    private companion object {
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * Screen state.
 *
 * The CLIENT is not here — it is on [ClientProfileViewModel.client], the stream. The original held
 * a nullable `client` beside a `DialogState`, which is what let a loading dialog sit over a
 * populated card and an error dialog over a stale one. `details` and `networkConnection` are gone:
 * `details` was never written by anything, and `networkConnection` fed an error component the
 * stream now drives.
 */
internal data class ClientProfileState(
    val profileImage: ByteArray? = null,
) {
    // ByteArray in a data class — equals/hashCode must be by CONTENT, or every recomposition sees
    // a "new" state because array equality is identity.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientProfileState) return false
        return profileImage.contentEquals(other.profileImage)
    }

    override fun hashCode(): Int = profileImage?.contentHashCode() ?: 0
}

internal sealed interface ClientProfileEvent {
    data object NavigateBack : ClientProfileEvent
    data class OnActionClick(val action: ClientProfileActionItem, val clientId: Int) : ClientProfileEvent
    data class NavigateToClientDetailsScreen(val clientId: Int) : ClientProfileEvent
    data class NavigateToGroupDetails(val groupId: Int) : ClientProfileEvent
}

internal sealed interface ClientProfileAction {
    data object NavigateBack : ClientProfileAction
    data class OnActionClick(val action: ClientProfileActionItem) : ClientProfileAction
    data object OnRetry : ClientProfileAction
    data object NavigateToClientDetailsScreen : ClientProfileAction
    data class OnGroupClick(val groupId: Int) : ClientProfileAction
}
