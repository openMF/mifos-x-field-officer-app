/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.conflict.ConflictEntry
import kpt.core.base.store.mutation.conflict.ConflictInbox
import kpt.core.base.store.mutation.conflict.ConflictResolution
import kpt.core.base.ui.viewmodel.BaseViewModel

/**
 * ViewModel for the Settings **Sync conflicts** screen — a live window over the [ConflictInbox]
 * pending feed. Each row is a write the app made that the server diverged on (server-wins was
 * applied, the user's version preserved). The two actions the generic list can honor:
 * **Keep server** (accept the ingested server record) and **Retry mine** (re-submit the local
 * payload). Same MVI [BaseViewModel] idiom as every other feature.
 */
class ConflictInboxViewModel(
    private val conflictInbox: ConflictInbox,
) : BaseViewModel<ConflictInboxUiState, Nothing, ConflictInboxAction>(ConflictInboxUiState.Loading) {

    init {
        conflictInbox.observePending()
            .onEach { conflicts -> updateState { ConflictInboxUiState.Success(conflicts) } }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: ConflictInboxAction) {
        when (action) {
            is ConflictInboxAction.AcceptServer ->
                viewModelScope.launch { conflictInbox.resolve(action.id, ConflictResolution.ACCEPT_SERVER) }
            is ConflictInboxAction.RetryLocal ->
                viewModelScope.launch { conflictInbox.resolve(action.id, ConflictResolution.RETRY_LOCAL) }
        }
    }

    /** Accept the server record for conflict [id] (server-wins already applied) — the per-row Keep-server. */
    fun acceptServer(id: String) = trySendAction(ConflictInboxAction.AcceptServer(id))

    /** Re-submit the user's local payload for conflict [id] — the per-row Retry-mine. */
    fun retryLocal(id: String) = trySendAction(ConflictInboxAction.RetryLocal(id))
}

/** Per-row conflict actions accepted by [ConflictInboxViewModel]. */
sealed interface ConflictInboxAction {
    /** Accept the server record for conflict [id]. */
    data class AcceptServer(val id: String) : ConflictInboxAction

    /** Retry the recorded local payload for conflict [id]. */
    data class RetryLocal(val id: String) : ConflictInboxAction
}

/** UI state for the Sync conflicts screen. */
sealed interface ConflictInboxUiState {
    /** Initial state before the first [ConflictInbox.observePending] emission. */
    data object Loading : ConflictInboxUiState

    /** The live pending-conflict feed, newest first. */
    data class Success(val conflicts: List<ConflictEntry>) : ConflictInboxUiState {
        /** True when there are no pending conflicts — drives the empty state. */
        val isEmpty: Boolean get() = conflicts.isEmpty()
    }
}
