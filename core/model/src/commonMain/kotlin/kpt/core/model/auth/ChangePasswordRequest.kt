/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.auth

import kotlinx.serialization.Serializable

/**
 * `POST users/{userId}/pwd` — the signed-in user changes their own password. The server checks the
 * policy AND that the two match (measured on the gateway 2026-10-27), so the screen's own match check
 * only saves a round trip.
 */
@Serializable
data class ChangePasswordRequest(
    /** The password. */
    val password: String,
    /** The new password typed a second time. */
    val repeatPassword: String,
)
