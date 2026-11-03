/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.activate.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.Constants
import kpt.core.data.center.CenterCommandRepository
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.group.GroupCommandRepository
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.error.serverErrorMessage

/**
 * Activate a client, centre or group — ported from
 * `6b66e8a43:feature/activate/ActivateViewModel.kt`.
 *
 * One screen for three entity types, keyed by the route's `type`. That is the original's design and
 * it is the right one: activation is the same interaction in all three cases (pick a date, submit),
 * and the only difference is which endpoint it posts to.
 *
 * ## Why this screen had to exist rather than staying inline
 *
 * An earlier pass of this port activated a GROUP inline from the group detail screen, sending
 * `ActivatePayload()` — all three fields null — with a comment claiming the server defaults the
 * activation date to today. That was an assumption, not a measurement: Fineract's activate command
 * takes `activationDate` + `dateFormat` + `locale`, and the original has always sent all three from
 * a date picker. Porting this screen removes both the guess and the divergence, and gives centre
 * and client activation the same path for free.
 *
 * ## `isSubmitting` instead of a `Loading` state
 *
 * The original's `ActivateUiState` had Initial / Loading / Error / ActivatedSuccessfully, where
 * Loading REPLACED the form — so the date the officer picked vanished while the request was in
 * flight, and reappeared reset if it failed. The form stays mounted here and the button disables.
 */
internal class ActivateViewModel(
    private val clients: ClientCommandRepository,
    private val centers: CenterCommandRepository,
    private val groups: GroupCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ActivateState, ActivateEvent, ActivateAction>(
    ActivateState(
        id = savedStateHandle.get<Int>(ID_KEY) ?: -1,
        type = savedStateHandle.get<String>(TYPE_KEY)?.let(ActivateType::fromRouteValue)
            ?: ActivateType.Client,
    ),
) {

    override fun handleAction(action: ActivateAction) {
        when (action) {
            ActivateAction.NavigateBack -> sendEvent(ActivateEvent.NavigateBack)
            ActivateAction.DismissDialog -> mutableStateFlow.value = state.copy(result = null)
            is ActivateAction.Submit -> activate(action.payload)
        }
    }

    private fun activate(payload: ActivatePayload) {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, result = null)

        viewModelScope.launch {
            // Each branch unwraps to the same three outcomes, so the result shape is uniform even
            // though the three repositories return different success types.
            val result: MutationResult<*> = when (state.type) {
                ActivateType.Client -> clients.activateClient(state.id, payload)
                ActivateType.Center -> centers.activateCenter(state.id, payload)
                ActivateType.Group -> groups.activateGroup(state.id, payload)
            }

            mutableStateFlow.value = when (result) {
                is MutationResult.Applied ->
                    state.copy(isSubmitting = false, result = ActivateResult.Succeeded)

                is MutationResult.Blocked -> state.copy(
                    isSubmitting = false,
                    result = if (result.reason == BlockReason.OFFLINE) {
                        ActivateResult.Offline
                    } else {
                        ActivateResult.Failed
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "activate ${state.type} failed" }
                    // The server's own reason (e.g. a 403 "Insufficient privileges"), as every other
                    // write screen shows it — the generic line alone told the officer nothing.
                    state.copy(
                        isSubmitting = false,
                        result = ActivateResult.Failed,
                        failureReason = result.cause.serverErrorMessage(),
                    )
                }

                is MutationResult.Conflicted ->
                    state.copy(isSubmitting = false, result = ActivateResult.Failed)
            }
        }
    }

    private companion object {
        const val TAG = "Activate"
        const val ID_KEY = "id"
        const val TYPE_KEY = "type"
    }
}

/**
 * Which entity is being activated.
 *
 * The original passed the raw `type: String` from the route and branched on
 * `Constants.ACTIVATE_CLIENT` / `_CENTER` / `_GROUP` string comparisons inside the screen. A typo
 * in either place silently fell through to no branch at all; this makes the three cases a closed
 * set the compiler checks.
 */
internal enum class ActivateType(val routeValue: String) {
    Client(Constants.ACTIVATE_CLIENT),
    Center(Constants.ACTIVATE_CENTER),
    Group(Constants.ACTIVATE_GROUP),
    ;

    companion object {
        fun fromRouteValue(value: String): ActivateType? =
            entries.firstOrNull { it.routeValue == value }
    }
}

internal data class ActivateState(
    val id: Int = -1,
    val type: ActivateType = ActivateType.Client,
    val isSubmitting: Boolean = false,
    val result: ActivateResult? = null,
    /** The server's reason for a [ActivateResult.Failed], when it gave one. */
    val failureReason: String? = null,
)

internal enum class ActivateResult { Succeeded, Offline, Failed }

internal sealed interface ActivateEvent {
    data object NavigateBack : ActivateEvent
}

internal sealed interface ActivateAction {
    data object NavigateBack : ActivateAction
    data object DismissDialog : ActivateAction
    data class Submit(val payload: ActivatePayload) : ActivateAction
}
