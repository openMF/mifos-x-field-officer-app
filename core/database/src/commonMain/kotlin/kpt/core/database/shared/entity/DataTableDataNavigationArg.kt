/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.shared.entity

import kotlinx.serialization.Serializable
import kpt.core.database.datatable.entity.DataTableEntity

@Serializable
data class DataTableDataNavigationArg(

    val tableName: String,

    val entityId: Int,

    val dataTable: DataTableEntity,
)
