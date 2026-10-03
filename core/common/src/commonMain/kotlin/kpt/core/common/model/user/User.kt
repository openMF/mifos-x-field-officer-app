/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.model.user

import kotlinx.serialization.Serializable

@Serializable
class User {
    var username: String? = null

    var userId = 0

    var base64EncodedAuthenticationKey: String? = null

    var isAuthenticated = false

    var officeId = 0

    var officeName: String? = null

    var roles: List<Role> = emptyList()

    var permissions: List<String> = emptyList()
}
