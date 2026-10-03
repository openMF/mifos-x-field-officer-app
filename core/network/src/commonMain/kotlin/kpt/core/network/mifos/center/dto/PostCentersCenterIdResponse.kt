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
 * PostCentersCenterIdResponse
 *
 * @param resourceId
 */

@Serializable
data class PostCentersCenterIdResponse(
    val resourceId: Long? = null,
)
