/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
/** Room row for a savings product offered by the client template. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "ClientTemplateSavingProductsOptions",
)
data class SavingProductOptionsEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    /** The display name. */
    val name: String = "",

    /** True when the withdrawal fee also applies to transfers. */
    val withdrawalFeeForTransfers: Boolean = false,

    /** True when the account may be overdrawn. */
    val allowOverdraft: Boolean = false,
)
