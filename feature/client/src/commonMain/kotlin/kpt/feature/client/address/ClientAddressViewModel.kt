/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.address

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientRepository
import kpt.core.model.objects.clients.ClientAddressEntity

/**
 * The client's postal addresses — ported from
 * `6b66e8a43:feature/client/clientAddress/ClientAddressViewModel.kt`.
 *
 * ## The connectivity observer this deletes was LEAKING
 *
 * The original's `observeNetwork()` collected `networkMonitor.isOnline` and ran both loads on every
 * `true`. It was called from `init` **and again from `handleRetry()`** — with no cancellation — so
 * each retry started another collector on the same flow. After three retries a single reconnect
 * fired four parallel address loads, and nothing on screen said so.
 *
 * `ScreenState.NoNetwork` is a state of the stream, decided once by the engine for every screen, so
 * the whole mechanism goes: the observer, the `networkConnection` field, the `NetworkError` case of
 * `AddressListScreenState`, and the leak with them.
 *
 * ## Two screen-states collapse to one stream each
 *
 * `AddressListScreenState` (Loading / ShowAddressList / NetworkError) and
 * `AddressFormScreenState` (Loading / ShowAddressForm / ShowStatusDialog) were hand-rolled
 * per-section state machines. The LIST half is [addresses]; the FORM half belongs to the add-address
 * screen, which is not ported (610-line screen) — so the template read and `createClientAddress`
 * are not here either. This screen is the LIST only, which is what the profile's Address row opens.
 *
 * ## `LaunchedEffect(Unit) { viewModel.loadClientAddress() }` is gone
 *
 * The original's screen re-triggered the load from a `LaunchedEffect` on top of the `init` load, so
 * opening the screen fetched twice. A stream is subscribed by the screen collecting it; there is
 * nothing to kick.
 */
internal class ClientAddressViewModel(
    repository: ClientRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit, ClientAddressEvent, ClientAddressAction>(Unit) {

    private val clientId: Int = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1

    private val stream = repository.clientAddressesStream(clientId, viewModelScope)

    val addresses: StateFlow<ScreenState<List<ClientAddressEntity>>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ClientAddressAction) {
        when (action) {
            ClientAddressAction.NavigateBack -> sendEvent(ClientAddressEvent.NavigateBack)
            ClientAddressAction.ShowAddressForm -> sendEvent(ClientAddressEvent.ShowAddressForm(clientId))
            ClientAddressAction.OnRetry -> stream.retry()
            ClientAddressAction.NavigateToSearch -> sendEvent(ClientAddressEvent.NavigateToSearch)
        }
    }

    private companion object {
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

internal sealed interface ClientAddressEvent {
    data object NavigateBack : ClientAddressEvent
    data class ShowAddressForm(val clientId: Int) : ClientAddressEvent
    data object NavigateToSearch : ClientAddressEvent
}

internal sealed interface ClientAddressAction {
    data object NavigateBack : ClientAddressAction
    data object ShowAddressForm : ClientAddressAction
    data object OnRetry : ClientAddressAction

    /** The header's search icon — every cached address, across clients (feature/searchrecord). */
    data object NavigateToSearch : ClientAddressAction
}
