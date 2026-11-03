/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.transfer

import kotlinx.serialization.Serializable

/**
 * Move money from one account to another.
 *
 * ## Every field is REQUIRED, where all thirteen used to be nullable with null defaults
 *
 * Fineract needs all of them, so a nullable field here could only ever mean "the caller forgot" —
 * and the original's caller resolved four of them with `!!` at submit time precisely because the
 * payload would have accepted nulls silently. Requiring them moves the failure from a 400 (or an
 * NPE) to the compiler.
 *
 * `locale` and `dateFormat` are required for the same reason they are on
 * [kpt.core.model.objects.account.loan.LoanApproval] and
 * [kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseInput]: a payload that defaults its
 * own format is how the date/format mismatch kept recurring in this port. Use
 * [kpt.core.model.utils.ApiDateFormatter].
 *
 * The original's caller happened to pair these correctly — `DateHelper.formattedFullDate` with
 * `Constants.DATE_FORMAT_LONG`, both "dd MMMM yyyy" — which is why this screen's dates worked and
 * four others did not. That was luck held in two unrelated constants, not a guarantee.
 */
@Serializable
data class AccountTransferRequest(
    /** The server id of the office the money leaves. */
    val fromOfficeId: Int,
    /** The server id of the client the money leaves. */
    val fromClientId: Int,
    /** The type of the account the money leaves. */
    val fromAccountType: Int,
    /** The server id of the account the money leaves. */
    val fromAccountId: Int,
    /** The server id of the office the money goes to. */
    val toOfficeId: Int,
    /** The server id of the client the money goes to. */
    val toClientId: Int,
    /** The type of the account the money goes to. */
    val toAccountType: Int,
    /** The server id of the account the money goes to. */
    val toAccountId: Int,
    /** The date of the transfer. */
    val transferDate: String,
    /** The amount to transfer. */
    val transferAmount: Double,
    /** A description of the transfer. */
    val transferDescription: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
)
