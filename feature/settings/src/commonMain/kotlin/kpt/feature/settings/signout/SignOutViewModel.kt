/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.signout

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.auth.AuthCommandRepository

/**
 * Sign-out, at the bottom of Settings (moved from the Profile tab, 2026-10-25).
 *
 * Local only, so it works offline — a field officer who cannot sign out without signal is stranded.
 * What goes is the officer's choice in the dialog; `AuthCommandRepository.signOut` owns what each
 * choice removes. No navigation callback: clearing the session flips `isAuthenticated`, and the root
 * navigator routes to sign-in.
 */
class SignOutViewModel(
    private val auth: AuthCommandRepository,
) : BaseViewModel<SignOutState, SignOutEvent, SignOutAction>(SignOutState()) {

    override fun handleAction(action: SignOutAction) {
        when (action) {
            // Every opening starts on the DEFAULT — "clear data" (product decision 2026-10-25: a shared
            // field device must not hand the next person the previous officer's clients). A previous
            // "keep data" pick must not carry over into a later sign-out.
            SignOutAction.Requested -> mutableStateFlow.value = SignOutState(confirming = true)
            SignOutAction.Cancelled -> mutableStateFlow.value = SignOutState()
            is SignOutAction.ModeSelected -> mutableStateFlow.value = state.copy(mode = action.mode)
            SignOutAction.Confirmed -> signOut()
        }
    }

    private fun signOut() {
        if (state.isSigningOut) return
        mutableStateFlow.value = state.copy(isSigningOut = true, failed = false)
        viewModelScope.launch {
            val result = auth.signOut(clearLocalData = state.mode == SignOutMode.SessionAndData)
            if (result is MutationResult.Applied) {
                mutableStateFlow.value = SignOutState()
                sendEvent(SignOutEvent.SignedOut)
            } else {
                // Still signed in, data intact: the purge runs before the session is cleared.
                mutableStateFlow.value = state.copy(isSigningOut = false, failed = true)
            }
        }
    }
}

/**
 * What sign-out removes. [SessionAndData] is the default: it deletes everything the app stored,
 * unsynced work included. [SessionOnly] keeps cached clients and unsynced work for the next sign-in.
 */
enum class SignOutMode {
    /** End the session only; cached clients and unsynced work stay for the next sign-in. */
    SessionOnly,

    /** End the session and delete everything the app stored, unsynced work included. */
    SessionAndData,
}

/** What the sign-out flow shows: the confirm sheet, the chosen mode and progress. */
data class SignOutState(
    /** True while the confirm sheet is open. */
    val confirming: Boolean = false,
    /** What sign-out will remove. */
    val mode: SignOutMode = SignOutMode.SessionAndData,
    /** True while sign-out is running. */
    val isSigningOut: Boolean = false,
    /** True when the last sign-out did not finish. */
    val failed: Boolean = false,
)

/** What the officer did in the sign-out flow. */
sealed interface SignOutAction {
    /** Sign-out was asked for — open the confirm sheet. */
    data object Requested : SignOutAction

    /** The confirm sheet was dismissed. */
    data object Cancelled : SignOutAction

    /**
     * A mode was picked in the confirm sheet.
     *
     * @property mode the chosen mode
     */
    data class ModeSelected(val mode: SignOutMode) : SignOutAction

    /** Sign out now, with the chosen mode. */
    data object Confirmed : SignOutAction
}

/** One-off effects of the sign-out flow, for the screen to act on. */
sealed interface SignOutEvent {
    /** The officer is signed out — the app returns to sign-in. */
    data object SignedOut : SignOutEvent
}
