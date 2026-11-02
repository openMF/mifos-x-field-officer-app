/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.group

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupPayloadEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class GroupPayload(
    val id: Int = 0,
    val errorMessage: String? = null,
    val officeId: Int = 0,
    val active: Boolean = false,
    val activationDate: String? = null,
    val submittedOnDate: String? = null,
    val externalId: String? = null,
    val name: String? = null,
    val locale: String? = null,
    val dateFormat: String? = null,
)
