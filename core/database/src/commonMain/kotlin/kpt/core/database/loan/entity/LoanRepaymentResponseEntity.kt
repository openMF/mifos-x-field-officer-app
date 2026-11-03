/*
 * Copyright 2024 Mifos Initiative
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
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.model.objects.Changes

/** Room row for a loan repayment response. */
@Serializable
@DbEntity
@Entity(
    tableName = "LoanRepaymentResponse",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
)
data class LoanRepaymentResponseEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /** The server id of the office. */
    val officeId: Int? = null,
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The server id of the loan. */
    val loanId: Int? = null,
    /** The server id of the resource the request created or changed. */
    val resourceId: Int? = null,
    /** The fields the request changed, as Fineract reports them. */
    val changes: Changes? = null,
)
