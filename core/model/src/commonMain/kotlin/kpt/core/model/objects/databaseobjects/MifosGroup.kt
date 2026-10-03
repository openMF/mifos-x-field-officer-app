/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.databaseobjects

import kotlinx.serialization.Serializable
@Serializable
data class MifosGroup(
    var staffId: Int = 0,

    var staffName: String? = null,

    var levelId: Int = 0,

    var levelName: String? = null,

    var groupId: Long = 0,

    var groupName: String? = null,

    var centerId: Long = 0,

    var clients: List<Client> = ArrayList(),
)
