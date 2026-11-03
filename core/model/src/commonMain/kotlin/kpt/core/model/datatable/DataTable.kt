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
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `DataTableEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class DataTable(
    /** The server id of this record. */
    val id: Int = 0,
    /** The application table the data table extends (`m_client`, `m_loan`, …). */
    val applicationTableName: String? = null,
    /** The table's column headers. */
    val columnHeaderData: List<ColumnHeader> = emptyList(),
    /** The registered name of the data table. */
    val registeredTableName: String? = null,
)
