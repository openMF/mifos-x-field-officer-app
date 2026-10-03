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
 * GetGroupsResponse
 *
 * @param pageItems
 * @param totalFilteredRecords
 */

@Serializable
data class GetGroupsResponse(

    val pageItems: Set<GetGroupsPageItems>? = null,

    val totalFilteredRecords: Int? = null,

)
