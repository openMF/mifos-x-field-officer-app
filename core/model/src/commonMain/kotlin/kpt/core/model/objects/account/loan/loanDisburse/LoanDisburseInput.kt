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
    val actualDisbursementDate: String,
    val transactionAmount: Double,
    val locale: String,
    val dateFormat: String,
    /** Null when the officer chose none — never `0`, which is not a payment type. */
    val paymentTypeId: Int? = null,
    val note: String? = null,
    val externalId: Int? = null,
    val accountNumber: String? = null,
    val checkNumber: String? = null,
    val routingCode: String? = null,
    val receiptNumber: String? = null,
    val bankNumber: String? = null,
)
