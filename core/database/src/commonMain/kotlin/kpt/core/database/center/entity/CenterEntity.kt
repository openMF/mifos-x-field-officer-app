/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.center.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.client.entity.ClientStatusEntity
import kpt.core.database.loan.entity.LoanTimelineEntity
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
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    val sync: Boolean = false,

    val accountNo: String? = null,

    val name: String? = null,

    val officeId: Int? = null,

    val officeName: String? = null,

    val staffId: Int? = null,

    val staffName: String? = null,

    val hierarchy: String? = null,

    val status: ClientStatusEntity? = null,

    val active: Boolean? = null,

    val centerDate: CenterDateEntity? = null,

    val activationDate: List<Int?> = emptyList(),

    val timeline: LoanTimelineEntity? = null,

    val externalId: String? = null,
)
