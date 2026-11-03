/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.collateraldetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.client.ClientRepository
import kpt.core.model.shared.CollateralItemResult

/**
 * The collateral a client has already pledged — ported from
 * `6b66e8a43:feature/client/clientCollateralDetails/ClientCollateralDetailViewmodel.kt`.
 *
 * Reached from the client-general screen's "Collateral Data" row. **Not** the add-collateral form:
 * that is `feature/client/collateral`, reached from client DETAILS' "Create Collateral". An earlier
 * pass of this port wired General's row to the form, which is a divergence — the original wires it
 * to this read-only list (`clientProfileGeneralDestination(collateralData =
 * navigateToClientCollateralDetailRoute)`). Fixed with this slice.
 *
 * ## `networkMonitor.isOnline.first()` is gone, and it was worse than the usual case
 *
 * Most ported screens had a `collect` on connectivity. This one took `.first()` — a ONE-SHOT read
 * at load time. So a load that began while offline reported an error and never recovered when the
 * connection returned; only the retry button helped. `ScreenState` reacts to reconnection for
 * every screen.
 *
 * Its four-case `State` machine (Loading / Success / Empty / Error) maps 1:1 onto [ScreenState],
 * which is the whole reason that enum existed.
 */
internal class ClientCollateralDetailsViewModel(
    repository: ClientRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit, Nothing, ClientCollateralDetailsAction>(Unit) {

    private val clientId: Int = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1

    private val stream = repository.clientCollateralStream(clientId, viewModelScope)

    val collaterals: StateFlow<ScreenState<List<CollateralItemResult>>> = stream.state
        // No collateral is Empty (the screen's empty slot); the stream reports it as empty Content.
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ClientCollateralDetailsAction) {
        when (action) {
            ClientCollateralDetailsAction.OnRetry -> stream.retry()
        }
    }

    private companion object {
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * The two figures each row shows, computed from the pledged item.
 *
 * Same arithmetic as the add-collateral form's `collateralTotals`, but off a DIFFERENT type:
 * `CollateralItemResult` carries the pledged `quantity` as a `Double`, where the form multiplies
 * by a typed quantity. Kept as the original had it in both places rather than unified — they
 * describe different things (a saved pledge vs. a form in progress).
 */
internal fun collateralRowTotals(item: CollateralItemResult): Pair<Double, Double> {
    val total = item.basePrice * item.quantity
    return total to (total * item.pctToBase) / 100
}

internal sealed interface ClientCollateralDetailsAction {
    data object OnRetry : ClientCollateralDetailsAction
}
