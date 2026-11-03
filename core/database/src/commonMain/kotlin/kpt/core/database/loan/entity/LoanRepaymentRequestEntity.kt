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
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity

/** Room row for a loan repayment request. */
@Serializable
@DbEntity
@Entity(
    tableName = "LoanRepaymentRequestEntity",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
)
data class LoanRepaymentRequestEntity(

// TODO(check this out)
//    @PrimaryKey(autoGenerate = true)
//    val id: Int = 0,

    /** When the record was created, in epoch milliseconds. */
    @PrimaryKey(autoGenerate = true)
    val timeStamp: Long = 0,

    /** The server id of the loan. */
    val loanId: Int? = null,

    /** The error from the last failed sync attempt; null when it has not failed. */
    val errorMessage: String? = null,

    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String? = null,

    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String? = null,

    /** The date of the transaction. */
    val transactionDate: String? = null,

    /** The transaction amount. */
    val transactionAmount: String? = null,

    /** The server id of the payment type. */
    val paymentTypeId: String? = null,

    /** A free-text note. */
    val note: String? = null,

    /** The account number the payment was made from or to. */
    val accountNumber: String? = null,

    /** The cheque number of the payment. */
    val checkNumber: String? = null,

    /** The routing code of the payment. */
    val routingCode: String? = null,

    /** The receipt number of the payment. */
    val receiptNumber: String? = null,

    /** The bank number of the payment. */
    val bankNumber: String? = null,
)
