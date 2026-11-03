/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.database.annotation.DbEntity

/** Room row for a loan timeline. */
@DbEntity
@Entity(
    tableName = "Timeline",
    // NO foreign keys.
    //
    // This entity used to declare one naming ActualDisbursementDateEntity as the PARENT of its own `id`,
    // which inverts the
    // relationship: it made every insert conditional on a timeline's own disbursement date ALREADY
    // existing as a row. Since that
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
    /** The server id of the loan. */
    @PrimaryKey(autoGenerate = true)
    @Transient
    val loanId: Int? = null,

    /** The date the record was submitted. */
    val submittedOnDate: List<Int>? = null,

    /** The username of whoever submitted the record. */
    val submittedByUsername: String? = null,

    /** The first name of whoever submitted the record. */
    val submittedByFirstname: String? = null,

    /** The last name of whoever submitted the record. */
    val submittedByLastname: String? = null,

    /** The date the record was approved. */
    val approvedOnDate: List<Int>? = null,

    /** The username of whoever approved the record. */
    val approvedByUsername: String? = null,

    /** The first name of whoever approved the record. */
    val approvedByFirstname: String? = null,

    /** The last name of whoever approved the record. */
    val approvedByLastname: String? = null,

    /** The date the loan is expected to be disbursed. */
    val expectedDisbursementDate: List<Int>? = null,

// todo check if its int
    /** The date the loan was actually disbursed. */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    @Transient
    val actualDisburseDate: ActualDisbursementDateEntity? = null,

    /** The date the loan was actually disbursed. */
    val actualDisbursementDate: List<Int?>? = null,

    /** The username of whoever disbursed the loan. */
    val disbursedByUsername: String? = null,

    /** The first name of whoever disbursed the loan. */
    val disbursedByFirstname: String? = null,

    /** The last name of whoever disbursed the loan. */
    val disbursedByLastname: String? = null,

    /** The date the record was closed. */
    val closedOnDate: List<Int>? = null,

    /** The date the loan is expected to mature. */
    val expectedMaturityDate: List<Int>? = null,

    /** The date the application was withdrawn. */
    val withdrawnOnDate: List<Int>? = null,
)
