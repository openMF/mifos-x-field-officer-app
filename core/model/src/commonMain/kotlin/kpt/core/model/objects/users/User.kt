/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.users

import kotlinx.serialization.Serializable
import kpt.core.model.objects.clients.Role

/** A signed-in user. */
@Serializable
data class User(
    /** The username. */
    val username: String? = null,
    /** The password. */
    val password: String? = null,
    /** The server id of the user. */
    val userId: Long = 0,
    /** The Basic-auth key for the session, base64-encoded. */
    val base64EncodedAuthenticationKey: String? = null,
    /** True once the session is authenticated. */
    val isAuthenticated: Boolean = false,
    /** The server id of the office. */
    val officeId: Long = 0,
    /** The name of the office. */
    val officeName: String? = null,
    /** The user's roles. */
    val roles: List<Role> = emptyList(),
    /** The permission codes the user holds. */
    val permissions: List<String> = emptyList(),
)
