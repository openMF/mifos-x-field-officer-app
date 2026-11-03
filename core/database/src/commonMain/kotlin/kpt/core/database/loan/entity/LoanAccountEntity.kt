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
import kpt.core.base.database.annotation.DbEntity
import kpt.core.model.objects.account.loan.Currency

/** Room row for a loan account. */
@DbEntity
@Entity(
    tableName = "LoanAccountEntity",
    foreignKeys = [],
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
)
@Serializable
data class LoanAccountEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    /** The server id of the client. */
    val clientId: Long = 0,

    /** The server id of the group. */
    val groupId: Long = 0,

    /** The server id of the centre. */
    val centerId: Long = 0,

    /** The account number Fineract assigned. */
    val accountNo: String? = null,

    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,

    /** The server id of the product. */
    val productId: Int? = null,

    /** The name of the product. */
    val productName: String? = null,

    /** The currency the amounts are in. */
    val currency: Currency? = null,

    /** The record's Fineract status. */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNDEFINED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val status: LoanStatusEntity? = null,

    /** The loan type (individual, group or JLG). */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val loanType: LoanTypeEntity? = null,

    /** The loan's cycle number for the client. */
    val loanCycle: Int? = null,

    /** True when an installment is overdue. */
    val inArrears: Boolean? = null,

    /** The original loan amount. */
    val originalLoan: Double? = null,

    /** The loan balance. */
    val loanBalance: Double? = null,

    /** The amount already paid. */
    val amountPaid: Double? = null,
)
