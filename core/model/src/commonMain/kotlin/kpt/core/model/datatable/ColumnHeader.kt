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
    val id: Int? = null,
    val columnCode: String? = null,
    val columnDisplayType: String? = null,
    val columnLength: Int? = null,
    val dataTableColumnName: String? = null,
    val columnType: String? = null,
    val columnNullable: Boolean? = null,
    val columnPrimaryKey: Boolean? = null,
    val registeredTableName: String? = null,
    val columnValues: List<ColumnValue> = emptyList(),
)
