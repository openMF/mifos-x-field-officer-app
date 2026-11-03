/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.client.dto

import kotlinx.serialization.Serializable

/** A client classification offered by the client template. */
@Serializable
data class GetClientClassificationOptions(

    /** The server id of this record. */
    val id: Long? = null,

    /** The display name. */
    val name: String? = null,
)
