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
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupDateEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class GroupDate(
    /** The server id of the group. */
    val groupId: Long = 0,
    /** The server id of the charge. */
    val chargeId: Long = 0,
    /** The day of the month. */
    val day: Int = 0,
    /** The month (1–12). */
    val month: Int = 0,
    /** The year. */
    val year: Int = 0,
)
