/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.shared.entity

import kotlinx.serialization.Serializable
import kpt.core.database.datatable.entity.DataTableEntity

/** Navigation arguments for a data-table form. */
@Serializable
data class DataTableDataNavigationArg(

    /** The name of the data table. */
    val tableName: String,

    /** The server id of the entity this record belongs to. */
    val entityId: Int,

    /** The data table to show. */
    val dataTable: DataTableEntity,
)
