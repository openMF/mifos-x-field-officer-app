/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.dto

import kotlinx.serialization.Serializable

/** Request body for a loan disburse. */
@Serializable
data class LoanDisburseRequestDto(
    /** The date the loan was actually disbursed. */
    val actualDisbursementDate: String,
    /** The transaction amount. */
    val transactionAmount: Double,
    /** The server id of the payment type. */
    val paymentTypeId: Int? = null,
    /** A free-text note. */
    val note: String? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: Int? = null,
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
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
)
