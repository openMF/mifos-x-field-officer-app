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

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.payment.entity.PaymentTypeOptionEntity

/** Room row for a savings account transaction template. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "SavingsAccountTransactionTemplate",
)
data class SavingsAccountTransactionTemplateEntity(
    /** The server id of the account. */
    @PrimaryKey(autoGenerate = true)
    val accountId: Int? = null,

    /** The account number Fineract assigned. */
    val accountNo: String? = null,

    /** The date. */
    val date: List<Int> = emptyList(),

    /** True when the transaction was reversed. */
    val reversed: Boolean? = null,

    /** The payment types the user may choose from. */
    val paymentTypeOptions: List<PaymentTypeOptionEntity> = emptyList(),
) {

    /** True when the transaction was reversed; null when the server did not say. */
    fun isReversed(): Boolean? {
        return reversed
    }
}
