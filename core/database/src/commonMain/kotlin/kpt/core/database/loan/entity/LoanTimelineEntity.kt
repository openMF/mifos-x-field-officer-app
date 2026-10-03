/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.database.annotation.DbEntity

@DbEntity
@Entity(
    tableName = "Timeline",
    // NO foreign keys.
    //
    // This entity used to declare one naming ActualDisbursementDateEntity as the PARENT of its own `id`, which inverts the
    // relationship: it made every insert conditional on a timeline's own disbursement date ALREADY existing as a row. Since that
    // detail is an embedded FIELD on this class, no such row is ever written first, so SQLite refused
    // every insert with `787 FOREIGN KEY constraint failed` — and Store5 swallowed the failure, so the
    // screen rendered an empty list rather than an error. Measured on device 2026-10-02: the clients
    // fetch returned 100 rows, the write failed, and the list showed "Nothing here yet".
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
)
@Serializable
data class LoanTimelineEntity(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val loanId: Int? = null,

    val submittedOnDate: List<Int>? = null,

    val submittedByUsername: String? = null,

    val submittedByFirstname: String? = null,

    val submittedByLastname: String? = null,

    val approvedOnDate: List<Int>? = null,

    val approvedByUsername: String? = null,

    val approvedByFirstname: String? = null,

    val approvedByLastname: String? = null,

    val expectedDisbursementDate: List<Int>? = null,

// todo check if its int
    @ColumnInfo(index = true, name = ColumnInfo.INHERIT_FIELD_NAME, typeAffinity = ColumnInfo.UNDEFINED, collate = ColumnInfo.UNSPECIFIED, defaultValue = ColumnInfo.VALUE_UNSPECIFIED)
    @Transient
    val actualDisburseDate: ActualDisbursementDateEntity? = null,

    val actualDisbursementDate: List<Int?>? = null,

    val disbursedByUsername: String? = null,

    val disbursedByFirstname: String? = null,

    val disbursedByLastname: String? = null,

    val closedOnDate: List<Int>? = null,

    val expectedMaturityDate: List<Int>? = null,

    val withdrawnOnDate: List<Int>? = null,
)
