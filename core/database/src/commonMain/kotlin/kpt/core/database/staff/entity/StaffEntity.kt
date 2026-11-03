/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.staff.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity

/** Room row for a staff member. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "Staff",
)
data class StaffEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    /** The person's first name. */
    val firstname: String? = null,

    /** The person's last name. */
    val lastname: String? = null,

    /** The mobile phone number. */
    val mobileNo: String? = null,

    /** The display name. */
    val displayName: String? = null,

    /** The server id of the office. */
    val officeId: Int? = null,

    /** The name of the office. */
    val officeName: String? = null,

    /** True when the staff member is a loan officer. */
    val isLoanOfficer: Boolean? = null,

    /** True when the record is active. */
    val isActive: Boolean? = null,
)
