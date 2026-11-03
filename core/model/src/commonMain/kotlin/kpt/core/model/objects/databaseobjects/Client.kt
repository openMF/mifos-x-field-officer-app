/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.databaseobjects

import kotlinx.serialization.Serializable
/** A client. */
@Serializable
data class Client(
    /** The server id of the client. */
    var clientId: Int = 0,

    /** The client's display name. */
    var clientName: String? = null,

    /** Whether the client attended the meeting. */
    var attendanceType: AttendanceType? = null,

    /** The group the client belongs to. */
    var mifosGroup: AppGroup? = null,

    /** The loans. */
    val loans: List<Loan> = ArrayList(),
)
