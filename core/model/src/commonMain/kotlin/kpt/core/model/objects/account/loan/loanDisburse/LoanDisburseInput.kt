/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.loanDisburse

/**
 * What the officer is asking Fineract to disburse.
 *
 * ## Why this lives in `core/model` rather than being the network DTO
 *
 * `feature/loan` must not depend on `core:network` — a feature naming a wire type inverts the
 * layering, and the layering is what keeps a screen from being coupled to one API's shape. The
 * command repository therefore takes THIS, and `core/data`'s impl maps it to
 * `LoanDisburseRequestDto`. Same reasoning as [kpt.core.model.objects.account.loan.LoanApproval],
 * and the same fix applied to the seven client commands that used to return Ktor's `HttpResponse`.
 *
 * ## `locale` and `dateFormat` are REQUIRED, deliberately
 *
 * They used to default — and an earlier version of this file defaulted `dateFormat` to the string
 * literal `"dd-MM-yyyy"` while its sibling `locale` referenced a constant. A payload that supplies
 * its own format is exactly how the date/format mismatch kept recurring in this port (four times:
 * `clientClosure`, `clientTransfer`, `loanApproval`, `loanDisbursement`) — the format field was
 * always filled in correctly and the DATE was formatted by some other helper.
 *
 * Required parameters move that decision to the call site that produces the date string, where the
 * two can be read together. Use [kpt.core.model.utils.ApiDateFormatter]: `formatForApi(millis)`
 * with its own `DATE_FORMAT` and `LOCALE`.
 */
data class LoanDisburseInput(
    /** The date the loan was actually disbursed. */
    val actualDisbursementDate: String,
    /** The transaction amount. */
    val transactionAmount: Double,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
    /** Null when the officer chose none — never `0`, which is not a payment type. */
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
)
