/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.auth.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kpt.core.base.network.UnauthorizedHandler
import kpt.core.data.auth.AuthCommandRepository
import kpt.core.model.objects.users.User

/**
 * Ends the session when the server rejects it, so the app returns to sign-in.
 *
 * Without this a rejected session was silent: a screen with cached data refreshed in the background,
 * the 401 went nowhere, and the officer kept reading stale data as if signed in. Measured on device
 * 2026-10-24 after repointing from the sandbox to the gateway — the old session's token was refused
 * on every call while Centers kept showing the sandbox's center.
 *
 * Keeps local data (`clearLocalData = false`): an expired session is not a reason to discard work not
 * yet synced. `RootNavViewModel` gates on the session flag, so clearing it IS the navigation.
 *
 * [auth] is resolved lazily: the auth repository's own API client is built by `restApi`, which asks
 * Koin for THIS handler — an eager dependency would be a construction cycle.
 */
internal class SignOutOnRejectedSession(
    private val auth: () -> AuthCommandRepository,
    private val session: () -> User,
    private val scope: CoroutineScope,
) : UnauthorizedHandler {

    // Parallel requests 401 together; one sign-out, and none once the session is already gone.
    private val mutex = Mutex()

    override fun onUnauthorized(accessPointId: String) {
        scope.launch {
            mutex.withLock {
                if (session().isAuthenticated) auth().signOut(clearLocalData = false)
            }
        }
    }
}
