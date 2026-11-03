/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.prefs

import kotlinx.coroutines.flow.StateFlow
import kpt.core.model.objects.users.User

/**
 * The fork's preference surface: everything the framework has no concept of.
 *
 * Inheritance is the contract — a framework preference cannot be dropped without a compile error,
 * and anything declared here is additive. What belongs here is only what is Fineract-specific:
 * WHICH server this install talks to, and WHO is signed in to it. Theme, language, passcode,
 * onboarding and the auth-token slot already exist on [UserPreferencesRepository] and must be used
 * from there rather than re-declared — the fork's old datastore carried its own parallel copies of
 * all four, which is how they drifted.
 */
interface ForkPreferencesRepository : UserPreferencesRepository {

    // The server this install talks to is NOT a preference here: it is the main runtime network
    // config, owned and persisted by core-base `NetworkConfigRepository` (read through core/data's
    // `ServerConfigRepository`). A second copy here is how the screen and the client once disagreed.

    /**
     * The signed-in Fineract user. Distinct from [userData], which is the framework's device-level
     * preference record — this is the server's account object (office, staff, roles, permissions).
     */
    val fineractUser: StateFlow<User>

    /**
     * `Basic <key>` for the current [fineractUser], or empty when signed out.
     *
     * Derived rather than stored: Fineract returns the encoded key as part of authentication, so a
     * second copy in preferences could disagree with the user record it came from.
     */
    val authHeader: String

    /** Stores the signed-in Fineract user. */
    suspend fun updateFineractUser(user: User)

    /** Sign out of Fineract, leaving device preferences (theme, language) intact. */
    suspend fun clearFineractUser()
}
