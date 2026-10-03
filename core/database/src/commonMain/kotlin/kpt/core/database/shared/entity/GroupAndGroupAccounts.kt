/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.shared.entity

import kotlinx.serialization.Serializable
import kpt.core.database.group.entity.GroupAccounts
import kpt.core.database.group.entity.GroupEntity

/**
 * Created by Rajan Maurya on 11/09/16.
 */
@Serializable
data class GroupAndGroupAccounts(
    var group: GroupEntity? = null,
    var groupAccounts: GroupAccounts? = null,
)
