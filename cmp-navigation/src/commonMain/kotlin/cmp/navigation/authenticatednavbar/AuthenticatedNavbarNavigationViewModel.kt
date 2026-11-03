/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.authenticatednavbar

import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.model.user.UserData

internal class AuthenticatedNavbarNavigationViewModel :
    BaseViewModel<Unit, AuthenticatedNavBarEvent, AuthenticatedNavBarAction>(
        initialState = Unit,
    ) {

    override fun handleAction(action: AuthenticatedNavBarAction) {
        when (action) {
            AuthenticatedNavBarAction.HomeTabClick -> handleHomeTabClicked()
            AuthenticatedNavBarAction.PendingTabClick -> Unit
            is AuthenticatedNavBarAction.Internal -> handleInternalAction(action)
        }
    }

    private fun handleInternalAction(action: AuthenticatedNavBarAction.Internal) {
        when (action) {
            is AuthenticatedNavBarAction.Internal.UserStateUpdateReceive -> {
            }
        }
    }

    private fun handleHomeTabClicked() {
        sendEvent(AuthenticatedNavBarEvent.NavigateToHomeScreen)
    }
}

internal sealed class AuthenticatedNavBarAction {
    /** The first tab. Search moved to the top bar's magnifier (see AuthenticatedNavBarTabItem). */
    data object HomeTabClick : AuthenticatedNavBarAction()

    /**
     * Centers / Groups, whose feature modules are not ported yet.
     *
     * Routed by the navbar straight to the pending host, so there is nothing for the ViewModel to
     * decide; it is modelled anyway rather than omitted so the `when` stays exhaustive and the day
     * those tabs become real, the compiler points here.
     */
    data object PendingTabClick : AuthenticatedNavBarAction()

    sealed class Internal : AuthenticatedNavBarAction() {
        data class UserStateUpdateReceive(val userState: UserData?) : Internal()
    }
}

internal sealed class AuthenticatedNavBarEvent {

    abstract val tab: AuthenticatedNavBarTabItem

    data object NavigateToHomeScreen : AuthenticatedNavBarEvent() {
        override val tab: AuthenticatedNavBarTabItem = AuthenticatedNavBarTabItem.HomeTab
    }
}
