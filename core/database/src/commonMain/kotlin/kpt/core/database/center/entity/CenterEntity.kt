/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.center.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.client.entity.ClientStatusEntity
import kpt.core.database.loan.entity.LoanTimelineEntity
/** Room row for a center. */
@DbEntity
@Entity(
    tableName = "Center",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
    // NO foreign keys.
    //
    // This entity used to declare one naming CenterDateEntity as the PARENT of its own `id`, which inverts the
    // relationship: it made every insert conditional on a center's own date block ALREADY existing as a row. Since that
    // detail is an embedded FIELD on this class, no such row is ever written first, so SQLite refused
    // every insert with `787 FOREIGN KEY constraint failed` — and Store5 swallowed the failure, so the
    // screen rendered an empty list rather than an error. Measured on device 2026-10-02: the clients
    // fetch returned 100 rows, the write failed, and the list showed "Nothing here yet".
)
@Serializable
data class CenterEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    /** True when this record is queued to be synced to the server. */
    val sync: Boolean = false,

    /** The account number Fineract assigned. */
    val accountNo: String? = null,

    /** The display name. */
    val name: String? = null,

    /** The server id of the office. */
    val officeId: Int? = null,

    /** The name of the office. */
    val officeName: String? = null,

    /** The server id of the staff member (loan officer). */
    val staffId: Int? = null,

    /** The staff member's display name. */
    val staffName: String? = null,

    /** The office hierarchy path (e.g. `.1.2.`). */
    val hierarchy: String? = null,

    /** The record's Fineract status. */
    val status: ClientStatusEntity? = null,

    /** True when the record is active. */
    val active: Boolean? = null,

    /** The centre's dates. */
    val centerDate: CenterDateEntity? = null,

    /** The date the record was activated. */
    val activationDate: List<Int?> = emptyList(),

    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: LoanTimelineEntity? = null,

    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
)
