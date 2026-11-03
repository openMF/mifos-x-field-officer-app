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
import kpt.core.database.payment.entity.PaymentTypeOptionEntity
import kpt.core.database.savings.entity.SavingAccountCurrencyEntity
import kpt.core.model.loan.LoanType

/** Room row for a loan repayment template. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "LoanRepaymentTemplate",
)
data class LoanRepaymentTemplateEntity(
    /** The server id of the loan. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val loanId: Int = 0,

    /** The record's type. */
    val type: LoanType? = null,

    /** The date. */
    val date: List<Int>? = null,

    /** The currency the amounts are in. */
    val currency: SavingAccountCurrencyEntity? = null,

    /** The amount, in the record's currency. */
    val amount: Double? = null,

    /** The part of the transaction that went to principal. */
    val principalPortion: Double? = null,

    /** The part of the transaction that went to interest. */
    val interestPortion: Double? = null,

    /** The part of the transaction that went to fees. */
    val feeChargesPortion: Double? = null,

    /** The part of the transaction that went to penalties. */
    val penaltyChargesPortion: Double? = null,

    /** The payment types the user may choose from. */
    val paymentTypeOptions: List<PaymentTypeOptionEntity>? = null,
)
