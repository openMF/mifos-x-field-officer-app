/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.collectionsheet

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupCollectionSheet`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
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
