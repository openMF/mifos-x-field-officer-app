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
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ColumnHeader`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class ColumnHeader(
    /** The server id of this record. */
    val id: Int? = null,
    /** The code-value list the column takes values from. */
    val columnCode: String? = null,
    /** How the column is displayed (text, date, code lookup, …). */
    val columnDisplayType: String? = null,
    /** The column's maximum length. */
    val columnLength: Int? = null,
    /** The column's name inside the data table. */
    val dataTableColumnName: String? = null,
    /** The column's database type. */
    val columnType: String? = null,
    /** True when the column may be empty. */
    val columnNullable: Boolean? = null,
    /** True when the column is the primary key. */
    val columnPrimaryKey: Boolean? = null,
    /** The registered name of the data table. */
    val registeredTableName: String? = null,
    /** The values the column may take. */
    val columnValues: List<ColumnValue> = emptyList(),
)
