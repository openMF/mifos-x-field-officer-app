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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
/** Room row for one client-template option. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "ClientTemplateOptions",
)
data class OptionsEntity(
    /** The kind of option. */
    val optionType: String? = null,

    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** The display name. */
    val name: String = "",

    /** The sort position of this option. */
    val position: Int = 0,

    /** A free-text description. */
    val description: String? = null,

    /** True when the option is active. */
    @SerialName("isActive")
    val activeStatus: Boolean = false,
)
