/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kotlinx.serialization.Serializable
/** One client's attendance on a collection sheet. */
@Serializable
data class ClientsAttendance(
    /** Whether the client attended the meeting. */
    val attendanceType: Int,
    /** The server id of the client. */
    val clientId: Int,
)
