/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.notification.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.notification.NotificationRepository
import kpt.core.model.notification.AppNotification

/**
 * The bell's notifications. No original: the app being ported had no notification screen, and the
 * template's `feature/settings` NotificationScreen is a static placeholder — this reads Fineract's.
 */
internal class NotificationsViewModel(
    private val repository: NotificationRepository,
) : BaseViewModel<NotificationsState, NotificationsEvent, NotificationsAction>(NotificationsState()) {

    private val stream = repository.notificationsStream(viewModelScope)

    val notifications: StateFlow<ScreenState<List<AppNotification>>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: NotificationsAction) {
        when (action) {
            NotificationsAction.Refresh -> stream.refresh(forceFresh = true)
            NotificationsAction.Retry -> stream.retry()
            NotificationsAction.MarkAllRead -> markAllRead()
            NotificationsAction.NavigateBack -> sendEvent(NotificationsEvent.NavigateBack)
            is NotificationsAction.Open -> if (action.notification.opensRecord) {
                sendEvent(NotificationsEvent.Open(action.notification))
            }
        }
    }

    private fun markAllRead() {
        if (state.marking) return
        updateState { copy(marking = true) }
        viewModelScope.launch {
            when (repository.markAllRead()) {
                // The server changed the list; read it back rather than guessing what it now holds.
                is MutationResult.Applied -> stream.refresh(forceFresh = true)
                else -> sendEvent(NotificationsEvent.MarkAllReadFailed)
            }
            updateState { copy(marking = false) }
        }
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

internal data class NotificationsState(val marking: Boolean = false)

internal sealed interface NotificationsAction {
    data object Refresh : NotificationsAction
    data object Retry : NotificationsAction
    data object MarkAllRead : NotificationsAction
    data object NavigateBack : NotificationsAction
    data class Open(val notification: AppNotification) : NotificationsAction
}

internal sealed interface NotificationsEvent {
    data object NavigateBack : NotificationsEvent
    data object MarkAllReadFailed : NotificationsEvent
    data class Open(val notification: AppNotification) : NotificationsEvent
}

/** The record kinds a notification can open — Fineract's `objectType`, lower-cased. */
internal enum class NotificationTarget(vararg val types: String) {
    LOAN("loan"),
    CLIENT("client"),
    GROUP("group"),
    CENTER("center", "centre"),
    SAVINGS("savingsaccount", "savings"),
    ;

    companion object {
        fun of(objectType: String): NotificationTarget? = entries.firstOrNull { objectType.lowercase() in it.types }
    }
}

internal val AppNotification.target: NotificationTarget? get() = NotificationTarget.of(objectType)

/** A row is a link only when it names a record this app can open. */
internal val AppNotification.opensRecord: Boolean get() = target != null && objectId != null
