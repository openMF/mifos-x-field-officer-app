/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.center.dto

import kotlinx.serialization.Serializable

/**
 *
 *
 * @param code
 * @param description
 * @param id
 */

@Serializable
data class GetCentersStatus(

    val code: String? = null,

    val description: String? = null,

    val id: Long? = null,

)
