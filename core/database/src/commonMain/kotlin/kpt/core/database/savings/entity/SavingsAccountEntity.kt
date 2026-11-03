/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity

/** Room row for a savings account. */
@DbEntity
@Entity(
    tableName = "SavingsAccount",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
    foreignKeys = [],
)
@Serializable
data class SavingsAccountEntity(
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

    /** The server id of the product. */
    val productId: Int? = null,

    /** The product's short name. */
    val shortProductName: String? = null,

    /** The name of the product. */
    val productName: String? = null,

    /** The record's Fineract status. */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val status: SavingsAccountStatusEntity? = null,

    /** The currency the amounts are in. */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val currency: SavingAccountCurrencyEntity? = null,

    /** The account balance. */
    val accountBalance: Double? = null,

    /** The deposit product type (savings, fixed deposit or recurring deposit). */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val depositType: SavingAccountDepositTypeEntity? = null,

    /** The date of the last active transaction. */
    val lastActiveTransactionDate: List<Int>? = null,
)
