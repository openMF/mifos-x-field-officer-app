/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.datatable

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ColumnValue`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class ColumnValue(
    /** The server id of this record. */
    val id: Int? = null,
    /** The value stored in this cell. */
    val value: String? = null,
    /** The score attached to this value. */
    val score: Int? = null,
    /** The registered name of the data table. */
    val registeredTableName: String? = null,
)
