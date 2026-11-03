/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.collectionsheet.entity

import kotlinx.serialization.Serializable

/**
 * Created by Tarun on 25-07-2017.
 */
@Serializable
data class GroupCollectionSheet(
    /** The clients. */
    var clients: MutableList<ClientCollectionSheet> = ArrayList(),

    /** The server id of the group. */
    var groupId: Int = 0,

    /** The name of the group. */
    var groupName: String? = null,

    /** The server id of the group level. */
    var levelId: Int = 0,

    /** The name of the group level. */
    var levelName: String? = null,

    /** The server id of the staff member (loan officer). */
    var staffId: Int = 0,

    /** The staff member's display name. */
    var staffName: String? = null,
)
