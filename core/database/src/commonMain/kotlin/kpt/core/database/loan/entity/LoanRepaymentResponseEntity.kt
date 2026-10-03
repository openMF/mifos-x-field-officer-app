/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.model.objects.Changes

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
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val officeId: Int? = null,
    val clientId: Int? = null,
    val loanId: Int? = null,
    val resourceId: Int? = null,
    val changes: Changes? = null,
)
