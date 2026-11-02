/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan

/**
 * What the officer is asking Fineract to approve.
 *
 * ## `locale` and `dateFormat` are REQUIRED, deliberately
 *
 * They used to default to `DateConstants.LOCALE` / `DateConstants.DATE_FORMAT` — and that default
 * is precisely how this payload shipped broken: the screen formatted `approvedOnDate` with
 * `DateHelper.getDateAsStringFromLong` ("21-10-2026") while the untouched default declared
 * "dd MMMM yyyy", so Fineract was told to parse a string it cannot parse. The format field was
 * always filled in correctly; the DATE was formatted elsewhere, by a helper that knew nothing
 * about it.
 *
 * The same defect appeared four times in this port (`clientClosure`, `clientTransfer`, here, and
 * `loanDisbursement`). Requiring both fields moves the decision to the call site that produces the
 * date string, where the two can be read together — `ClientCloseRequest`,
 * `ProposeTransferRequest` and
 * [kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseInput] all require theirs for the
 * same reason. Use [kpt.core.model.utils.ApiDateFormatter].
 *
 * A `data class` with `val`s, where this was a plain `class` with `var`s — nothing mutates an
 * approval after constructing it, and the original's mutability bought only the ability to build
 * one field at a time.
 */
data class LoanApproval(
    val approvedOnDate: String,
    val locale: String,
    val dateFormat: String,
    val approvedLoanAmount: String? = null,
    val expectedDisbursementDate: String? = null,
    val note: String? = null,
)
