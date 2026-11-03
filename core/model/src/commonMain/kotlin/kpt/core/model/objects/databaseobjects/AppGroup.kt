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
/** An app group. */
@Serializable
data class AppGroup(
    /** The server id of the staff member (loan officer). */
    var staffId: Int = 0,

    /** The staff member's display name. */
    var staffName: String? = null,

    /** The server id of the group level. */
    var levelId: Int = 0,

    /** The name of the group level. */
    var levelName: String? = null,

    /** The server id of the group. */
    var groupId: Long = 0,

    /** The name of the group. */
    var groupName: String? = null,

    /** The server id of the centre. */
    var centerId: Long = 0,

    /** The clients. */
    var clients: List<Client> = ArrayList(),
)
