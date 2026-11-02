/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.charge.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.charge.ChargeRepository
import kpt.core.model.objects.charge.ChargeResource
import kpt.feature.charge.ui.CHARGE_ID_KEY
import kpt.feature.charge.ui.ChargeUi
import kpt.feature.charge.ui.chargeResource
import kpt.feature.charge.ui.chargeResourceId
import kpt.feature.charge.ui.toUi

/**
 * One charge in full — the loan list's four labelled rows
 * (`6b66e8a43:feature/loan/loanCharge/LoanChargeScreen.kt`) plus the paid / waived / outstanding
 * split the list row has no room for.
 *
 * Read from the SAME stream the list renders, filtered to this charge: the row is already in the
 * `Charges` table, so it opens offline, and an edit or a delete made elsewhere is reflected here
 * because both observe one table. A charge no longer on the account reads as `Empty`.
 */
internal class ChargeDetailViewModel(
    repository: ChargeRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<Unit, ChargeDetailEvent, ChargeDetailAction>(Unit) {

    private val resource: ChargeResource = savedStateHandle.chargeResource()
    private val resourceId: Int = savedStateHandle.chargeResourceId()
    private val chargeId: Int = savedStateHandle.get<Int>(CHARGE_ID_KEY) ?: -1

    val canEdit: Boolean = resource.canUpdate

    private val stream = repository.chargesStream(resource, resourceId, viewModelScope)

    val charge: StateFlow<ScreenState<ChargeUi>> = stream.state
        .mapContent { rows, _ -> rows.firstOrNull { it.id == chargeId }?.toUi() }
        .emptyIfContent { it == null }
        // Sound: after `emptyIfContent` no Content carries null.
        .map {
            @Suppress("UNCHECKED_CAST")
            it as ScreenState<ChargeUi>
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ChargeDetailAction) {
        when (action) {
            ChargeDetailAction.NavigateBack -> sendEvent(ChargeDetailEvent.NavigateBack)
            ChargeDetailAction.OnRefresh -> stream.refresh(forceFresh = true)
            ChargeDetailAction.OnEdit -> if (canEdit) {
                sendEvent(ChargeDetailEvent.EditCharge(resource.path, resourceId, chargeId))
            }
        }
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

internal sealed interface ChargeDetailEvent {
    data object NavigateBack : ChargeDetailEvent
    data class EditCharge(val resourceType: String, val resourceId: Int, val chargeId: Int) : ChargeDetailEvent
}

internal sealed interface ChargeDetailAction {
    data object NavigateBack : ChargeDetailAction
    data object OnRefresh : ChargeDetailAction
    data object OnEdit : ChargeDetailAction
}
