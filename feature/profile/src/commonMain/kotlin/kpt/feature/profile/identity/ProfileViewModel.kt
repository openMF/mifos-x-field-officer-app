/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.identity

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.datastore.prefs.ProjectPreferencesRepository
import kpt.core.datastore.prefs.UserPreferencesRepository

/**
 * Signed-in officer identity and sign-out — the Profile tab's content.
 *
 * There is no original to port: the field-officer app had no Profile tab. The template backbone
 * always renders one (spec §6), so this is a deliberate NEW surface, kept to identity plus sign-out
 * so "new surface inside a migration" stays defensible (`feature/profile/MIGRATION.md` §7).
 *
 * Everything shown is already persisted at login, which is why this needs no `core/data` addition.
 */
class ProfileViewModel(
    private val project: ProjectPreferencesRepository,
    private val user: UserPreferencesRepository,
) : BaseViewModel<ProfileState, ProfileEvent, ProfileAction>(ProfileState()) {

    init {
        // fineractUser is a StateFlow persisted at sign-in — readable with no network.
        viewModelScope.launch {
            project.fineractUser.collect { fineract ->
                mutableStateFlow.value = state.copy(
                    username = fineract.username.orEmpty(),
                    officeName = fineract.officeName.orEmpty(),
                    instanceUrl = project.instanceUrl,
                )
            }
        }
    }

    override fun handleAction(action: ProfileAction) {
        when (action) {
            ProfileAction.SignOutRequested ->
                mutableStateFlow.value = state.copy(confirmingSignOut = true)

            ProfileAction.SignOutCancelled ->
                mutableStateFlow.value = state.copy(confirmingSignOut = false)

            ProfileAction.SignOutConfirmed -> signOut()
        }
    }

    /**
     * Clears the session locally — deliberately NOT a `MutationGateway` mutation.
     *
     * Sign-out must succeed offline; a field officer who cannot sign out without a connection is
     * stranded (`MIGRATION.md` §4). Both halves are cleared: the Fineract user AND the framework's
     * auth flags, because `RootNavViewModel` gates on `isAuthenticated`, and clearing only the
     * Fineract half would leave the shell believing the user is still signed in.
     */
    private fun signOut() {
        viewModelScope.launch {
            project.clearFineractUser()
            user.clearUserData()
            mutableStateFlow.value = state.copy(confirmingSignOut = false)
            sendEvent(ProfileEvent.SignedOut)
        }
    }
}

data class ProfileState(
    val username: String = "",
    val officeName: String = "",
    val instanceUrl: String = "",
    val confirmingSignOut: Boolean = false,
)

sealed interface ProfileAction {
    data object SignOutRequested : ProfileAction
    data object SignOutCancelled : ProfileAction
    data object SignOutConfirmed : ProfileAction
}

sealed interface ProfileEvent {
    data object SignedOut : ProfileEvent
}
