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

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/** Room row for an actual disbursement date. */
@DbEntity
@Entity(
    tableName = "ActualDisbursementDateEntity",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
)
data class ActualDisbursementDateEntity(
    /** The server id of the loan. */
    @PrimaryKey(autoGenerate = true)
    val loanId: Int? = null,
    /** The year. */
    val year: Int? = null,
    /** The month (1–12). */
    val month: Int? = null,
    /** The day of the month. */
    val date: Int? = null,
)
