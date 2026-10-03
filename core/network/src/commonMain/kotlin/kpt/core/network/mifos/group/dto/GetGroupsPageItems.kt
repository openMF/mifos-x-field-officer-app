/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.group.dto

import kotlinx.serialization.Serializable

/**
 *
 *
 * @param active
 * @param hierarchy
 * @param id
 * @param name
 * @param officeId
 * @param officeName
 * @param status
 */

@Serializable
data class GetGroupsPageItems(

    val active: Boolean? = null,

    val hierarchy: String? = null,

    val id: Long? = null,

    val name: String? = null,

    val officeId: Long? = null,

    val officeName: String? = null,

    val status: GetGroupsStatus? = null,

)
