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
/** Room row for a staff member offered by the client template. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "ClientTemplateStaffOptions",
)
data class StaffOptionsEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** The person's first name. */
    val firstname: String = "",

    /** The person's last name. */
    val lastname: String = "",

    /** The display name. */
    val displayName: String = "",

    /** The server id of the office. */
    val officeId: Int = 0,

    /** The name of the office. */
    val officeName: String = "",

    /** True when the staff member is a loan officer. */
    val isLoanOfficer: Boolean = false,

    /** True when the record is active. */
    val isActive: Boolean = false,
)
